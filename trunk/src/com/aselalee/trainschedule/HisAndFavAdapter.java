package com.aselalee.trainschedule;

import android.content.Context;
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
            this.setOrientation(VERTICAL);
            myTV1 = new TextView(context);
            myTV1.setText(params.start_station_txt + " <-> " + params.end_station_txt);
            addView(myTV1, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            myTV2 = new TextView(context);
            myTV2.setText(params.start_time_txt + " to " + params.end_time_txt);
            addView(myTV2, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }
        public void setParams(ParameterSet params) {
        	myTV1.setText(params.start_station_txt + " <-> " + params.end_station_txt);
        	myTV2.setText(params.start_time_txt + " to " + params.end_time_txt);
        }
    }
}
