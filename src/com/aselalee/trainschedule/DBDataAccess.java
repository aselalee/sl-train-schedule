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
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;

public class DBDataAccess extends SQLiteOpenHelper {

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
				+ Constants.COL_END_TIME_VAL + " TEXT,"
				+ Constants.COL_DATE_TXT + " TEXT"
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
				+ Constants.COL_END_TIME_VAL + " TEXT,"
				+ Constants.COL_FAV_NAME + " TEXT"
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
			String end_time_txt, String end_time_val, String date_txt) {
		SQLiteDatabase db = null;
		Cursor myCur = null;
		long rowID = -1;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return false;
		}

		ContentValues keyValPairs = new ContentValues(8);
		keyValPairs.put(Constants.COL_START_STATION_TXT, start_st_txt);
		keyValPairs.put(Constants.COL_START_STATION_VAL, start_st_val);
		keyValPairs.put(Constants.COL_END_STATION_TXT, end_st_txt);
		keyValPairs.put(Constants.COL_END_STATION_VAL, end_st_val);
		keyValPairs.put(Constants.COL_START_TIME_TXT, start_time_txt);
		keyValPairs.put(Constants.COL_START_TIME_VAL, start_time_val);
		keyValPairs.put(Constants.COL_END_TIME_TXT, end_time_txt);
		keyValPairs.put(Constants.COL_END_TIME_VAL, end_time_val);
		keyValPairs.put(Constants.COL_DATE_TXT, date_txt);
		db.beginTransaction();
		try {
			if(db.insert(Constants.TABLE_HIS, null, keyValPairs) < 0) {
				Log.e(Constants.LOG_TAG, "Error writing to DB");
				db.close();
				return false;
			}
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error pushing data to DB " + e);
			db.endTransaction();
			db.close();
			return false;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		try {
			myCur = db.query(Constants.TABLE_HIS, new String [] {"_ID"}, null, null, null, null, null);
			if(myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				return false;
			}
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in db.query " + e);
			db.close();
			return false;
		}
		db.beginTransaction();
		myCur.moveToFirst();
		for(int i = myCur.getCount(); i > Constants.MAX_HIS_COUNT; i--) {
			try {
				rowID = myCur.getLong(0);
				if(rowID < 0) {
					db.close();
					myCur.close();
					Log.e(Constants.LOG_TAG, "Errornous row ID value= " + rowID);
					return false;
				}
				int delCount = db.delete(Constants.TABLE_HIS, "_ID = " + rowID, null);
				if(delCount <= 0) {
					Log.w(Constants.LOG_TAG, "Extra row not deleted...");
				}
			} catch(Exception e) {
				db.endTransaction();
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
		myCur = null;
		db = null;
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
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return null;
		}
		try {
			myCur = db.query(Constants.TABLE_HIS, new String [] {"*"}, null, null, null, null, null);
			if(myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				return null;
			}
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in db.query " + e);
			db.close();
			return null;
		}
		paramsList = new ParameterSet[myCur.getCount()];
		myCur.moveToLast();
		for(int i = 0; i < myCur.getCount(); i++) {
			paramsList[i] = new ParameterSet();
			paramsList[i].start_station_txt =  CommonUtilities.ToTitleCase(myCur.getString(1));
			paramsList[i].start_station_val = myCur.getString(2);
			paramsList[i].end_station_txt =  CommonUtilities.ToTitleCase(myCur.getString(3));
			paramsList[i].end_station_val = myCur.getString(4);
			paramsList[i].start_time_txt = myCur.getString(5);
			paramsList[i].start_time_val = myCur.getString(6);
			paramsList[i].end_time_txt = myCur.getString(7);
			paramsList[i].end_time_val = myCur.getString(8);
			paramsList[i].date_txt = myCur.getString(9);
			myCur.moveToPrevious();
		}
		myCur.close();
		db.close();
		myCur = null;
		db = null;
		return paramsList;
	}

	public void ClearHistoryTable() {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
			return;
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return;
		}
		db.beginTransaction();
		try {
			db.delete(Constants.TABLE_HIS, null, null);
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in deleting all records in table" + e);
			db.endTransaction();
			db.close();
			return;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		db = null;
	}

	public boolean PushDataFavourites(String start_st_txt, String start_st_val,
			String end_st_txt, String end_st_val,
			String start_time_txt, String start_time_val,
			String end_time_txt, String end_time_val,
			String name, Handler handler ) {
		SQLiteDatabase db = null;
		Cursor myCur = null;
		int rowCount = Constants.MAX_FAV_COUNT;
		Message myMsg = new Message();
		String msgStr = "Error... Restart application";
		myMsg.obj = (Object)msgStr;
		myMsg.arg1 = Constants.THREAD_PUSH_DATA_FAVOURITES;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
			handler.sendMessage(myMsg);
			return false;
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			handler.sendMessage(myMsg);
			return false;
		}
		try {
			myCur = db.query(Constants.TABLE_FAV, new String [] {"_ID"}, null, null, null, null, null);
			if(myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				handler.sendMessage(myMsg);
				return false;
			}
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in db.query " + e);
			handler.sendMessage(myMsg);
			return false;
		}
		rowCount = myCur.getCount();
		myCur.close();
		/**
		 * If items in history table is less than the max count add data. Else return false.
		 */
		if(rowCount < Constants.MAX_FAV_COUNT) {
			ContentValues keyValPairs = new ContentValues(9);
			keyValPairs.put(Constants.COL_START_STATION_TXT, start_st_txt);
			keyValPairs.put(Constants.COL_START_STATION_VAL, start_st_val);
			keyValPairs.put(Constants.COL_END_STATION_TXT, end_st_txt);
			keyValPairs.put(Constants.COL_END_STATION_VAL, end_st_val);
			keyValPairs.put(Constants.COL_START_TIME_TXT, start_time_txt);
			keyValPairs.put(Constants.COL_START_TIME_VAL, start_time_val);
			keyValPairs.put(Constants.COL_END_TIME_TXT, end_time_txt);
			keyValPairs.put(Constants.COL_END_TIME_VAL, end_time_val);
			keyValPairs.put(Constants.COL_FAV_NAME, name);
			db.beginTransaction();
			try {
				if( db.insert(Constants.TABLE_FAV, null, keyValPairs) < 0 ) {
					Log.e(Constants.LOG_TAG, "Error writing to DB");
					db.close();
					handler.sendMessage(myMsg);
					return false;
				}
			} catch(Exception e) {
				Log.e(Constants.LOG_TAG, "Error pushing data to DB " + e);
				db.endTransaction();
				db.close();
				handler.sendMessage(myMsg);
				return false;
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		else {
			/** Maximum favourites limit exceeded **/
			Log.w(Constants.LOG_TAG, "Maximum favourites limit exceeded");
			db.close();
			msgStr = "Maximum favourites limit of " + Constants.MAX_FAV_COUNT + " reached.";
			myMsg.obj = (Object)msgStr;
			handler.sendMessage(myMsg);
			return false;
		}
		db.close();
		db = null;
		msgStr = "Sucessfully added to favourites.";
		myMsg.obj = (Object)msgStr;
		handler.sendMessage(myMsg);
		return true;
	}

	public ParameterSet [] GetFavourites() {
		ParameterSet [] paramsList = null;
		SQLiteDatabase db = null;
		Cursor myCur = null;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return null;
		}
		try {
			myCur = db.query(Constants.TABLE_FAV, new String [] {"*"}, null, null, null, null, null);
			if(myCur == null) {
				Log.e(Constants.LOG_TAG, "Select operation failed");
				db.close();
				return null;
			}
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in db.query " + e);
			db.close();
			return null;
		}
		paramsList = new ParameterSet[myCur.getCount()];
		myCur.moveToFirst();
		for(int i = 0; i < myCur.getCount(); i++) {
			paramsList[i] = new ParameterSet();
			paramsList[i].id = myCur.getLong(0);
			paramsList[i].start_station_txt =  CommonUtilities.ToTitleCase(myCur.getString(1));
			paramsList[i].start_station_val = myCur.getString(2);
			paramsList[i].end_station_txt =  CommonUtilities.ToTitleCase(myCur.getString(3));
			paramsList[i].end_station_val = myCur.getString(4);
			paramsList[i].start_time_txt = myCur.getString(5);
			paramsList[i].start_time_val = myCur.getString(6);
			paramsList[i].end_time_txt = myCur.getString(7);
			paramsList[i].end_time_val = myCur.getString(8);
			paramsList[i].name = myCur.getString(9);
			myCur.moveToNext();
		}
		myCur.close();
		db.close();
		myCur = null;
		db = null;
		return paramsList;
	}

	public void ClearFavouritesTable() {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
			return;
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return;
		}
		db.beginTransaction();
		try {
			db.delete(Constants.TABLE_FAV, null, null);
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in deleting all records in table" + e);
			db.endTransaction();
			db.close();
			return;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		db = null;
	}
	public boolean DeleteFavRecord(long id) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
			return false;
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return false;
		}
		db.beginTransaction();
		try {
			db.delete(Constants.TABLE_FAV, "_ID=" + String.valueOf(id), null);
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in deleting record" + e);
			db.endTransaction();
			db.close();
			return false;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		db = null;
		return true;
	}

	public boolean RenameFavRecord(long id, String newName) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in getWritableDatabase" + e);
			return false;
		}
		if(db == null) {
			Log.e(Constants.LOG_TAG, "Cannot open writable DB");
			return false;
		}
		db.beginTransaction();
		try {
			ContentValues keyValPairs = new ContentValues(1);
			keyValPairs.put(Constants.COL_FAV_NAME, newName);
			db.update(Constants.TABLE_FAV, keyValPairs, "_ID=" + String.valueOf(id), null);
		} catch(Exception e) {
			Log.e(Constants.LOG_TAG, "Error in deleting record" + e);
			db.endTransaction();
			db.close();
			return false;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		db = null;
		return true;
	}
}
