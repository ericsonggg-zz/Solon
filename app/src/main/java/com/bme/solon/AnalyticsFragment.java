package com.bme.solon;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bme.solon.bluetooth.BluetoothBroadcast;
import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Instance;
import com.bme.solon.strip.StripStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.vo.DateData;

public class AnalyticsFragment extends MainFragment {
    public static final String TAG = "AnalyticsFragment";

    private DatabaseHelper db;

    private TextView riskText;
    private TextView riskSubText;
    private TextView responseText;
    private TextView responseSubText;
    private TextView incidentText;
    private TextView incidentSubText;
    private TextView utiText;
    private TextView utiSubText;
    private MCalendarView calendarView;     //https://github.com/SpongeBobSun/mCalendarView?utm_source=android-arsenal.com&utm_medium=referral&utm_campaign=2420

    /**
     * Required empty constructor
     */
    public AnalyticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_analytics, container, false);

        db = DatabaseHelper.getInstance(getActivity());

        //find Views
        riskText = fragmentView.findViewById(R.id.analytics_risk_time_data);
        riskSubText = fragmentView.findViewById(R.id.analytics_risk_time_subtext);
        responseText = fragmentView.findViewById(R.id.analytics_response_time_data);
        responseSubText = fragmentView.findViewById(R.id.analytics_response_time_subtext);
        incidentText = fragmentView.findViewById(R.id.analytics_incident_count_data);
        incidentSubText = fragmentView.findViewById(R.id.analytics_incident_count_subtext);
        utiText = fragmentView.findViewById(R.id.analytics_uti_count_data);
        utiSubText = fragmentView.findViewById(R.id.analytics_uti_count_subtext);
        calendarView = fragmentView.findViewById(R.id.analytics_calendar);

        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //Populate all views
        List<Instance> instances = db.getAllInstances();
        updateRiskTime(instances);
        updateResponseTime(instances);
        updateIncidents(instances);
        updateUTIs(instances);
        bulkUpdateCalendar(instances);
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
        List<Instance> instances = db.getAllInstances();
        long id = intent.getLongExtra(BluetoothBroadcast.KEY_INSTANCE_ID, -1);

        switch (intent.getAction()) {
            case BluetoothBroadcast.ACTION_NEW_INSTANCE:
                updateRiskTime(instances);
                updateResponseTime(instances);
                updateIncidents(instances);
                updateUTIs(instances);
                if (id != -1) {
                    updateCalendar(db.retrieveInstance(id));
                }
                break;
            case BluetoothBroadcast.ACTION_INSTANCE_UPDATE:
                updateResponseTime(instances);
                updateUTIs(instances);
                if (id != -1) {
                    updateCalendar(db.retrieveInstance(id));
                }
                break;
        }
    }

    /**
     * Update the risk time UI to the median
     * @param instances
     */
    private void updateRiskTime(List<Instance> instances) {
        Log.v(TAG, "updateRiskTime: posting task");

        AsyncTask<List<Instance>, Void, Void> task = new AsyncTask<List<Instance>, Void, Void>() {
            int riskTime = -1;

            @Override
            protected Void doInBackground(List<Instance>... lists) {
                Log.v(TAG, "updateRiskTime: task doInBackground");
                List<Instance> instances = lists[0];

                if (instances.size() > 0) {
                    //Get all time in minutes
                    int[] timeInMins = new int[instances.size()];
                    for (int i = 0; i < instances.size(); i++) {
                        Calendar time = instances.get(i).getDateTimeAsCalendar();
                        timeInMins[i] = time.get(Calendar.MINUTE) + time.get(Calendar.HOUR) * 60;
                    }

                    Arrays.sort(timeInMins);
                    int n = timeInMins.length;
                    if (n % 2 != 0) {
                        riskTime = timeInMins[n / 2];
                    }
                    else {
                        riskTime = (timeInMins[(n-1)/2] + timeInMins[n/2]) / 2;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.v(TAG, "updateRiskTime: task onPostExecute, riskTime = " + riskTime);

                if (riskTime == -1) {
                    riskText.setText(R.string.analytics_risk_time_default);
                    riskSubText.setText(R.string.analytics_risk_time_subdefault);
                }
                else {
                    int hr = riskTime / 60;
                    int min = riskTime % 60;

                    if (hr >= 12) {
                        riskSubText.setText(R.string.analytics_risk_time_pm);
                    }
                    else {
                        riskSubText.setText(R.string.analytics_risk_time_am);
                    }
                    hr = hr % 12;
                    riskText.setText(String.format("%1$d:%2$d", hr==0 ? 12 : hr, min));
                }
            }
        };
        task.execute(new ArrayList<>(instances));
    }

    /**
     * Update the response time UI to the best average in minutes or hours.
     */
    private void updateResponseTime(List<Instance> instances) {
        Log.v(TAG, "updateResponseTime: posting task");

        AsyncTask<List<Instance>, Void, Void> task = new AsyncTask<List<Instance>, Void, Void>() {
           int totalTime = 0;
           int count = 0;

            @Override
            protected Void doInBackground(List<Instance>... lists) {
                Log.v(TAG, "updateResponseTime: task doInBackground");
                List<Instance> instances = lists[0];
                for (Instance instance : instances) {
                    if (instance.getResolution() == Instance.RESOLVED) {
                        totalTime += instance.getResolutionTimeAsCalender().getTimeInMillis() - instance.getDateTimeAsCalendar().getTimeInMillis();
                        count++;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.v(TAG, "updateResponseTime: task onPostExecute");
                if (count == 0) {
                    responseText.setText(R.string.analytics_response_time_default);
                    responseSubText.setText(R.string.analytics_response_time_subdefault);
                }
                else {
                    long averageMillis = totalTime / count;
                    if (averageMillis >= 100 * 60 * 1000) {
                        Log.d(TAG, "updateResponseTime: in hours - " + averageMillis);
                        long averageHr = Math.round (averageMillis / (60*60*1000.0));
                        responseText.setText(Long.toString(averageHr));
                        responseSubText.setText(averageHr == 1 ? R.string.analytics_response_time_hr : R.string.analytics_response_time_hrs);
                    }
                    else {
                        Log.d(TAG, "updateResponseTime: in minutes - " + averageMillis);
                        long averageMin = Math.round (averageMillis / (60*1000.0));
                        responseText.setText(Long.toString(averageMin));
                        responseSubText.setText(averageMin == 1 ? R.string.analytics_response_time_min : R.string.analytics_response_time_mins);
                    }
                }
            }
        };
        task.execute(new ArrayList<>(instances));
    }

    /**
     * Get average number of incidents per calendar month.
     */
    private void updateIncidents(List<Instance> instances) {
        Log.v(TAG, "updateIncidents: posting task");

        AsyncTask<List<Instance>, Void, Void> task = new AsyncTask<List<Instance>, Void, Void>() {
            double average = -1;

            @Override
            protected Void doInBackground(List<Instance>... lists) {
                Log.v(TAG, "updateIncidents: task doInBackground");
                List<Instance> instances = lists[0];

                if (instances.size() > 0) {
                    List<Integer> count = new ArrayList<>();
                    count.add(0); //add 1st one
                    int counter = 0;
                    int month = instances.get(0).getDateTimeAsCalendar().get(Calendar.MONTH);

                    //compute totals
                    for (Instance instance : instances) {
                        int thisMonth = instance.getDateTimeAsCalendar().get(Calendar.MONTH);
                        if (month != thisMonth) {
                            count.add(1);
                            counter++;
                            month = thisMonth;
                        }
                        else {
                            count.set(counter, count.get(counter) + 1);
                        }
                    }

                    //compute average
                    long total = 0;
                    for (Integer integer : count) {
                        total += integer;
                    }
                    average = total / (counter+1.0);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.v(TAG, "updateIncidents: task onPostExecute");

                if (average == -1) {
                    incidentText.setText(R.string.analytics_incident_count_default);
                }
                else {
                    incidentText.setText(String.format("%1$.2f", average));
                }
            }
        };
        task.execute(new ArrayList<>(instances));
    }

    /**
     * Get the average number of UTIs per calendar month
     */
    private void updateUTIs(List<Instance> instances) {
        Log.v(TAG, "updateUTIs: posting task");

        AsyncTask<List<Instance>, Void, Void> task = new AsyncTask<List<Instance>, Void, Void>() {
            double average = -1;

            @Override
            protected Void doInBackground(List<Instance>... lists) {
                Log.v(TAG, "updateUTIs: task doInBackground");
                List<Instance> instances = lists[0];

                if (instances.size() > 0) {
                    List<Integer> count = new ArrayList<>();
                    count.add(0); //add 1st one
                    int counter = 0;
                    int month = instances.get(0).getDateTimeAsCalendar().get(Calendar.MONTH);

                    //compute totals
                    for (Instance instance : instances) {
                        if (instance.getSeverity() > StripStatus.NO_UTI.getSeverity()) {
                            int thisMonth = instance.getDateTimeAsCalendar().get(Calendar.MONTH);
                            if (month != thisMonth) {
                                count.add(1);
                                counter++;
                                month = thisMonth;
                            } else {
                                count.set(counter, count.get(counter) + 1);
                            }
                        }
                    }

                    //compute average
                    long total = 0;
                    for (Integer integer : count) {
                        total += integer;
                    }
                    average = total / (counter+1.0);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.v(TAG, "updateUTIs: task onPostExecute");

                if (average == -1) {
                    utiText.setText(R.string.analytics_incident_count_default);
                }
                else {
                    utiText.setText(String.format("%1$.2f", average));
                }
            }
        };
        task.execute(new ArrayList<>(instances));
    }

    /**
     * Mark all dates with incidents.
     * @param instances All incidents to mark
     */
    private void bulkUpdateCalendar(List<Instance> instances) {
        Log.v(TAG, "bulkUpdateCalendar");

        //Clear calendar first
        List<DateData> previousDates = new ArrayList<>(calendarView.getMarkedDates().getAll());
        for (DateData dateData : previousDates) {
            calendarView.unMarkDate(dateData);
        }

        //Mark today
        Calendar todayCal = GregorianCalendar.getInstance();
        DateData today = new DateData(todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH)+1, todayCal.get(Calendar.DAY_OF_MONTH));
        calendarView.markDate(today.setMarkStyle(MarkStyle.BACKGROUND, Color.BLUE));

        //Mark all instances
        for (Instance instance : instances) {
            Calendar instanceDate = instance.getDateTimeAsCalendar();
            DateData data = new DateData(instanceDate.get(Calendar.YEAR), instanceDate.get(Calendar.MONTH)+1, instanceDate.get(Calendar.DAY_OF_MONTH));

            if (instance.getSeverity() == StripStatus.NO_UTI.getSeverity()) {
                data.setMarkStyle(MarkStyle.DOT, Color.WHITE);
            }
            else if (instance.getSeverity() == StripStatus.LIGHT_UTI.getSeverity()) {
                data.setMarkStyle(MarkStyle.BACKGROUND, Color.WHITE);
            }
            else if (instance.getSeverity() == StripStatus.MEDIUM_UTI.getSeverity()) {
                data.setMarkStyle(MarkStyle.BACKGROUND, Color.YELLOW);
            }
            else {
                data.setMarkStyle(MarkStyle.BACKGROUND, Color.RED);
            }
            calendarView.markDate(data);
        }
    }

    /**
     * Update the calendar marking for a single day
     * @param instance  Instance to update
     */
    private void updateCalendar(Instance instance) {
        Log.v(TAG, "updateCalendar: instance = " + instance.toString());

        Calendar instanceDate = instance.getDateTimeAsCalendar();
        DateData data = new DateData(instanceDate.get(Calendar.YEAR), instanceDate.get(Calendar.MONTH), instanceDate.get(Calendar.DAY_OF_MONTH));

        if (instance.getSeverity() == StripStatus.NO_UTI.getSeverity()) {
            data.setMarkStyle(MarkStyle.DOT, Color.WHITE);
        }
        else if (instance.getSeverity() == StripStatus.LIGHT_UTI.getSeverity()) {
            data.setMarkStyle(MarkStyle.BACKGROUND, Color.WHITE);
        }
        else if (instance.getSeverity() == StripStatus.MEDIUM_UTI.getSeverity()) {
            data.setMarkStyle(MarkStyle.BACKGROUND, Color.YELLOW);
        }
        else {
            data.setMarkStyle(MarkStyle.BACKGROUND, Color.RED);
        }
        calendarView.markDate(data);
    }
}
