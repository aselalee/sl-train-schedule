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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HisAndFavAdapter extends BaseAdapter {
	public ParameterSet [] paramSet = null;
	private Context mContext = null;
	private boolean isHistory = true;

	public HisAndFavAdapter(Context context, ParameterSet [] params, boolean for_history) {
		paramSet = params;
		mContext = context;
		isHistory = for_history;
	}

	public int getCount() {
		if(paramSet != null) {
			return paramSet.length;
		}
		return 0;
	}

	public Object getItem(int position) {
		/**
		 * Since we are the ones whose using this value
		 * position is enough. We will use this as an integer
		 * when using in the getView.
		 */
		return position;
	}

	public long getItemId(int position) {
		return paramSet[position].id;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if(isHistory == true) {
			ItemViewHistory iv = null;
			if(convertView == null) {
				iv = new ItemViewHistory(mContext, paramSet[position]);
			} else {
				iv = (ItemViewHistory) convertView;
				iv.setParams(paramSet[position]);
			}
			return iv;
		} else {
			ItemViewFavourites iv = null;
			if(convertView == null) {
				iv = new ItemViewFavourites(mContext, paramSet[position]);
			} else {
				iv = (ItemViewFavourites) convertView;
				iv.setParams(paramSet[position]);
			}
			return iv;
		}
	}

	/**
	 * History Item view.
	 */
	private class ItemViewHistory extends LinearLayout {
		private TextView startStationTV = null;
		private TextView endStationTV = null;
		private TextView filterTV = null;
		public ItemViewHistory(Context context, ParameterSet params) {
			super(context);
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view=layoutInflater.inflate(R.layout.his_list_item, this, true);
			startStationTV = (TextView)view.findViewById(R.id.hist_list_item_start_station_name);
			endStationTV = (TextView)view.findViewById(R.id.hist_list_item_end_station_name);
			filterTV = (TextView)view.findViewById(R.id.hist_list_item_filter_time);
			setParams(params);
		}
		public void setParams(ParameterSet params) {
			startStationTV.setText("\t" + params.start_station_txt);
			endStationTV.setText("\t" + params.end_station_txt);
			filterTV.setText(
							"Time Filter: " + 
							params.start_time_txt + " to " + params.end_time_txt + 
							" (" + params.date_txt + ")"
							);
		}
	}

	/**
	 * Favourites Item view.
	 */
	private class ItemViewFavourites extends LinearLayout {
		private TextView startStationTV = null;
		private TextView endStationTV = null;
		private TextView filterTV = null;
		private TextView nameTV = null;
		
		public ItemViewFavourites(Context context, ParameterSet params) {
			super(context);
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view=layoutInflater.inflate(R.layout.fav_list_item, this, true);
			startStationTV = (TextView)view.findViewById(R.id.fav_start_station_name);
			endStationTV = (TextView)view.findViewById(R.id.fav_end_station_name);
			filterTV = (TextView)view.findViewById(R.id.fav_filter_time);
			nameTV = (TextView)view.findViewById(R.id.fav_item_name);
			setParams(params);
		}
		
		public void setParams(ParameterSet params) {
			startStationTV.setText(params.start_station_txt);
			endStationTV.setText(params.end_station_txt);
			filterTV.setText("Time Filter: " + params.start_time_txt + " to " + params.end_time_txt + " (Today)");
			nameTV.setText(params.name);
		}
	}
}
