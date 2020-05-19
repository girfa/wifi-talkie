package com.girfa.apps.wifitalkie.service;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.girfa.api.Packet;

public class AudioPlayer {
	public static String TAG = AudioPlayer.class.getSimpleName();
	static final int SAMPLE_RATE = 44100;
	static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
	static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	static final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE,
			CHANNEL_CONFIG, AUDIO_FORMAT);

	private LongSparseArray<AudioTrack> tracks = new LongSparseArray<AudioTrack>();

	void play(final byte[] mac, final byte[] audio) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				long id = Packet.bytesToLong(mac, Long.SIZE);
				if (tracks.indexOfKey(id) < 0) {
					AudioTrack track = new AudioTrack(
							android.media.AudioManager.STREAM_MUSIC,
							SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT,
							BUFFER_SIZE, AudioTrack.MODE_STREAM);
					tracks.put(id, track);
					Log.i(TAG, "BUFFER_SIZE: " + BUFFER_SIZE);
				}
				AudioTrack track = tracks.get(id);
				if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
					track.play();
				}
				track.write(audio, 0, audio.length);
				Log.i(TAG, "length: " + audio.length);
			}
		};
		new Thread(run).start();
	}
}
