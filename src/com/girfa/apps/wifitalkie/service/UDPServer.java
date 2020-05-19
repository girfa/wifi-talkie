package com.girfa.apps.wifitalkie.service;

import android.content.Context;
import android.util.Log;
import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Header;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

class UDPServer implements Runnable, Networker {
	private static final String TAG = UDPServer.class.getSimpleName();
	private AudioManager audio;
	private Context ctx;
	private FileManager file;
	private Pinger ping;
	private Session ses;
	private ChatManager chat;
	private VideoManager video;
	private DatagramSocket socket;

	UDPServer(Context context, MainService.Connector con) {
		ctx = context;
		ping = new Pinger(context, con);
		ses = new Session(context, con);
		audio = new AudioManager(context, con);
		video = new VideoManager(context, con);
		chat = new ChatManager(context, con);
		file = new FileManager(context, con);
	}

	public void start(boolean main) {
		ping.start(false);
		ses.start(false);
		audio.start(false);
		video.start(false);
		chat.start(false);
		file.start(false);
		if (main)
			new Thread(this).start();
	}

	public void run() {
		try {
			socket = new DatagramSocket(Config.read(ctx).port());
			Log.e(TAG, "UDP listening");
			while (!socket.isClosed()) {
				try {
					byte[] raw = new byte[8092];
					DatagramPacket packet = new DatagramPacket(raw, raw.length);
					socket.receive(packet);
					byte[] data = new byte[packet.getLength()];
					System.arraycopy(raw, 0, data, 0, data.length);
					onReceive(packet.getAddress().getAddress(), data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	void onReceive(byte[] ip, byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		byte[] rawh = new byte[buffer.getShort()];
		buffer.get(rawh);
		Header header = new Header(rawh);
		header.setIP(ip);
		Log.e(TAG, "UDP Receive " + header.getCommand());
		byte[] message = new byte[buffer.remaining()];
		buffer.get(message);
		switch (header.getCommand()) {
		case SYN:
			ses.sendACK(header);
			break;
		case ACK:
			ses.saveACK(header, message);
			break;
		case PING:
			if (ses.state(header))
				ping.sendPONG(header);
			break;
		case AUDIO:
			if (ses.state(header))
				audio.playAUDIO(header, message);
			break;
		case VIDEO:
			if (ses.state(header))
				video.playVIDEO(header, message);
			break;
		case CHAT:
			if (ses.state(header))
				chat.saveCHAT(header, message);
			break;
		case FILE:
			if (ses.state(header))
				file.acceptFILE(header, message);
			break;
		case FILE_OK:
			if (ses.state(header))
				file.sendFILE(header, message);
			break;
		case END:
			ses.offline(header);
			break;
		default:
			break;
		}
	}

	public void stop(boolean main) {
		if (main) {
			socket.close();
		}
		file.stop(false);
		chat.stop(false);
		video.stop(false);
		audio.stop(false);
		ses.stop(false);
		ping.stop(false);
	}
}