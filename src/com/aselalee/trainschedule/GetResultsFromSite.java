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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetResultsFromSite extends Thread {
	private Handler mHandler = null;
	private String station_from;
	private String station_to;
	private String time_from;
	private String time_to;
	private String date_today;
	private volatile Result [] results = null;
	private volatile int errorCode = Constants.ERR_NO_ERROR;

	private String errorString = "No Error";

	public GetResultsFromSite(Handler handle, String st_from, String st_to, String tm_from, String tm_to, String date) {
		mHandler = handle;
		station_from = st_from;
		station_to = st_to;
		time_from = tm_from;
		time_to = tm_to;
		date_today = date;
	}

	public void run() {
		/**
		 * Call the "GetResults" method to retrieve data from server.
		 */ 
		GetResultsViaJASON(station_from, station_to, time_from, time_to, date_today);
		/**
		 * This will send message to the calling thread to continue and display data.
		 */
		Message myMsg = new Message();
		myMsg.arg1 = Constants.THREAD_GET_RESULTS;
		mHandler.sendMessage(myMsg);
	}

	private void GetResultsViaJASON(String station_from, String station_to,
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
			errorCode = Constants.ERR_NETWORK_ERROR;
			errorString = "HTTPERROR : ClientProtocolException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		} catch(IOException e) {
			errorCode = Constants.ERR_NETWORK_ERROR;
			errorString = "HTTPERROR : IOException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		}

		/**
		 * Get output from response.
		 */
		InputStream ips = null;
		try {
			ips = response.getEntity().getContent();
		} catch(IOException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "getEntity.getContentERROR : IOException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		} catch(IllegalStateException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "getEntity.getContentERROR : IllegalStateException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		}
		/**
		 * Read output result from server.
		 */
		StringBuilder strBuilder = new StringBuilder();
		try {
			char[] bytes = new char[1024];
			int numRead = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(ips, "UTF-8"), 8192);
			while((numRead = reader.read(bytes)) > 0) {
				strBuilder.append(new String(bytes, 0, numRead));
			}
			reader.close();
		} catch(IOException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "InputStreamReaderERROR : IOException - Read/Close Error : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		}
		JSONToResultsList(strBuilder.toString());
		strBuilder = null;
		httpClient = null;
		httpGet = null;
		response = null;
		try {
			ips.close();
		} catch (IOException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "InputStreamReaderError: IOException - Close Error" + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		}

	}

	private void JSONToResultsList(String strJSON) {
		JSONObject jObject;
		JSONArray trainsArray;
		String strTmp;

		try {
			jObject = new JSONObject(strJSON); 
		} catch(JSONException e) {
			errorCode = Constants.ERR_JASON_ERROR;
			errorString =  "JASONObjectERROR : Error Parsing JSON string : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		}
		try {
			trainsArray = jObject.getJSONArray("trains");
		} catch(JSONException e) {
			errorCode = Constants.ERR_JASON_ERROR;
			errorString = "getJASONArrayERROR : Error Parsing JSON object :" + e;
			Log.e(Constants.LOG_TAG, errorString);
			results = null;
			return;
		}
		if(trainsArray.length() < 1) {
			errorCode = Constants.ERR_NO_RESULTS_FOUND_ERROR;
			errorString = "No Results Found";
			results = null;
			return;
		}
		results = new Result[trainsArray.length()];
		for(int i = 0; i < trainsArray.length(); i++) {
			SimpleDateFormat dateFormatterIn = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat dateFormatterOut = new SimpleDateFormat("HH:mm");
			try {
				results[i] = new Result();
				results[i].name = trainsArray.getJSONObject(i).getString("name").toString().trim();
				strTmp = trainsArray.getJSONObject(i).getString("arrivalTime").toString().trim();
				results[i].arrivalTime_dt = dateFormatterIn.parse(strTmp);
				results[i].arrivalTime_str = dateFormatterOut.format(results[i].arrivalTime_dt);
				strTmp = trainsArray.getJSONObject(i).getString("depatureTime").toString().trim();
				results[i].depatureTime_dt = dateFormatterIn.parse(strTmp);
				results[i].depatureTime_str = dateFormatterOut.format(results[i].depatureTime_dt);
				strTmp = trainsArray.getJSONObject(i).getString("arrivalAtDestinationTime").toString().trim();
				results[i].arrivalAtDestinationTime_dt = dateFormatterIn.parse(strTmp);
				results[i].arrivalAtDestinationTime_str = dateFormatterOut.format(results[i].arrivalAtDestinationTime_dt);
				strTmp = trainsArray.getJSONObject(i).getString("delayTime").toString().trim();
				results[i].delayTime_str = chop(strTmp);
				results[i].comment = trainsArray.getJSONObject(i).getString("comment").toString().trim();
				results[i].startStationName = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("startStationName").toString().trim());
				results[i].endStationName = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("endStationName").toString().trim());
				results[i].toTrStationName = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("toTrStationName").toString().trim());
				results[i].fDescription_original =
						CommonUtilities.ToTitleCase(trainsArray.getJSONObject(i).getString("fDescription").toString().trim());
				results[i].fDescription = formatFrequency(results[i].fDescription_original);
				results[i].tyDescription = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("tyDescription").toString().trim());
				results[i].duration_str = calcDuration(results[i].depatureTime_dt,
						results[i].arrivalAtDestinationTime_dt);
			} catch(JSONException e) {
				errorCode = Constants.ERR_JASON_ERROR;
				errorString = "getJSONObject.getStringError : Error Parsing JSON array object : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				results = null;
				return;
			} catch(ParseException e) {
				errorCode = Constants.ERR_DATE_STRING_PARSE_ERROR;
				errorString = "dateFormatter.parse() : Error Parsing Time String : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				results = null;
				return;
			}
		}
	}

	private String formatFrequency(String frequency) {
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

	private String calcDuration(Date depatureTime, Date arrAtDestinationTime) {
		String durationStr = "";
		long startTimeInMilliSecs = depatureTime.getTime();
		long endTimeInMilliSecs = arrAtDestinationTime.getTime();
		long durationInMilliSecs = endTimeInMilliSecs - startTimeInMilliSecs;
		if(durationInMilliSecs > 0) {
			int hours = (int)(durationInMilliSecs/1000/60/60);
			int mins = (int)(durationInMilliSecs/1000/60)%60;
			durationStr = strToDoubleDigits(hours) + ":" + strToDoubleDigits(mins);
		} else {
			durationStr = "---/---";
		}
		return durationStr;
	}

	private String strToDoubleDigits(int value) {
		String output = "";
		if(value <= 9) {
			output += "0" + String.valueOf(value);
		} else {
			output += String.valueOf(value);
		}
		return output;
	}

	private String chop(String strIn) {
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

	public int GetErrorCode() {
		return errorCode;
	}

	public String GetErrorString() {
		return errorString;
	}
	
	public Result [] GetResults() {
		return results;
	}
}
