package almanza1112.spottrade.search;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 6/27/17.
 */

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private double latitude, longitude;
    private String apiKey, query;
    RecyclerView rvLocations;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setLocationByRadius();
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                query = "query=" + etSearch.getText().toString();
                String searchURL = "https://maps.googleapis.com/maps/api/place/textsearch/json?" + query + "&location="+latitude+"," + longitude + "&radius=300&key=" + apiKey;
                searchPlaces(searchURL);
            }
        });

        rvLocations = (RecyclerView) findViewById(R.id.rvLocations);
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
    private void setLocationByRadius() {
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLastKnownLocation = locationManager.getLastKnownLocation(provider);
        latitude = myLastKnownLocation.getLatitude();
        longitude = myLastKnownLocation.getLongitude();
        apiKey = getResources().getString(R.string.google_maps_key);
        String mapsURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "," + longitude + "&radius=200&key=" + apiKey;
        searchPlaces(mapsURL);
    }

    private void searchPlaces(String url){
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        List<String> locationAddress = new ArrayList<>();
                        List<String> locationName = new ArrayList<>();
                        List<Double> locationLat = new ArrayList<>();
                        List<Double> locationLng = new ArrayList<>();
                        try {
                            String results = response.getString("results");
                            JSONArray jsonArray = new JSONArray(results);
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject resultObj = jsonArray.getJSONObject(i);
                                String name = resultObj.getString("name");
                                String address;
                                if (resultObj.has("vicinity")){
                                    address = resultObj.getString("vicinity");
                                }
                                else if (resultObj.has("formatted_address")){
                                    address = resultObj.getString("formatted_address");
                                }
                                else {
                                    address = "";
                                }
                                String geometry = resultObj.getString("geometry");
                                JSONObject geometryObj = new JSONObject(geometry);
                                String location = geometryObj.getString("location");
                                JSONObject locationObj = new JSONObject(location);
                                String lat = locationObj.getString("lat");
                                String lng = locationObj.getString("lng");

                                locationLat.add(Double.valueOf(lat));
                                locationLng.add(Double.valueOf(lng));
                                locationName.add(name);
                                locationAddress.add(address);
                            }
                            adapter = new SearchAdapter(SearchActivity.this, locationName, locationAddress, locationLat, locationLng);
                            layoutManager = new LinearLayoutManager(SearchActivity.this);
                            rvLocations.setLayoutManager(layoutManager);
                            rvLocations.setAdapter(adapter);

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
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);
    }
}
