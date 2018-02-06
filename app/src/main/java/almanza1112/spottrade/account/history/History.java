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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private TextView tvNoHistory;

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
        tvNoHistory = (TextView) view.findViewById(R.id.tvNoHistory);
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/history?sellerID="+ SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)) + "&type=" + type, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        tvNoHistory.setVisibility(View.GONE);
                        List<String> id = new ArrayList<>();
                        List<String> type = new ArrayList<>();
                        List<String> dateCompleted = new ArrayList<>();
                        List<String> locationName = new ArrayList<>();
                        List<String> locationAddress = new ArrayList<>();
                        List<String> locationStaticMapUrl = new ArrayList<>();

                        String locations = response.getString("location");
                        JSONArray jsonArray = new JSONArray(locations);

                        String lat, lng;
                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject locationObj = jsonArray.getJSONObject(i);
                            id.add(locationObj.getJSONArray("buyerInfo").getJSONObject(0).getString("_id"));
                            type.add(locationObj.getString("type"));
                            String convertedDate = epochToDateString(locationObj.getJSONArray("buyerInfo").getJSONObject(0).getLong("dateTransactionCompleted"));
                            dateCompleted.add(convertedDate);
                            locationName.add(locationObj.getString("name"));
                            locationAddress.add(locationObj.getString("address"));
                            lat = locationObj.getString("latitude");
                            lng = locationObj.getString("longitude");
                            String url = "http://maps.google.com/maps/api/staticmap?center=" +
                                    lat +
                                    "," +
                                    lng +
                                    "&zoom=15&" +
                                    "markers=color:0xFFC107|" + lat + "," + lng +
                                    "&size=1000x150&scale=2&" +
                                    "key=" + getResources().getString(R.string.google_maps_key);
                            locationStaticMapUrl.add(url);
                        }

                        adapter = new HistoryAdapter(   getActivity(), id, type, dateCompleted,
                                                        locationName, locationAddress,
                                                        locationStaticMapUrl);
                        layoutManager = new LinearLayoutManager(getActivity());
                        rvHistory.setLayoutManager(layoutManager);
                        rvHistory.setAdapter(adapter);
                    }
                    else if (response.getString("status").equals("fail")){
                        rvHistory.setAdapter(null);
                        tvNoHistory.setVisibility(View.VISIBLE);
                    }
                }
                catch (JSONException e){
                    Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
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
