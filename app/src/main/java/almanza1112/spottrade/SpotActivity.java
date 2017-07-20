package almanza1112.spottrade;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
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

import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;
import almanza1112.spottrade.search.SearchActivity;

/**
 * Created by almanza1112 on 6/29/17.
 */

public class SpotActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvLocationName, tvLocationAddress, tvAddLocation;
    private TextInputLayout tilPrice;
    private TextInputEditText tietDescription, tietPrice;
    private CheckBox cbBids;
    private int ADD_LOCATION_CODE = 0;
    private double latitude, longitude;
    private String locationName, locationAddress, type;

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

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (type.equals("requesting")){
            toolbar.setTitle(R.string.Request_a_Spot);
        }
        else if (type.equals("selling")){
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
            case R.id.tvAddLocation:
                startActivityForResult(new Intent(this, SearchActivity.class), ADD_LOCATION_CODE);
                break;

            case R.id.tvLocationName:
                startActivityForResult(new Intent(this, SearchActivity.class), ADD_LOCATION_CODE);
                break;

            case R.id.tvLocationAddress:
                startActivityForResult(new Intent(this, SearchActivity.class), ADD_LOCATION_CODE);
                break;

            case R.id.fabDone:
                if (validatePrice()) {
                    postRequest();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_LOCATION_CODE){
            if (resultCode == RESULT_OK){
                latitude = data.getDoubleExtra("latitude", 0);
                longitude = data.getDoubleExtra("longitude", 0);
                locationName = data.getStringExtra("locationName");
                locationAddress = data.getStringExtra("locationAddress");

                tvLocationName.setText(locationName);
                tvLocationAddress.setText(locationAddress);
                tvAddLocation.setVisibility(View.GONE);
                tvLocationName.setVisibility(View.VISIBLE);
                tvLocationAddress.setVisibility(View.VISIBLE);
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

    private void postRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", type);
            jsonObject.put("transaction", "available");
            jsonObject.put("sellerID", SharedPref.getID(this));
            jsonObject.put("name", locationName);
            jsonObject.put("price", tietPrice.getText().toString());
            jsonObject.put("bidAllowed", cbBids.isChecked());
            jsonObject.put("address", locationAddress);
            jsonObject.put("latitude", String.valueOf(latitude));
            jsonObject.put("longitude", String.valueOf(longitude));
            jsonObject.put("description", tietDescription.getText().toString());
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
                            String status = response.getString("status");
                            if (status.equals("success")){
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