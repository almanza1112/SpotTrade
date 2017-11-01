package almanza1112.spottrade.account.personal;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 8/1/17.
 */

public class ChangePassword extends Fragment {
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private TextInputEditText tietCurrentPassword, tietNewPassword, tietConfirmNewPassword;
    private ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.change_password, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Change_Password);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        tilCurrentPassword = (TextInputLayout) view.findViewById(R.id.tilCurrentPassword);
        tietCurrentPassword = (TextInputEditText) view.findViewById(R.id.tietCurrentPassword);

        tilNewPassword = (TextInputLayout) view.findViewById(R.id.tilNewPassword);
        tietNewPassword = (TextInputEditText) view.findViewById(R.id.tietNewPassword);

        tilConfirmNewPassword = (TextInputLayout) view.findViewById(R.id.tilConfirmNewPassword);
        tietConfirmNewPassword = (TextInputEditText) view.findViewById(R.id.tietConfirmNewPassword);

        final FloatingActionButton fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateCurrentPassword() && validateNewPassword()){
                    updatePassword();
                }
            }
        });

        return view;
    }

    private boolean validateCurrentPassword(){
        boolean sitch;
        if (!tietCurrentPassword.getText().toString().isEmpty()){
            if (tietCurrentPassword.getText().toString().equals(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_password)))){
                sitch = true;
                tilCurrentPassword.setErrorEnabled(false);
            }
            else {
                sitch = false;
                tilCurrentPassword.setError(getResources().getString(R.string.Incorrect_Password));
            }
        }
        else{
            sitch = false;
            tilCurrentPassword.setError(getResources().getString(R.string.Field_cant_be_empty));
        }
        return sitch;
    }

    private boolean validateNewPassword(){
        boolean sitch;
        if (tietNewPassword.length() > 7){
            tilNewPassword.setErrorEnabled(false);
            Log.e("password", "confirm: " + tietConfirmNewPassword.getText().toString() + "\nnew: " + tietNewPassword.getText().toString());
            if (tietConfirmNewPassword.getText().toString().equals(tietNewPassword.getText().toString())){
                tilConfirmNewPassword.setErrorEnabled(false);
                sitch = true;
            }
            else {
                sitch = false;
                tilConfirmNewPassword.setError(getResources().getString(R.string.Password_doesnt_match));
            }
        }
        else {
            sitch = false;
            tilNewPassword.setError(getResources().getString(R.string.Minimum_8_characters));
        }
        return sitch;
    }


    private void updatePassword() {
        progressBar.setVisibility(View.VISIBLE);
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("password", tietConfirmNewPassword.getText().toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/update/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    progressBar.setVisibility(View.INVISIBLE);
                    if (response.getString("status").equals("success")){
                        SharedPref.removeSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_password));
                        SharedPref.setSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_password), tietConfirmNewPassword.getText().toString());
                        Toast.makeText(getActivity(), getResources().getString(R.string.Password) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
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
