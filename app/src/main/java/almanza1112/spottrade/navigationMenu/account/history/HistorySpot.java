package almanza1112.spottrade.navigationMenu.account.history;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 1/5/18.
 */

public class HistorySpot extends Fragment {

    private ProgressBar progressBar;
    private TextView tvLocationName, tvLocationAddress, tvDate, tvPosterText, tvPosterName,
            tvAcceptorText, tvAcceptorName, tvQuantity, tvPrice, tvTotal, tvDescription, tvFeedback;
    private ImageView ivStaticMap, ivPosterProfilePhoto, ivAcceptorProfilePhoto;
    private CardView cvPosterProfilePhoto, cvAcceptorProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_spot, container, false);

        String id = getArguments().getString("id");
        String locationName = getArguments().getString("locationName");

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
        toolbar.setTitle(locationName);

        progressBar = view.findViewById(R.id.progressBar);
        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvLocationAddress = view.findViewById(R.id.tvLocationAddress);
        ivStaticMap = view.findViewById(R.id.ivStaticMap);
        tvDate = view.findViewById(R.id.tvDate);
        tvPosterText = view.findViewById(R.id.tvPosterText);
        tvPosterName = view.findViewById(R.id.tvPosterName);
        tvAcceptorText = view.findViewById(R.id.tvAcceptorText);
        tvAcceptorName = view.findViewById(R.id.tvAcceptorName);
        cvPosterProfilePhoto = view.findViewById(R.id.cvPosterProfilePhoto);
        ivPosterProfilePhoto = view.findViewById(R.id.ivPosterProfilePhoto);
        cvAcceptorProfilePhoto = view.findViewById(R.id.cvAcceptorProfilePhoto);
        ivAcceptorProfilePhoto = view.findViewById(R.id.ivAcceptorProfilePhoto);
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

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "/location/history/transinfo/" + id + "?userID=" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)), null, new Response.Listener<JSONObject>() {
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
                        Picasso.get().load(url).fit().into(ivStaticMap);
                        String dateTime = epochToDateString(locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getLong("datePurchased")); // TODO: change "datePurchased" to "dateTransactionCompleted"
                        tvDate.setText(dateTime);
                        String type = locationObj.getString("type");
                        String posterId = locationObj.getJSONObject("posterInfo").getString("posterId");
                        //Check if the logged in user is the seller for type Sell
                        if (type.equals("Sell") && posterId.equals(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)))){
                            //If logged in user is the seller of type Sell then make tvPosterText
                            // "You" and then add the name of the other user as the buyer
                            cvAcceptorProfilePhoto.setVisibility(View.VISIBLE);
                            Picasso.
                                    get().
                                    load(locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getString("acceptorProfilePhotoUrl")).
                                    fit().
                                    into(ivAcceptorProfilePhoto);
                            tvPosterText.setText(getResources().getString(R.string.Seller));
                            tvPosterName.setText(getResources().getString(R.string.You));
                            tvAcceptorText.setText(getResources().getString(R.string.Buyer));
                            tvAcceptorName.setText(
                                    locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getString("acceptorFirstName") + " " +
                                    locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getString("acceptorLastName") + " " +
                                            locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getString("acceptorOverallRating") + " (" +
                                            locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getString("acceptorTotalRatings") + ")");
                        } else if (type.equals("Sell") && !posterId.equals(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)))){
                            cvPosterProfilePhoto.setVisibility(View.VISIBLE);
                            Picasso.
                                    get().
                                    load(locationObj.getJSONObject("posterInfo").getString("posterProfilePhotoUrl")).
                                    into(ivPosterProfilePhoto);
                            tvPosterText.setText(getResources().getString(R.string.Seller));
                            tvPosterName.setText(
                                    locationObj.getJSONObject("posterInfo").getString("posterFirstName") + " " +
                                    locationObj.getJSONObject("posterInfo").getString("posterLastName") + " " +
                                    locationObj.getJSONObject("posterInfo").getString("posterOverallRating") + "(" +
                                    locationObj.getJSONObject("posterInfo").getString("posterTotalRatings") + ")");
                            tvAcceptorText.setText(getResources().getString(R.string.Buyer));
                            tvAcceptorName.setText(getResources().getString(R.string.You));
                        }
                        int quantityBought = locationObj.getJSONArray("acceptorInfo").getJSONObject(0).getInt("quantityBought");
                        double price = Double.valueOf(locationObj.getString("price"));
                        double total = quantityBought * price;
                        tvQuantity.setText(quantityBought + "");
                        tvPrice.setText("$" + price);
                        tvTotal.setText("$" + total);
                        tvDescription.setText(locationObj.getString("description"));
                    } else if (response.getString("status").equals("fail")){
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

    private String epochToDateString(long epochSeconds) {
        Date updatedate = new Date(epochSeconds * 1000);
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy 'at' H:mm a", Locale.getDefault());
        return format.format(updatedate);
    }

    private void setToastServerError(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }
    }
}
