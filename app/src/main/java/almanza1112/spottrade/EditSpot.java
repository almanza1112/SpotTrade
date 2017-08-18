package almanza1112.spottrade;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.search.SearchActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Created by almanza1112 on 8/16/17.
 */

public class EditSpot extends Fragment implements View.OnClickListener{

    private ProgressBar progressBar;
    private String lid;
    private int LOCATION_CODE = 0;
    TextView tvLocationName, tvLocationAddress, tvType, tvPrice, tvDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_spot, container, false);

        lid = getArguments().getString("lid");
        String name = getArguments().getString("locationName");
        String address = getArguments().getString("locationAddress");
        String type = getArguments().getString("type");
        String price = getArguments().getString("price");
        String description = getArguments().getString("description");
        boolean bidAllowed = getArguments().getBoolean("bidAllowed");

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Edit_Spot);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
        tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
        tvType = (TextView) view.findViewById(R.id.tvType);
        tvPrice = (TextView) view.findViewById(R.id.tvPrice);
        CheckBox cbBids = (CheckBox) view.findViewById(R.id.cbBids);
        cbBids.setChecked(bidAllowed);
        cbBids.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                progressBar.setVisibility(View.GONE);
                updateField("bidAllowed", String.valueOf(isChecked));
            }
        });
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);

        final ImageView ivEditLocation = (ImageView) view.findViewById(R.id.ivEditLocation);
        ivEditLocation.setOnClickListener(this);
        final ImageView ivEditType = (ImageView) view.findViewById(R.id.ivEditType);
        ivEditType.setOnClickListener(this);
        final ImageView ivEditPrice = (ImageView) view.findViewById(R.id.ivEditPrice);
        ivEditPrice.setOnClickListener(this);
        final ImageView ivEditDescription = (ImageView) view.findViewById(R.id.ivEditDescription);
        ivEditDescription.setOnClickListener(this);
        final Button bDelete = (Button) view.findViewById(R.id.bDelete);
        bDelete.setOnClickListener(this);

        tvLocationName.setText(name);
        tvLocationAddress.setText(address);
        tvType.setText(type);
        tvPrice.setText(price);
        tvDescription.setText(description);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivEditLocation:
                startActivityForResult(new Intent(getActivity(), SearchActivity.class), LOCATION_CODE);
                break;

            case R.id.ivEditType:
                ADeditType();
                break;

            case R.id.ivEditPrice:
                ADeditPrice();
                break;

            case R.id.ivEditDescription:
                ADeditDescription();
                break;

            case R.id.bDelete:
                ADdeleteSpot();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_CODE){
            if (resultCode == RESULT_OK){
                progressBar.setVisibility(View.VISIBLE);
                updateField("name", data.getStringExtra("locationName"));
                updateField("address", data.getStringExtra("locationAddress"));
                updateField("latitude", data.getStringExtra("latitude"));
                updateField("longitude", data.getStringExtra("longitude"));
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
        MenuItem item=menu.findItem(R.id.search);
        item.setVisible(false);
    }

    private void ADeditType(){
        final CharSequence[] items = {getResources().getString(R.string.Selling), getResources().getString(R.string.Requesting)};
        final int i;
        final int[] newI = new int[1];
        if (tvType.getText().toString().equals("Selling")){
            i = 0;
        }
        else {
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
                progressBar.setVisibility(View.VISIBLE);
                String str;
                if (newI[0] == 0){
                    str = "Selling";
                }
                else {
                    str = "Requesting";
                }
                updateField("type", str);
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

        final EditText etPrice = (EditText) alertLayout.findViewById(R.id.etPrice);
        etPrice.setText(tvPrice.getText().toString());
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Edit_Price);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                updateField("price", etPrice.getText().toString());
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

    private void ADeditDescription(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.edit_spot_description_alertdialog, null);

        final EditText etDescription = (EditText) alertLayout.findViewById(R.id.etDescription);
        etDescription.setText(tvDescription.getText().toString());
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setTitle(R.string.Edit_Description);
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                updateField("description", etDescription.getText().toString());
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

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, httpConnection.htppConnectionURL() + "/location/delete/" + lid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        progressBar.setVisibility(View.GONE);
                        getActivity().getFragmentManager().popBackStack();
                        Toast.makeText(getActivity(), R.string.Spot_successfully_deleted, Toast.LENGTH_SHORT).show();
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
                });
        queue.add(jsonObjectRequest);
    }

    private void updateField(final String field, final String str) {
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put(field, str);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/update/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getString("status").equals("success")){
                        switch (field){
                            case "type":
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getResources().getString(R.string.Type) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                tvType.setText(str);
                                break;
                            case "price":
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getResources().getString(R.string.Price) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                tvPrice.setText(str);
                                break;
                            case "description":
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getActivity().getString(R.string.Description) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                tvDescription.setText(str);
                                break;
                            case "bidAllowed":
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getActivity().getString(R.string.Allow_bids) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                break;
                            case "name":
                                tvLocationName.setText(str);
                                break;
                            case "address":
                                tvLocationAddress.setText(str);
                                break;
                            case "longitude":
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getActivity().getString(R.string.Location) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
                catch(JSONException e){
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
}