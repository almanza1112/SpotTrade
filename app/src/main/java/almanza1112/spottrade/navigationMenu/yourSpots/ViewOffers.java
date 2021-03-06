package almanza1112.spottrade.navigationMenu.yourSpots;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
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
 * Created by almanza1112 on 1/30/18.
 */

public class ViewOffers extends Fragment {


    private ProgressBar progressBar;
    private String lid;
    public Snackbar snackbar;

    RecyclerView rvOffers;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_offers, container, false);

        lid = getArguments().getString("lid");

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
        toolbar.setTitle(getResources().getString(R.string._Offers));

        progressBar = view.findViewById(R.id.progressBar);
        rvOffers = view.findViewById(R.id.rvOffers);
        getOffers();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            offerAcceptedListener = (OfferAcceptedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItemClickedListener");
        }
    }

    @Override
    public void onDestroy() {
        if (snackbar != null){
            snackbar.dismiss();
        }
        super.onDestroy();
    }

    OfferAcceptedListener offerAcceptedListener = onOfferAcceptedMethodCallback;

    public interface OfferAcceptedListener{
        void onOfferAccepted(String lid, String id, String latitude, String longitude, String profilePhotoUrl);
    }

    public static OfferAcceptedListener onOfferAcceptedMethodCallback = new OfferAcceptedListener() {
        @Override
        public void onOfferAccepted(String lid, String id, String latitude, String longitude, String profilePhotoUrl) {

        }
    };

    public void offerAccepted(String lid, String id, String latitude, String longitude, String profilePhotoUrl){
        offerAcceptedListener.onOfferAccepted(lid, id, latitude, longitude, profilePhotoUrl);
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
    }

    private void getOffers(){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/yourspots/offers?lid="+lid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        String latitude = response.getString("latitude");
                        String longitude = response.getString("longitude");
                        List<String> _id = new ArrayList<>();
                        List<String> userID = new ArrayList<>();
                        List<String> firstName = new ArrayList<>();
                        List<String> profilePhotoUrl = new ArrayList<>();
                        List<String> priceOffered = new ArrayList<>();
                        List<Integer> quantityOffered = new ArrayList<>();
                        List<String> totalOfferPrice = new ArrayList<>();
                        List<Long> dateOffered = new ArrayList<>();

                        JSONArray offersArr = new JSONArray(response.getString("offers"));
                        for (int i = 0; i < offersArr.length(); i++){
                            _id.add(offersArr.getJSONObject(i).getString("_id"));
                            userID.add(offersArr.getJSONObject(i).getString("offererID"));
                            firstName.add(  offersArr.getJSONObject(i).getString("offererFirstName") + " " +
                                            offersArr.getJSONObject(i).getString("offererLastName") + " " +
                                            offersArr.getJSONObject(i).getString("offererOverallRating") + "(" +
                                            offersArr.getJSONObject(i).getString("offererTotalRatings") + ")"
                            );
                            if (offersArr.getJSONObject(i).has("offererProfilePhotoUrl")){
                                profilePhotoUrl.add(offersArr.getJSONObject(i).getString("offererProfilePhotoUrl"));
                            } else {
                                profilePhotoUrl.add("empty");
                            }
                            String price = offersArr.getJSONObject(i).getString("offerPrice");
                            int quantity = offersArr.getJSONObject(i).getInt("offerQuantity");
                            double finalPrice = Double.valueOf(price) * quantity;
                            priceOffered.add(price);
                            quantityOffered.add(quantity);
                            totalOfferPrice.add("$"+finalPrice);
                            dateOffered.add(offersArr.getJSONObject(i).getLong("offerDate"));
                        }

                        adapter = new ViewOffersAdapter(    ViewOffers.this, getActivity(), lid, latitude, longitude, _id, userID, firstName, profilePhotoUrl,
                                                        priceOffered, quantityOffered, totalOfferPrice,
                                                        dateOffered);
                        layoutManager = new LinearLayoutManager(getActivity());
                        rvOffers.setLayoutManager(layoutManager);
                        rvOffers.setAdapter(adapter);

                    } else {
                        setToastServerError();
                    }
                } catch (JSONException e){
                    setToastServerError();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                setToastServerError();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    public void setSnackbar(String snackbarText) {
        snackbar = Snackbar.make(getActivity().findViewById(R.id.view_offers), snackbarText, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void setProgressBar(int view){
        progressBar.setVisibility(view);
    }

    public void setToastServerError(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }
    }
}
