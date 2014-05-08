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

public class ResultViewAdapter extends BaseAdapter {
	private Result [] mResults = null;
	private Context mContext = null;

	public ResultViewAdapter(Context context, Result [] results) {
		mResults = results;
		mContext = context;
	}

	public int getCount() {
		return mResults.length;
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
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ResultItemView iv = null;
		if(convertView == null) {
			iv = new ResultItemView(mContext, mResults[position]);
		} else {
			iv = (ResultItemView) convertView;
			iv.setParams(mResults[position]);
		}
		return iv;
	}

	/**
	 * Results list item view.
	 */
	private class ResultItemView extends LinearLayout {
		private TextView startTime = null;
		private TextView endTime = null;
		private TextView duration = null;
		private TextView freq = null;
		public ResultItemView(Context context, Result result) {
			super(context);
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view=layoutInflater.inflate(R.layout.result_list_item, this, true);
			startTime = (TextView)view.findViewById(R.id.res_list_item_start_time);
			endTime = (TextView)view.findViewById(R.id.res_list_item_end_time);
			duration = (TextView)view.findViewById(R.id.res_list_item_duration);
			freq = (TextView)view.findViewById(R.id.res_list_item_freq);
			setParams(result);
		}
		public void setParams(Result result) {
			if(startTime == null || endTime == null || duration == null || freq == null)
				return;
			startTime.setText(result.depatureTime_str);
			endTime.setText(result.arrivalAtDestinationTime_str);
			duration.setText(result.duration_str);
			freq.setText(result.fDescription);
		}
	}
}
