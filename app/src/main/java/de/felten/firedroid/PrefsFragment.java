package de.felten.firedroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private EditTextPreference server;
	private EditTextPreference port;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		server = (EditTextPreference)getPreferenceScreen().findPreference("prefs_server_name_key");
		port = (EditTextPreference)getPreferenceScreen().findPreference("prefs_server_port_key");
	}

	@Override
	public void onResume() {
        super.onResume();

        // Setup the initial values
        server.setSummary(getPreferenceScreen().getSharedPreferences().getString("prefs_server_name_key", ""));
        port.setSummary(getPreferenceScreen().getSharedPreferences().getString("prefs_server_port_key", ""));

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
	public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	    // Let's do something a preference value changes
        if (key.equals("prefs_server_name_key")) {
        	server.setSummary(sharedPreferences.getString("prefs_server_name_key", ""));
        }
        else if (key.equals("prefs_server_port_key")) {
        	port.setSummary(sharedPreferences.getString("prefs_server_port_key", ""));
        }
	}






}