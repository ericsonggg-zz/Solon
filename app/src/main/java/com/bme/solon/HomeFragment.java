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

    private ColorStateList themeTextColor;
    private TextView instanceResolution;
    private CustomAnalogClock instanceAnalogClock; //https://github.com/rosenpin/custom-analog-clock-view
    private TextView instanceDigitalClock;
    private TextView instanceDevice;
    private TextView instanceDate;

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

        //Find views
        instanceResolution = fragmentView.findViewById(R.id.home_instance_resolution);
        instanceAnalogClock = fragmentView.findViewById(R.id.home_instance_analog_clock);
        instanceAnalogClock.setScale(0.75f);
        instanceDigitalClock = fragmentView.findViewById(R.id.home_instance_digital_clock);
        instanceDevice = fragmentView.findViewById(R.id.home_instance_device);
        instanceDate = fragmentView.findViewById(R.id.home_instance_date);

        //Store default text color from theme
        themeTextColor = instanceResolution.getTextColors();

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        //Get data from database
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        Instance instance = db.getLatestInstance();
        if (instance != null) {
            Device device = db.getDevice(instance.getDeviceId());

            //Update instance views
            if (instance.getResolution() == Instance.RESOLVED) {
                instanceResolution.setText(R.string.home_instance_resolved);
                instanceResolution.setTextColor(themeTextColor);
            } else {
                instanceResolution.setText(R.string.home_instance_unresolved);
                instanceResolution.setTextColor(Color.RED);
            }

            instanceDevice.setText(device.getAppName());
            //instanceAnalogClock.set
            instanceDigitalClock.setText(instance.getDateTime().format(Instance.TIME_FORMAT));
            instanceDate.setText(instance.getDateTime().format(Instance.DATE_FORMAT));
        }
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
    }
}
