package almanza1112.spottrade;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

import almanza1112.spottrade.login.LoginActivity;
import almanza1112.spottrade.nonActivity.HttpConnection;

import almanza1112.spottrade.nonActivity.SharedPref;
import almanza1112.spottrade.search.SearchActivity;


public class MapsActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener{
    private FloatingActionMenu fabMenu;
    private View llWhite;
    private Toolbar toolbar;
    private GoogleMap mMap;
    private ViewGroup hiddenPanel;
    private Animation bottomUp, bottomDown;
    private TextView tvFullName, tvUserRating, tvTotalRating, tvLocationName, tvLocationAddress, tvTransaction, tvDescription;
    private Button bBuyNow, bPlaceBid, bCancelBid, bDelete;
    private Marker marker;

    private boolean isFabMenuClicked, isMarkerClicked;
    private double latitude =0, longitude=0;
    private String locationName="empty", locationAddress="empty";
    private int SEARCH_CODE = 0;
    private int SPOT_CODE = 1;
    private String lid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
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
        tvFullName = (TextView) findViewById(R.id.tvFullName);
        tvUserRating = (TextView) findViewById(R.id.tvUserRating);
        tvTotalRating = (TextView) findViewById(R.id.tvTotalRating);
        tvTransaction = (TextView) findViewById(R.id.tvTransaction);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvLocationAddress = (TextView) findViewById(R.id.tvLocationAddress);
        tvLocationName = (TextView) findViewById(R.id.tvLocationName);
        bBuyNow = (Button) findViewById(R.id.bBuyNow);
        bBuyNow.setOnClickListener(this);
        bPlaceBid = (Button) findViewById(R.id.bPlaceBid);
        bPlaceBid.setOnClickListener(this);
        bCancelBid = (Button) findViewById(R.id.bCancelBid);
        bCancelBid.setOnClickListener(this);
        bDelete = (Button) findViewById(R.id.bDelete);
        bDelete.setOnClickListener(this);

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

        FloatingActionButton fabRequest = (FloatingActionButton) findViewById(R.id.fabRequest);
        fabRequest.setOnClickListener(this);

