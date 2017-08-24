package almanza1112.spottrade.account.history;

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
        String otherUser;
        if (buyerID.get(position).equals(SharedPref.getID(activity))){
            otherUser = sellerName.get(position);
        }
        else {
            otherUser = buyerName.get(position);
        }
        String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude.get(position) + "," + longitude.get(position) + "&zoom=15&size=1000x150&scale=2&sensor=false";
        Picasso.with(activity).load(url).fit().into(holder.ivStaticMap);
        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));
        holder.tvType.setText(type.get(position));
        holder.tvDescription.setText(description.get(position));
        holder.tvOtherUser.setText(otherUser);
        holder.tvPrice.setText(price.get(position));
        holder.tvDateCompleted.setText(dateCompleted.get(position));
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivStaticMap;
        TextView    tvPrice, tvLocationName, tvLocationAddress, tvType,
                    tvDescription, tvOtherUser, tvDateCompleted;
        RecyclerViewHolder(View view){
            super(view);
            ivStaticMap = (ImageView) view.findViewById(R.id.ivStaticMap);
            tvType = (TextView) view.findViewById(R.id.tvType);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            tvOtherUser = (TextView) view.findViewById(R.id.tvOtherUser);
            tvDateCompleted = (TextView) view.findViewById(R.id.tvDateCompleted);
        }
    }
}
