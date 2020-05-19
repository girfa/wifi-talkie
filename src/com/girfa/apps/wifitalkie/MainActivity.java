package com.girfa.apps.wifitalkie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.girfa.apps.wifitalkie.fragment.ChatFragment;
import com.girfa.apps.wifitalkie.fragment.FilesFragment;
import com.girfa.apps.wifitalkie.fragment.TalkiesFragment;
import com.girfa.apps.wifitalkie.helper.Broadcast;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.service.MainService;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = MainActivity.class.getSimpleName();
	private ActionBar ab;
	private Connector con;
	private int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		con = new Connector();
		ab = getSupportActionBar();
		if (savedInstanceState != null)
			index = savedInstanceState.getInt(TAG);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		MenuItem item = menu.findItem(R.id.toggle);
		if(Config.read(this).toggle()) {
			item.setIcon(R.drawable.ab_stop);
			item.setTitle(R.string.stop);
		} else {
			item.setIcon(R.drawable.ab_start);
			item.setTitle(R.string.start);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		con.register();
		con.send(Command._WIFI_STATUS, null);
		super.onResume();
	}

	@Override
	protected void onPause() {
		con.unregister();
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.toggle:
			if(Config.read(this).toggle()) {
				Config.write(this).toggle(false);
				item.setIcon(R.drawable.ab_start);
				item.setTitle(R.string.start);
				con.send(Command._STOP, null);
				Log.e(TAG, "stop");
			} else {
				Config.write(this).toggle(true);
				item.setIcon(R.drawable.ab_stop);
				item.setTitle(R.string.stop);
				con.send(Command._START, null);
				Log.e(TAG, "start");
			}
			return true;
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.help:
			startActivity(new Intent(this, Help.class));
			return true;
		case R.id.about:
			startActivity(new Intent(this, About.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(TAG, index);
		super.onSaveInstanceState(outState);
	}

	private void wifi(Command cmd) {
		if (Command._WIFI_ON_CONNECTED.equals(cmd)) {
			if (ab.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
				return;
			setContentView(R.layout.fragment);
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			Tab talkies = ab.newTab().setText(R.string.talkies)
					.setTabListener(new TabControl(TalkiesFragment.create()));
			Tab chat = ab.newTab().setText(R.string.chat)
					.setTabListener(new TabControl(ChatFragment.create()));
			Tab files = ab.newTab().setText(R.string.files)
					.setTabListener(new TabControl(FilesFragment.create()));
			ab.addTab(talkies, 0, index == 0);
			ab.addTab(chat, 1, index == 1);
			ab.addTab(files, 2, index == 2);
		} else {
			ab.removeAllTabs();
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			setContentView(R.layout.wifi_switch);
			Button ws = (Button) findViewById(R.id.wifi_switch);
			if (Command._WIFI_OFF.equals(cmd)) {
				ws.setText(R.string.ws_turnon);
				ws.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						con.send(Command._WIFI_TURN_ON, null);
						setContentView(R.layout.loading);
					}
				});
			} else if (Command._WIFI_ON_DISCONNECTED.equals(cmd)) {
				ws.setText(R.string.ws_connect);
				ws.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						con.send(Command._WIFI_CONNECT, null);
						setContentView(R.layout.loading);
					}
				});
			}
		}
	}

	private class TabControl implements TabListener {
		private Fragment mFragment;

		public TabControl(Fragment fragment) {
			mFragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			index = tab.getPosition();
			ft.replace(R.id.fragment, mFragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(mFragment);
		}

	}

	private class Connector extends Broadcast {

		public Connector() {
			super(MainActivity.this, MainService.class);
		}

		@Override
		public void onReceive(Command cmd, Intent intent) {
			switch (cmd) {
			case _WIFI_OFF:
			case _WIFI_ON_DISCONNECTED:
			case _WIFI_ON_CONNECTED:
				wifi(cmd);
				break;
			default:
				break;
			}
		}
	}

}
