package com.girfa.apps.wifitalkie.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.model.Chat;
import com.girfa.apps.wifitalkie.model.Chat.Column;
import com.girfa.apps.wifitalkie.model.Talkie;

public class ChatDB extends Database {
	public static final String TAG = ChatDB.class.getSimpleName();
	static final String DB_TABLE = "chat";
	private static final String FROM = "from_";
	private static final String TO = "to_";

	public ChatDB(Context context) {
		super(context);
	}

	public long add(Chat chat) {
		if (chat == null)
			return 0;
		return db.insertOrThrow(DB_TABLE, null,
				toCV(chat, Column.valuesOf(Column.all() | ~Column._id.id)));
	}

	public int remove(Chat chat) {
		if (chat == null)
			return 0;
		return db.delete(DB_TABLE, Column._id + " = ?",
				new String[] { String.valueOf(chat.getId()) });
	}

	public ChatResult gets(boolean showOffline) {
		int flags = Talkie.Column.mac.id | Talkie.Column.name.id
				| Talkie.Column.device.id | Talkie.Column.model.id
				| Talkie.Column.photo.id | Talkie.Column.last.id;
		String from = "";
		String to = "";
		for (Talkie.Column col : Talkie.Column.valuesOf(flags)) {
			from += ", f.[" + col + "] [" + FROM + col + "]";
			to += ", t.[" + col + "] [" + TO + col + "]";
		}
		String query = "SELECT c.*" + from + to + " FROM [" + DB_TABLE + "] c" //
				+ " LEFT JOIN [" + TalkieDB.DB_TABLE + "] f" //
				+ " ON c.[" + Column.t_from + "] = f.[" + Talkie.Column.mac + "]" //
				+ " LEFT JOIN [" + TalkieDB.DB_TABLE + "] t" //
				+ " ON c.[" + Column.t_to + "] = t.[" + Talkie.Column.mac + "]" //
				+ " WHERE " + (showOffline ? 1 : 0) //
				+ " OR " + System.currentTimeMillis() //
				+ " < f.[" + Talkie.Column.last + "] + " + Config.TIMEOUT //
				+ " ORDER BY c.[" + Column._id + "]";
		try {
			Cursor cs = db.rawQuery(query, null);
			return new ChatResult(cs);
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ChatResult gets(Talkie with) {
		if (with == null || with.getMAC() == null)
			return null;
		int flags = Talkie.Column.mac.id | Talkie.Column.name.id
				| Talkie.Column.device.id | Talkie.Column.model.id
				| Talkie.Column.photo.id | Talkie.Column.last.id;
		String from = "";
		String to = "";
		for (Talkie.Column col : Talkie.Column.valuesOf(flags)) {
			from += ", f.[" + col + "] [" + FROM + col + "]";
			to += ", t.[" + col + "] [" + TO + col + "]";
		}
		String query = "SELECT c.*" + from + to + " FROM [" + DB_TABLE + "] c" //
				+ " LEFT JOIN [" + TalkieDB.DB_TABLE + "] f" //
				+ " ON c.[" + Column.t_from + "] = f.[" + Talkie.Column.mac + "]" //
				+ " LEFT JOIN [" + TalkieDB.DB_TABLE + "] t" //
				+ " ON c.[" + Column.t_to + "] = t.[" + Talkie.Column.mac + "]" //
				+ " WHERE f.[" + Talkie.Column.mac + "]" //
				+ " = x'" + Utils.bytesToHex(with.getMAC()) + "'" //
				+ " ORDER BY c.[" + Column._id + "]";
		try {
			Cursor cs = db.rawQuery(query, null);
			return new ChatResult(cs);
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Chat get(int id) {
		String from = "";
		String to = "";
		int flags = Talkie.Column.mac.id | Talkie.Column.name.id
				| Talkie.Column.device.id | Talkie.Column.model.id
				| Talkie.Column.photo.id | Talkie.Column.last.id;
		for (Talkie.Column col : Talkie.Column.valuesOf(flags)) {
			from += ", f.[" + col + "] [" + FROM + col + "]";
			to += ", t.[" + col + "] [" + TO + col + "]";
		}
		String query = "SELECT c.*" + from + to + " FROM [" + DB_TABLE + "] c" //
				+ " LEFT JOIN [" + TalkieDB.DB_TABLE + "] f" //
				+ " ON c.[" + Column.t_from + "] = f.[" + Talkie.Column.mac + "]" //
				+ " LEFT JOIN [" + TalkieDB.DB_TABLE + "] t" //
				+ " ON c.[" + Column.t_to + "] = t.[" + Talkie.Column.mac + "]" //
				+ " WHERE c.[" + Column._id + "] = ?"; //
		try {
			Cursor cs = db.rawQuery(query, new String[] { String.valueOf(id) });
			return new ChatResult(cs).get();
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class ChatResult {
		private Cursor cs;

		public ChatResult(Cursor cursor) {
			cs = cursor;
		}

		public void close() {
			cs.close();
		}

		public Chat get() {
			return get(0);
		}

		public Chat get(int position) {
			try {
				cs.moveToPosition(position);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
			Chat chat = new Chat();
			for (Column c : Column.values()) {
				write(chat, cs, c, c.toString());
			}
			Talkie from = chat.getFrom();
			if (from != null) {
				for (Talkie.Column c : Talkie.Column.values()) {
					TalkieDB.TalkieResult
							.write(chat.getFrom(), cs, c, FROM + c);
				}
			}
			Talkie to = chat.getTo();
			if (to != null) {
				for (Talkie.Column c : Talkie.Column.values()) {
					TalkieDB.TalkieResult.write(chat.getTo(), cs, c, TO + c);
				}
			}
			return chat;
		}

		public int size() {
			return cs.getCount();
		}

		public static void write(Chat chat, Cursor cs, Column column,
				String name) {
			int index = cs.getColumnIndex(name);
			if (index < 0 || cs.isAfterLast() || cs.isBeforeFirst()
					|| cs.isClosed())
				return;
			switch (column) {
			case _id:
				chat.setId(cs.getInt(index));
				break;
			case t_from:
				byte[] fromId = cs.getBlob(index);
				if (fromId != null) {
					Talkie from = new Talkie();
					from.setMAC(fromId);
					chat.setFrom(from);
				}
				break;
			case message:
				chat.setMessage(cs.getString(index));
				break;
			case t_to:
				byte[] toId = cs.getBlob(index);
				if (toId != null) {
					Talkie to = new Talkie();
					to.setMAC(toId);
					chat.setTo(to);
				}
				break;
			case s_when:
				chat.setWhen(cs.getLong(index));
				break;
			}
		}
	}

	private static ContentValues toCV(Chat chat, Column[] cols) {
		if (chat == null || cols == null || cols.length == 0) {
			return null;
		}
		ContentValues cv = new ContentValues();
		for (Column col : cols) {
			String name = col.toString();
			switch (col) {
			case _id:
				// cv.put(name, chat.getId());
				break;
			case t_from:
				if (chat.getFrom() != null)
					cv.put(name, chat.getFrom().getMAC());
				break;
			case t_to:
				if (chat.getTo() != null)
					cv.put(name, chat.getTo().getMAC());
				break;
			case message:
				cv.put(name, chat.getMessage());
				break;
			case s_when:
				cv.put(name, chat.getWhen());
				break;
			}
		}
		return cv;
	}

	static void buildTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS [" + DB_TABLE + "] (" //
				+ "[" + Column._id + "] INTEGER PRIMARY KEY AUTOINCREMENT, " //
				+ "[" + Column.t_from + "] BLOB NOT NULL, " //
				+ "[" + Column.t_to + "] BLOB, " //
				+ "[" + Column.message + "] TEXT NOT NULL, "//
				+ "[" + Column.s_when + "] INTEGER" //
				+ ");");
	}
}