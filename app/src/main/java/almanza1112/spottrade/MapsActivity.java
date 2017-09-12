package almanza1112.spottrade;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import almanza1112.spottrade.account.payment.AddPaymentMethod;
import almanza1112.spottrade.account.payment.Payment;
import almanza1112.spottrade.account.history.History;
import almanza1112.spottrade.account.personal.Personal;
import almanza1112.spottrade.login.LoginActivity;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;
import almanza1112.spottrade.search.SearchActivity;
import almanza1112.spottrade.yourSpots.YourSpots;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener{
    private FloatingActionMenu fabMenu;
    private GoogleMap mMap;
    private ProgressDialog pd = null;
    NavigationView navigationView;
    DrawerLayout drawer;
    LatLng currentLocation, spotLocation;
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
    private String lid, price, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        CardView cvToolbar = (CardView) findViewById(R.id.cvToolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        RelativeLayout.LayoutParams tb = (RelativeLayout.LayoutParams) cvToolbar.getLayoutParams();
        tb.setMargins(20, 20, 20, 0);

        pd = new ProgressDialog(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setSupportActionBar(toolbar);

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

        fabMenu = (FloatingActionMenu) findViewById(R.id.fabMenu);
        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                isFabMenuClicked = opened;
            }
        });

        FloatingActionButton fabRequest = (FloatingActionButton) findViewById(R.id.fabRequest);
        fabRequest.setOnClickListener(this);

        FloatingActionButton fabSell = (FloatingActionButton) findViewById(R.id.fabSell);
        fabSell.setOnClickListener(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
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
                        .putExtra("type", "Sell")
                        .putExtra("locationName", locationName)
                        .putExtra("locationAddress", locationAddress)
                        .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude), SPOT_CODE);
                break;

            case R.id.fabRequest:
                fabMenu.close(true);
                startActivityForResult(new Intent(MapsActivity.this, SpotActivity.class)
                        .putExtra("type", "Request")
                        .putExtra("locationName", locationName)
                        .putExtra("locationAddress", locationAddress)
                        .putExtra("latitude", latitude)
                        .putExtra("longitude", longitude), SPOT_CODE);
                break;

            case R.id.bDelete:
                ADdeleteSpot();
                break;

            case R.id.bBuyNow:
                /*

                TODO: MAJOR ISSUE REGARDING REQUEST
                For when a user that purchases a spot that is selling, code is easy to reject buyer
                if card gets rejected etc. Need to verify the requester's payment as well as not
                allowing them to delete their default payment if they have a spot up for Request
                and if upon purchase of the person accepting the request, throw any errors Gateway,
                card validation etc.

                 */

                if (type.equals("Sell")){
                    validatePaymentMethod();
                }
                else if (type.equals("Request")){
                    transactionBuyNow();
                }
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
        Fragment fragment = null;

        switch (item.getItemId()){
            case R.id.nav_home:
                if (getFragmentManager().getBackStackEntryCount() > 0){
                    getFragmentManager().popBackStack();
                }
                refreshMap();
                break;
            case R.id.nav_your_spots:
                fragment = new YourSpots();
                break;
            case R.id.nav_history:
                fragment = new History();
                break;
            case R.id.nav_personal:
                fragment = new Personal();
                break;
            case R.id.nav_payment:
                fragment = new Payment();
                break;
            case R.id.nav_log_out:
                ADlogOut();
                break;
        }

        if (fragment != null){
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.drawer_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search){
            startActivityForResult(new Intent(this, SearchActivity.class), SEARCH_CODE);
        }
        else if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            if (count > 0){
                getFragmentManager().popBackStack();
            }
            else {
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

        /*
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
            if (!success) {
                Log.e("raw", "Style parsing failed.");
            }
        }
        catch (Resources.NotFoundException e) {
            Log.e("raw", "Can't find style. Error: ", e);
        }
        */

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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

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
        currentLocation = new LatLng(latitude, longitude);

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setMapToolbarEnabled(false); //disables the bottom right buttons that appear when you click on a marker
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);

        getAvailableSpots();
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
                    price = response.getString("price");

                    tvLocationAddress.setText(response.getString("address"));
                    JSONObject sellerInfoObj = response.getJSONObject("sellerInfo");
                    tvFullName.setText(sellerInfoObj.getString("sellerFirstName") + " " + sellerInfoObj.getString("sellerLastName"));
                    tvUserRating.setText(" - "+sellerInfoObj.getString("sellerOverallRating"));
                    tvTotalRating.setText("("+sellerInfoObj.getString("sellerTotalRatings")+")");

                    type = response.getString("type");
                    if (type.equals("Sell")) {
                        tvTransaction.setText(getResources().getString(R.string.Selling) + " - $" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Buy_Now));
                    }
                    else if (type.equals("Request")) {
                        tvTransaction.setText(getResources().getString(R.string.Requesting) + " - $" + response.getString("price"));
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
                            bPlaceBid.setVisibility(View.GONE);
                            bCancelBid.setVisibility(View.GONE);
                        }
                        else{
                            if (response.getBoolean("bidden")){
                                bPlaceBid.setVisibility(View.GONE);
                                bCancelBid.setText(getResources().getString(R.string.Cancel) + " $" + response.getString("biddenAmount") + " " + getResources().getString(R.string.Bid));
                            }
                            else {
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

                    double dLat = Double.valueOf(response.getString("latitude"));
                    double dLong = Double.valueOf(response.getString("longitude"));
                    spotLocation = new LatLng(dLat, dLong);
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
                pd.dismiss();
                try{
                    if (response.getString("status").equals("success")){
                        setRoute();
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

    private void validatePaymentMethod(){
        pd.setTitle(R.string.Verifying);
        pd.setMessage(getResources().getString(R.string.Checking_for_payment_methods));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                httpConnection.htppConnectionURL() + "/payment/customer/" + SharedPref.getID(this),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray jsonArray = new JSONArray(response.getJSONObject("customer").getString("paymentMethods"));
                                if (jsonArray.length() > 0){
                                    for (int i = 0; i < jsonArray.length(); i++){
                                        if (jsonArray.getJSONObject(i).getBoolean("default")){
                                            if (jsonArray.getJSONObject(i).has("cardType")){
                                                //means the default payment is a credit card
                                                int len = jsonArray.getJSONObject(i).getString("maskedNumber").length() - 4;
                                                String astr = "";
                                                for (int j = 0; j < len; j++){
                                                    astr += "*";
                                                }
                                                String last4 = astr + jsonArray.getJSONObject(i).getString("last4");
                                                ADareYouSurePaymentMethod(
                                                        jsonArray.getJSONObject(i).getString("cardType"),
                                                        last4,
                                                        jsonArray.getJSONObject(i).getString("imageUrl"),
                                                        jsonArray.getJSONObject(i).getString("token"));
                                            }
                                            else{
                                                //means the default payment is PayPal
                                                ADareYouSurePaymentMethod(
                                                        "PayPal",
                                                        jsonArray.getJSONObject(i).getString("email"),
                                                        jsonArray.getJSONObject(i).getString("imageUrl"),
                                                        jsonArray.getJSONObject(i).getString("token"));
                                            }
                                            pd.dismiss();
                                            break;
                                        }
                                    }
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

    private void ADareYouSurePaymentMethod(String paymentType, String paymentCredentials, String paymentImageUrl, final String paymentToken){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.maps_activity_transaction_are_you_sure__alertdialog, null);

        ImageView ivPaymentImage = (ImageView) alertLayout.findViewById(R.id.ivPaymentImage);
        TextView tvPaymentName = (TextView) alertLayout.findViewById(R.id.tvPaymentName);
        TextView tvPaymentCredentials = (TextView) alertLayout.findViewById(R.id.tvPaymentCredentials);

        Picasso.with(this).load(paymentImageUrl).into(ivPaymentImage);
        tvPaymentName.setText(paymentType);
        tvPaymentCredentials.setText(paymentCredentials);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Complete_Transaction);
        alertDialogBuilder.setNegativeButton(R.string.Other_Method, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ADotherPaymentMethod();

            }
        });
        alertDialogBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkout(paymentToken);
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADotherPaymentMethod(){
        pd.setTitle(R.string.Loading);
        pd.setMessage(getResources().getString(R.string.Loading_payment_methods));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                httpConnection.htppConnectionURL() + "/payment/customer/" + SharedPref.getID(this),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                List<String> paymentType = new ArrayList<>();
                                List<String> paymentTypeName = new ArrayList<>();
                                List<String> credentials = new ArrayList<>();
                                final List<String> token = new ArrayList<>();
                                JSONObject customerObj = new JSONObject(response.getString("customer"));
                                if (customerObj.has("creditCards")){
                                    JSONArray creditCardsArray = new JSONArray(customerObj.getString("creditCards"));
                                    for (int i = 0; i < creditCardsArray.length(); i++){
                                        paymentType.add("creditCard");
                                        paymentTypeName.add(creditCardsArray.getJSONObject(i).getString("cardType"));
                                        int len = creditCardsArray.getJSONObject(i).getString("maskedNumber").length() - 4;
                                        String astr = "";
                                        for (int j = 0; j < len; j++){
                                            astr += "*";
                                        }
                                        token.add(creditCardsArray.getJSONObject(i).getString("token"));
                                        credentials.add(astr + creditCardsArray.getJSONObject(i).getString("last4"));
                                    }
                                }
                                if (customerObj.has("paypalAccounts")){
                                    JSONArray paypalAccountsArray = new JSONArray(customerObj.getString("paypalAccounts"));
                                    for (int i = 0; i < paypalAccountsArray.length(); i++){
                                        paymentType.add("paypal");
                                        paymentTypeName.add("PayPal");
                                        token.add(paypalAccountsArray.getJSONObject(i).getString("token"));
                                        credentials.add(paypalAccountsArray.getJSONObject(i).getString("email"));
                                    }
                                }
                                CharSequence[] csArr = new CharSequence[paymentType.size()];
                                for (int i = 0; i < paymentType.size(); i++){
                                    csArr[i] = paymentTypeName.get(i) + "\n" + credentials.get(i);
                                }
                                final String[] tokenString = new String[1];
                                pd.dismiss();
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                                alertDialogBuilder.setMultiChoiceItems(csArr, null, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        tokenString[0] = token.get(which);
                                    }
                                });
                                alertDialogBuilder.setTitle(R.string.Choose_Payment_Method);
                                alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        checkout(tokenString[0]);
                                    }
                                });

                                final AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                            }
                            else if (!response.getString("status").equals("fail")) {
                                Toast.makeText(MapsActivity.this, "Error: could not retrieve payment methods", Toast.LENGTH_SHORT).show();
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
                bundle.putString("from", "MapsActivity");
                addPaymentMethod.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.drawer_layout, addPaymentMethod);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void checkout(String token){
        pd.setTitle(R.string.Completing);
        pd.setMessage(getResources().getString(R.string.Completing_transaction));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("paymentMethodToken", token);
            jsonObject.put("amount", price);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() +"/payment/checkout", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")){
                                transactionBuyNow();
                            }
                            else {
                                ADerrorProcessingPayment();
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

    private void ADerrorProcessingPayment(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.Error);
        alertDialogBuilder.setMessage(R.string.Error_processing_payment);
        alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //When user buys spot it will give him directions to the spot
    private void setRoute(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(spotLocation));

        String url = getUrl(currentLocation, spotLocation);

        getDataFromUrl(url);
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private void getDataFromUrl(String url){
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray routesObj = new JSONArray(response.getString("routes"));
                            JSONObject jBound = routesObj.getJSONObject(0);
                            JSONObject boundsObj = new JSONObject(jBound.getString("bounds"));
                            JSONObject northeastObj = boundsObj.getJSONObject("northeast");
                            JSONObject southwestObj = boundsObj.getJSONObject("southwest");
                            LatLng boundSouthWest = new LatLng(southwestObj.getDouble("lat"),southwestObj.getDouble("lng"));
                            LatLng boundNorthEast = new LatLng(northeastObj.getDouble("lat"),northeastObj.getDouble("lng"));
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(boundSouthWest);
                            builder.include(boundNorthEast);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));

                            hiddenPanel.startAnimation(bottomDown);
                            hiddenPanel.setVisibility(View.INVISIBLE);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new ParserTask().execute(response.toString());
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

    private void refreshMap(){
        if (mMap != null){
            mMap.clear();
            getAvailableSpots();
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
