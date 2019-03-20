package com.bme.solon;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Instance;

public class SplashActivity extends AppCompatActivity {
    private DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void toHome(View view) {
//        db = new DatabaseHelper(this);
//        long id = db.addInstance(3);
//        Instance instanceTest = db.retrieveInstance(id);
//        Log.d("testingggggggggggg", Integer.toString(instanceTest.getStatus()));
//        //List<Instance> instances = db.getAllInstances();
//        db.markInstanceAsRead(instanceTest);
//        Instance instanceTest2 = db.retrieveInstance(id);
//        //List<Instance> instancesTime = db.getInstancesByTime("week");
////        Log.d("testing", instanceTest.toString());
////        Log.d("testinggggggggg", Integer.toString(instanceTest.getSeverity()));
////        Log.d("testingggggggggggg", instanceTest.getTime());
////        Log.d("testingggggggggggg", Integer.toString(instances.size()));
//        Log.d("testingggggggggggg", Integer.toString(instanceTest2.getStatus()));
//
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }
}
