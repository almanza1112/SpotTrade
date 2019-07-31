package almanza1112.spottrade.navigationMenu.yourSpots;

import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 8/11/17.
 */

class YourSpotsAdapter extends RecyclerView.Adapter<YourSpotsAdapter.RecyclerViewHolder>{

    private Activity activity;
    private List<String> lid, locationName, locationAddress, type, category, price, offerTotalString, description;
    private List<Long> dateTimeStart;
    private List<Integer> quantity, offerTotal;
    private List<Boolean> offerAllowed;
    YourSpotsAdapter(
            Activity activity, List<String> lid, List<String> locationName,
            List<String> locationAddress, List<String> type, List<String> category,
            List<Integer> quantity, List<String> price, List<Long> dateTimeStart,
            List<Boolean> offerAllowed, List<Integer> offerTotal, List<String> offerTotalString,
            List<String> description){
        this.activity = activity;
        this.lid = lid;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.type = type;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.dateTimeStart = dateTimeStart;
        this.offerAllowed = offerAllowed;
        this.offerTotal = offerTotal;
        this.offerTotalString = offerTotalString;
        this.description = description;
    }

    @NonNull
    @Override
    public YourSpotsAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_spots_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        if (type.get(position).equals("Sell")){
            holder.ivTypeIcon.setImageResource(R.mipmap.ic_currency_usd_grey600_24dp);
        } else {
            holder.ivTypeIcon.setImageResource(R.mipmap.ic_human_handsup_grey600_24dp);
        }

        holder.tvLocationName.setText(locationName.get(position));
        holder.tvLocationAddress.setText(locationAddress.get(position));

        if (quantity.get(position) > 1){
            holder.tvQuantity.setText(quantity.get(position) + " " + activity.getResources().getString(R.string.available));
            holder.tvQuantity.setVisibility(View.VISIBLE);
        }

        if (offerAllowed.get(position)){
            holder.tvOfferAmount.setText(offerTotalString.get(position));
        } else{
            holder.tvOfferAmount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return locationName.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView tvLocationName, tvLocationAddress, tvOfferAmount, tvQuantity;
        ImageView ivTypeIcon;
        CardView cardView;
        RecyclerViewHolder(View view){
            super(view);
            ivTypeIcon = view.findViewById(R.id.ivTypeIcon);
            tvLocationName = view.findViewById(R.id.tvLocationName);
            tvLocationAddress = view.findViewById(R.id.tvLocationAddress);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            tvOfferAmount = view.findViewById(R.id.tvOfferAmount);
            cardView = view.findViewById(R.id.cardView);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("lid", lid.get(getAdapterPosition()));
                    bundle.putString("locationName", locationName.get(getAdapterPosition()));
                    bundle.putString("locationAddress", locationAddress.get(getAdapterPosition()));
                    bundle.putString("type", type.get(getAdapterPosition()));
                    bundle.putString("category", category.get(getAdapterPosition()));
                    bundle.putString("price", price.get(getAdapterPosition()));
                    bundle.putInt("quantity", quantity.get(getAdapterPosition()));
                    bundle.putLong("dateTimeStart", dateTimeStart.get(getAdapterPosition()));
                    bundle.putBoolean("offerAllowed", offerAllowed.get(getAdapterPosition()));
                    bundle.putString("description", description.get(getAdapterPosition()));
                    bundle.putInt("offerTotal", offerTotal.get(getAdapterPosition()));
                    EditSpot editSpot = new EditSpot();
                    editSpot.setArguments(bundle);/*
                    FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
                    fragmentTransaction.replace(R.id.your_spots, editSpot);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit(); */
                }
            });

        }
    }
}

