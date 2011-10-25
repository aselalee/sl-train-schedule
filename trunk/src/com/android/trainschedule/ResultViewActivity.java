package com.android.trainschedule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

public class ResultViewActivity extends Activity implements Runnable {
	String station_from;
	String station_to;
	String time_from;
	String time_to;
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
								 	true, false);

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
		result= GetResultsFromSite.GetResults(station_from, station_to, time_from, time_to);
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
}
