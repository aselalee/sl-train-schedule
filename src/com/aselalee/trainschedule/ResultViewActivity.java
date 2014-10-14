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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultViewActivity extends Activity{
	private String station_from;
	private String station_from_txt;
	private String station_to;
	private String station_to_txt;
	private String time_from;
	private String time_from_txt;
	private String time_to;
	private String time_to_txt;
	private String query_date;
	private ProgressDialog pd;
	private GetResultsFromSiteV2 resultsThread = null;
	private boolean isStop = false;
	private int activePosition = 0;
	private boolean isAddToFavsActive = true;
	private ListView listView = null;
	private ResultViewAdapter adapter = null;
	private TextView tv = null;
	private Result [] results = null;
	private String pricesStr = "";
	private Context myContext = null;
	private int errorCode = Constants.ERR_NO_ERROR;
	private String selectedResult = "";
	private AnalyticsWrapper tracker;

	private static final int DIALOG_DETAILS = 1;
	private static final int DIALOG_PROGRESS = 2;
	private static final int DIALOG_ADD_TO_FAV = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Setup analytics.
		 */
		tracker = new AnalyticsWrapper(ResultViewActivity.this);
		tracker.TrackPageView("/ResultViewActivity");

		myContext = ResultViewActivity.this;

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
			query_date = extras.getString("query_date");
		}
		isStop = false;
		isAddToFavsActive = true;
		/**
		 * Setup UI. Call this function when configuration changes
		 * to re-setup the UI elements. This is because a different
		 * layout is used for landscape.
		 */
		setupUI();
		/**
		 * Display progress Dialog.
		 */
		showDialog(DIALOG_PROGRESS);

		/**
		 * This will execute the "run" method in a new thread.
		 */
		resultsThread = new GetResultsFromSiteV2(handler,
				station_from, station_to,
				time_from, time_to,
				query_date);
		resultsThread.start();
	}

	private void setupUI() {
		setContentView(R.layout.result_table);
		/**
		 * Get the required handles.
		 * Update start station and end station
		 */
		listView = (ListView) findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					activePosition = position;
					tracker.TrackEvent("ResultViewActivity", "Get_Result_Details", "List_Item_Click", 1);
					showDialog(DIALOG_DETAILS);
			}
		});
		tv = (TextView) findViewById(R.id.res_table_station_names);
		tv.setText( CommonUtilities.ToTitleCase(station_from_txt) + " - " +
				CommonUtilities.ToTitleCase(station_to_txt) + " (" + query_date + ")");
		return;
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
					dismissDialog(DIALOG_PROGRESS);
				}
				if(isStop == false) {
					results = resultsThread.GetResults();
					errorCode = resultsThread.GetErrorCode();
					if(results != null) {
						isAddToFavsActive = true;
						formatPricesString();
						setupPricesView();
						adapter = new ResultViewAdapter(myContext, results);
						listView.setAdapter(adapter);
					} else {
						isAddToFavsActive = false;
						setNoResultsState();
						Log.i(Constants.LOG_TAG, "No Results");
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

	private void formatPricesString() {
		float [] prices = resultsThread.GetPrices();
		if (prices == null) {
			pricesStr = "Ticket Prices Unknown";
			return;
		}
		pricesStr = "";
		switch(prices.length) {
			case 3:
				pricesStr = ", 3<sup>rd</sup> - Rs." + String.format("%.2f", prices[2]);
				/* No break */
			case 2:
				pricesStr = ", 2<sup>nd</sup> - Rs." + String.format("%.2f", prices[1]) + pricesStr;
				/* No break */
			case 1:
				pricesStr = "1<sup>st</sup> - Rs." + String.format("%.2f", prices[0]) + pricesStr;
				break;
			default:
				Log.e(Constants.LOG_TAG, "Unsupported rates count");
				pricesStr = "Ticket Prices Unknown";
		}
	}
	private void setupPricesView() {
		tv = (TextView) findViewById(R.id.res_table_prices);
		tv.setText(Html.fromHtml(pricesStr));
		LayoutParams parms = (LinearLayout.LayoutParams)tv.getLayoutParams();
		parms.height = (int) (tv.getTextSize() + 20);
		tv.setLayoutParams(parms);

		return;
	}
	private void setNoResultsState() {
		LinearLayout linlay_root = (LinearLayout) findViewById(R.id.res_table_root_linlay);
		LinearLayout linlay_table_head = (LinearLayout) linlay_root.findViewById(R.id.res_table_table_head);
		linlay_table_head.setVisibility(View.GONE);
		TextView tv_head = (TextView) linlay_root.findViewById(R.id.res_table_prices);
		tv_head.setVisibility(View.GONE);
		LayoutInflater factory = LayoutInflater.from(ResultViewActivity.this);
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
				String msg = "Train schedule search service provided by ICTA is unavailable. Please try again later.\n\n";
				msg += "The availability of this service and the accuracy of information provided is beyond my control.\n\n";
				msg += "Contact info of ICTA:\n";
				msg += "Email: info@icta.lk\n";
				msg += "Phone: +94-11-236 9100\n";
				msg += "URL: http://www.icta.lk";
				tv.setText(msg);
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
		tracker.Dispatch();
	}

	@Override
	public void onDestroy() {
		super.onStop();
		isStop = true;
		tracker.Dispatch();
		tracker.StopSession();
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
		/**
		 * isAddToFavsActive == false means that some error occurred
		 * and data is not present to display in the list. Activity
		 * is showing an error state. So do nothing. 
		 */
		if(isAddToFavsActive == true) {
			setupUI();
			if(adapter != null && results != null) {
				setupPricesView();
				listView.setAdapter(adapter);
			} else {
				/**
				 * User changed the orientation of the device while
				 * fetching results from the server. Do nothing.
				 * Screen will be updated when results are available.
				 */
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.results_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.results_menu_add_to_fav:
			tracker.TrackEvent("ResultViewActivity", "Add_to_Favs", "Menu_Click", 1);
			showDialog(DIALOG_ADD_TO_FAV);
			return true;
		case R.id.results_menu_share:
			tracker.TrackEvent("ResultViewActivity", "Send_All_Result", "Menu_Click", 1);
			String msg = CommonUtilities.FormatResultForSharing(results);
			CommonUtilities.ShareResult(ResultViewActivity.this,
										msg,
										"Train Schedule");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(isAddToFavsActive == false) {
			menu.findItem(R.id.results_menu_add_to_fav).setEnabled(false);
			menu.findItem(R.id.results_menu_share).setEnabled(false);
		} else {
			menu.findItem(R.id.results_menu_add_to_fav).setEnabled(true);
			menu.findItem(R.id.results_menu_share).setEnabled(true);
		}
		return true;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id) {
			case DIALOG_DETAILS:
				dialog = new Dialog(ResultViewActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
				dialog.setContentView(R.layout.result_details_dialog);
				Button button = (Button) dialog.findViewById(R.id.result_table_details_back_btn);
				button.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dismissDialog(DIALOG_DETAILS);
					}
				});
				button = (Button) dialog.findViewById(R.id.result_table_details_send_btn);
				button.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						tracker.TrackEvent("ResultViewActivity", "Send_Result", "Details_Dialog_Click", 1);
						CommonUtilities.ShareResult(ResultViewActivity.this,
								selectedResult,
								"I'll be on this train!!!");
					}
				});
				/**
				 * Dim background.
				 */
				WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
				lp.dimAmount=0.6f;  
				dialog.getWindow().setAttributes(lp);  
				dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
				break;
			case DIALOG_PROGRESS:
				pd = new ProgressDialog(ResultViewActivity.this);
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
				CommonUtilities cu = new CommonUtilities(ResultViewActivity.this);
				dialog = cu.GetNewFavNameAndAddToFavs(true,
						station_from_txt, station_from,
						station_to_txt, station_to,
						time_from_txt, time_from,
						time_to_txt, time_to, handler);
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
			case DIALOG_PROGRESS:
			case DIALOG_ADD_TO_FAV:
				break;
			default:
				return;
		}
		return;
	}

	private void set_dialog_details(Dialog dialog, int pos) {
		TextView tv = null;
		if(results == null || 0 > pos || results.length < pos || results[pos] == null) {
			dismissDialog(DIALOG_DETAILS);
			return;
		}
		selectedResult = "";
		selectedResult += "From: ";
		selectedResult += results[pos].startStationName;
		selectedResult += "\n";
		selectedResult += "To: ";
		selectedResult += results[pos].endStationName;
		selectedResult += "\n";
		selectedResult += "Dep: ";
		selectedResult += results[pos].depatureTime_str;
		selectedResult += "\n";
		selectedResult += "Arr: ";
		selectedResult += results[pos].arrivalAtDestinationTime_str;
		selectedResult += "\n";
		selectedResult += "Dur: ";
		selectedResult += results[pos].duration_str;
		selectedResult += "\n";
		selectedResult += "Freq: ";
		selectedResult += results[pos].fDescription_original;
		
		/* Arrival at start */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_arr_at_start_txt);
		tv.setText("Arrival at\n" + results[pos].startStationName);
		tv = (TextView)dialog.findViewById(R.id.result_table_details_arr_at_start_val);
		tv.setText(results[pos].arrivalTime_str);
		/* Departing from start */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_depart_from_start_txt);
		tv.setText("Departing from\n" + results[pos].startStationName);
		tv = (TextView)dialog.findViewById(R.id.result_table_details_depart_from_start_val);
		tv.setText(results[pos].depatureTime_str);
		/* Reaching end */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_reach_dest_txt);
		tv.setText("Reaching\n" + results[pos].endStationName);
		tv = (TextView)dialog.findViewById(R.id.result_table_details_reach_dest_val);
		tv.setText(results[pos].arrivalAtDestinationTime_str);
		/* Frequency */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_freq_txt);
		tv.setText("Frequency");
		tv = (TextView)dialog.findViewById(R.id.result_table_details_freq_val);
		tv.setText(results[pos].fDescription);
		/* Duration */
		tv = (TextView)dialog.findViewById(R.id.result_table_details_dur_txt);
		tv.setText("Duration");
		tv = (TextView)dialog.findViewById(R.id.result_table_details_dur_val);
		tv.setText(results[pos].duration_str);
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
