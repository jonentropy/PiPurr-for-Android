package org.canthack.tris.pipurr.client;

/**
 * Created by tristan on 23/09/2014.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private final static String PIPURR_SETTING_LOCATION = "location";

    private EditTextPreference mLocationPref;

    //Settings
    public static String getLocation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PIPURR_SETTING_LOCATION, context.getString(R.string.http));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        mLocationPref = (EditTextPreference) getPreferenceScreen().findPreference(PIPURR_SETTING_LOCATION);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(PIPURR_SETTING_LOCATION)) {
            mLocationPref.setSummary(prefs.getString(key, ""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLocationPref.setSummary(mLocationPref.getText());

        // Set up a listener whenever a setting changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a setting changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}