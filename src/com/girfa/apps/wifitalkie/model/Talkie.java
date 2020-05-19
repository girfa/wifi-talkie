package com.girfa.apps.wifitalkie.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.girfa.api.Packet;
import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.helper.Config;

public class Talkie extends Packet {
	private int id;
	private byte[] mac;
	private String name;
	private byte[] photo;
	private String status;
	private String device;
	private String model;
	private long since;
	private byte[] ip;
	private double latitude;
	private double longitude;
	private long last;
	private boolean online;
	private int field;

	public Talkie() {
	}

	public Talkie(byte[] data) {
		write(data);
	}

	protected void onRead() {
		putInt(id);
		put(mac);
		putString(name);
		put(photo);
		putString(status);
		putString(device);
		putString(model);
		putLong(since);
		put(ip);
		putDouble(latitude);
		putDouble(longitude);
		putLong(last);
		putBoolean(online);
		putInt(field);
	}

	protected void onWrite() {
		id = getInt();
		mac = get();
		name = getString();
		photo = get();
		status = getString();
		device = getString();
		model = getString();
		since = getLong();
		ip = get();
		latitude = getDouble();
		longitude = getDouble();
		last = getLong();
		online = getBoolean();
		field = getInt();
	}

	public boolean equals(Talkie talkie) {
		byte[] tMac = talkie.getMAC();
		if (mac == null || talkie == null || talkie.getMAC() == null)
			return false;
		return Arrays.equals(mac, tMac);
	}

	public int getId() {
		return id;
	}

	public Talkie setId(int id) {
		this.id = id;
		field |= Column._id.id;
		return this;
	}

	public byte[] getMAC() {
		return mac;
	}

	public Talkie setMAC(byte[] mac) {
		this.mac = mac;
		field |= Column.mac.id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Talkie setName(String name) {
		this.name = name;
		field |= Column.name.id;
		return this;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public Talkie setPhoto(byte[] photo) {
		this.photo = photo;
		field |= Column.photo.id;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public Talkie setStatus(String status) {
		this.status = status;
		field |= Column.status.id;
		return this;
	}

	public String getDevice() {
		return device;
	}

	public Talkie setDevice(String device) {
		this.device = device;
		field |= Column.device.id;
		return this;
	}

	public String getModel() {
		return model;
	}

	public Talkie setModel(String model) {
		this.model = model;
		field |= Column.model.id;
		return this;
	}

	public long getSince() {
		return since;
	}

	public Talkie setSince(long since) {
		this.since = since;
		field |= Column.since.id;
		return this;
	}

	public byte[] getIP() {
		return ip;
	}

	public Talkie setIP(byte[] ip) {
		this.ip = ip;
		field |= Column.ip.id;
		return this;
	}

	public double getLatitude() {
		return latitude;
	}

	public Talkie setLatitude(double latitude) {
		this.latitude = latitude;
		field |= Column.latitude.id;
		return this;
	}

	public double getLongitude() {
		return longitude;
	}

	public Talkie setLongitude(double longitude) {
		this.longitude = longitude;
		field |= Column.longitude.id;
		return this;
	}

	public long getLast() {
		return last;
	}

	public Talkie setLast(long last) {
		this.last = last;
		field |= Column.last.id;
		return this;
	}

	public int getField() {
		return field;
	}

	public boolean isOnline() {
		return System.currentTimeMillis() < Config.TIMEOUT + this.last;
	}

	public enum Column {
		_id(0x1), mac(0x2), name(0x4), photo(0x8), status(0x10), device(0x20), model(
				0x40), since(0x80), ip(0x100), latitude(0x200), longitude(0x400), last(
				0x800);

		Column(int id) {
			this.id = id;
		}

		public int id;

		public static int all() {
			int val = 0;
			for (Column c : values()) {
				val |= c.id;
			}
			return val;
		}

		public static String[] namesOf(int i) {
			List<String> names = new ArrayList<String>();
			for (Column c : values()) {
				if ((c.id & i) != 0)
					names.add(c.toString());
			}
			return names.toArray(new String[names.size()]);
		}

		public static Column[] valuesOf(int i) {
			List<Column> names = new ArrayList<Column>();
			for (Column c : values()) {
				if ((c.id & i) != 0)
					names.add(c);
			}
			return names.toArray(new Column[names.size()]);
		}
	}

	public String toString() {
		return "id:" + this.id + " mac:" + Utils.bytesToHex(this.mac)
				+ " name:" + this.name + " photo:" + this.photo + " status:"
				+ this.status + " device:" + this.device + " model:"
				+ this.model + " since:" + this.since + " ip:"
				+ Utils.bytesToHex(this.ip) + " latitude:" + this.latitude
				+ " longitude:" + this.longitude + " last:" + this.last
				+ " online:" + this.online;
	}
}