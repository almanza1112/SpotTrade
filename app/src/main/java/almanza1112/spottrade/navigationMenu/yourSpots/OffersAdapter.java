package almanza1112.spottrade.navigationMenu.yourSpots;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.List;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 1/30/18.
 */

class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.RecyclerViewHolder> {

    private ViewOffers viewOffers;
    private Activity activity;
    private String lid;
    private String latitude;
    private String longitude;
    private List<String> _id;
    private List<String> userID;
    private List<String> firstName;
    private List<String> profilePhotoUrl;
    private List<String> priceOffered;
    private List<Integer> quantityOffered;
    private List<String> totalOfferPrice;
    private List<Long> dateOffered;

    OffersAdapter(  ViewOffers viewOffers, Activity activity, String lid, String latitude,
                    String longitude, List<String> _id, List<String> userID, List<String> firstName,
                    List<String> profilePhotoUrl, List<String> priceOffered,
                    List<Integer> quantityOffered, List<String> totalOfferPrice,
                    List<Long> dateOffered){
        this.viewOffers = viewOffers;
        this.activity = activity;
        this.lid = lid;
        this.latitude = latitude;
        this.longitude = longitude;
        this._id = _id;
        this.userID = userID;
        this.firstName = firstName;
        this.profilePhotoUrl = profilePhotoUrl;
        this.priceOffered = priceOffered;
        this.quantityOffered = quantityOffered;
        this.totalOfferPrice = totalOfferPrice;
        this.dateOffered = dateOffered;
    }

    @Override
    public OffersAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_spots_spot_offers_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.tvOffererName.setText(firstName.get(position));
        Picasso.with(activity).load(profilePhotoUrl.get(position)).fit().centerCrop().into(holder.ivOffererProfilePhoto);
        holder.tvPriceOffered.setText("$"+priceOffered.get(position));
        holder.tvQuantityOffered.setText(quantityOffered.get(position) + "");
        holder.tvTotalOffered.setText(totalOfferPrice.get(position));
    }

    @Override
    public int getItemCount() {
        return firstName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView  tvOffererName, tvPriceOffered, tvQuantityOffered, tvTotalOffered;
        ImageView ivOffererProfilePhoto;
        Button bDeclineOffer, bAcceptOffer;
        RecyclerViewHolder(View view){
            super(view);
            tvOffererName = view.findViewById(R.id.tvOffererName);
            ivOffererProfilePhoto = view.findViewById(R.id.ivOffererProfilePhoto);
            tvPriceOffered = view.findViewById(R.id.tvPriceOffered);
            tvQuantityOffered = view.findViewById(R.id.tvQuantityOffered);
            tvTotalOffered = view.findViewById(R.id.tvTotalOffered);
            bDeclineOffer = view.findViewById(R.id.bDeclineOffer);
            bAcceptOffer = view.findViewById(R.id.bAcceptOffer);
            bDeclineOffer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    declineOffer(_id.get(getAdapterPosition()), userID.get(getAdapterPosition()), getAdapterPosition());
                }
            });
            bAcceptOffer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptOffer(    _id.get(getAdapterPosition()),
                                    priceOffered.get(getAdapterPosition()),
                                    quantityOffered.get(getAdapterPosition()),
                                    userID.get(getAdapterPosition()),
                                    getAdapterPosition());
                }
            });
        }
    }

    private void declineOffer(String id, String uid, final int position){
        viewOffers.setProgressBar(View.VISIBLE);
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("lid", lid);
            jObject.put("_id", id); // id of the item in offers array
            jObject.put("userID", uid);
            jObject.put("sellerID", SharedPref.getSharedPreferences(activity, activity.getResources().getString(R.string.logged_in_user_id)));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(activity);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/transaction/offers/decline", jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        viewOffers.setSnackbar(activity.getResources().getString(R.string.Offer_declined));
                        _id.remove(position);
                        userID.remove(position);
                        firstName.remove(position);
                        profilePhotoUrl.remove(position);
                        priceOffered.remove(position);
                        quantityOffered.remove(position);
                        totalOfferPrice.remove(position);
                        dateOffered.remove(position);
                        notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(activity, activity.getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    Toast.makeText(activity, activity.getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                }
                viewOffers.setProgressBar(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                viewOffers.setProgressBar(View.GONE);
                Toast.makeText(activity, activity.getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void acceptOffer(final String id, String price, int quantity, String uid, final int position){
        viewOffers.setProgressBar(View.VISIBLE);
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("lid", lid);
            jObject.put("_id", id); // id of the item in offers array
            jObject.put("quantityBought", quantity);
            jObject.put("offerPrice", price);
            jObject.put("buyerID", uid);
            jObject.put("sellerID", SharedPref.getSharedPreferences(activity, activity.getResources().getString(R.string.logged_in_user_id)));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(activity);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/transaction/offers/accept", jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        if (response.getBoolean("isComplete") || _id.size() == 1){
                            viewOffers.offerAccepted(lid, id, latitude, longitude, profilePhotoUrl.get(position));
                        }
                        else{
                            viewOffers.setSnackbar(activity.getResources().getString(R.string.Offer_accepted));
                            _id.remove(position);
                            userID.remove(position);
                            firstName.remove(position);
                            profilePhotoUrl.remove(position);
                            priceOffered.remove(position);
                            quantityOffered.remove(position);
                            totalOfferPrice.remove(position);
                            dateOffered.remove(position);
                            notifyDataSetChanged();
                        }
                    }
                    else {
                        Toast.makeText(activity, activity.getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    Toast.makeText(activity, activity.getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                }
                viewOffers.setProgressBar(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                viewOffers.setProgressBar(View.GONE);
                Toast.makeText(activity, activity.getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }
}
