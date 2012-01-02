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
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
	private boolean isThreadFavourites = false;
	private String name_txt = "";
	private ProgressDialog pd;
	private Thread thread = null;
	private volatile boolean isStop = false;
	
	private ListView listView = null;
	private TextView tv_from = null;
	private TextView tv_to = null;
	private volatile Result [] results = null;
	private Context myContext = null;
	
	private volatile int errorCode = Constants.ERR_NO_ERROR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_table);
		myContext = this;

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
		isStop = false;
		/**
		 * Get the required handles.
		 * Update start station and end station
		 */
		listView = (ListView) findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					show_details(position);
			}
		});
		tv_from = (TextView) findViewById(R.id.res_table_start_station_name);
		tv_from.setText( Constants.toTitleCase(station_from_txt));
		tv_to = (TextView) findViewById(R.id.res_table_end_station_name);
		tv_to.setText( Constants.toTitleCase(station_to_txt));
		
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
		isThreadFavourites = false;
		thread.start();
	}

	private void show_details(int pos) {
		LayoutInflater factory = LayoutInflater.from(this);
		View detailsView = factory.inflate(R.layout.result_details_dialog, null);
		TextView tv = null;

		/* Arrival at start */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_arr_at_start_txt);
		tv.setText("Arrival at\n" + results[pos].startStationName);
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_arr_at_start_val);
		tv.setText(results[pos].arrivalTime);
		/* Departing from start */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_depart_from_start_txt);
		tv.setText("Departing from\n" + results[pos].startStationName);
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_depart_from_start_val);
		tv.setText(results[pos].depatureTime);
		/* Reaching end */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_reach_dest_txt);
		tv.setText("Reaching\n" + results[pos].endStationName);
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_reach_dest_val);
		tv.setText(results[pos].arrivalAtDestinationTime);
		/* Duration */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_freq_txt);
		tv.setText("Frequency");
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_freq_val);
		tv.setText(results[pos].fDescription);
		/* Duration */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_dur_txt);
		tv.setText("Duration");
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_dur_val);
		tv.setText(results[pos].duration);
		/* Final destination */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_final_dest_txt);
		tv.setText("Final\nDestination");
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_final_dest_val);
		tv.setText(results[pos].toTrStationName);
		/* Train Type */
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_train_type_txt);
		tv.setText("Train Type");
		tv = (TextView)detailsView.findViewById(R.id.result_table_details_train_type_val);
		tv.setText(results[pos].tyDescription);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(detailsView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	/**
	 * run() method that must be implemented when implementing "Runnable" class.
	 */
	public void run() {
		if(isThreadFavourites == false ) {
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
			if(msg.arg1 == Constants.THREAD_GET_RESULTS) {
				pd.dismiss();
				if(isStop == false) {
					if(results != null) {
						listView.setAdapter(new ResultViewAdapter(myContext, results));
					} else {
						setNoResultsState();
						Log.e(Constants.LOG_TAG, "No Results");
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

	private void setNoResultsState() {
		LinearLayout linlay_root = (LinearLayout) findViewById(R.id.res_table_root_linlay);
		LinearLayout linlay_table_head = (LinearLayout) linlay_root.findViewById(R.id.res_table_table_head);
		linlay_table_head.setVisibility(View.GONE);
		LayoutInflater factory = LayoutInflater.from(this);
		View errorView = factory.inflate(R.layout.result_error, null);
		TextView tv = (TextView) errorView.findViewById(R.id.results_error_msg);
		Button bv = (Button) errorView.findViewById(R.id.results_error_button);
		linlay_root.addView(errorView);
		switch(errorCode) {
			case Constants.ERR_NO_RESULTS_FOUND_ERROR:
				tv.setText("No Results ...");
				bv.setText("Back");
				bv.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
							finish();
						}
					});
				break;
			default:
				tv.setText("Network Error ...");
				bv.setText("Retry");
				bv.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Bundle tempBundle = new Bundle();
						onCreate(tempBundle);
						}
					});
		}
	}
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onStop();
		isStop = true;
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
			getNewFavName();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getNewFavName() {
		LayoutInflater factory = LayoutInflater.from(this);
		View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		final EditText et = (EditText)textEntryView.findViewById(R.id.dialog_new_name);
		final CheckBox cb = (CheckBox)textEntryView.findViewById(R.id.dialog_isTimeFilterOnCB);
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
			Toast.makeText(this, "Invalid name", Toast.LENGTH_LONG).show();
			return;
		}
		thread.start();
	}
}
