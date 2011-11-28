package com.aselalee.trainschedule;

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
	public static final String COL_START_TIME = "start_time";
	public static final String COL_END_TIME = "end_time";
}
