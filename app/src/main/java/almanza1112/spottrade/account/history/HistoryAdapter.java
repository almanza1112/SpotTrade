package almanza1112.spottrade.account.history;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 7/26/17.
 */

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.RecyclerViewHolder>{

    private Activity activity;
    private List<String> type;
    private List<String> description;
    private List<String> price;
    private List<String> dateCompleted;
    private List<String> locationName;
    private List<String> locationAddress;
    private List<String> locationStaticMapUrl;
    private List<String> otherName;
    private List<String> otherPhotoUrl;

    HistoryAdapter(Activity activity, List<String> type, List<String> description,
                   List<String> price, List<String> dateCompleted, List<String> locationName,
                   List<String> locationAddress, List<String> locationStaticMapUrl,
                   List<String> otherName, List<String> otherPhotoUrl){
        this.activity = activity;
        this.type = type;
        this.description = description;
        this.price = price;
        this.dateCompleted = dateCompleted;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationStaticMapUrl = locationStaticMapUrl;
        this.otherName = otherName;
        this.otherPhotoUrl = otherPhotoUrl;
    }

    @Override
    public HistoryAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Picasso.with(activity).load(otherPhotoUrl.get(position)).fit().centerCrop().into(holder.ivProfilePhoto);
        Picasso.with(activity).load(locationStaticMapUrl.get(position)).fit().into(holder.ivStaticMap);
        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));
        holder.tvType.setText(type.get(position));
        holder.tvDescription.setText(description.get(position));
        holder.tvOtherUser.setText(otherName.get(position));
        holder.tvPrice.setText(price.get(position));
        holder.tvDateCompleted.setText(dateCompleted.get(position));
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivStaticMap, ivProfilePhoto;
        TextView    tvPrice, tvLocationName, tvLocationAddress, tvType,
                    tvDescription, tvOtherUser, tvDateCompleted;
        RecyclerViewHolder(View view){
            super(view);
            ivStaticMap = (ImageView) view.findViewById(R.id.ivStaticMap);
            ivProfilePhoto = (ImageView) view.findViewById(R.id.ivProfilePhoto);
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
