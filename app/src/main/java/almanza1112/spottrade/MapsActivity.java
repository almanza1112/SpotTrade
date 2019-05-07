package almanza1112.spottrade;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
import com.facebook.login.LoginManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

import almanza1112.spottrade.navigationMenu.About;
import almanza1112.spottrade.navigationMenu.account.feedback.Feedback;
import almanza1112.spottrade.navigationMenu.account.payment.AddCreditDebitCard;
import almanza1112.spottrade.navigationMenu.account.payment.AddPaymentMethod;
import almanza1112.spottrade.navigationMenu.account.payment.Payment;
import almanza1112.spottrade.navigationMenu.account.history.History;
import almanza1112.spottrade.navigationMenu.account.personal.Personal;
import almanza1112.spottrade.login.LoginActivity;
import almanza1112.spottrade.nonActivity.CircleTransform;
import almanza1112.spottrade.nonActivity.SharedPref;
import almanza1112.spottrade.nonActivity.tracking.TrackerService;
import almanza1112.spottrade.navigationMenu.yourSpots.ViewOffers;
import almanza1112.spottrade.navigationMenu.yourSpots.YourSpots;

public class MapsActivity extends AppCompatActivity
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        AddPaymentMethod.PaymentMethodAddedListener,
        AddCreditDebitCard.CreditCardAddedListener,
        ViewOffers.OfferAcceptedListener ,
        CreateSpot.SpotCreatedListener,
        Personal.ProfilePhotoChangedListener{

    private ProgressBar progressBar;
    private GoogleMap mMap;
    Location myLocation;
    private ProgressDialog pd = null;
    NavigationView navigationView;
    DrawerLayout drawer;
    LatLng currentLocation, spotLocation;
    private ViewGroup hiddenPanel;
    private Animation bottomUp, bottomDown;
    private TextView tvFullName, tvUserRating, tvTotalRating, tvLocationName, tvLocationAddress,
            tvTransaction, tvDescription, tvQuantity;
    private ImageView ivSellerProfilePhoto, ivProfilePhoto;
    private Button bBuyNow, bMakeOffer, bCancelOffer, bDelete;
    private Marker marker;
    private GoogleApiClient mGoogleApiClient;

    private boolean isMarkerClicked;
    private double latitude = 0, longitude = 0;
    private String locationName = "empty", locationAddress = "empty";
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private int quantity;
    private String lid, price, type;
    private String typeSelected = "All";
    private String categorySelected = "All";
    private int typePosition = 0;
    private int categoryPosition = 0;


    private final int ACCESS_FINE_LOCATION_PERMISSION_MAP = 5;
    private final int ACCESS_FINE_LOCATION_PERMISSION_TRACKING = 6;
    private final int READ_EXTERNAL_STORAGE_PERMISSION = 2;

    private DatabaseReference databaseReference;

    Payment paymentFragment;
    CreateSpot createSpotFragment;
    Personal personalFragment;

    // For onOfferAccepted
    boolean isOfferAccepted;
    String lidBought, idBought, latBought, lngBought, profilePhotoUrlBought;

    // For the inflated menu in toolbar
    private MenuItem searchMenuItem, filterMenuItem;

    private RelativeLayout.LayoutParams tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        final CardView cvToolbar = findViewById(R.id.cvToolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                final int statusBarHeight = insets.getSystemWindowInsetTop();
                SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.status_bar_height), String.valueOf(statusBarHeight));
                tb = (RelativeLayout.LayoutParams) cvToolbar.getLayoutParams();
                tb.setMargins(20, statusBarHeight + 20, 20, 0);
                return insets;
            }
        });

        final TypedArray ta = getTheme().obtainStyledAttributes(
                new int[] {android.R.attr.actionBarSize});
        int actionBarHeight = (int) ta.getDimension(0, 0);
        SharedPref.setSharedPreferences(this, getResources().getString(R.string.action_bar_height), String.valueOf(actionBarHeight));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        pd = new ProgressDialog(this);
        progressBar = findViewById(R.id.progressBar);
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        // GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            checkOnGoingTransactions();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setSupportActionBar(toolbar);

        // For notifications
        try {
            String pendingData = getIntent().getExtras().getString("message");
            JSONObject dataObj = new JSONObject(pendingData);
            String type = dataObj.getString("type");
            Log.e("message", pendingData);
            switch (type) {
                // Offer received for the logged in user's spot
                case "offerReceived":
                    goToViewOffers(dataObj.getString("lid"));
                    break;
                // Offer declined for a spot the logged in user made
                case "offerDeclined":
                    LatLng locash = new LatLng(dataObj.getDouble("latitude"), dataObj.getDouble("longitude"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locash, 16));
                    break;
                // Offer accepted for a spot the logged in user made
                case "offerAccepted":
                    startTrackingService(
                            dataObj.getString("lid"),
                            dataObj.getString("latitude"),
                            dataObj.getString("longitude"),
                            false);
                    getFirebaseData(lid);
                    break;
                // A spot the logged in user owns that is bought
                case "buy":

                    break;
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        hiddenPanel = findViewById(R.id.hidden_panel);

        tvQuantity = findViewById(R.id.tvQuantity);
        ivSellerProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvFullName = findViewById(R.id.tvFullName);
        tvUserRating = findViewById(R.id.tvUserRating);
        tvTotalRating = findViewById(R.id.tvTotalRating);
        tvTransaction = findViewById(R.id.tvTransaction);
        tvDescription = findViewById(R.id.tvDescription);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        tvLocationName = findViewById(R.id.tvLocationName);
        bBuyNow = findViewById(R.id.bBuyNow);
        bBuyNow.setOnClickListener(this);
        bMakeOffer = findViewById(R.id.bMakeOffer);
        bMakeOffer.setOnClickListener(this);
        bCancelOffer = findViewById(R.id.bCancelOffer);
        bCancelOffer.setOnClickListener(this);
        bDelete = findViewById(R.id.bDelete);
        bDelete.setOnClickListener(this);

        findViewById(R.id.fabMyLocation).setOnClickListener(this);
        findViewById(R.id.fabSpot).setOnClickListener(this);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
                drawerView.requestFocus();
                drawerView.requestLayout();
                navigationView.bringToFront();
                navigationView.requestFocus();
                navigationView.requestLayout();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        View navHeaderView = navigationView.getHeaderView(0);
        ivProfilePhoto = navHeaderView.findViewById(R.id.ivProfilePhoto);
        if (SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_photo_url)) != null) {
            Picasso.get().load(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_photo_url))).fit().centerCrop().into(ivProfilePhoto);
        }
        final TextView tvLoggedInFullName = navHeaderView.findViewById(R.id.tvLoggedInFullName);
        tvLoggedInFullName.setText(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_first_name)) + " " + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_last_name)));
        final TextView tvLoggedInEmail = navHeaderView.findViewById(R.id.tvLoggedInEmail);
        tvLoggedInEmail.setText(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_email)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabMyLocation:
                getMyLocation();
                break;
            case R.id.fabSpot:
                Bundle bundle = new Bundle();
                bundle.putString("locationName", locationName);
                bundle.putString("locationAddress", locationAddress);
                bundle.putDouble("latitude", latitude);
                bundle.putDouble("longitude", longitude);
                createSpotFragment = new CreateSpot();
                createSpotFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
                fragmentTransaction.add(R.id.drawer_layout, createSpotFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_TRACKING);
                } else {
                    if (type.equals("Sell")) {
                        validatePaymentMethod();
                    } else if (type.equals("Request")) {
                        /*
                         * TODO
                         * need to validate received payment form, PAYPAL!
                         * */
                        transactionBuyNow(1);
                    }
                }
                break;

            case R.id.bMakeOffer:
                if (quantity == 1) {
                    ADmakeOfferPrice(false, 1);
                } else {
                    ADmakeOfferQuantity();
                }
                break;

            case R.id.bCancelOffer:
                transactionCancelOffer();
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
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
                personalFragment = new Personal();
                fragment = personalFragment;
                break;
            case R.id.nav_payment:
                paymentFragment = new Payment();
                fragment = paymentFragment;
                break;
            case R.id.nav_about:
                fragment = new About();
                break;
            case R.id.nav_log_out:
                ADlogOut();
                break;
        }

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.bottom_in, R.animator.bottom_out, R.animator.bottom_in, R.animator.bottom_out);
            fragmentTransaction.add(R.id.drawer_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        drawer = findViewById(R.id.drawer_layout);
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

        searchMenuItem = menu.findItem(R.id.search);
        filterMenuItem = menu.findItem(R.id.filterMaps);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
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
                ADfilterSpotsByCategory();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (count > 0) {
                getFragmentManager().popBackStack();
            } else if (isMarkerClicked) {
                isMarkerClicked = false;
                hiddenPanel.startAnimation(bottomDown);
                hiddenPanel.setVisibility(View.INVISIBLE);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                locationName = place.getName().toString();
                locationAddress = place.getAddress().toString();
                LatLng locash = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locash, 16));

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

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locMan = locationManager;
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        this.provider = provider;

        myLocation = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_PERMISSION_MAP);
        } else {
            // Get Current Location
            myLocation = locationManager.getLastKnownLocation(provider);
            mMap.setMyLocationEnabled(true);
            double latitude = 0;
            double longitude = 0;

            try {
                latitude = myLocation.getLatitude();
                longitude = myLocation.getLongitude();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            currentLocation = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        mMap.getUiSettings().setMapToolbarEnabled(false); //disables the bottom right buttons that appear when you click on a marker
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            switch (requestCode){
                case ACCESS_FINE_LOCATION_PERMISSION_MAP:
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, true);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    myLocation = locationManager.getLastKnownLocation(provider);
                    mMap.setMyLocationEnabled(true);
                    double latitude = 0;
                    double longitude = 0;
                    try {
                        latitude = myLocation.getLatitude();
                        longitude = myLocation.getLongitude();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    currentLocation = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                    break;

                case ACCESS_FINE_LOCATION_PERMISSION_TRACKING:
                    if (type.equals("Sell")) {
                        validatePaymentMethod();
                    } else if (type.equals("Request")) {
                        transactionBuyNow(1);
                    }
                    break;

                case READ_EXTERNAL_STORAGE_PERMISSION:
                    personalFragment.onPermissionGranted();
                    break;
            }
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        this.marker = marker;
        isMarkerClicked = true;
        tvDescription.setVisibility(View.VISIBLE);
        bCancelOffer.setVisibility(View.VISIBLE);
        bMakeOffer.setVisibility(View.VISIBLE);
        bBuyNow.setVisibility(View.VISIBLE);
        bDelete.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/" + marker.getTag() + "?user=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("onMarker", response + "");
                    lid = response.getString("_id");
                    tvLocationName.setText(response.getString("name"));
                    price = response.getString("price");
                    quantity = response.getInt("quantity");

                    tvLocationAddress.setText(response.getString("address"));
                    JSONObject sellerInfoObj = response.getJSONObject("sellerInfo");
                    tvFullName.setText(sellerInfoObj.getString("sellerFirstName") + " " + sellerInfoObj.getString("sellerLastName"));
                    tvUserRating.setText(" - " + sellerInfoObj.getString("sellerOverallRating"));
                    tvTotalRating.setText("(" + sellerInfoObj.getString("sellerTotalRatings") + ")");
                    tvQuantity.setText(quantity + " " + getResources().getString(R.string.available));

                    if (sellerInfoObj.has("sellerProfilePhotoUrl")) {
                        Picasso.get().load(sellerInfoObj.getString("sellerProfilePhotoUrl")).fit().centerCrop().into(ivSellerProfilePhoto);
                    }
                    type = response.getString("type");
                    if (type.equals("Sell")) {
                        tvTransaction.setText(getResources().getString(R.string.Selling) + " - $" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Buy_Now));
                    } else if (type.equals("Request")) {
                        tvTransaction.setText(getResources().getString(R.string.Requesting) + " - $" + response.getString("price"));
                        bBuyNow.setText(getResources().getString(R.string.Accept));
                    }

                    if (sellerInfoObj.getString("sellerID").equals(SharedPref.getSharedPreferences(MapsActivity.this, getResources().getString(R.string.logged_in_user_id)))) {
                        bMakeOffer.setVisibility(View.GONE);
                        bCancelOffer.setVisibility(View.GONE);
                        bBuyNow.setVisibility(View.GONE);
                    } else {
                        bDelete.setVisibility(View.GONE);
                        if (!response.getBoolean("offerAllowed")) {
                            bMakeOffer.setVisibility(View.GONE);
                            bCancelOffer.setVisibility(View.GONE);
                        } else {
                            if (response.getBoolean("offered")) {
                                bMakeOffer.setVisibility(View.GONE);
                                bCancelOffer.setText(getResources().getString(R.string.Cancel) + " " + getResources().getString(R.string.Offer));
                            } else {
                                bCancelOffer.setVisibility(View.GONE);
                            }
                        }
                    }

                    if (!response.getString("description").isEmpty()) {
                        tvDescription.setText("\"" + response.getString("description") + "\"");
                    } else {
                        tvDescription.setVisibility(View.GONE);
                    }

                    double dLat = Double.valueOf(response.getString("latitude"));
                    double dLong = Double.valueOf(response.getString("longitude"));
                    spotLocation = new LatLng(dLat, dLong);
                } catch (JSONException e) {
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
        hiddenPanel.setElevation(20);

        return false;
    }

    @Override
    public void onCreditCardAdded(String from) {
        switch (from){
            case "Payment":
                getFragmentManager().popBackStack();
                getFragmentManager().popBackStack();
                paymentFragment.getCustomer();
                paymentFragment.setSnackbar(getString(R.string.Payment_method_added));
                break;

            case "CreateSpot":
                getFragmentManager().popBackStack();
                getFragmentManager().popBackStack();
                createSpotFragment.validatePaymentMethod();
                break;
            case "MapsActivity":

                break;
        }
    }

    @Override
    public void onPaymentMethodAdded(String from) {
        switch (from){
            case "Payment":
                getFragmentManager().popBackStack();
                paymentFragment.getCustomer();
                paymentFragment.setSnackbar(getString(R.string.Payment_method_added));
                break;
            case "CreateSpot":
                getFragmentManager().popBackStack();
                createSpotFragment.validatePaymentMethod();
                break;
            case "MapsActivity":
                validatePaymentMethod();
                break;
        }
    }

    @Override
    public void onSpotCreated(Double lat, Double lng, String name, String id) {
        Log.e("hi", "works");
        getFragmentManager().popBackStack();
        LatLng locash = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locash, 16));
        marker = mMap.addMarker(new MarkerOptions().position(locash).title(name));
        marker.setTag(id);
    }

    @Override
    public void onProfilePhotoChanged(String profilePhotoUrl) {
        Picasso.get().load(SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_photo_url))).fit().centerCrop().into(ivProfilePhoto);
    }

    @Override
    public void onOfferAccepted(final String lid, String id, final String latitude, final String longitude, String profilePhotoUrl) {
        // You accepted an offer and now it's going to redirect
        isOfferAccepted = true;
        lidBought = lid;
        idBought = id;
        latBought = latitude;
        lngBought = longitude;
        profilePhotoUrlBought = profilePhotoUrl;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mLocationRequestCallback)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void getMyLocation() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mLocationRequestCallback)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private GoogleApiClient.ConnectionCallbacks mLocationRequestCallback = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (isOfferAccepted){
                        Map<String, Object> latLng = new HashMap<>();
                        latLng.put("lat", location.getLatitude());
                        latLng.put("lng", location.getLongitude());

                        databaseReference = FirebaseDatabase.getInstance().getReference("tracking");
                        databaseReference.child(lidBought).child(SharedPref.getSharedPreferences(MapsActivity.this, getResources().getString(R.string.logged_in_user_id))).setValue(latLng, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    startTrackingService(lidBought, latBought, lngBought, true);
                                    getFirebaseData(lidBought);
                                } else {
                                    setSnackBar(getString(R.string.Server_error));
                                }
                            }
                        });
                        isOfferAccepted = false;
                    } else {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    }
                    mGoogleApiClient.disconnect();
                }
            });
        }

        @Override
        public void onConnectionSuspended(int reason) {
        }
    };

    private void getAvailableSpots(String typeSelected, String categorySelected){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/maps?type=" + typeSelected + "&category=" + categorySelected, null, new Response.Listener<JSONObject>() {
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
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(locash)
                                    .icon(BitmapDescriptorFactory.fromBitmap(customMarkerPrice("$"+locationObj.getString("price"), locationObj.getString("type"))))
                                    .title(locationObj.getString("name")));
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
        });
        queue.add(jsonObjectRequest);
    }

    private void transactionDeleteSpot(){
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, getString(R.string.URL) + "/location/delete/" + lid, null, new Response.Listener<JSONObject>() {
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

    private void transactionBuyNow(int quantity) {
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("buyerID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
            jObject.put("quantity", quantity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/transaction/buy/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pd.dismiss();
                try{
                    if (response.getString("status").equals("success")){
                        final String lidBought = response.getString("_id");
                        final String latBought = response.getString("latitude");
                        final String lngBought = response.getString("longitude");
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
                                    startTrackingService(lidBought, latBought, lngBought, false);
                                }
                                else {
                                    // There is an error
                                    setSnackBar(getString(R.string.Server_error));
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

    private void transactionMakeOffer(final String offerAmount, int quantity){
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("offererID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
            jObject.put("offerPrice", offerAmount);
            jObject.put("offerQuantity", quantity);
            jObject.put("offererFirstName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_first_name)));
            jObject.put("offererLastName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_last_name)));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/transaction/offer/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        Toast.makeText(MapsActivity.this, "Offer Made", Toast.LENGTH_SHORT).show();
                        bMakeOffer.setVisibility(View.GONE);
                        bCancelOffer.setText(getResources().getString(R.string.Cancel) + " $" + offerAmount + " " + getResources().getString(R.string.Offer));
                        bCancelOffer.setVisibility(View.VISIBLE);
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

    private void transactionCancelOffer(){
        final JSONObject jObject = new JSONObject();

        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/transaction/offer/cancel/" + lid + "?user=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        hiddenPanel.startAnimation(bottomDown);
                        hiddenPanel.setVisibility(View.INVISIBLE);
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.Offer_canceled), Toast.LENGTH_SHORT).show();
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

    private void ADmakeOfferQuantity(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.number_picker, null);

        final NumberPicker numberPicker = alertLayout.findViewById(R.id.npQuantity);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(this.quantity);
        numberPicker.setValue(1);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(getResources().getString(R.string.Make_Offer)+ " - " + getResources().getString(R.string.Quantity));
        alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ADmakeOfferPrice(true, numberPicker.getValue());
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

    private void ADmakeOfferPrice(final boolean hasMoreThanOne, final int quantity){
        String title = getResources().getString(R.string.Make_Offer);
        String positiveButton = getResources().getString(R.string.Offer);
        if (hasMoreThanOne){
            title += " - " + getResources().getString(R.string.Price);
            positiveButton = getResources().getString(R.string.OK);
        }
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.maps_activity_make_offer_alertdialog, null);

        final EditText etOfferPrice = alertLayout.findViewById(R.id.etOfferPrice);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (hasMoreThanOne){
                    ADconfirmQuantityAndPrice(etOfferPrice.getText().toString(), quantity);
                }
                else {
                    transactionMakeOffer(etOfferPrice.getText().toString(), 1);
                }
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

    private void ADconfirmQuantityAndPrice(final String offeredPrice, final int quantity){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        double price = Double.valueOf(offeredPrice);
        double totalPrice = price * quantity;
        alertDialogBuilder.setMessage(
                getResources().getString(R.string.Your_offer_is) + " $" +
                offeredPrice + " " +
                getResources().getString(R.string._for) + " " +
                quantity + " " +
                getResources().getString(R.string.spots_) + " " +
                getResources().getString(R.string.for_a_total_of) + ": $" +
                totalPrice);
        alertDialogBuilder.setTitle(getResources().getString(R.string.Make_Offer) + " - " + getResources().getString(R.string.Confirm));
        alertDialogBuilder.setPositiveButton(R.string.Offer, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                transactionMakeOffer(offeredPrice, quantity);
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
                LoginManager.getInstance().logOut();
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

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.URL) + "/payment/customer/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)),
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

        final NumberPicker npQuantity = alertLayout.findViewById(R.id.npQuantity);
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

    private void ADareYouSurePaymentMethod(String paymentType, String paymentCredentials, String paymentImageUrl, final String paymentToken, final int quantity){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.maps_activity_transaction_are_you_sure__alertdialog, null);


        final float totalPrice = Float.valueOf(price) * quantity;
        TextView tvCompleteTransactionDialog = alertLayout.findViewById(R.id.tvCompleteTransactionDialog);
        String completeTransactionText;
        if (quantity > 1){
            completeTransactionText = getResources().getString(R.string.You_will_be_charged) + " $" + totalPrice + " ($" + price + " x " + quantity + ") " + getResources().getString(R.string.with_the_following_payment_method);
        }
        else {
            completeTransactionText = getResources().getString(R.string.You_will_be_charged) + " $" + totalPrice + " " + getResources().getString(R.string.with_the_following_payment_method);
        }
        tvCompleteTransactionDialog.setText(completeTransactionText);
        ImageView ivPaymentImage = alertLayout.findViewById(R.id.ivPaymentImage);
        TextView tvPaymentName = alertLayout.findViewById(R.id.tvPaymentName);
        TextView tvPaymentCredentials = alertLayout.findViewById(R.id.tvPaymentCredentials);

        Picasso.get().load(paymentImageUrl).into(ivPaymentImage);
        tvPaymentName.setText(paymentType);
        tvPaymentCredentials.setText(paymentCredentials);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Complete_Transaction);
        alertDialogBuilder.setNegativeButton(R.string.Other_Method, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ADotherPaymentMethod(quantity, totalPrice);

            }
        });
        alertDialogBuilder.setPositiveButton(R.string.Complete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkout(paymentToken, quantity, totalPrice);
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADotherPaymentMethod(final int quantity, final float totalPrice){
        pd.setTitle(R.string.Loading);
        pd.setMessage(getResources().getString(R.string.Loading_payment_methods));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.URL) + "/payment/customer/" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)),
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
                                        checkout(tokenString[0], quantity, totalPrice);
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

    private void checkout(String token, final int quantity, float totalPrice){
        pd.setTitle(R.string.Completing);
        pd.setMessage(getResources().getString(R.string.Completing_transaction));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("paymentMethodToken", token);
            jsonObject.put("amount", totalPrice);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, getString(R.string.URL) +"/payment/checkout", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")){
                                transactionBuyNow(quantity);
                            } else {
                                ADerrorProcessingPayment();
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
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

    private void ADfilterSpotsByType(){
        String title = "";
        switch (categorySelected){
            case "All":
                title = getResources().getString(R.string.Filter_All_Spots);
                break;

            case "Parking":
                title = getResources().getString(R.string.Filter_Parking_Spots);
                break;

            case "Line":
                title = getResources().getString(R.string.Filter_Line_Spots);
                break;

            case "Other":
                title = getResources().getString(R.string.Filter_Spots);
                break;
        }

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setItems(R.array.type_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        typeSelected = "All";
                        break;
                    case 1:
                        typeSelected = "Sell";
                        break;
                    case 2:
                        typeSelected = "Request";
                        break;
                }

                getAvailableSpots(typeSelected, categorySelected);
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADfilterSpotsByCategory(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.Filter_Spots));
        alertDialogBuilder.setItems(R.array.category_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        categorySelected = "All";
                        break;
                    case 1:
                        categorySelected = "Parking";
                        break;
                    case 2:
                        categorySelected = "Line";
                        break;
                    case 3:
                        categorySelected = "Other";
                }

                ADfilterSpotsByType();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void refreshMap(){
        if (mMap != null){
            mMap.clear();
            getAvailableSpots(typeSelected, categorySelected);
        }
    }

    private void startTrackingService(String lid, String lat, String lng, boolean isSeller){
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("almanza1112.spottrade", "almanza1112.spottrade.nonActivity.tracking.TrackerService");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent serviceIntent = new Intent(this, TrackerService.class);
        serviceIntent.putExtra("lid", lid);
        serviceIntent.putExtra("lat", lat);
        serviceIntent.putExtra("lng", lng);
        serviceIntent.putExtra("isSeller", isSeller);
        this.startService(serviceIntent);
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

    private void checkOnGoingTransactions(){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/transaction/check?uid="+ SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    Log.e("onGoing", response + "");
                    if (response.getString("status").equals("success")){
                        JSONArray jsonArray = new JSONArray(response.getString("onGoingTransactions"));
                        String lidBought = jsonArray.getJSONObject(0).getString("lid");
                        Map<String, String> userInfo = new HashMap<>();
                        List<String> ids = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++){
                            String buyerID = jsonArray.getJSONObject(i).getString("buyerID");
                            String buyerProfilePhotoUrl = jsonArray.getJSONObject(i).getString("buyerProfilePhotoUrl");
                            ids.add(buyerID);
                            //marker = mMap.addMarker(new MarkerOptions().position(locash).title(locationObj.getString("name")));
                            //marker.setTag(locationObj.getString("_id"));
                        }
                        //getFirebaseData(lidBought);
                        progressBar.setVisibility(View.GONE);
                        //startTrackingService();
                    } else if (response.getString("status").equals("fail") && response.getString("reason").equals("no onGoingTransactions")){
                        getAvailableSpots(typeSelected, categorySelected);
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    boolean firstTime;
    private void getFirebaseData(String lidBought){
        searchMenuItem.setVisible(false);
        filterMenuItem.setVisible(false);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("tracking").child(lidBought);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    BuyerTracker buyerTracker = new BuyerTracker();
                    buyerTracker.setKey(ds.getKey());
                    buyerTracker.setLat(ds.getValue(BuyerTracker.class).getLat());
                    buyerTracker.setLng(ds.getValue(BuyerTracker.class).getLng());
                    if (!buyerTracker.getKey().equals(SharedPref.getSharedPreferences(MapsActivity.this, getString(R.string.logged_in_user_id)))){
                        LatLng locash = new LatLng(buyerTracker.getLat(), buyerTracker.getLng());
                        if (!firstTime){
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(locash)
                                    .icon(BitmapDescriptorFactory.fromBitmap(customMarkerProfilePhoto(SharedPref.getSharedPreferences(MapsActivity.this, getString(R.string.logged_in_user_photo_url))))));
                            firstTime = true;
                        } else {
                            animateMarker(marker, locash, false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setSnackBar(getString(R.string.Server_error));
            }
        });
    }

    private Bitmap customMarkerProfilePhoto(String profilePhotoUrl) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_user, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.ivProfilePhoto);
        Picasso.get().load(profilePhotoUrl).transform(new CircleTransform()).into(markerImageView);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private Bitmap customMarkerPrice(String price, String type) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_price, null);

        ImageView ivMarker = customMarkerView.findViewById(R.id.ivMarker);
        TextView tvPrice = customMarkerView.findViewById(R.id.tvPrice);

        if (type.equals("Sell")){
            ivMarker.setImageResource(R.drawable.spottrade_marker_primary);
            tvPrice.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            ivMarker.setImageResource(R.drawable.spottrade_marker_accent);
            tvPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        tvPrice.setText(price);

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void goToViewOffers(String lid){
        Bundle bundle = new Bundle();
        bundle.putString("lid", lid);
        ViewOffers viewOffers = new ViewOffers();
        viewOffers.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_layout, viewOffers);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void setSnackBar(String snackBarText){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), snackBarText, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
