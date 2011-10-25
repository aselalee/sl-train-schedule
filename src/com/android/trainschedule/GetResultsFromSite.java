package com.android.trainschedule;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class GetResultsFromSite {
	
	public static String GetResults(String station_from, String station_to,
							 String time_from, String time_to)
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
        nameValuePairs.add(new BasicNameValuePair("datepicker", ""));
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
	    String htmlNoResult = "";
	    htmlNoResult += "<html><head></head><body><a>Results Not Found</a></body></html>";

        /**
         * Setup networking.
         * Then set HTTP POST data.
         */
        HttpPost httpPost = new HttpPost(url);
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			Log.e("TR_SCH_ERR", "ENCODINGERROR : UnsupportedEncodingException : "+e);
			return htmlNoResult;
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
	    	return htmlNoResult;
	    } catch (IOException e) {
	    	Log.e("TR_SCH_ERR", "HTTPERROR : IOException : "+e);
	    	return htmlNoResult;
	    }

	    /**
	     * Get output from response.
	     */
	    InputStream ips = null;
	    try {
	    	ips = response.getEntity().getContent();
	    } catch (IOException e) {
	    	Log.e("TR_SCH_ERR", "InputStreamERROR : IOException : "+e);
	    	return htmlNoResult;
	    } catch (IllegalStateException e) {
	    	Log.e("TR_SCH_ERR", "InputStreamERROR : IllegalStateException : "+e);
	    	return htmlNoResult;
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
	 		return htmlNoResult;
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

	    return htmlOutput;
	}
}
