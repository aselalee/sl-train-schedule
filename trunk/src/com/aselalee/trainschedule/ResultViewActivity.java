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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ResultViewActivity extends Activity implements Runnable {
	private String station_from;
	private String station_from_txt;
	private String station_to;
	private String station_to_txt;
	private String time_from;
	private String time_from_txt;
	private String time_to;
	private String time_to_txt;
	private String date_today;
	private String result;
	private boolean isThreadFavourites = false;
	private String name_txt = "";
	
	private WebView mWebView = null;
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
    	mWebView = (WebView) findViewById(R.id.webview);
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
    	Thread thread = new Thread(this);
    	isThreadFavourites = false;
    	thread.start();
	}
	/**
	 * run() method that must be implemented when implementing "Runnable" class.
	 */
	public void run() {
		if( isThreadFavourites == false ) {
			/**
			 * Call the "GetResults" method to retrieve data from server.
			 */
			result= GetResultsFromSite.GetResultsJson(station_from, station_to, time_from, time_to, date_today);
			/**
			 * This will send message to the calling thread to continue and display data.
			 */
			Message myMsg = new Message();
			myMsg.arg1 = Constants.THREAD_GET_RESULTS;
			handler.sendMessage(myMsg);
		} else {
			DBDataAccess myDBAcc = new DBDataAccess(this);
    		myDBAcc.PushDataFavourites(station_from_txt, station_from,
					station_to_txt, station_to,
					time_from_txt, time_from,
					time_to_txt, time_to,
					name_txt, handler );
    		myDBAcc.close();
		}
	}
	/**
	 * Handler variable which is used to handle processing after results are received.
	 * 1. Remove the progress dialog
	 * 2. Display the resultant HTML
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if( msg.arg1 == Constants.THREAD_GET_RESULTS) {
				pd.dismiss();
				if( mWebView != null && result != null) {
					try {
						mWebView.loadDataWithBaseURL("", result,"text/html", "UTF-8", null);
					} catch (Exception e) {
						Log.e(Constants.LOG_TAG, "Eror occurred in loadDataWithBaseURL " + e);
					}
				} else {
					Toast.makeText(getBaseContext(), "Error occured. Please try again.", Toast.LENGTH_SHORT).show();
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
    	if( mWebView != null) {
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
        case R.id.add_to_fav:
        	getNewFavName();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    private void getNewFavName() {
        LayoutInflater factory = LayoutInflater.from(this);
        View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
        final EditText et = (EditText)textEntryView.findViewById(R.id.new_name);
        final CheckBox cb = (CheckBox)textEntryView.findViewById(R.id.isTimeFilterOnCB);
        cb.setVisibility(View.GONE);
        et.setText(station_from_txt + " - " + station_to_txt);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(textEntryView);
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					addParamsToFavs(et.getEditableText().toString());
    					Constants.HideSoftKeyboard(et, getBaseContext());
    				}
    			});
    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					Constants.HideSoftKeyboard(et, getBaseContext());
    					dialog.cancel();
    				}
    			});
    	builder.setTitle("Enter New Name");
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    private void addParamsToFavs(String newName) {
    	Thread thread = new Thread(this);
    	isThreadFavourites = true;
	    name_txt = newName;
	    if(name_txt.length() == 0) {
	       	Toast.makeText(this,
                    "Invalid name", Toast.LENGTH_LONG).show();
			return;
		}
    	thread.start();
    }
}
