package com.bme.solon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.xml.sax.DTDHandler;

public class SplashActivity extends AppCompatActivity {
    private DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    public void toHome(View view) {
        db = new DatabaseHelper(this);
        long id = db.addInstance(3);
        Instance instanceTest = db.retrieveInstance(id);
        Log.d("testing", instanceTest.toString());




//        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
    }
}
