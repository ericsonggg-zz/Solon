package com.bme.solon;

import android.content.Intent;
import android.os.Bundle;
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
    }
}
