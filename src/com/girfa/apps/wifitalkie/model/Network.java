package com.girfa.apps.wifitalkie.model;

import java.util.ArrayList;
import java.util.List;

public class Network {
	private String name;
	private byte[] address;
	private byte[] broadcast;
	private short prefix;
	private int nodes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getAddress() {
		return address;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}

	public byte[] getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(byte[] broadcast) {
		this.broadcast = broadcast;
	}

	public short getPrefix() {
		return prefix;
	}

	public void setPrefix(short prefix) {
		this.prefix = prefix;
	}

	public int getNodes() {
		return nodes;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public enum Column {
		_id(0x1), name(0x2), address(0x4), broadcast(0x8), prefix(0x10), nodes(
				0x20);

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

}