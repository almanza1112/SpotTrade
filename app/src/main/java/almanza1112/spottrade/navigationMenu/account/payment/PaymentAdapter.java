package almanza1112.spottrade.navigationMenu.account.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
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
    private Payment payment;

    private int defaultPos;

    private ProgressDialog pd = null;

    PaymentAdapter(Payment payment,Activity activity, List<String> paymentType, List<String> paymentTypeName,
                   List<String> imageURL, List<String> credentials, List<String> expirationDate,
                   List<String> token, List<Boolean> isDefault){
        this.payment = payment;
        this.paymentType = paymentType;
        this.activity = activity;
        this.paymentType = paymentType;
        this.paymentTypeName = paymentTypeName;
        this.imageURL = imageURL;
        this.credentials = credentials;
        this.expirationDate = expirationDate;
        this.paymentToken = token;
        this.isDefault = isDefault;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentAdapter.RecyclerViewHolder holder, int position) {
        Picasso.get().load(imageURL.get(position)).into(holder.ivImage);
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
            ivImage = view.findViewById(R.id.ivImage);
            tvPaymentTypeName = view.findViewById(R.id.tvPaymentTypeName);
            tvCredentials = view.findViewById(R.id.tvCredentials);
            tvDefault = view.findViewById(R.id.tvDefault);
            ivEdit = view.findViewById(R.id.ivEdit);
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

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                activity.getString(R.string.URL) + "/payment/customer/updatepaymentmethod",
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
                        Toast.makeText(activity, activity.getResources().getString(R.string.Error_unable_to_update_payment_method), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    Toast.makeText(activity, activity.getResources().getString(R.string.Error_unable_to_delete_payment_method), Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Toast.makeText(activity, activity.getResources().getString(R.string.Error_unable_to_delete_payment_method), Toast.LENGTH_SHORT).show();
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void ADvalidateDeletion(final String token, final int position){
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.payment_delete_alertdialog, null);

        ImageView ivPaymentImage = alertLayout.findViewById(R.id.ivPaymentImage);
        TextView tvPaymentName = alertLayout.findViewById(R.id.tvPaymentName);
        TextView tvPaymentCredentials = alertLayout.findViewById(R.id.tvPaymentCredentials);

        Picasso.get().load(imageURL.get(position)).into(ivPaymentImage);
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

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                activity.getString(R.string.URL) + "/payment/customer/deletepaymentmethod?token=" + token + "&id=" + SharedPref.getSharedPreferences(activity, activity.getResources().getString(R.string.logged_in_user_id)),
                null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")){
                                payment.setSnackbar(activity.getResources().getString(R.string.Payment_method_deleted));
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

                                if (paymentType.size() == 0){
                                    payment.tvNoPaymentMethods.setVisibility(View.VISIBLE);
                                }
                            } else{
                                Toast.makeText(activity, activity.getResources().getString(R.string.Error_unable_to_delete_payment_method), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e){
                            Toast.makeText(activity, activity.getResources().getString(R.string.Error_unable_to_delete_payment_method), Toast.LENGTH_SHORT).show();
                        }
                        pd.dismiss();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(activity, activity.getResources().getString(R.string.Error_unable_to_delete_payment_method), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }
}
