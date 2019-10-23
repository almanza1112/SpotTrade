package almanza1112.spottrade;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import com.google.android.material.chip.Chip;
import androidx.fragment.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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

/**
 * Created by almanza1112 on 6/29/17.
 */

public class CreateSpot extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private TextView tvLocationName, tvLocationAddress, tvAddLocation, tvStartDate, tvStartTime, tvEndDate, tvEndTime, tvQuantity;
    private TextInputLayout tilPrice;
    private TextInputEditText tietDescription, tietPrice;
    private Chip cSell, cRequest, cParking, cLine, cOther;
    private CheckBox cbOffers, cbNow, cbUntilBought;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private double latitude, longitude;
    private String type, category, locationName, locationAddress;
    private int quantity = 1;
    int startYear, startMonth, startDay, startHour, startMinute, endYear, endMonth, endDay, endHour, endMinute;

    private ProgressDialog pd = null;
    private Calendar startCalendar, endCalendar;

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
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
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

        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvLocationName.setOnClickListener(this);
        tvLocationAddress = view.findViewById(R.id.tvLocationAddress);
        tvLocationAddress.setOnClickListener(this);
        tvAddLocation = view.findViewById(R.id.tvAddLocation);
        tvAddLocation.setOnClickListener(this);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvStartDate.setOnClickListener(this);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvStartTime.setOnClickListener(this);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvEndDate.setOnClickListener(this);
        tvEndTime = view.findViewById(R.id.tvEndTime);
        tvEndTime.setOnClickListener(this);
        cbNow = view.findViewById(R.id.cbNow);
        cbNow.setChecked(true);
        cbNow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    tvStartDate.setTextColor(getActivity().getColor(R.color.grey600));
                    tvStartTime.setTextColor(getActivity().getColor(R.color.grey600));
                } else {
                    tvStartDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    tvStartTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                }
            }
        });
        cbUntilBought = view.findViewById(R.id.cbUntilBought);
        cbUntilBought.setChecked(true);
        cbUntilBought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    tvEndDate.setTextColor(getActivity().getColor(R.color.grey600));
                    tvEndTime.setTextColor(getActivity().getColor(R.color.grey600));
                } else {
                    tvEndDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    tvEndTime.setTextColor(getActivity().getColor(R.color.colorAccent));
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
        cSell = view.findViewById(R.id.cSell);
        cSell.setChecked(true);
        cSell.setClickable(false);
        type = getString(R.string.Sell);
        cSell.setOnCheckedChangeListener(this);
        cRequest = view.findViewById(R.id.cRequest);
        cRequest.setOnCheckedChangeListener(this);
        cParking = view.findViewById(R.id.cParking);
        cParking.setChecked(true);
        cParking.setClickable(false);
        category = getString(R.string.Parking);
        cParking.setOnCheckedChangeListener(this);
        cLine = view.findViewById(R.id.cLine);
        cLine.setOnCheckedChangeListener(this);
        cOther = view.findViewById(R.id.cOther);
        cOther.setOnCheckedChangeListener(this);
        cOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ADaddOtherCategory();
            }
        });

        view.findViewById(R.id.mbCreateSpot).setOnClickListener(this);
        startCalendar = Calendar.getInstance();
        startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        startMonth = startCalendar.get(Calendar.MONTH);
        startYear = startCalendar.get(Calendar.YEAR);
        startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        startMinute = startCalendar.get(Calendar.MINUTE);

        endCalendar = Calendar.getInstance();
        endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        endMonth = endCalendar.get(Calendar.MONTH);
        endYear = endCalendar.get(Calendar.YEAR);
        endHour = endCalendar.get(Calendar.HOUR);
        endMinute = endCalendar.get(Calendar.MINUTE);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mbCreateSpot:
                boolean price = validatePrice();
                boolean loc = validateLocation();
                boolean startDateTime = validateStartDateTime();
                boolean endDateTime = validateEndDateTime();
                if (price && loc && startDateTime && endDateTime){
                    pd.setTitle(R.string.Adding_Spot);
                    pd.setCancelable(false);
                    pd.show();
                    postRequest();
                }
                break;

            case R.id.tvQuantity:
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.number_picker, null);

                final NumberPicker npQuantity = alertLayout.findViewById(R.id.npQuantity);
                npQuantity.setMinValue(1);
                npQuantity.setMaxValue(100);
                npQuantity.setValue(quantity);

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustomTheme);
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

            case R.id.tvStartDate:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startYear = year;
                        startMonth = month;
                        startDay = dayOfMonth;
                        Calendar cDate = Calendar.getInstance();
                        cDate.set(Calendar.YEAR, year);
                        cDate.set(Calendar.MONTH, month);
                        cDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvStartDate.setText(epochToDateString(cDate.getTimeInMillis()));

                        cbNow.setChecked(false);
                        tvStartTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                        tvStartDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    }
                }, startYear, startMonth, startDay);
                datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
                datePickerDialog.show();
                break;

            case R.id.tvStartTime:
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startHour = hourOfDay;
                        startMinute = minute;
                        Calendar calendarTime = Calendar.getInstance();
                        calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendarTime.set(Calendar.MINUTE, minute);
                        tvStartTime.setText(epochToTimeString(calendarTime.getTimeInMillis()));

                        cbNow.setChecked(false);
                        tvStartTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                        tvStartDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    }
                }, startHour, startMinute, false);
                timePickerDialog.show();
                break;

            case R.id.tvEndDate:
                DatePickerDialog endDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        endYear = year;
                        endMonth = month;
                        endDay = dayOfMonth;
                        Calendar cDate = Calendar.getInstance();
                        cDate.set(Calendar.YEAR, year);
                        cDate.set(Calendar.MONTH, month);
                        cDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvEndDate.setText(epochToDateString(cDate.getTimeInMillis()));

                        cbUntilBought.setChecked(false);
                        tvEndTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                        tvEndDate.setTextColor(getActivity().getColor(R.color.colorAccent));
                    }
                }, endYear, endMonth, endDay);
                endDatePickerDialog.getDatePicker().setMinDate(endCalendar.getTimeInMillis());
                endDatePickerDialog.show();
                break;

            case R.id.tvEndTime:
                TimePickerDialog endTimePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endHour = hourOfDay;
                        endMinute = minute;
                        Calendar calendarTime = Calendar.getInstance();
                        calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendarTime.set(Calendar.MINUTE, minute);
                        tvEndTime.setText(epochToTimeString(calendarTime.getTimeInMillis()));

                        cbUntilBought.setChecked(false);
                        tvEndTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                        tvEndTime.setTextColor(getActivity().getColor(R.color.colorAccent));
                    }
                }, endHour, endMinute, false);
                endTimePickerDialog.show();
                break;

            default:
                /**
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                } **/
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            if (buttonView.getId() == cSell.getId()){
                type = getString(R.string.Sell);
                cSell.setClickable(false);
                cRequest.setClickable(true);
            }
            if (buttonView.getId() == cRequest.getId()){
                type = getString(R.string.Request);
                cRequest.setClickable(false);
                cSell.setClickable(true);
            }
            if (buttonView.getId() == cParking.getId()){
                category = getString(R.string.Parking);
                cParking.setClickable(false);
                cLine.setClickable(true);
                cOther.setText(getString(R.string.Other));
            }
            if (buttonView.getId() == cLine.getId()){
                category = getString(R.string.Line);
                cLine.setClickable(false);
                cParking.setClickable(true);
                cOther.setText(getString(R.string.Other));
            }
            if (buttonView.getId() == cOther.getId()){
                cLine.setClickable(true);
                cParking.setClickable(true);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            /**
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
            } **/
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

    private void ADaddOtherCategory(){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.create_spot_add_other_category_alertdialog, null);
        final TextInputLayout tilOtherCategory = alertLayout.findViewById(R.id.tilOtherCategory);
        final TextInputEditText tietOtherCategory = alertLayout.findViewById(R.id.etOtherCategory);
        if (!category.equals(getString(R.string.Parking)) && !category.equals(getString(R.string.Line))){
            tietOtherCategory.setText(category);
        }
        final AlertDialog.Builder alertDB = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustomTheme);
        alertDB.setView(alertLayout);
        alertDB.setTitle(R.string.Add_Category);
        alertDB.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDB.setPositiveButton(R.string.Confirm, null);

        final AlertDialog adCategory = alertDB.create();

        adCategory.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialogInterface) {

                Button button = adCategory.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        if (tietOtherCategory.getText().toString().isEmpty()){
                            tilOtherCategory.setError(getResources().getString(R.string.Field_cant_be_empty));

                        } else {
                            String otherCategory = tietOtherCategory.getText().toString();
                            category = otherCategory;
                            cOther.setChecked(true);
                            cOther.setText(otherCategory);
                            adCategory.dismiss();
                        }
                    }
                });
            }
        });
        adCategory.show();
    }

    private boolean validatePrice(){
        if (tietPrice.getText().toString().isEmpty()){
            tilPrice.setError(getResources().getString(R.string.Must_have_price));
            return false;
        } else {
            tilPrice.setErrorEnabled(false);
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

    private boolean validateStartDateTime(){
        if (cbNow.isChecked()){
            return true;
        } else {
            tvStartDate.setError(null);
            tvStartTime.setError(null);
            boolean bDate = tvStartDate.getText().toString().equals(getString(R.string.Date));
            boolean bTime = tvStartTime.getText().toString().equals(getString(R.string.Time));
            if (bDate || bTime){
                if (bDate){
                    tvStartDate.setError(getString(R.string.No_date_selected));
                }
                if (bTime){
                    tvStartTime.setError(getString(R.string.No_time_selected));
                }
                return false;
            } else{
                return true;
            }
        }
    }

    private boolean validateEndDateTime(){
        if (cbUntilBought.isChecked()){
            return true;
        } else {
            tvEndDate.setError(null);
            tvEndTime.setError(null);
            boolean bDate = tvEndDate.getText().toString().equals(getString(R.string.Date));
            boolean bEnd = tvEndTime.getText().toString().equals(getString(R.string.Time));
            if (bDate || bEnd) {
                if (bDate) {
                    tvEndDate.setError(getResources().getString(R.string.No_date_selected));
                }
                if (bEnd) {
                    tvEndTime.setError(getResources().getString(R.string.No_time_selected));
                }
                return false;
            } else {
                return true;
            }
        }
    }

    private void ADnoPaymentMethod(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustomTheme);
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
        final JSONObject posterInfoObj = new JSONObject();
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
            jsonObject.put("hasAcceptor", false);

            posterInfoObj.put("posterId", SharedPref.getSharedPreferences(getActivity(), getResources().getString(R.string.logged_in_user_id)));
            jsonObject.put("posterInfo", posterInfoObj);

            Calendar startCalendar = Calendar.getInstance();
            if (!cbNow.isChecked()){
                startCalendar.set(Calendar.YEAR, startYear);
                startCalendar.set(Calendar.MONTH, startMonth);
                startCalendar.set(Calendar.DAY_OF_MONTH, startDay);
                startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
                startCalendar.set(Calendar.MINUTE, startMinute);
            }
            jsonObject.put("dateTimeStart", startCalendar.getTimeInMillis());
            jsonObject.put("hasEndDateTime", !cbUntilBought.isChecked());
            if (!cbUntilBought.isChecked()){
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.set(Calendar.YEAR, endYear);
                endCalendar.set(Calendar.MONTH, endMonth);
                endCalendar.set(Calendar.DAY_OF_MONTH, endDay);
                endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
                endCalendar.set(Calendar.MINUTE, endMinute);
                jsonObject.put("dateTimeEnd", endCalendar.getTimeInMillis());
            }
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
                                spotCreatedListener.onSpotCreated(
                                        Double.valueOf(response.getString("latitude")),
                                        Double.valueOf(response.getString("longitude")),
                                        response.getString("_id"),
                                        response.getString("name"));
                            } else if (status.equals("fail") && response.getString("reason").equals("user not found")){
                                ADnoPaymentMethod();
                            } else {
                                Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e){
                            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
                        }
                        pd.dismiss();
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
}