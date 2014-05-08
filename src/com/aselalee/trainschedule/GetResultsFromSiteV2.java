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
import java.util.ListIterator;

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

public class GetResultsFromSiteV2 extends Thread {
	private Handler mHandler = null;
	private String station_from;
	private String station_to;
	private String time_from;
	private String time_to;
	private String date_today;
	
	private volatile Result [] results = null;
	private volatile List<Result> resultsList = new ArrayList<Result>();
	private volatile float [] prices = null;
	private volatile int errorCode = Constants.ERR_NO_ERROR;
	private String errorString = "No Error";

	public GetResultsFromSiteV2(Handler handle, String st_from, String st_to, String tm_from, String tm_to, String date) {
		mHandler = handle;
		station_from = st_from;
		station_to = st_to;
		time_from = tm_from;
		time_to = tm_to;
		date_today = date;
	}

	public void run() {
		/**
		 * Call the "GetResults" method to retrieve schedule data from server.
		 */ 
		GetResultsViaJSON(station_from, station_to, time_from, time_to, date_today);
		/**
		 * Call the "GetPrice" method to retrieve price from server.
		 */
		GetPriceViaJSON(station_from, station_to);
		/**
		 * This will send message to the calling thread to continue and display data.
		 */
		Message myMsg = new Message();
		myMsg.arg1 = Constants.THREAD_GET_RESULTS;
		mHandler.sendMessage(myMsg);
	}

