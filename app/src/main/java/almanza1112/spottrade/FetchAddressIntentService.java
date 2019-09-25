package almanza1112.spottrade;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class FetchAddressIntentService extends IntentService {

    private static final int SUCCESS_RESULT = 0;
    private static final int FAILURE_RESULT = 1;
    private static final int SUCCESS_RESULT_USING_GOOGLE_MAPS = 2;
    private static final String PACKAGE_NAME = "almanza1112.spottrade";
    private static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    private static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    private static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    protected ResultReceiver mReceiver;
    private boolean geoCoderSuccessful;
    protected String response;
    protected boolean addressFound;
    private boolean testAPI = false; // set to true only when you want to test the google maps api

    public FetchAddressIntentService() {
        super("name");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String errorMessage = "";
        geoCoderSuccessful = false;
        addressFound = false;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // get the location passed through this service through extra

        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(RECEIVER);

        List<Address> addresses = null;

        if (geocoder.isPresent() && !testAPI) {
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                geoCoderSuccessful = true;
            } catch (IOException e) {
                errorMessage = "Service not available";
                Log.e(TAG, errorMessage, e);
                e.printStackTrace();
            } catch (IllegalArgumentException illegal) {
                // catch invalid lat and long
                errorMessage = "Invalid latitude and longitude used";
                Log.e(TAG, errorMessage + ", " + "Lat = " + location.getLatitude() + ", Long = " + location.getLongitude());
            }

            if (addresses == null || addresses.size() == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = "No address found";
                    Log.e(TAG, errorMessage);
                }
                deliverResultsToReceiver(FAILURE_RESULT, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<>();

                // Fetch the address lines using getAddressLine
                // join them and send them to MapsActivity

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.e(TAG, "Address Found");
                addressFound = true;
                deliverResultsToReceiver(SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
            }
        }

        if ((!geocoder.isPresent() || !geoCoderSuccessful) && isOnline()){
            // geoCoder fallback code
            // API call to Google Maps API

            GetHttp googleMapsApi = new GetHttp();

            String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&key=" + getResources().getString(R.string.google_maps_key) + "&sensor=true";

            try {
                response = googleMapsApi.run(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Create JSON Object
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                String status = jsonObject.getString("status").toString();
                if (status.equalsIgnoreCase("OK")){
                    JSONArray results = jsonObject.getJSONArray("results");
                    // get first object i.e. just one address
                    JSONObject resultsObject = results.getJSONObject(0);
                    // get formatted address
                    String formattedAddressApi = resultsObject.getString("formatted_address").toString();
                    Log.e(TAG, "Address found using Google Maps API");
                    addressFound = true;
                    deliverResultsToReceiver(SUCCESS_RESULT_USING_GOOGLE_MAPS, formattedAddressApi);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                errorMessage = "Google Maps API Failed";
                deliverResultsToReceiver(FAILURE_RESULT, errorMessage);
            }


        }

        if (!addressFound && !isOnline()){
            errorMessage = "Geocoder failed and no Internet access";
            Log.e(TAG, errorMessage);
            deliverResultsToReceiver(FAILURE_RESULT, errorMessage);
        }
    }

    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void deliverResultsToReceiver(int failureResult, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        mReceiver.send(failureResult, bundle);
    }

    public class GetHttp {

        OkHttpClient client = new OkHttpClient();

        String run(String url) throws IOException{
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()){
                return response.body().string();
            }
        }


    }
}
