package almanza1112.spottrade.account.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by almanza1112 on 8/28/17.
 */

class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.RecyclerViewHolder> {

    private Activity activity;
    private List<String> paymentType;
    private List<String> paymentTypeName;
    private List<String> imageURL;
    private List<String> credentials;
    private List<String> expirationDate;
    private List<String> paymentToken;
    private List<Boolean> isDefault;

    private int defaultPos;

    private ProgressDialog pd = null;

    PaymentAdapter(Activity activity, List<String> paymentType, List<String> paymentTypeName,
                   List<String> imageURL, List<String> credentials, List<String> expirationDate,
                   List<String> token, List<Boolean> isDefault){
        this.activity = activity;
        this.paymentType = paymentType;
        this.paymentTypeName = paymentTypeName;
        this.imageURL = imageURL;
        this.credentials = credentials;
        this.expirationDate = expirationDate;
        this.paymentToken = token;
        this.isDefault = isDefault;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PaymentAdapter.RecyclerViewHolder holder, int position) {
        Picasso.with(activity).load(imageURL.get(position)).into(holder.ivImage);
        holder.tvPaymentTypeName.setText(paymentTypeName.get(position));
        holder.tvCredentials.setText(credentials.get(position));
        if (isDefault.get(position)){
            defaultPos = position;
            holder.tvDefault.setVisibility(View.VISIBLE);
        }
        else{
            holder.tvDefault.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return paymentType.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivImage, ivEdit;
        TextView tvPaymentTypeName, tvCredentials, tvDefault;
        RecyclerViewHolder(View view){
            super(view);
            ivImage = (ImageView) view.findViewById(R.id.ivImage);
            tvPaymentTypeName = (TextView) view.findViewById(R.id.tvPaymentTypeName);
            tvCredentials = (TextView) view.findViewById(R.id.tvCredentials);
            tvDefault = (TextView) view.findViewById(R.id.tvDefault);
            ivEdit = (ImageView) view.findViewById(R.id.ivEdit);
            ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setTitle(R.string.Payment_Method_Options);
                    CharSequence[] items;
                    if (isDefault.get(getAdapterPosition())){
                        items = new CharSequence[]{activity.getResources().getString(R.string.Delete)};
                    }
                    else{
                        items = new CharSequence[]{activity.getResources().getString(R.string.Delete), activity.getResources().getString(R.string.Make_default_payment_method)};
                    }
                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pd = new ProgressDialog(activity);
                            switch (which){
                                case 0:
                                    ADvalidateDeletion(paymentToken.get(getAdapterPosition()), getAdapterPosition());
                                    break;
                                case 1:
                                    updateDefaultPaymentMethod(paymentToken.get(getAdapterPosition()), getAdapterPosition());
                                    break;
                            }
                        }
                    });
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

        }
    }

    private void updateDefaultPaymentMethod(final String token, final int position){
        pd.setTitle(R.string.Updating);
        pd.setMessage(activity.getResources().getString(R.string.Updating_default_payment_method));
        pd.setCancelable(false);
        pd.show();
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put("token", token);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(activity);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                httpConnection.htppConnectionURL() + "/payment/customer/updatepaymentmethod",
                jObject,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        isDefault.set(position, true);
                        isDefault.set(defaultPos, false);
                        notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(activity, "Error: could not update default payment method", Toast.LENGTH_SHORT).show();
                    }
                    pd.dismiss();
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void ADvalidateDeletion(final String token, final int position){
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.payment_delete_alertdialog, null);

        ImageView ivPaymentImage = (ImageView) alertLayout.findViewById(R.id.ivPaymentImage);
        TextView tvPaymentName = (TextView) alertLayout.findViewById(R.id.tvPaymentName);
        TextView tvPaymentCredentials = (TextView) alertLayout.findViewById(R.id.tvPaymentCredentials);

        Picasso.with(activity).load(imageURL.get(position)).into(ivPaymentImage);
        tvPaymentName.setText(paymentTypeName.get(position));
        tvPaymentCredentials.setText(credentials.get(position));

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Delete);
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePaymentMethod(token, position);
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deletePaymentMethod(final String token, final int position){
        pd.setTitle(R.string.Deleting);
        pd.setMessage(activity.getResources().getString(R.string.Deleting_payment_method));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(activity);

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                httpConnection.htppConnectionURL() + "/payment/customer/deletepaymentmethod?token=" + token + "&id=" + SharedPref.getID(activity),
                null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            pd.dismiss();
                            if (response.getString("status").equals("success")){
                                Toast.makeText(activity, "Payment method deleted", Toast.LENGTH_SHORT).show();

                                if (response.has("defaultPaymentMethodToken")) {
                                    isDefault.set(paymentToken.indexOf(response.getString("defaultPaymentMethodToken")), true);
                                }

                                paymentType.remove(position);
                                paymentTypeName.remove(position);
                                imageURL.remove(position);
                                credentials.remove(position);
                                expirationDate.remove(position);
                                paymentToken.remove(position);
                                isDefault.remove(position);
                                notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(activity, "Error: could not delete payment method", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }
}
