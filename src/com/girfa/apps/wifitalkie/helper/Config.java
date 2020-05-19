package com.girfa.apps.wifitalkie.helper;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.girfa.api.Packet;
import com.girfa.api.Utils;
import com.girfa.apps.Preferences;
import com.girfa.apps.wifitalkie.R;
import com.girfa.apps.wifitalkie.model.Talkie;

public class Config {
	public static final long LOCATION_INTERVAL = 10000;
	public static final long PING_INTERVAL = 30000;
	private static final boolean SHOW_OFFLINE = false;
	public static final int DEFAULT_PORT = 31354;
	public static final long TIMEOUT = 60000;
	public static final int UDP_MAX_SIZE = 1024;

	private static Talkie me;

	public static Talkie me() {
		if (me == null)
			me = new Talkie().setDevice(Utils.toUpperFirst(Build.MANUFACTURER))
					.setModel(Build.MODEL);
		return me;
	}

	public static Read read(Context context) {
		return new Read(context);
	}

	public static Write write(Context context) {
		return new Write(context);
	}

	public static class Read {
		private Preferences pref;
		private Resources res;

		private Read(Context context) {
			res = context.getResources();
			pref = new Preferences(context);
		}

		public Talkie me() {
			Config.me().setName(
					pref.getString(res.getString(R.string.key_name),
							res.getString(R.string.def_name)));
			Config.me().setStatus(
					pref.getString(res.getString(R.string.key_status),
							res.getString(R.string.def_status)));
			String keyMac = res.getString(R.string.key_mac);
			if (pref.contains(keyMac)) {
				Config.me().setMAC(Packet.longToBytes(pref.getLong(keyMac, 0)));
			}
			return Config.me();
		}

		public boolean showOffline() {
			return pref.getBoolean(res.getString(R.string.key_show),
					Config.SHOW_OFFLINE);
		}

		public boolean toggle() {
			return pref.getBoolean(res.getString(R.string.key_toggle), false);
		}

		public boolean wifi() {
			return pref.getBoolean(res.getString(R.string.key_wifi), false);
		}

		public int port() {
			return Integer.valueOf(pref.getString(
					res.getString(R.string.key_port),
					String.valueOf(DEFAULT_PORT)));
		}
		
		public boolean ptt() {
			return pref.getBoolean(res.getString(R.string.key_ptt), true);
		}
	}

	public static class Write {
		private Preferences pref;
		private Resources res;

		private Write(Context context) {
			res = context.getResources();
			pref = new Preferences(context);
		}

		public void me(byte[] mac) {
			String keyMac = res.getString(R.string.key_mac);
			if (mac != null && mac.length > 0 && !pref.contains(keyMac)) {
				pref.setLong(keyMac, Packet.bytesToLong(mac, Long.SIZE));
			}
		}

		public void showOffline(boolean is) {
			pref.setBoolean(res.getString(R.string.key_show), is);
		}

		public void toggle(boolean on) {
			pref.setBoolean(res.getString(R.string.key_toggle), on);
		}

		public void wifi(boolean is) {
			pref.setBoolean(res.getString(R.string.key_wifi), is);
		}

		public void port(int port) {
			pref.setInt(res.getString(R.string.key_port), port);
		}
		
		public void ptt(boolean is) {
			pref.setBoolean(res.getString(R.string.key_ptt), is);
		}
	}
}