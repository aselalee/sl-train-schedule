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

public final class Constants {
	/**
	 * Save TrainScheduleActivity currents values to this file.
	 */
	public static final String PREFERENCES_FILE = "mainPrefs";

	/**
	 * Keys to be saved/read in the TrainScheduleActivity.
	 */
	public static final int TRUE = 1;
	public static final int FALSE = 0;
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
	public static final String JSONURL_GETSCH = "http://m.icta.lk/services/railwayservice/getSchedule.php";
	public static final String JSONURL_GETSCH_V2 = "http://m.icta.lk/services/railwayservicev2/train/searchTrain";
	public static final String JSONURL_GETPRICE = "http://m.icta.lk/services/railwayservice/getTicketPrices.php";
	public static final String JSONURL_GETPRICE_V2 = "http://m.icta.lk/services/railwayservicev2/ticket/getPrice";

	/**
	 * Application version code save in to preferences file.
	 */
	public static final String APP_VERSION_CODE = "app_version_code";

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
	public static final int MAX_HIS_COUNT = 10;

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

	/**
	 * Error Codes
	 */
	public static final int ERR_NO_ERROR = 1;
	public static final int ERR_ERROR = 0;
	public static final int ERR_NETWORK_ERROR = -1;
	public static final int ERR_NO_RESULTS_FOUND_ERROR = -2;
	public static final int ERR_JSON_ERROR = -3;
	public static final int ERR_PROCESSING_ERROR = -4;
	public static final int ERR_DATE_STRING_PARSE_ERROR = -5;
	public static final int ERR_SERVER_ERROR = -6;

	/**
	 * Strings to be displayed as first and last time stamp strings.
	 */
	public static final String TIME_FIRST_FROM = "00:00";
	public static final String TIME_LAST_TO = "24:00";
	
	/**
	 * Google Analytics ID
	 */
	public static final String ANALYTICS_ID = "UA-29241401-1";

}
