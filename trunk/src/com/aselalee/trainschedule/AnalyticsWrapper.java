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
		useAnalytics = false;

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
