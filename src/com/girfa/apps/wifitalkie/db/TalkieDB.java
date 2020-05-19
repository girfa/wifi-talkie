package com.girfa.apps.wifitalkie.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.girfa.api.Utils;
import com.girfa.apps.wifitalkie.db.TalkieDB.TalkieResult;
import com.girfa.apps.wifitalkie.helper.Config;
import com.girfa.apps.wifitalkie.model.Talkie;
import com.girfa.apps.wifitalkie.model.Talkie.Column;

public class TalkieDB extends Database {
	public static final String TAG = TalkieDB.class.getSimpleName();
	static final String DB_TABLE = "talkie";

	public TalkieDB(Context context) {
		super(context);
	}

	public long add(Talkie talkie) throws SQLiteException {
		if (talkie == null || talkie.getMAC() == null)
			return 0;
		return db.insertOrThrow(DB_TABLE, null,
				toCV(talkie, Column.valuesOf(Column.all() | ~Column._id.id)));
	}

	public Talkie get(byte[] mac) {
		if (mac == null)
			return null;
		try {
			Cursor cs = db.query(DB_TABLE, Column.namesOf(Column.all()),
					Column.mac + " = x'" + Utils.bytesToHex(mac) + "'", null,
					null, null, null);
			Talkie talkie = (new TalkieResult(cs)).get();
			cs.close();
			return talkie;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public TalkieResult gets(boolean showOffline) {
		try {
			Cursor cs = db.query(DB_TABLE, Column.namesOf(Column.all()),
					(showOffline ? 1 : 0) + " OR " + System.currentTimeMillis()
							+ " < " + Column.last + " + " + Config.TIMEOUT,
					null, null, null, null);
			return new TalkieResult(cs);
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public TalkieResult listIPs() {
		try {
			Cursor cs = db.query(
					DB_TABLE,
					Column.namesOf(Column.mac.id | Column.ip.id
							| Column.last.id), System.currentTimeMillis()
							+ " < " + Column.last + " + " + Config.TIMEOUT,
					null, null, null, Column.ip.toString());
			return new TalkieResult(cs);
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int offline(Talkie talkie) {
		if (talkie == null || talkie.getMAC() == null)
			return 0;
		talkie.setLast(System.currentTimeMillis() - Config.TIMEOUT
				- Config.PING_INTERVAL);
		return db.update(DB_TABLE,
				toCV(talkie, Column.valuesOf(Column.last.id)), Column.mac
						+ " = x'" + Utils.bytesToHex(talkie.getMAC()) + "'",
				null);
	}

	public int online(Talkie talkie) {
		if (talkie == null || talkie.getMAC() == null)
			return 0;
		talkie.setLast(System.currentTimeMillis());
		return db.update(
				DB_TABLE,
				toCV(talkie,
						Column.valuesOf(Column.ip.id | Column.last.id
								| Column.latitude.id | Column.longitude.id)),
				Column.mac + " = x'" + Utils.bytesToHex(talkie.getMAC()) + "'",
				null);

	}

	public Talkie state(byte mac[]) {
		if (mac == null)
			return null;
		try {
			Cursor cs = db.query(DB_TABLE, Column.namesOf(Column.last.id),
					Column.mac + " = x'" + Utils.bytesToHex(mac) + "'", null,
					null, null, null);
			Talkie talkie = (new TalkieResult(cs)).get();
			cs.close();
			return talkie;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int update(Talkie talkie) {
		if (talkie == null || talkie.getMAC() == null)
			return 0;
		return db.update(DB_TABLE,
				toCV(talkie, Column.valuesOf(talkie.getField())), Column.mac
						+ " = x'" + Utils.bytesToHex(talkie.getMAC()) + "'",
				null);
	}

	public static class TalkieResult {
		private Cursor cs;

		public TalkieResult(Cursor cursor) {
			cs = cursor;
		}

		public void close() {
			cs.close();
		}

		public Talkie get() {
			return get(0);
		}

		public Talkie get(int position) {
			try {
				cs.moveToPosition(position);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
			Talkie talkie = new Talkie();
			for (Column c : Column.values()) {
				write(talkie, cs, c, c.toString());
			}
			return talkie;
		}

		public int size() {
			return cs.getCount();
		}

		public static void write(Talkie talkie, Cursor cs, Column column,
				String name) {
			int index = cs.getColumnIndex(name);
			if (index < 0 || cs.isAfterLast() || cs.isBeforeFirst()
					|| cs.isClosed())
				return;
			switch (column) {
			case _id:
				talkie.setId(cs.getInt(index));
				break;
			case mac:
				talkie.setMAC(cs.getBlob(index));
				break;
			case name:
				talkie.setName(cs.getString(index));
				break;
			case photo:
				talkie.setPhoto(cs.getBlob(index));
				break;
			case status:
				talkie.setStatus(cs.getString(index));
				break;
			case device:
				talkie.setDevice(cs.getString(index));
				break;
			case model:
				talkie.setModel(cs.getString(index));
				break;
			case since:
				talkie.setSince(cs.getLong(index));
				break;
			case ip:
				talkie.setIP(cs.getBlob(index));
				break;
			case latitude:
				talkie.setLatitude(cs.getDouble(index));
				break;
			case longitude:
				talkie.setLongitude(cs.getDouble(index));
				break;
			case last:
				talkie.setLast(cs.getLong(index));
			}
		}
	}

	private static ContentValues toCV(Talkie talkie, Column cols[]) {
		if (talkie == null || cols == null || cols.length == 0) {
			return null;
		}
		ContentValues cv = new ContentValues();
		for (Column col : cols) {
			String name = col.toString();
			switch (col) {
			case _id:
				// cv.put(name, talkie.getId());
				break;
			case mac:
				cv.put(name, talkie.getMAC());
				break;
			case name:
				cv.put(name, talkie.getName());
				break;
			case photo:
				cv.put(name, talkie.getPhoto());
				break;
			case status:
				cv.put(name, talkie.getStatus());
				break;
			case device:
				cv.put(name, talkie.getDevice());
				break;
			case model:
				cv.put(name, talkie.getModel());
				break;
			case since:
				cv.put(name, talkie.getSince());
				break;
			case ip:
				cv.put(name, talkie.getIP());
				break;
			case latitude:
				cv.put(name, talkie.getLatitude());
				break;
			case longitude:
				cv.put(name, talkie.getLongitude());
				break;
			case last:
				cv.put(name, talkie.getLast());
			}
		}
		return cv;
	}

	static void buildTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS [" + DB_TABLE + "] (" //
				+ "[" + Column._id + "] INTEGER PRIMARY KEY AUTOINCREMENT, " //
				+ "[" + Column.mac + "] BLOB UNIQUE NOT NULL, "//
				+ "[" + Column.name + "] TEXT, " //
				+ "[" + Column.photo + "] BLOB, " //
				+ "[" + Column.status + "] TEXT, " //
				+ "[" + Column.device + "] TEXT, " //
				+ "[" + Column.model + "] TEXT, " //
				+ "[" + Column.since + "] INTEGER, " //
				+ "[" + Column.ip + "] BLOB, " //
				+ "[" + Column.latitude + "] REAL, " //
				+ "[" + Column.longitude + "] REAL, " //
				+ "[" + Column.last + "] INTEGER" //
				+ ");");
	}

	public TalkieResult listen() {
		return listIPs();
	}
}
