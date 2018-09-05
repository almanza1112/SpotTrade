package almanza1112.spottrade.navigationMenu.yourSpots;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
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
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.List;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 8/10/17.
 */

public class YourSpots extends Fragment {

    RecyclerView rvYourSpots;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    final int[] pos = {2};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_spots, container, false);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        int statusBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.status_bar_height)));
        int actionBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.action_bar_height)));
        layoutParams.height = actionBarHeight + statusBarHeight;
        toolbar.setLayoutParams(layoutParams);
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Your_Spots);

        progressBar = view.findViewById(R.id.progressBar);
        rvYourSpots = view.findViewById(R.id.rvYourSpots);
        getYourSpots("all");
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
        inflater.inflate(R.menu.your_spots_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filterYourSpots){
            final CharSequence[] items = {getResources().getString(R.string.Sell), getResources().getString(R.string.Request), getResources().getString(R.string.All)};
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getResources().getString(R.string.Filter) + " " + getResources().getString(R.string.Your_Spots));
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
                    } else if (pos[0] == 1){
                        type = "Request";
                    } else {
                        type = "all";
                    }
                    getYourSpots(type);
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

    private void getYourSpots(String type){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/yourspots?id="+ SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)) + "&transaction=available&type=" + type, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        List<String> lid = new ArrayList<>();
                        List<String> locationName = new ArrayList<>();
                        List<String> locationAddress = new ArrayList<>();
                        List<String> type = new ArrayList<>();
                        List<String> category = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        List<Long> dateTimeStart = new ArrayList<>();
                        List<Integer> quantity = new ArrayList<>();
                        List<Boolean> offerAllowed = new ArrayList<>();
                        List<Integer> offerTotal = new ArrayList<>();
                        List<String> offerTotalString = new ArrayList<>();
                        List<String> description = new ArrayList<>();

                        JSONArray locationArray = new JSONArray(response.getString("location"));
                        for (int i = 0; i < locationArray.length(); i++){
                            JSONObject locationObj = locationArray.getJSONObject(i);
                            lid.add(locationObj.getString("_id"));
                            locationName.add(locationObj.getString("name"));
                            locationAddress.add(locationObj.getString("address"));
                            type.add(locationObj.getString("type"));
                            category.add(locationObj.getString("category"));
                            quantity.add(locationObj.getInt("quantity"));
                            price.add(locationObj.getString("price"));
                            dateTimeStart.add(locationObj.getLong("dateTimeStart"));
                            offerAllowed.add(locationObj.getBoolean("offerAllowed"));
                            description.add(locationObj.getString("description"));

                            if (locationObj.getBoolean("offerAllowed")){
                                int offersTotal = locationObj.getInt("offersTotal");
                                offerTotal.add(offersTotal);
                                String offersTotalString;
                                if (offersTotal == 1){
                                    offersTotalString = offersTotal + " " + getResources().getString(R.string.offer);
                                } else {
                                    offersTotalString = offersTotal + " " + getResources().getString(R.string.offers);
                                }
                                offerTotalString.add(offersTotalString);
                            } else{
                                offerTotal.add(0);
                                offerTotalString.add("0");
                            }
                        }
                        adapter = new YourSpotsAdapter(getActivity(), lid, locationName,
                                locationAddress, type, category, quantity, price, dateTimeStart, offerAllowed, offerTotal,
                                offerTotalString, description);
                        layoutManager = new LinearLayoutManager(getActivity());
                        rvYourSpots.setLayoutManager(layoutManager);
                        rvYourSpots.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
                    e.printStackTrace();
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
}
