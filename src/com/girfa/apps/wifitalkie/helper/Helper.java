package com.girfa.apps.wifitalkie.helper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import android.content.Context;

import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Header;
import com.girfa.apps.wifitalkie.model.Talkie;

public class Helper {
	public static byte[] header(Command paramCommand) {
		Header data = new Header();
		data.setCommand(paramCommand);
		Talkie localTalkie = Config.me();
		data.setMAC(localTalkie.getMAC());
		data.setLatitude(localTalkie.getLatitude());
		data.setLongitude(localTalkie.getLongitude());
		byte[] raw = data.read();
		if (raw == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.allocate(2 + raw.length);
		buffer.putShort((short) raw.length);
		buffer.put(raw);
		return buffer.array();
	}

	public static void sendUDP(Context context, DatagramSocket socket,
			byte[] ip, byte[] data) {
		if (ip == null)
			return;
		try {
			socket.send(new DatagramPacket(data, data.length, InetAddress
					.getByAddress(ip), Config.read(context).port()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}