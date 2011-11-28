package com.aselalee.trainschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBDataAccess extends SQLiteOpenHelper {

	/* public DBDataAccess(Context context, String name, CursorFactory factory,
			int version) { */
	public DBDataAccess(Context context) {
		super(context, Constants.DB_NAME, null, Constants.DB_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Constants.TABLE_HIS + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ Constants.COL_START_STATION_TXT + " TEXT,"
        		+ Constants.COL_START_STATION_VAL + " TEXT,"
        		+ Constants.COL_END_STATION_TXT + " TEXT,"
        		+ Constants.COL_END_STATION_VAL + " TEXT,"
        		+ Constants.COL_START_TIME + " TEXT,"
        		+ Constants.COL_END_TIME + " TEXT"
        		+ ");");
        db.execSQL("CREATE TABLE " + Constants.TABLE_FAV + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ Constants.COL_START_STATION_TXT + " TEXT,"
        		+ Constants.COL_START_STATION_VAL + " TEXT,"
        		+ Constants.COL_END_STATION_TXT + " TEXT,"
        		+ Constants.COL_END_STATION_VAL + " TEXT,"
        		+ Constants.COL_START_TIME + " TEXT,"
        		+ Constants.COL_END_TIME + " TEXT"
        		+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Constants.LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
	}
	
	public boolean PushData(String start_st_txt,
							String start_st_val,
							String end_st_txt,
							String end_st_val,
							String start_time,
							String end_time ) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues keyValPairs = new ContentValues(6);
		keyValPairs.put(Constants.COL_START_STATION_TXT, start_st_txt);
		keyValPairs.put(Constants.COL_START_STATION_VAL, start_st_val);
		keyValPairs.put(Constants.COL_END_STATION_TXT, end_st_txt);
		keyValPairs.put(Constants.COL_END_STATION_VAL, end_st_val);
		keyValPairs.put(Constants.COL_START_TIME, start_time);
		keyValPairs.put(Constants.COL_END_TIME, end_time);
		try {
			if( db.insert(Constants.TABLE_HIS, null, keyValPairs) < 0 ) {
				return false;
			}
		} catch ( Exception e) {
			Log.e(Constants.LOG_TAG, "Error pushing data to DB" + e);
		}
		return true;
	}

}
