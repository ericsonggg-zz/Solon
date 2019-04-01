package com.bme.solon;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;
import com.bme.solon.database.Instance;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

public class HomeFragment extends MainFragment {
    private static final String TAG = "HomeFragment";


    /**
     * Required empty constructor
     */
    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        //Get data from database
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        Instance instance = db.getLatestInstance();
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
    }
}
