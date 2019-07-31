package almanza1112.spottrade.navigationMenu.yourSpots;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.support.v4.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.SharedPref;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 8/16/17.
 */

public class EditSpot extends Fragment implements View.OnClickListener{

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private String lid;
    private int quantity;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0;
    private Snackbar snackbar;
    TextView tvLocationName, tvLocationAddress, tvType, tvCategory, tvPrice, tvQuantity, tvDescription,
            tvViewOffers, tvDate, tvTime;
    private int year, month, day, hour, minute, epochTime;
    private Calendar calendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_spot, container, false);

        lid = getArguments().getString("lid");
        String name = getArguments().getString("locationName");
        String address = getArguments().getString("locationAddress");
        String type = getArguments().getString("type");
        String category = getArguments().getString("category");
        String price = getArguments().getString("price");
        quantity = getArguments().getInt("quantity");
        long dateTimeStart = getArguments().getLong("dateTimeStart");
        String description = getArguments().getString("description");
        boolean bidAllowed = getArguments().getBoolean("offerAllowed");
        int offerTotal = getArguments().getInt("offerTotal");

        toolbar = view.findViewById(R.id.toolbar);
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
        toolbar.setTitle(name);

        progressBar = view.findViewById(R.id.progressBar);

        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvLocationAddress = view.findViewById(R.id.tvLocationAddress);
        tvType = view.findViewById(R.id.tvType);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvDate = view.findViewById(R.id.tvDate);
        tvTime = view.findViewById(R.id.tvTime);
        CheckBox cbBids = view.findViewById(R.id.cbOffers);
        cbBids.setChecked(bidAllowed);
        cbBids.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateField("offerAllowed", String.valueOf(isChecked), null);
            }
        });
        tvDescription = view.findViewById(R.id.tvDescription);
        tvViewOffers = view.findViewById(R.id.tvViewOffers);
        LinearLayout llViewOffers = view.findViewById(R.id.llViewOffers);

        view.findViewById(R.id.ivEditLocation).setOnClickListener(this);
        view.findViewById(R.id.ivEditType).setOnClickListener(this);
        view.findViewById(R.id.ivEditCategory).setOnClickListener(this);
        view.findViewById(R.id.ivEditPrice).setOnClickListener(this);
        view.findViewById(R.id.ivEditQuantity).setOnClickListener(this);
        view.findViewById(R.id.ivEditDate).setOnClickListener(this);
        view.findViewById(R.id.ivEditTime).setOnClickListener(this);
        view.findViewById(R.id.ivEditDescription).setOnClickListener(this);
        view.findViewById(R.id.bDelete).setOnClickListener(this);

        tvLocationName.setText(name);
        tvLocationAddress.setText(address);
        tvType.setText(type);
        tvCategory.setText(category);
        tvPrice.setText(price);
        tvQuantity.setText(String.valueOf(quantity));
        tvDescription.setText(description);
        setDateTimeText(dateTimeStart);

        if (bidAllowed){
            if (offerTotal == 1){
                tvViewOffers.setText(getResources().getString(R.string.View_1_offer));
                tvViewOffers.setOnClickListener(this);
            } else if (offerTotal > 1){
                tvViewOffers.setText(getResources().getString(R.string.View_all) + " " + offerTotal + " " + getResources().getString(R.string.offers));
                tvViewOffers.setOnClickListener(this);
            } else { // offerTotal == 0
                tvViewOffers.setText(getResources().getString(R.string.No_offers));
            }
        } else {
            llViewOffers.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivEditLocation:
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.ivEditType:
                ADeditType();
                break;

            case R.id.ivEditCategory:
                ADeditCategory();
                break;

            case R.id.ivEditPrice:
                ADeditPrice();
                break;

            case R.id.ivEditQuantity:
                ADeditQuantity();
                break;

            case R.id.ivEditDate:
                ADeditDate();
                break;

            case R.id.ivEditTime:
                ADeditTime();
                break;

            case R.id.ivEditDescription:
                ADeditDescription();
                break;

            case R.id.tvViewOffers:
                Bundle bundle = new Bundle();
                bundle.putString("lid", lid);
                ViewOffers viewOffers = new ViewOffers();
                viewOffers.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.right_out, R.animator.right_in, R.animator.right_out);
                fragmentTransaction.replace(R.id.drawer_layout, viewOffers);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.bDelete:
                ADdeleteSpot();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("name", place.getName().toString());
                    jsonObject.put("address", place.getAddress().toString());
                    jsonObject.put("latitude", String.valueOf(place.getLatLng().latitude));
                    jsonObject.put("longitude", String.valueOf(place.getLatLng().longitude));
                } catch (JSONException e){e.printStackTrace();}
                updateField("location", null, jsonObject);
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
        MenuItem filterMaps = menu.findItem(R.id.filterMaps);
        filterMaps.setVisible(false);
        MenuItem filterYourSpots = menu.findItem(R.id.filterYourSpots);
        filterYourSpots.setVisible(false);
    }

    @Override
    public void onDestroy() {
        if (snackbar != null){
            snackbar.dismiss();
        }
        super.onDestroy();
    }

    private void ADeditType(){
        final CharSequence[] items = {getResources().getString(R.string.Sell), getResources().getString(R.string.Request)};
        final int i;
        final int[] newI = new int[1];
        if (tvType.getText().toString().equals("Sell")){
            i = 0;
        } else {
            i = 1;
        }
        newI[0] = i;
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.Edit_Type);
        alertDialogBuilder.setSingleChoiceItems(items, i, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newI[0] = which;
            }
        });
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str;
                if (newI[0] == 0){
                    str = "Sell";
                } else {
                    str = "Request";
                }
                updateField("type", str, null);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADeditCategory(){
        final CharSequence[] items = {getResources().getString(R.string.Regular), getResources().getString(R.string.Line), getResources().getString(R.string.Parking)};
        final int i;
        final int[] newI = new int[1];
        if (tvCategory.getText().toString().equals(getResources().getString(R.string.Regular))){
            i = 0;
        } else if (tvCategory.getText().toString().equals(getResources().getString(R.string.Line))){
            i = 1;
        } else{
            i = 2;
        }
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.Edit_Type);
        alertDialogBuilder.setSingleChoiceItems(items, i, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newI[0] = which;
            }
        });
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str;
                if (newI[0] == 0){
                    str = "Regular";
                } else if (newI[0] == 1){
                    str = "Line";
                } else {
                    str = "Parking";
                }
                updateField("category", str, null);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADeditPrice(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.edit_spot_price_alertdialog, null);

        final EditText etPrice = alertLayout.findViewById(R.id.etPrice);
        etPrice.setText(tvPrice.getText().toString());
        etPrice.setSelection(etPrice.getText().length());
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Edit_Price);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateField("price", etPrice.getText().toString(), null);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADeditQuantity(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                updateField("quantity", String.valueOf(npQuantity.getValue()), null);
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADeditDate(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setDate(year, month, dayOfMonth);
                Calendar cDate = Calendar.getInstance();
                cDate.set(Calendar.YEAR, year);
                cDate.set(Calendar.MONTH, month);
                cDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvDate.setText(epochToDateString(cDate.getTimeInMillis()));
            }
        }, year, month , day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void ADeditTime(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setTime(hourOfDay, minute);
                Calendar calendarTime = Calendar.getInstance();
                calendarTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarTime.set(Calendar.MINUTE, minute);
                tvTime.setText(epochToTimeString(calendarTime.getTimeInMillis()));
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void ADeditDescription(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.edit_spot_description_alertdialog, null);

        final EditText etDescription = alertLayout.findViewById(R.id.etDescription);
        etDescription.setText(tvDescription.getText().toString());
        etDescription.setSelection(tvDescription.getText().length());
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Edit_Description);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateField("description", etDescription.getText().toString(), null);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADdeleteSpot(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getResources().getString(R.string.Delete) + " " + getResources().getString(R.string.Spot));
        alertDialogBuilder.setMessage(R.string.Are_you_sure_you_want_to_delete_this_spot);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                deleteSpot();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteSpot(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, getString(R.string.URL) + "/location/delete/" + lid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        progressBar.setVisibility(View.GONE);
                        getActivity().getFragmentManager().popBackStack();
                        Toast.makeText(getActivity(), R.string.Spot_successfully_deleted, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonObjectRequest);
    }

    private void updateField(final String field, final String str, final JSONObject location) {
        progressBar.setVisibility(View.VISIBLE);
        JSONObject jObject = new JSONObject();
        try {
            if (location == null){
                jObject.put(field, str);
            } else {
                jObject = location;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, getString(R.string.URL) + "/location/update/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        switch (field){
                            case "type":
                                setSnackbar(getResources().getString(R.string.Type) + " " + getResources().getString(R.string.updated));
                                tvType.setText(str);
                                break;
                            case "category":
                                setSnackbar(getResources().getString(R.string.Category) + " " + getResources().getString(R.string.updated));
                                tvCategory.setText(str);
                                break;
                            case "price":
                                setSnackbar(getResources().getString(R.string.Price) + " " + getResources().getString(R.string.updated));
                                tvPrice.setText(str);
                                break;
                            case "quantity":
                                setSnackbar(getResources().getString(R.string.Quantity) + " " + getResources().getString(R.string.updated));
                                quantity = Integer.valueOf(str);
                                tvQuantity.setText(String.valueOf(quantity));
                                break;
                            case "dateTimeStart":
                                setSnackbar(getActivity().getString(R.string.Date_and_Time) + " " + getActivity().getString(R.string.updated));

                                break;
                            case "description":
                                setSnackbar(getActivity().getString(R.string.Description) + " " + getResources().getString(R.string.updated));
                                tvDescription.setText(str);
                                break;
                            case "bidAllowed":
                                setSnackbar(getActivity().getString(R.string.Allow_offers) + " " + getResources().getString(R.string.updated));
                                break;
                            case "location":
                                setSnackbar(getActivity().getString(R.string.Location) + " " + getResources().getString(R.string.updated));
                                toolbar.setTitle(location.getString("name"));
                                tvLocationName.setText(location.getString("name"));
                                tvLocationAddress.setText(location.getString("address"));
                                break;
                        }
                    }
                } catch(JSONException e){
                    setToastServerError();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                setToastServerError();
            }
        }
        );
        queue.add(jsonObjectRequest);
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

    private void setDateTimeText(long epochSeconds) {
        Date updatedate = new Date(epochSeconds);
        SimpleDateFormat formatReg = new SimpleDateFormat("EEE, d MMM yyyy 'at' h:mm a", Locale.getDefault());
        String[] s = formatReg.format(updatedate).split("at");
        tvDate.setText(s[0]);
        tvTime.setText(s[1]);

        SimpleDateFormat format = new SimpleDateFormat("EEE, d MM yyyy 'at' h:mm a", Locale.getDefault());

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

    private void setSnackbar(String snackbarText) {
        snackbar = Snackbar.make(getActivity().findViewById(R.id.edit_spot), snackbarText, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void setToastServerError(){
        if (isAdded()){
            Toast.makeText(getActivity(), getResources().getString(R.string.Server_error), Toast.LENGTH_SHORT).show();
        }
    }
}
