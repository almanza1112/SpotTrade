package almanza1112.spottrade.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

    // Facebook Login
    private CallbackManager callbackManager;
    private boolean isFacebookClicked;

    // Google Login
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 0;
    //private boolean facebookSignUp, googleSignUp;
    //private String profilePhotoUrl = "", email = "", userID = "", firstName = "", lastName = "";


    private Pattern pattern = Pattern.compile(RegularExpression.EMAIL_PATTERN);

    private Button bAddProfilePhoto, bDeleteProfilePhoto;
    private TextInputLayout tilFirstName, tilLastName, tilEmail;
    private TextInputEditText tietFirstName, tietLastName, tietEmail;
    private ImageView ivProfilePhoto;
    private String firstName, lastName, email, phoneNumber, profilePhotoUrl = "";
    private final int GALLERY_CODE = 1;
    private final int READ_EXTERNAL_STORAGE_PERMISSION = 2;
    private boolean isGallerySelected;

    ProgressDialog progressDialog = null;

    private Uri uri = null;
    StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_up, container, false);

        phoneNumber = getArguments().getString("phoneNumber");

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

        bAddProfilePhoto = view.findViewById(R.id.bAddProfilePhoto);
        bAddProfilePhoto.setOnClickListener(this);
        bDeleteProfilePhoto = view.findViewById(R.id.bDeleteProfilePhoto);
        bDeleteProfilePhoto.setOnClickListener(this);

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);

        tilFirstName = view.findViewById(R.id.tilFirstName);
        tietFirstName = view.findViewById(R.id.tietFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tietLastName = view.findViewById(R.id.tietLastName);

        tilEmail = view.findViewById(R.id.tilEmail);
        tietEmail = view.findViewById(R.id.tietEmail);

        TextView tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvPhoneNumber.setText(phoneNumber);

        view.findViewById(R.id.bFacebookLogin).setOnClickListener(this);
        view.findViewById(R.id.bGoogleLogin).setOnClickListener(this);

        view.findViewById(R.id.fabDone).setOnClickListener(this);

        storageReference = FirebaseStorage.getInstance().getReference();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bAddProfilePhoto:
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

            case R.id.bDeleteProfilePhoto:
                uri = null;
                profilePhotoUrl = null;
                ivProfilePhoto.setImageBitmap(null);
                bDeleteProfilePhoto.setVisibility(View.GONE);
                bAddProfilePhoto.setVisibility(View.VISIBLE);
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
                boolean nameBool = validateName();
                boolean emailBool = validateEmail();
                if (nameBool && emailBool){
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
        super.onActivityResult(requestCode, resultCode, data);
        if (isFacebookClicked){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            isGallerySelected = true;
            bAddProfilePhoto.setVisibility(View.GONE);
            uri = data.getData();
            ivProfilePhoto.setImageURI(uri);
            bDeleteProfilePhoto.setVisibility(View.VISIBLE);
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
                        // These commented out lines are no longer needed for new sign in flow
                        // keeping them around for future
                        //userID = loginResult.getAccessToken().getUserId();
                        //handleFacebookAccessToken(loginResult.getAccessToken());
                        getInfoFromFacebook(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Unable_to_connect_with_Facebook), Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
    }

    // Step 2
    /*
    // We no longer need step 2 because of different sign in process that only involves phone number
    // keeping around just in case for the future
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
    */

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
                            tietFirstName.setText(firstName);
                            tietLastName.setText(lastName);
                            if (object.has("picture")){
                                profilePhotoUrl =  object.getJSONObject("picture").getJSONObject("data").getString("url");
                                Picasso.get().load(profilePhotoUrl).into(ivProfilePhoto);
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
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
            isGallerySelected = false;
            //firebaseAuthWithGoogle(account);
            firstName = account.getGivenName();
            lastName = account.getFamilyName();
            email = account.getEmail();
            profilePhotoUrl = account.getPhotoUrl().toString();

            tietFirstName.setText(firstName);
            tietLastName.setText(lastName);
            tietEmail.setText(email);
            Picasso.get().load(profilePhotoUrl).into(ivProfilePhoto);
        }
        catch (ApiException e) {
            // Sign in failed
        }
    }

    // Step 2
    /*
    // We no longer need step 2 because of different sign in process that only involves phone number
    // keeping around just in case for the future
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
    */

    public File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File mFileTemp = null;
        String root=getActivity().getDir("my_sub_dir", Context.MODE_PRIVATE).getAbsolutePath();
        File myDir = new File(root + "/Img");
        if(!myDir.exists()){
            myDir.mkdirs();
        }
        try {
            mFileTemp= File.createTempFile(imageFileName,".jpg",myDir.getAbsoluteFile());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return mFileTemp;
    }

    private Uri getUri(){
        File file = createImageFile();
            FileOutputStream fout;
            try {
                fout = new FileOutputStream(file);
                BitmapDrawable bitmapDrawable = ((BitmapDrawable) ivProfilePhoto.getDrawable());
                Bitmap bitmap = bitmapDrawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, fout);
                fout.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Uri.fromFile(file);

    }

    private boolean validateEmail(){
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()){
            tilEmail.setError(getResources().getString(R.string.Invalid_email_format));
            return false;
        }
        else {
            tilEmail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateName(){
        boolean firstName = validateFirstName();
        boolean lastName = validateLastName();
        return firstName && lastName;
    }

    private boolean validateFirstName(){
        if (firstName.isEmpty()){
            tilFirstName.setError(getResources().getString(R.string.Field_cant_be_empty));
            return false;
        } else {
            tilFirstName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateLastName(){
        if (lastName.isEmpty()){
            tilLastName.setError(getResources().getString(R.string.Field_cant_be_empty));
            return false;
        }
        else {
            tilLastName.setErrorEnabled(false);
            return true;
        }
    }

    private void addNewUser(){
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
            jObject.put("firebaseTokenID", FirebaseInstanceId.getInstance().getToken());
            jObject.put("phoneNumber", phoneNumber);

        } catch (JSONException e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, getString(R.string.URL) +"/user/create", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                String id = response.getString("_id");
                                String email = response.getString("email");
                                String firstName = response.getString("firstName");
                                String lastName = response.getString("lastName");
                                String totalRatings = response.getString("totalRatings");
                                String overallRating = response.getString("overallRating");
                                String phoneNumber = response.getString("phoneNumber");

                                if (response.has("profilePhotoUrl")){
                                    SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url), response.getString("profilePhotoUrl"));
                                }

                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number), phoneNumber);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id), id);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email), email);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name), firstName);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name), lastName);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_total_ratings), totalRatings);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_overall_rating), overallRating);

                                if (uri != null || profilePhotoUrl != null) {
                                    Uri newUri;
                                    if (isGallerySelected){
                                        newUri = uri;
                                    } else {
                                        newUri = getUri();
                                    }
                                    uploadImageToFirebase(newUri, SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)));
                                }
                                else{
                                    progressDialog.dismiss();
                                    startActivity(new Intent(getActivity(), MapsActivity.class));
                                    getActivity().finish();
                                }
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

    private void uploadImageToFirebase(Uri uri, String id){
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
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/update/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                startActivity(new Intent(getActivity(), MapsActivity.class));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }
}