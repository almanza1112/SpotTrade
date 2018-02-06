package almanza1112.spottrade.account.personal;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.RegularExpression;
import almanza1112.spottrade.nonActivity.SharedPref;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 7/29/17.
 */

public class Personal extends Fragment implements View.OnClickListener {
    private TextView tvFistName, tvLastName, tvEmail;
    private ImageView ivProfilePhoto;
    private ProgressBar progressBar;
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
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Personal);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        ivProfilePhoto = (ImageView) view.findViewById(R.id.ivProfilePhoto);
        final ImageView ivEditProfilePhoto = (ImageView) view.findViewById(R.id.ivEditProfilePhoto);
        ivEditProfilePhoto.setOnClickListener(this);
        if (!SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)).isEmpty()){
            Picasso.with(getActivity()).load(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url))).fit().centerCrop().into(ivProfilePhoto);
        }

        tvFistName = (TextView) view.findViewById(R.id.tvFirstName);
        tvFistName.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name)));
        final ImageView ivEditFirstName = (ImageView) view.findViewById(R.id.ivEditFirstName);
        ivEditFirstName.setOnClickListener(this);
        tvLastName = (TextView) view.findViewById(R.id.tvLastName);
        tvLastName.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name)));
        final ImageView ivEditLastName = (ImageView) view.findViewById(R.id.ivEditLastName);
        ivEditLastName.setOnClickListener(this);

        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvEmail.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email)));
        final ImageView ivEditEmail = (ImageView) view.findViewById(R.id.ivEditEmail);
        ivEditEmail.setOnClickListener(this);
        final TextView tvPassword = (TextView) view.findViewById(R.id.tvPassword);
        tvPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tvPassword.setText(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_password)));
        final ImageView ivEditPassword = (ImageView) view.findViewById(R.id.ivEditPassword);
        ivEditPassword.setOnClickListener(this);

        AppCompatActivity actionBar = (AppCompatActivity) getActivity();
        actionBar.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) actionBar.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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

            case R.id.ivEditPassword:
                ChangePassword changePassword = new ChangePassword();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.personal_activity, changePassword);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
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
            // Check if there is an image already
            if (!SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)).isEmpty()){
                // There is an image, proceed to delete it
                StorageReference photoRef = firebaseStorage.getReferenceFromUrl(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)));
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // On success of deletion, proceed to add new image
                        uploadImageToFirebase(data.getData());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                // There is no image, proceed to just uploading it
                uploadImageToFirebase(data.getData());
            }
        }
    }

    private void uploadImageToFirebase(Uri uri){
        ivProfilePhoto.setImageURI(uri);
        StorageReference filePath = storageReference.child("Photos").child(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id))).child(uri.getLastPathSegment());
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url));
                SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url), downloadUrl.toString());
                uploadDownloadUrl(downloadUrl.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_upload_image), Toast.LENGTH_SHORT).show();
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
                try{
                    if (response.getString("status").equals("success")){
                        setSnackBar(getResources().getString(R.string.Profile_photo_updated));
                    }
                    else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
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
        if (SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_photo_url)).isEmpty()){
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

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/delete/photo/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        setSnackBar(getResources().getString(R.string.Profile_photo_deleted));
                    }
                    else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_change_photo), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
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

        final ImageView ivIcon = (ImageView) alertLayout.findViewById(R.id.ivIcon);
        final TextInputLayout tilUpdate = (TextInputLayout) alertLayout.findViewById(R.id.tilUpdate);
        final TextInputEditText tietUpdate = (TextInputEditText) alertLayout.findViewById(R.id.tietUpdate);

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
        }
        tietUpdate.setSelection(tietUpdate.getText().length());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(R.string.Update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // This is empty because onClickListener is implemented below
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
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
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
                            wantToCloseDialog = true;
                            updateField(field, str);
                        } else {
                            tilUpdate.setError(getResources().getString(R.string.Invalid_email_format));
                        }
                        break;
                }
                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
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

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/update/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
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

    private boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void setSnackBar(String snackBarText){
        snackbar = Snackbar.make(getActivity().findViewById(R.id.personal_activity), snackBarText, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
