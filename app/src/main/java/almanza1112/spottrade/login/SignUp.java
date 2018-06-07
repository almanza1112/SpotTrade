package almanza1112.spottrade.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.RegularExpression;
import almanza1112.spottrade.nonActivity.SharedPref;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class SignUp extends Fragment implements View.OnClickListener{
    private static final String TAG = "SignUp";

    // Facebook Login
    private CallbackManager callbackManager;
    private boolean isFacebookClicked;

    // Google Login
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 0;
    //private boolean facebookSignUp, googleSignUp;
    //private String profilePhotoUrl = "", email = "", userID = "", firstName = "", lastName = "";


    private Pattern pattern = Pattern.compile(RegularExpression.EMAIL_PATTERN);

    private TextView tvAddProfilePhoto, tvDeleteProfilePhoto;
    TextInputLayout  tilEmail;
    private TextInputLayout tilFirstName, tilLastName;
    private TextInputEditText tietFirstName, tietLastName, tietEmail;
    private ImageView ivProfilePhoto;
    private String firstName, lastName, email, phoneNumber;
    private final int GALLERY_CODE = 1;
    private final int READ_EXTERNAL_STORAGE_PERMISSION = 2;

    ProgressDialog progressDialog = null;

    private Uri uri = null;
    StorageReference storageReference;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_up, container, false);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getResources().getString(R.string.Sign_Up));

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        progressDialog = new ProgressDialog(getActivity());

        tvAddProfilePhoto = view.findViewById(R.id.tvAddProfilePhoto);
        tvAddProfilePhoto.setOnClickListener(this);
        tvDeleteProfilePhoto = view.findViewById(R.id.tvDeleteProfilePhoto);
        tvDeleteProfilePhoto.setOnClickListener(this);

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);

        tilFirstName = view.findViewById(R.id.tilFirstName);
        tietFirstName = view.findViewById(R.id.tietFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tietLastName = view.findViewById(R.id.tietLastName);

        tilEmail = view.findViewById(R.id.tilEmail);
        tietEmail = view.findViewById(R.id.tietEmail);

        view.findViewById(R.id.fabDone).setOnClickListener(this);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                    Log.e("firebase", "onAuthStateChanged: signed_in " + firebaseUser.getUid());
                }
                else {
                    Log.e("firebase", "onAuthStateChanged: signed_out ");
                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuthStateListener != null){
            firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvAddProfilePhoto:
                /*
                    Checks if permission was NOT granted == ask for permission
                    Else == permission was already granted, proceed to opening gallery
                 */
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        &&
                        ContextCompat.checkSelfPermission(
                                getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED){

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_PERMISSION);
                    /*
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.READ_CONTACTS)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed, we can request the permission.
                        // READ_EXTERNAL_STORAGE_PERMISSION is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                    */
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_CODE);
                }
                break;

            case R.id.tvDeleteProfilePhoto:
                uri = null;
                ivProfilePhoto.setImageBitmap(null);
                tvDeleteProfilePhoto.setVisibility(View.GONE);
                tvAddProfilePhoto.setVisibility(View.VISIBLE);
                break;

            case R.id.bFacebookLogin:
                facebookLogin();
                break;

            case R.id.bGoogleLogin:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;

            case R.id.fabDone:
                firstName = tietFirstName.getText().toString();
                lastName = tietLastName.getText().toString();
                email = tietEmail.getText().toString();
                //phoneNumber = tietPhoneNumber.getText().toString();
                if (validateName() && validateEmail()){
                    addNewUser();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_CODE);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isFacebookClicked){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            tvAddProfilePhoto.setVisibility(View.GONE);
            uri = data.getData();
            ivProfilePhoto.setImageURI(uri);
            tvDeleteProfilePhoto.setVisibility(View.VISIBLE);
        }

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /* FACEBOOK LOGIN */
    // Step 1
    private void facebookLogin(){
        isFacebookClicked = true;
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = new LoginButton(getActivity());
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.performClick();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        //userID = loginResult.getAccessToken().getUserId();
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.e(TAG, "works");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Unable_to_connect_with_Facebook), Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
    }

    // Step 2
    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getInfoFromFacebook(token);
                        }
                        else {
                            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Step 3
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
                                //profilePhotoUrl =  object.getJSONObject("picture").getJSONObject("data").getString("url");
                            }
                            //facebookSignUp = true;
                            //signInFacebookOrGoogle();
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

    /* GOOGLE LOGIN */
    // Step 1
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

    // Step 2
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            firstName = acct.getGivenName();
                            lastName = acct.getFamilyName();
                            email = acct.getEmail();
                            //profilePhotoUrl = acct.getPhotoUrl().toString();
                            //userID = acct.getId();
                            //googleSignUp = true;
                            //signInFacebookOrGoogle();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            //Snackbar.make(findViewById(R.id.login_activity), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateEmail(){
        boolean sitch;
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()){
            sitch = false;
            tilEmail.setError(getResources().getString(R.string.Invalid_email_format));
        }
        else {
            tilEmail.setErrorEnabled(false);
            sitch = true;
        }
        return sitch;
    }

    private boolean validateName(){
        boolean sitch;
        if (firstName.isEmpty()){
            sitch = false;
            tilFirstName.setError(getResources().getString(R.string.Field_cant_be_empty));
        }
        else {
            tilFirstName.setErrorEnabled(false);
            if (lastName.isEmpty()){
                sitch = false;
                tilLastName.setError(getResources().getString(R.string.Field_cant_be_empty));
            }
            else {
                sitch = true;
                tilLastName.setErrorEnabled(false);
            }
        }
        return sitch;
    }

    private void addNewUser(){
        tilEmail.setErrorEnabled(false);
        progressDialog.setTitle(getResources().getString(R.string.Registering));
        progressDialog.setMessage(getResources().getString(R.string.Creating_user));
        progressDialog.setCancelable(false);
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("firstName", firstName);
            jObject.put("lastName", lastName);
            jObject.put("email", email);
            jObject.put("totalRatings", 0);
            jObject.put("overallRating", 0);
            jObject.put("facebookSignUp", false);
            jObject.put("googleSignUp", false);
            jObject.put("firebaseTokenID", FirebaseInstanceId.getInstance().getToken());
            if (!phoneNumber.isEmpty()){
                jObject.put("phoneNumber", phoneNumber);
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

                                firebaseAuth.createUserWithEmailAndPassword(response.getString("email"), response.getString("password")).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_some_features_may_be_unavailable), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), MapsActivity.class));
                                        getActivity().finish();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        // If uri is not empty then that means that user wants to upload image
                                        if (uri != null) {
                                            uploadImageToFirebase(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)));
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            startActivity(new Intent(getActivity(), MapsActivity.class));
                                            getActivity().finish();
                                        }
                                    }
                                });
                            }
                            else if (response.getString("status").equals("fail")){
                                progressDialog.dismiss();
                                String reason = response.getString("reason");
                                if (reason.equals("Email already in use")){
                                    tilEmail.setError(reason);
                                }
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
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

    private void uploadImageToFirebase(String id){
        progressDialog.setMessage(getResources().getString(R.string.Uploading_profile_image));
        StorageReference filePath = storageReference.child("Photos").child(id).child(uri.getLastPathSegment());
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url), downloadUrl.toString());
                uploadDownloadUrl(downloadUrl.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_upload_image), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MapsActivity.class));
            }
        });
    }

    // Uploads download url for profile photo to database
    private void uploadDownloadUrl(String url){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("profilePhotoUrl", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/update/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("uploadDownloadUrl", response +  "");
                progressDialog.dismiss();
                startActivity(new Intent(getActivity(), MapsActivity.class));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }
}