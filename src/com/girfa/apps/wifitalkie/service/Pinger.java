package com.girfa.apps.wifitalkie.service;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.db.TalkieDB;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.helper.Helper;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Header;
import com.girfa.apps.wifitalkie.model.Talkie;

class Pinger implements Networker, LocationListener {
	private static final String TAG = Pinger.class.getSimpleName();
	private Context ctx;
	private LocationManager lm;
	private MainService.Connector mCon;
	private TalkieDB tdb;
	private Timer timer;

	Pinger(Context context, MainService.Connector con) {
		ctx = context;
		tdb = new TalkieDB(context);
		mCon = con;
	}

	public void start(boolean main) {
		tdb.openWrite();
		if (main) {
			timer = new Timer(true);
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					Pinger.this.pingAll();
				}
			}, Config.PING_INTERVAL, Config.PING_INTERVAL);
			lm = ((LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE));
			Criteria criteria = new Criteria();
			String provider = lm.getBestProvider(criteria, false);
			onLocationChanged(lm.getLastKnownLocation(provider));
			lm.requestLocationUpdates(provider, Config.LOCATION_INTERVAL, 1, this);
		}
	}

	void pingAll() {
		byte[] data = Helper.header(Command.PING);
		try {
			TalkieDB.TalkieResult list = tdb.listIPs();
			if (list.size() >= 0) {
				DatagramSocket socket = new DatagramSocket();
				for (int i = 0; i < list.size(); i++) {
					Talkie talkie = list.get(i);
					Helper.sendUDP(ctx, socket, talkie.getIP(), data);
					Log.w(TAG, "sendPING." + Utils.bytesToHex(talkie.getIP()));
					if (System.currentTimeMillis() > Config.TIMEOUT
							+ talkie.getLast()) {
						tdb.offline(talkie);
					}
				}
				tdb.online(Config.me());
				socket.close();
				mCon.send(Command.PING, null);
				Log.w(TAG, "pingAll." + Utils.bytesToHex(data));
			}
			list.close();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	void sendPONG(Header header) {
		byte[] data = Helper.header(Command.PONG);
		try {
			DatagramSocket socket = new DatagramSocket();
			Helper.sendUDP(ctx, socket, header.getIP(), data);
			socket.close();
			Log.w(TAG, "sendPONG." + Utils.bytesToHex(header.getIP()));
			return;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void stop(boolean main) {
		if (main) {
			timer.cancel();
			lm.removeUpdates(this);
		}
		tdb.close();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null) {
			return;
		}
		Config.me().setLatitude(location.getLatitude());
		Config.me().setLatitude(location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}