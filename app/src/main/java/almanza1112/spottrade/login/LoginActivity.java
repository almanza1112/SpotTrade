package almanza1112.spottrade.login;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class LoginActivity extends AppCompatActivity{
    private ProgressDialog pd = null;
    private String phoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        pd = new ProgressDialog(this);

        findViewById(R.id.bGetStarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneNumber phoneNumber = new PhoneNumber();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.login_activity, phoneNumber);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
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

    public PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneVerificationCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.e("phoneAuth", "onVerificationCompleted");
            pd.dismiss();
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.e("phoneAuth", "onCodeSent");
            pd.dismiss();
            Bundle bundle = new Bundle();
            bundle.putString("verificationID", s);
            ConfirmationCode confirmationCode = new ConfirmationCode();
            confirmationCode.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.phone_number, confirmationCode);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // TODO: handle this
            pd.dismiss();
            e.printStackTrace();
        }
    };

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pd.setTitle(R.string.Confirming);
        pd.setMessage(getString(R.string.Signing_in));
        pd.setCancelable(false);
        pd.show();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("signin", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            login();
                        } else {
                            Log.w("signin", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                pd.dismiss();
                                // The verification code entered was invalid
                                // Alert dialog??
                            }
                        }
                    }
                });
    }

    public void setPD(){
        pd.setTitle(R.string.Confirming);
        pd.setMessage(getString(R.string.Sending_confirmation_code));
        pd.setCancelable(false);
        pd.show();
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    private void login(){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("phoneNumber", phoneNumber);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, getString(R.string.URL) + "/user/login", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")) {
                                String id = response.getString("_id");
                                String phoneNumber = response.getString("phoneNumber");
                                String email = response.getString("email");
                                String firstName = response.getString("firstName");
                                String lastName = response.getString("lastName");
                                String totalRatings = response.getString("totalRatings");
                                String overallRating = response.getString("overallRating");

                                if (response.has("profilePhotoUrl")){
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_photo_url), response.getString("profilePhotoUrl"));
                                }

                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_id), id);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_phone_number), phoneNumber);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_email), email);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_first_name), firstName);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_last_name), lastName);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_total_ratings), totalRatings);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_overall_rating), overallRating);

                                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                                finish();
                            }
                            else if (response.getString("status").equals("fail")){
                                String reason = response.getString("reason");
                                if (reason.contains("User does not exist")){
                                    // If phone number does not exist in DB, proceed to SignUp
                                    pd.dismiss();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("phoneNumber", phoneNumber);
                                    SignUp signUp = new SignUp();
                                    signUp.setArguments(bundle);
                                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                    fragmentTransaction.replace(R.id.login_activity, signUp);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
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

        queue.add(jsObjRequest);
    }

    /**

    private void signInFacebookOrGoogle(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("firstName", firstName);
            jObject.put("lastName", lastName);
            jObject.put("facebookSignUp", facebookSignUp);
            jObject.put("googleSignUp", googleSignUp);
            jObject.put("totalRatings", 0);
            jObject.put("overallRating", 0);
            jObject.put("profilePhotoUrl", profilePhotoUrl);
            jObject.put("firebaseTokenID", FirebaseInstanceId.getInstance().getToken());
            if (facebookSignUp){
                jObject.put("facebookUserID", userID);
            }
            else if (googleSignUp){
                jObject.put("email", email);
                jObject.put("googleUserID", userID);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() +"/user/create", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                String id = response.getString("_id");
                                String firstName = response.getString("firstName");
                                String lastName = response.getString("lastName");
                                String totalRatings = response.getString("totalRatings");
                                String overallRating = response.getString("overallRating");

                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_id), id);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_first_name), firstName);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_last_name), lastName);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_total_ratings), totalRatings);
                                SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_overall_rating), overallRating);

                                if (facebookSignUp){
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_facebookSignUp), Boolean.toString(true));
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_facebookUserID), userID);
                                }

                                if (googleSignUp){
                                    String email = response.getString("email");
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_email), email);
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_facebookSignUp), Boolean.toString(true));
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_googleUserID), userID);
                                }

                                if (response.has("password")) {
                                    String password = response.getString("password");
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_password), password);
                                }

                                if (response.has("phoneNumber")){
                                    String phoneNumber = response.getString("phoneNumber");
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_phone_number), phoneNumber);
                                }

                                if (response.has("profilePhotoUrl")){
                                    SharedPref.setSharedPreferences(LoginActivity.this, getResources().getString(R.string.logged_in_user_photo_url), response.getString("profilePhotoUrl"));
                                }

                                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                                finish();
                            }
                            else if (response.getString("status").equals("fail")){
                                String reason = response.getString("reason");
                                if (reason.equals("Email already in use")){
                                    tilEmail.setError(reason);
                                }
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
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

        queue.add(jsObjRequest);
    }

    private void updateFirebaseTokenID(){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("firebaseTokenID", FirebaseInstanceId.getInstance().getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/update/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

     **/
}