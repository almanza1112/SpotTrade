package almanza1112.spottrade.search;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import almanza1112.spottrade.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 6/29/17.
 */

class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.RecyclerViewHolder>{
    private Activity activity;
    private List<String> locationName;
    private List<String> locationAddress;
    private List<Double> locationLat;
    private List<Double> locationLng;

    SearchAdapter(Activity activity, List<String> locationName, List<String> locationAddress, List<Double> locationLat, List<Double> locationLng){
        this.activity = activity;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
    }

    @Override
    public SearchAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView tvLocationName, tvLocationAddress;
        RelativeLayout rlLocation;
        RecyclerViewHolder(View view){
            super(view);
            tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
            rlLocation = (RelativeLayout) view.findViewById(R.id.rlLocation);
            rlLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = activity.getIntent();
                    intent.putExtra("latitude", locationLat.get(getAdapterPosition()));
                    intent.putExtra("longitude", locationLng.get(getAdapterPosition()));
                    intent.putExtra("locationName", locationName.get(getAdapterPosition()));
                    intent.putExtra("locationAddress", locationAddress.get(getAdapterPosition()));
                    activity.setResult(RESULT_OK, intent);
                    activity.finish();
                }
            });
        }
    }
}
