package almanza1112.spottrade.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
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
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText tietEmail, tietPassword;

    private FirebaseAuth firebaseAuth;
    private static final String TAG = "LoginActivity";

    private CallbackManager callbackManager;

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 0;

    private String profilePhotoUrl = "", email = "", userID = "", firstName = "", lastName = "";
    private boolean facebookSignUp, googleSignUp, isFacebookClicked;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        setContentView(R.layout.login_activity);

        tilEmail = findViewById(R.id.tilEmail);
        tietEmail = findViewById(R.id.tietEmail);

        tilPassword = findViewById(R.id.tilPassword);
        tietPassword = findViewById(R.id.tietPassword);

        findViewById(R.id.bSignIn).setOnClickListener(this);
        findViewById(R.id.bForgotPassword).setOnClickListener(this);
        findViewById(R.id.bFacebookLogin).setOnClickListener(this);
        findViewById(R.id.bGoogleLogin).setOnClickListener(this);
        findViewById(R.id.bCreateSpotTradeAccount).setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isFacebookClicked){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bSignIn:
                tilEmail.setErrorEnabled(false);
                tilPassword.setErrorEnabled(false);
                signInSpotTrade();
                break;
            case R.id.bForgotPassword:
                // TODO: need to implement this at a later time
                break;

            case R.id.bFacebookLogin:
                facebookLogin();
                break;

            case R.id.bGoogleLogin:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;

            case R.id.bCreateSpotTradeAccount:
                LoginSignUp loginSignUp = new LoginSignUp();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
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

    private void facebookLogin(){
        isFacebookClicked = true;
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = new LoginButton(this);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.performClick();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        userID = loginResult.getAccessToken().getUserId();
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.e(TAG, "works");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.Unable_to_connect_with_Facebook), Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getInfoFromFacebook(token);
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getInfoFromFacebook(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String facebookName = object.getString("name");
                            String[] names = facebookName.split(" ");
                            for(int i = 0; i < names.length - 1; i++){
                                firstName += names[i];
                                if (i != names.length - 2){
                                    firstName += " ";
                                }
                            }
                            lastName = names[names.length - 1];
                            if (object.has("picture")){
                                profilePhotoUrl =  object.getJSONObject("picture").getJSONObject("data").getString("url");
                            }
                            facebookSignUp = true;
                            signInFacebookOrGoogle();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Sign in successful
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        }
        catch (ApiException e) {
            // Sign in failed
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            firstName = acct.getGivenName();
                            lastName = acct.getFamilyName();
                            email = acct.getEmail();
                            profilePhotoUrl = acct.getPhotoUrl().toString();
                            userID = acct.getId();
                            googleSignUp = true;
                            signInFacebookOrGoogle();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(findViewById(R.id.login_activity), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
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
}