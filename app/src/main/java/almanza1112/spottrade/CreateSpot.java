package almanza1112.spottrade;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import almanza1112.spottrade.navigationMenu.account.payment.AddPaymentMethod;
import almanza1112.spottrade.nonActivity.SharedPref;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 6/29/17.
 */

public class CreateSpot extends Fragment implements View.OnClickListener{
    private TextView tvType, tvCategory, tvLocationName, tvLocationAddress, tvAddLocation,
            tvDate, tvTime, tvQuantity;
    private TextInputLayout tilPrice;
    private TextInputEditText tietDescription, tietPrice;
    private CheckBox cbOffers, cbNow;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private double latitude, longitude;
    private String locationName, locationAddress, type, category;
    private int quantity = 1;
    int year, month, day, hour, minute;

    private ProgressDialog pd = null;
    final int[] posType = {0};
    final int[] posCategory = {0};
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_spot, container, false);

        pd = new ProgressDialog(getActivity());

        locationName = getArguments().getString("locationName");
        locationAddress = getArguments().getString("locationAddress");
        latitude = getArguments().getDouble("latitude", 0);
        longitude = getArguments().getDouble("longitude", 0);

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
        toolbar.setTitle(getResources().getString(R.string.Create_Spot));

        tvType = view.findViewById(R.id.tvType);
        tvType.setOnClickListener(this);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCategory.setOnClickListener(this);
        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvLocationName.setOnClickListener(this);
        tvLocationAddress = view.findViewById(R.id.tvLocationAddress);
        tvLocationAddress.setOnClickListener(this);
        tvAddLocation = view.findViewById(R.id.tvAddLocation);
        tvAddLocation.setOnClickListener(this);
        tvDate = view.findViewById(R.id.tvDate);
        tvDate.setOnClickListener(this);
        tvTime = view.findViewById(R.id.tvTime);
        tvTime.setOnClickListener(this);
        cbNow = view.findViewById(R.id.cbNow);
        cbNow.setChecked(true);
        cbNow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    tvDate.setTextColor(getActivity().getColor(R.color.grey600));
                    tvTime.setTextColor(getActivity().getColor(R.color.grey600));
                } else {
                    tvDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    tvTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                }
            }
        });
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvQuantity.setText("1 " + getResources().getString(R.string.available));
        tvQuantity.setOnClickListener(this);

        if (locationName.equals("empty")){
            tvLocationName.setVisibility(View.GONE);
            tvLocationAddress.setVisibility(View.GONE);
            tvAddLocation.setVisibility(View.VISIBLE);
        } else{
            tvLocationName.setText(locationName);
            tvLocationAddress.setText(locationAddress);
        }

        tietDescription = view.findViewById(R.id.tietDescription);
        tilPrice = view.findViewById(R.id.tilPrice);
        tietPrice = view.findViewById(R.id.tietPrice);
        cbOffers = view.findViewById(R.id.cbOffers);

        view.findViewById(R.id.bCreateSpot).setOnClickListener(this);

        calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvType:
                final CharSequence[] itemsType = {getResources().getString(R.string.Sell), getResources().getString(R.string.Request)};
                final AlertDialog.Builder alertDBType = new AlertDialog.Builder(getActivity());
                alertDBType.setTitle(R.string.Type);
                alertDBType.setSingleChoiceItems(itemsType, posType[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        posType[0] = which;
                    }
                });
                alertDBType.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDBType.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvType.setError(null);
                        if (posType[0] == 0){
                            type = "Sell";
                            tvType.setText(R.string.Sell);
                        } else {
                            type = "Request";
                            tvType.setText(R.string.Request);
                        }
                    }
                });
                final AlertDialog adType = alertDBType.create();
                adType.show();
                break;

            case R.id.tvCategory:
                final CharSequence[] itemsCategory = {getString(R.string.Line), getString(R.string.Parking), getString(R.string.Other),};
                final AlertDialog.Builder alertDB = new AlertDialog.Builder(getActivity());
                alertDB.setTitle(R.string.Category);
                alertDB.setSingleChoiceItems(itemsCategory, posCategory[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        posCategory[0] = which;
                    }
                });
                alertDB.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDB.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvCategory.setError(null);
                        if (posCategory[0] == 0){
                            category = "Line";
                            tvCategory.setText(R.string.Line);
                        } else if (posCategory[0] == 1){
                            category = "Parking";
                            tvCategory.setText(R.string.Parking);
                        } else if (posCategory[0] == 2){
                            category = "Other";
                            tvCategory.setText(R.string.Other);
                        }
                    }
                });
                final AlertDialog adCategory = alertDB.create();
                adCategory.show();
                break;

            case R.id.bCreateSpot:
                boolean price = validatePrice();
                boolean cat = validateCategory();
                boolean loc = validateLocation();
                boolean ty = validateType();
                boolean dateTime = validateDateTime();
                if (price && cat && loc && ty && dateTime){
                    pd.setTitle(R.string.Adding_Spot);
                    pd.setCancelable(false);
                    pd.show();
                    if (type.equals("Request")){
                        validatePaymentMethod();
                    } else {
                        postRequest();
                    }
                }
                break;

            case R.id.tvQuantity:
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.number_picker, null);

                final NumberPicker npQuantity = alertLayout.findViewById(R.id.npQuantity);
                npQuantity.setMinValue(1);
                npQuantity.setMaxValue(100);
                npQuantity.setValue(quantity);

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setView(alertLayout);
                alertDialogBuilder.setTitle(R.string.Quantity);
                alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quantity = npQuantity.getValue();
                        tvQuantity.setText(String.valueOf(quantity) + " " + getResources().getString(R.string.available));
                    }
                });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;

            case R.id.tvDate:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        setDate(year, month, dayOfMonth);
                        Calendar cDate = Calendar.getInstance();
                        cDate.set(Calendar.YEAR, year);
                        cDate.set(Calendar.MONTH, month);
                        cDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvDate.setText(epochToDateString(cDate.getTimeInMillis()));

                        cbNow.setChecked(false);
                        tvTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                        tvDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    }
                }, year, month , day);
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();
                break;

            case R.id.tvTime:
                Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setTime(hourOfDay, minute);
                        Calendar calendarTime = Calendar.getInstance();
                        calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendarTime.set(Calendar.MINUTE, minute);
                        tvTime.setText(epochToTimeString(calendarTime.getTimeInMillis()));

                        cbNow.setChecked(false);
                        tvTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                        tvDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    }
                }, hour, minute, false);
                timePickerDialog.show();
                break;

            default:
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                locationName = place.getName().toString();
                locationAddress = place.getAddress().toString();

                tvLocationName.setText(locationName);
                tvLocationAddress.setText(locationAddress);
                tvAddLocation.setError(null);
                tvAddLocation.setVisibility(View.GONE);
                tvLocationName.setVisibility(View.VISIBLE);
                tvLocationAddress.setVisibility(View.VISIBLE);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(getActivity(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setVisible(false);
        MenuItem filterItem = menu.findItem(R.id.filterMaps);
        filterItem.setVisible(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            spotCreatedListener = (SpotCreatedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnItemClickedListener");
        }
    }

    SpotCreatedListener spotCreatedListener = spotCreatedMethodCallback;

    public interface SpotCreatedListener{
        void onSpotCreated(Double lat, Double lng, String name, String id);
    }

    public static SpotCreatedListener spotCreatedMethodCallback = new SpotCreatedListener() {
        @Override
        public void onSpotCreated(Double lat, Double lng, String name, String id) {

        }
    };

    private boolean validatePrice(){
        if (tietPrice.getText().toString().isEmpty()){
            tilPrice.setError(getResources().getString(R.string.Must_have_price));
            return false;
        } else {
            tilPrice.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateType(){
        if (type == null){
            tvType.setError(getString(R.string.No_Type_selected));
            return false;
        } else {
            tvType.setError(null);
            return true;
        }
    }

    private boolean validateCategory(){
        if (category == null){
            tvCategory.setError(getString(R.string.No_Category_selected));
            return false;
        } else {
            tvCategory.setError(null);
            return true;
        }
    }

    private boolean validateLocation(){
        if (tvLocationName.getText().toString().isEmpty()){
            tvAddLocation.setError(getString(R.string.No_Location_added));
            return false;
        } else {
            tvAddLocation.setError(null);
            return true;
        }
    }

    private boolean validateDateTime(){
        if (cbNow.isChecked()){
            return true;
        } else {
            tvDate.setError(null);
            tvTime.setError(null);
            boolean bDate = tvDate.getText().toString().equals(getString(R.string.Date));
            boolean bTime = tvTime.getText().toString().equals(getString(R.string.Time));
            if (bDate || bTime){
                if (bDate){
                    tvDate.setError(getString(R.string.No_date_selected));
                }
                if (bTime){
                    tvTime.setError(getString(R.string.No_time_selected));
                }
                return false;
            } else{
                return true;
            }
        }
    }

    public void validatePaymentMethod(){
        if (!pd.isShowing()){
            pd.setTitle(R.string.Adding_Spot);
            pd.setCancelable(false);
            pd.show();
        }
        pd.setMessage(getResources().getString(R.string.Checking_for_payment_methods));
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
                                if (new JSONArray(response.getJSONObject("customer").getString("paymentMethods")).length() != 0){
                                    postRequest();
                                } else {
                                    pd.dismiss();
                                    ADnoPaymentMethod();
                                }
                            } else if (response.getString("status").equals("fail")) {
                                pd.dismiss();
                                ADnoPaymentMethod();
                            }
                        } catch (JSONException e){
                            pd.dismiss();
                            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(jsonObjectRequest);
    }

    private void ADnoPaymentMethod(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.No_Payment_Method);
        alertDialogBuilder.setMessage(R.string.You_have_no_payment_method);
        alertDialogBuilder.setNegativeButton(R.string.Not_Now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.Add_Payment_Method, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = new Bundle();
                AddPaymentMethod addPaymentMethod = new AddPaymentMethod();
                bundle.putString("from", "CreateSpot");
                addPaymentMethod.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
                fragmentTransaction.add(R.id.create_spot, addPaymentMethod);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void postRequest(){
        pd.setMessage(getResources().getString(R.string.Adding_spot_to_SpotTrade_database));
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final JSONObject jsonObject = new JSONObject();
        final JSONObject sellerInfoObj = new JSONObject();
        try {
            jsonObject.put("type", type);
            jsonObject.put("category", category);
            jsonObject.put("transaction", "available");
            jsonObject.put("name", locationName);
            jsonObject.put("price", tietPrice.getText().toString());
            jsonObject.put("offerAllowed", cbOffers.isChecked());
            jsonObject.put("address", locationAddress);
            jsonObject.put("latitude", String.valueOf(latitude));
            jsonObject.put("longitude", String.valueOf(longitude));
            jsonObject.put("description", tietDescription.getText().toString());
            jsonObject.put("quantity", quantity);
            jsonObject.put("hasBuyer", false);

            sellerInfoObj.put("sellerID", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)));
            jsonObject.put("sellerInfo", sellerInfoObj);

            Calendar calendar = Calendar.getInstance();
            if (!cbNow.isChecked()){
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            }
            jsonObject.put("dateTimeStart", calendar.getTimeInMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, getString(R.string.URL) +"/location/add", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")){
                                pd.dismiss();
                                spotCreatedListener.onSpotCreated(
                                        Double.valueOf(response.getString("latitude")),
                                        Double.valueOf(response.getString("longitude")),
                                        response.getString("_id"),
                                        response.getString("name"));
                            } else {
                                pd.dismiss();
                                Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e){
                            pd.dismiss();
                            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
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

    private String epochToDateString(long epochSeconds) {
        Date updatedate = new Date(epochSeconds);
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        return format.format(updatedate);
    }

    private String epochToTimeString(long epochSeconds) {
        Date updatedate = new Date(epochSeconds);
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return format.format(updatedate);
    }

    private void setDate(int year, int month, int day){
        this.year = year;
        this.month = month;
        this.day = day;
    }

    private void setTime(int hour, int minute){
        this.hour = hour;
        this.minute = minute;
    }
}