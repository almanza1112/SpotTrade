package almanza1112.spottrade.navigationMenu.account.history;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 1/5/18.
 */

public class HistorySpot extends Fragment {

    private ProgressBar progressBar;
    private TextView tvLocationName, tvLocationAddress, tvDate, tvSellerText, tvSellerName,
            tvBuyerText, tvBuyerName, tvQuantity, tvPrice, tvTotal, tvDescription, tvFeedback;
    private ImageView ivStaticMap, ivSellerProfilePhoto, ivBuyerProfilePhoto;
    private CardView cvSellerProfilePhoto, cvBuyerProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_spot, container, false);

        String id = getArguments().getString("id");
        String locationName = getArguments().getString("locationName");

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(locationName);

        progressBar = view.findViewById(R.id.progressBar);
        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvLocationAddress = view.findViewById(R.id.tvLocationAddress);
        ivStaticMap = view.findViewById(R.id.ivStaticMap);
        tvDate = view.findViewById(R.id.tvDate);
        tvSellerText = view.findViewById(R.id.tvSellerText);
        tvSellerName = view.findViewById(R.id.tvSellerName);
        tvBuyerText = view.findViewById(R.id.tvBuyerText);
        tvBuyerName = view.findViewById(R.id.tvBuyerName);
        cvSellerProfilePhoto = view.findViewById(R.id.cvSellerProfilePhoto);
        ivSellerProfilePhoto = view.findViewById(R.id.ivSellerProfilePhoto);
        cvBuyerProfilePhoto = view.findViewById(R.id.cvBuyerProfilePhoto);
        ivBuyerProfilePhoto = view.findViewById(R.id.ivBuyerProfilePhoto);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvFeedback = view.findViewById(R.id.tvFeedback);

        getInformation(id);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem filterItem = menu.findItem(R.id.filterHistory);
        filterItem.setVisible(false);
    }

    private void getInformation(String id){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, httpConnection.htppConnectionURL() + "/location/history/transinfo/" + id + "?userID=" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        String location = response.getString("location");
                        JSONObject locationObj = new JSONObject(location);
                        tvLocationName.setText(locationObj.getString("name"));
                        tvLocationAddress.setText(locationObj.getString("address"));
                        String lat = locationObj.getString("latitude");
                        String lng = locationObj.getString("longitude");
                        String url = "http://maps.google.com/maps/api/staticmap?center=" +
                                lat +
                                "," +
                                lng +
                                "&zoom=15&" +
                                "markers=color:0xFFC107|" + lat + "," + lng +
                                "&size=1000x150&scale=2&" +
                                "key=" + getResources().getString(R.string.google_maps_key);
                        Picasso.with(getActivity()).load(url).fit().into(ivStaticMap);
                        String dateTime = epochToDateString(locationObj.getJSONArray("buyerInfo").getJSONObject(0).getLong("datePurchased")); // TODO: change "datePurchased" to "dateTransactionCompleted"
                        tvDate.setText(dateTime);
                        String type = locationObj.getString("type");
                        String sellerID = locationObj.getJSONObject("sellerInfo").getString("sellerID");
                        //Check if the logged in user is the seller for type Sell
                        if (type.equals("Sell") && sellerID.equals(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)))){
                            //If logged in user is the seller of type Sell then make tvSellerText
                            // "You" and then add the name of the other user as the buyer
                            cvBuyerProfilePhoto.setVisibility(View.VISIBLE);
                            Picasso.
                                    with(getActivity()).
                                    load(locationObj.getJSONArray("buyerInfo").getJSONObject(0).getString("buyerProfilePhotoUrl")).
                                    fit().
                                    into(ivBuyerProfilePhoto);
                            tvSellerText.setText(getResources().getString(R.string.Seller));
                            tvSellerName.setText(getResources().getString(R.string.You));
                            tvBuyerText.setText(getResources().getString(R.string.Buyer));
                            tvBuyerName.setText(
                                    locationObj.getJSONArray("buyerInfo").getJSONObject(0).getString("buyerFirstName") + " " +
                                    locationObj.getJSONArray("buyerInfo").getJSONObject(0).getString("buyerLastName") + " " +
                                            locationObj.getJSONArray("buyerInfo").getJSONObject(0).getString("buyerOverallRating") + " (" +
                                            locationObj.getJSONArray("buyerInfo").getJSONObject(0).getString("buyerTotalRatings") + ")");
                        } else if (type.equals("Sell") && !sellerID.equals(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)))){
                            cvSellerProfilePhoto.setVisibility(View.VISIBLE);
                            Picasso.
                                    with(getActivity()).
                                    load(locationObj.getJSONObject("sellerInfo").getString("sellerProfilePhotoUrl")).
                                    into(ivSellerProfilePhoto);
                            tvSellerText.setText(getResources().getString(R.string.Seller));
                            tvSellerName.setText(
                                    locationObj.getJSONObject("sellerInfo").getString("sellerFirstName") + " " +
                                    locationObj.getJSONObject("sellerInfo").getString("sellerLastName") + " " +
                                    locationObj.getJSONObject("sellerInfo").getString("sellerOverallRating") + "(" +
                                    locationObj.getJSONObject("sellerInfo").getString("sellerTotalRatings") + ")");
                            tvBuyerText.setText(getResources().getString(R.string.Buyer));
                            tvBuyerName.setText(getResources().getString(R.string.You));
                        }
                        int quantityBought = locationObj.getJSONArray("buyerInfo").getJSONObject(0).getInt("quantityBought");
                        double price = Double.valueOf(locationObj.getString("price"));
                        double total = quantityBought * price;
                        tvQuantity.setText(quantityBought + "");
                        tvPrice.setText("$" + price);
                        tvTotal.setText("$" + total);
                        tvDescription.setText(locationObj.getString("description"));
                    } else if (response.getString("status").equals("fail")){
                        Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
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
