package almanza1112.spottrade;


import android.os.Bundle;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 8/10/17.
 */

public class YourSpots extends Fragment {

    RecyclerView rvYourSpots;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_spots, container, false);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Your_Spots);

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

        rvYourSpots = (RecyclerView) view.findViewById(R.id.rvYourSpots);
        getHistory();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.search);
        item.setVisible(false);
    }

    private void getHistory(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/all?sellerID="+ SharedPref.getID(getActivity()) + "&transaction=available&type=all", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        List<String> lid = new ArrayList<>();
                        List<String> locationName = new ArrayList<>();
                        List<String> locationAddress = new ArrayList<>();
                        List<String> type = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        List<Boolean> bidAllowed = new ArrayList<>();
                        List<String> bidAmount = new ArrayList<>();
                        List<String> description = new ArrayList<>();

                        JSONArray locationArray = new JSONArray(response.getString("location"));
                        Log.e("array.length", locationArray.length() + "");
                        for (int i = 0; i < locationArray.length(); i++){
                            JSONObject locationObj = locationArray.getJSONObject(i);
                            lid.add(locationObj.getString("_id"));
                            locationName.add(locationObj.getString("name"));
                            locationAddress.add(locationObj.getString("address"));
                            type.add(locationObj.getString("type"));
                            price.add(locationObj.getString("price"));
                            bidAllowed.add(locationObj.getBoolean("bidAllowed"));
                            if (locationObj.getBoolean("bidAllowed")){
                                bidAmount.add(locationObj.getString("biddenAmount") + " " + getResources().getString(R.string.bids));
                            }
                            else{
                                bidAmount.add("0");
                            }
                            description.add(locationObj.getString("description"));
                            Log.e("desc", locationObj.getString("description"));
                        }
                        adapter = new YourSpotsAdapter(getActivity(), lid, locationName, locationAddress, type, price, bidAllowed, bidAmount, description);
                        layoutManager = new LinearLayoutManager(getActivity());
                        rvYourSpots.setLayoutManager(layoutManager);
                        rvYourSpots.setAdapter(adapter);
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
}
