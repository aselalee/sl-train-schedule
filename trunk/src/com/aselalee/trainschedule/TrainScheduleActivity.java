/**
* @copyright	Copyright (C) 2010 - 2011 Asela Leelaratne
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class TrainScheduleActivity extends Activity {
	private Button get_given_btn;
	private Button get_all_btn;
	private Spinner spinner_from;
	private Spinner spinner_to;
	private Spinner spinner_times_from;
	private Spinner spinner_times_to;

	/**
	 * Save spinner positions to this file.
	 */
	private static final String PREFERENCES_FILE = "SpinnerPrefs";
	/**
	 * Keys to be saved.
	 */
	private static final String STATION_FROM_POS = "station_from_pos";
	private static final String STATION_TO_POS = "station_to_pos";
	private static final String TIME_FROM_POS = "time_from_pos";
	private static final String TIME_TO_POS = "time_to_pos";
	/**
	 * Default values.
	 */
	private int def_station_from = 246;
	private int def_station_to = 153;
	private int def_time_from = 14;
	private int def_time_to = 19;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /**
         * Setup "spinners"
         */
        
        ArrayAdapter<CharSequence> adapter_stations = ArrayAdapter.createFromResource(
        		this, R.array.stations_array, android.R.layout.simple_spinner_item);
        adapter_stations.setDropDownViewResource(R.layout.list_item);
        spinner_from = (Spinner) findViewById(R.id.station_from);
        spinner_from.setAdapter(adapter_stations);
       	spinner_to = (Spinner) findViewById(R.id.station_to);
       	spinner_to.setAdapter(adapter_stations);

       	ArrayAdapter<CharSequence> adapter_times_from = ArrayAdapter.createFromResource(
       			this, R.array.times_from_array, android.R.layout.simple_spinner_item);
        adapter_times_from.setDropDownViewResource(R.layout.list_item);
       	spinner_times_from = (Spinner) findViewById(R.id.times_from);
        spinner_times_from.setAdapter(adapter_times_from);

       	ArrayAdapter<CharSequence> adapter_times_to = ArrayAdapter.createFromResource(
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
       			show_results();
        	}
        });
       	get_all_btn = (Button) findViewById(R.id.get_all);
       	get_all_btn.setOnClickListener(new View.OnClickListener() {
       		public void onClick(View v) {
       			show_all_results();
        	}
        });
        /**
         * Setup listeners for the time select spinners.
         */
        spinner_times_from.setOnItemSelectedListener(new FromSpinnerOnItemSelectedListener());
        spinner_times_to.setOnItemSelectedListener(new ToSpinnerOnItemSelectedListener());
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
     * Get data from UI elements and calls the next activity to display results.
     */
    private void show_results() {
    	String station_from = map_station(spinner_from.getSelectedItemPosition());
    	String station_to = map_station(spinner_to.getSelectedItemPosition());
    	String time_from = map_time_from(spinner_times_from.getSelectedItemPosition());
    	String time_to = map_time_to(spinner_times_to.getSelectedItemPosition());
    	String date_today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
    	
    	Intent intent = new Intent(this, ResultViewActivity.class);
    	intent.putExtra("station_from", station_from);
    	intent.putExtra("station_to", station_to);
    	intent.putExtra("time_from", time_from);
    	intent.putExtra("time_to", time_to);
    	intent.putExtra("date_today", date_today);
    	startActivity(intent);

    }
    /**
     * Get data from UI elements and calls the next activity to display results.
     * To get the full schedule, times are mapped to least starting time and 
     * most ending time.
     */
    private void show_all_results() {
    	String station_from = map_station(spinner_from.getSelectedItemPosition());
    	String station_to = map_station(spinner_to.getSelectedItemPosition());
    	String time_from = map_time_from(0);
    	String time_to = map_time_to(spinner_times_to.getCount() - 1);
    	String date_today = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
    	
    	Intent intent = new Intent(this, ResultViewActivity.class);
    	intent.putExtra("station_from", station_from);
    	intent.putExtra("station_to", station_to);
    	intent.putExtra("time_from", time_from);
    	intent.putExtra("time_to", time_to);
    	intent.putExtra("date_today", date_today);
    	startActivity(intent);

    }
    /**
     * Map station name in spinner to actual string received by the server.
     */
    private String map_station(int pos) {
    	String station[] = {"ABN","ANM","AUH","APR","AKU","AWP","ALW","ALT","ABA","ABL","APS","AVD",
    						"AND","AGT","AGL","ANP","APT","AKT","ARW","ASL","AWK","AVS","BAD","BNA",
    						"BPA","BPT","BDA","BSL","BOA","BCO","BTU","BEM","BNT","BRL","BLT","BSH",
    						"BSA","BTL","BGH","BJM","CHL","CBY","FOT","CLY","CRD","DRL","DWL","DAG",
    						"DDR","DPM","DLA","DNA","EYA","ELL","EDM","EKM","EVR","EPN","IPZ","GBD",
    						"GLM","GLE","GAL","GOA","GMA","GPH","GPL","GND","GAN","GNW","GDA","GTL",
    						"GEY","GNT","GRB","GGA","GWN","HBD","HBN","HEA","HPT","HAU","HKT","HTN",
    						"HLO","HDP","HML","HKD","HRG","YPP","HMA","HHR","HRP","HLA","HUN","IGH",
    						"IHA","IKT","IWL","IDA","INO","JLA","JAP","KDN","KMA","KGW","KDG","KWE",
    						"KYA","KLW","KKH","KTN","KTS","KMG","KAN","KGD","KDT","KNI","KAW","KPL",
    						"KTL","KAT","KTG","KUG","KKD","CAK","KTK","KEN","KRA","KLA","KNM","KPE",
    						"KEL","KCH","KOG","KLP","KLN","KPN","KON","KOR","KSG","KDA","KHA","KGA",
    						"KOT","KWW","KUD","KMK","KMB","KUR","KRN","LYA","LGM","LNA","LWL","MPA",
    						"MDP","MKI","MGG","MGN","MYA","MAG","MHO","MPL","MPT","MGE","MDA","MTL",
    						"MTR","MEM","MWH","MGD","MWA","MED","MHN","MHJ","MIY","MIR","MIS","MLP",
    						"MLG","MKP","MRT","MLV","MNL","MTG","NAG","NLY","NOA","NHP","NAT","NVP",
    						"NWN","NGM","NGB","NPK","NOR","NUG","OHA","OMT","PDK","PVI","PLL","PUW",
    						"PND","PNG","PNL","PAN","PKU","PHW","PGD","PTP","PPL","PGN","PGS","PDA",
    						"PKP","PRL","PNV","PLD","PLT","PNW","PIN","PGM","PYA","PLN","PLG","PLR",
    						"PON","PTA","PCK","PNI","PTM","PWP","RDL","RGM","RBK","RMA","RGA","RTG",
    						"RML","RCH","RZL","SAL","SUA","SWR","SCR","SED","SMA","SGM","SVP","SYA",
    						"SRP","TLA","TKL","TWG","TAN","TWT","TBL","TLP","TBM","TDK","TDY","TNA",
    						"TIM","TSM","TRH","TCO","TUD","TDR","UDL","UWL","UHM","UDW","UGL","UKL",
    						"ULP","UNW","VCH","VML","VNA","VGD","WDA","WGG","WKL","WHP","WLG","WPA",
    						"WSL","WRW","WTG","WAT","WLA","WGA","WLM","WKD","WEL","WTE","WRD","WWT",
    						"WKA","YGD","YPW","YGM","YTG"};
    	return station[pos];
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
    	SharedPreferences p = c.getSharedPreferences(PREFERENCES_FILE, MODE_WORLD_READABLE);
        /**
         * Get the position and value of the spinner from the file
         */
    	spinner_from.setSelection(p.getInt(STATION_FROM_POS, def_station_from));
    	spinner_to.setSelection(p.getInt(STATION_TO_POS, def_station_to));
    	spinner_times_from.setSelection(p.getInt(TIME_FROM_POS, def_time_from));
    	spinner_times_to.setSelection(p.getInt(TIME_TO_POS, def_time_to));
    }
    /**
     * Write current spinner positions to preferences file.
     */
    private boolean writeCurrentState(Context c) {
    	/**
         * Get the SharedPreferences object for this application
         */
    	SharedPreferences p = c.getSharedPreferences(PREFERENCES_FILE, MODE_WORLD_READABLE);
        /**
         * Get the editor for this object.
         */
        SharedPreferences.Editor e = p.edit();
        /**
         * Write values.
         */
        e.putInt(STATION_FROM_POS, spinner_from.getSelectedItemPosition());
        e.putInt(STATION_TO_POS, spinner_to.getSelectedItemPosition());
        e.putInt(TIME_FROM_POS, spinner_times_from.getSelectedItemPosition());
        e.putInt(TIME_TO_POS, spinner_times_to.getSelectedItemPosition());
        return (e.commit());
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if( !writeCurrentState(this) ) {
    		Toast.makeText(this,
                    "Failed to write state!", Toast.LENGTH_LONG).show();
    	}
    }    
    @Override
    public void onResume() {
        super.onResume();
        readCurrentState(this);
    }
}