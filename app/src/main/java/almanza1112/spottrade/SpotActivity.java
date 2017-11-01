package almanza1112.spottrade;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.account.payment.AddPaymentMethod;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 6/29/17.
 */

public class SpotActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvLocationName, tvLocationAddress, tvAddLocation;
    private TextInputLayout tilPrice;
    private TextInputEditText tietDescription, tietPrice;
    private CheckBox cbBids;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private double latitude, longitude;
    private String locationName, locationAddress, type;

    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        locationName = intent.getStringExtra("locationName");
        locationAddress = intent.getStringExtra("locationAddress");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        setContentView(R.layout.spot_actiivty);

        pd = new ProgressDialog(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (type.equals("Request")){
            toolbar.setTitle(R.string.Request_a_Spot);
        }
        else if (type.equals("Sell")){
            toolbar.setTitle(R.string.Sell_a_Spot);
        }

        tvLocationName = (TextView) findViewById(R.id.tvLocationName);
        tvLocationName.setOnClickListener(this);
        tvLocationAddress = (TextView) findViewById(R.id.tvLocationAddress);
        tvLocationAddress.setOnClickListener(this);
        tvAddLocation = (TextView) findViewById(R.id.tvAddLocation);
        tvAddLocation.setOnClickListener(this);

        if (locationName.equals("empty")){
            tvLocationName.setVisibility(View.GONE);
            tvLocationAddress.setVisibility(View.GONE);
            tvAddLocation.setVisibility(View.VISIBLE);
        }
        else{
            tvLocationName.setText(locationName);
            tvLocationAddress.setText(locationAddress);
        }

        tietDescription = (TextInputEditText) findViewById(R.id.tietDescription);
        tilPrice = (TextInputLayout) findViewById(R.id.tilPrice);
        tietPrice = (TextInputEditText) findViewById(R.id.tietPrice);
        cbBids = (CheckBox) findViewById(R.id.cbBids);

        final FloatingActionButton fabDone = (FloatingActionButton) findViewById(R.id.fabDone);
        fabDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabDone:
                if (validatePrice()) {
                    pd.setTitle(R.string.Adding_Spot);
                    pd.setCancelable(false);
                    pd.show();
                    if (type.equals("Request")){
                        validatePaymentMethod();
                    }
                    else {
                        postRequest();
                    }
                }
                break;
            default:
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Place place = PlaceAutocomplete.getPlace(this, data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                locationName = place.getName().toString();
                locationAddress = place.getAddress().toString();

                tvLocationName.setText(locationName);
                tvLocationAddress.setText(locationAddress);
                tvAddLocation.setVisibility(View.GONE);
                tvLocationName.setVisibility(View.VISIBLE);
                tvLocationAddress.setVisibility(View.VISIBLE);
            }
            else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
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

    private boolean validatePrice(){
        boolean sitch;
        if (tietPrice.getText().toString().isEmpty()){
            sitch = false;
            tilPrice.setError(getResources().getString(R.string.Must_have_price));
        }
        else {
            tilPrice.setErrorEnabled(false);
            sitch = true;
        }
        return sitch;
    }

    private void validatePaymentMethod(){
        pd.setMessage(getResources().getString(R.string.Checking_for_payment_methods));
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                httpConnection.htppConnectionURL() + "/payment/customer/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                if (new JSONArray(response.getJSONObject("customer").getString("paymentMethods")).length() != 0){
                                    postRequest();
                                }
                                else {
                                    pd.dismiss();
                                    ADnoPaymentMethod();
                                }
                            }
                            else if (response.getString("status").equals("fail")) {
                                pd.dismiss();
                                ADnoPaymentMethod();
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void ADnoPaymentMethod(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.No_Payment_Method);
        alertDialogBuilder.setMessage(R.string.You_have_no_payment_method);
        alertDialogBuilder.setNegativeButton(R.string.Not_Now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.Add_Payment_Method, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = new Bundle();
                AddPaymentMethod addPaymentMethod = new AddPaymentMethod();
                bundle.putString("from", "SpotActivity");
                addPaymentMethod.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.spot_activity, addPaymentMethod);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void postRequest(){
        pd.setMessage(getResources().getString(R.string.Adding_spot_to_SpotTrade_database));
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jsonObject = new JSONObject();
        final JSONObject sellerInfoObj = new JSONObject();
        try {
            jsonObject.put("type", type);
            jsonObject.put("transaction", "available");
            jsonObject.put("sellerID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
            jsonObject.put("name", locationName);
            jsonObject.put("price", tietPrice.getText().toString());
            jsonObject.put("bidAllowed", cbBids.isChecked());
            jsonObject.put("address", locationAddress);
            jsonObject.put("latitude", String.valueOf(latitude));
            jsonObject.put("longitude", String.valueOf(longitude));
            jsonObject.put("description", tietDescription.getText().toString());

            sellerInfoObj.put("sellerFirstName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_first_name)));
            sellerInfoObj.put("sellerLastName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_last_name)));

            jsonObject.put("sellerInfo", sellerInfoObj);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() +"/location/add", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("postRequest", response + "");
                            String status = response.getString("status");
                            if (status.equals("success")){
                                pd.dismiss();
                                Intent intent = getIntent();
                                intent.putExtra("latitude", response.getString("latitude"));
                                intent.putExtra("longitude", response.getString("longitude"));
                                intent.putExtra("id", response.getString("_id"));
                                intent.putExtra("name", response.getString("name"));
                                setResult(RESULT_OK, intent);
                                finish();
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
}