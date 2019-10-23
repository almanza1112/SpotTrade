package almanza1112.spottrade;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;


public class BottomSheetFilterMap extends BottomSheetDialogFragment implements AppCompatRadioButton.OnCheckedChangeListener {

    private AppCompatRadioButton rbAllType, rbSell, rbRequest, rbAllCateoory, rbParking, rbLine, rbOtherCategory;
    private String type, category;
    private EditText etOtherCategory;

    public BottomSheetFilterMap() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maps_activity_filter_map_bottomsheet, container, false);

        rbAllType = view.findViewById(R.id.rbAllType);
        rbSell = view.findViewById(R.id.rbSell);
        rbRequest = view.findViewById(R.id.rbRequest);
        rbAllCateoory = view.findViewById(R.id.rbAllCategory);
        rbParking = view.findViewById(R.id.rbParking);
        rbLine = view.findViewById(R.id.rbLine);
        rbOtherCategory = view.findViewById(R.id.rbOtherCategory);
        etOtherCategory = view.findViewById(R.id.etOtherCategory);
        final SwitchCompat sOffersAllowed = view.findViewById(R.id.sOffersAllowed);

        rbAllType.setOnCheckedChangeListener(this);
        rbSell.setOnCheckedChangeListener(this);
        rbRequest.setOnCheckedChangeListener(this);
        rbAllCateoory.setOnCheckedChangeListener(this);
        rbParking.setOnCheckedChangeListener(this);
        rbLine.setOnCheckedChangeListener(this);
        rbOtherCategory.setOnCheckedChangeListener(this);

        view.findViewById(R.id.mbDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateOtherCategory()){
                    filterMapListener.onFilterMap(type, category, sOffersAllowed.isChecked());
                }
            }
        });

        type = getArguments().getString("type");
        category = getArguments().getString("category");

        switch (type){
            case "All":
                rbAllType.setChecked(true);
                break;

            case "Sell":
                rbSell.setChecked(true);
                break;

            case "Request":
                rbRequest.setChecked(true);
                break;
        }

        switch (category){
            case "All":
                rbAllCateoory.setChecked(true);
                break;

            case "Parking":
                rbParking.setChecked(true);
                break;

            case "Line":
                rbLine.setChecked(true);
                break;

                default:
                    rbOtherCategory.setChecked(true);
                    etOtherCategory.setText(category);
                    break;
        }
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Following code expands BottomSheetDialogFragment, layout was too big so it needed to be expanded
        BottomSheetDialog bottomSheetDialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet =  d .findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
        return bottomSheetDialog;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (buttonView.getId() == rbAllType.getId()) {
                type = "All";
            }
            if (buttonView.getId() == rbSell.getId()){
                type = "Sell";
            }
            if (buttonView.getId() == rbRequest.getId()){
                type = "Request";
            }
            if (buttonView.getId() == rbAllCateoory.getId()){
                category = "All";
                rbAllCateoory.setChecked(true);
                rbParking.setChecked(false);
                rbLine.setChecked(false);
                rbOtherCategory.setChecked(false);
            }
            if (buttonView.getId() == rbParking.getId()){
                category = "Parking";
                rbAllCateoory.setChecked(false);
                rbParking.setChecked(true);
                rbLine.setChecked(false);
                rbOtherCategory.setChecked(false);
            }
            if (buttonView.getId() == rbLine.getId()){
                category = "Line";
                rbAllCateoory.setChecked(false);
                rbParking.setChecked(false);
                rbLine.setChecked(true);
                rbOtherCategory.setChecked(false);
            }
            if (buttonView.getId() == rbOtherCategory.getId()){
                category = "other";
                rbAllCateoory.setChecked(false);
                rbParking.setChecked(false);
                rbLine.setChecked(false);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            filterMapListener = (FilterMapListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onFilterMap");
        }
    }

    FilterMapListener filterMapListener = filterMapTypeSelectedCallback;

    public interface FilterMapListener {
        void onFilterMap(String type, String category, boolean offersAllowed);
    }

    public static FilterMapListener filterMapTypeSelectedCallback = new FilterMapListener() {
        @Override
        public void onFilterMap(String type, String category, boolean offersAllowed) {

        }
    };

    private boolean validateOtherCategory(){
        if (rbOtherCategory.isChecked()){
            if (etOtherCategory.getText().toString().isEmpty()){
                etOtherCategory.setError(getResources().getString(R.string.Field_cant_be_empty));
                return false;
            } else {
                category = etOtherCategory.getText().toString();
                return true;
            }
        } else {
            return true;
        }
    }
}
