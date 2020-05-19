package com.girfa.apps.wifitalkie.model;

import java.util.ArrayList;
import java.util.List;

public class File {
	private Talkie from;
	private Talkie to;
	private String location;
	private long when;

	public Talkie getFrom() {
		return from;
	}

	public void setFrom(Talkie from) {
		this.from = from;
	}

	public Talkie getTo() {
		return to;
	}

	public void setTo(Talkie to) {
		this.to = to;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getWhen() {
		return when;
	}

	public void setWhen(long when) {
		this.when = when;
	}

	public enum Column {
		_id(0x1), t_from(0x2), t_to(0x4), location(0x8), s_when(0x10);

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