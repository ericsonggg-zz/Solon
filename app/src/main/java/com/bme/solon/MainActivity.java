package com.bme.solon;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
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

    private DrawerLayout navigationDrawer; //navigation pane parent view
    private FragmentManager fragmentManager; //the fragment manager
    /*
        current fragment being displayed
        0 = HomeFragment
        1 = ConnectFragment
        2 = AnalyticsFragment
     */
    private int currentFragment;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        if (currentFragment != 0) {
                            replaceFragment(new HomeFragment());
                            currentFragment = 0;
                        }
                        break;
                    case R.id.menu_connect:
                        if (currentFragment != 1) {
                            replaceFragment(new ConnectFragment());
                            currentFragment = 1;
                        }
                        break;
                    case R.id.menu_analytics:
                        if (currentFragment != 2) {
                            replaceFragment(new AnalyticsFragment());
                            currentFragment = 2;
                        }
                        break;
                }
                navigationDrawer.closeDrawers();
                return true;
            }
        });
        fragmentManager = getSupportFragmentManager();

        //Add HomeFragment as the initial fragment
        currentFragment = 0;
        fragmentManager.beginTransaction().add(R.id.main_fragment_view, new HomeFragment()).commit();
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
     *
     * @param fragment      The fragment to replace with
     * @return              True if successful, false otherwise
     */
    private boolean replaceFragment (Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.main_fragment_view, fragment).commit();
        return true;
    }
}
