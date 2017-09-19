package almanza1112.spottrade.account.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class History extends Fragment {

    RecyclerView rvHistory;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    final int[] pos = {2};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.History);

        AppCompatActivity actionBar = (AppCompatActivity) getActivity();
        actionBar.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) actionBar.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        rvHistory = (RecyclerView) view.findViewById(R.id.rvHistory);
        getHistory("all");
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(false);
        MenuItem filterItem = menu.findItem(R.id.filterMaps);
        filterItem.setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filterHistory){
            final CharSequence[] items = {getResources().getString(R.string.Sell), getResources().getString(R.string.Request), getResources().getString(R.string.All)};
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getResources().getString(R.string.Filter) + " " + getResources().getString(R.string.History));
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
                    getHistory(type);
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
        }
        return true;
    }

    private void getHistory(String type){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/history?sellerID="+ SharedPref.getID(getActivity()) + "&type=" + type, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    Log.e("response", response + "");
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
                        List<String> profilePhotoUrl = new ArrayList<>();

                        String locations = response.getString("location");
                        JSONArray jsonArray = new JSONArray(locations);

                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject locationObj = jsonArray.getJSONObject(i);
                            Log.e("historyArr", locationObj + "");
                            type.add(locationObj.getString("type"));
                            description.add(locationObj.getString("description"));
                            price.add(locationObj.getString("price"));
                            String convertedDate = epochToDateString(locationObj.getLong("dateCompleted"));
                            dateCompleted.add(convertedDate);
                            locationName.add(locationObj.getString("name"));
                            locationAddress.add(locationObj.getString("address"));
                            latitude.add(locationObj.getString("latitude"));
                            longitude.add(locationObj.getString("longitude"));
                            buyerID.add(locationObj.getString("buyerID"));
                            sellerID.add(locationObj.getString("sellerID"));

                            String buyerProfilePhotoUrl = "empty";
                            if (locationObj.has("buyerInfo")){
                                String buyerInfoString = locationObj.getString("buyerInfo");
                                JSONObject buyerInfoObj = new JSONObject(buyerInfoString);
                                buyerName.add(buyerInfoObj.getString("buyerFirstName") + " " + buyerInfoObj.getString("buyerLastName"));
                                if (buyerInfoObj.has("buyerProfilePhotoUrl")){
                                    buyerProfilePhotoUrl = buyerInfoObj.getString("buyerProfilePhotoUrl");
                                }
                                else {
                                    buyerProfilePhotoUrl = "empty";
                                }
                            }
                            else {
                                buyerName.add("ERROR");
                            }

                            String sellerInfoString = locationObj.getString("sellerInfo");
                            JSONObject sellerInfoObj = new JSONObject(sellerInfoString);
                            sellerName.add(sellerInfoObj.getString("sellerFirstName") + " " + sellerInfoObj.getString("sellerLastName"));


                            if (buyerID.get(i).equals(SharedPref.getID(getActivity()))){
                                if (sellerInfoObj.has("sellerProfilePhotoUrl")){
                                    profilePhotoUrl.add(sellerInfoObj.getString("sellerProfilePhotoUrl"));
                                }
                                else {
                                    profilePhotoUrl.add("empty");
                                }
                            }
                            else {
                                profilePhotoUrl.add(buyerProfilePhotoUrl);
                            }

                        }

                        adapter = new HistoryAdapter(   getActivity(), type, description, price,
                                                        dateCompleted, locationName, locationAddress,
                                                        latitude, longitude, buyerID, buyerName,
                                                        sellerID, sellerName, profilePhotoUrl);
                        layoutManager = new LinearLayoutManager(getActivity());
                        rvHistory.setLayoutManager(layoutManager);
                        rvHistory.setAdapter(adapter);
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
                Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
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
