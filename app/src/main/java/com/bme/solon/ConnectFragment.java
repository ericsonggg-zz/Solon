package com.bme.solon;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bme.solon.bluetooth.BluetoothManager;

public class ConnectFragment extends Fragment {
    private BluetoothManager bluetoothManager;
    private RecyclerView pairList;
    private RecyclerView.Adapter pairAdapter;
    private RecyclerView.LayoutManager pairLayoutManager;

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
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.startDiscovery();
    }
}
