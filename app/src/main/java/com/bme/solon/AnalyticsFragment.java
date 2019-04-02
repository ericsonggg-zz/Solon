package com.bme.solon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.bme.solon.database.DatabaseHelper;

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
    private CalendarView calendarView;

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
    protected void receiveBroadcast(Intent intent) {
    }
}
