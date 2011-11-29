/**
* @copyright	Copyright (C) 2011 Asela Leelaratne
* @license		GNU/GPL Version 3
* 
* This Application is released to the public under the GNU General Public License.
* 
* GNU/GPL V3 Extract.
* 15. Disclaimer of Warranty.
* THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
* EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
* PROVIDE THE PROGRAM AS IS WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
* PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
* NECESSARY SERVICING, REPAIR OR CORRECTION.
*/

package com.aselalee.trainschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        		+ Constants.COL_START_TIME_TXT + " TEXT,"
        		+ Constants.COL_START_TIME_VAL + " TEXT,"
        		+ Constants.COL_END_TIME_TXT + " TEXT,"
        		+ Constants.COL_END_TIME_VAL + " TEXT"
        		+ ");");
        db.execSQL("CREATE TABLE " + Constants.TABLE_FAV + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ Constants.COL_START_STATION_TXT + " TEXT,"
        		+ Constants.COL_START_STATION_VAL + " TEXT,"
        		+ Constants.COL_END_STATION_TXT + " TEXT,"
        		+ Constants.COL_END_STATION_VAL + " TEXT,"
        		+ Constants.COL_START_TIME_TXT + " TEXT,"
        		+ Constants.COL_START_TIME_VAL + " TEXT,"
        		+ Constants.COL_END_TIME_TXT + " TEXT,"
        		+ Constants.COL_END_TIME_VAL + " TEXT"
        		+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Constants.LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_HIS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_FAV);
        onCreate(db);
	}
	
	public boolean PushDataHistory(String start_st_txt, String start_st_val,
							String end_st_txt, String end_st_val,
							String start_time_txt, String start_time_val,
							String end_time_txt, String end_time_val ) {
		SQLiteDatabase db = null;
		Cursor myCur = null;
		long rowID = -1;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
		}
		if( db == null ) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return false;
		}

		ContentValues keyValPairs = new ContentValues(6);
		keyValPairs.put(Constants.COL_START_STATION_TXT, start_st_txt);
		keyValPairs.put(Constants.COL_START_STATION_VAL, start_st_val);
		keyValPairs.put(Constants.COL_END_STATION_TXT, end_st_txt);
		keyValPairs.put(Constants.COL_END_STATION_VAL, end_st_val);
		keyValPairs.put(Constants.COL_START_TIME_TXT, start_time_txt);
		keyValPairs.put(Constants.COL_START_TIME_VAL, start_time_val);
		keyValPairs.put(Constants.COL_END_TIME_TXT, end_time_txt);
		keyValPairs.put(Constants.COL_END_TIME_VAL, end_time_val);
		db.beginTransaction();
		try {
			if( db.insert(Constants.TABLE_HIS, null, keyValPairs) < 0 ) {
				Log.e(Constants.LOG_TAG, "Error writing to DB");
				db.close();
				return false;
			}
		}catch ( Exception e) {
			Log.e(Constants.LOG_TAG, "Error pushing data to DB " + e);
			return false;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		try {
			myCur = db.query(Constants.TABLE_HIS, new String [] {"_ID"}, null, null, null, null, null);
			if( myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				return false;
			}
		} catch ( Exception e) {
			Log.e(Constants.LOG_TAG, "Error in db.query " + e);
			return false;
		}
		db.beginTransaction();
		myCur.moveToFirst();
		for( int i = myCur.getCount(); i > Constants.MAX_HIS_COUNT; i--) {
			try {
				rowID = myCur.getLong(0);
				if( rowID < 0 ) {
					db.close();
					myCur.close();
					Log.e(Constants.LOG_TAG, "Errornous row ID value= " + rowID);
					return false;
				}
				int delCount = db.delete(Constants.TABLE_HIS, "_ID = " + rowID, null);
				if( delCount <= 0) {
					Log.w(Constants.LOG_TAG, "Extra row not deleted...");
				}
			} catch (Exception e) {
				myCur.close();
				db.close();
				Log.e(Constants.LOG_TAG, "Error Deleting a row " + e);
				return false;
			}
			myCur.moveToNext();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		myCur.close();
		db.close();
		return true;
	}

	public ParameterSet [] GetHistory() {
		ParameterSet [] paramsList = null;
		paramsList = null;
		SQLiteDatabase db = null;
		Cursor myCur = null;
		
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
		}
		if( db == null ) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return null;
		}
		try {
			myCur = db.query(Constants.TABLE_HIS, new String [] {"*"}, null, null, null, null, null);
			if( myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				return null;
			}
		} catch ( Exception e) {
			Log.e(Constants.LOG_TAG, "Error in db.query " + e);
			return null;
		}
		paramsList = new ParameterSet[myCur.getCount()];
		myCur.moveToLast();
		for(int i = 0; i < myCur.getCount(); i++) {
			paramsList[i] = new ParameterSet();
			paramsList[i].start_station_txt = myCur.getString(1);
			paramsList[i].start_station_val = myCur.getString(2);
			paramsList[i].end_station_txt = myCur.getString(3);
			paramsList[i].end_station_val = myCur.getString(4);
			paramsList[i].start_time_txt = myCur.getString(5);
			paramsList[i].start_time_val = myCur.getString(6);
			paramsList[i].end_time_txt = myCur.getString(7);
			paramsList[i].end_time_val = myCur.getString(8);
			myCur.moveToPrevious();
		}
		myCur.close();
		db.close();
		return paramsList;
	}
}
