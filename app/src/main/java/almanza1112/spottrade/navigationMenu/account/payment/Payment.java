package almanza1112.spottrade.navigationMenu.account.payment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 7/19/17.
 */

public class Payment extends Fragment {

    private ProgressBar progressBar;
    public TextView tvNoPaymentMethods;
    RecyclerView rvPaymentMethods;
    public Snackbar snackbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment, container, false);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        int statusBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.status_bar_height)));
        int actionBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.action_bar_height)));
        layoutParams.height = actionBarHeight + statusBarHeight;
        toolbar.setLayoutParams(layoutParams);
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Payment);

        progressBar = view.findViewById(R.id.progressBar);
        tvNoPaymentMethods = view.findViewById(R.id.tvNoPaymentMethods);
        rvPaymentMethods = view.findViewById(R.id.rvPaymentMethods);
        view.findViewById(R.id.fabAddPaymentMethod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("from", "Payment");
                AddPaymentMethod addPaymentMethod = new AddPaymentMethod();
                addPaymentMethod.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
                fragmentTransaction.add(R.id.payment, addPaymentMethod);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        getCustomer();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        if (snackbar != null){
            snackbar.dismiss();
        }
        super.onDestroy();
    }

    public void getCustomer(){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.URL) + "/payment/customer/" + SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                List<String> paymentType = new ArrayList<>();
                                List<String> paymentTypeName = new ArrayList<>();
                                List<String> imageURL = new ArrayList<>();
                                List<String> credentials = new ArrayList<>();
                                List<String> expirationDate = new ArrayList<>();
                                List<String> token = new ArrayList<>();
                                List<Boolean> isDefault = new ArrayList<>();
                                JSONObject customerObj = new JSONObject(response.getString("customer"));
                                if (customerObj.has("creditCards")){
                                    JSONArray creditCardsArray = new JSONArray(customerObj.getString("creditCards"));
                                    for (int i = 0; i < creditCardsArray.length(); i++){
                                        paymentType.add("creditCard");
                                        paymentTypeName.add(creditCardsArray.getJSONObject(i).getString("cardType"));
                                        imageURL.add(creditCardsArray.getJSONObject(i).getString("imageUrl"));
                                        int len = creditCardsArray.getJSONObject(i).getString("maskedNumber").length() - 4;
                                        String astr = "";
                                        for (int j = 0; j < len; j++){
                                            astr += "*";
                                        }
                                        token.add(creditCardsArray.getJSONObject(i).getString("token"));
                                        credentials.add(astr + creditCardsArray.getJSONObject(i).getString("last4"));
                                        expirationDate.add(creditCardsArray.getJSONObject(i).getString("expirationDate"));
                                        isDefault.add(creditCardsArray.getJSONObject(i).getBoolean("default"));
                                    }
                                }
                                if (customerObj.has("paypalAccounts")){
                                    JSONArray paypalAccountsArray = new JSONArray(customerObj.getString("paypalAccounts"));
                                    for (int i = 0; i < paypalAccountsArray.length(); i++){
                                        paymentType.add("paypal");
                                        paymentTypeName.add("PayPal");
                                        imageURL.add(paypalAccountsArray.getJSONObject(i).getString("imageUrl"));
                                        token.add(paypalAccountsArray.getJSONObject(i).getString("token"));
                                        credentials.add(paypalAccountsArray.getJSONObject(i).getString("email"));
                                        expirationDate.add("empty");
                                        isDefault.add(paypalAccountsArray.getJSONObject(i).getBoolean("default"));
                                    }
                                }
                                if (paymentType.size() > 0){
                                    tvNoPaymentMethods.setVisibility(View.GONE);
                                    RecyclerView.Adapter  adapter = new PaymentAdapter(Payment.this ,getActivity(), paymentType, paymentTypeName, imageURL, credentials, expirationDate, token, isDefault);
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                    rvPaymentMethods.setLayoutManager(layoutManager);
                                    rvPaymentMethods.setAdapter(adapter);
                                }

                            }
                            /*
                            // Returns fail if user was not found
                            else if (response.getString("status").equals("fail")) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                            }
                            */
                        }
                        catch (JSONException e){
                            setToastServerError();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        setToastServerError();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }

    public void setSnackbar(String snackbarText) {
        snackbar = Snackbar.make(getActivity().findViewById(R.id.payment), snackbarText, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void setToastServerError(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void setToastUnableToDeletePaymentMethod(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_delete_payment_method), Toast.LENGTH_SHORT).show();
        }
    }

    public void setToastUnableToUpdatePaymentMethod(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Error_unable_to_update_payment_method), Toast.LENGTH_SHORT).show();
        }
    }
}
