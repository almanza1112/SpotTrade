package almanza1112.spottrade.account.payment;

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
 * Created by almanza1112 on 8/28/17.
 */

class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.RecyclerViewHolder> {

    private Activity activity;
    private List<String> paymentType;
    private List<String> imageURL;
    private List<String> credentials;
    private List<String> expirationDate;
    PaymentAdapter(Activity activity, List<String> paymentType, List<String> imageURL, List<String> credentials, List<String> expirationDate){
        this.activity = activity;
        this.paymentType = paymentType;
        this.imageURL = imageURL;
        this.credentials = credentials;
        this.expirationDate = expirationDate;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PaymentAdapter.RecyclerViewHolder holder, int position) {
        Picasso.with(activity).load(imageURL.get(position)).into(holder.ivImage);
        holder.tvCredentials.setText(credentials.get(position));
    }

    @Override
    public int getItemCount() {
        return paymentType.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivImage;
        TextView tvCredentials;
        RecyclerViewHolder(View view){
            super(view);
            ivImage = (ImageView) view.findViewById(R.id.ivImage);
            tvCredentials = (TextView) view.findViewById(R.id.tvCredentials);
        }
    }
}
