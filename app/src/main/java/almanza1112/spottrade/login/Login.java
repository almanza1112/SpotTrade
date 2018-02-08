package almanza1112.spottrade.login;

import android.app.Fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class Login extends Fragment implements View.OnClickListener{
    TextInputLayout tilEmail, tilPassword;
    TextInputEditText tietEmail, tietPassword;

    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_activity, container, false);
        tilEmail = (TextInputLayout) view.findViewById(R.id.tilEmail);
        tietEmail = (TextInputEditText) view.findViewById(R.id.tietEmail);

        tilPassword = (TextInputLayout) view.findViewById(R.id.tilPassword);
        tietPassword = (TextInputEditText) view.findViewById(R.id.tietPassword);

        final TextView tvSignUp = (TextView) view.findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(this);

        final Button bLogin = (Button) view.findViewById(R.id.bLogin);
        bLogin.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        return view;
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
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.login_activity, loginSignUp);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            default:
                Toast.makeText(getActivity(), "onClick not implemented for this", Toast.LENGTH_SHORT).show();
                break;
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
        RequestQueue queue = Volley.newRequestQueue(getActivity());

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
                                        SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number), phoneNumber);
                                    }
                                    if (response.has("profilePhotoUrl")){
                                        SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url), response.getString("profilePhotoUrl"));
                                    }

                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id), id);
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email), email);
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name), firstName);
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name), lastName);
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_password), password);
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_total_ratings), totalRatings);
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_overall_rating), overallRating);

                                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.Error_some_features_may_be_unavailable), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            //TODO: put interface here

                                            //startActivity(new Intent(getActivity() MapsActivity.class));
                                            //finish();
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
