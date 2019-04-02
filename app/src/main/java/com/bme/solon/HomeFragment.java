package com.bme.solon;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bme.solon.bluetooth.BluetoothBroadcast;
import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;
import com.bme.solon.database.Instance;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends MainFragment {
    private static final String TAG = "HomeFragment";

    private RecyclerView instanceList;
    private InstanceListAdapter instanceListAdapter;

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

        //initialize instances RecyclerView
        instanceList = fragmentView.findViewById(R.id.home_instance_list);
        instanceList.setHasFixedSize(true);
        instanceList.setLayoutManager(new LinearLayoutManager(getContext()));
        instanceListAdapter = new InstanceListAdapter();
        instanceList.setAdapter(instanceListAdapter);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        //Populate instanceList
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        List<Instance> instances = db.getAllInstances();
        List<Device> devices = new ArrayList<>();
        for (Instance instance : instances) {
            devices.add(db.getDevice(instance.getDeviceId()));
        }
        instanceListAdapter.addInstances(instances, devices);
        instanceListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());

        switch (intent.getAction()) {
            case BluetoothBroadcast.ACTION_NEW_INSTANCE:
                long id = intent.getLongExtra(BluetoothBroadcast.KEY_INSTANCE_ID, -1);
                if (id != -1) {
                    Instance instance = db.retrieveInstance(id);
                    Device device = db.getDevice(instance.getId());
                    instanceListAdapter.addInstance(instance, device);
                    instanceListAdapter.notifyDataSetChanged();
                }
                break;
            case BluetoothBroadcast.ACTION_INSTANCE_UPDATE:
        }
    }
}
