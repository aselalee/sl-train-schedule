/**
* @copyright	Copyright (C) 2010 - 2011 Asela Leelaratne
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
import android.webkit.WebView;

public class ResultViewActivity extends Activity implements Runnable {
	String station_from;
	String station_to;
	String time_from;
	String time_to;
	String date_today;
	String result;
	
	WebView mWebView;
	private ProgressDialog pd;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.result_table);

	    	/**
	    	 * Read data passed from the calling activity.
	    	 */
	    	Bundle extras = getIntent().getExtras(); 
	    	if(extras !=null) {
	    		station_from = extras.getString("station_from");
	    		station_to = extras.getString("station_to");
	    		time_from = extras.getString("time_from");
	    		time_to = extras.getString("time_to");
	    		date_today = extras.getString("date_today");
	    	}

	    	/**
	    	 * Get the webview handle.
	    	 */
	    	mWebView = (WebView) findViewById(R.id.webview);
	    
	    	/**
	    	 * Display progress Dialog.
	    	 */
	    	pd = ProgressDialog.show(this, "Working..",
								 	"Getting Results from www.railway.gov.lk",
								 	true, true,
								 	new DialogInterface.OnCancelListener(){
                						public void onCancel(DialogInterface dialog) {
                							finish();
                						}
	    							});

	    	/**
	    	 * This will execute the "run" method in a new thread.
	    	 */
	    	Thread thread = new Thread(this);
	    	thread.start();
	}
	/**
	 * run() method that must be implemented when implementing "Runnable" class.
	 */
	public void run() {
		/**
		 * Call the "GetResults" method to retrieve data from server.
		 */
		result= GetResultsFromSite.GetResults(station_from, station_to, time_from, time_to, date_today);
		/**
		 * This will send message to the calling thread to continue and display data.
		 */
		handler.sendEmptyMessage(0);
	}
	/**
	 * Handler variable which is used to handle processing after results are received.
	 * 1. Remove the progress dialog
	 * 2. Display the resultant HTML
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
		    mWebView.loadDataWithBaseURL("", result,"text/html", "UTF-8", null);
		}
	};
	
    @Override
    public void onPause() {
    	super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    }
}
