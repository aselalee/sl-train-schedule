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
	private ParameterSet [] paramSet = null;
	private Context mContext = null;

	public HisAndFavAdapter(Context context, ParameterSet [] params) {
		paramSet = params;
		mContext = context;
	}

	public int getCount() {
		return paramSet.length;
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
		ItemView iv = null;
        if (convertView == null) {
            iv = new ItemView(mContext, paramSet[position]);
        } else {
            iv = (ItemView) convertView;
            iv.setParams(paramSet[position]);
        }
		return iv;
	}

    /**
     * History and Favourites Item view.
     */
    private class ItemView extends LinearLayout {
    	private TextView myTV1 = null;
    	private TextView myTV2 = null;
        public ItemView(Context context, ParameterSet params) {
            super(context);
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view=layoutInflater.inflate(R.layout.his_fav_list_item, this, true);
            myTV1 = (TextView)view.findViewById(R.id.station_names);
            myTV1.setText(params.start_station_txt + " - "  + params.end_station_txt);
            myTV2 = (TextView)view.findViewById(R.id.filter_time);
            myTV2.setText(params.start_time_txt + " to " + params.end_time_txt);
        }
        public void setParams(ParameterSet params) {
        	myTV1.setText(params.start_station_txt + " - " + params.end_station_txt);
        	myTV2.setText(params.start_time_txt + " to " + params.end_time_txt);
        }
    }
}
