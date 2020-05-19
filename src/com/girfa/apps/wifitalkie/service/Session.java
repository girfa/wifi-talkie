package com.girfa.apps.wifitalkie.service;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Iterator;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.girfa.api.Packet;
import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.db.TalkieDB;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.helper.Helper;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Header;
import com.girfa.apps.wifitalkie.model.Talkie;

public class Session implements Runnable, Networker {
	private static final String TAG = Session.class.getSimpleName();
	private Context ctx;
	private MainService.Connector mCon;
	private TalkieDB tdb;

	Session(Context context, MainService.Connector con) {
		ctx = context;
		tdb = new TalkieDB(context);
		mCon = con;
	}

	public void start(boolean main) {
		tdb.openWrite();
		if (main) {
			new Thread(this).start();
		}
	}

	public void run() {
		boolean macFound = Config.read(ctx).me().getMAC() != null;
		if (macFound)
			addSelf();
		DatagramSocket socket;
		Enumeration<NetworkInterface> nis;
		try {
			socket = new DatagramSocket();
			nis = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		while (nis.hasMoreElements()) {
			NetworkInterface ni = nis.nextElement();
			try {
				Log.i(TAG,
						" name:" + ni.getName() + " mac:"
								+ Utils.bytesToHex(ni.getHardwareAddress())
								+ " loop:" + ni.isLoopback() + " up:"
								+ ni.isUp());
			} catch (SocketException e) {
				e.printStackTrace();
			}
			try {
				if (ni.isLoopback())
					continue;
				if (!ni.isUp())
					continue;
				if (ni.getName() == null)
					continue;
				if (!ni.getName().contains("ap")
						&& !ni.getName().contains("wlan"))
					continue;
				Config.write(ctx).me(ni.getHardwareAddress());
				if (!macFound)
					addSelf();
			} catch (SocketException e) {
				e.printStackTrace();
				continue;
			}
			Iterator<InterfaceAddress> ifas = ni.getInterfaceAddresses()
					.iterator();
			while (ifas.hasNext()) {
				InterfaceAddress ifa = ifas.next();
				InetAddress ia = ifa.getAddress();
				if (ia == null || ia.isLoopbackAddress())
					continue;
				int pre = ifa.getNetworkPrefixLength();
				int pos = 32 - pre;
				if (pre > 32)
					continue;
				int ip = (int) Packet.bytesToLong(ia.getAddress(), Long.SIZE);
				int from = ip >> pos << pos;
				int to = ip | -1 >>> pre;
				for (int i = from + 1; i < to; i++) {
					if (i != ip)
						sendSYN(socket, ByteBuffer.allocate(4).putInt(i)
								.array());
				}
			}
		}
		mCon.send(Command._START, null);
	}

	private void addSelf() {
		try {
			tdb.add(Config.me());
		} catch (SQLiteException e) {
			Config.me().setLast(System.currentTimeMillis());
			tdb.update(Config.me());
		}
	}

	private void sendSYN(Header header) {
		try {
			DatagramSocket socket = new DatagramSocket();
			sendSYN(socket, header.getIP());
			socket.close();
			Log.w(TAG, "sendSYN." + Utils.bytesToHex(header.getMAC()));
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void sendSYN(DatagramSocket socket, byte[] ip) {
		byte[] data = Helper.header(Command.SYN);
		Helper.sendUDP(ctx, socket, ip, data);
	}

	void sendACK(Header header) {
		byte[] head = Helper.header(Command.ACK);
		byte[] me = Config.read(ctx).me().read();
		Log.i(TAG, "sendACK." + Config.read(ctx).me().toString());
		byte[] data = new byte[head.length + me.length];
		ByteBuffer.wrap(data).put(head).put(me);
		try {
			DatagramSocket socket = new DatagramSocket();
			Helper.sendUDP(ctx, socket, header.getIP(), data);
			socket.close();
			Log.i(TAG, "sendACK." + Utils.bytesToHex(data));
			Talkie talkie = tdb.state(header.getMAC());
			if (talkie == null || !talkie.isOnline()) {
				sendSYN(header);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	void saveACK(Header header, byte[] data) {
		try {
			Talkie talkie = new Talkie(data);
			talkie.setMAC(header.getMAC()).setIP(header.getIP())
					.setSince(System.currentTimeMillis())
					.setLast(System.currentTimeMillis());
			Log.i(TAG, "saveACK." + talkie.toString());
			try {
				tdb.add(talkie);
				Log.w(TAG, "saveACK." + Utils.bytesToHex(header.getMAC()));
			} catch (SQLiteException e) {
				tdb.update(talkie);
			}
			mCon.send(Command.ACK, null);
		} catch (IllegalArgumentException e) {
			sendSYN(header);
			e.printStackTrace();
		}
	}

	boolean state(Header header) {
		Talkie talkie = tdb.state(header.getMAC());
		if (talkie != null && talkie.isOnline()) {
			online(header);
			return true;
		}
		sendSYN(header);
		return false;
	}

	void offline(Header header) {
		Talkie talkie = new Talkie().setMAC(header.getMAC());
		tdb.offline(talkie);
		mCon.send(Command.END, null);
		Log.w(TAG, "offline." + Utils.bytesToHex(header.getMAC()));
	}

	void online(Header header) {
		Talkie talkie = new Talkie().setMAC(header.getMAC())
				.setIP(header.getIP()).setLatitude(header.getLatitude())
				.setLongitude(header.getLongitude());
		tdb.online(talkie);
		Log.w(TAG, "online." + Utils.bytesToHex(header.getMAC()));
	}

	public void stop(boolean main) {
		if (main) {
			new Thread(new Runnable() {
				public void run() {
					byte[] data = Helper.header(Command.END);
					try {
						tdb.openWrite();
						TalkieDB.TalkieResult list = tdb.listIPs();
						if (list.size() >= 0) {
							DatagramSocket socket = new DatagramSocket();
							for (int i = 0; i < list.size(); i++) {
								Talkie talkie = list.get(i);
								Helper.sendUDP(ctx, socket, talkie.getIP(),
										data);
								tdb.offline(talkie);
							}
							tdb.offline(Config.me());
							socket.close();
							Log.w(TAG, "END." + Utils.bytesToHex(data));
						}
						list.close();
					} catch (SocketException e) {
						e.printStackTrace();
					}
					mCon.send(Command._STOP, null);
				}
			}).start();
		}
		tdb.close();
	}
}