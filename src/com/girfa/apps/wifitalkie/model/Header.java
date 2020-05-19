package com.girfa.apps.wifitalkie.model;

import com.girfa.api.Packet;

public class Header extends Packet {
	private Command command;
	private byte[] mac;
	private double latitude;
	private double longitude;
	private byte[] ip;

	public Header() {}

	public Header(byte[] paramArrayOfByte) {
		write(paramArrayOfByte);
	}

	@Override
	protected void onRead() {
		putInt(command.id);
		put(mac);
		putDouble(latitude);
		putDouble(longitude);
		put(ip);
	}

	@Override
	protected void onWrite() {
		command = Command.valueOf(getInt());
		mac = get();
		latitude = getDouble();
		longitude = getDouble();
		ip = get();
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public byte[] getMAC() {
		return mac;
	}

	public void setMAC(byte[] mac) {
		this.mac = mac;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public byte[] getIP() {
		return ip;
	}

	public void setIP(byte[] ip) {
		this.ip = ip;
	}
}