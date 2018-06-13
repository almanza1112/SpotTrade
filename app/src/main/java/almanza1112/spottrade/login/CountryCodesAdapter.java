package almanza1112.spottrade.login;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import almanza1112.spottrade.R;

/**
 * Created by Almanza on 6/11/2018.
 */

class CountryCodesAdapter extends RecyclerView.Adapter<CountryCodesAdapter.RecyclerViewHolder>{

    private CountryCodes countryCodes;
    private Activity activity;
    private List<String> countryCode, countryID, countryName;
    CountryCodesAdapter(CountryCodes countryCodes, Activity activity, List<String> countryCode, List<String> countryID,
                        List<String> countryName){
        this.countryCodes = countryCodes;
        this.activity = activity;
        this.countryCode = countryCode;
        this.countryID = countryID;
        this.countryName = countryName;
    }

    @Override
    public CountryCodesAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_codes_recyclerview_row, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.ivCountryFlag.setImageResource(activity.getResources().getIdentifier("drawable/" + countryID.get(position), null, activity.getPackageName()));
        holder.tvCountryNameCode.setText(countryName.get(position) + " (+" + countryCode.get(position) + ")");
    }

    @Override
    public int getItemCount() {
        return countryCode.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        LinearLayout llCountry;
        ImageView ivCountryFlag;
        TextView tvCountryNameCode;
        RecyclerViewHolder(View view){
            super(view);
            llCountry = view.findViewById(R.id.llCountry);
            llCountry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("rvLL", "clicked");
                    countryCodes.countrySelected(countryCode.get(getAdapterPosition()), countryID.get(getAdapterPosition()));
                }
            });
            ivCountryFlag = view.findViewById(R.id.ivCountryFlag);
            tvCountryNameCode = view.findViewById(R.id.tvCountryNameCode);
        }
    }
}
