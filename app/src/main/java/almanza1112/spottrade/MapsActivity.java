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
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        AddPaymentMethod.PaymentMethodAddedListener,
        AddCreditDebitCard.CreditCardAddedListener,
        ViewOffers.OfferAcceptedListener,
        CreateSpot.SpotCreatedListener,
        Personal.ProfilePhotoChangedListener,
        BottomSheetFilterMap.FilterMapListener {

    private static final String TAG = "MapsActivity";

    // for the google maps location
    private GoogleMap mMap;
    private Marker marker;
    private GoogleApiClient mGoogleApiClient;

    // for the navigationDrawer/menu
    NavigationView navigationView;
    DrawerLayout drawer;
    private ImageView ivProfilePhoto;

    private ProgressDialog pd = null;
    LatLng currentLocation, spotLocation;

    // for toolbar
    private Toolbar toolbar;
    private CardView cvToolbar;
    private RelativeLayout.LayoutParams tb;
    private View iProgressBar;

    // for Floating Action Buttons
    private FloatingActionButton fabCreateSpot;
    private FloatingActionButton fabMyLocation;

    // for creating a spot
    private boolean isCreateSpotStarted = false;

    // for filter map tags
    private String typeFilterSelected = "All", categoryFilterSelected = "All";
    private boolean offersAllowed;

    // for marker persistent bottom sheet
    private BottomSheetBehavior bottomSheetBehavior;
    private View iBottomSheetMarker;
    private RelativeLayout.LayoutParams rlToolbarLayoutParams;
    private RelativeLayout rlToolbar, bsToolbar;
    private TextView tvDescription, tvTimeAndDateAvailable, tvQuantityAvailable, tvPosterFirstNameAndRating, tvLocationName, tvLocationAddress, tvCategoryAndPrice, tvType;
    private ImageView ivCloseBottomSheet;
    private MaterialButton mbBuyNow, mbMakeOffer;
    private int quantityAvailable;

    // for fetching address service and receiver for appearing mid map
    private TextView tvMidAddress;
    private static final int SUCCESS_RESULT = 0;
    private static final int FAILURE_RESULT = 1;
    private static final int SUCCESS_RESULT_USING_GOOGLE_MAPS = 2;
    private static final String PACKAGE_NAME = "almanza1112.spottrade";
    private static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    private static final String RESULT_LOCATION_NAME = PACKAGE_NAME + ".RESULT_LOCATION_NAME";
    private static final String RESULT_LOCATION_ADDRESS = PACKAGE_NAME + ".RESULT_LOCATION_ADDRESS";
    private static final String RESULT_LOCATION_LATITUDE = PACKAGE_NAME + ".RESULT_LOCATION_LATITUDE";
    private static final String RESULT_LOCATION_LONGITUDE = PACKAGE_NAME + ".RESULT_LOCATION_LONGITUDE";
    private static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastKnownLocation;
    private AddressResultReceiver mResultReceiver;
    private String mAddressOutput;

    // for Place Autocomplete
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    // for mid-map LatLng
    private LatLng midCurrentLocation;


    private boolean isMarkerClicked;

    private double midLatitude = 0;
    private double midLongitude = 0;
    private String midLocationName = "empty";
    private String midLocationAddress = "empty";
    private String lidMarker, priceMarker, typeMarker;

    private final int ACCESS_FINE_LOCATION_PERMISSION_MAP = 5;
    private final int ACCESS_FINE_LOCATION_PERMISSION_TRACKING = 6;
    private final int READ_EXTERNAL_STORAGE_PERMISSION = 2;

    private DatabaseReference databaseReference;

    Payment paymentFragment;
    CreateSpot createSpotFragment;
    Personal personalFragment;
    BottomSheetFilterMap bottomSheetFilterMap;

    // for onOfferAccepted
    boolean isOfferAccepted;
    String lidBought, idBought, latBought, lngBought, profilePhotoUrlBought;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        // for fetching address service and receiver
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvMidAddress = findViewById(R.id.tvMidAddress);

        // for toolbar
        iProgressBar = findViewById(R.id.iProgressBar);
        cvToolbar = findViewById(R.id.cvToolbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // This code below gets the status height for fragments toolbars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            final int statusBarHeight = insets.getSystemWindowInsetTop();
            SharedPref.setSharedPreferences(MapsActivity.this, getResources().getString(R.string.status_bar_height), String.valueOf(statusBarHeight));
            //tb = (RelativeLayout.LayoutParams) rlToolbar.getLayoutParams();
            //tb.setMargins(0, statusBarHeight, 0, 0);
            //rlToolbarLayoutParams.setMargins(0, statusBarHeight, 0, 0); // for persistent bottom sheet marker
            return insets;
        });

        // for Floating Action Buttons
        fabCreateSpot = findViewById(R.id.fabCreateSpot);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        // for marker persistent bottom sheet
        iBottomSheetMarker = findViewById(R.id.iBottomSheetMarker);
        rlToolbar = iBottomSheetMarker.findViewById(R.id.rlToolbar);
        bsToolbar = iBottomSheetMarker.findViewById(R.id.bsToolBar);
        rlToolbarLayoutParams = (RelativeLayout.LayoutParams) bsToolbar.getLayoutParams();
        ivCloseBottomSheet = iBottomSheetMarker.findViewById(R.id.ivCloseBottomSheet);
        ivCloseBottomSheet.setOnClickListener(this);
        tvCategoryAndPrice = iBottomSheetMarker.findViewById(R.id.tvCategoryAndPrice);
        tvType = iBottomSheetMarker.findViewById(R.id.tvType);
        tvLocationName = iBottomSheetMarker.findViewById(R.id.tvLocationName);
        tvLocationAddress = iBottomSheetMarker.findViewById(R.id.tvLocationAddress);
        tvPosterFirstNameAndRating = iBottomSheetMarker.findViewById(R.id.tvPosterFirstNameAndRating);
        tvQuantityAvailable = iBottomSheetMarker.findViewById(R.id.tvQuantityAvailable);
        tvTimeAndDateAvailable = iBottomSheetMarker.findViewById(R.id.tvStartTimeAndDate);
        tvDescription = iBottomSheetMarker.findViewById(R.id.tvDescription);
        mbBuyNow = iBottomSheetMarker.findViewById(R.id.mbBuyNow);
        mbMakeOffer = iBottomSheetMarker.findViewById(R.id.mbMakeOffer);
        bottomSheetBehavior = BottomSheetBehavior.from(iBottomSheetMarker);

        final TypedArray ta = getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = (int) ta.getDimension(0, 0);
        SharedPref.setSharedPreferences(this, getResources().getString(R.string.action_bar_height), String.valueOf(actionBarHeight));

        pd = new ProgressDialog(this);

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

        // for notifications
        try {
            String pendingData = getIntent().getExtras().getString("message");
            JSONObject dataObj = new JSONObject(pendingData);
            String type = dataObj.getString("typeMarker");
            Log.e("message", pendingData);
            switch (type) {
                // Offer received for the logged in user's spot
                case "offerReceived":
                    goToViewOffers(dataObj.getString("lidMarker"));
                    break;
                // Offer declined for a spot the logged in user made
                case "offerDeclined":
                    LatLng locash = new LatLng(dataObj.getDouble("latitude"), dataObj.getDouble("longitude"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locash, 16));
                    break;
                // Offer accepted for a spot the logged in user made
                case "offerAccepted":
                    startTrackingService(
                            dataObj.getString("lidMarker"),
                            dataObj.getString("latitude"),
                            dataObj.getString("longitude"),
                            false);
                    getFirebaseData(lidMarker);
                    break;
                // A spot the logged in user owns that is bought
                case "buy":

                    break;
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        findViewById(R.id.fabMyLocation).setOnClickListener(this);
        findViewById(R.id.fabCreateSpot).setOnClickListener(this);

        // for the navigation drawer/side menu
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

        // for the menu photo/email aka account info area on the top
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

            case R.id.fabCreateSpot:
                createSpot();
                /*
                Bundle bundle = new Bundle();
                bundle.putString("locationName", midLocationName);
                bundle.putString("locationAddress", midLocationAddress);
                bundle.putDouble("latitude", midLatitude);
                bundle.putDouble("longitude", midLongitude);
                createSpotFragment = new CreateSpot();
                createSpotFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
                fragmentTransaction.add(R.id.drawer_layout, createSpotFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                 */
                break;

            case R.id.bDelete:
                ADdeleteSpot();
                break;

                /*
            case R.id.bBuyNow:

                // TODO: MAJOR ISSUE REGARDING REQUEST
                // For when a user that purchases a spot that is selling, code is easy to reject buyer
                // if card gets rejected etc. Need to verify the requester's payment as well as not
                // allowing them to delete their default payment if they have a spot up for Request
                // and if upon purchase of the person accepting the request, throw any errors Gateway,
                // card validation etc.


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_TRACKING);
                } else {
                    if (typeMarker.equals("Sell")) {
                        validatePaymentMethod();
                    } else if (typeMarker.equals("Request")) {

                          //TODO: need to validate received payment form, PAYPAL!

                        transactionBuyNow(1);
                    }
                }
                break; */

                /*
            case R.id.bMakeOffer:
                if (quantityAvailable == 1) {
                    ADmakeOfferPrice(false, 1);
                } else {
                    ADmakeOfferQuantity();
                }
                break;

            case R.id.bCancelOffer:
                transactionCancelOffer();
                break;
                */
            case R.id.ivCloseBottomSheet:
                onBackPressed();
                break;
        }
    }

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
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
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
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (count > 0) {
                getSupportFragmentManager().popBackStack();
            } else if (isCreateSpotStarted){
                showToolbarAndFABs();
            } else if (isMarkerClicked) {
                isMarkerClicked = false;
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.iSearch:
                // Set the fields to specify which types of place date to return after the user has made a selection
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

                // Start the autocomplete
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;

            case R.id.iFilterMaps:
                BSfilterMapType();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                midLatitude = place.getLatLng().latitude;
                midLongitude = place.getLatLng().longitude;
                midLocationName = place.getName();
                midLocationAddress = place.getAddress();
                toolbar.setTitle(midLocationName);
                toolbar.setSubtitle(midLocationAddress);
                LatLng locash = new LatLng(midLatitude, midLongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locash, 16));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
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
            // Customise the styling of the base map using a JSON object define in a raw resource file.
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locMan = locationManager;
        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        this.provider = locationManager.getBestProvider(criteria, true);

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_MAP);
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                }
            });
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true); // this is for the blue dot that shows exactly where you are in the map
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false); // false disables the bottom right buttons that appear when you click on a marker
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);

        // This gets the current address in the middle of the map
        mMap.setOnCameraIdleListener(() -> {
            if (ContextCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LatLng midLatLng = mMap.getCameraPosition().target;
                Location midLocation = new Location("");
                midCurrentLocation = new LatLng(midLocation.getLatitude(), midLocation.getLongitude());
                midLocation.setLatitude(midLatLng.latitude);
                midLocation.setLongitude(midLatLng.longitude);
                //startIntentService(midLocation);
            } else {
                // Ask for permission
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION_MAP);
            }


        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case ACCESS_FINE_LOCATION_PERMISSION_MAP:
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                        }
                    });
                    break;

                case ACCESS_FINE_LOCATION_PERMISSION_TRACKING:
                    if (typeMarker.equals("Sell")) {
                        validatePaymentMethod();
                    } else if (typeMarker.equals("Request")) {
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
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        this.marker = marker;
        isMarkerClicked = true;

        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/" + marker.getTag() + "?user=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("response", response+"");
                    lidMarker = response.getString("_id");
                    priceMarker = response.getString("price");
                    quantityAvailable = response.getInt("quantity");

                    tvCategoryAndPrice.setText(response.getString("category") + " - $"+priceMarker);
                    tvLocationName.setText(response.getString("name"));
                    tvLocationAddress.setText(response.getString("address"));
                    JSONObject posterInfoObj = response.getJSONObject("posterInfo");
                    tvPosterFirstNameAndRating.setText(posterInfoObj.getString("posterFirstName") + " " + posterInfoObj.getString("posterOverallRating") + "(" + posterInfoObj.getString("posterTotalRatings") + ")");
                    tvQuantityAvailable.setText(String.valueOf(quantityAvailable) + " " + getResources().getString(R.string.available));
                    tvTimeAndDateAvailable.setText(epochToDateString(response.getLong("dateTimeStart")));

                    typeMarker = response.getString("type");
                    if (typeMarker.equals("Sell")) {
                        tvType.setText(getResources().getString(R.string.Selling));
                        // tvTypeAndPrice.setText(getResources().getString(R.string.Selling) + " - $" + response.getString("priceMarker"));
                        // bBuyNow.setText(getResources().getString(R.string.Buy_Now));
                    } else if (typeMarker.equals("Request")) {
                        tvType.setText(getResources().getString(R.string.Requesting));
                        // tvTypeAndPrice.setText(getResources().getString(R.string.Requesting) + " - $" + response.getString("priceMarker"));
                        // bBuyNow.setText(getResources().getString(R.string.Accept));
                    }

                    if (!response.getString("description").isEmpty()) {
                        tvDescription.setText("\"" + response.getString("description") + "\"");
                    } else {
                        tvDescription.setVisibility(View.GONE);
                    }

                    if (!response.getBoolean("offerAllowed")){
                        mbMakeOffer.setVisibility(View.GONE);
                    } else {
                        mbMakeOffer.setVisibility(View.VISIBLE);
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
        return false;
    }

    @Override
    public void onCreditCardAdded(String from) {
        switch (from) {
            case "Payment":
                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().popBackStack();
                paymentFragment.getCustomer();
                paymentFragment.setSnackbar(getString(R.string.Payment_method_added));
                break;

            case "CreateSpot":
                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().popBackStack();
                // TODO: find out what the hell this last line is doing
                //createSpotFragment.validatePaymentMethod();
                break;
            case "MapsActivity":

                break;
        }
    }

    @Override
    public void onPaymentMethodAdded(String from) {
        switch (from) {
            case "Payment":
                getSupportFragmentManager().popBackStack();
                paymentFragment.getCustomer();
                paymentFragment.setSnackbar(getString(R.string.Payment_method_added));
                break;
            case "CreateSpot":
                getSupportFragmentManager().popBackStack();
                // TODO: find out what the hell this last line is doing
                //createSpotFragment.validatePaymentMethod();
                break;
            case "MapsActivity":
                validatePaymentMethod();
                break;
        }
    }

    @Override
    public void onSpotCreated(Double lat, Double lng, String name, String id) {
        getSupportFragmentManager().popBackStack();
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

    @Override
    public void onFilterMap(String type, String category, boolean offersAllowed) {
        bottomSheetFilterMap.dismiss();
        typeFilterSelected = type;
        categoryFilterSelected = category;
        this.offersAllowed = offersAllowed;
        getAvailableSpots(type, category);
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
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (isOfferAccepted) {
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

    private void getAvailableSpots(final String type, final String category) {
        iProgressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/maps?type=" + type + "&category=" + category, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        mMap.clear();
                        String locations = response.getString("location");
                        JSONArray jsonArray = new JSONArray(locations);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject locationObj = jsonArray.getJSONObject(i);
                            Double lat = Double.valueOf(locationObj.getString("latitude"));
                            Double lng = Double.valueOf(locationObj.getString("longitude"));
                            LatLng locash = new LatLng(lat, lng);

                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(locash)
                                    .icon(BitmapDescriptorFactory.fromBitmap(customMarkerPrice("$" + locationObj.getString("price"), locationObj.getString("type")))));
                            marker.setTag(locationObj.getString("_id"));
                        }
                        iProgressBar.setVisibility(View.GONE);

                        typeFilterSelected = type;
                        categoryFilterSelected = category;
                    }
                } catch (JSONException e) {
                    iProgressBar.setVisibility(View.GONE);
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

    private void transactionDeleteSpot() {
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, getString(R.string.URL) + "/location/delete/" + lidMarker, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        marker.remove();
                        //hiddenPanel.startAnimation(bottomDown);
                        //hiddenPanel.setVisibility(View.INVISIBLE);
                    }
                } catch (JSONException e) {
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
            jObject.put("lid", lidMarker);
            jObject.put("buyerID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
            jObject.put("quantityBought", quantity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/transaction/buy/", jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pd.dismiss();
                try {
                    if (response.getString("status").equals("success")) {
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
                                if (databaseError == null) {
                                    // There is no error
                                    startTrackingService(lidBought, latBought, lngBought, false);
                                } else {
                                    // There is an error
                                    setSnackBar(getString(R.string.Server_error));
                                }
                            }
                        });
                        //startNavigationApp(response.getString("latitude"), response.getString("longitude"), response.getString("name"));
                    }
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
    }

    private void transactionMakeOffer(final String offerAmount, int quantity) {
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("offererID", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)));
            jObject.put("offerPrice", offerAmount);
            jObject.put("offerQuantity", quantity);
            jObject.put("offererFirstName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_first_name)));
            jObject.put("offererLastName", SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_last_name)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/transaction/offer/" + lidMarker, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        Toast.makeText(MapsActivity.this, "Offer Made", Toast.LENGTH_SHORT).show();
                        // bMakeOffer.setVisibility(View.GONE);
                        // bCancelOffer.setText(getResources().getString(R.string.Cancel) + " $" + offerAmount + " " + getResources().getString(R.string.Offer));
                        // bCancelOffer.setVisibility(View.VISIBLE);
                    }
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
    }

    private void transactionCancelOffer() {
        final JSONObject jObject = new JSONObject();

        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/transaction/offer/cancel/" + lidMarker + "?user=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("success")) {
                        //hiddenPanel.startAnimation(bottomDown);
                        //hiddenPanel.setVisibility(View.INVISIBLE);
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.Offer_canceled), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapsActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
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
    }

    private void ADmakeOfferQuantity() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.number_picker, null);

        final NumberPicker numberPicker = alertLayout.findViewById(R.id.npQuantity);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(this.quantityAvailable);
        numberPicker.setValue(1);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(getResources().getString(R.string.Make_Offer) + " - " + getResources().getString(R.string.Quantity));
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

    private void ADmakeOfferPrice(final boolean hasMoreThanOne, final int quantity) {
        String title = getResources().getString(R.string.Make_Offer);
        String positiveButton = getResources().getString(R.string.Offer);
        if (hasMoreThanOne) {
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
                if (hasMoreThanOne) {
                    ADconfirmQuantityAndPrice(etOfferPrice.getText().toString(), quantity);
                } else {
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

    private void ADconfirmQuantityAndPrice(final String offeredPrice, final int quantity) {
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

    private void ADdeleteSpot() {
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

    private void ADlogOut() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogCustomTheme);
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

    private void validatePaymentMethod() {
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
                                if (jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        if (jsonArray.getJSONObject(i).getBoolean("default")) {
                                            if (jsonArray.getJSONObject(i).has("cardType")) {
                                                //means the default payment is a credit card
                                                int len = jsonArray.getJSONObject(i).getString("maskedNumber").length() - 4;
                                                String astr = "";
                                                for (int j = 0; j < len; j++) {
                                                    astr += "*";
                                                }
                                                String last4 = astr + jsonArray.getJSONObject(i).getString("last4");
                                                if (quantityAvailable > 1) {
                                                    ADselectQuantity(
                                                            jsonArray.getJSONObject(i).getString("cardType"),
                                                            last4,
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"));
                                                } else {
                                                    ADareYouSurePaymentMethod(
                                                            jsonArray.getJSONObject(i).getString("cardType"),
                                                            last4,
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"),
                                                            1);
                                                }
                                            } else {
                                                //means the default payment is PayPal
                                                if (quantityAvailable > 1) {
                                                    ADselectQuantity(
                                                            "PayPal",
                                                            jsonArray.getJSONObject(i).getString("email"),
                                                            jsonArray.getJSONObject(i).getString("imageUrl"),
                                                            jsonArray.getJSONObject(i).getString("token"));
                                                } else {
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
                                } else {
                                    pd.dismiss();
                                    ADnoPaymentMethod();
                                }
                            } else if (response.getString("status").equals("fail")) {
                                pd.dismiss();
                                ADnoPaymentMethod();
                            }
                        } catch (JSONException e) {
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

    private void ADselectQuantity(final String paymentType, final String paymentCredentials, final String paymentImageUrl, final String paymentToken) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.number_picker, null);

        final NumberPicker npQuantity = alertLayout.findViewById(R.id.npQuantity);
        npQuantity.setMinValue(1);
        npQuantity.setMaxValue(quantityAvailable);

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

    private void ADareYouSurePaymentMethod(String paymentType, String paymentCredentials, String paymentImageUrl, final String paymentToken, final int quantity) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.maps_activity_transaction_are_you_sure__alertdialog, null);


        final float totalPrice = Float.valueOf(priceMarker) * quantity;
        TextView tvCompleteTransactionDialog = alertLayout.findViewById(R.id.tvCompleteTransactionDialog);
        String completeTransactionText;
        if (quantity > 1) {
            completeTransactionText = getResources().getString(R.string.You_will_be_charged) + " $" + totalPrice + " ($" + priceMarker + " x " + quantity + ") " + getResources().getString(R.string.with_the_following_payment_method);
        } else {
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

    private void ADotherPaymentMethod(final int quantity, final float totalPrice) {
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
                                if (customerObj.has("creditCards")) {
                                    JSONArray creditCardsArray = new JSONArray(customerObj.getString("creditCards"));
                                    for (int i = 0; i < creditCardsArray.length(); i++) {
                                        paymentType.add("creditCard");
                                        paymentTypeName.add(creditCardsArray.getJSONObject(i).getString("cardType"));
                                        int len = creditCardsArray.getJSONObject(i).getString("maskedNumber").length() - 4;
                                        String astr = "";
                                        for (int j = 0; j < len; j++) {
                                            astr += "*";
                                        }
                                        token.add(creditCardsArray.getJSONObject(i).getString("token"));
                                        credentials.add(astr + creditCardsArray.getJSONObject(i).getString("last4"));
                                    }
                                }
                                if (customerObj.has("paypalAccounts")) {
                                    JSONArray paypalAccountsArray = new JSONArray(customerObj.getString("paypalAccounts"));
                                    for (int i = 0; i < paypalAccountsArray.length(); i++) {
                                        paymentType.add("paypal");
                                        paymentTypeName.add("PayPal");
                                        token.add(paypalAccountsArray.getJSONObject(i).getString("token"));
                                        credentials.add(paypalAccountsArray.getJSONObject(i).getString("email"));
                                    }
                                }
                                CharSequence[] csArr = new CharSequence[paymentType.size()];
                                for (int i = 0; i < paymentType.size(); i++) {
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

                            } else if (!response.getString("status").equals("fail")) {
                                Toast.makeText(MapsActivity.this, "Error: could not retrieve payment methods", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
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

    private void ADnoPaymentMethod() {
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
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.drawer_layout, addPaymentMethod);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // for creating a spot
    private void createSpot(){
        hideToolbarAndFABs();
    }

    private void hideToolbarAndFABs(){
        isCreateSpotStarted = true;
        cvToolbar.animate()
                .translationX(-toolbar.getWidth())
                .alpha(1.0f)
                .setDuration(250)
                .start();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midCurrentLocation, 18));

        fabMyLocation.hide();
        fabCreateSpot.hide();
    }

    private void showToolbarAndFABs(){
        isCreateSpotStarted = false;
        cvToolbar.animate()
                .translationX(0)
                .alpha(1.0f)
                .start();

        fabMyLocation.show();
        fabCreateSpot.show();
    }

    private void checkout(String token, final int quantity, float totalPrice) {
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
                (Request.Method.POST, getString(R.string.URL) + "/payment/checkout", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                transactionBuyNow(quantity);
                            } else {
                                ADerrorProcessingPayment();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
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

    private void ADerrorProcessingPayment() {
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

    private void BSfilterMapType() {
        Bundle bundle = new Bundle();
        bundle.putString("type", typeFilterSelected);
        bundle.putString("category", categoryFilterSelected);
        bottomSheetFilterMap = new BottomSheetFilterMap();
        bottomSheetFilterMap.setArguments(bundle);
        bottomSheetFilterMap.show(getSupportFragmentManager(), bottomSheetFilterMap.getTag());
    }

    private void refreshMap() {
        if (mMap != null) {
            mMap.clear();
            getAvailableSpots(typeFilterSelected, categoryFilterSelected);
        }
    }

    private void startTrackingService(String lid, String lat, String lng, boolean isSeller) {
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName("almanza1112.spottrade", "almanza1112.spottrade.nonActivity.tracking.TrackerService");
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent serviceIntent = new Intent(this, TrackerService.class);
        serviceIntent.putExtra("lidMarker", lid);
        serviceIntent.putExtra("lat", lat);
        serviceIntent.putExtra("lng", lng);
        serviceIntent.putExtra("isSeller", isSeller);
        this.startService(serviceIntent);
    }

    private void startNavigationApp(String lat, String lng, String label) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(" + Uri.encode(label) + ")");
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

    private void checkOnGoingTransactions() {
        iProgressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/transaction/check?uid=" + SharedPref.getSharedPreferences(this, getResources().getString(R.string.logged_in_user_id)) + "&offersAllowed=" + offersAllowed, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e("onGoing", response + "");
                    if (response.getString("status").equals("success")) {
                        JSONArray jsonArray = new JSONArray(response.getString("onGoingTransactions"));
                        String lidBought = jsonArray.getJSONObject(0).getString("lidMarker");
                        Map<String, String> userInfo = new HashMap<>();
                        List<String> ids = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String buyerID = jsonArray.getJSONObject(i).getString("buyerID");
                            String buyerProfilePhotoUrl = jsonArray.getJSONObject(i).getString("buyerProfilePhotoUrl");
                            ids.add(buyerID);
                            //marker = mMap.addMarker(new MarkerOptions().position(locash).title(locationObj.getString("name")));
                            //marker.setTag(locationObj.getString("_id"));
                        }
                        //getFirebaseData(lidBought);
                        iProgressBar.setVisibility(View.GONE);
                        //startTrackingService();
                    } else if (response.getString("status").equals("fail") && response.getString("reason").equals("no onGoingTransactions")) {
                        getAvailableSpots(typeFilterSelected, categoryFilterSelected);
                        iProgressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    iProgressBar.setVisibility(View.GONE);
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                iProgressBar.setVisibility(View.GONE);
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    boolean firstTime;

    private void getFirebaseData(String lidBought) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("tracking").child(lidBought);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    BuyerTracker buyerTracker = new BuyerTracker();
                    buyerTracker.setKey(ds.getKey());
                    buyerTracker.setLat(ds.getValue(BuyerTracker.class).getLat());
                    buyerTracker.setLng(ds.getValue(BuyerTracker.class).getLng());
                    if (!buyerTracker.getKey().equals(SharedPref.getSharedPreferences(MapsActivity.this, getString(R.string.logged_in_user_id)))) {
                        LatLng locash = new LatLng(buyerTracker.getLat(), buyerTracker.getLng());
                        if (!firstTime) {
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

        if (type.equals("Sell")) {
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

    private void goToViewOffers(String lid) {
        Bundle bundle = new Bundle();
        bundle.putString("lidMarker", lid);
        ViewOffers viewOffers = new ViewOffers();
        viewOffers.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.drawer_layout, viewOffers);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private String epochToDateString(long epochSeconds) {
        // TODO: need to check this again in future
        Date updatedate = new Date(epochSeconds);
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy 'at' H:mm a", Locale.getDefault());
        return format.format(updatedate);
    }

    public void setSnackBar(String snackBarText) {
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

    private void startIntentService(Location midLatLng){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(RECEIVER, mResultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, midLatLng);
        startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            // Display the address string
            // or error message sent from Intent Service
            if (resultCode == SUCCESS_RESULT){
                mAddressOutput = resultData.getString(RESULT_LOCATION_ADDRESS);
                tvMidAddress.setText(mAddressOutput);
            } else if (resultCode == SUCCESS_RESULT_USING_GOOGLE_MAPS){
                mAddressOutput = resultData.getString(RESULT_LOCATION_ADDRESS);
                tvMidAddress.setText(mAddressOutput);
            } else if (resultCode == FAILURE_RESULT){
                setSnackBar("Unable to get location");
            }
        }
    }
}