	private void GetPriceViaJSON(String station_from, String station_to) {
		/**
		 * Create name value pairs to be sent.
		 */
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("lang", "en"));
		nameValuePairs.add(new BasicNameValuePair("startStationID", station_from));
		nameValuePairs.add(new BasicNameValuePair("endStationID", station_to));
		String strParams =  URLEncodedUtils.format(nameValuePairs, "utf-8");
		strParams = Constants.JSONURL_GETPRICE_V2 + "?" + strParams;
		String JSONStr = doJSONRequest(strParams);
		if(JSONStr == null) {
			prices = null;
			return;
		}
		if(JSONToPriceList(JSONStr) == false) {
			prices = null;
			return;
		}
	}

	private void GetResultsViaJSON(String station_from, String station_to,
			String time_from, String time_to, String date_today)
	{
		/**
		 * Create name value pairs to be sent.
		 */
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
		nameValuePairs.add(new BasicNameValuePair("lang", "en"));
		nameValuePairs.add(new BasicNameValuePair("startStationID", station_from));
		nameValuePairs.add(new BasicNameValuePair("endStationID", station_to));
		nameValuePairs.add(new BasicNameValuePair("startTime", time_from));
		nameValuePairs.add(new BasicNameValuePair("endTime", time_to));
		nameValuePairs.add(new BasicNameValuePair("searchDate", date_today));
		String strParams =  URLEncodedUtils.format(nameValuePairs, "utf-8");
		strParams = Constants.JSONURL_GETSCH_V2 + "?" + strParams;
		String JSONStr = doJSONRequest(strParams);
		if(JSONStr == null) {
			results = null;
			resultsList = null;
			prices = null;
			return;
		}
		if(JSONToResultsList(JSONStr) == false) {
			results = null;
			resultsList = null;
			prices = null;
			return;
		}
	}
	
	private String doJSONRequest(String getReqURLStr) {
		String JSONOutputStr = null;
		/**
		 * Setup networking.
		 */
		HttpGet httpGet = new HttpGet(getReqURLStr);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		/**
		 * Send HTTP GET request.
		 */
		try {
			response = httpClient.execute(httpGet);
		} catch(ClientProtocolException e) {
			errorCode = Constants.ERR_NETWORK_ERROR;
			errorString = "HTTPERROR : ClientProtocolException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		} catch(IOException e) {
			errorCode = Constants.ERR_NETWORK_ERROR;
			errorString = "HTTPERROR : IOException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		}
		/**
		 * Get output stream from response.
		 */
		InputStream ips = null;
		try {
			ips = response.getEntity().getContent();
		} catch(IOException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "getEntity.getContentERROR : IOException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		} catch(IllegalStateException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "getEntity.getContentERROR : IllegalStateException : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		}
		/**
		 * Read output result from stream.
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
			reader = null;
		} catch(IOException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "InputStreamReaderERROR : IOException - Read/Close Error : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		}
		JSONOutputStr = strBuilder.toString();
		try {
			ips.close();
		} catch (IOException e) {
			errorCode = Constants.ERR_ERROR;
			errorString = "InputStreamReaderError: IOException - Close Error" + e;
			Log.e(Constants.LOG_TAG, errorString);
		}
		strBuilder = null;
		httpClient = null;
		httpGet = null;
		response = null;
		ips = null;
		return JSONOutputStr;
	}

	private JSONObject GetResultsObject(String strJSON) {
		JSONObject jObject = null;
		JSONObject jObjectResults = null;
		boolean bIsSuccess = false;
		String strStatusMsg = "";
		int iStatusCode = 0;

		try {
			jObject = new JSONObject(strJSON); 
			bIsSuccess	= jObject.getBoolean("SUCCESS");
			strStatusMsg = jObject.getString("MESSAGE");
			iStatusCode = jObject.getInt("STATUSCODE");
			jObjectResults = jObject.getJSONObject("RESULTS");
		} catch (JSONException e) {
			errorCode = Constants.ERR_JSON_ERROR;
			errorString =  "JSONObjectERROR : Error Parsing JSON string : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		}
		if (bIsSuccess == false) {
			errorCode = Constants.ERR_SERVER_ERROR;
			errorString = "Server status message: " + strStatusMsg;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		}
		if (iStatusCode != 2000) {
			errorCode = Constants.ERR_NO_RESULTS_FOUND_ERROR;
			errorString = "No results found. Server status message: " + strStatusMsg;
			Log.e(Constants.LOG_TAG, errorString);
			return null;
		}
		return jObjectResults;
	}

	private boolean JSONToPriceList(String strJSON) {
		JSONObject jObject = GetResultsObject(strJSON);
		JSONArray ratesArray = null;
		
		if (jObject == null)
			return false;

		try {
			ratesArray = jObject.getJSONArray("priceList");
		} catch (JSONException e) {
			errorCode = Constants.ERR_JSON_ERROR;
			errorString =  "JSONObjectERROR : Error Parsing JSON string : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return false;
		}

		prices = new float[ratesArray.length()];
		String strTmp = null;
		for(int i = 0; i < ratesArray.length(); i++) {
			try {
				strTmp =  ratesArray.getJSONObject(i).getString("priceLKR").trim();
				prices[i] = Float.valueOf(strTmp);
			} catch (JSONException e) {
				errorCode = Constants.ERR_JSON_ERROR;
				errorString = "getJSONObject.getStringError : Error Parsing JSON array object : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				return false;
			}
		}
		return true;
	}

	private boolean JSONToResultsList(String strJSON) {
		JSONObject jObject = GetResultsObject(strJSON);
		JSONObject jObjectDT = null;
		JSONObject jObjectCT = null;
		JSONArray directTrains = null;
		JSONArray connectedTrains = null;
		
		if (jObject == null)
			return false;
		
		try {
			jObjectDT = jObject.getJSONObject("directTrains");
			jObjectCT = jObject.getJSONObject("connectingTrains");
			directTrains = jObjectDT.getJSONArray("trainsList");
			connectedTrains = jObjectCT.getJSONArray("trainsList");
		} catch (JSONException e) {
			errorCode = Constants.ERR_JSON_ERROR;
			errorString =  "JSONObjectERROR : Error Parsing JSON string : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return false;
		} catch (NullPointerException e) {
			errorCode = Constants.ERR_JSON_ERROR;
			errorString =  "JSONObjectERROR : Error Parsing JSON string : " + e;
			Log.e(Constants.LOG_TAG, errorString);
			return false;
		}
		if (directTrains == null || connectedTrains == null) {
			errorCode = Constants.ERR_JSON_ERROR;
			errorString =  "JSONObjectERROR : Error Parsing JSON string";
			Log.e(Constants.LOG_TAG, errorString);
			return false;
		}
			
		if (ProcessDirectTrains(directTrains) == false || ProcessConnectedTrains(connectedTrains) == false)
			return false;
		if (resultsList.size() == 0) {
			errorCode = Constants.ERR_NO_RESULTS_FOUND_ERROR;
			errorString =  "No results for this query.";
			Log.e(Constants.LOG_TAG, errorString);
			return false;
		}
		results = new Result[resultsList.size()];
		int idx = 0;
		for(ListIterator<Result> it = resultsList.listIterator(); it.hasNext();)
		{
			results[idx] = it.next();
			idx++;
		}
		return true;
	}

	private boolean ProcessDirectTrains(JSONArray trainsArray) {
		String strTmp = null;

		for(int i = 0; i < trainsArray.length(); i++) {
			Result result = new Result();
			SimpleDateFormat dateFormatterIn = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat dateFormatterOut = new SimpleDateFormat("HH:mm");
			try {
				result.name = trainsArray.getJSONObject(i).getString("trainNo").toString().trim();
				
				strTmp = trainsArray.getJSONObject(i).getString("arrivalTime").toString().trim();
				result.arrivalTime_dt = dateFormatterIn.parse(strTmp);
				result.arrivalTime_str = dateFormatterOut.format(result.arrivalTime_dt);
				
				strTmp = trainsArray.getJSONObject(i).getString("depatureTime").toString().trim();				
				result.depatureTime_dt = dateFormatterIn.parse(strTmp);
				result.depatureTime_str = dateFormatterOut.format(result.depatureTime_dt);
				
				strTmp = trainsArray.getJSONObject(i).getString("arrivalTimeEndStation").toString().trim();
				result.arrivalAtDestinationTime_dt = dateFormatterIn.parse(strTmp);
				result.arrivalAtDestinationTime_str = dateFormatterOut.format(result.arrivalAtDestinationTime_dt);
				
				result.delayTime_str = "";
				result.comment = "";
				
				result.startStationName = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("startStationName").toString().trim());
				
				result.endStationName = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("endStationName").toString().trim());
				
				result.toTrStationName = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("finalStationName").toString().trim());
				
				result.fDescription_original =
						CommonUtilities.ToTitleCase(trainsArray.getJSONObject(i).getString("trainFrequncy").toString().trim());
				result.fDescription = formatFrequency(result.fDescription_original);
				
				result.tyDescription = CommonUtilities.ToTitleCase(
						trainsArray.getJSONObject(i).getString("trainType").toString().trim());
				
				result.duration_str = calcDuration(result.depatureTime_dt,
						result.arrivalAtDestinationTime_dt);
			} catch(JSONException e) {
				errorCode = Constants.ERR_JSON_ERROR;
				errorString = "getJSONObject.getStringError : Error Parsing JSON array object : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				return false;
			} catch(ParseException e) {
				errorCode = Constants.ERR_DATE_STRING_PARSE_ERROR;
				errorString = "dateFormatter.parse() : Error Parsing Time String : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				return false;
			}
			resultsList.add(result);
		}
		return true;
	}
	private boolean ProcessConnectedTrains(JSONArray trainsArray) {
		String strTmp = null;
		
		if (trainsArray.length() == 0) return true;
		JSONObject [] recHeader = new JSONObject[trainsArray.length()];
		
		for(int i = 0; i < trainsArray.length(); i++) {
			try {
				recHeader[i] = trainsArray.getJSONObject(i).getJSONArray("recordHeader").getJSONObject(0);
			} catch (JSONException e) {
				errorCode = Constants.ERR_JSON_ERROR;
				errorString =  "JSONObjectERROR : Error Parsing JSON string : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				return false;
			}
		}
		
		for(int i = 0; i < recHeader.length; i++) {
			Result result = new Result();
			SimpleDateFormat dateFormatterIn = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat dateFormatterOut = new SimpleDateFormat("HH:mm");
			try {
				result.name = "-";
				
				strTmp = recHeader[i].getString("startArrivalTime").toString().trim();
				result.arrivalTime_dt = dateFormatterIn.parse(strTmp);
				result.arrivalTime_str = dateFormatterOut.format(result.arrivalTime_dt);
				
				strTmp = recHeader[i].getString("startDepartureTime").toString().trim();
				result.depatureTime_dt = dateFormatterIn.parse(strTmp);
				result.depatureTime_str = dateFormatterOut.format(result.depatureTime_dt);
				
				strTmp = recHeader[i].getString("endArrivalTime").toString().trim();
				result.arrivalAtDestinationTime_dt = dateFormatterIn.parse(strTmp);
				result.arrivalAtDestinationTime_str = dateFormatterOut.format(result.arrivalAtDestinationTime_dt);
				
				result.delayTime_str = "-";
				result.comment = "-";
				
				result.startStationName = CommonUtilities.ToTitleCase(
						recHeader[i].getString("startName").toString().trim());
				
				result.endStationName = CommonUtilities.ToTitleCase(
						recHeader[i].getString("endName").toString().trim());
				
				result.toTrStationName = "-";
				
				result.fDescription_original = "Connected Train";
				result.fDescription = "Connected Train";
				
				result.tyDescription = "Connected Train";
				
				result.duration_str = calcDuration(result.depatureTime_dt,
						result.arrivalAtDestinationTime_dt);
			} catch(JSONException e) {
				errorCode = Constants.ERR_JSON_ERROR;
				errorString = "getJSONObject.getStringError : Error Parsing JSON array object : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				return false;
			} catch(ParseException e) {
				errorCode = Constants.ERR_DATE_STRING_PARSE_ERROR;
				errorString = "dateFormatter.parse() : Error Parsing Time String : " + e;
				Log.e(Constants.LOG_TAG, errorString);
				return false;
			}
			resultsList.add(result);
		}
		return true;
	}
	
	private String formatFrequency(String frequency) {
		String result = frequency;
		if(frequency.contains(" Except Holidays)")) {
			result = frequency.replace(" Except Holidays)", "\n(Except Holidays)");
			return result;
		}
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

	public int GetErrorCode() {
		return errorCode;
	}

	public String GetErrorString() {
		return errorString;
	}

	public float [] GetPrices() {
		return prices;
	}

	public Result [] GetResults() {
		return results;
	}
}
