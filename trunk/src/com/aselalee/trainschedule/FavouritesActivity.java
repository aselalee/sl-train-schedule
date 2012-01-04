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
import android.app.ListActivity;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
				if(paramsList != null) {
					getResults(paramsList[position]);
				} else {
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
		 * 1. Get favourites from database.
		 * 2. Then show it in the list view.
		 * 3. Call this when ever the database is updated.
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
			case R.id.fav_menu_clear_fav:
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
		DBDataAccess myDBAcc = null;
		switch (item.getItemId()) {
			case R.id.fav_ctx_menu_rename_fav:
				myDBAcc = new DBDataAccess(this);
				renameFavItem(info.position, myDBAcc);
				break;
			case R.id.fav_ctx_menu_delete_fav:
				myDBAcc = new DBDataAccess(this);
				myDBAcc.DeleteFavRecord(paramsList[info.position].id);
				break;
			default:
				return super.onContextItemSelected(item);
		}
		myDBAcc.close();
		onResume();
		return true;
	}

	private void getResults(ParameterSet paramSet) {
		Intent intent = Constants.GetResultViewIntent(this);
		Constants.PupulateIntentForResultsActivity(
				paramSet.start_station_val, paramSet.start_station_txt,
				paramSet.end_station_val, paramSet.end_station_txt,
				paramSet.start_time_val, paramSet.start_time_txt,
				paramSet.end_time_val, paramSet.end_time_txt,
				intent);
		startActivity(intent);
	}

	private void renameFavItem(final int itemPosition, final DBDataAccess myDBAcc) {
		LayoutInflater factory = LayoutInflater.from(this);
		View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
		final EditText et = (EditText)textEntryView.findViewById(R.id.dialog_new_name);
		et.setText(paramsList[itemPosition].name);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(textEntryView);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String newName = "";
				newName = et.getEditableText().toString();
				if(newName.length() != 0) {
					myDBAcc.RenameFavRecord(paramsList[itemPosition].id, newName);
				} else {
					runToast("Invalid name.");
				}
				Constants.HideSoftKeyboard(et, getBaseContext());
				onResume();
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

	private void runToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
