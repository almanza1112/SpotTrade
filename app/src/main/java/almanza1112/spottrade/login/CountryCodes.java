package almanza1112.spottrade.login;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import almanza1112.spottrade.R;

/**
 * Created by Almanza on 6/6/2018.
 */

public class CountryCodes extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.country_codes, container, false);

        return view;
    }
}
