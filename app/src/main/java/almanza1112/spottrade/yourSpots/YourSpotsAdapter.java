package almanza1112.spottrade.yourSpots;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 8/11/17.
 */

class YourSpotsAdapter extends RecyclerView.Adapter<YourSpotsAdapter.RecyclerViewHolder>{

    private Activity activity;
    private List<String> lid;
    private List<String> locationName;
    private List<String> locationAddress;
    private List<String> type;
    private List<String> price;
    private List<Boolean> offerAllowed;
    private List<String> offerAmount;
    private List<String> description;
    YourSpotsAdapter(
            Activity activity, List<String> lid, List<String> locationName,
            List<String> locationAddress, List<String> type, List<String> price,
            List<Boolean> offerAllowed, List<String> offerAmount, List<String> description){
        this.activity = activity;
        this.lid = lid;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.type = type;
        this.price = price;
        this.offerAllowed = offerAllowed;
        this.offerAmount = offerAmount;
        this.description = description;
    }

    @Override
    public YourSpotsAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_spots_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));
        holder.tvType.setText(type.get(position));
        holder.tvPrice.setText(price.get(position));
        if (offerAllowed.get(position)){
            holder.tvOfferAmount.setText(offerAmount.get(position));
        }
        else{
            holder.llOffers.setVisibility(View.GONE);
        }
        holder.tvDescription.setText(description.get(position));
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView tvLocationName, tvLocationAddress, tvType, tvOfferAmount, tvPrice, tvDescription;
        LinearLayout llOffers;
        ImageView ivEdit;
        RecyclerViewHolder(View view){
            super(view);
            tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
            tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
            tvType = (TextView) view.findViewById(R.id.tvType);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            llOffers = (LinearLayout) view.findViewById(R.id.llOffers);
            tvOfferAmount = (TextView) view.findViewById(R.id.tvOfferAmount);
            tvDescription = (TextView) view.findViewById(R.id.tvDescription);
            ivEdit = (ImageView) view.findViewById(R.id.ivEdit);
            ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("lid", lid.get(getAdapterPosition()));
                    bundle.putString("locationName", locationName.get(getAdapterPosition()));
                    bundle.putString("locationAddress", locationAddress.get(getAdapterPosition()));
                    bundle.putString("type", type.get(getAdapterPosition()));
                    bundle.putString("price", price.get(getAdapterPosition()));
                    bundle.putBoolean("offerAllowed", offerAllowed.get(getAdapterPosition()));
                    bundle.putString("description", description.get(getAdapterPosition()));
                    EditSpot editSpot = new EditSpot();
                    editSpot.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.drawer_layout, editSpot);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }
    }
}

