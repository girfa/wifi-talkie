package com.girfa.apps.wifitalkie.helper;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import com.girfa.apps.wifitalkie.model.Command;

public abstract class Broadcast extends BroadcastReceiver {
	private static final String WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	private Context ctx;
	private LocalBroadcastManager lbm;
	private Class<?> me;
	private Command last;
	private WifiManager wm;

	public Broadcast(Context context, Class<?> dest) {
		ctx = context;
		lbm = LocalBroadcastManager.getInstance(context);
		me = dest;
		wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
	}

	public void register() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WIFI_AP_STATE_CHANGED);
		ctx.registerReceiver(this, filter);
		lbm.registerReceiver(this, new IntentFilter(Broadcast.class.getName()));
	}

	public void unregister() {
		lbm.unregisterReceiver(this);
		ctx.unregisterReceiver(this);
	}

	public void send(Command cmd, Intent intent) {
		if (Command._WIFI_TURN_ON.equals(cmd)) {
			wm.setWifiEnabled(true);
			return;
		} else if (Command._WIFI_CONNECT.equals(cmd)) {
			ctx.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			return;
		}
		if (intent == null) {
			intent = new Intent();
		}
		intent.setAction(Broadcast.class.getName());
		intent.putExtra(Broadcast.class.getName(), me.getName());
		intent.putExtra(Command.class.getName(), cmd.id);
		lbm.sendBroadcast(intent);
	}

	public abstract void onReceive(Command cmd, Intent intent);

	@Override
	public void onReceive(Context context, Intent intent) {
		if (me.getName().equals(
				intent.getStringExtra(Broadcast.class.getName())))
			return;
		Command cmd = Command.valueOf(intent.getIntExtra(
				Command.class.getName(), 0));
		String action = intent.getAction();
		if (Command._WIFI_STATUS.equals(cmd)
				|| WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)
				|| WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
				|| WIFI_AP_STATE_CHANGED.equals(action)) {
			NetworkInfo ni = ((ConnectivityManager) ctx
					.getSystemService(Context.CONNECTIVITY_SERVICE))
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wm.isWifiEnabled()) {
				if (ni.isConnected()) {
					cmd = Command._WIFI_ON_CONNECTED;
				} else {
					cmd = Command._WIFI_ON_DISCONNECTED;
				}
			} else {
				cmd = Command._WIFI_OFF;
				for (Method method : wm.getClass().getDeclaredMethods()) {
					if (method.getName().equals("isWifiApEnabled")) {
						try {
							if ((Boolean) method.invoke(wm)) {
								cmd = Command._WIFI_ON_CONNECTED;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			Config.write(ctx).wifi(Command._WIFI_ON_CONNECTED.equals(cmd));
			if (cmd.equals(last)) {
				return;
			} else {
				last = cmd;
			}
		}
		onReceive(cmd, intent);
	}
}
