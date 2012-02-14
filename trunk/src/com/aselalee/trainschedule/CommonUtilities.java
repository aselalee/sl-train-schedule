package com.aselalee.trainschedule;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

public class CommonUtilities {

	/**
	 * *********************************************
	 * Static methods Than can be accessed directly.
	 * *********************************************
	 */

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

	public static AlertDialog GetResultsViewChoiceFromUser(final Context packageContext) {
		AlertDialog.Builder builder = new AlertDialog.Builder(packageContext);
		SharedPreferences pref = packageContext.getSharedPreferences(Constants.PREFERENCES_FILE,
				android.content.Context.MODE_WORLD_READABLE);
		int current_choice = pref.getInt(Constants.IS_RESULTS_WEB_VIEW, -1);
		CharSequence[] choice = {"New List View\n(Fast and Looks Cool)",
								"Old Web View\n(Slow and Looks ...old)"};
		builder.setSingleChoiceItems(choice, current_choice, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					CommonUtilities.setResultsViewState(packageContext, Constants.FALSE);
				} else {
					CommonUtilities.setResultsViewState(packageContext, Constants.TRUE);
				}
				dialog.cancel();
			}
		});
		builder.setTitle("Select Reuslts View");
		AlertDialog alert = builder.create();
		return alert;
	}

	private static void setResultsViewState(Context packageContext, int isResultWebView) {
		SharedPreferences pref = packageContext.getSharedPreferences(Constants.PREFERENCES_FILE,
				android.content.Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor e = pref.edit();
		e.putInt(Constants.IS_RESULTS_WEB_VIEW, isResultWebView);
		e.commit();
	}

	public static void AddParamsToHistory(final Context mPackageContext,
			final String mStationFromTxt, final String mStationFromVal,
			final String mStationToTxt, final String mStationToVal,
			final String mTimeFromTxt, final String mTimeFromVal,
			final String mTimeToTxt, final String mTimeToVal) {
		Runnable runnable = new Runnable() {
			public void run() {
				DBDataAccess myDBAcc = new DBDataAccess(mPackageContext);
				myDBAcc.PushDataHistory(mStationFromTxt, mStationFromVal,
						mStationToTxt, mStationToVal,
						mTimeFromTxt, mTimeFromVal,
						mTimeToTxt, mTimeToVal);
				myDBAcc.close();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
		return;
	}

	public static void ShareApplication(Context mContext) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Intent.EXTRA_SUBJECT, "Search Sri Lanka Railway Time Table");
		intent.putExtra(Intent.EXTRA_TITLE, "Search Sri Lanka Railway Time Table");
		intent.putExtra(Intent.EXTRA_TEXT, "Search \"Sri Lanka Railway Time Table\" on your Android. http://market.android.com/details?id=com.aselalee.trainschedule");
		mContext.startActivity(Intent.createChooser(intent, "Spread the word"));
		return;
	}

	/**
	 * Intent chooser is customized to remove unwanted apps.
	 * 1. FaceBook has bug where only links can be shared.
	 * 2. Cannot share this type of content via Google Docs and Skype.
	 */
	public static void ShareResult(Context mContext, String mResult) {
		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(shareIntent, 0);
		if(!resInfo.isEmpty()) {
			for(ResolveInfo resolveInfo : resInfo) {
				String packageName = resolveInfo.activityInfo.packageName;
				Intent targetedShareIntent = new Intent(android.content.Intent.ACTION_SEND);
				targetedShareIntent.setType("text/plain");
				targetedShareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "I'll be on this train");
				targetedShareIntent.putExtra(android.content.Intent.EXTRA_TITLE, "I'll be on this train");
				targetedShareIntent.putExtra(android.content.Intent.EXTRA_TEXT, mResult);
				if(!packageName.toLowerCase().contains("com.facebook.katana") &&
					!packageName.toLowerCase().contains("com.google.android.apps.docs") &&
					!packageName.toLowerCase().contains("com.skype.raider")) {
					targetedShareIntent.setPackage(packageName);
					targetedShareIntents.add(targetedShareIntent);
				}
			}
			Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Send your result");
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
			mContext.startActivity(chooserIntent);
		}
		return;
	}

	public static void NewVersionInfo(Context mContext) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(R.string.version_info)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		return;
	}
	/**
	 * **********************************************************************
	 * Utilities methods that can be accessed via an instance of this class.
	 * **********************************************************************
	 */
	private Context packageContext = null;
	private Handler handler = null;
	private String station_from_txt = "";
	private String station_from_val = "";
	private String station_to_txt = "";
	private String station_to_val = "";
	private String time_from_txt = "";
	private String time_from_val = "";
	private String time_to_txt = "";
	private String time_to_val = "";

	public CommonUtilities(Context mPackageContext) {
		packageContext = mPackageContext;
	}

	/**
	 * Add data to favourites table.
	 */
	public AlertDialog GetNewFavNameAndAddToFavs(final boolean isCheckBoxGone,
			String mStationFromTxt, String mStationFromVal,
			String mStationToTxt, String mStationToVal,
			String mTimeFromTxt, String mTimeFromVal,
			String mTimeToTxt, String mTimeToVal, Handler mHandler) {
		/**
		 * Init Class variables.
		 */
		handler = mHandler;
		updateClassVariables(mStationFromTxt, mStationFromVal,
				mStationToTxt, mStationToVal,
				mTimeFromTxt, mTimeFromVal,
				mTimeToTxt, mTimeToVal);

		LayoutInflater factory = LayoutInflater.from(packageContext);
		View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		final EditText et = (EditText)textEntryView.findViewById(R.id.dialog_new_name);
		final CheckBox cb = (CheckBox)textEntryView.findViewById(R.id.dialog_isTimeFilterOnCB);
		if(isCheckBoxGone == true) {
			cb.setVisibility(View.GONE);
		}
		et.setText(station_from_txt + " - " + station_to_txt);
		et.setSelection(et.getText().length());
		AlertDialog.Builder builder = new AlertDialog.Builder(packageContext);
		builder.setView(textEntryView);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				boolean isTimeFilterON = true;
				if(isCheckBoxGone == false) {
					isTimeFilterON = cb.isChecked();
				}
				addParamsToFavs(et.getEditableText().toString(), isTimeFilterON);
				CommonUtilities.HideSoftKeyboard(et, packageContext);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				CommonUtilities.HideSoftKeyboard(et, packageContext);
				dialog.cancel();
			}
		});
		builder.setTitle("Enter New Name");
		AlertDialog alert = builder.create();
		return alert;
	}

	private void addParamsToFavs(final String name_txt, final boolean isTimeFilterON) {
		if(name_txt.length() == 0) {
			Message myMsg = new Message();
			String msgStr = "Invalid name";
			myMsg.obj = (Object)msgStr;
			myMsg.arg1 = Constants.THREAD_PUSH_DATA_FAVOURITES;
			handler.sendMessage(myMsg);
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
								Constants.TIME_FIRST_FROM,
								CommonUtilities.MapTimeFrom(0),
								Constants.TIME_LAST_TO,
								CommonUtilities.MapTimeTo(-1),
								name_txt, handler );
					}
					myDBAcc.close();
				}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public void UpdateFavParams(String mStationFromTxt, String mStationFromVal,
				String mStationToTxt, String mStationToVal,
				String mTimeFromTxt, String mTimeFromVal,
				String mTimeToTxt, String mTimeToVal, Dialog dialog) {

		/**
		 * Update Class variables.
		 */
		updateClassVariables(mStationFromTxt, mStationFromVal,
				mStationToTxt, mStationToVal,
				mTimeFromTxt, mTimeFromVal,
				mTimeToTxt, mTimeToVal);

		/**
		 * Update edit text box.
		 */
		EditText et = null;
		et = (EditText)dialog.findViewById(R.id.dialog_new_name);
		et.setText(station_from_txt + " - " + station_to_txt);
		et.setSelection(et.getText().length());

		return;
	}
	
	private void updateClassVariables(String mStationFromTxt, String mStationFromVal,
			String mStationToTxt, String mStationToVal,
			String mTimeFromTxt, String mTimeFromVal,
			String mTimeToTxt, String mTimeToVal) {
		station_from_txt = mStationFromTxt;
		station_from_val = mStationFromVal;
		station_to_txt = mStationToTxt;
		station_to_val = mStationToVal;
		time_from_txt = mTimeFromTxt;
		time_from_val = mTimeFromVal;
		time_to_txt = mTimeToTxt;
		time_to_val = mTimeToVal;
	}
}
