package com.bme.solon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
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

import java.util.List;

public class HomeFragment extends MainFragment {
    private static final String TAG = "HomeFragment";

    private TextView activeNameView;
    private TextView activeStatusView;
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

        //find reminaing views
        activeNameView = fragmentView.findViewById(R.id.home_active_device);
        activeStatusView = fragmentView.findViewById(R.id.home_active_status);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        //Populate instanceList
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        List<Instance> instances = db.getAllInstances();
        List<Device> devices = db.getPairedDevices();
        instanceListAdapter.addInstances(instances, devices);
        instanceListAdapter.notifyDataSetChanged();

        //Set text for remaining UI
        if (isServiceBound) {
            if (!btService.isBluetoothOn()) {
                activeNameView.setText(getText(R.string.status_disconnected_device));
                activeStatusView.setText(getText(R.string.status_bluetooth_off));
            }
            else {
                Device device = btService.getConnectedDevice();
                switch (btService.getGattStatus()) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        activeNameView.setText(getText(R.string.status_disconnected_device));
                        activeStatusView.setText(getText(R.string.status_disconnected));
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        activeNameView.setText(device.getAppName());
                        activeStatusView.setText(getText(R.string.status_connecting));
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        activeNameView.setText(device.getAppName());
                        activeStatusView.setText(getText(R.string.status_connected));
                        break;
                }
            }
        }
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        long id = intent.getLongExtra(BluetoothBroadcast.KEY_INSTANCE_ID, -1);

        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        activeNameView.setText(getText(R.string.status_disconnected_device));
                        activeStatusView.setText(getText(R.string.status_bluetooth_off));
                        break;
                    case BluetoothAdapter.STATE_ON:
                        activeNameView.setText(getText(R.string.status_disconnected_device));
                        activeStatusView.setText(getText(R.string.status_disconnected));
                        break;
                }
                break;
            case BluetoothBroadcast.ACTION_CONNECTING:
                activeNameView.setText(intent.getStringExtra(BluetoothBroadcast.KEY_DEVICE_NAME));
                activeStatusView.setText(getText(R.string.status_connecting));
                break;
            case BluetoothBroadcast.ACTION_CONNECTED:
            case BluetoothBroadcast.ACTION_CONNECTED_UPDATE:
                activeStatusView.setText(getText(R.string.status_connected));
                break;
            case BluetoothBroadcast.ACTION_DISCONNECTED:
                activeNameView.setText(getText(R.string.status_disconnected_device));
                activeStatusView.setText(getText(R.string.status_disconnected));
                break;
            case BluetoothBroadcast.ACTION_NEW_INSTANCE:
                if (id != -1) {
                    Instance instance = db.retrieveInstance(id);
                    Device device = db.getDevice(instance.getDeviceId());
                    instanceListAdapter.addInstance(instance, device);
                    instanceListAdapter.notifyDataSetChanged();
                }
                break;
            case BluetoothBroadcast.ACTION_INSTANCE_UPDATE:
                if (id != -1) {
                    Instance instance = db.retrieveInstance(id);
                    Device device = db.getDevice(instance.getDeviceId());
                    instanceListAdapter.updateInstance(instance, device);
                    instanceListAdapter.notifyDataSetChanged();
                }
                break;
            case BluetoothBroadcast.ACTION_SERVICE_BOUND:
            case BluetoothBroadcast.ACTION_SERVICE_DISCONNECTED:
                if (isServiceBound) {
                    if (!btService.isBluetoothOn()) {
                        activeNameView.setText(getText(R.string.status_disconnected_device));
                        activeStatusView.setText(getText(R.string.status_bluetooth_off));
                    }
                    else {
                        Device device = btService.getConnectedDevice();
                        switch (btService.getGattStatus()) {
                            case BluetoothProfile.STATE_DISCONNECTED:
                                activeNameView.setText(getText(R.string.status_disconnected_device));
                                activeStatusView.setText(getText(R.string.status_disconnected));
                                break;
                            case BluetoothProfile.STATE_CONNECTING:
                                activeNameView.setText(device.getAppName());
                                activeStatusView.setText(getText(R.string.status_connecting));
                                break;
                            case BluetoothProfile.STATE_CONNECTED:
                                activeNameView.setText(device.getAppName());
                                activeStatusView.setText(getText(R.string.status_connected));
                                break;
                        }
                    }
                }
                break;
        }
    }
}
