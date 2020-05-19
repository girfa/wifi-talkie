package com.girfa.apps.wifitalkie.model;

import android.util.SparseArray;

public enum Command {
	SYN(11), ACK(12), END(13),
	PING(21), PONG(22),
	AUDIO(31), AUDIO_ON(32), AUDIO_OFF(33),
	VIDEO(41), VIDEO_ON(42), VIDEO_OFF(43),
	CHAT(51),
	FILE(61), FILE_OK(62),
	_WIFI_STATUS(71), _WIFI_TURN_ON(72),_WIFI_CONNECT(73),
		_WIFI_ON_CONNECTED(74), _WIFI_ON_DISCONNECTED(75), _WIFI_OFF(76),
	_START(81), _STOP(82);

	public int id;
	private static final SparseArray<Command> array = new SparseArray<Command>();

	private Command(int id) {
		this.id = id;
	}

	static {
		for (Command value : Command.values()) {
			array.put(value.id, value);
		}
	}

	public static Command valueOf(int value) {
		return array.get(value);
	}
}