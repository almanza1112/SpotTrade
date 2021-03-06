package almanza1112.spottrade.navigationMenu.account.payment;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PaymentMethodNonce;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 8/28/17.
 */

public class AddCreditDebitCard extends Fragment {


    private ProgressDialog pd = null;
    private TextInputEditText tietCardNumber, tietExpirationDate, tietCVV, tietZipCode;
    private TextInputLayout tilCardNumber, tilExpirationDate, tilCVV, tilZipCode;
    private String from;

    final PaymentMethodNonceCreatedListener paymentMethodNonceCreatedListener = new PaymentMethodNonceCreatedListener() {
        @Override
        public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
            addPaymentMethod(paymentMethodNonce.getNonce());

        }
    };

    final BraintreeErrorListener errorListener = new BraintreeErrorListener() {
        @Override
        public void onError(Exception error) {
            pd.dismiss();
            Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            Log.e("cancelListener", requestCode + "");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_credit_debit_card, container, false);

        pd = new ProgressDialog(getActivity());

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
        int statusBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.status_bar_height)));
        int actionBarHeight = Integer.valueOf(SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.action_bar_height)));
        layoutParams.height = actionBarHeight + statusBarHeight;
        toolbar.setLayoutParams(layoutParams);
        toolbar.setPadding(0, statusBarHeight, 0, 0);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Add_Credit_or_Debit_Card);

        tilCardNumber = view.findViewById(R.id.tilCardNumber);
        tietCardNumber = view.findViewById(R.id.tietCardNumber);
        tilExpirationDate = view.findViewById(R.id.tilExpirationDate);
        tietExpirationDate = view.findViewById(R.id.tietExpirationDate);
        tilCVV = view.findViewById(R.id.tilCVV);
        tietCVV = view.findViewById(R.id.tietCVV);
        tilZipCode = view.findViewById(R.id.tilZipCode);
        tietZipCode = view.findViewById(R.id.tietZipCode);

        tietCardNumber.addTextChangedListener(new TextWatcher() {

            private static final int TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
            private static final int TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
            private static final int DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
            private static final int DIVIDER_POSITION = DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
            private static final char DIVIDER = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isInputCorrect(s, TOTAL_SYMBOLS, DIVIDER_MODULO, DIVIDER)) {
                    s.replace(0, s.length(), buildCorrectString(getDigitArray(s, TOTAL_DIGITS), DIVIDER_POSITION, DIVIDER));
                }

            }

            private boolean isInputCorrect(Editable s, int totalSymbols, int dividerModulo, char divider) {
                boolean isCorrect = s.length() <= totalSymbols; // check size of entered string
                for (int i = 0; i < s.length(); i++) { // chech that every element is right
                    if (i > 0 && (i + 1) % dividerModulo == 0) {
                        isCorrect &= divider == s.charAt(i);
                    } else {
                        isCorrect &= Character.isDigit(s.charAt(i));
                    }
                }
                return isCorrect;
            }

            private String buildCorrectString(char[] digits, int dividerPosition, char divider) {
                final StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < digits.length; i++) {
                    if (digits[i] != 0) {
                        formatted.append(digits[i]);
                        if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                            formatted.append(divider);
                        }
                    }
                }

                return formatted.toString();
            }

            private char[] getDigitArray(final Editable s, final int size) {
                char[] digits = new char[size];
                int index = 0;
                for (int i = 0; i < s.length() && index < size; i++) {
                    char current = s.charAt(i);
                    if (Character.isDigit(current)) {
                        digits[index] = current;
                        index++;
                    }
                }
                return digits;
            }

        });
        tietExpirationDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0 && (editable.length() % 3) == 0) {
                    final char c = editable.charAt(editable.length() - 1);
                    if ('/' == c) {
                        editable.delete(editable.length() - 1, editable.length());
                    }
                }
                if (editable.length() > 0 && (editable.length() % 3) == 0) {
                    char c = editable.charAt(editable.length() - 1);
                    if (Character.isDigit(c) && TextUtils.split(editable.toString(), String.valueOf("/")).length <= 2) {
                        editable.insert(editable.length() - 1, String.valueOf("/"));
                    }
                }
            }
        });

        final Button bAddCard = view.findViewById(R.id.bAddCard);
        bAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    getClientToken();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        from = getArguments().getString("from");
        try {
            creditCardAddedListener = (CreditCardAddedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItemClickedListener");
        }
    }

    public interface CreditCardAddedListener {
        void onCreditCardAdded(String from);
    }

    CreditCardAddedListener creditCardAddedListener = creditCardAddedCallback;

    private static CreditCardAddedListener creditCardAddedCallback = new CreditCardAddedListener() {
        @Override
        public void onCreditCardAdded(String from) {

        }
    };

    private boolean validateFields(){
        boolean cardNum, exprDate, cvv, zipcode;
        if (tietCardNumber.getText().toString().isEmpty()){
            cardNum = false;
            tilCardNumber.setError(getResources().getString(R.string.Field_cant_be_empty));
        } else{
            cardNum = true;
            tilCardNumber.setErrorEnabled(false);
        }

        if (tietCVV.getText().toString().isEmpty()){
            cvv = false;
            tilCVV.setError(getResources().getString(R.string.Field_cant_be_empty));
        } else {
            cvv = true;
            tilCVV.setErrorEnabled(false);
        }

        if (tietExpirationDate.getText().toString().isEmpty()){
            exprDate = false;
            tilExpirationDate.setError(getResources().getString(R.string.Field_cant_be_empty));
        } else {
            exprDate = true;
            tilExpirationDate.setErrorEnabled(false);
        }

        if (tietZipCode.getText().toString().isEmpty()){
            zipcode = false;
            tilZipCode.setError(getResources().getString(R.string.Field_cant_be_empty));
        } else {
            zipcode = true;
            tilZipCode.setErrorEnabled(false);
        }

        return cardNum && cvv && exprDate && zipcode;
    }

    private void getClientToken(){
        pd.setTitle(R.string.Adding_Card);
        pd.setMessage(getResources().getString(R.string.Adding_and_verifying_card));
        pd.setCancelable(false);
        pd.show();
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.URL) + "/payment/clientToken",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")){
                                String clientToken = response.getString("clientToken");
                                try {
                                    BraintreeFragment mBraintreeFragment = BraintreeFragment.newInstance(getActivity(), clientToken);
                                    mBraintreeFragment.addListener(paymentMethodNonceCreatedListener);
                                    mBraintreeFragment.addListener(errorListener);
                                    mBraintreeFragment.addListener(configurationListener);
                                    mBraintreeFragment.addListener(braintreeCancelListener);
                                    CardBuilder cardBuilder = new CardBuilder()
                                            .cardNumber(tietCardNumber.getText().toString())
                                            .expirationMonth(tietExpirationDate.getText().toString().substring(0, 2))
                                            .expirationYear(tietExpirationDate.getText().toString().substring(3, 5))
                                            .cvv(tietCVV.getText().toString())
                                            .postalCode(tietZipCode.getText().toString())
                                            .validate(true);
                                    Card.tokenize(mBraintreeFragment, cardBuilder);
                                } catch (InvalidArgumentException e) {
                                    setToastServerError();
                                }
                            } else {
                                setToastServerError();
                            }
                        } catch (JSONException e){
                            setToastServerError();
                        }
                        pd.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        setToastServerError();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void addPaymentMethod(String paymentMethodNonce){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)));
            jsonObject.put("firstName", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_first_name)));
            jsonObject.put("lastName", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_last_name)));
            jsonObject.put("email", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_email)));
            jsonObject.put("paymentMethodNonce", paymentMethodNonce);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, getString(R.string.URL) +"/payment/customer/addpaymentmethod", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("success")){
                                creditCardAddedListener.onCreditCardAdded(from);
                            } else {
                                setToastServerError();
                            }
                        } catch (JSONException e){
                            setToastServerError();
                        }
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        setToastServerError();
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

        queue.add(jsObjRequest);
    }

    private void setToastServerError(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }
    }
}
