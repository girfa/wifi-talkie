package com.girfa.apps.wifitalkie.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class Database {
	protected SQLiteDatabase db;
	private OpenHelper helper;

	public Database(Context context) {
		helper = new OpenHelper(context);
	}

	public synchronized void close() {
		db.close();
	}

	public boolean isOpen() {
		return db.isOpen();
	}

	public synchronized Database openRead() {
		db = helper.getReadableDatabase();
		return this;
	}

	public synchronized Database openWrite() {
		db = helper.getWritableDatabase();
		return this;
	}
}