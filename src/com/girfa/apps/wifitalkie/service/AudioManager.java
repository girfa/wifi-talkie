package com.girfa.apps.wifitalkie.service;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioEncoder;

import com.girfa.apps.wifitalkie.helper.Networker;
import com.girfa.apps.wifitalkie.model.Header;

class AudioManager implements Networker {
	private Context ctx;
	private MainService.Connector mCon;
	private AudioStream stream;
	private AudioPlayer player;

	AudioManager(Context context, MainService.Connector con) {
		ctx = context;
		mCon = con;
		stream = new AudioStream(context);
		player = new AudioPlayer();
	}

	@Override
	public void start(boolean main) {

	}

	void playAUDIO(Header header, byte[] data) {
		player.play(header.getMAC(), data);
	}

	public void startStream(byte[] mac) {
		stream.start(mac);
	}

	public void stopStream() {
		stream.stop();
	}

	@Override
	public void stop(boolean main) {

	}
}