package almanza1112.spottrade.yourSpots;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by almanza1112 on 1/30/18.
 */

class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.RecyclerViewHolder> {

    private Activity activity;
    private List<String> firstName;
    private List<String> profilePhotoUrl;
    private List<String> priceOffered;
    private List<Integer> quantityOffered;
    private List<String> totalOfferPrice;
    private List<Long> dateOffered;

    OffersAdapter(  Activity activity, List<String> firstName, List<String> profilePhotoUrl,
                    List<String> priceOffered, List<Integer> quantityOffered,
                    List<String> totalOfferPrice, List<Long> dateOffered){
        this.activity = activity;
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
        holder.tvPriceOffered.setText(priceOffered.get(position));
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
            tvOffererName = (TextView) view.findViewById(R.id.tvOffererName);
            ivOffererProfilePhoto = (ImageView) view.findViewById(R.id.ivOffererProfilePhoto);
            tvPriceOffered = (TextView) view.findViewById(R.id.tvPriceOffered);
            tvQuantityOffered = (TextView) view.findViewById(R.id.tvQuantityOffered);
            tvTotalOffered = (TextView) view.findViewById(R.id.tvTotalOffered);
            bDeclineOffer = (Button) view.findViewById(R.id.bDeclineOffer);
            bAcceptOffer = (Button) view.findViewById(R.id.bAcceptOffer);
        }
    }
}
