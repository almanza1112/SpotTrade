package almanza1112.spottrade.account.history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 7/25/17.
 */

public class History extends AppCompatActivity {

    RecyclerView rvHistory;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(R.string.History);

        rvHistory = (RecyclerView) findViewById(R.id.rvHistory);
        getHistory();
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

    private void getHistory(){
        RequestQueue queue = Volley.newRequestQueue(this);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/all?sellerID="+ SharedPref.getID(this) + "&transaction=complete&type=all", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", response+ "");
                try{
                    /*

                    1.) Bought a spot being sold
                    2.) Bought a spotter/when you request
                    3.) Sold a spot
                    4.) accept a request
                    Spotter
                     */
                    if (response.getString("status").equals("success")){
                        List<String> type = new ArrayList<>();
                        List<String> description = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        List<String> dateCompleted = new ArrayList<>();
                        List<String> locationName = new ArrayList<>();
                        List<String> locationAddress = new ArrayList<>();
                        List<String> latitude = new ArrayList<>();
                        List<String> longitude = new ArrayList<>();
                        List<String> buyerID = new ArrayList<>();
                        List<String> buyerName = new ArrayList<>();
                        List<String> sellerID = new ArrayList<>();
                        List<String> sellerName = new ArrayList<>();

                        String locations = response.getString("location");
                        JSONArray jsonArray = new JSONArray(locations);

                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject locationObj = jsonArray.getJSONObject(i);
                            type.add(locationObj.getString("type"));
                            description.add(locationObj.getString("description"));
                            price.add(locationObj.getString("price"));
                            //dateCompleted.add(locationObj.getLong("dateCompleted"));
                            String convertedDate = epochToDateString(locationObj.getLong("dateCompleted"));
                            dateCompleted.add(convertedDate);
                            locationName.add(locationObj.getString("name"));
                            locationAddress.add(locationObj.getString("address"));
                            latitude.add(locationObj.getString("latitude"));
                            longitude.add(locationObj.getString("longitude"));
                            buyerID.add(locationObj.getString("buyerID"));
                            sellerID.add(locationObj.getString("sellerID"));

                            String buyerInfoString = locationObj.getString("buyerInfo");
                            JSONObject buyerInfoObj = new JSONObject(buyerInfoString);
                            buyerName.add(buyerInfoObj.getString("buyerFirstName") + " " + buyerInfoObj.getString("buyerLastName"));

                            String sellerInfoString = locationObj.getString("sellerInfo");
                            JSONObject sellerInfoObj = new JSONObject(sellerInfoString);
                            sellerName.add(sellerInfoObj.getString("sellerFirstName") + " " + sellerInfoObj.getString("sellerLastName"));
                        }

                        adapter = new HistoryAdapter(History.this, type, description, price, dateCompleted, locationName, locationAddress, latitude, longitude, buyerID, buyerName, sellerID, sellerName);
                        layoutManager = new LinearLayoutManager(History.this);
                        rvHistory.setLayoutManager(layoutManager);
                        rvHistory.setAdapter(adapter);
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

    private String epochToDateString(long epochSeconds) {
        Date updatedate = new Date(epochSeconds * 1000);
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy 'at' H:mm a", Locale.getDefault());
        return format.format(updatedate);
    }
}