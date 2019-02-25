package almanza1112.spottrade.navigationMenu.account.personal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.RegularExpression;
import almanza1112.spottrade.nonActivity.SharedPref;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 7/29/17.
 */

public class Personal extends Fragment implements View.OnClickListener {
    private TextView tvFistName;
    private TextView tvLastName;
    private TextView tvEmail;
    private TextView tvPhoneNumber;
    private TextInputLayout tilUpdate;
    private ImageView ivProfilePhoto;
    private ProgressBar progressBar;
    private Uri profileImageUri;
    private Pattern pattern = Pattern.compile(RegularExpression.EMAIL_PATTERN);

    private final int GALLERY_CODE = 1;
    private final int READ_EXTERNAL_STORAGE_PERMISSION = 2;

    StorageReference storageReference;
    FirebaseStorage firebaseStorage;

    private Snackbar snackbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal, container, false);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        int statusBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.status_bar_height)));
        int actionBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.action_bar_height)));
        layoutParams.height = actionBarHeight + statusBarHeight;
        toolbar.setLayoutParams(layoutParams);
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Personal);

        progressBar = view.findViewById(R.id.progressBar);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        view.findViewById(R.id.ivEditProfilePhoto).setOnClickListener(this);
        if (SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)) != null){
            Picasso.get().load(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url))).fit().centerCrop().into(ivProfilePhoto);
        }

        tvFistName = view.findViewById(R.id.tvFirstName);
        tvFistName.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name)));
        view.findViewById(R.id.ivEditFirstName).setOnClickListener(this);

        tvLastName = view.findViewById(R.id.tvLastName);
        tvLastName.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name)));
        view.findViewById(R.id.ivEditLastName).setOnClickListener(this);

        tvEmail = view.findViewById(R.id.tvEmail);
        tvEmail.setText(SharedPref.getSharedPreferences(getActivity(), getString(R.string.logged_in_user_email)));

        tvEmail.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email)));
        view.findViewById(R.id.ivEditEmail).setOnClickListener(this);

        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        setPhoneNumber();
        view.findViewById(R.id.ivEditPhoneNumber).setOnClickListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        if (snackbar != null){
            snackbar.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivEditProfilePhoto:
                ADupdateProfilePhoto();
                break;

            case R.id.ivEditFirstName:
                ADupdateField("firstName");
                break;

            case R.id.ivEditLastName:
                ADupdateField("lastName");
                break;

            case R.id.ivEditEmail:
                ADupdateField("email");
                break;

            case R.id.ivEditPhoneNumber:
                ADupdateField("phoneNumber");
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(false);
        MenuItem filterItem = menu.findItem(R.id.filterMaps);
        filterItem.setVisible(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            progressBar.setVisibility(View.VISIBLE);
            // Convert image to base64
            profileImageUri = data.getData();
            Uri image = data.getData(); //The uri with the location of the file
            if (image != null) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                uploadImage(Base64.encodeToString(byteArray, Base64.DEFAULT));
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            profilePhotoChangedListener = (ProfilePhotoChangedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnItemClickedListener");
        }
    }

    ProfilePhotoChangedListener profilePhotoChangedListener = profilePhotoChangedCallback;

    public interface ProfilePhotoChangedListener {
        void onProfilePhotoChanged(String profilePhotoUrl);
    }

    public static ProfilePhotoChangedListener profilePhotoChangedCallback = new ProfilePhotoChangedListener() {
        @Override
        public void onProfilePhotoChanged(String profilePhotoUrl) {

        }
    };

    private void uploadImage(String encodedImage){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("encodedImage", encodedImage);
            jObject.put("_id", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.URL) + "/user/update/photo", jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        ivProfilePhoto.setImageURI(profileImageUri);
                        SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url));
                        SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url), response.getString("profilePhotoUrl"));
                        profilePhotoChangedListener.onProfilePhotoChanged(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)));
                        setSnackBar(getResources().getString(R.string.Profile_photo_updated));
                    }
                    else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void ADupdateProfilePhoto(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        final CharSequence[] items;
        if (SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)) == null){
            items = new CharSequence[]{getResources().getString(R.string.Add_profile_photo)};
        }
        else {
            items = new CharSequence[]{getResources().getString(R.string.Delete), getResources().getString(R.string.Change_Profile_Photo)};
        }

        alertDialogBuilder.setTitle(getResources().getString(R.string.Profile_Photo));
        alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items.length == 1){
                    //means there is no photo
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            &&
                            ContextCompat.checkSelfPermission(
                                    getActivity(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                READ_EXTERNAL_STORAGE_PERMISSION);
                    }
                    else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_CODE);
                    }
                }
                else if (items.length == 2){
                    //means there is a photo
                    if (which == 0){
                        progressBar.setVisibility(View.VISIBLE);
                        //delete photo
                        StorageReference photoRef = firebaseStorage.getReferenceFromUrl(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)));
                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // On success of deletion, proceed to add new image
                                ivProfilePhoto.setImageBitmap(null);
                                SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url));
                                deleteDownloadUrl();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if (which == 1){
                        //change photo
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                &&
                                ContextCompat.checkSelfPermission(
                                        getActivity(),
                                        Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    READ_EXTERNAL_STORAGE_PERMISSION);
                        }
                        else {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_CODE);
                        }
                    }
                }
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteDownloadUrl(){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("profilePhotoUrl", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/user/delete/photo/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        setSnackBar(getResources().getString(R.string.Profile_photo_deleted));
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void ADupdateField(final String field) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.personal_activity_update_field_alertdialog, null);

        final ImageView ivIcon =  alertLayout.findViewById(R.id.ivIcon);
        tilUpdate = alertLayout.findViewById(R.id.tilUpdate);
        final TextInputEditText tietUpdate = alertLayout.findViewById(R.id.tietUpdate);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);
        String title = getResources().getString(R.string.Update);
        switch (field) {
            case "firstName":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_account_circle_black_24dp));
                title += " " + getResources().getString(R.string.First_Name);
                tietUpdate.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name)));
                tietUpdate.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                break;

            case "lastName":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_account_circle_black_24dp));
                title += " " + getResources().getString(R.string.Last_Name);
                tietUpdate.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name)));
                tietUpdate.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                break;

            case "email":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_email_grey600_24dp));
                title += " " + getResources().getString(R.string.Email);
                tietUpdate.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email)));
                tietUpdate.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;

            case "phoneNumber":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_phone_grey600_24dp));
                title += " " + getResources().getString(R.string.Phone_Number);
                if (SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number)) != null){
                    tietUpdate.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email)));
                }
                tietUpdate.setInputType(InputType.TYPE_CLASS_PHONE |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
        }
        tietUpdate.setSelection(tietUpdate.getText().length());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(R.string.Update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // This is empty because onClickListener is implemented below so that alertDialog
                // remains open if the new parameters fail
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                String str = tietUpdate.getText().toString();
                switch (field) {
                    case "firstName":
                        if (str.length() > 0) {
                            wantToCloseDialog = true;
                            updateField(field, str);
                        } else {
                            tilUpdate.setError(getResources().getString(R.string.Field_cant_be_empty));
                        }
                        break;
                    case "lastName":
                        if (str.length() > 0) {
                            wantToCloseDialog = true;
                            updateField(field, str);
                        } else {
                            tilUpdate.setError(getResources().getString(R.string.Field_cant_be_empty));
                        }
                        break;
                    case "email":
                        if (validateEmail(str)) {
                            tilUpdate.setErrorEnabled(false);
                            wantToCloseDialog = true;
                            updateField(field, str);
                        }
                        break;
                    case "phoneNumber":
                        wantToCloseDialog = true;
                        updateField(field, str);
                        break;
                }
                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
            }
        });
    }

    private void updateField(final String field, final String str) {
        progressBar.setVisibility(View.VISIBLE);
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put(field, str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/user/update/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        String snackBarText = "";
                        switch (field) {
                            case "firstName":
                                SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name));
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name), str);
                                snackBarText = getResources().getString(R.string.First_Name) + " " + getResources().getString(R.string.updated);
                                tvFistName.setText(str);
                                break;
                            case "lastName":
                                SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name));
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name), str);
                                snackBarText = getResources().getString(R.string.Last_Name) + " " + getResources().getString(R.string.updated);
                                tvLastName.setText(str);
                                break;
                            case "email":
                                SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email));
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email), str);
                                snackBarText = getResources().getString(R.string.Email) + " " + getResources().getString(R.string.updated);
                                tvEmail.setText(str);
                                break;
                            case "phoneNumber":
                                SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number));
                                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number), str);
                                snackBarText = getResources().getString(R.string.Email) + " " + getResources().getString(R.string.updated);
                                tvPhoneNumber.setText(str);
                                break;
                        }
                        setSnackBar(snackBarText);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_update_field), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_update_field), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_update_field), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void setPhoneNumber(){
        if (SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number)) != null){
            tvPhoneNumber.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_phone_number)));
        }
        else{
            tvPhoneNumber.setText("--------------");
        }
    }

    private boolean validateEmail(String email) {
        final boolean[] sitch = new boolean[1];
        Matcher matcher = pattern.matcher(email);
        // If email matches the format required, check if the email exists already
        if (matcher.matches()){
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/user/check?email=" + email, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if (response.getString("status").equals("success")){
                            sitch[0] = true;
                        } else if (response.getString("status").equals("fail")){
                            sitch[0] = false;
                            tilUpdate.setError(response.getString("reason"));
                        }
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException e){
                        sitch[0] = false;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    sitch[0] = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                }
            }
            );
            queue.add(jsonObjectRequest);
        } else {
            sitch[0] = false;
        }
        return sitch[0];
    }

    private void setSnackBar(String snackBarText){
        snackbar = Snackbar.make(getActivity().findViewById(R.id.personal_activity), snackBarText, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
