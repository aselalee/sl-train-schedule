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

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class Constants {
	/**
	 * Save TrainScheduleActivity currents values to this file.
	 */
	public static final String PREFERENCES_FILE = "mainPrefs";
	/**
	 * Keys to be saved/read in the TrainScheduleActivity.
	 */
	public static final String TIME_FROM_POS = "time_from_pos";
	public static final String TIME_TO_POS = "time_to_pos";
	public static final String STATION_FROM_TXT = "station_from_txt";
	public static final String STATION_FROM_VAL = "station_from_val";
	public static final String STATION_TO_TXT = "station_to_txt";
	public static final String STATION_TO_VAL = "station_to_val";

	/**
	 * LOG Tag
	 */
	public static final String LOG_TAG = "TR_SCH";

	/**
	 * Site to fetch results from.
	 */
	public static final String JASONURL = "http://mobile.icta.lk/services/railwayservice/getSchedule.php";

	/**
	 * Database constants
	 */
	public static final String DB_NAME = "trains_serch_info.db";
	public static final int DB_VER = 2;
	public static final String TABLE_HIS = "history";
	public static final String TABLE_FAV = "favourites";
	public static final String COL_START_STATION_TXT = "start_station_txt";
	public static final String COL_END_STATION_TXT = "end_station_txt";
	public static final String COL_START_STATION_VAL = "start_station_val";
	public static final String COL_END_STATION_VAL = "end_station_val";
	public static final String COL_START_TIME_TXT = "start_time_txt";
	public static final String COL_END_TIME_TXT = "end_time_txt";
	public static final String COL_START_TIME_VAL = "start_time_val";
	public static final String COL_END_TIME_VAL = "end_time_val";
	public static final String COL_FAV_NAME = "fav_name";

	/**
	 * Maximum entries kept in the history table.
	 */
	public static final int MAX_HIS_COUNT = 5;
	/**
	 * Maximum entries kept in the favourites table.
	 */
	public static final int MAX_FAV_COUNT = 15;
	/**
	 * Indicates message is sent from GetResultsActivity thread.
	 */
	public static final int THREAD_GET_RESULTS = 0;
	/**
	 * Indicates message is sent from DBDataAccess.PushDataFavourites thread.
	 */
	public static final int THREAD_PUSH_DATA_FAVOURITES = 1;
	
	public static final void PupulateIntentForResultsActivity(
			String station_from_val, String station_from_txt,
			String station_to_val, String station_to_txt,
			String time_from_val, String time_from_txt,
			String time_to_val, String time_to_txt,
			Intent intent) {
		String date_today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
		intent.putExtra("station_from", station_from_val);
		intent.putExtra("station_from_txt", station_from_txt);
		intent.putExtra("station_to", station_to_val);
		intent.putExtra("station_to_txt",station_to_txt);
		intent.putExtra("time_from", time_from_val);
		intent.putExtra("time_from_txt", time_from_txt);
		intent.putExtra("time_to", time_to_val);
		intent.putExtra("time_to_txt", time_to_txt);
		intent.putExtra("date_today", date_today);
		return;
	}
    public static final void HideSoftKeyboard(View view, Context context) {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
