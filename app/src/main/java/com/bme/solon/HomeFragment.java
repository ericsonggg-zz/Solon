package com.bme.solon;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends MainFragment {
    private static final String TAG = "HomeFragment";

    /**
     * Required empty constructor
     */
    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        //Set version name
        try {
            PackageInfo pinfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            TextView version = fragmentView.findViewById(R.id.home_version);
            version.setText(String.format(getString(R.string.home_version), pinfo.versionName));
        } catch (PackageManager.NameNotFoundException | NullPointerException ignored) {}

        return fragmentView;
    }

    @Override
    protected void receiveBroadcast(Intent intent) {
    }
}
