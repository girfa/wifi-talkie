package com.girfa.apps.wifitalkie;

import com.girfa.apps.wifitalkie.helper.Config;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Settings extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT < 11) {
			startActivity(new Intent(this, SettingsActivity.class));
			finish();
		} else {
			setContentView(R.layout.fragment);
			android.support.v7.app.ActionBar ab = getSupportActionBar();
			ab.setDisplayHomeAsUpEnabled(true);
			getFragmentManager().beginTransaction()
					.replace(R.id.fragment, new SettingsFragment()).commit();
		}

	}

	private static void updateSummary(Context ctx, Preference pref) {
		if (pref.getKey().equals(ctx.getString(R.string.key_name))) {
			pref.setSummary(Config.read(ctx).me().getName());
		} else if (pref.getKey().equals(ctx.getString(R.string.key_status))) {
			pref.setSummary(Config.read(ctx).me().getStatus());
		} else if (pref.getKey().equals(ctx.getString(R.string.key_port))) {
			pref.setSummary(String.valueOf(Config.read(ctx).port()));
		}

	}

	public static class SettingsActivity extends PreferenceActivity implements
			OnSharedPreferenceChangeListener {
		private SharedPreferences shared;

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (Build.VERSION.SDK_INT >= 11) {
				ActionBar ab = getActionBar();
				ab.setDisplayHomeAsUpEnabled(true);
			}
			addPreferencesFromResource(R.xml.settings);
			shared = PreferenceManager.getDefaultSharedPreferences(this);
			updateSummary(this, findPreference(getString(R.string.key_name)));
			updateSummary(this, findPreference(getString(R.string.key_status)));
			updateSummary(this, findPreference(getString(R.string.key_port)));
		}

		@Override
		protected void onResume() {
			super.onResume();
			shared.registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		protected void onPause() {
			shared.unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			finish();
			return super.onOptionsItemSelected(item);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			updateSummary(this, findPreference(key));
		}

	}

	public static class SettingsFragment extends PreferenceFragment implements
			OnSharedPreferenceChangeListener {
		private SharedPreferences shared;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
			shared = PreferenceManager.getDefaultSharedPreferences(getActivity());
			updateSummary(getActivity(),
					findPreference(getString(R.string.key_name)));
			updateSummary(getActivity(),
					findPreference(getString(R.string.key_status)));
			updateSummary(getActivity(),
					findPreference(getString(R.string.key_port)));
		}

		@Override
		public void onResume() {
			super.onResume();
			shared.registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			shared.unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			updateSummary(getActivity(), findPreference(key));
		}

	}

}
