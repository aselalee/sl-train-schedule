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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
	    htmlOutput += 	"td {border-width:1px;padding:2px;border-color:black;border-style:outset;text-align:center}";
	    htmlOutput += 	"th {border-width:1px;padding:2px;border-color:black;border-style:outset;text-align:center}";
	    htmlOutput += 	"table {font-size:10px;border-width:1px;border-collapse:collapse;border-color:black;border-style:outset;}";
	    htmlOutput +=   "tbody {height:100px;overflow:auto;}";
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
	    htmlOutput += "</body></html>";
	    Log.i("TR", htmlOutput);
	    
	    return htmlOutput;
	}
	private static String JSONToHTMLTable(String strJSON) {
		String htmlTable = "";
		JSONObject jObject;
		JSONArray trainsArray;
		String strTmp = "";
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
		
		htmlTable += "<table width=\"100%\">";
		htmlTable += "<thead><tr>";
		//htmlTable += "<th>Starting Station</th>";
		htmlTable += "<th>Arrival Time</th>";
		htmlTable += "<th>Depature Time</th>";
		htmlTable += "<th>Reach Destination At</th>";
		htmlTable += "<th>Train Freaquency</th>";
		htmlTable += "<th>Final Destination</th>";
		htmlTable += "<th>Train Type</th>";
		//htmlTable += "<th class=\"HeadText\">Destination Station</th>";
		htmlTable += "</tr></thead>";
		htmlTable += "<tbody>";
		for(int i = 0; i < trainsArray.length(); i++) {
			try {
				htmlTable += "<tr>";
				//htmlTable += "<td>";
				//strTmp = trainsArray.getJSONObject(i).getString("startStationName").toString().trim();
				//htmlTable += strTmp + " ";
				//htmlTable += "</td>";
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
				strTmp = trainsArray.getJSONObject(i).getString("arrivalAtDestinationTime").toString().trim();
				strTmp = chop(strTmp);
				htmlTable +=  strTmp;
				htmlTable += "</td>";
				htmlTable += "<td>";
				strTmp = trainsArray.getJSONObject(i).getString("fDescription").toString().trim();
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
				//htmlTable += "<td>";
				//strTmp = trainsArray.getJSONObject(i).getString("endStationName").toString().trim();
				//htmlTable +=  strTmp;
				//htmlTable += "</td>";
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
	
	public static String GetResults(String station_from, String station_to,
							 String time_from, String time_to, String date_today)
	{
		/**
		 * Base URL to get results from.
		 */
	    String url = "http://www.railway.gov.lk/Search_Result.php";
	    
	    /**
	     * Create name value pairs to be sent the the above URL.
	     * variable names were extracted manually from the site.
	     */
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        nameValuePairs.add(new BasicNameValuePair("startStation", station_from));
        nameValuePairs.add(new BasicNameValuePair("endStation", station_to));
        nameValuePairs.add(new BasicNameValuePair("txtFromTime", time_from));
        nameValuePairs.add(new BasicNameValuePair("txtToTime", time_to));
        nameValuePairs.add(new BasicNameValuePair("datepicker", date_today));
        nameValuePairs.add(new BasicNameValuePair("Submit", "Find"));
        
        /**
         * HTML to be sent as the output. The results table will be appended
         * to this variable.
         */
	    String htmlOutput = "";
	    htmlOutput += "<html><head>";
	    htmlOutput += "<style type=\"text/css\">tr.HeadText{font-size:10px} tr.detailText{font-size:10px}</style>";
	    htmlOutput += "</head><body>";
	    
	    /**
	     * In case an error occurs this HTML string will be returned.
	     */
	    String htmlNoResult = "<html><head></head><body><h1>Results Not Found.</h1></body></html>";
	    String htmlEncErr = "<html><head></head><body><h1>Encoding Error. Please Try Again.</h1></body></html>";
	    String htmlHTTPErr = "<html><head></head><body><h1>Network Error. Please Try Again.</h1></body></html>";
	    String htmlIOErr = "<html><head></head><body><h1>IO Stream Error. Close Application and Try Again.</h1></body></html>";

        /**
         * Setup networking.
         * Then set HTTP POST data.
         */
        HttpPost httpPost = new HttpPost(url);
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			Log.e("TR_SCH_ERR", "ENCODINGERROR : UnsupportedEncodingException : "+e);
			return htmlEncErr;
		}
	    HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;

        /**
         * Send HTTP POST request.
         */
	    try {
	        response = httpClient.execute(httpPost);
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
	    
	    /**
	     * Extract the required part from the output HTML received from the site.
	     */
	    String siteOutput = strBuilder.toString();
	    int index = siteOutput.indexOf("<tr bgcolor=\"#FFCC66\" class=\"HeadText\" ><td colspan=\"8\" bordercolor=\"#FFCC66\" style=\"color:#FF0000\"><h3>Train Schedules");
	    if(index != -1) {
	    	/**
	    	 * Get a new substring starting from the <table...
	    	 */
	    	String tmp = siteOutput.substring(index - 35);
	    	/**
	    	 * Search for the end of HTML table.
	    	 */
	    	int	tmpIndex = tmp.indexOf("</table>");
	    	if( tmpIndex  != -1 )
	    	{
	    		/**
	    		 * Extract the required table.
	    		 */
	    		htmlOutput += tmp.substring(0, tmpIndex + 8);
	    	}
	    	else
	    	{
	    		return htmlNoResult;
	    	}
	    }
	    else
	    {
	    	return htmlNoResult;
	    }
	    htmlOutput += "</body></html>";
	    htmlOutput = CorrectGrammerAndSpelling( htmlOutput );
	    return htmlOutput;
	}
	private static String CorrectGrammerAndSpelling( String htmlCode ) {
		String tmp = htmlCode.replace("Arrival to", "Arrival at");
		tmp = tmp.replace("Reaching to", "Reaching");
		return tmp.replace("Type of the Train", "Type of Train");		
	}
}
