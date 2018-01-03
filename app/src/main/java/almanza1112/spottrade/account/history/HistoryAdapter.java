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
    private List<String> dateCompleted;
    private List<String> locationName;
    private List<String> locationAddress;
    private List<String> locationStaticMapUrl;

    HistoryAdapter(Activity activity, List<String> type,
                   List<String> dateCompleted, List<String> locationName,
                   List<String> locationAddress, List<String> locationStaticMapUrl){
        this.activity = activity;
        this.type = type;
        this.dateCompleted = dateCompleted;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationStaticMapUrl = locationStaticMapUrl;
    }

    @Override
    public HistoryAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        if (type.get(position).equals("Sell")){
            holder.ivTypeIcon.setImageResource(R.mipmap.ic_currency_usd_grey600_24dp);
        }
        else {
            holder.ivTypeIcon.setImageResource(R.mipmap.ic_human_handsup_grey600_24dp);
        }
        Picasso.with(activity).load(locationStaticMapUrl.get(position)).fit().into(holder.ivStaticMap);
        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));
        holder.tvDateCompleted.setText(dateCompleted.get(position));
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView   ivTypeIcon, ivStaticMap;
        TextView    tvLocationName, tvLocationAddress,
                    tvDateCompleted;
        RecyclerViewHolder(View view){
            super(view);
            ivTypeIcon = (ImageView) view.findViewById(R.id.ivTypeIcon);
            ivStaticMap = (ImageView) view.findViewById(R.id.ivStaticMap);
            tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
            tvDateCompleted = (TextView) view.findViewById(R.id.tvDateCompleted);
        }
    }
}
