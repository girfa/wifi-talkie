package com.girfa.apps.wifitalkie.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper {
	public static final String TAG = OpenHelper.class.getSimpleName();
	static final String DB_NAME = "talkie.db";
	static final int DB_VERSION = 1;

	public OpenHelper(Context context) {
		super(context, "talkie.db", null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		TalkieDB.buildTable(db);
		ChatDB.buildTable(db);
		FileDB.buildTable(db);
		NetworkDB.buildTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}