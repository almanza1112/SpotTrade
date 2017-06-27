package almanza1112.spottrade.login;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPref.getEmail(this).length() > 0){
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
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void authenticateUserLogin(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, httpConnection.htppConnectionURL() +"/user/login?email=" + tietEmail.getText().toString() + "&password=" + tietPassword.getText().toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                            Log.e("loginResponse", response + "");
                            try{
                                if (response.getString("status").equals("success")) {
                                    String id = response.getString("_id");
                                    String email = response.getString("email");
                                    String firstName = response.getString("firstName");
                                    String lastName = response.getString("lastName");
                                    String password = response.getString("password");
                                    if (response.has("phoneNumber")){
                                        String phoneNumber = response.getString("phoneNumber");
                                        SharedPref.setPhoneNumber(LoginActivity.this, phoneNumber);
                                    }

                                    SharedPref.setID(LoginActivity.this, id);
                                    SharedPref.setEmail(LoginActivity.this, email);
                                    SharedPref.setFirstName(LoginActivity.this, firstName);
                                    SharedPref.setLastName(LoginActivity.this, lastName);
                                    SharedPref.setPassword(LoginActivity.this, password);
                                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                                    finish();
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
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }
}
