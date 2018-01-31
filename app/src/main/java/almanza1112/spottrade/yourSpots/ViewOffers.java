package almanza1112.spottrade.yourSpots;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import almanza1112.spottrade.nonActivity.HttpConnection;

/**
 * Created by almanza1112 on 1/30/18.
 */

public class ViewOffers extends Fragment {


    private ProgressBar progressBar;
    private String lid;

    RecyclerView rvOffers;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_offers, container, false);
        lid = getArguments().getString("lid");

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getResources().getString(R.string._Offers));

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        rvOffers = (RecyclerView) view.findViewById(R.id.rvOffers);
        getOffers();
        return view;
    }

    private void getOffers(){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/yourspots/offers?lid="+lid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        List<String> firstName = new ArrayList<>();
                        List<String> profilePhotoUrl = new ArrayList<>();
                        List<String> priceOffered = new ArrayList<>();
                        List<Integer> quantityOffered = new ArrayList<>();
                        List<String> totalOfferPrice = new ArrayList<>();
                        List<Long> dateOffered = new ArrayList<>();

                        JSONArray offersArr = new JSONArray(response.getString("offers"));
                        for (int i = 0; i < offersArr.length(); i++){
                            firstName.add(  offersArr.getJSONObject(i).getString("offererFirstName") + " " +
                                            offersArr.getJSONObject(i).getString("offererLastName") + " " +
                                            offersArr.getJSONObject(i).getString("offererOverallRating") + "(" +
                                            offersArr.getJSONObject(i).getString("offererTotalRatings") + ")"
                            );
                            if (offersArr.getJSONObject(i).has("offererProfilePhotoUrl")){
                                profilePhotoUrl.add(offersArr.getJSONObject(i).getString("offererProfilePhotoUrl"));
                            }
                            else {
                                profilePhotoUrl.add("empty");
                            }
                            String price = offersArr.getJSONObject(i).getString("offerPrice");
                            int quantity = offersArr.getJSONObject(i).getInt("offerQuantity");
                            double finalPrice = Double.valueOf(price) * quantity;
                            priceOffered.add("$"+price);
                            quantityOffered.add(quantity);
                            totalOfferPrice.add("$"+finalPrice);
                            dateOffered.add(offersArr.getJSONObject(i).getLong("offerDate"));
                        }

                        adapter = new OffersAdapter(    getActivity(), firstName, profilePhotoUrl,
                                                        priceOffered, quantityOffered, totalOfferPrice,
                                                        dateOffered);
                        layoutManager = new LinearLayoutManager(getActivity());
                        rvOffers.setLayoutManager(layoutManager);
                        rvOffers.setAdapter(adapter);
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
                error.printStackTrace();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }
}
