package almanza1112.spottrade.login;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import almanza1112.spottrade.R;

/**
 * Created by Almanza on 6/7/2018.
 */

public class ConfirmationCode extends Fragment implements View.OnClickListener{

    private String verificationID;
    private EditText etConfirmationCode1, etConfirmationCode2, etConfirmationCode3,
            etConfirmationCode4, etConfirmationCode5, etConfirmationCode6;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditText text = (EditText)getActivity().getCurrentFocus();

            if (text != null && text.length() > 0)
            {
                View next = text.focusSearch(View.FOCUS_RIGHT); // or FOCUS_FORWARD
                if (next != null)
                    next.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.confirmation_code, container, false);

        verificationID = getArguments().getString("verificationID");

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getResources().getString(R.string.Confirmation_Code));

        etConfirmationCode1 = view.findViewById(R.id.etConfirmationCode1);
        etConfirmationCode2 = view.findViewById(R.id.etConfirmationCode2);
        etConfirmationCode3 = view.findViewById(R.id.etConfirmationCode3);
        etConfirmationCode4 = view.findViewById(R.id.etConfirmationCode4);
        etConfirmationCode5 = view.findViewById(R.id.etConfirmationCode5);
        etConfirmationCode6 = view.findViewById(R.id.etConfirmationCode6);

        etConfirmationCode1.addTextChangedListener(textWatcher);
        etConfirmationCode2.addTextChangedListener(textWatcher);
        etConfirmationCode3.addTextChangedListener(textWatcher);
        etConfirmationCode4.addTextChangedListener(textWatcher);
        etConfirmationCode5.addTextChangedListener(textWatcher);
        etConfirmationCode6.addTextChangedListener(textWatcher);

        view.findViewById(R.id.fabNext).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fabNext:
                String confirmationCode =   etConfirmationCode1.getText().toString() +
                                            etConfirmationCode2.getText().toString() +
                                            etConfirmationCode3.getText().toString() +
                                            etConfirmationCode4.getText().toString() +
                                            etConfirmationCode5.getText().toString() +
                                            etConfirmationCode6.getText().toString();
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, confirmationCode);
                ((LoginActivity)getActivity()).signInWithPhoneAuthCredential(phoneAuthCredential);
                break;

        }
    }
}
