package com.bme.solon;

import android.app.AlertDialog;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bme.solon.bluetooth.BluetoothBroadcast;
import com.bme.solon.bluetooth.BluetoothService;
import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;

import java.util.ArrayList;
import java.util.List;

public class PairListAdapter extends RecyclerView.Adapter<PairListAdapter.PairListViewHolder> {
    private static final String TAG = "PairListAdapter";

    private List<Device> deviceList;
    private BluetoothService btService;

    /**
     * ViewHolder specific for scanned devices.
     */
    public static class PairListViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView address;
        private AppCompatImageButton rename;
        private AppCompatImageButton connect;

        /**
         * Constructor
         * @param itemView  View object
         */
        public PairListViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.connect_pair_list_name);
            address = itemView.findViewById(R.id.connect_pair_list_address);
            rename = itemView.findViewById(R.id.connect_pair_list_rename);
            connect = itemView.findViewById(R.id.connect_pair_list_connect);
        }

        /**
         * Bind device data to the appropriate TextViews.
         * Also attach listeners to the buttons
         * @param device    Device to show
         */
        public void bindData(Device device, PairListListener listener) {
            Log.v(TAG, "bindData");
            if (device != null) {
                Log.v(TAG, "bindData: device - " + device.getAppName() + " " + device.getAddress());
                if (device.getAppName() == null || device.getAppName().length() <= 0) {
                    name.setText(R.string.connect_scan_no_name);
                }
                else {
                    name.setText(device.getAppName());
                }
                address.setText(device.getAddress());

                rename.setOnClickListener(listener);
                connect.setOnClickListener(listener);
            }
        }
    }

    /**
     * OnClickListener for each List item in the RecyclerView.
     */
    public class PairListListener implements View.OnClickListener {
        private int position;

        /**
         * Constructor
         * @param position      Position in RecyclerView list
         */
        public PairListListener(int position) {
            this.position = position;
        }

        /**
         * Try to connect with device
         * @param view  View that was clicked
         */
        @Override
        public void onClick(View view) {
            Log.v(TAG,"onClick");

            Device device = deviceList.get(position);
            Context context = view.getContext();
            switch (view.getId()) {
                case R.id.connect_pair_list_rename:
                    Log.d(TAG, "onClick: rename device " + device.toString());
                    EditText renameEdit = new EditText(context);
                    AlertDialog.Builder renameBuilder = new AlertDialog.Builder(context)
                            .setTitle(String.format(view.getContext().getString(R.string.connect_rename_title), device.getAppName()))
                            .setView(renameEdit)
                            .setCancelable(true)
                            .setPositiveButton(R.string.okay, (dialogInterface, i) -> {
                                if (renameEdit.getText().toString().isEmpty()) {
                                    Log.w(TAG, "onClick: rename is empty, not renaming");
                                    Toast.makeText(context, R.string.connect_rename_invalid, Toast.LENGTH_SHORT);
                                }
                                else {
                                    Log.d(TAG, "onClick: rename device to " + renameEdit.getText().toString());
                                    device.setAppName(renameEdit.getText().toString());
                                    DatabaseHelper db = DatabaseHelper.getInstance(context);
                                    db.updateAppName(device);

                                    Intent intent = new Intent(BluetoothBroadcast.ACTION_DEVICE_UPDATED);
                                    intent.putExtra(BluetoothBroadcast.KEY_DEVICE_ID, device.getId());
                                    context.sendBroadcast(intent);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null);
                    renameBuilder.create().show();
                    break;
                case R.id.connect_pair_list_connect:
                    Log.d(TAG, "onClick: connect");
                    AlertDialog.Builder connectBuilder = new AlertDialog.Builder(context)
                            .setTitle(String.format(context.getString(R.string.connect_connect_title), device.getAppName()))
                            .setCancelable(true)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                Log.d(TAG, "onClick: connect to device " + device.toString());
                                if (btService != null) {
                                    btService.connectToDevice(device);
                                }
                                else {
                                    Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(R.string.no, null);
                    if (btService != null) {
                        if (btService.getGattStatus() == BluetoothProfile.STATE_DISCONNECTED) {
                            connectBuilder.setMessage(String.format(context.getString(R.string.connect_connect_message_disconnected), device.getAppName()));
                        }
                        else {
                            connectBuilder.setMessage(R.string.connect_connect_message_streaming);
                        }
                        if (btService.isBluetoothOn()) {
                            connectBuilder.create().show();
                        }
                        else {
                            Toast.makeText(context, R.string.bluetooth_is_off, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Constructor
     * @param btService Bound service
     */
    public PairListAdapter(BluetoothService btService) {
        super();
        deviceList = new ArrayList<>();
        this.btService = btService;
    }

    /**
     * Create a new PairListViewHolder
     * @param parent        The ViewGroup that the new View will be added to
     * @param viewType      View type of the new View
     * @return              New PairListViewHolder
     */
    @Override
    @NonNull
    public PairListViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        Log.v(TAG, "onCreateViewHolder: viewType " + viewType);
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_pair_list, parent, false);
        return new PairListViewHolder(view);
    }

    /**
     * Bind data to existing ScanListViewHolder
     * @param holder    Holder to bind data to
     * @param position  Position of data to bind
     */
    @Override
    public void onBindViewHolder(@NonNull final PairListViewHolder holder, final int position) {
        Log.v(TAG, "onBindViewHolder: position " + position);
        holder.bindData(deviceList.get(position), new PairListListener(position));
    }

    /**
     * Get the total number of Devices
     * @return  Number of Devices
     */
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: # is " + deviceList.size());
        return deviceList.size();
    }

    /**
     * Add new Device to the list
     * @param device    Device to add
     */
    public void addDevice(Device device) {
        Log.v(TAG, "addDevice");
        if (!deviceList.contains(device)) {
            Log.d(TAG, "addDevice: new device " + device.toString() + " added");
            deviceList.add(device);
        }
    }

    /**
     * Add multiple Devices to the list
     * @param devices   A list of Devices to add
     */
    public void addDevices(List<Device> devices) {
        Log.v(TAG, "addDevices: # = " + devices.size());
        for (Device device : devices) {
            addDevice(device);
        }
    }

    public void updateDevice(Device device) {
        Log.v(TAG, "updateDevice: " + device.toString());
        if (device != null && device.getId() != -1) {
            for (Device storedDevice : deviceList) {
                if (storedDevice != null && storedDevice.getId() == device.getId()) {
                    storedDevice = device;
                    break;
                }
            }
        }
    }

    /**
     * Delete all Devices and add the new list
     * @param devices   New device list
     */
    public void resetDevices(List<Device> devices) {
        Log.v(TAG, "resetDevices: # = " + devices.size());
        deviceList = new ArrayList<>();
        addDevices(devices);
    }
}
