package almanza1112.spottrade;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class BottomSheetFilterMapCategory extends BottomSheetDialogFragment implements View.OnClickListener {

    private String type;
    private TextView tvCategoryTitle;
    private TextInputEditText tietOther;

    public BottomSheetFilterMapCategory(){
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_filter_map_category, container, false);

        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        view.findViewById(R.id.tvAll).setOnClickListener(this);
        view.findViewById(R.id.tvParking).setOnClickListener(this);
        view.findViewById(R.id.tvLine).setOnClickListener(this);
        final Button bSearch = view.findViewById(R.id.bSearch);
        bSearch.setOnClickListener(this);
        tietOther = view.findViewById(R.id.tietOther);
        tietOther.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0){
                    bSearch.setTextColor(getResources().getColor(R.color.colorAccent));
                    bSearch.setClickable(true);
                } else {
                    bSearch.setTextColor(getResources().getColor(R.color.grey600));
                    bSearch.setClickable(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setTitle(type);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        type = getArguments().getString("type");
        try {
            filterMapCategorySelectedListener = (FilterMapCategorySelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnItemClickedListener");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvAll:
                filterMapCategorySelectedListener.onFilterMapCategorySelected(type, "All");
                break;

            case R.id.tvParking:
                filterMapCategorySelectedListener.onFilterMapCategorySelected(type, "Parking");
                break;

            case R.id.tvLine:
                filterMapCategorySelectedListener.onFilterMapCategorySelected(type, "Line");
                break;

            case R.id.bSearch:
                filterMapCategorySelectedListener.onFilterMapCategorySelected(type, tietOther.getText().toString());
                break;
        }
    }

    private void setTitle(String type){
        switch (type){
            case "All":
                tvCategoryTitle.setText(R.string.FILTER_ALL_SPOTS);
                break;

            case "Request":
                tvCategoryTitle.setText(R.string.FILTER_REQUESTING_SPOTS);
                break;

            case "Sell":
                tvCategoryTitle.setText(R.string.FILTER_SELLING_SPOTS);
                break;
        }
    }

    FilterMapCategorySelectedListener filterMapCategorySelectedListener = filterMapCategorySelectedCallback;

    public interface FilterMapCategorySelectedListener{
        void onFilterMapCategorySelected(String type, String category);
    }

    public static FilterMapCategorySelectedListener filterMapCategorySelectedCallback = new FilterMapCategorySelectedListener() {
        @Override
        public void onFilterMapCategorySelected(String type, String category) {

        }
    };
}
