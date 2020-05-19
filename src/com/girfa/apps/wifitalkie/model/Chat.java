package com.girfa.apps.wifitalkie.model;

import java.util.ArrayList;
import java.util.List;

import com.girfa.api.Packet;

public class Chat extends Packet {
	private int id;
	private Talkie from;
	private Talkie to;
	private String message;
	private long when;

	public Chat() {
	}

	public Chat(byte[] data) {
		write(data);
	}

	protected void onRead() {
		putInt(id);
		putPacket(from);
		putPacket(to);
		putString(message);
		putLong(when);
	}

	protected void onWrite() {
		id = getInt();
		from = ((Talkie) getPacket(Talkie.class));
		to = ((Talkie) getPacket(Talkie.class));
		message = getString();
		when = getLong();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getWhen() {
		return when;
	}

	public void setWhen(long when) {
		this.when = when;
	}

	public enum Column {
		_id(0x1), t_from(0x2), t_to(0x4), message(0x8), s_when(0x10);

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