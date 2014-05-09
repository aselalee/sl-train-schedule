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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FavouritesActivity extends ListActivity {
	private ParameterSet [] paramsList = null;
	private HisAndFavAdapter adapter = null;
	private int postionToRename = 0;
	private AnalyticsWrapper tracker;

	private static final int DIALOG_RENAME_FAV = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Setup analytics.
		 */
		tracker = new AnalyticsWrapper(FavouritesActivity.this);
		tracker.TrackPageView("/FavouritesActivity");

		ListView lv = getListView();
		registerForContextMenu (lv);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adv, View view,
					int position, long id) {
				if(paramsList != null) {
					tracker.TrackEvent("FavouritesAcitiviy", "Get_Results", "List_Item_Click", 1);
					getResults(paramsList[position]);
				} else {
					Log.w(Constants.LOG_TAG, "Favourites list is empty");
				}
			}
		});
		adapter = new HisAndFavAdapter(FavouritesActivity.this, paramsList, false);
		setListAdapter(adapter);
	}

	@Override
	public void onPause() {
		super.onPause();
		tracker.Dispatch();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateFavList();
	}

	@Override
	public void onDestroy() {
		super.onStop();
		tracker.Dispatch();
		tracker.StopSession();
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
			case R.id.fav_menu_clear_fav:
				DBDataAccess myDBAcc = new DBDataAccess(FavouritesActivity.this);
				myDBAcc.ClearFavouritesTable();
				myDBAcc.close();
				myDBAcc = null;
				updateFavList();
				tracker.TrackEvent("FavouritesAcitiviy", "Clear_Favs", "Menu_Click", 1);
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
			case R.id.fav_ctx_menu_rename_fav:
				postionToRename = info.position;
				tracker.TrackEvent("FavouritesAcitiviy", "Rename_Fav", "Ctx_Menu_Click", 1);
				showDialog(DIALOG_RENAME_FAV);
				break;
			case R.id.fav_ctx_menu_delete_fav:
				DBDataAccess myDBAcc = new DBDataAccess(FavouritesActivity.this);
				myDBAcc.DeleteFavRecord(paramsList[info.position].id);
				myDBAcc.close();
				myDBAcc = null;
				tracker.TrackEvent("FavouritesAcitiviy", "Delete_Fav", "Ctx_Menu_Click", 1);
				break;
			default:
				return super.onContextItemSelected(item);
		}
		updateFavList();
		return true;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id) {
			case DIALOG_RENAME_FAV:
				dialog = renameFavItem();
				break;
			default:
				dialog = null;
		}
		return dialog;
	}
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
			case DIALOG_RENAME_FAV:
				EditText et = (EditText)dialog.findViewById(R.id.dialog_new_name);
				et.setText(paramsList[postionToRename].name);
				et.setSelection(et.getText().length());
				break;
			default:
				return;
		}
		return;
	}

	private void getResults(ParameterSet paramSet) {
		Intent intent = new Intent(FavouritesActivity.this, ResultViewActivity.class);
		String date_today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
		CommonUtilities.PupulateIntentForResultsActivity(
				paramSet.start_station_val, paramSet.start_station_txt,
				paramSet.end_station_val, paramSet.end_station_txt,
				paramSet.start_time_val, paramSet.start_time_txt,
				paramSet.end_time_val, paramSet.end_time_txt,
				date_today, intent);
		startActivity(intent);
	}

	private Dialog renameFavItem() {
		final Context thisContext = FavouritesActivity.this;
		LayoutInflater factory = LayoutInflater.from(FavouritesActivity.this);
		View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		final EditText et = (EditText)textEntryView.findViewById(R.id.dialog_new_name);
		final CheckBox cb = (CheckBox)textEntryView.findViewById(R.id.dialog_isTimeFilterOnCB);
		cb.setVisibility(View.GONE);
		AlertDialog.Builder builder = new AlertDialog.Builder(FavouritesActivity.this);
		builder.setView(textEntryView);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newName = "";
				newName = et.getEditableText().toString();
				if(newName.length() != 0) {
					DBDataAccess myDBAcc = new DBDataAccess(thisContext);
					myDBAcc.RenameFavRecord(paramsList[postionToRename].id, newName);
					myDBAcc.close();
					myDBAcc = null;
				} else {
					Toast.makeText(thisContext, "Invalid name.", Toast.LENGTH_LONG).show();
				}
				CommonUtilities.HideSoftKeyboard(et, getBaseContext());
				updateFavList();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				CommonUtilities.HideSoftKeyboard(et, getBaseContext());
				dialog.cancel();
			}
		});
		builder.setTitle("Enter New Name");
		AlertDialog alert = builder.create();
		return alert;
	}

	private void updateFavList() {
		/**
		 * 1. Get favourites from database.
		 * 2. Then show it in the list view.
		 * 3. Call this when ever the database is updated.
		 */
		DBDataAccess myDBAcc = new DBDataAccess(FavouritesActivity.this);
		paramsList = myDBAcc.GetFavourites();
		adapter.paramSet = paramsList;
		myDBAcc.close();
		myDBAcc = null;
		adapter.notifyDataSetChanged();
		return;
	}
}
