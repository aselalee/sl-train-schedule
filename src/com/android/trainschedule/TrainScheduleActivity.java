package com.android.trainschedule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class TrainScheduleActivity extends Activity {
	private Button submit_btn;
	private Spinner spinner_from;
	private Spinner spinner_to;
	private Spinner spinner_times_from;
	private Spinner spinner_times_to;
	private boolean isInit = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if( isInit == false) {
        	/**
        	 * Setup "spinners"
        	 */
        	spinner_from = (Spinner) findViewById(R.id.station_from);
        	ArrayAdapter<CharSequence> adapter_from = ArrayAdapter.createFromResource(
                this, R.array.stations_array, android.R.layout.simple_spinner_item);
        	adapter_from.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	spinner_from.setAdapter(adapter_from);
        	spinner_from.setSelection(246);
        
        	spinner_to = (Spinner) findViewById(R.id.station_to);
        	ArrayAdapter<CharSequence> adapter_to = ArrayAdapter.createFromResource(
                this, R.array.stations_array, android.R.layout.simple_spinner_item);
        	adapter_to.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	spinner_to.setAdapter(adapter_to);
        	spinner_to.setSelection(153);
        
        	spinner_times_from = (Spinner) findViewById(R.id.times_from);
        	ArrayAdapter<CharSequence> adapter_times_from = ArrayAdapter.createFromResource(
                this, R.array.times_from_array, android.R.layout.simple_spinner_item);
        	adapter_times_from.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	spinner_times_from.setAdapter(adapter_times_from);
        	spinner_times_from.setSelection(14);
        
        	spinner_times_to = (Spinner) findViewById(R.id.times_to);
        	ArrayAdapter<CharSequence> adapter_times_to = ArrayAdapter.createFromResource(
                this, R.array.times_to_array, android.R.layout.simple_spinner_item);
        	adapter_times_to.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	spinner_times_to.setAdapter(adapter_times_to);
        	spinner_times_to.setSelection(19);
        	/**
        	 * Setup submit button.
        	 */
        	submit_btn = (Button) findViewById(R.id.submit);
        	submit_btn.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) {
        			show_results();
        		}
        	});
        	isInit = true;
        	/**
        	 * Setup listeners for the time select spinners.
        	 */
        	spinner_times_from.setOnItemSelectedListener(new FromSpinnerOnItemSelectedListener());
        	spinner_times_to.setOnItemSelectedListener(new ToSpinnerOnItemSelectedListener());
        }
    }
    /**
     * Checks whether from time is greater than the to time.
     */
    public class FromSpinnerOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        		int toPos = spinner_times_to.getFirstVisiblePosition();
        		if( pos > toPos) {
        			spinner_times_to.setSelection(pos);
        		}
        }
        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    /**
     * Checks whether the to time is less than the from time.
     */
    public class ToSpinnerOnItemSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        		int toPos = spinner_times_from.getFirstVisiblePosition();
        		if( pos < toPos) {
        			spinner_times_from.setSelection(pos);
        		}
        }
        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    /**
     * Calls the next activity to display results.
     */
    private void show_results() {
    	String station_from = map_station(spinner_from.getFirstVisiblePosition());
    	String station_to = map_station(spinner_to.getFirstVisiblePosition());
    	String time_from = map_time_from(spinner_times_from.getFirstVisiblePosition());
    	String time_to = map_time_to(spinner_times_to.getFirstVisiblePosition());
    	
    	Intent intent = new Intent(this, ResultViewActivity.class);
    	intent.putExtra("station_from", station_from);
    	intent.putExtra("station_to", station_to);
    	intent.putExtra("time_from", time_from);
    	intent.putExtra("time_to", time_to);
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
    
    @Override
    public void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
}