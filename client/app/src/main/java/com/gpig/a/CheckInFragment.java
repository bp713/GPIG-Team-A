package com.gpig.a;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gpig.a.utils.FIDO2Utils;

//TODO: check conditions and update check in display
public class CheckInFragment extends Fragment implements View.OnClickListener {

    private CheckInViewModel mViewModel;
    private final String TAG = "TicketFragment";

    public static CheckInFragment newInstance() {
        return new CheckInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.check_in_fragment, container, false);
        Button b = v.findViewById(R.id.check_in_button);
        b.setOnClickListener(this);
        b = v.findViewById(R.id.register_button);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CheckInViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.check_in_button){
            //TODO check in
        }else if(v.getId() == R.id.register_button){
            Log.i(TAG, "onClick: " + getActivity().toString());
            FIDO2Utils fu = new FIDO2Utils(getActivity());
            fu.register(MainActivity.username);
        }
    }
}
