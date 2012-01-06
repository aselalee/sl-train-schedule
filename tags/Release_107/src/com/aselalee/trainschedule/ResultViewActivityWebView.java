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

import android.app.Activity;
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

public class ResultViewActivityWebView extends Activity implements Runnable {
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
	private Thread thread = null;
	private volatile boolean isStop = false;
	private boolean isAddToFavsActive = true;
	private String resultHTML = "<html><head><title>Test</title></head><body><h1>Dummy</h1></body></html>";
	
	private volatile Result [] results = null;
	private volatile int errorCode = Constants.ERR_NO_ERROR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_table_web_view);

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
		mWebView.getSettings().setBuiltInZoomControls(true);

		/**
		 * Display progress Dialog.
		 */
		pd = ProgressDialog.show(this, "Working..",
				"Fetching Results...",
				true, true,
				new DialogInterface.OnCancelListener(){
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});

		/**
		 * This will execute the "run" method in a new thread.
		 */
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * run() method that must be implemented when implementing "Runnable" class.
	 */
	public void run() {
		/**
		 * Call the "GetResults" method to retrieve data from server.
		 */
		GetResultsFromSite getResults = new GetResultsFromSite(); 
		results = getResults.GetResultsViaJASON(station_from, station_to, time_from, time_to, date_today);
		if(results == null) {
			errorCode = getResults.GetErrorCode();
		}
		/**
		 * This will send message to the calling thread to continue and display data.
		 */
		Message myMsg = new Message();
		myMsg.arg1 = Constants.THREAD_GET_RESULTS;
		handler.sendMessage(myMsg);
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
				pd.dismiss();
				if(isStop == false) {
					if(results != null) {
						isAddToFavsActive = true;
						resultHTML = createHTMLFromResults(results);
					} else {
						isAddToFavsActive = false;
						resultHTML = createHTMLErrorState(errorCode);
						Log.e(Constants.LOG_TAG, "No Results");
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
	}

	@Override
	public void onDestroy() {
		super.onStop();
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search_menu_add_to_fav:
			Constants.GetNewFavNameAndAddToFavs(this, true,
					station_from_txt, station_from,
					station_to_txt, station_to,
					time_from_txt, time_from,
					time_to_txt, time_to, handler);
			return true;
		case R.id.search_menu_switch_result_view:
			Constants.GetResultsViewChoiceFromUser(this);
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
			htmlOutput += results[i].arrivalTime;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].depatureTime;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].fDescription;
			htmlOutput += "</td>";
			htmlOutput += "<td>";
			htmlOutput +=  results[i].arrivalAtDestinationTime;
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
