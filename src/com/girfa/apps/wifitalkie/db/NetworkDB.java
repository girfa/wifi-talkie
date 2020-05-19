package com.girfa.apps.wifitalkie.db;


import com.girfa.apps.wifitalkie.model.Network.Column;

import android.database.sqlite.SQLiteDatabase;

public class NetworkDB {
	public static final String TAG = NetworkDB.class.getSimpleName();
	static final String DB_TABLE = "network";

	static void buildTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS [" + DB_TABLE + "] (" 
				+ "[" + Column._id + "] INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ "[" + Column.name + "] TEXT, " 
				+ "[" + Column.address + "] BLOB, "
				+ "[" + Column.broadcast + "] BLOB, " 
				+ "[" + Column.prefix + "] INTEGER, " 
				+ "[" + Column.nodes + "] INTEGER" 
				+ ");");
	}
}