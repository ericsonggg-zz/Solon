package com.bme.solon;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bme.solon.bluetooth.BluetoothManager;
import com.bme.solon.bluetooth.DeviceListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConnectFragment extends Fragment {
    private static final String TAG = "ConnectFragment";

    private BluetoothManager bluetoothManager;
    private RecyclerView pairList;
    private DeviceListAdapter pairAdapter;
    private RecyclerView.LayoutManager pairLayoutManager;

    private AlertDialog scanDialog;
    private ScanCallback scanCallback;  //need to start & stop scan

    /**
     * Required empty constructor
     */
    public ConnectFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_connect, container, false);

        //initialize RecyclerView
        pairList = fragmentView.findViewById(R.id.connect_pair_list);
        pairList.setHasFixedSize(true);
        pairLayoutManager = new LinearLayoutManager(getContext());
        pairList.setLayoutManager(pairLayoutManager);

        scanDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.connect_scan_title)
                .setView(R.layout.connect_scan_recycler)
                .setCancelable(true)
                .setOnCancelListener((arg0) -> {
                    scanDialog.dismiss();
                    bluetoothManager.getAdapter().getBluetoothLeScanner().stopScan(scanCallback);
                    Log.d(TAG, "scanDialog: stopped LE scan");
                })
                .create();

        //initialize BluetoothManager
        bluetoothManager = BluetoothManager.getInstance();

        //Set button listeners
        AppCompatImageButton button = fragmentView.findViewById(R.id.connect_button_power);
        button.setOnClickListener((view) -> toggleBluetooth(view));

        button = fragmentView.findViewById(R.id.connect_button_discover);
        button.setOnClickListener((view) -> discoverBluetooth(view));

        return fragmentView;
    }

    private void toggleBluetooth(View view) {
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            AlertDialog.Builder confirmOffDialog = new AlertDialog.Builder(getActivity());
            confirmOffDialog.setTitle(R.string.confirm)
                    .setMessage(R.string.connect_power_message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        bluetoothAdapter.disable();
                        //TODO: add other UI hooks
                    }).setNegativeButton(R.string.cancel, null)
                    .create().show();
        }
        else {
            bluetoothAdapter.enable();
            //TODO: add hooks
        }
    }

    private void discoverBluetooth(View view) {
        if(!bluetoothManager.isBluetoothOn()) {
            Toast.makeText(getActivity(), R.string.bluetooth_is_off, Toast.LENGTH_SHORT).show();
            return;
        }

        scanDialog.show();      //show dialog

        //Initialize RecyclerView in dialog
        RecyclerView scanDialogView = scanDialog.findViewById(R.id.connect_scan_view);
        scanDialogView.setHasFixedSize(true);
        scanDialogView.setLayoutManager(new LinearLayoutManager(scanDialog.getContext()));
        DeviceListAdapter scanAdapter = new DeviceListAdapter(scanDialog);
        scanDialogView.setAdapter(scanAdapter);

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                getActivity().runOnUiThread(() -> {
                    //Manually filter since auto-filtering doesn't work on Galaxy S7Edge
                    ScanFilter filter = new ScanFilter.Builder().setServiceUuid(BluetoothManager.HARDWARE_UUID).build();
                    Log.d(TAG, "discoverBluetooth: scan found result - "+result.getDevice().getName() + " ! " + filter.matches(result));

                    if (filter.matches(result)) {     //only show valid systems
                        scanAdapter.addDevice(result.getDevice());
                        scanAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        //Start Bluetooth LE scan
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.getBluetoothLeScanner().startScan(null, new ScanSettings.Builder().build(), scanCallback);
        Log.d(TAG, "discoverBluetooth: started LE scan");
    }
}
