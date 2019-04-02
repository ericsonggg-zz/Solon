package com.bme.solon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;
import com.bme.solon.database.Instance;
import com.bme.solon.strip.StripStatus;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceListAdapter extends RecyclerView.Adapter<InstanceListAdapter.InstanceListViewHolder> {
    private static final String TAG = "InstanceListAdapter";

    private List<Instance> instanceList;
    private Map<Long, Device> deviceList;

    /**
     * ViewHolder specific for scanned devices.
     */
    public static class InstanceListViewHolder extends RecyclerView.ViewHolder {
        private ColorStateList themeTextColor;
        private TextView instanceResolution;
        private ImageView instanceUTI;
        private CustomAnalogClock instanceAnalogClock; //https://github.com/rosenpin/custom-analog-clock-view
        private TextView instanceDigitalClock;
        private TextView instanceDevice;
        private TextView instanceDate;
        private TextView instanceResolutionTime;

        /**
         * Constructor
         * @param itemView  View object
         */
        public InstanceListViewHolder(View itemView) {
            super(itemView);

            //Find views
            instanceResolution = itemView.findViewById(R.id.home_instance_resolution);
            instanceUTI = itemView.findViewById(R.id.home_instance_uti);
            instanceAnalogClock = itemView.findViewById(R.id.home_instance_analog_clock);
            instanceAnalogClock.setScale(0.45f);
            instanceDigitalClock = itemView.findViewById(R.id.home_instance_digital_clock);
            instanceDevice = itemView.findViewById(R.id.home_instance_device);
            instanceDate = itemView.findViewById(R.id.home_instance_date);
            instanceResolutionTime = itemView.findViewById(R.id.home_instance_resolution_time);

            //Store default text color from theme
            themeTextColor = instanceResolution.getTextColors();
        }

        /**
         * Bind instance data to the appropriate TextViews.
         * Also attach listeners to the buttons
         * @param instance    Instance to show
         * @param device      Supporting device
         */
        @SuppressLint("ResourceAsColor")
        public void bindData(Instance instance, Device device) {
            Log.v(TAG, "bindData");
            if (instance != null && device != null) {
                Log.v(TAG, "bindData: instance - " + instance.toString());

                //Update instance views
                if (instance.getResolution() == Instance.RESOLVED) {
                    instanceResolution.setText(R.string.home_instance_resolved);
                    instanceResolution.setTextColor(Color.rgb(103, 103, 103));
                    Context context = instanceResolutionTime.getContext();
                    instanceResolutionTime.setText(String.format(context.getString(R.string.home_instance_resolution_time), Instance.TIME_FORMAT.format(instance.getResolutionTime())));
                }
                else {
                    instanceResolution.setText(R.string.home_instance_unresolved);
                    instanceResolution.setTextColor(themeTextColor);
                    instanceResolutionTime.setText(R.string.home_instance_default_resolution_time);
                }
                if (instance.getSeverity() == StripStatus.NO_UTI.getSeverity()) { //hide image if no uti
                    instanceUTI.setVisibility(View.INVISIBLE);
                }
                else {
                    instanceUTI.setVisibility(View.VISIBLE);
                }
                instanceDevice.setText(device.getAppName());
                instanceAnalogClock.setTime(instance.getDateTimeAsCalendar());
                instanceDigitalClock.setText(Instance.TIME_FORMAT.format(instance.getDateTime()));
                instanceDate.setText(Instance.DATE_FORMAT.format(instance.getDateTime()));
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
         * Ask to resolve instance
         * @param view  View that was clicked
         */
        @Override
        public void onClick(View view) {
            Instance instance = instanceList.get(position);
            Log.v(TAG, "onClick");

            if (instance.getResolution() == Instance.UNRESOLVED) {
                Log.v(TAG, "onClick: unresolved " + instance.toString());
                Device device = deviceList.get(instance.getDeviceId());
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle(R.string.home_dialog_title)
                        .setMessage(String.format(view.getContext().getString(R.string.home_dialog_message), device.getAppName(), instance.getDateTimeAsString()))
                        .setCancelable(true)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            Log.d(TAG, "onClick: resolving instance");
                            instance.setResolution(Instance.RESOLVED);
                            instance.setResolutionTime();

                            DatabaseHelper db = DatabaseHelper.getInstance(view.getContext());
                            db.updateInstance(instance);

                            view.notify();
                        })
                        .setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }
        }
    }

    /**
     * Constructor
     */
    public InstanceListAdapter() {
        super();
        instanceList = new ArrayList<>();
        deviceList = new HashMap<>();
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
        holder.bindData(instanceList.get(position), deviceList.get(instanceList.get(position).getDeviceId()));
        holder.itemView.setOnClickListener(new InstanceListListener(position));
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
        if (!instanceList.contains(instance) && device != null) {
            Log.d(TAG, "addInstance: new instance " + instance.toString() + " added");
            instanceList.add(0, instance);
            deviceList.put(device.getId(), device);
        }
    }

    /**
     * Add multiple Instances to the list
     * @param instances   A list of Instances to add
     */
    public void addInstances(List<Instance> instances, List<Device> devices) {
        Log.v(TAG, "addInstances: # = " + instances.size());
        for (Instance instance : instances) {
            if (!instanceList.contains(instance)) {
                if (deviceList.containsKey(instance.getDeviceId())) {
                    Log.d(TAG, "addInstance: add only instance " + instance.toString());
                    instanceList.add(0, instance);
                }
                else {
                    for (Device device : devices) {
                        if (device.getId() == instance.getDeviceId()) {
                            Log.d(TAG, "addInstance: add instance " + instance.toString() + " and device " + device.toString());
                            instanceList.add(0, instance);
                            deviceList.put(device.getId(), device);
                        }
                    }
                }
            }
        }
    }

    /**
     * Update an existing instance's details
     * @param instance  Instance to update
     */
    public void updateInstance(Instance instance) {
        Log.v(TAG, "updateInstance: " + instance.toString());
        for (Instance oldInstance : instanceList)
        {
            if (oldInstance.getId() == instance.getId()) {
                Log.v(TAG, "updateInstance: found old instance");
                oldInstance.setSeverity(instance.getSeverity());
                oldInstance.setResolution(instance.getResolution());
                oldInstance.setResolutionTime(instance.getResolutionTime());
            }
        }
    }
}
