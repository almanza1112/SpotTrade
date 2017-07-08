package almanza1112.spottrade.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.MapsActivity;
import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.RegularExpression;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class LoginSignUp extends Fragment implements View.OnClickListener{
    private Pattern pattern = Pattern.compile(RegularExpression.EMAIL_PATTERN);

    private TextView tvAddProfilePhoto;
    private TextInputLayout tilFirstName, tilLastName, tilEmail, tilPassword, tilConfirmPassword, tilPhoneNumber;
    private TextInputEditText tietFirstName, tietLastName, tietEmail, tietPassword, tietConfirmPassword, tietPhoneNumber;
    private String firstName, lastName, email, password, confirmPassword, phoneNumber;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_sign_up, container, false);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Sign Up");

        tvAddProfilePhoto = (TextView) view.findViewById(R.id.tvAddProfilePhoto);
        tvAddProfilePhoto.setOnClickListener(this);

        tilFirstName = (TextInputLayout) view.findViewById(R.id.tilFirstName);
        tietFirstName = (TextInputEditText) view.findViewById(R.id.tietFirstName);
        tilLastName = (TextInputLayout) view.findViewById(R.id.tilLastName);
        tietLastName = (TextInputEditText) view.findViewById(R.id.tietLastName);

        tilEmail = (TextInputLayout) view.findViewById(R.id.tilEmail);
        tietEmail = (TextInputEditText) view.findViewById(R.id.tietEmail);

        tilPassword = (TextInputLayout) view.findViewById(R.id.tilPassword);
        tietPassword = (TextInputEditText) view.findViewById(R.id.tietPassword);
        tilConfirmPassword = (TextInputLayout) view.findViewById(R.id.tilConfirmPassword);
        tietConfirmPassword = (TextInputEditText) view.findViewById(R.id.tietConfirmPassword);

        tilPhoneNumber = (TextInputLayout) view.findViewById(R.id.tilPhoneNumber);
        tietPhoneNumber = (TextInputEditText) view.findViewById(R.id.tietPhoneNumber);

        FloatingActionButton fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        fabDone.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvAddProfilePhoto:

                break;

            case R.id.fabDone:
                firstName = tietFirstName.getText().toString();
                lastName = tietLastName.getText().toString();
                email = tietEmail.getText().toString();
                password = tietPassword.getText().toString();
                confirmPassword = tietConfirmPassword.getText().toString();
                phoneNumber = tietPhoneNumber.getText().toString();
                if (validateName() && validateEmail() && validatePassword()){
                    addNewUser();
                }
                break;
        }
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

    private boolean validatePassword(){
        boolean sitch;
        if (password.length() < 8){
            sitch = false;
            tilPassword.setError(getResources().getString(R.string.Minimum_8_characters));
        }
        else {
            tilPassword.setErrorEnabled(false);
            if (!confirmPassword.equals(password)){
                sitch = false;
                tilConfirmPassword.setError(getResources().getString(R.string.Password_doesnt_match));
            }
            else {
                sitch = true;
                tilConfirmPassword.setErrorEnabled(false);
            }
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

    //private boolean checkIfEmailExists(){}

    private void addNewUser(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("firstName", firstName);
            jObject.put("lastName", lastName);
            jObject.put("email", email);
            jObject.put("password", password);
            jObject.put("phoneNumber", phoneNumber);
            jObject.put("totalRatings", 0);
            jObject.put("overallRating", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() +"/user", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(response.has("firstName")){
                            try {
                                SharedPref.setID(getActivity() ,response.getString("_id"));
                                SharedPref.setFirstName(getActivity(), response.getString("firstName"));
                                SharedPref.setLastName(getActivity(), response.getString("lastName"));
                                SharedPref.setEmail(getActivity(), response.getString("email"));
                                SharedPref.setPassword(getActivity(), response.getString("password"));
                                SharedPref.setPhoneNumber(getActivity(), response.getString("phoneNumber"));
                                SharedPref.setTotalRatings(getActivity(), response.getString("totalRatings"));
                                SharedPref.setOverallRating(getActivity(), response.getString("overallRating"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        startActivity(new Intent(getActivity(), MapsActivity.class));
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
}
