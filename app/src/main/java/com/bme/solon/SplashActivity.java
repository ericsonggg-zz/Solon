package com.bme.solon;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bme.solon.bluetooth.BluetoothManager;

public class SplashActivity extends AppCompatActivity {

    /**
     * Turns on bluetooth if currently off.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Turn on bluetooth
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothManager.getInstance().getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BluetoothManager.REQUEST_ENABLE_BT);
            }
        }
        catch (BluetoothManager.BluetoothException e) {
            //TODO: Dialog & exit the app because its not compatible
        }
    }

    /**
     * Check whether user enabled Bluetooth if previously turned off.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //TODO: continue
            }
            else {
                //TODO: continue but show Toast
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //TODO: Remove button and change screens to home when all loading processes are complete
    public void toHome(View view) {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
    }

}
