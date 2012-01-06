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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
	public static final String IS_RESULTS_WEB_VIEW = "is_results_web_view";

	/**
	 * LOG Tag
	 */
	public static final String LOG_TAG = "TR_SCH";

	/**
	 * Site to fetch results from.
	 */
	public static final String JASONURL = "http://mobile.icta.lk/services/railwayservice/getSchedule.php";
	
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
	/**
	 * Error Codes
	 */
	public static final int ERR_NO_ERROR = 1;
	public static final int ERR_ERROR = 0;
	public static final int ERR_NETWORK_ERROR = -1;
	public static final int ERR_NO_RESULTS_FOUND_ERROR = -2;
	public static final int ERR_JASON_ERROR = -3;
	public static final int ERR_PROCESSING_ERROR = -4;

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

	public static final String ToTitleCase(String string) {
		String [] words = string.split(" ");
		String result  = "";
		for(int i = 0; i < words.length; i++) {
			result += toInitialCap( words[i] ) + " ";
		}
		return result.trim();
	}

	private static final String toInitialCap(String word) {
		if( word != null && word.length() > 1) {
			return String.valueOf(word.toUpperCase().charAt(0)) + word.substring(1).toLowerCase();
		}
		return "";
	}

	public static Intent GetResultViewIntent(Context packageContext) {
		Intent intent = null;
		SharedPreferences pref = packageContext.getSharedPreferences(Constants.PREFERENCES_FILE,
													android.content.Context.MODE_WORLD_READABLE);
		if(pref.getInt(Constants.IS_RESULTS_WEB_VIEW, Constants.FALSE) == Constants.FALSE) {		
			intent = new Intent(packageContext, ResultViewActivity.class);
		} else {
			intent = new Intent(packageContext, ResultViewActivityWebView.class);
		}
		return intent;
	}

	public static void GetResultsViewChoiceFromUser(final Context packageContext) {
		AlertDialog.Builder builder = new AlertDialog.Builder(packageContext);
		SharedPreferences pref = packageContext.getSharedPreferences(Constants.PREFERENCES_FILE,
				android.content.Context.MODE_WORLD_READABLE);
		int current_choice = pref.getInt(Constants.IS_RESULTS_WEB_VIEW, -1);
		CharSequence[] choice = {"New List View\n(Fast and Looks Cool)",
								"Old Web View\n(Slow and Looks ...old)"};
		builder.setSingleChoiceItems(choice, current_choice, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					setResultsViewState(packageContext, Constants.FALSE);
				} else {
					setResultsViewState(packageContext, Constants.TRUE);
				}
				dialog.cancel();
			}
		});
		builder.setTitle("Select Reuslts View");
		AlertDialog alert = builder.create();
		alert.show();
	}

	private static void setResultsViewState(Context packageContext, int isResultWebView) {
		SharedPreferences pref = packageContext.getSharedPreferences(Constants.PREFERENCES_FILE,
				android.content.Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor e = pref.edit();
		e.putInt(Constants.IS_RESULTS_WEB_VIEW, isResultWebView);
		e.commit();
	}
	
	/**
	 * Add data to favourites table.
	 */
	public static void GetNewFavNameAndAddToFavs(final Context packageContext, final boolean isCheckBoxGone,
			final String station_from_txt, final String station_from_val,
			final String station_to_txt, final String station_to_val,
			final String time_from_txt, final String time_from_val,
			final String time_to_txt, final String time_to_val, final Handler handler) {
		LayoutInflater factory = LayoutInflater.from(packageContext);
		View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		final EditText et = (EditText)textEntryView.findViewById(R.id.dialog_new_name);
		final CheckBox cb = (CheckBox)textEntryView.findViewById(R.id.dialog_isTimeFilterOnCB);
		if(isCheckBoxGone == true) {
			cb.setVisibility(View.GONE);
		}
		et.setText(station_from_txt + " - " + station_to_txt);
		AlertDialog.Builder builder = new AlertDialog.Builder(packageContext);
		builder.setView(textEntryView);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				boolean isTimeFilterON = true;
				if(isCheckBoxGone == false) {
					isTimeFilterON = cb.isChecked();
				}
				addParamsToFavs(packageContext,
						station_from_txt, station_from_val,
						station_to_txt, station_to_val,
						time_from_txt, time_from_val,
						time_to_txt, time_to_val,
						et.getEditableText().toString(), handler, isTimeFilterON);
				Constants.HideSoftKeyboard(et, packageContext);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Constants.HideSoftKeyboard(et, packageContext);
				dialog.cancel();
			}
		});
		builder.setTitle("Enter New Name");
		AlertDialog alert = builder.create();
		alert.show();
	}

	private static void addParamsToFavs(final Context packageContext,
			final String station_from_txt, final String station_from_val,
			final String station_to_txt, final String station_to_val,
			final String time_from_txt, final String time_from_val,
			final String time_to_txt, final String time_to_val,
			final String name_txt, final Handler handler, final boolean isTimeFilterON) {
		if(name_txt.length() == 0) {
			Toast.makeText(packageContext, "Invalid name", Toast.LENGTH_LONG).show();
			return;
		}
		Runnable runnable = new Runnable() {
				public void run() {
					DBDataAccess myDBAcc = new DBDataAccess(packageContext);
					if(isTimeFilterON == true) {
						myDBAcc.PushDataFavourites(station_from_txt, station_from_val,
								station_to_txt, station_to_val,
								time_from_txt, time_from_val,
								time_to_txt, time_to_val,
								name_txt, handler );
					} else {
						myDBAcc.PushDataFavourites(station_from_txt, station_from_val,
								station_to_txt, station_to_val,
								packageContext.getString(R.string.earliest_from_time), MapTimeFrom(0),
								packageContext.getString(R.string.latest_to_time), MapTimeTo(-1),
								name_txt, handler );
					}
					myDBAcc.close();
				}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	/**
	 * Match "from time" in spinner to actual string received by the server.
	 */
	public static String MapTimeFrom(int pos) {
		String time_from[] = {"00:00:01","01:00:00","02:00:00","03:00:00","04:00:00","05:00:00",
				"06:00:00","07:00:00","08:00:00","09:00:00","10:00:00","11:00:00",
				"11:59:59","13:00:00","14:00:00","15:00:00","16:00:00","17:00:00",
				"18:00:00","19:00:00","20:00:00","21:00:0,","22:00:00","23:00:00"};
		if(pos == -1) {
			return time_from[time_from.length - 1];
		}
		return time_from[pos];
	}

	/**
	 * Match "to time" in spinner to actual string received by the server.
	 */
	public static String MapTimeTo(int pos) {
		String time_to[] = {"01:00:00","02:00:00","03:00:00","04:00:00","05:00:00","06:00:00","07:00:00",
				"08:00:00","09:00:00","10:00:00","11:00:00","11:59:59","13:00:00","14:00:00",
				"15:00:00","16:00:00","17:00:00","18:00:00","19:00:00","20:00:00","21:00:00",
				"22:00:0,","23:00:00","23:59:59"};
		if(pos == -1) {
			return time_to[time_to.length - 1];
		}
		return time_to[pos];
	}
}
