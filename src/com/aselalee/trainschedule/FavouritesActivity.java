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

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FavouritesActivity extends ListActivity {
	private ParameterSet [] paramsList = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    ListView lv = getListView();
        registerForContextMenu (lv);
	    lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adv, View view,
									int position, long id) {
				if( paramsList != null) {
					getResults(paramsList[position]);
				}
				else {
					Log.w(Constants.LOG_TAG, "Favourites list is empty");
				}
			}
	    });
	}
    @Override
    public void onPause() {
    	super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    	/**
    	 * 1. Get history from database.
    	 * 2. Then show it in the list view.
    	 */
	    DBDataAccess myDBAcc = new DBDataAccess(this);
	    paramsList = myDBAcc.GetFavourites();
	    if(paramsList == null) {
	    	Log.e(Constants.LOG_TAG, "Parameter List not populated properly");
	    	return;
	    }
	    myDBAcc.close();
	    setListAdapter(new HisAndFavAdapter(this, paramsList, false));
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.favourites_activity_menu, menu);
    	return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.clear_fav:
    			DBDataAccess myDBAcc = new DBDataAccess(this);
    			myDBAcc.ClearFavouritesTable();
    			myDBAcc.close();
    			onResume();
    			return true;
        default:
        	return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    								ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.favourites_activity_context_menu, menu);
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    	menu.setHeaderTitle(paramsList[info.position].name);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch (item.getItemId()) {
    		case R.id.rename_fav:
    			Log.i(Constants.LOG_TAG, String.valueOf(paramsList[info.position].id) + "/" + paramsList[info.position].name);
    			return true;
    		default:
    			return super.onContextItemSelected(item);
      }
    }
    private void getResults(ParameterSet paramSet) {
    	Intent intent = new Intent(this, ResultViewActivity.class);
    	String date_today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
    	intent.putExtra("station_from", paramSet.start_station_val);
    	intent.putExtra("station_to", paramSet.end_station_val);
    	intent.putExtra("time_from", paramSet.start_time_val);
    	intent.putExtra("time_to", paramSet.end_time_val);
    	intent.putExtra("date_today", date_today);
    	startActivity(intent);
    }
}
