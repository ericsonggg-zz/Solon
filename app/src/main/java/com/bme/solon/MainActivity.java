package com.bme.solon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.bme.solon.bluetooth.BluetoothBroadcast;
import com.bme.solon.bluetooth.BluetoothService;

/**
 * Main activity that provides navigation. Starts with HomeFragment.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private DrawerLayout navigationDrawer; //navigation pane parent view
    private FragmentManager fragmentManager; //the fragment manager
    /*
        current fragment being displayed
        0 = HomeFragment
        1 = ConnectFragment
        2 = AnalyticsFragment
     */
    private int currentFragmentNum;
    private MainFragment currentFragment;

    private ServiceConnection btServiceConnection;
    private BroadcastReceiver btServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: action = " + intent.getAction());
            currentFragment.receiveBroadcast(intent);
        }
    };

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        //Setup the toolbar and navigation drawer
        Toolbar appToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(appToolbar);
        ActionBar appBar = getSupportActionBar();
        appBar.setDisplayHomeAsUpEnabled(true);
        appBar.setHomeAsUpIndicator(R.drawable.nav_menu);

        //Setup navigation view and item onClick
        navigationDrawer = findViewById(R.id.navigation_pane);
        NavigationView navigationView = findViewById(R.id.main_navigation_view);
        navigationView.setCheckedItem(R.id.menu_home);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setCheckable(true); //make clicked item selected
                switch (item.getItemId()) { //replace the fragments if its not currently displayed
                    case R.id.menu_home:
                        if (currentFragmentNum != 0) {
                            MainFragment tempFragment = new HomeFragment(); //TODO: change to reuse
                            replaceFragment(tempFragment);
                            currentFragment = tempFragment;
                            currentFragmentNum = 0;
                        }
                        break;
                    case R.id.menu_connect:
                        if (currentFragmentNum != 1) {
                            MainFragment tempFragment = new ConnectFragment();
                            replaceFragment(tempFragment);
                            currentFragment = tempFragment;
                            currentFragmentNum = 1;
                        }
                        break;
                    case R.id.menu_analytics:
                        if (currentFragmentNum != 2) {
                            MainFragment tempFragment = new AnalyticsFragment();
                            replaceFragment(tempFragment);
                            currentFragment = tempFragment;
                            currentFragmentNum = 2;
                        }
                        break;
                }
                navigationDrawer.closeDrawers();
                return true;
            }
        });
        fragmentManager = getSupportFragmentManager();

        //Add HomeFragment as the initial fragment
        currentFragmentNum = 0;
        currentFragment = new HomeFragment();
        fragmentManager.beginTransaction().add(R.id.main_fragment_view, currentFragment).commit();
    }

    /**
     * Bind to {@link BluetoothService}
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //service bound listener
        btServiceConnection = new ServiceConnection() {
            /**
             * On connection (via bindService()).
             * @param iBinder       {@link BluetoothService.Binder}
             */
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: " + componentName.flattenToString());
                BluetoothService.Binder binder = (BluetoothService.Binder) iBinder;
                currentFragment.onServiceConnected(binder.getService());
            }

            /**
             * On crash or disconnect
             * TODO: auto-reconnect
             */
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: " + componentName.flattenToString());
                currentFragment.onServiceDisconnected();
            }
        };

        //bind to service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, btServiceConnection, Context.BIND_AUTO_CREATE);

        //register broadcast receiver
        registerReceiver(btServiceReceiver, BluetoothBroadcast.getIntentFilter());
    }

    /**
     * Unbind from service
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        //unregister broadcast receiver
        unregisterReceiver(btServiceReceiver);

        //unbind service
        if (currentFragment.isServiceBound) {
            Log.d(TAG, "onStop: unbinding service");
            unbindService(btServiceConnection);
            currentFragment.onServiceUnbound();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: destroy
    }

    /**
     * Listener for ActionBar
     *
     * @param item      Selected item
     * @return          true if successful, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //open nav pane
                navigationDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Use the fragment manager to replace the fragment in main_fragment_view
     * @param fragment      The fragment to replace with
     */
    private void replaceFragment (MainFragment fragment) {
        Log.d(TAG, "replaceFragment: isServiceBound = " + currentFragment.isServiceBound);
        fragment.btService = currentFragment.btService;
        fragment.isServiceBound = currentFragment.isServiceBound;

        fragmentManager.beginTransaction().replace(R.id.main_fragment_view, fragment).commit();
    }
}
