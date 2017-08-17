package almanza1112.spottrade;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by almanza1112 on 8/16/17.
 */

public class EditSpot extends Fragment implements View.OnClickListener{

    private ProgressBar progressBar;
    private TextView tvLocationName, tvLocationAddress, tvType, tvPrice, tvDescription;
    private String lid, type;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_spot, container, false);

        lid = getArguments().getString("lid");
        String name = getArguments().getString("locationName");
        String address = getArguments().getString("locationAddress");
        type = getArguments().getString("type");
        String price = getArguments().getString("price");
        String description = getArguments().getString("description");
        boolean bidAllowed = getArguments().getBoolean("bidAllowed");

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.Edit_Spot);

        tvLocationName = (TextView) view.findViewById(R.id.tvLocationName);
        tvLocationAddress = (TextView) view.findViewById(R.id.tvLocationAddress);
        tvType = (TextView) view.findViewById(R.id.tvType);
        tvPrice = (TextView) view.findViewById(R.id.tvPrice);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);

        final ImageView ivEditLocation = (ImageView) view.findViewById(R.id.ivEditLocation);
        ivEditLocation.setOnClickListener(this);
        final ImageView ivEditType = (ImageView) view.findViewById(R.id.ivEditType);
        ivEditType.setOnClickListener(this);
        final ImageView ivEditPrice = (ImageView) view.findViewById(R.id.ivEditPrice);
        ivEditPrice.setOnClickListener(this);
        final ImageView ivEditDescription = (ImageView) view.findViewById(R.id.ivEditDescription);
        ivEditDescription.setOnClickListener(this);

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
                break;

            case R.id.ivEditType:
                ADupdateType();
                break;

            case R.id.ivEditPrice:

                break;
            case R.id.ivEditDescription:

                break;
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

    private void ADupdateType(){
        final CharSequence[] items = {getResources().getString(R.string.Selling), getResources().getString(R.string.Requesting)};
        boolean[] selected = {false, false};
        if (type.equals("Selling")){
            selected[0] = true;
        }
        else {
            selected[1] = true;
        }
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.Edit_Type);
        alertDialogBuilder.setMultiChoiceItems(items, selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                //Log.e("")
            }
        });
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //updateField("type", );
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

    private void ADupdatePrice(String field){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.personal_activity_update_field_alertdialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void ADupdateDescription(String field){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.personal_activity_update_field_alertdialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void updateField(final String field, final String str) {
        final JSONObject jObject = new JSONObject();
        try {
            jObject.put(field, str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        HttpConnection httpConnection = new HttpConnection();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/location/update/" + lid, jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("updateField", response + "");
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
