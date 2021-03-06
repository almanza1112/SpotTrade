package almanza1112.spottrade.login;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by Almanza on 6/6/2018.
 */

public class CountryCodes extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.country_codes, container, false);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getResources().getString(R.string.Country_Codes));

        List<String> countryCode = new ArrayList<>();
        List<String> countryID = new ArrayList<>();
        List<String> countryName = new ArrayList<>();
        String[] cc = this.getResources().getStringArray(R.array.CountryCodes);
        for (String cc1: cc){
            String[] x = cc1.split(",");
            countryCode.add(x[0].trim());
            countryID.add(x[1].trim().toLowerCase());
            countryName.add(x[1].trim());
        }

        RecyclerView rvCountryCodes = view.findViewById(R.id.rvCountryCodes);
        rvCountryCodes.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvCountryCodes.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new CountryCodesAdapter(CountryCodes.this, getActivity(), countryCode, countryID,countryName);
        rvCountryCodes.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            countrySelectedListener = (CountrySelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnItemClickedListener");
        }
    }

    @Override
    public void onDetach() {
        countrySelectedListener = null;
        super.onDetach();
    }

    CountrySelectedListener countrySelectedListener = countrySelectedCallback;

    public interface CountrySelectedListener{
        void onCountrySelected(String countryCode, String countryID);
    }

    public static CountrySelectedListener countrySelectedCallback = new CountrySelectedListener() {
        @Override
        public void onCountrySelected(String countryCode, String countryID) {

        }
    };

    public void countrySelected(String countryCode, String countryID){
        getFragmentManager().popBackStack();
        countrySelectedListener.onCountrySelected(countryCode, countryID);
    }
}
