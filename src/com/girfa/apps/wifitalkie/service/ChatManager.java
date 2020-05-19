package com.girfa.apps.wifitalkie.service;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.db.ChatDB;
import com.girfa.apps.wifitalkie.db.TalkieDB;
import com.girfa.apps.wifitalkie.db.TalkieDB.TalkieResult;
import com.girfa.apps.wifitalkie.helper.Helper;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Chat;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Header;
import com.girfa.apps.wifitalkie.model.Talkie;

class ChatManager implements Networker {
	public static final String TAG = ChatManager.class.getSimpleName();
	private Context ctx;
	private MainService.Connector mCon;
	private TalkieDB tdb;
	private ChatDB cdb;

	ChatManager(Context context, MainService.Connector con) {
		ctx = context;
		mCon = con;
		tdb = new TalkieDB(context);
		cdb = new ChatDB(context);
	}

	public void start(boolean main) {
		tdb.openRead();
		cdb.openWrite();
	}

	void sendCHAT(final int id) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				Chat chat = cdb.get(id);
				if (chat == null || Utils.isEmpty(chat.getMessage()))
					return;
				byte[] header = Helper.header(Command.CHAT);
				byte[] raw = chat.read();
				byte[] data = new byte[header.length + raw.length];
				ByteBuffer.wrap(data).put(header).put(raw);
				Talkie to = chat.getTo();
				try {
					DatagramSocket socket = new DatagramSocket();
					if (to == null) {
						TalkieResult result = tdb.listIPs();
						for (int i = 0; i < result.size(); i++) {
							Helper.sendUDP(ctx, socket, result.get(i).getIP(),
									data);
						}
						result.close();
					} else {
						Helper.sendUDP(ctx, socket, to.getIP(), data);
					}
					socket.close();
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(run).start();
	}

	void saveCHAT(Header header, byte[] message) {
		Chat chat = new Chat(message);
		chat.setWhen(System.currentTimeMillis());
		try {
			cdb.add(chat);
			mCon.send(Command.CHAT, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
	}

	public void stop(boolean main) {
		cdb.close();
		tdb.close();
	}
}