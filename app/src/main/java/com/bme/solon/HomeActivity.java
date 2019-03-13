package com.bme.solon;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toolbar;
import android.annotation.TargetApi

public class HomeActivity extends AppCompatActivity {
    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar appToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        // setSupportActionBar(appToolbar);

        DatabaseHelper dbHelp = new DatabaseHelper(this);
        dbHelp.
    }
}
