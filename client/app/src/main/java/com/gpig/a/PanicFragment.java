package com.gpig.a;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class PanicFragment extends Fragment implements View.OnClickListener {

    public PanicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_panic, container, false);

        Button b = v.findViewById(R.id.missed_connection);
        b.setOnClickListener(this);
        b = v.findViewById(R.id.package_lost);
        b.setOnClickListener(this);
        b = v.findViewById(R.id.tampered_with);
        b.setOnClickListener(this);
        b = v.findViewById(R.id.urgent_assistance);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //TODO tell server user is in distress
            case R.id.missed_connection:
                break;
            case R.id.package_lost:
                break;
            case R.id.tampered_with:
                break;
            case R.id.urgent_assistance:
                break;
        }
    }
}
