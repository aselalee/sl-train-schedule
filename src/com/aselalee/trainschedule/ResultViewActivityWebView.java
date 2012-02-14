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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

public class ResultViewActivityWebView extends Activity {
	private String station_from;
	private String station_from_txt;
	private String station_to;
	private String station_to_txt;
	private String time_from;
	private String time_from_txt;
	private String time_to;
	private String time_to_txt;
	private String date_today;
	private WebView mWebView = null;
	private ProgressDialog pd;
	private GetResultsFromSite resultsThread = null;
	private boolean isStop = false;
	private boolean isAddToFavsActive = true;
	private String resultHTML = "<html><head><title>Test</title></head><body><h1>Dummy</h1></body></html>";
	private Result [] results = null;
	private int errorCode = Constants.ERR_NO_ERROR;
	GoogleAnalyticsTracker tracker;
	
	private static final int DIALOG_PROGRESS = 2;
	private static final int DIALOG_ADD_TO_FAV = 3;
	private static final int DIALOG_CHANGE_RESULTS_VIEW = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_table_web_view);
		/**
		 * Setup analytics.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.setAnonymizeIp(true);
		tracker.startNewSession("UA-29173474-1", 20, ResultViewActivityWebView.this);
		tracker.trackPageView("/ResultViewActivityWebView");

		/**
		 * Read data passed from the calling activity.
		 */
		Bundle extras = getIntent().getExtras();
		if(extras !=null) {
			station_from = extras.getString("station_from");
			station_from_txt = extras.getString("station_from_txt");
			station_to = extras.getString("station_to");
			station_to_txt = extras.getString("station_to_txt");
			time_from = extras.getString("time_from");
			time_from_txt = extras.getString("time_from_txt");
			time_to = extras.getString("time_to");
			time_to_txt = extras.getString("time_to_txt");
			date_today = extras.getString("date_today");
		}

		/**
		 * Get the webview handle.
		 */
		mWebView = (WebView)findViewById(R.id.result_table_web_view);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);

		/**
		 * Display progress Dialog.
		 */
		showDialog(DIALOG_PROGRESS);

		/**
		 * This will execute the "run" method in a new thread.
		 */
		resultsThread = new GetResultsFromSite(handler,
				station_from, station_to,
				time_from, time_to,
				date_today);
		resultsThread.start();
	}

	/**
	 * Handler variable which is used to handle processing after results are received.
	 * 1. Remove the progress dialog
	 * 2. Display the resultant HTML
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.arg1 == Constants.THREAD_GET_RESULTS) {
				if(pd.isShowing() == true) {
					dismissDialog(DIALOG_PROGRESS);
				}
				if(isStop == false) {
					results = resultsThread.GetResults();
					errorCode = resultsThread.GetErrorCode();
					if(results != null) {
						isAddToFavsActive = true;
						resultHTML = createHTMLFromResults(results);
					} else {
						isAddToFavsActive = false;
						resultHTML = createHTMLErrorState(errorCode);
						Log.i(Constants.LOG_TAG, "No Results");
					}
					if(mWebView != null) {
						try {
							mWebView.loadDataWithBaseURL("", resultHTML,"text/html", "UTF-8", null);
						} catch(Exception e) {
							Log.e(Constants.LOG_TAG, "Eror occurred in loadDataWithBaseURL " + e);
							finish();
						}
					} else {
						Log.e(Constants.LOG_TAG, "WebView is NULL");
						finish();
					}
				} else {
					Log.i(Constants.LOG_TAG, "Thread Exited by Force.");
				}
			} else {
				String msgStr = (String)msg.obj;
				Toast.makeText(getBaseContext(), msgStr, Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		tracker.dispatch();
	}

	@Override
	public void onDestroy() {
		super.onStop();
		tracker.stopSession();
		isStop = true;
		if(mWebView != null) {
			mWebView.destroy();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_activity_menu, menu);
		menu.findItem(R.id.search_menu_share).setEnabled(false);
		menu.findItem(R.id.search_menu_share).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search_menu_add_to_fav:
			showDialog(DIALOG_ADD_TO_FAV);
			return true;
		case R.id.search_menu_switch_result_view:
			showDialog(DIALOG_CHANGE_RESULTS_VIEW);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(isAddToFavsActive == false) {
			menu.findItem(R.id.search_menu_add_to_fav).setEnabled(false);
		} else {
			menu.findItem(R.id.search_menu_add_to_fav).setEnabled(true);
		}
		return true;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id) {
			case DIALOG_PROGRESS:
				pd = new ProgressDialog(ResultViewActivityWebView.this);
				pd.setMessage("Fetching Results...");
				pd.setCancelable(true);
				pd.setCanceledOnTouchOutside(false);
				pd.setOnCancelListener(
						new DialogInterface.OnCancelListener(){
							public void onCancel(DialogInterface dialog) {
								finish();
							}
						});
				dialog = pd;
				break;
			case DIALOG_ADD_TO_FAV:
				CommonUtilities cu = new CommonUtilities(ResultViewActivityWebView.this);
				dialog = cu.GetNewFavNameAndAddToFavs(true,
						station_from_txt, station_from,
						station_to_txt, station_to,
						time_from_txt, time_from,
						time_to_txt, time_to, handler);
				break;
			case DIALOG_CHANGE_RESULTS_VIEW:
				dialog = CommonUtilities.GetResultsViewChoiceFromUser(ResultViewActivityWebView.this);
				break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private String createHTMLFromResults(Result results[]) {
		String htmlOutput = "";
		String style = "";
		String strTmp = "";
		htmlOutput += "<html><head>";
		htmlOutput += "<style type=\"text/css\">";
		htmlOutput += 	"tr {background-color:#CBCBCB;}";
		htmlOutput += 	"tr.alt {background-color:#E8E8EA;}";
		htmlOutput += 	"td {border-width:1px;padding:2px;border-color:black;border-style:outset;text-align:center;}";
		htmlOutput += 	"th {background-color:#3C3C3D;color:white;border-width:1px;padding:2px;border-color:black;border-style:outset;text-align:center;}";
		htmlOutput += 	"table {font-size:10px;border-width:1px;border-collapse:collapse;border-color:black;border-style:outset;}";
		htmlOutput += "</style>";
		htmlOutput += "</head><body>";
		htmlOutput += "<table width=\"100%\" align=\"center\">";
		htmlOutput += "<thead><tr>";
		htmlOutput += "<th><a>Arriving at</a><br/><a>" + results[0].startStationName + "</a></th>";
		htmlOutput += "<th><a>Departing from</a><br/><a>" + results[0].startStationName + "</a></th>";
		htmlOutput += "<th><a>Train</a><br/><a>Frequency</a></th>";
		htmlOutput += "<th><a>Arriving at<a><br/><a>Destination</a><br/><a>(" + results[0].endStationName + ")</a></th>";
		htmlOutput += "<th><a>Final</a><br/><a>Destination</a></th>";
		htmlOutput += "<th><a>Train</a><br/><a>Type</a></th>";
		htmlOutput += "</tr></thead>";
		htmlOutput += "<tbody>";
		for(int i = 0; i < results.length; i++) {
			if(i%2 == 0) {
				style = "class=\"alt\"";
			} else {
				style = "";
			}
			htmlOutput += "<tr " + style + ">";
			htmlOutput += "<td>";
			htmlOutput += results[i].arrivalTime_str;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].depatureTime_str;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].fDescription;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].arrivalAtDestinationTime_str;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].toTrStationName;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			strTmp = results[i].tyDescription;
			htmlOutput +=  strTmp;
			htmlOutput += "</td>";
			htmlOutput += "</tr>";
		}
		htmlOutput += "</tbody>";
		htmlOutput += "</table>";
		htmlOutput += "<br/><br/><br/></body></html>";
		return htmlOutput;
	}

	private String createHTMLErrorState(int errorCode) {
		String htmlOutput = "";
		switch(errorCode) {
			case Constants.ERR_NO_RESULTS_FOUND_ERROR:	
				htmlOutput = "<html><head></head><body><h1>Results Not Found.</h1></body></html>";
				break;
			default:
				htmlOutput = "<html><head></head><body><h1>Network Error. Please Try Again.</h1></body></html>";
		}
		return htmlOutput;
	}
}
