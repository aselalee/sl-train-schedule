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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class TrainScheduleActivity extends Activity implements Runnable {
	private LinearLayout lin_lay;
	private Button get_given_btn;
	private Button get_all_btn;
	private Button swap_btn;
	private AutoCompleteTextView actv_from;
	private AutoCompleteTextView actv_to;
	private ArrayAdapter<Station> adapter;
	private ArrayAdapter<CharSequence> adapter_times_from;
	private ArrayAdapter<CharSequence> adapter_times_to;
	private Spinner spinner_times_from;
	private Spinner spinner_times_to;
	private Station stations[];
	private String[] stationsText;
	private String[] stationsVal;
	private String station_from_txt = "";
	private String station_to_txt = "";
	private String station_from_val = "";
	private String station_to_val = "";
	private String time_from_txt = "";
	private String time_to_txt = "";
	private String time_from_val = "";
	private String time_to_val = "";
	private String name_txt = "";
	private boolean isThreadHistory = true;

	/**
	 * Default values.
	 */
	private int def_time_from = 14;
	private int def_time_to = 19;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_user_input);
        /**
         * Setup "AutoCompleteText" views
         */
        populateStations();
        actv_from = (AutoCompleteTextView) findViewById(R.id.stations_from);
        actv_to = (AutoCompleteTextView) findViewById(R.id.stations_to);
        adapter = new ArrayAdapter<Station>(this, R.layout.list_item, stations);
        actv_from.setAdapter(adapter);
        actv_to.setAdapter(adapter);
        actv_from.setOnItemClickListener(new ACTVFromItemClickListner());
        actv_to.setOnItemClickListener(new ACTVToItemClickListner());
        
        /**
         * Setup time "spinner"s
         */
       	adapter_times_from = ArrayAdapter.createFromResource(
       			this, R.array.times_from_array, android.R.layout.simple_spinner_item);
        adapter_times_from.setDropDownViewResource(R.layout.list_item);
       	spinner_times_from = (Spinner) findViewById(R.id.times_from);
        spinner_times_from.setAdapter(adapter_times_from);

       	adapter_times_to = ArrayAdapter.createFromResource(
       			this, R.array.times_to_array, android.R.layout.simple_spinner_item);
       	adapter_times_to.setDropDownViewResource(R.layout.list_item);
       	spinner_times_to = (Spinner) findViewById(R.id.times_to);
       	spinner_times_to.setAdapter(adapter_times_to);
       	/**
       	 * Setup submit buttons.
       	 */
       	get_given_btn = (Button) findViewById(R.id.get_given);
       	get_given_btn.setOnClickListener(new View.OnClickListener() {
       		public void onClick(View v) {
       	    	time_from_val = map_time_from(spinner_times_from.getSelectedItemPosition());
       	    	time_to_val = map_time_to(spinner_times_to.getSelectedItemPosition());
       	    	time_from_txt = spinner_times_from.getSelectedItem().toString();
       	    	time_to_txt = spinner_times_to.getSelectedItem().toString();
       			show_results();
        	}
        });
       	get_all_btn = (Button) findViewById(R.id.get_all);
       	get_all_btn.setOnClickListener(new View.OnClickListener() {
       		public void onClick(View v) {
       		    /**
       		     * To get the full schedule, times are mapped to least starting time and 
       		     * most ending time.
       		     */
       		   	time_from_val = map_time_from(0);
       	    	time_to_val = map_time_to(spinner_times_to.getCount() - 1);
       		   	time_from_txt = "00.00";
       		   	time_to_txt = "24.00";
       			show_results();
        	}
        });
        /**
         * Setup listeners for the time select spinners.
         */
        spinner_times_from.setOnItemSelectedListener(new FromSpinnerOnItemSelectedListener());
        spinner_times_to.setOnItemSelectedListener(new ToSpinnerOnItemSelectedListener());
        /**
         * Get Layout handle.
         */
        lin_lay = (LinearLayout) findViewById(R.id.lin_lay);
        /**
         * Setup swap button.
         */
        swap_btn = (Button) findViewById(R.id.swap);
        swap_btn.setOnClickListener(new View.OnClickListener() {
       		public void onClick(View v) {
       			lin_lay.requestFocusFromTouch();
       			String tmp = station_to_txt;
       			station_to_txt = station_from_txt;
       			station_from_txt = tmp;
       			tmp = station_to_val;
       			station_to_val = station_from_val;
       			station_from_val = tmp;
       			tmp = null;
       			actv_to.setText(station_to_txt);
       			actv_from.setText(station_from_txt);
        	}
        });
    }
    /**
     * Populate stations adapter with string resources.
     */
    private void populateStations( )
    {
    	stationsText = getResources().getStringArray(R.array.stations_array);
    	stationsVal = getResources().getStringArray(R.array.stations_val_array);
    	stations = new Station[stationsText.length];
    	int index = 0;
    	for( index = 0; index < stationsText.length; index++ )
    	{
    		stations[index] = new Station(stationsText[index],stationsVal[index]);
    	}
    }
    /**
     * Checks whether from time is greater than the to time.
     */
    public class FromSpinnerOnItemSelectedListener implements OnItemSelectedListener {
    	public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        		int toPos = spinner_times_to.getSelectedItemPosition();
        		if( pos > toPos) {
        			spinner_times_to.setSelection(pos);
        		}
        }
        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    }
    /**
     * Checks whether the to time is less than the from time.
     */
    public class ToSpinnerOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        		int toPos = spinner_times_from.getSelectedItemPosition();
        		if( pos < toPos) {
        			spinner_times_from.setSelection(pos);
        		}
        }
        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    }
    /**
     * From station AutoCompleteText callback
     */
    private class ACTVFromItemClickListner implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			Station selectedStation = (Station) parent.getItemAtPosition(pos);
			station_from_txt = selectedStation.getText();
			station_from_val = selectedStation.getValue();
			actv_to.requestFocusFromTouch();;
		}
    }
    /**
     * From station AutoCompleteText callback
     */
    private class ACTVToItemClickListner implements OnItemClickListener {
    	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			Station selectedStation = (Station) parent.getItemAtPosition(pos);
			station_to_txt = selectedStation.getText();
			station_to_val = selectedStation.getValue();
			hideSoftKeyboard(actv_to);
			lin_lay.requestFocusFromTouch();
		}
    }
    /**
     * Calls the next activity to display results.
     */
    private void show_results() {
    	/**
    	 * Add params to history database.
    	 * This is done in a separate thread.
    	 */
    	Thread thread = new Thread(this);
    	isThreadHistory = true;
    	thread.start();

    	hideSoftKeyboard(actv_to);
    	if( validateStations() ) {
        	Intent intent = new Intent(this, ResultViewActivity.class);
    	   	populateIntent(intent);
    		startActivity(intent);
    	}
    	return;
    }
    /**
     * Match "from time" in spinner to actual string received by the server.
     */
    private String map_time_from(int pos) {
    	String time_from[] = {"00:00:01","01:00:00","02:00:00","03:00:00","04:00:00","05:00:00",
    						  "06:00:00","07:00:00","08:00:00","09:00:00","10:00:00","11:00:00",
    						  "11:59:59","13:00:00","14:00:00","15:00:00","16:00:00","17:00:00",
    						  "18:00:00","19:00:00","20:00:00","21:00:0,","22:00:00","23:00:00"};
    	return time_from[pos];
    }
    /**
     * Match "to time" in spinner to actual string received by the server.
     */
    private String map_time_to(int pos) {
    	String time_to[] = {"01:00:00","02:00:00","03:00:00","04:00:00","05:00:00","06:00:00","07:00:00",
    					    "08:00:00","09:00:00","10:00:00","11:00:00","11:59:59","13:00:00","14:00:00",
    					    "15:00:00","16:00:00","17:00:00","18:00:00","19:00:00","20:00:00","21:00:00",
    					    "22:00:0,","23:00:00","23:59:59"};
    	return time_to[pos];
    }
    /**
     * Read spinner positions from preference file.
     */
    private void readCurrentState(Context c) {
    	/**
         * Get the SharedPreferences object for this application
         */
    	SharedPreferences p = c.getSharedPreferences(Constants.PREFERENCES_FILE, MODE_WORLD_READABLE);
        /**
         * Get the position and value of the spinner from the file
         */
    	station_from_txt = p.getString(Constants.STATION_FROM_TXT, "");
    	actv_from.setText(station_from_txt);
    	station_from_val = p.getString(Constants.STATION_FROM_VAL, "");
    	station_to_txt = p.getString(Constants.STATION_TO_TXT, "");
    	actv_to.setText(station_to_txt);
    	station_to_val = p.getString(Constants.STATION_TO_VAL, "");
    	spinner_times_from.setSelection(p.getInt(Constants.TIME_FROM_POS, def_time_from));
    	spinner_times_to.setSelection(p.getInt(Constants.TIME_TO_POS, def_time_to));
    }
    /**
     * Write current spinner positions to preferences file.
     */
    private boolean writeCurrentState(Context c) {
    	/**
         * Get the SharedPreferences object for this application
         */
    	SharedPreferences p = c.getSharedPreferences(Constants.PREFERENCES_FILE, MODE_WORLD_READABLE);
        /**
         * Get the editor for this object.
         */
        SharedPreferences.Editor e = p.edit();
        /**
         * Write values.
         */
        e.putString(Constants.STATION_FROM_TXT, station_from_txt);
        e.putString(Constants.STATION_FROM_VAL, station_from_val);
        e.putString(Constants.STATION_TO_TXT, station_to_txt);
        e.putString(Constants.STATION_TO_VAL, station_to_val);
        e.putInt(Constants.TIME_FROM_POS, spinner_times_from.getSelectedItemPosition());
        e.putInt(Constants.TIME_TO_POS, spinner_times_to.getSelectedItemPosition());
        return (e.commit());
    }
    @Override
    public void onPause() {
    	super.onPause();
    	if( !writeCurrentState(this) ) {
    		Toast.makeText(this,
                    "Failed to save state!", Toast.LENGTH_LONG).show();
    	}
    }    
    @Override
    public void onResume() {
        super.onResume();
        readCurrentState(this);
        lin_lay.requestFocusFromTouch();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    }
	public void run() {
    	DBDataAccess myDBAcc = new DBDataAccess(this);
    	if ( isThreadHistory == true) {
    		myDBAcc.PushDataHistory(station_from_txt, station_from_val,
    							station_to_txt, station_to_val,
    							time_from_txt, time_from_val,
    							time_to_txt, time_to_val);
    	}
    	else {
    		myDBAcc.PushDataFavourites(station_from_txt, station_from_val,
					station_to_txt, station_to_val,
					time_from_txt, time_from_val,
					time_to_txt, time_to_val,
					name_txt, this );
    	}
    	myDBAcc.close();
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
	Handler toastHandler = new Handler();
	Runnable toastRunnableOK = new Runnable() {
		public void run() {
			Toast.makeText(getBaseContext(),"Added to favourites.", Toast.LENGTH_SHORT).show();
		}
	};
	Runnable toastRunnableERROR = new Runnable() {
		public void run() {
			Toast.makeText(getBaseContext(),"Error occured.", Toast.LENGTH_SHORT).show();
		}
	};
	Runnable toastRunnableFULL = new Runnable() {
		public void run() {
			Toast.makeText(getBaseContext(),"Hisotry list Full. Delete one or more items.", Toast.LENGTH_SHORT).show();
		}
	};
    private void getNewFavName() {
    	if( validateStations() == false) {
    		return;
    	}
        LayoutInflater factory = LayoutInflater.from(this);
        View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
        final EditText et = (EditText)textEntryView.findViewById(R.id.new_name);
        et.setText(station_from_txt + " - " + station_to_txt);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(textEntryView);
    	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					addParamsToFavs(et.getEditableText().toString());
    					hideSoftKeyboard(et);
    				}
    			});
    	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					hideSoftKeyboard(et);
    					dialog.cancel();
    				}
    			});
    	builder.setTitle("Enter New Name");
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    private void addParamsToFavs(String newName) {
    	Thread thread = new Thread(this);
    	isThreadHistory = false;
	    time_from_val = map_time_from(spinner_times_from.getSelectedItemPosition());
	    time_to_val = map_time_to(spinner_times_to.getSelectedItemPosition());
	    time_from_txt = spinner_times_from.getSelectedItem().toString();
	    time_to_txt = spinner_times_to.getSelectedItem().toString();
	    name_txt = newName;
	    if(name_txt.length() == 0) {
	       	Toast.makeText(this,
                    "Invalid name", Toast.LENGTH_LONG).show();
			return;
		}
    	thread.start();
    }
    /**
     * Private class to hold stations data.
     * This container is used as the adapter object for auto complete text
     * views.
     */
    private class Station {
        private String text;
        private String value;

        public Station( String textStr, String valueStr ) {
            this.text = textStr;
            this.value = valueStr;
        }

        public String getText() {
            return this.text;
        }

        public String getValue() {
            return this.value;
        }

        public String toString() {
            return this.text;
        }
    }
    /**
     * Utility functions.
     */
    private boolean validateStations() {
    	if( (searchString(stationsText, actv_from.getText().toString()) > -1) &&
        		(searchString(stationsText, actv_to.getText().toString()) > -1) ) {
        		return true;
        	}
       	Toast.makeText(this,
                        "Invalid Station Names", Toast.LENGTH_LONG).show();
        return false;
    }
    private int searchString(String strArray[], String strSrc) {
    	for( int i = 0; i < strArray.length; i++) {
    		if( strArray[i].equals(strSrc) ) {
    			return i;
    		}
    	}
    	return -1;
    }
    private void hideSoftKeyboard(View actv) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(actv.getWindowToken(), 0);
    }
    private void populateIntent(Intent intent) {
    	String date_today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
    	intent.putExtra("station_from", station_from_val);
    	intent.putExtra("station_from_txt", station_from_txt);
    	intent.putExtra("station_to", station_to_val);
    	intent.putExtra("station_to_txt", station_to_txt);
    	intent.putExtra("time_from", time_from_val);
    	intent.putExtra("time_from_txt", time_from_txt);
    	intent.putExtra("time_to", time_to_val);
    	intent.putExtra("time_to_txt", time_to_txt);
    	intent.putExtra("date_today", date_today);
    }
}