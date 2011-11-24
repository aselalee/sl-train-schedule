/**
* @copyright	Copyright (C) 2010 - 2011 Asela Leelaratne
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
	
	public static String GetResultsJson(String station_from, String station_to,
							 String time_from, String time_to, String date_today)
	{
		/**
		 * Base URL to get results from.
		 */
	    String url = "http://mobile.icta.lk/services/railwayservice/getSchedule.php";
	    
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
        url = url + "?" + strParams;
        
        /**
         * HTML to be sent as the output. The results table will be appended
         * to this variable.
         */
	    String htmlOutput = "";
	    htmlOutput += "<html><head>";
	    htmlOutput += "<style type=\"text/css\">";
	    htmlOutput += 	"tr {background-color:#CBCBCB;}";
	    htmlOutput += 	"tr.alt {background-color:#E8E8EA;}";
	    htmlOutput += 	"td {border-width:1px;padding:2px;border-color:black;border-style:outset;text-align:center;}";
	    htmlOutput += 	"th {background-color:#3C3C3D;color:white;border-width:1px;padding:2px;border-color:black;border-style:outset;text-align:center;}";
	    htmlOutput += 	"table {font-size:10px;border-width:1px;border-collapse:collapse;border-color:black;border-style:outset;}";
	    htmlOutput += "</style>";
	    htmlOutput += "</head><body>";
	    
	    /**
	     * In case an error occurs this HTML string will be returned.
	     */
	    String htmlHTTPErr = "<html><head></head><body><h1>Network Error. Please Try Again.</h1></body></html>";
	    String htmlIOErr = "<html><head></head><body><h1>IO Stream Error. Close Application and Try Again.</h1></body></html>";

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
	    } catch (ClientProtocolException e) {
	    	Log.e("TR_SCH_ERR", "HTTPERROR : ClientProtocolException : "+e);
	    	return htmlHTTPErr;
	    } catch (IOException e) {
	    	Log.e("TR_SCH_ERR", "HTTPERROR : IOException : "+e);
	    	return htmlHTTPErr;
	    }

	    /**
	     * Get output from response.
	     */
	    InputStream ips = null;
	    try {
	    	ips = response.getEntity().getContent();
	    } catch (IOException e) {
	    	Log.e("TR_SCH_ERR", "InputStreamERROR : IOException : "+e);
	    	return htmlIOErr;
	    } catch (IllegalStateException e) {
	    	Log.e("TR_SCH_ERR", "InputStreamERROR : IllegalStateException : "+e);
	    	return htmlIOErr;
	    }
	    /**
	     * Read output result from server.
	     */
    	StringBuilder strBuilder = new StringBuilder();
	    try {
	    	char[] bytes = new char[1024];
	    	int numRead = 0;
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
	    	while ((numRead = reader.read(bytes)) > 0) {
	    		strBuilder.append(new String(bytes, 0, numRead));
	    	}
	 	} catch (IOException e) {
	 		Log.e("TR_SCH_ERR", "InputStreamERROR : IOException - Read Error: "+e);
	 		return htmlIOErr;
	 	}    
	    htmlOutput += JSONToHTMLTable(strBuilder.toString());
	    htmlOutput += "<br/><br/><br/></body></html>";
	    //Log.i("TR", htmlOutput);
	    return htmlOutput;
	}
	private static String JSONToHTMLTable(String strJSON) {
		String htmlTable = "";
		JSONObject jObject;
		JSONArray trainsArray;
		String strTmp = "";
		String style = "";
		String startStation;
		String endStation;
		try {
			jObject = new JSONObject(strJSON); 
		} catch (JSONException e) {
			Log.e("TR_SCH_ERR", "Error Parsing JSON string:"+e);
			return "<h1>Error Parsing JSON string</h1>";
		}
		try {
			trainsArray = jObject.getJSONArray("trains");
		} catch (JSONException e) {
			Log.e("TR_SCH_ERR", "Error Parsing JSON object:"+e);
			return "<h1>Error Parsing JSON object</h1>";
		}
		if( trainsArray.length() < 1 ) {
			return "<h1>Results Not Found.</h1>";
		}
		try {
				startStation = trainsArray.getJSONObject(0).getString("startStationName").toString().trim();
				endStation = trainsArray.getJSONObject(0).getString("endStationName").toString().trim();
		} catch (JSONException e) {
			Log.e("TR_SCH_ERR", "Error Parsing JSON array object:"+e);
			return "<h1>Error Parsing JSON array object</h1>";
		}
		
		htmlTable += "<table width=\"100%\">";
		htmlTable += "<thead><tr>";
		htmlTable += "<th><a>Arrival Time at</a><br/><a>" + startStation + "</a></th>";
		htmlTable += "<th><a>Depature Time at</a><br/><a>" + startStation + "</a></th>";
		htmlTable += "<th><a>Train</a><br/><a>Frequency</a></th>";
		htmlTable += "<th><a>Arrival Time at<a><br/><a>Destination</a><br/><a>(" + endStation + ")</a></th>";
		htmlTable += "<th><a>Final</a><br/><a>Destination</a></th>";
		htmlTable += "<th><a>Train</a><br/><a>Type</a></th>";
		htmlTable += "</tr></thead>";
		htmlTable += "<tbody>";
		for(int i = 0; i < trainsArray.length(); i++) {
			if(i%2 == 0) {
				style = "class=\"alt\"";
			}
			else
			{
				style = "";
			}
			try {
				htmlTable += "<tr " + style + ">";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("arrivalTime").toString().trim();
				strTmp = chop(strTmp);
				htmlTable += strTmp;
				htmlTable += "</td>";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("depatureTime").toString().trim();
				strTmp = chop(strTmp);
				htmlTable +=  strTmp;
				htmlTable += "</td>";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("fDescription").toString().trim();
				htmlTable +=  strTmp;
				htmlTable += "</td>";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("arrivalAtDestinationTime").toString().trim();
				strTmp = chop(strTmp);
				htmlTable +=  strTmp;
				htmlTable += "</td>";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("toTrStationName").toString().trim();
				htmlTable +=  strTmp;
				htmlTable += "</td>";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("tyDescription").toString().trim();
				htmlTable +=  strTmp;
				htmlTable += "</td>";
				htmlTable += "</tr>";
			} catch (JSONException e) {
				Log.e("TR_SCH_ERR", "Error Parsing JSON array object:"+e);
				return "<h1>Error Parsing JSON array object</h1>";
			}
		}
		htmlTable += "</tbody>";
		htmlTable += "</table>";
		return htmlTable;
	}
	private static String chop(String strIn) {
		String strOut;
	    if (strIn == null) {
	    	return null;
	    }
	    int strLen = strIn.length();
	    if (strLen < 4) {
	    	return "";
	    }
	    int lastIdx = strLen - 3;
	    strOut = strIn.substring(0, lastIdx);
		return strOut;
	}
}
