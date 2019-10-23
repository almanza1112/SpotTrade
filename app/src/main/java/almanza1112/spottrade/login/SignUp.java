package almanza1112.spottrade.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;
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
    private String firstName, lastName, email, phoneNumber;
    private final int GALLERY_CODE = 1;
    private final int READ_EXTERNAL_STORAGE_PERMISSION = 2;

    ProgressDialog progressDialog = null;

    StorageReference storageReference;

    //private static final String TAG = "SignUp";

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
                .requestIdToken("88729508985-s7iphrjb0nk6hta99oq059r4elge18lm.apps.googleusercontent.com")
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
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
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Photo"), GALLERY_CODE);
                }
                break;

            case R.id.bDeleteProfilePhoto:
                ivProfilePhoto.setImageDrawable(null);
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
                progressDialog.setTitle(getResources().getString(R.string.Registering));
                progressDialog.setMessage(getResources().getString(R.string.Creating_user));
                progressDialog.setCancelable(false);
                progressDialog.show();
                firstName = tietFirstName.getText().toString();
                lastName = tietLastName.getText().toString();
                email = tietEmail.getText().toString();
                boolean profilePhotoBool = validateProfilePhoto();
                boolean nameBool = validateName();
                boolean emailBool = validateEmail();
                if (nameBool && emailBool && profilePhotoBool){
                    new BitmapCompression().execute();
                } else {
                    progressDialog.dismiss();
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
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
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
            bAddProfilePhoto.setVisibility(View.GONE);
            bAddProfilePhoto.setTextColor(getResources().getColor(R.color.colorAccent));
            bAddProfilePhoto.setError(null);

            bDeleteProfilePhoto.setVisibility(View.VISIBLE);
            Picasso.get().load(data.getData()).into(ivProfilePhoto);
            //ivProfilePhoto.setImageURI(data.getData());
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
                                Picasso.get().load(object.getJSONObject("picture").getJSONObject("data").getString("url")).into(ivProfilePhoto);
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
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Sign in successful
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            //firebaseAuthWithGoogle(account);
            firstName = account.getGivenName();
            lastName = account.getFamilyName();
            email = account.getEmail();

            tietFirstName.setText(firstName);
            tietLastName.setText(lastName);
            tietEmail.setText(email);
            Picasso.get().load(account.getPhotoUrl().toString()).into(ivProfilePhoto);
        }
        catch (ApiException e) {
            // Sign in failed
            Log.e("GoogleSignIn", e + "");
        }
    }

    public File createImageFile() {
        String imageFileName = "profilePhoto";
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

    private File getFile(){
        File file = createImageFile();
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(file);
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) ivProfilePhoto.getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public Bitmap reduceBitmapSize(File file){
        try {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 50;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            //Log.e(TAG, "sizeAfterReduction: " + BitmapCompat.getAllocationByteCount(selectedBitmap));

            // here i override the original image file
            // commenting this out because i do not need it, taking different approach
            // might come in handy in the future
            /*
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file
            */

            return selectedBitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateProfilePhoto(){
        if (ivProfilePhoto.getDrawable() != null){
            return true;
        } else {
            bAddProfilePhoto.setTextColor(getResources().getColor(R.color.bt_error_red));
            bAddProfilePhoto.setError(getString(R.string.Profile_photo_must_be_added));
            return false;
        }
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

    private void addNewUser(byte[] byteArray){
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
            jObject.put("encodedImage", Base64.encodeToString(byteArray, Base64.DEFAULT));

        } catch (JSONException e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, getString(R.string.URL) +"/user/create", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("response", response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                String id = response.getString("_id");
                                String email = response.getString("email");
                                String firstName = response.getString("firstName");
                                String lastName = response.getString("lastName");
                                String totalRatings = response.getString("totalRatings");
                                String overallRating = response.getString("overallRating");
                                String phoneNumber = response.getString("phoneNumber");
                                String profilePhotoUrl = response.getString("profilePhotoUrl");

                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number), phoneNumber);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id), id);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email), email);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name), firstName);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name), lastName);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_total_ratings), totalRatings);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_overall_rating), overallRating);
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url), profilePhotoUrl);

                                progressDialog.dismiss();
                                startActivity(new Intent(getActivity(), MapsActivity.class));
                                getActivity().finish();
                            } else if (response.getString("status").equals("fail")){
                                progressDialog.dismiss();
                                String reason = response.getString("reason");
                                if (reason.equals("Email already in use")){
                                    tilEmail.setError(reason);
                                }
                            }
                        }
                        catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
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

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private class BitmapCompression extends AsyncTask<Void, Void, Void>
    {
        byte[] byteArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
        }

        @Override
        protected Void doInBackground(Void... params) {
            //this method will be running on background thread so don't update UI from here
            //do your long running http tasks here,you don't want to pass argument and u can access the parent class' variable url over here
            float photoSize = BitmapCompat.getAllocationByteCount((((BitmapDrawable)ivProfilePhoto.getDrawable()).getBitmap()));
            //Log.e("IMAGE_SIZE", "SIZE: "+BitmapCompat.getAllocationByteCount((((BitmapDrawable)ivProfilePhoto.getDrawable()).getBitmap())));
            Bitmap bitmap;

            if (photoSize > 200000){
                bitmap = reduceBitmapSize(getFile());
            } else {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) ivProfilePhoto.getDrawable();
                bitmap = bitmapDrawable.getBitmap();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100, outputStream);
            byteArray = outputStream.toByteArray();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //this method will be running on UI thread
            addNewUser(byteArray);
        }

    }

}