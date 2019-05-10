package almanza1112.spottrade.login;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class LoginActivity extends AppCompatActivity  implements CountryCodes.CountrySelectedListener{
    private ProgressDialog pd = null;
    private String phoneNumber;
    PhoneNumber phoneNumberFrag = new PhoneNumber();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        pd = new ProgressDialog(this);

        findViewById(R.id.bGetStarted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.bottom_in, R.animator.bottom_out, R.animator.bottom_in, R.animator.bottom_out);
                fragmentTransaction.add(R.id.login_activity, phoneNumberFrag);
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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCountrySelected(String countryCode, String countryID) {
        phoneNumberFrag.updateCountry(countryCode, countryID);
    }

    public PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneVerificationCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            pd.dismiss();
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            pd.dismiss();
            Bundle bundle = new Bundle();
            bundle.putString("verificationID", s);
            ConfirmationCode confirmationCode = new ConfirmationCode();
            confirmationCode.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
            fragmentTransaction.add(R.id.phone_number, confirmationCode);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            pd.dismiss();
            if (e.toString().contains("The format of the phone number provided is incorrect")){
                phoneNumberFrag.tilPhoneNumber.setError(getString(R.string.Invalid_number_format));
            } else {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
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
                            login();
                        } else {
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
        } catch (JSONException e) {
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
                                String firebaseTokenID = response.getString("firebaseTokenID");

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

                                if (firebaseTokenID.equals(FirebaseInstanceId.getInstance().getToken())){
                                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                                    finish();
                                } else {
                                    updateFirebaseTokenID();
                                }
                            } else if (response.getString("status").equals("fail")){
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
                        } catch (JSONException e){
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
            protected Map<String, String> getParams() {
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

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/user/update/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
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
}