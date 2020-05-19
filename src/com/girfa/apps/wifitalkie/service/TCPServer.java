package com.girfa.apps.wifitalkie.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.content.Context;
import android.util.Log;

import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Header;

class TCPServer implements Runnable, Networker {
	private static final String TAG = TCPServer.class.getSimpleName();
	private Context ctx;
	private FileManager file;
	private Pinger ping;
	private ServerSocket server;
	private Session ses;

	TCPServer(Context context, MainService.Connector con) {
		ctx = context;
		ses = new Session(context, con);
		ping = new Pinger(context, con);
		file = new FileManager(context, con);
	}

	public void start(boolean main) {
		ses.start(false);
		ping.start(false);
		file.start(false);
		if (main)
			new Thread(this).start();
	}

	public void run() {
		try {
			server = new ServerSocket(Config.read(ctx).port());
			Log.e(TAG, "TCP listening");
			while (!server.isClosed()) {
				try {
					Socket socket = server.accept();
					onReceive(socket.getInetAddress().getAddress(),
							socket.getInputStream());
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void onReceive(byte[] ip, InputStream data) throws IOException {
		byte[] len = new byte[Short.SIZE / 8];
		data.read(len);
		byte[] raw = new byte[ByteBuffer.wrap(len).getShort()];
		data.read(raw);
		Header header = new Header(raw);
		header.setIP(ip);
		Log.e(TAG, "TCP Receive " + header.getCommand());
		if (!ses.state(header))
			return;
		switch (header.getCommand()) {
		case FILE:
			file.saveFILE(header, data);
			break;
		default:
			break;
		}
	}

	public void stop(boolean main) {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		file.stop(false);
		ping.stop(false);
		ses.stop(false);
	}
}