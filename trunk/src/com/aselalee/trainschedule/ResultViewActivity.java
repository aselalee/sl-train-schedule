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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
	private ProgressDialog pd;
	private Thread thread = null;
	private volatile boolean isStop = false;
	private int activePosition = 0;
	private boolean isAddToFavsActive = true;
	
	private ListView listView = null;
	private TextView tv = null;
	private volatile Result [] results = null;
	private Context myContext = null;
	
	private volatile int errorCode = Constants.ERR_NO_ERROR;
	private static final int DIALOG_DETAILS = 1;

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
					activePosition = position;
					showDialog(DIALOG_DETAILS);
			}
		});
		tv = (TextView) findViewById(R.id.res_table_station_names);
		tv.setText( Constants.ToTitleCase(station_from_txt) + " - " +
					Constants.ToTitleCase(station_to_txt));
		
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
		pd.setCanceledOnTouchOutside(false);

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
	 * 2. Display the results
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.arg1 == Constants.THREAD_GET_RESULTS) {
				if(pd.isShowing() == true) {
					pd.dismiss();
				}
				if(isStop == false) {
					if(results != null) {
						isAddToFavsActive = true;
						listView.setAdapter(new ResultViewAdapter(myContext, results));
					} else {
						isAddToFavsActive = false;
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
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
		window.getDecorView().getBackground().setDither(true);
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

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
			case DIALOG_DETAILS:
				dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
				dialog.setContentView(R.layout.result_details_dialog);
				Button button = (Button) dialog.findViewById(R.id.result_table_details_ok_btn);
				button.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dismissDialog(DIALOG_DETAILS);
					}
				});
				break;
		default:
			dialog = null;
		}
		return dialog;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
			case DIALOG_DETAILS:
				set_dialog_details(dialog, activePosition);
				break;
			default:
				return;
		}
	}

	private void set_dialog_details(Dialog dialog, int pos) {
		TextView tv = null;
		/* Arrival at start */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_arr_at_start_txt);
		tv.setText("Arrival at\n" + results[pos].startStationName);
		tv = (TextView)dialog.findViewById(R.id.result_table_details_arr_at_start_val);
		tv.setText(results[pos].arrivalTime);
		/* Departing from start */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_depart_from_start_txt);
		tv.setText("Departing from\n" + results[pos].startStationName);
		tv = (TextView)dialog.findViewById(R.id.result_table_details_depart_from_start_val);
		tv.setText(results[pos].depatureTime);
		/* Reaching end */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_reach_dest_txt);
		tv.setText("Reaching\n" + results[pos].endStationName);
		tv = (TextView)dialog.findViewById(R.id.result_table_details_reach_dest_val);
		tv.setText(results[pos].arrivalAtDestinationTime);
		/* Duration */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_freq_txt);
		tv.setText("Frequency");
		tv = (TextView)dialog.findViewById(R.id.result_table_details_freq_val);
		tv.setText(results[pos].fDescription);
		/* Duration */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_dur_txt);
		tv.setText("Duration");
		tv = (TextView)dialog.findViewById(R.id.result_table_details_dur_val);
		tv.setText(results[pos].duration);
		/* Final destination */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_final_dest_txt);
		tv.setText("Final\nDestination");
		tv = (TextView)dialog.findViewById(R.id.result_table_details_final_dest_val);
		tv.setText(results[pos].toTrStationName);
		/* Train Type */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_train_type_txt);
		tv.setText("Train Type");
		tv = (TextView)dialog.findViewById(R.id.result_table_details_train_type_val);
		tv.setText(results[pos].tyDescription);
	}
}
