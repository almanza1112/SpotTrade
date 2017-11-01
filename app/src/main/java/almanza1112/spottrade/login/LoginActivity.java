package almanza1112.spottrade.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    TextInputLayout tilEmail, tilPassword;
    TextInputEditText tietEmail, tietPassword;

    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)) != null){
            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
            finish();
        }
        setContentView(R.layout.login_activity);

        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        tietEmail = (TextInputEditText) findViewById(R.id.tietEmail);

        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        tietPassword = (TextInputEditText) findViewById(R.id.tietPassword);

        final TextView tvSignUp = (TextView) findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(this);

        final Button bLogin = (Button) findViewById(R.id.bLogin);
        bLogin.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bLogin:
                tilEmail.setErrorEnabled(false);
                tilPassword.setErrorEnabled(false);
                authenticateUserLogin();
                break;
            case R.id.tvSignUp:
                LoginSignUp loginSignUp = new LoginSignUp();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.login_activity, loginSignUp);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            default:
                Toast.makeText(this, "onClick not implemented for this", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void authenticateUserLogin(){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("email", tietEmail.getText().toString());
            jObject.put("password", tietPassword.getText().toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() + "/user/login", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                            try{
                                if (response.getString("status").equals("success")) {
                                    String id = response.getString("_id");
                                    String email = response.getString("email");
                                    String firstName = response.getString("firstName");
                                    String lastName = response.getString("lastName");
                                    String password = response.getString("password");
                                    String totalRatings = response.getString("totalRatings");
                                    String overallRating = response.getString("overallRating");

                                    if (response.has("phoneNumber")){
                                        String phoneNumber = response.getString("phoneNumber");
                                        SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_phone_number), phoneNumber);
                                    }
                                    if (response.has("profilePhotoUrl")){
                                        SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_photo_url), response.getString("profilePhotoUrl"));
                                    }

                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_id), id);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_email), email);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_first_name), firstName);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_last_name), lastName);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_password), password);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_total_ratings), totalRatings);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_overall_rating), overallRating);

                                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.Error_some_features_may_be_unavailable), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                                            finish();
                                        }
                                    });
                                }
                                else if (response.getString("status").equals("fail")){
                                    String reason = response.getString("reason");
                                    if (reason.contains("Email")){
                                        tilEmail.setError(reason);
                                    }
                                    else if (reason.contains("Password")){
                                        tilPassword.setError(reason);
                                    }
                                }
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }
}
