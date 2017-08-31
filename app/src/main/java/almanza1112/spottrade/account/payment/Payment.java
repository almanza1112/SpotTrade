package almanza1112.spottrade.account.payment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 7/19/17.
 */

public class Payment extends Fragment {

    private ProgressBar progressBar;
    RecyclerView rvPaymentMethods;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Payment);

        AppCompatActivity actionBar = (AppCompatActivity) getActivity();
        actionBar.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) actionBar.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        rvPaymentMethods = (RecyclerView) view.findViewById(R.id.rvPaymentMethods);
        final Button bAddPaymentMethod = (Button) view.findViewById(R.id.bAddPaymentMethod);
        bAddPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPaymentMethod addPaymentMethod = new AddPaymentMethod();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.payment_activity, addPaymentMethod);
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
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.search);
        item.setVisible(false);
    }

    private void getCustomer(){
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                httpConnection.htppConnectionURL() + "/payment/customer/" + SharedPref.getID(getActivity()),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                boolean isEmpty = true;
                                List<String> paymentType = new ArrayList<>();
                                List<String> paymentTypeName = new ArrayList<>();
                                List<String> imageURL = new ArrayList<>();
                                List<String> credentials = new ArrayList<>();
                                List<String> expirationDate = new ArrayList<>();
                                List<String> token = new ArrayList<>();
                                List<Boolean> isDefault = new ArrayList<>();
                                JSONObject customerObj = new JSONObject(response.getString("customer"));
                                if (customerObj.has("creditCards")){
                                    isEmpty = false;
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
                                    isEmpty = false;
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
                                if (!isEmpty){
                                    RecyclerView.Adapter  adapter = new PaymentAdapter(getActivity(), paymentType, paymentTypeName, imageURL, credentials, expirationDate, token, isDefault);
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                    rvPaymentMethods.setLayoutManager(layoutManager);
                                    rvPaymentMethods.setAdapter(adapter);
                                }

                            }
                            else if (!response.getString("status").equals("fail")) {
                                Toast.makeText(getActivity(), "Error: could not retrieve payment methods", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
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
}
