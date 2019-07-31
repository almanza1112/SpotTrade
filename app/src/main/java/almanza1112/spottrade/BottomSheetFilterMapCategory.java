package almanza1112.spottrade;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BottomSheetFilterMapCategory extends BottomSheetDialogFragment implements View.OnClickListener {

    private String type;
    private TextView tvCategoryTitle;

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
