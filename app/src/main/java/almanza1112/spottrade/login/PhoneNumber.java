package almanza1112.spottrade.login;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import almanza1112.spottrade.R;
import almanza1112.spottrade.nonActivity.RegularExpression;

/**
 * Created by Almanza on 6/5/2018.
 */

public class PhoneNumber extends Fragment implements
        View.OnClickListener,
        CountryCodes.CountrySelectedListener{

    private TextView tvCountryCode;
    private ImageView ivCountryFlag;
    private TextInputLayout tilPhoneNumber;
    private TextInputEditText tietPhoneNumber;
    //private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneVerificationCallback;

    private Pattern phoneNumberPattern = Pattern.compile(RegularExpression.PHONE_NUMBER_PATTERN);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.phone_number, container, false);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getResources().getString(R.string.Phone_Number));

        view.findViewById(R.id.llCountryFlagCode).setOnClickListener(this);
        tvCountryCode = view.findViewById(R.id.tvCountryCode);
        ivCountryFlag = view.findViewById(R.id.ivCountryFlag);
        tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber);
        tietPhoneNumber = view.findViewById(R.id.tietPhoneNumber);
        view.findViewById(R.id.fabNext).setOnClickListener(this);
        getCountryZipCode();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.llCountryFlagCode:
                CountryCodes countryCodes = new CountryCodes();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.phone_number, countryCodes);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.fabNext:
                sendConfirmationCode();
                break;
        }
    }

    @Override
    public void onCountrySelected(String countryCode, String countryID) {
        Log.e("onCountrySelected", countryCode + " " + countryID);
        ivCountryFlag.setImageResource(getActivity().getResources().getIdentifier("drawable/" + countryID, null, getActivity().getPackageName()));
        tvCountryCode.setText("+" + countryCode);
    }

    private void getCountryZipCode() {
        String CountryID;
        String CountryZipCode;

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (String aRl : rl) {
            String[] g = aRl.split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                Log.e("CountryZipCode", CountryZipCode);
                Log.e("CountryID", CountryID);

                // Handle the "-" change to underscore for gb countries and do(Dominican Republic) which is now dr
                String pngName = CountryID.toLowerCase();
                ivCountryFlag.setImageResource(getActivity().getResources().getIdentifier("drawable/" + pngName, null, getActivity().getPackageName()));
                tvCountryCode.setText("+" + CountryZipCode);

                break;
            }
        }
    }

    private void sendConfirmationCode(){
        String phoneNumber = tietPhoneNumber.getText().toString();
        if (phoneNumber.isEmpty()){
            tilPhoneNumber.setError(getString(R.string.Please_enter_your_mobile_number));
        } else {
            tilPhoneNumber.setErrorEnabled(false);
            if (validatePhoneNumber(phoneNumber)){
                ((LoginActivity)getActivity()).setPD();
                String newPhoneNumber = tvCountryCode.getText().toString() + phoneNumber;
                ((LoginActivity)getActivity()).setPhoneNumber(newPhoneNumber);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        newPhoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        getActivity(),
                        ((LoginActivity)getActivity()).phoneVerificationCallback);
            }
        }
    }

    private boolean validatePhoneNumber(String phoneNumber){
        Matcher matcher = phoneNumberPattern.matcher(phoneNumber);
        if (!matcher.matches()){
            tilPhoneNumber.setError(getString(R.string.Invalid_number));
            return false;
        }
        else {
            tilPhoneNumber.setErrorEnabled(false);
            return true;
        }
    }
}
