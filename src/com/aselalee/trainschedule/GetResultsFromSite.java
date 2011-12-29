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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GetResultsFromSite {

	public static Result [] GetResultsJson(String station_from, String station_to,
			String time_from, String time_to, String date_today)
	{
		/**
		 * Create name value pairs to be sent the the above URL.
		 * variable names were extracted manually from the site.
		 */
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
		nameValuePairs.add(new BasicNameValuePair("lang", "en"));
		nameValuePairs.add(new BasicNameValuePair("startStationCode", station_from));
		nameValuePairs.add(new BasicNameValuePair("endStationCode", station_to));
		nameValuePairs.add(new BasicNameValuePair("arrivalTime", time_from));
		nameValuePairs.add(new BasicNameValuePair("depatureTime", time_to));
		nameValuePairs.add(new BasicNameValuePair("currentDate", date_today));
		nameValuePairs.add(new BasicNameValuePair("currentTime","01:00:00"));
		String strParams =  URLEncodedUtils.format(nameValuePairs, "utf-8");
		String url = Constants.JASONURL + "?" + strParams;

		/**
		 * Setup networking.
		 * Then set HTTP POST data.
		 */
		HttpGet httpGet = new HttpGet(url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;

		/**
		 * Send HTTP POST request.
		 */
		try {
			response = httpClient.execute(httpGet);
		} catch(ClientProtocolException e) {
			Log.e(Constants.LOG_TAG, "HTTPERROR : ClientProtocolException : "+e);
			return null;
		} catch(IOException e) {
			Log.e(Constants.LOG_TAG, "HTTPERROR : IOException : "+e);
			return null;
		}

		/**
		 * Get output from response.
		 */
		InputStream ips = null;
		try {
			ips = response.getEntity().getContent();
		} catch(IOException e) {
			Log.e(Constants.LOG_TAG, "InputStreamERROR : IOException : "+e);
			return null;
		} catch(IllegalStateException e) {
			Log.e(Constants.LOG_TAG, "InputStreamERROR : IllegalStateException : "+e);
			return null;
		}
		/**
		 * Read output result from server.
		 */
		StringBuilder strBuilder = new StringBuilder();
		try {
			char[] bytes = new char[1024];
			int numRead = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
			while((numRead = reader.read(bytes)) > 0) {
				strBuilder.append(new String(bytes, 0, numRead));
			}
		} catch(IOException e) {
			Log.e(Constants.LOG_TAG, "InputStreamERROR : IOException - Read Error: "+e);
			return null;
		}
		return JSONToResultsList(strBuilder.toString());
	}

	private static  Result [] JSONToResultsList(String strJSON) {
		JSONObject jObject;
		JSONArray trainsArray;
		String strTmp;
		Result [] results = null;

		try {
			jObject = new JSONObject(strJSON); 
		} catch(JSONException e) {
			Log.e(Constants.LOG_TAG, "Error Parsing JSON string:"+e);
			return null;
		}
		try {
			trainsArray = jObject.getJSONArray("trains");
		} catch(JSONException e) {
			Log.e(Constants.LOG_TAG, "Error Parsing JSON object:"+e);
			return null;
		}
		if(trainsArray.length() < 1) {
			return null;
		}
		results = new Result[trainsArray.length()];
		for(int i = 0; i < trainsArray.length(); i++) {
			try {
				results[i] = new Result();
				results[i].name = trainsArray.getJSONObject(i).getString("name").toString().trim();
				strTmp = trainsArray.getJSONObject(i).getString("arrivalTime").toString().trim();
				results[i].arrivalTime = chop(strTmp);
				strTmp = trainsArray.getJSONObject(i).getString("depatureTime").toString().trim();
				results[i].depatureTime = chop(strTmp);
				strTmp = trainsArray.getJSONObject(i).getString("arrivalAtDestinationTime").toString().trim();
				results[i].arrivalAtDestinationTime = chop(strTmp);
				strTmp = trainsArray.getJSONObject(i).getString("delayTime").toString().trim();
				results[i].delayTime = chop(strTmp);
				results[i].comment = trainsArray.getJSONObject(i).getString("comment").toString().trim();
				results[i].startStationName = trainsArray.getJSONObject(i).getString("startStationName").toString().trim();
				results[i].endStationName = trainsArray.getJSONObject(i).getString("endStationName").toString().trim();
				results[i].toTrStationName = trainsArray.getJSONObject(i).getString("toTrStationName").toString().trim();
				results[i].fDescription = formatFrequency(trainsArray.getJSONObject(i).getString("fDescription").toString().trim());
				results[i].tyDescription = trainsArray.getJSONObject(i).getString("tyDescription").toString().trim();
				results[i].duration = calcDuration(results[i].depatureTime, results[i].arrivalAtDestinationTime);
			} catch(JSONException e) {
				Log.e(Constants.LOG_TAG, "Error Parsing JSON array object:"+e);
				return null;
			}
		}
		return results;
	}

	private static String formatFrequency(String frequency) {
		String result = frequency;
		int freqLength = frequency.length();
		if( freqLength > 12) {
			int firstSpace = -1;
			int secondSpace = -1;
			firstSpace = frequency.indexOf(" ");
			if(firstSpace > 0) {
				secondSpace = frequency.indexOf(" ", firstSpace + 1);
			}
			if(secondSpace > 0) {
				char [] charArray = new char[freqLength];
				frequency.getChars(0, freqLength, charArray, 0);
				charArray[secondSpace] = '\n';
				result = null;
				result = new String(charArray);
			}
		}
		return result;
	}
	private static String calcDuration(String depatureTime, String arrAtDestinationTime) {
		String durationStr = "";
		long startTimeInMins = (Integer.parseInt(depatureTime.substring(0, 2)) * 60) +  
				(Integer.parseInt(depatureTime.substring(3,5)));
		long endTimeInMins = (Integer.parseInt(arrAtDestinationTime.substring(0, 2)) * 60) +  
				(Integer.parseInt(arrAtDestinationTime.substring(3,5)));
		long durationInMins = endTimeInMins - startTimeInMins;
		if(durationInMins > 0) {
			int hours = (int)(durationInMins/60);
			if(hours < 9) {
				durationStr = "0" + String.valueOf(hours);
			} else {
				durationStr = String.valueOf(hours);
			}
			durationStr += ":"; 
			int mins = (int)(durationInMins%60);
			if(mins < 9) {
				durationStr += "0" + String.valueOf(mins);
			} else {
				durationStr += String.valueOf(mins);
			}
		} else {
			durationStr = "---/---";
		}
		return durationStr;
	}
	private static String chop(String strIn) {
		String strOut;
		if(strIn == null) {
			return "";
			}
		int strLen = strIn.length();
		if(strLen < 4) {
			return "";
		}
		int lastIdx = strLen - 3;
		strOut = strIn.substring(0, lastIdx);
		return strOut;
	}
}
