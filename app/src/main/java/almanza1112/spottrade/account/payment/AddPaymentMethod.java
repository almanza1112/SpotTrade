package almanza1112.spottrade.account.payment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.AndroidPay;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.Venmo;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.BraintreeResponseListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 8/24/17.
 */

public class AddPaymentMethod extends Fragment implements View.OnClickListener{

    private ProgressDialog pd = null;

    final PaymentMethodNonceCreatedListener paymentMethodNonceCreatedListener = new PaymentMethodNonceCreatedListener() {
        @Override
        public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
            addPaymentMethod(paymentMethodNonce.getNonce());
        }
    };
    final BraintreeErrorListener errorListener = new BraintreeErrorListener() {
        @Override
        public void onError(Exception error) {
            Log.e("errorListener", "error");
            error.printStackTrace();
        }
    };

    final ConfigurationListener configurationListener = new ConfigurationListener() {
        @Override
        public void onConfigurationFetched(Configuration configuration) {
            Log.e("confidListender", configuration + "");
        }
    };
    final BraintreeCancelListener braintreeCancelListener = new BraintreeCancelListener() {
        @Override
        public void onCancel(int requestCode) {
        }
    };


    BraintreeFragment mBraintreeFragment;
    //private final String CREDIT_DEBIT_CARD = "credit or debit card";
    private final String PAYPAL = "PayPal";
    private final String VENMO = "Venmo";
    private final String ANDROID_PAY = "Android Pay";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_payment_method, container, false);

        pd = new ProgressDialog(getActivity());

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Add_Payment_Method);

        final LinearLayout llCreditDebitCard = (LinearLayout) view.findViewById(R.id.llCreditDebitCard);
        final TextView tvPayPal = (TextView) view.findViewById(R.id.tvPayPal);
        final TextView tvVenmo = (TextView) view.findViewById(R.id.tvVenmo);
        final TextView tvAndroidPay = (TextView) view.findViewById(R.id.tvAndroidPay);

        llCreditDebitCard.setOnClickListener(this);
        tvPayPal.setOnClickListener(this);
        tvVenmo.setOnClickListener(this);
        tvAndroidPay.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.llCreditDebitCard:
                AddCreditDebitCard addCreditDebitCard = new AddCreditDebitCard();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.payment_activity, addCreditDebitCard);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.tvPayPal:
                getClientToken(PAYPAL);
                break;

            case R.id.tvVenmo:
                getClientToken(VENMO);
                break;

            case R.id.tvAndroidPay:
                getClientToken(ANDROID_PAY);
                break;
        }
    }

    private void getClientToken(final String type){
        pd.setTitle(type);
        pd.setMessage(getResources().getString(R.string.Obtaining_credentials));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                httpConnection.htppConnectionURL() + "/payment/clientToken",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")){
                                String clientToken = response.getString("clientToken");
                                try {
                                    mBraintreeFragment = BraintreeFragment.newInstance(getActivity(), clientToken);
                                    mBraintreeFragment.addListener(paymentMethodNonceCreatedListener);
                                    mBraintreeFragment.addListener(errorListener);
                                    mBraintreeFragment.addListener(configurationListener);
                                    mBraintreeFragment.addListener(braintreeCancelListener);
                                    switch (type){
                                        case PAYPAL:
                                            PayPal.authorizeAccount(mBraintreeFragment);
                                            break;

                                        case VENMO:
                                            Venmo.authorizeAccount(mBraintreeFragment);
                                            break;

                                        case ANDROID_PAY:
                                            AndroidPay.isReadyToPay(mBraintreeFragment, new BraintreeResponseListener<Boolean>() {
                                                @Override
                                                public void onResponse(Boolean aBoolean) {
                                                    if (!aBoolean){
                                                        pd.dismiss();
                                                        Toast.makeText(getActivity(), getResources().getString(R.string.Please_set_up_Android_Pay), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            break;
                                    }
                                } catch (InvalidArgumentException e) {
                                    // There was an issue with your authorization string.
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Log.e("client", "error retrieving clienToken");
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void addPaymentMethod(String paymentMethodNonce){
        pd.setMessage(getResources().getString(R.string.Adding_payment_method));
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", SharedPref.getID(getActivity()));
            jsonObject.put("firstName", SharedPref.getFirstName(getActivity()));
            jsonObject.put("lastName", SharedPref.getLastName(getActivity()));
            jsonObject.put("email", SharedPref.getEmail(getActivity()));
            jsonObject.put("paymentMethodNonce", paymentMethodNonce);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() +"/payment/customer/addpaymentmethod", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            pd.dismiss();
                            if (response.getString("status").equals("success")){
                                Toast.makeText(getActivity(), getResources().getString(R.string.Payment_method_successfully_added), Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.e("addPaymentMethod", "error");
                        error.printStackTrace();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);

    }

    private void postNonceToServer(String nonce){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("payment_method_nonce", nonce);
            jsonObject.put("amount", 20);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnection httpConnection = new HttpConnection();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, httpConnection.htppConnectionURL() +"/payment/checkout", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("nonce", response + "");
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(jsObjRequest);

    }
}
