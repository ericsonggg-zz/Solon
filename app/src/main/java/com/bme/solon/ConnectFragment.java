package com.bme.solon;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bme.solon.bluetooth.BluetoothBroadcast;
import com.bme.solon.bluetooth.BluetoothManager;
import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;

public class ConnectFragment extends MainFragment {
    private static final String TAG = "ConnectFragment";

    private RecyclerView pairList;
    private PairListAdapter pairAdapter;

    private AlertDialog scanDialog;
    private ScanCallback scanCallback;  //need to start & stop scan, never null after discoverBluetooth() is called the first time

    private TextView activeNameView;
    private TextView activeStatusView;
    private TextView toggleButtonTextView;

    /**
     * Required empty constructor
     */
    public ConnectFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_connect, container, false);

        //initialize paired devices RecyclerView
        pairList = fragmentView.findViewById(R.id.connect_pair_list);
        pairList.setHasFixedSize(true);
        pairList.setLayoutManager(new LinearLayoutManager(getContext()));
        pairAdapter = new PairListAdapter(btService);   //TODO: change this so its reactive to service unbinding
        pairList.setAdapter(pairAdapter);

        scanDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.connect_scan_title)
                .setView(R.layout.connect_scan_recycler)
                .setCancelable(true)
                .setOnCancelListener((arg0) -> {
                    scanDialog.dismiss();
                    if (isServiceBound) {
                        Log.d(TAG, "scanDialog: stopping LE scan due to cancel");
                        btService.stopDiscovery(scanCallback);
                    }
                })
                .setOnDismissListener((arg0) -> {
                    scanDialog.dismiss();
                    if (isServiceBound) {
                        Log.d(TAG, "scanDialog: stopping LE scan due to dismiss");
                        btService.stopDiscovery(scanCallback);
                    }
                })
                .create();

        //Set button listeners
        AppCompatImageButton button = fragmentView.findViewById(R.id.connect_button_power);
        button.setOnClickListener((view) -> toggleBluetooth(view));

        button = fragmentView.findViewById(R.id.connect_button_discover);
        button.setOnClickListener((view) -> discoverBluetooth(view));

        //Find the rest of the views
        activeNameView = fragmentView.findViewById(R.id.connect_active_name);
        activeStatusView = fragmentView.findViewById(R.id.connect_active_status);
        toggleButtonTextView = fragmentView.findViewById(R.id.connect_button_power_text);

        return fragmentView;
    }

    /**
     * Initialize views based on current Bluetooth status.
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //Populate pairList
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        pairAdapter.addDevices(db.getPairedDevices());
        pairAdapter.notifyDataSetChanged();

        //Set text for remaining UI
        if (isServiceBound) {
            if (!btService.isBluetoothOn()) {
                activeNameView.setText(getText(R.string.status_disconnected_device));
                activeStatusView.setText(getText(R.string.status_bluetooth_off));
                toggleButtonTextView.setText(getText(R.string.on));
            }
            else {
                Device device = btService.getConnectedDevice();
                toggleButtonTextView.setText(getText(R.string.off));
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

    /**
     * Update UI accordingly to broadcast
     * @param intent    The broadcasted intent.
     */
    @Override
    protected void receiveBroadcast(Intent intent) {
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        activeNameView.setText(getText(R.string.status_disconnected_device));
                        activeStatusView.setText(getText(R.string.status_bluetooth_off));
                        toggleButtonTextView.setText(getText(R.string.on));
                        break;
                    case BluetoothAdapter.STATE_ON:
                        activeNameView.setText(getText(R.string.status_disconnected_device));
                        activeStatusView.setText(getText(R.string.status_disconnected));
                        toggleButtonTextView.setText(getText(R.string.off));
                        break;
                }
                break;
            case BluetoothBroadcast.ACTION_CONNECTING:
                activeNameView.setText(intent.getStringExtra(BluetoothBroadcast.KEY_DEVICE_NAME));
                activeStatusView.setText(getText(R.string.status_connecting));
                break;
            case BluetoothBroadcast.ACTION_CONNECTED:
                break;
            case BluetoothBroadcast.ACTION_CONNECTED_UPDATE:
                activeStatusView.setText(getText(R.string.status_connected));
                break;
            case BluetoothBroadcast.ACTION_DISCONNECTED:
                activeNameView.setText(getText(R.string.status_disconnected_device));
                activeStatusView.setText(getText(R.string.status_disconnected));
                break;
            case BluetoothBroadcast.ACTION_DEVICES_CHANGED:
                DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                pairAdapter.resetDevices(db.getPairedDevices());
                pairAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * Enable or disable Bluetooth.
     * Prompts user to confirm turning it off. No prompt for turning on.
     * @param view  The button view.
     */
    private void toggleBluetooth(View view) {
        if (!isServiceBound) {
            Log.e(TAG, "toggleBluetooth: service is unbound when it should be bound");
            Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
            return;
        }

        if (btService.isBluetoothOn()) {
            AlertDialog.Builder confirmOffDialog = new AlertDialog.Builder(getActivity());
            confirmOffDialog.setTitle(R.string.confirm)
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        Log.d(TAG, "toggleBluetooth: off");
                        btService.disableBluetooth();
                    }).setNegativeButton(R.string.cancel, null);

            if (btService.getGattStatus() == BluetoothProfile.STATE_DISCONNECTED) {
                Log.v(TAG, "toggleBluetooth: prompt for dead connection");
                confirmOffDialog.setMessage(R.string.connect_power_disconnected_message);
            }
            else {
                Log.v(TAG, "toggleBluetooth: prompt for live connection");
                confirmOffDialog.setMessage(R.string.connect_power_connected_message);
            }
            confirmOffDialog.create().show();
        }
        else {
            Log.d(TAG, "toggleBluetooth: on");
            btService.enableBluetooth();
        }
    }

    /**
     * Start scanning for new Solon devices.
     * @param view  Button view
     */
    private void discoverBluetooth(View view) {
        if (!isServiceBound) {
            Log.e(TAG, "discoverBluetooth: service is unbound when it should be bound");
            Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
            return;
        }

        if(!btService.isBluetoothOn()) {
            Log.d(TAG, "discoverBluetooth: bluetooth is off, not starting discovery");
            Toast.makeText(getActivity(), R.string.bluetooth_is_off, Toast.LENGTH_SHORT).show();
            return;
        }

        scanDialog.show(); //show dialog

        //Initialize RecyclerView in dialog
        RecyclerView scanDialogView = scanDialog.findViewById(R.id.connect_scan_view);
        scanDialogView.setHasFixedSize(true);
        scanDialogView.setLayoutManager(new LinearLayoutManager(scanDialog.getContext()));
        ScanListAdapter scanAdapter = new ScanListAdapter(scanDialog, btService);
        scanDialogView.setAdapter(scanAdapter);

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.d(TAG, "discoverBluetooth: onScanResult");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        //Manually filter since auto-filtering doesn't work on Galaxy S7Edge
                        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(BluetoothManager.HARDWARE_UUID).build();
                        Log.v(TAG, "discoverBluetooth: scan found result - " + result.getDevice().getName() + " ! " + filter.matches(result));

                        if (filter.matches(result)) {     //only show valid systems
                            scanAdapter.addDevice(result.getDevice());
                            scanAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };

        //Start Bluetooth LE scan
        btService.startDiscovery(scanCallback);
        Log.d(TAG, "discoverBluetooth: started LE scan");
    }
}
