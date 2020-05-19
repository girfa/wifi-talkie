package com.girfa.apps.wifitalkie.db;


import com.girfa.apps.wifitalkie.model.File.Column;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class FileDB {
	public static final String TAG = FileDB.class.getSimpleName();
	static final String DB_TABLE = "file";
	
	public FileDB(Context paramContext) {
	}

	static void buildTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS [" + DB_TABLE + "] (" 
				+ "[" + Column._id + "] INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ "[" + Column.t_from + "] BLOB NOT NULL, " 
				+ "[" + Column.t_to + "] BLOB, "
				+ "[" + Column.location + "] TEXT NOT NULL, " 
				+ "[" + Column.s_when + "] INTEGER" 
				+ ");");
	}
}