package com.girfa.apps.wifitalkie.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.girfa.apps.wifitalkie.MainActivity;
import com.girfa.apps.wifitalkie.R;
import com.girfa.apps.wifitalkie.helper.Broadcast;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Chat;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Talkie;

public class MainService extends Service implements Networker {
	public static final int ID = 354313;
	public static final String TAG = MainService.class.getSimpleName();
	private boolean state = false;
	private Config.Read config;
	private Connector con;
	private NotificationCompat.Builder notif;
	private UDPServer udp;
	private TCPServer tcp;
	private Session ses;
	private Pinger ping;
	private ChatManager chat;
	private AudioManager audio;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "Service created");
		config = Config.read(this);
		con = new Connector();
		con.register();
		con.send(Command._WIFI_STATUS, null);
		udp = new UDPServer(this, con);
		tcp = new TCPServer(this, con);
		ses = new Session(this, con);
		ping = new Pinger(this, con);
		chat = new ChatManager(this, con);
		audio = new AudioManager(this, con);
		Intent intent = new Intent(this, MainActivity.class);
		TaskStackBuilder stack = TaskStackBuilder.create(this);
		stack.addParentStack(MainActivity.class);
		stack.addNextIntent(intent);
		PendingIntent pending = stack.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notif = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.nf_online)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.online)).setOngoing(true)
				.setContentIntent(pending);
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	@Override
	public void start(boolean main) {
		if (state) {
			return;
		}
		state = true;
		Log.e(TAG, "start");
		udp.start(true);
		tcp.start(true);
		ses.start(true);
		ping.start(true);
		chat.start(true);
		audio.start(true);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(ID, notif.build());
	}

	@Override
	public void stop(boolean main) {
		if (!state) {
			return;
		}
		state = false;
		Log.e(TAG, "stop");
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(ID);
		audio.start(true);
		chat.start(true);
		ping.stop(true);
		ses.stop(main);
		tcp.stop(true);
		udp.stop(true);
	}

	@Override
	public void onDestroy() {
		stop(true);
		con.unregister();
		super.onDestroy();
	}

	class Connector extends Broadcast {

		public Connector() {
			super(MainService.this, MainActivity.class);
		}

		public void onReceive(Command cmd, Intent intent) {
			switch (cmd) {
			case SYN:
			case ACK:
			case PONG:
				break;
			case CHAT:
				chat.sendCHAT(intent.getIntExtra(Chat.Column._id.toString(), 0));
				break;
			case AUDIO:
				break;
			case AUDIO_OFF:
				audio.stopStream();
				break;
			case AUDIO_ON:
				audio.startStream(intent.getByteArrayExtra(Talkie.Column.mac
						.toString()));
				break;
			case FILE:
			case FILE_OK:
				break;
			case _WIFI_ON_CONNECTED:
				if (config.toggle() && config.wifi())
					start(false);
				break;
			case _WIFI_OFF:
			case _WIFI_ON_DISCONNECTED:
				stop(false);
				break;
			case _START:
				if (config.wifi())
					start(true);
				break;
			case _STOP:
				stop(true);
				break;
			default:
				break;
			}
			Log.e(TAG, cmd + "");
		}
	}
}