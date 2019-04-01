package com.bme.solon;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bme.solon.database.Device;
import com.bme.solon.database.Instance;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.util.ArrayList;
import java.util.List;

public class InstanceListAdapter extends RecyclerView.Adapter<InstanceListAdapter.InstanceListViewHolder> {
    private static final String TAG = "InstanceListAdapter";

    private List<Instance> instanceList;
    private List<Device> deviceList;

    /**
     * ViewHolder specific for scanned devices.
     */
    public static class InstanceListViewHolder extends RecyclerView.ViewHolder {
        private ColorStateList themeTextColor;
        private TextView instanceResolution;
        private CustomAnalogClock instanceAnalogClock; //https://github.com/rosenpin/custom-analog-clock-view
        private TextView instanceDigitalClock;
        private TextView instanceDevice;
        private TextView instanceDate;

        /**
         * Constructor
         * @param itemView  View object
         */
        public InstanceListViewHolder(View itemView) {
            super(itemView);

            //Find views
            instanceResolution = itemView.findViewById(R.id.home_instance_resolution);
            instanceAnalogClock = itemView.findViewById(R.id.home_instance_analog_clock);
            instanceAnalogClock.setScale(0.75f);
            instanceDigitalClock = itemView.findViewById(R.id.home_instance_digital_clock);
            instanceDevice = itemView.findViewById(R.id.home_instance_device);
            instanceDate = itemView.findViewById(R.id.home_instance_date);

            //Store default text color from theme
            themeTextColor = instanceResolution.getTextColors();
        }

        /**
         * Bind instance data to the appropriate TextViews.
         * Also attach listeners to the buttons
         * @param instance    Device to show
         */
        public void bindData(Instance instance, Device device, InstanceListListener listener) {
            Log.v(TAG, "bindData");
            if (instance != null) {
                Log.v(TAG, "bindData: instance - " + instance.toString());

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
    }

    /**
     * OnClickListener for each List item in the RecyclerView.
     */
    public class InstanceListListener implements View.OnClickListener {
        private int position;

        /**
         * Constructor
         * @param position      Position in RecyclerView list
         */
        public InstanceListListener(int position) {
            this.position = position;
        }

        /**
         * Try to connect with device
         * @param view  View that was clicked
         */
        @Override
        public void onClick(View view) {
            Log.d(TAG,"onClick");

            Instance instance = instanceList.get(position);
            switch (view.getId()) {
                case R.id.connect_pair_list_rename:
                    Log.d(TAG, "onClick: rename instance " + instance.toString());
                    break;
                case R.id.connect_pair_list_connect:
                    Log.d(TAG, "onClick: connect to instance " + instance.toString());
                    //btService.connectToDevice(instance);
                    break;
            }
        }
    }

    /**
     * Constructor
     */
    public InstanceListAdapter() {
        super();
        instanceList = new ArrayList<>();
    }

    /**
     * Create a new InstanceListViewHolder
     * @param parent        The ViewGroup that the new View will be added to
     * @param viewType      View type of the new View
     * @return              New InstanceListViewHolder
     */
    @Override
    @NonNull
    public InstanceListViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        Log.v(TAG, "onCreateViewHolder: viewType " + viewType);
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_instance_list, parent, false);
        return new InstanceListViewHolder(view);
    }

    /**
     * Bind data to existing ScanListViewHolder
     * @param holder    Holder to bind data to
     * @param position  Position of data to bind
     */
    @Override
    public void onBindViewHolder(@NonNull final InstanceListViewHolder holder, final int position) {
        Log.v(TAG, "onBindViewHolder: position " + position);
        holder.bindData(instanceList.get(position), deviceList.get(position), new InstanceListListener(position));
    }

    /**
     * Get the total number of Devices
     * @return  Number of Devices
     */
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: # is " + instanceList.size());
        return instanceList.size();
    }

    /**
     * Add new Instance to the list
     * @param instance    Device to add
     */
    public void addInstance(Instance instance, Device device) {
        Log.v(TAG, "addInstance");
        if (!instanceList.contains(instance)) {
            Log.d(TAG, "addInstance: new instance " + instance.toString() + " added");
            instanceList.add(instance);
            deviceList.add(device);
        }
    }

    /**
     * Add multiple Instances to the list
     * @param instances   A list of Instances to add
     */
    public void addInstances(List<Instance> instances, List<Device> devices) {
        Log.v(TAG, "addInstances: # = " + instances.size());
        for (int i = 0; i < instances.size(); i++) {
            addInstance(instances.get(i), devices.get(i));
        }
    }
}
