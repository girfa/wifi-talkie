package com.girfa.apps.wifitalkie.service;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.girfa.apps.wifitalkie.db.TalkieDB;
import com.girfa.apps.wifitalkie.db.TalkieDB.TalkieResult;
import com.girfa.apps.wifitalkie.helper.Helper;
import com.girfa.apps.wifitalkie.model.Command;
import com.girfa.apps.wifitalkie.model.Talkie;

public class AudioStream implements Runnable {
	static final int SAMPLE_RATE = 44100;
	static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
			CHANNEL_CONFIG, AUDIO_FORMAT);

	private static final String TAG = AudioStream.class.getSimpleName();
	private Context ctx;
	private TalkieDB tdb;
	private byte[] dest;
	private DatagramSocket socket;

	AudioStream(Context context) {
		ctx = context;
		tdb = new TalkieDB(context);
	}

	void start(byte[] mac) {
		dest = mac;
		new Thread(this).start();
	}

	@Override
	public void run() {
		Log.i(TAG, TAG + " started");
		tdb.openRead();
		List<Talkie> receiver = new ArrayList<Talkie>();
		AudioRecord recorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE,
				CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
		if (dest == null) {
			TalkieResult result = tdb.listen();
			if (result != null) {
				for (int n = 0; n < result.size(); n++) {
					receiver.add(result.get(n));
				}
				result.close();
			}
		} else {
			Talkie talkie = tdb.get(dest);
			if (talkie != null)
				receiver.add(talkie);
		}
		if (receiver.size() > 0) {
			try {
				socket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		if (socket != null) {
			Log.i(TAG, TAG + " socket initialized");
			Log.i(TAG, "BUFFER_SIZE: " + BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			recorder.startRecording();
			while (!socket.isClosed()) {
				int read = recorder.read(buffer, 0, buffer.length);
				Log.i(TAG, "length: " + read);
				byte[] raw = new byte[read];
				System.arraycopy(buffer, 0, raw, 0, read);
				byte[] head = Helper.header(Command.AUDIO);
				byte[] data = new byte[head.length + raw.length];
				ByteBuffer.wrap(data).put(head).put(raw);
				for (Talkie talkie : receiver) {
					Helper.sendUDP(ctx, socket, talkie.getIP(), data);
				}
			}
			recorder.stop();
		}
		receiver.clear();
		tdb.close();
		Log.i(TAG, TAG + " stopped");
	}

	void stop() {
		if (socket != null)
			socket.close();
	}

}
