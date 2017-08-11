package almanza1112.spottrade.account.personal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.HttpConnection;
import almanza1112.spottrade.nonActivity.RegularExpression;
import almanza1112.spottrade.nonActivity.SharedPref;

/**
 * Created by almanza1112 on 7/29/17.
 */

public class Personal extends Fragment implements View.OnClickListener{
    private TextView tvFistName, tvLastName, tvEmail;
    private ProgressBar progressBar;
    private Pattern pattern = Pattern.compile(RegularExpression.EMAIL_PATTERN);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Personal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            RelativeLayout.LayoutParams tb = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
            tb.setMargins(0, getStatusBarHeight(), 0, 0);
        }

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvFistName = (TextView) view.findViewById(R.id.tvFirstName);
        tvFistName.setText(SharedPref.getFirstName(getActivity()));
        final ImageView ivEditFirstName = (ImageView) view.findViewById(R.id.ivEditFirstName);
        ivEditFirstName.setOnClickListener(this);
        tvLastName = (TextView) view.findViewById(R.id.tvLastName);
        tvLastName.setText(SharedPref.getLastName(getActivity()));
        final ImageView ivEditLastName = (ImageView) view.findViewById(R.id.ivEditLastName);
        ivEditLastName.setOnClickListener(this);

        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvEmail.setText(SharedPref.getEmail(getActivity()));
        final ImageView ivEditEmail = (ImageView) view.findViewById(R.id.ivEditEmail);
        ivEditEmail.setOnClickListener(this);
        final TextView tvPassword = (TextView) view.findViewById(R.id.tvPassword);
        tvPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType. TYPE_TEXT_VARIATION_PASSWORD);
        tvPassword.setText(SharedPref.getPassword(getActivity()));
        final ImageView ivEditPassword = (ImageView) view.findViewById(R.id.ivEditPassword);
        ivEditPassword.setOnClickListener(this);

        AppCompatActivity actionBar = (AppCompatActivity) getActivity();
        actionBar.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) actionBar.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivEditFirstName:
                ADupdateField("firstName");
                break;

            case R.id.ivEditLastName:
                ADupdateField("lastName");
                break;

            case R.id.ivEditEmail:
                ADupdateField("email");
                break;

            case R.id.ivEditPassword:
                ChangePassword changePassword = new ChangePassword();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.personal_activity, changePassword);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
        }
    }

    private void ADupdateField(final String field){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.personal_activity_update_field_alertdialog, null);

        final ImageView ivIcon = (ImageView) alertLayout.findViewById(R.id.ivIcon);
        final TextInputLayout tilUpdate = (TextInputLayout) alertLayout.findViewById(R.id.tilUpdate);
        final TextInputEditText tietUpdate = (TextInputEditText) alertLayout.findViewById(R.id.tietUpdate);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(alertLayout);
        String title = getResources().getString(R.string.Update);
        switch (field){
            case "firstName":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_account_circle_black_24dp));
                title+= " " + getResources().getString(R.string.First_Name);
                tietUpdate.setText(SharedPref.getFirstName(getActivity()));
                tietUpdate.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                break;

            case "lastName":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_account_circle_black_24dp));
                title+= " " + getResources().getString(R.string.Last_Name);
                tietUpdate.setText(SharedPref.getLastName(getActivity()));
                tietUpdate.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                break;

            case "email":
                ivIcon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_email_grey600_24dp));
                title+= " " + getResources().getString(R.string.Email);
                tietUpdate.setText(SharedPref.getEmail(getActivity()));
                tietUpdate.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
        }
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton(R.string.Update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        alertDialogBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //Do stuff, possibly set wantToCloseDialog to true then...
                String str = tietUpdate.getText().toString();
                switch (field){
                    case "firstName":
                        if (str.length() > 0){
                            wantToCloseDialog = true;
                            updateField(field, str);
                        }
                        else {
                            tilUpdate.setError(getResources().getString(R.string.Field_cant_be_empty));
                        }
                        break;
                    case "lastName":
                        if (str.length() > 0){
                            wantToCloseDialog = true;
                            updateField(field, str);
                        }
                        else {
                            tilUpdate.setError(getResources().getString(R.string.Field_cant_be_empty));
                        }
                        break;
                    case "email":
                        if (validateEmail(str)){
                            wantToCloseDialog = true;
                            updateField(field, str);
                        }
                        else {
                            tilUpdate.setError(getResources().getString(R.string.Invalid_email_format));
                        }
                        break;
                }
                if(wantToCloseDialog) {
                    progressBar.setVisibility(View.VISIBLE);
                    alertDialog.dismiss();
                }
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, httpConnection.htppConnectionURL() + "/user/update/" + SharedPref.getID(getActivity()), jObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    progressBar.setVisibility(View.INVISIBLE);
                    if (response.getString("status").equals("success")){
                        switch (field){
                            case "firstName":
                                SharedPref.clearFirstName(getActivity());
                                SharedPref.setFirstName(getActivity(), str);
                                Toast.makeText(getActivity(), getResources().getString(R.string.First_Name) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                tvFistName.setText(str);
                                break;
                            case "lastName":
                                SharedPref.clearLastName(getActivity());
                                SharedPref.setLastName(getActivity(), str);
                                Toast.makeText(getActivity(), getResources().getString(R.string.Last_Name) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                tvLastName.setText(str);
                                break;
                            case "email":
                                SharedPref.clearEmail(getActivity());
                                SharedPref.setEmail(getActivity(), str);
                                Toast.makeText(getActivity(), getResources().getString(R.string.Email) + " " + getResources().getString(R.string.updated), Toast.LENGTH_SHORT).show();
                                tvEmail.setText(str);
                                break;
                        }
                    }
                    else {
                        Toast.makeText(getContext(), getResources().getString(R.string.Error_service_unavailable), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
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

    private boolean validateEmail(String email){
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
