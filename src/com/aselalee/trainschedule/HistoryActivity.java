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

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class HistoryActivity extends ListActivity {
	private ParameterSet [] paramsList = null;
	private HisAndFavAdapter adapter = null;
	GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Setup analytics.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.setAnonymizeIp(true);
		tracker.startNewSession("UA-29173474-1", 20, HistoryActivity.this);
		tracker.trackPageView("/HistoryActivity");

		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adv, View view,
					int position, long id) {
				if(paramsList != null) {
					tracker.trackEvent("HistoryActivity", "Get_Results", "List_Item Click", 1);
					getResults(paramsList[position]);
				} else {
					Log.w(Constants.LOG_TAG, "History list is empty");
				}
			}
		});
		adapter = new HisAndFavAdapter(HistoryActivity.this, paramsList, true);
		setListAdapter(adapter);
	}

	@Override
	public void onPause() {
		super.onPause();
		tracker.dispatch();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateHistoryList();

	}

	@Override
	public void onDestroy() {
		super.onStop();
		tracker.stopSession();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.history_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.history_menu_clear_his:
				DBDataAccess myDBAcc = new DBDataAccess(HistoryActivity.this);
				myDBAcc.ClearHistoryTable();
				myDBAcc.close();
				updateHistoryList();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void getResults(ParameterSet paramSet) {
		Intent intent = CommonUtilities.GetResultViewIntent(HistoryActivity.this);
		CommonUtilities.PupulateIntentForResultsActivity(
				paramSet.start_station_val, paramSet.start_station_txt,
				paramSet.end_station_val, paramSet.end_station_txt,
				paramSet.start_time_val, paramSet.start_time_txt,
				paramSet.end_time_val, paramSet.end_time_txt,
				intent);
		startActivity(intent);
	}

	private void updateHistoryList() {
		/**
		 * 1. Get history from database.
		 * 2. Then show it in the list view.
		 * 3. Call this when ever the database is updated.
		 */
		DBDataAccess myDBAcc = new DBDataAccess(HistoryActivity.this);
		paramsList = myDBAcc.GetHistory();
		adapter.paramSet = paramsList;
		myDBAcc.close();
		adapter.notifyDataSetChanged();
		return;
	}
}