package com.bme.solon;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Starts with HomeFragment as defined in XML
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout navigationPane;

    private FragmentManager fragmentManager; //the fragment manager
    private int currentFragment; //current fragment being displayed

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the toolbar and navigation pane
        Toolbar appToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(appToolbar);
        ActionBar appBar = getSupportActionBar();
        appBar.setDisplayHomeAsUpEnabled(true);
        appBar.setHomeAsUpIndicator(R.drawable.nav_menu);

        navigationPane = findViewById(R.id.navigation_pane);

        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //open nav pane
                navigationPane.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_home:
                if (currentFragment != 0)

                break;
            case R.id.action_connect:
                break;
            case R.id.action_analytics:
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
