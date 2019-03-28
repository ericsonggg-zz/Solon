package com.bme.solon.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bme.solon.R;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder> {

    public static class DeviceListViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView address;

        public DeviceListViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.connect_pair_list_name);
            address = itemView.findViewById(R.id.connect_pair_list_address);
        }

        public void bindData(BluetoothDevice device) {
            if (device != null) {
                Log.d("DeviceListAdapter", device.getName() + " " + device.getAddress());
                if (device.getName() == null || device.getName().length() <= 0) {
                    name.setText(R.string.connect_scan_no_name);
                }
                else {
                    name.setText(device.getName());
                }
                address.setText(device.getAddress());
            }
        }
    }

    public class ScanListListener implements View.OnClickListener {
        private int position;

        public ScanListListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            //TODO: cancel scan
            Log.d("YAY","hue");
            dialog.dismiss();
            BluetoothDevice device = mLeDevices.get(position);
            synchronized(this)
            {
                BluetoothGatt mBluetoothGatt = device.connectGatt(view.getContext(), false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        Log.d("YAY", "trying;");
                        gatt.discoverServices();
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        for (BluetoothGattService service : gatt.getServices()) {
                            Log.d("YAY", "ohno " +service.getUuid().toString());
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                Log.d("YAY","chr " + characteristic.getUuid().toString());
                                if (characteristic.getUuid().toString().equals("0000dfb1-0000-1000-8000-00805f9b34fb")){
                                    Log.d("YAY", "set");
                                    gatt.setCharacteristicNotification(characteristic, true);
                                    gatt.readCharacteristic(characteristic);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicRead(gatt, characteristic, status);
                        Log.d("YAY", new String (characteristic.getValue()));
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicChanged(gatt, characteristic);
                        Log.d("YAY", new String (characteristic.getValue()));
                    }
                });
            }
        }
    }

    private ArrayList<BluetoothDevice> mLeDevices;
    private AlertDialog dialog;

    public DeviceListAdapter(AlertDialog dialog) {
        super();
        mLeDevices = new ArrayList<>();
        this.dialog = dialog;
    }

    @Override
    public DeviceListViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_scan_list, parent, false);
        return new DeviceListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DeviceListViewHolder holder, final int position) {
        holder.bindData(mLeDevices.get(position));
        holder.itemView.setOnClickListener(new ScanListListener(position));
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }
}
