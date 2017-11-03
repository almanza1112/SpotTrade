package almanza1112.spottrade;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.NumberPicker;
import android.widget.ProgressBar;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import almanza1112.spottrade.account.feedback.Feedback;
import almanza1112.spottrade.account.payment.AddPaymentMethod;
import almanza1112.spottrade.account.payment.Payment;
import almanza1112.spottrade.account.history.History;
import almanza1112.spottrade.account.personal.Personal;
import almanza1112.spottrade.login.LoginActivity;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;
import almanza1112.spottrade.nonActivity.tracking.TrackerService;
import almanza1112.spottrade.yourSpots.YourSpots;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener{
    private ProgressBar progressBar;
    private FloatingActionMenu fabMenu;
    private GoogleMap mMap;
    Location myLocation;
    private ProgressDialog pd = null;
    NavigationView navigationView;
    DrawerLayout drawer;
    LatLng currentLocation, spotLocation;
    private ViewGroup hiddenPanel;
    private Animation bottomUp, bottomDown;
    private TextView tvFullName, tvUserRating, tvTotalRating, tvLocationName, tvLocationAddress, tvTransaction, tvDescription;
    private ImageView ivSellerProfilePhoto;
    private Button bBuyNow, bPlaceBid, bCancelBid, bDelete;
    private Marker marker;

    private boolean isFabMenuClicked, isMarkerClicked;
    private double latitude =0, longitude=0;
    private String locationName="empty", locationAddress="empty";
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private int SPOT_CODE = 1;
    private int quantity;
    private String lid, price, type;
    private String typeSelected = "all";
    final int[] pos = {2};

    private final int ACCESS_FINE_LOCATION_PERMISSION_MAP = 5;
    private final int ACCESS_FINE_LOCATION_PERMISSION_TRACKING = 6;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        CardView cvToolbar = (CardView) findViewById(R.id.cvToolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        RelativeLayout.LayoutParams tb = (RelativeLayout.LayoutParams) cvToolbar.getLayoutParams();
        tb.setMargins(20, 20, 20, 0);

        pd = new ProgressDialog(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setSupportActionBar(toolbar);

        bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        hiddenPanel = (ViewGroup)findViewById(R.id.hidden_panel);

        ivSellerProfilePhoto = (ImageView) findViewById(R.id.ivProfilePhoto);
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
        final ImageView ivProfilePhoto = (ImageView) navHeaderView.findViewById(R.id.ivProfilePhoto);
        if (SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_photo_url)) != null){
            Picasso.with(this).load(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_photo_url))).fit().centerCrop().into(ivProfilePhoto);
        }
        final TextView tvLoggedInFullName = (TextView) navHeaderView.findViewById(R.id.tvLoggedInFullName);
        tvLoggedInFullName.setText(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_first_name)) + " " + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_last_name)));
        final TextView tvLoggedInEmail = (TextView) navHeaderView.findViewById(R.id.tvLoggedInEmail);
        tvLoggedInEmail.setText(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_email)));
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

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ACCESS_FINE_LOCATION_PERMISSION_TRACKING);
                }
                else {
                    if (type.equals("Sell")){
                        validatePaymentMethod();
                    }
                    else if (type.equals("Request")){
                        /*
                         * TODO
                         * need to validate received payment form, PAYPAL!
                         * */
                        transactionBuyNow();
                    }
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
            case R.id.nav_feedback:
                fragment = new Feedback();
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
        switch (item.getItemId()) {
            case R.id.search:

                /* TODO: this is an example of how to retrieve data from database
                databaseReference = FirebaseDatabase.getInstance().getReference().child("tracking").child("lidthatisbought");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            BuyerTracker buyerTracker = new BuyerTracker();
                            buyerTracker.setKey(ds.getKey());
                            buyerTracker.setLat(ds.getValue(BuyerTracker.class).getLat());
                            buyerTracker.setLng(ds.getValue(BuyerTracker.class).getLng());

                            Log.e("k", "key: " +buyerTracker.getKey() + "\n"+buyerTracker.getLat() + " " + buyerTracker.getLng());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                */

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

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.filterMaps:
                final CharSequence[] items = {getResources().getString(R.string.Sell), getResources().getString(R.string.Request), getResources().getString(R.string.All)};
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.Filter) + " " + getResources().getString(R.string.Spots));
                alertDialogBuilder.setSingleChoiceItems(items, pos[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pos[0] = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.Apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String type;
                        if (pos[0] == 0){
                            type = "Sell";
                        }
                        else if (pos[0] == 1){
                            type = "Request";
                        }
                        else {
                            type = "all";
                        }
                        typeSelected = type;
                        getAvailableSpots(type);
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
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
                if (isMarkerClicked){
                    isMarkerClicked = false;
                    hiddenPanel.startAnimation(bottomDown);
                    hiddenPanel.setVisibility(View.INVISIBLE);
                }
                else if (isFabMenuClicked){
                    fabMenu.close(true);
                    isFabMenuClicked = false;
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
        if (requestCode == SPOT_CODE){
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
        else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                locationName = place.getName().toString();
                locationAddress = place.getAddress().toString();
                LatLng locash = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(locash));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
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

    String provider;
    LocationManager locMan;
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
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locMan = locationManager;
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        this.provider = provider;

        myLocation = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_PERMISSION_MAP);
        }
        else {
            // Get Current Location
            myLocation = locationManager.getLastKnownLocation(provider);
            mMap.setMyLocationEnabled(true);
            double latitude = 0;
            double longitude = 0;

            try {
                latitude = myLocation.getLatitude();
                longitude = myLocation.getLongitude();
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }

            currentLocation = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        mMap.getUiSettings().setMapToolbarEnabled(false); //disables the bottom right buttons that appear when you click on a marker
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);

        if (isServiceRunning(TrackerService.class)){
            // If the service class is running, then set up the map so that you will see the current transaction

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (requestCode == ACCESS_FINE_LOCATION_PERMISSION_MAP) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                myLocation = locationManager.getLastKnownLocation(provider);
                mMap.setMyLocationEnabled(true);
                double latitude = 0;
                double longitude = 0;
                try {
                    latitude = myLocation.getLatitude();
                    longitude = myLocation.getLongitude();
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
                currentLocation = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
            else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == ACCESS_FINE_LOCATION_PERMISSION_TRACKING){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (type.equals("Sell")){
                    validatePaymentMethod();
                }
                else if (type.equals("Request")){
                    transactionBuyNow();
                }
            }
            else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/" + marker.getTag() + "?user=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    lid = response.getString("_id");
                    tvLocationName.setText(response.getString("name"));
                    price = response.getString("price");
                    quantity = response.getInt("quantity");

                    tvLocationAddress.setText(response.getString("address"));
                    JSONObject sellerInfoObj = response.getJSONObject("sellerInfo");
                    tvFullName.setText(sellerInfoObj.getString("sellerFirstName") + " " + sellerInfoObj.getString("sellerLastName"));
                    tvUserRating.setText(" - "+sellerInfoObj.getString("sellerOverallRating"));
                    tvTotalRating.setText("("+sellerInfoObj.getString("sellerTotalRatings")+")");

                    if (sellerInfoObj.has("sellerProfilePhotoUrl")){
                        Picasso.with(MapsActivity.this).load(sellerInfoObj.getString("sellerProfilePhotoUrl")).fit().centerCrop().into(ivSellerProfilePhoto);
                    }
                    type = response.getString("type");
                    if (type.equals("Sell")) {
                        tvTransaction.setText(getResources().getString(R.string.Selling) + " - $" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Buy_Now));
                    }
                    else if (type.equals("Request")) {
                        tvTransaction.setText(getResources().getString(R.string.Requesting) + " - $" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Accept));
                    }

                    if (response.getString("sellerID").equals(SharedPref.getSharedPreferences(MapsActivity.this, getResources().getString(R.string.logged_in_user_id)))){
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

    private void getAvailableSpots(String typeSelected){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/all?sellerID=all&transaction=available&type=" + typeSelected, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        mMap.clear();
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
                        progressBar.setVisibility(View.GONE);
                    }
                }
                catch (JSONException e){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
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
            jObject.put("transaction", "ongoing");
            jObject.put("buyerID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
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
                        String lidBought = response.getString("_id");
                        String latBought = response.getString("latitude");
                        String lngBought = response.getString("longitude");
                        String addressBought = response.getString("address");
                        String nameBought = response.getString("name");

                        SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.bought_lid), lidBought);
                        SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.bought_lat), latBought);
                        SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.bought_lng), lngBought);
                        SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.bought_address), addressBought);
                        SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.bought_name), nameBought);

                        Map<String, String> latLng = new HashMap<>();
                        latLng.put("lat", "null");
                        latLng.put("lng", "null");

                        databaseReference = FirebaseDatabase.getInstance().getReference("tracking");
                        databaseReference.child(lidBought).child(SharedPref.getSharedPreferences(MapsActivity.this, getResources().getString(R.string.logged_in_user_id))).setValue(latLng, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null){
                                    // There is no error
                                    startTrackingService();
                                }
                                else {
                                    // There is an error

                                }
                            }
                        });
                        //startNavigationApp(response.getString("latitude"), response.getString("longitude"), response.getString("name"));
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
            jObject.put("bidderID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
            jObject.put("bidAmount", bidAmount);
            jObject.put("bidderFirstName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_first_name)));
            jObject.put("bidderLastName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_last_name)));
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/transaction/bid/cancel/" + lid + "?user=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
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
                FirebaseAuth.getInstance().signOut();
                SharedPref.clearSharedPreferences(MapsActivity.this);
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
                httpConnection.htppConnectionURL() + "/payment/customer/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)),
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
                                                if (quantity > 1){
                                                    ADselectQuantity(
                                                            jsonArray.getJSONObject(i).getString("cardType"),
                                                            last4,
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"));
                                                }
                                                else {
                                                    ADareYouSurePaymentMethod(
                                                            jsonArray.getJSONObject(i).getString("cardType"),
                                                            last4,
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"),
                                                            1);
                                                }
                                            }
                                            else{
                                                //means the default payment is PayPal
                                                if (quantity > 1){
                                                    ADselectQuantity(
                                                            "PayPal",
                                                            jsonArray.getJSONObject(i).getString("email"),
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"));
                                                }
                                                else {
                                                    ADareYouSurePaymentMethod(
                                                            "PayPal",
                                                            jsonArray.getJSONObject(i).getString("email"),
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"),
                                                            1);
                                                }
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

    private void ADselectQuantity(final String paymentType, final String paymentCredentials, final String paymentImageUrl, final String paymentToken){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.number_picker, null);

        final NumberPicker npQuantity = (NumberPicker) alertLayout.findViewById(R.id.npQuantity);
        npQuantity.setMinValue(1);
        npQuantity.setMaxValue(quantity);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Quantity);
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ADareYouSurePaymentMethod(
                        paymentType,
                        paymentCredentials,
                        paymentImageUrl,
                        paymentToken,
                        npQuantity.getValue());
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private void ADareYouSurePaymentMethod(String paymentType, String paymentCredentials, String paymentImageUrl, final String paymentToken, int quantity){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.maps_activity_transaction_are_you_sure__alertdialog, null);


        TextView tvCompleteTransactionDialog = (TextView) alertLayout.findViewById(R.id.tvCompleteTransactionDialog);
        String completeTransactionText;
        if (quantity > 1){
            int totalPrice = Integer.valueOf(price) * quantity;
            completeTransactionText = getResources().getString(R.string.You_will_be_charged) + " $" + totalPrice + " ($" + price + " x " + quantity + ") " + getResources().getString(R.string.with_the_following_payment_method);
        }
        else {

            completeTransactionText = getResources().getString(R.string.You_will_be_charged) + " $" + price + " " + getResources().getString(R.string.with_the_following_payment_method);
        }
        tvCompleteTransactionDialog.setText(completeTransactionText);
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
        alertDialogBuilder.setPositiveButton(R.string.Complete, new DialogInterface.OnClickListener() {
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
                httpConnection.htppConnectionURL() + "/payment/customer/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)),
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

    private void refreshMap(){
        if (mMap != null){
            mMap.clear();
            getAvailableSpots(typeSelected);
        }
    }

    private void startTrackingService(){
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("almanza1112.spottrade", "almanza1112.spottrade.nonActivity.tracking.TrackerService");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        startService(new Intent(this, TrackerService.class));
    }

    private void startNavigationApp(String lat, String lng, String label){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng+"(" + Uri.encode(label) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        //mapIntent.setPackage("com.google.android.apps.maps"); //this line of code opens up Google Maps only
        startActivity(mapIntent);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void isOngoingSpots(){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/history?sellerID="+ SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)) + "&type=all&transaction=ongoing", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        String locations = response.getString("location");
                        JSONArray jsonArray = new JSONArray(locations);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject locationObj = jsonArray.getJSONObject(i);

                        }

                        progressBar.setVisibility(View.GONE);
                    }
                    else if (response.getString("status").equals("fail")){
                        getAvailableSpots(typeSelected);
                        progressBar.setVisibility(View.GONE);
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }
}
