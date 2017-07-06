package almanza1112.spottrade;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.nonActivity.HttpConnection;

import almanza1112.spottrade.search.SearchActivity;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabSell, fabRequest;
    private View llWhite;
    private Toolbar toolbar;
    private GoogleMap mMap;
    private ViewGroup hiddenPanel;
    private Animation bottomUp, bottomDown;
    private TextView tvLocationName, tvLocationAddress;

    private boolean isFabMenuClicked, isMarkerClicked;
    private double latitude =0, longitude=0;
    private String locationName="empty", locationAddress="empty";
    private int SEARCH_CODE = 0;
    private int SELL_CODE = 1;
    private int REQUEST_CODE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            RelativeLayout.LayoutParams tb = (RelativeLayout.LayoutParams)toolbar.getLayoutParams();
            tb.setMargins(0, getStatusBarHeight(), 0, 0);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        hiddenPanel = (ViewGroup)findViewById(R.id.hidden_panel);
        tvLocationAddress = (TextView) findViewById(R.id.tvLocationAddress);
        tvLocationName = (TextView) findViewById(R.id.tvLocationName);

        llWhite = findViewById(R.id.llWhite);
        fabMenu = (FloatingActionMenu) findViewById(R.id.fabMenu);
        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    isFabMenuClicked = true;
                    llWhite.setVisibility(View.VISIBLE);
                }
                else {
                    isFabMenuClicked = false;
                    llWhite.setVisibility(View.GONE);
                }
            }
        });

        fabRequest = (FloatingActionButton) findViewById(R.id.fabRequest);
        fabRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                fabMenu.close(true);
                startActivityForResult(new Intent(MapsActivity.this, RequestActivity.class)
                        .putExtra("locationName", locationName)
                        .putExtra("locationAddress", locationAddress)
                        .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude), REQUEST_CODE);
            }
        });

        fabSell = (FloatingActionButton) findViewById(R.id.fabSell);
        fabSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                startActivityForResult(new Intent(MapsActivity.this, SellActivity.class)
                        .putExtra("locationName", locationName)
                        .putExtra("locationAddress", locationAddress)
                        .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude), SELL_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search){
            startActivityForResult(new Intent(this, SearchActivity.class), SEARCH_CODE);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isFabMenuClicked){
            fabMenu.close(true);
            isFabMenuClicked = false;
        }
        else if (isMarkerClicked){
            isMarkerClicked = false;
            hiddenPanel.startAnimation(bottomDown);
            hiddenPanel.setVisibility(View.INVISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_CODE){
            if (resultCode == RESULT_OK){
                latitude = data.getDoubleExtra("latitude", 0);
                longitude = data.getDoubleExtra("longitude", 0);
                locationName = data.getStringExtra("locationName");
                locationAddress = data.getStringExtra("locationAddress");
                LatLng locash = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(locash));
            }
        }
        else if (requestCode == SELL_CODE){
            if (resultCode == RESULT_OK){
                latitude = Double.valueOf(data.getStringExtra("latitude"));
                longitude = Double.valueOf(data.getStringExtra("longitude"));
                LatLng locash = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(locash));            }
        }
        else if (requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK){
                latitude = Double.valueOf(data.getStringExtra("latitude"));
                longitude = Double.valueOf(data.getStringExtra("longitude"));
                LatLng locash = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(locash));
            }
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        double latitude = 0;
        double longitude = 0;

        try {
            // Get latitude of the current location
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

        // Create a LatLng object for the current location
        LatLng currentLocation = new LatLng(latitude, longitude);

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setMapToolbarEnabled(false); //disables the bottom right buttons that appear when you click on a marker
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);

        getAvailableSpots();
    }

    private void getAvailableSpots(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/all?sellerID=all&transaction=available&type=all", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        String locations = response.getString("location");
                        JSONArray jsonArray = new JSONArray(locations);

                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject locationObj = jsonArray.getJSONObject(i);
                            Double lat = Double.valueOf(locationObj.getString("latitude"));
                            Double lng = Double.valueOf(locationObj.getString("longitude"));
                            LatLng locash = new LatLng(lat, lng);
                            Marker marker;
                            marker = mMap.addMarker(new MarkerOptions().position(locash).title(locationObj.getString("name")));
                            marker.setTag(locationObj);
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
                error.printStackTrace();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        isMarkerClicked = true;
        try {
            JSONObject jsonObject = (JSONObject) marker.getTag();
            tvLocationName.setText(jsonObject.getString("name"));
            tvLocationAddress.setText(jsonObject.getString("address"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        hiddenPanel.startAnimation(bottomUp);
        hiddenPanel.setVisibility(View.VISIBLE);

        return false;
    }

    private void jsonArrayRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/user", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{

                    for (int i = 0; i < response.length(); i++){
                        JSONObject jsonObject = response.getJSONObject(i);
                        String username = jsonObject.getString("username");
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");

                        Log.e("jsonArray",username +"\n" + firstName + "\n" + lastName);
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
        queue.add(jsonArrayRequest);
    }

    private void jsonObjectGETRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, httpConnection.htppConnectionURL() +"/user/594a200a7918ffde96803087", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String username = response.getString("username");
                            String firstName = response.getString("firstName");
                            String lastName = response.getString("lastName");
                            Log.e("jsonObject",username +"\n" + firstName + "\n" + lastName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void jsonObjectPUTRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("username", "porkchoplaya23");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, httpConnection.htppConnectionURL() +"/user/update/594a34ed4f9cdd3f0c23e750", jObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("PUT", response + "");
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
                Map<String, String> headers = new HashMap<String, String>();
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


    private void jsonObjectDELETERequest(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.DELETE, httpConnection.htppConnectionURL() +"/user/remove/594a34ed4f9cdd3f0c23e750", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("DELETE",response + "");
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }

    private void stringRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, httpConnection.htppConnectionURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("get", "Response is: "+ response);
                        //mTextView.setText("Response is: "+ response.substring(0,500));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //mTextView.setText("That didn't work!");
                        Log.d("get", "That didn't work!");
                        error.printStackTrace();
                    }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