        FloatingActionButton fabSell = (FloatingActionButton) findViewById(R.id.fabSell);
        fabSell.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);
        TextView tvLoggedInFullName = (TextView) navHeaderView.findViewById(R.id.tvLoggedInFullName);
        tvLoggedInFullName.setText(SharedPref.getFirstName(this) + " " + SharedPref.getLastName(this));
        TextView tvLoggedInEmail = (TextView) navHeaderView.findViewById(R.id.tvLoggedInEmail);
        tvLoggedInEmail.setText(SharedPref.getEmail(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabSell:
                fabMenu.close(true);
                startActivityForResult(new Intent(MapsActivity.this, SpotActivity.class)
                        .putExtra("type", "selling")
                        .putExtra("locationName", locationName)
                        .putExtra("locationAddress", locationAddress)
                        .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude), SPOT_CODE);
                break;

            case R.id.fabRequest:
                fabMenu.close(true);
                startActivityForResult(new Intent(MapsActivity.this, SpotActivity.class)
                        .putExtra("type", "requesting")
                        .putExtra("locationName", locationName)
                        .putExtra("locationAddress", locationAddress)
                        .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude), SPOT_CODE);
                break;

            case R.id.bDelete:
                ADdeleteSpot();
                break;

            case R.id.bBuyNow:
                transactionBuyNow();
                break;

            case R.id.bPlaceBid:
                ADplaceBid();
                break;

            case R.id.bCancelBid:
                transactionCancelBid();
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()){
            case R.id.nav_payment:
                startActivity(new Intent(this, Payment.class));
                break;
            case R.id.nav_log_out:
                ADlogOut();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (isFabMenuClicked){
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
        else if (requestCode == SPOT_CODE){
            if (resultCode == RESULT_OK){
                latitude = Double.valueOf(data.getStringExtra("latitude"));
                longitude = Double.valueOf(data.getStringExtra("longitude"));
                String name = data.getStringExtra("name");
                String id = data.getStringExtra("id");
                LatLng locash = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(locash));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                marker = mMap.addMarker(new MarkerOptions().position(locash).title(name));
                marker.setTag(id);
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
                            marker = mMap.addMarker(new MarkerOptions().position(locash).title(locationObj.getString("name")));
                            marker.setTag(locationObj.getString("_id"));
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
    public boolean onMarkerClick(final Marker marker) {
        this.marker = marker;
        isMarkerClicked = true;
        tvDescription.setVisibility(View.VISIBLE);
        bCancelBid.setVisibility(View.VISIBLE);
        bPlaceBid.setVisibility(View.VISIBLE);
        bBuyNow.setVisibility(View.VISIBLE);
        bDelete.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/" + marker.getTag() + "?user=" + SharedPref.getID(this), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    lid = response.getString("_id");
                    tvLocationName.setText(response.getString("name"));

                    tvLocationAddress.setText(response.getString("address"));
                    JSONObject sellerInfoObj = response.getJSONObject("sellerInfo");
                    tvFullName.setText(sellerInfoObj.getString("sellerFirstName") + " " + sellerInfoObj.getString("sellerLastName"));
                    tvUserRating.setText(" - "+sellerInfoObj.getString("sellerOverallRating"));
                    tvTotalRating.setText("("+sellerInfoObj.getString("sellerTotalRatings")+")");

                    if (response.getString("type").equals("selling")) {
                        tvTransaction.setText(getResources().getString(R.string.Selling) + "$" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Buy_Now));
                    }
                    else if (response.getString("type").equals("requesting")) {
                        tvTransaction.setText(getResources().getString(R.string.Requesting) + "$" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Accept));
                    }

                    if (response.getString("sellerID").equals(SharedPref.getID(MapsActivity.this))){
                        bPlaceBid.setVisibility(View.GONE);
                        bCancelBid.setVisibility(View.GONE);
                        bBuyNow.setVisibility(View.GONE);
                    }
                    else {
                        bDelete.setVisibility(View.GONE);
                        if (!response.getBoolean("bidAllowed")) {
                            Log.e("ok", "bid not allowed");
                            bPlaceBid.setVisibility(View.GONE);
                            bCancelBid.setVisibility(View.GONE);
                        }
                        else{
                            Log.e("ok", "bid allowed");
                            if (response.getBoolean("bidden")){
                                Log.e("ok", "bidden");
                                bPlaceBid.setVisibility(View.GONE);
                                bCancelBid.setText(getResources().getString(R.string.Cancel) + " $" + response.getString("biddenAmount") + " " + getResources().getString(R.string.Bid));
                            }
                            else {

                                Log.e("ok", "not bidden");
                                bCancelBid.setVisibility(View.GONE);
                            }
                        }
                    }

                    if (!response.getString("description").isEmpty()) {
                        tvDescription.setText("\"" + response.getString("description") + "\"");
                    }
                    else {
                        tvDescription.setVisibility(View.GONE);
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

        hiddenPanel.startAnimation(bottomUp);
        hiddenPanel.setVisibility(View.VISIBLE);

        return false;
    }

    private void transactionDeleteSpot(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, httpConnection.htppConnectionURL() + "/location/delete/" + lid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        marker.remove();
                        hiddenPanel.startAnimation(bottomDown);
                        hiddenPanel.setVisibility(View.INVISIBLE);
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
                });
        queue.add(jsonObjectRequest);
    }

    private void transactionBuyNow() {
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("transaction", "complete");
            jObject.put("buyerID", SharedPref.getID(this));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/transaction/buy/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){

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

    private void transactionPlaceBid(final String bidAmount){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("bidderID", SharedPref.getID(this));
            jObject.put("bidAmount", bidAmount);
            jObject.put("bidderFirstName", SharedPref.getFirstName(this));
            jObject.put("bidderLastName", SharedPref.getLastName(this));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/transaction/bid/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        Toast.makeText(MapsActivity.this, "Bid placed", Toast.LENGTH_SHORT).show();
                        bPlaceBid.setVisibility(View.GONE);
                        bCancelBid.setText(getResources().getString(R.string.Cancel) + " $" + bidAmount + " " + getResources().getString(R.string.Bid));
                        bCancelBid.setVisibility(View.VISIBLE);
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

    private void transactionCancelBid(){
        final JSONObject jObject = new JSONObject();

        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/transaction/bid/cancel/" + lid + "?user=" + SharedPref.getID(this), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        hiddenPanel.startAnimation(bottomDown);
                        hiddenPanel.setVisibility(View.INVISIBLE);
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.Bid_canceled), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
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

    private void ADplaceBid(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.maps_activity_place_bid_alertdialog, null);

        final EditText etBid = (EditText) alertLayout.findViewById(R.id.etBid);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Place_Bid);
        alertDialogBuilder.setPositiveButton(R.string.Bid, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                transactionPlaceBid(etBid.getText().toString());
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
    }

    private void ADdeleteSpot(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.Delete);
        alertDialogBuilder.setMessage(R.string.Are_you_sure_you_want_to_delete_this_spot);
        alertDialogBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                transactionDeleteSpot();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADlogOut(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.Log_Out);
        alertDialogBuilder.setMessage(R.string.Are_you_sure_you_want_to_log_out);
        alertDialogBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                SharedPref.clearAll(MapsActivity.this);
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
