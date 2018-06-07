package almanza1112.spottrade.login;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class LoginActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
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

    /**
    private void signInSpotTrade(){
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
                                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        updateFirebaseTokenID();
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