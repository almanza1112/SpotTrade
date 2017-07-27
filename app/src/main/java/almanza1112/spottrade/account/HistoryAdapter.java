package almanza1112.spottrade.account;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 7/26/17.
 */

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.RecyclerViewHolder>{

    private Activity activity;
    private List<String> type = new ArrayList<>();
    private List<String> description = new ArrayList<>();
    private List<String> price = new ArrayList<>();
    private List<String> dateCompleted = new ArrayList<>();
    private List<String> locationName = new ArrayList<>();
    private List<String> locationAddress = new ArrayList<>();
    private List<String> latitude = new ArrayList<>();
    private List<String> longitude = new ArrayList<>();
    private List<String> buyerID = new ArrayList<>();
    private List<String> buyerName = new ArrayList<>();
    private List<String> sellerID = new ArrayList<>();
    private List<String> sellerName = new ArrayList<>();

    HistoryAdapter(Activity activity, List<String> type, List<String> description,
                   List<String> price, List<String> dateCompleted, List<String> locationName,
                   List<String> locationAddress, List<String> latitude,
                   List<String> longitude, List<String> buyerID, List<String> buyerName,
                   List<String> sellerID, List<String> sellerName){
        this.activity = activity;
        this.type = type;
        this.description = description;
        this.price = price;
        this.dateCompleted = dateCompleted;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.buyerID = buyerID;
        this.buyerName = buyerName;
        this.sellerID = sellerID;
        this.sellerName = sellerName;
    }

    @Override
    public HistoryAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        String title, otherUser;
        if (type.get(position).equals("selling")){
            if (buyerID.get(position).equals(SharedPref.getID(activity))) {
                //YOU are the one who bought a spot - "Bought Spot"
                title = activity.getResources().getString(R.string.Bought_Spot) + " - " + "$" + price.get(position);
                //OTHER USER is the one who sold you their spot - "sold their spot"
                otherUser = sellerName.get(position) + " " + activity.getResources().getString(R.string.sold_their_spot);
            }
            else {//ELSE would be you are the sellerID
                //YOU are the one who sold the spot - "Sold Spot"
                title = activity.getResources().getString(R.string.Sold_Spot) + " - " + "$" + price.get(position);
                //OTHER USER is the one who bought your spot - "bought your spot"
                otherUser = buyerName.get(position) + " " + activity.getResources().getString(R.string.bought_your_spot);
            }
        }
        else {//ELSE would be requesting
            if (buyerID.get(position).equals(SharedPref.getID(activity))) {
                //YOU are the one who accepted someone's request - "Spotted"
                title = activity.getResources().getString(R.string.Spotted) + " - " + "$" + price.get(position);
                //OTHER USER is the one who request - "got spotted"
                otherUser = sellerName.get(position) + " " + activity.getResources().getString(R.string.got_spotted);
            }
            else {//ELSE would be you are the sellerID
                //YOU are the one who requested the spot - "Requested Spotter"
                title = activity.getResources().getString(R.string.Requested_Spotter) + " - " + "$" + price.get(position);
                //OTHER USer is the one who accepted the request - "spotted you"
                otherUser = buyerName.get(position) + " " + activity.getResources().getString(R.string.spotted_you);
            }
        }
        String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude.get(position) + "," + longitude.get(position) + "&zoom=15&size=1000x150&scale=2&sensor=false";
        Picasso.with(activity).load(url).fit().into(holder.ivStaticMap);
        holder.tvTypeAndPrice.setText(title);
        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));
        holder.tvDescription.setText(description.get(position));
        holder.tvOtherUser.setText(otherUser);
        holder.tvDateCompleted.setText(dateCompleted.get(position));
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivStaticMap;
        TextView    tvTypeAndPrice, tvLocationName, tvLocationAddress,
                    tvDescription, tvOtherUser, tvDateCompleted;
        RecyclerViewHolder(View view){
            super(view);
            ivStaticMap = (ImageView) view.findViewById(R.id.ivStaticMap);
            tvTypeAndPrice = (TextView) view.findViewById(R.id.tvTypeAndPrice);
            tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            tvOtherUser = (TextView) view.findViewById(R.id.tvOtherUser);
            tvDateCompleted = (TextView) view.findViewById(R.id.tvDateCompleted);
        }
    }
}
