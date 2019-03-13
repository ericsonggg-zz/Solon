package com.bme.solon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.database.sqlite.*;

public class AnalyticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
    }
}