package almanza1112.spottrade;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BottomSheetFilterMapType extends BottomSheetDialogFragment implements View.OnClickListener {

    public BottomSheetFilterMapType() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_filter_map_type, container, false);

        view.findViewById(R.id.tvAll).setOnClickListener(this);
        view.findViewById(R.id.tvSelling).setOnClickListener(this);
        view.findViewById(R.id.tvRequesting).setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            filterMapTypeSelectedListener = (FilterMapTypeSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnItemClickedListener");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvAll:
                filterMapTypeSelectedListener.onFilterMapTypeSelected("All");
                break;

            case R.id.tvSelling:
                filterMapTypeSelectedListener.onFilterMapTypeSelected("Sell");
                break;

            case R.id.tvRequesting:
                filterMapTypeSelectedListener.onFilterMapTypeSelected("Request");
                break;
        }
    }

    FilterMapTypeSelectedListener filterMapTypeSelectedListener = filterMapTypeSelectedCallback;

    public interface FilterMapTypeSelectedListener{
        void onFilterMapTypeSelected(String type);
    }

    public static FilterMapTypeSelectedListener filterMapTypeSelectedCallback = new FilterMapTypeSelectedListener() {
        @Override
        public void onFilterMapTypeSelected(String type) {

        }
    };
}
