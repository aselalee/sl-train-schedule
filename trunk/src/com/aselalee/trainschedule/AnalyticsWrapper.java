package com.aselalee.trainschedule;

import android.content.Context;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsWrapper {
	private boolean useAnalytics = false;
	private GoogleAnalyticsTracker tracker = null;
	
	public AnalyticsWrapper(Context mContext) {
		/**
		 * useAnalytics can be set to true/false according to a global
		 * variable or a value saved in a SharedPreferences file.
		 * For now it's fixed to true.
		 */
		useAnalytics = true;

		if(useAnalytics == true) {
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.setAnonymizeIp(true);
			tracker.startNewSession(Constants.ANALYTICS_ID, 10, mContext);
		}
	}

	public void TrackPageView(String arg0) {
		if(useAnalytics == true && tracker != null) {
			tracker.trackPageView(arg0);
		}
	}

	public void TrackEvent(String arg0, String arg1, String arg2, int arg3) {
		if(useAnalytics == true && tracker != null) {
			tracker.trackEvent(arg0, arg1, arg2, arg3);
		}
	}

	public void Dispatch() {
		if(useAnalytics == true && tracker != null) {
			tracker.dispatch();
		}
	}

	public void StopSession() {
		if(tracker != null) {
			tracker.stopSession();
		}
	}
}
