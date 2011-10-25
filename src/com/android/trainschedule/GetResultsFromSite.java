package com.android.trainschedule;

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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class GetResultsFromSite {
	
	public static String GetResults(String station_from, String station_to,
							 String time_from, String time_to)
	{
	    String url = "http://www.railway.gov.lk/Search_Result.php";
	    
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        nameValuePairs.add(new BasicNameValuePair("startStation", station_from));
        nameValuePairs.add(new BasicNameValuePair("endStation", station_to));
        nameValuePairs.add(new BasicNameValuePair("txtFromTime", time_from));
        nameValuePairs.add(new BasicNameValuePair("txtToTime", time_to));
        nameValuePairs.add(new BasicNameValuePair("datepicker", ""));
        nameValuePairs.add(new BasicNameValuePair("Submit", "Find"));

        HttpPost httpPost = new HttpPost(url);
	    HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;

	    try {
	    	Log.i("TR_SCH_INF", "Sending Request");
	    	httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        response = httpClient.execute(httpPost);
	    } catch (ClientProtocolException e) {
	    	Log.e("TR_SCH_ERR", "HTTPERROR : ClientProtocolException : "+e);
	    } catch (IOException e) {
	    	Log.e("TR_SCH_ERR", "HTTPERROR : IOException : "+e);
	    }
	    Log.i("TR_SCH_INF", response.getStatusLine().toString());

	    InputStream ips = null;
	    try {
	    	ips = response.getEntity().getContent();
	    } catch (IOException e) {
	    	Log.e("TR_SCH_ERR", "InputStreamERROR : IOException : "+e);
	    } catch (IllegalStateException e) {
	    	Log.e("TR_SCH_ERR", "InputStreamERROR : IllegalStateException : "+e);
	    }
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
	 	}
	    String htmlOutput = strBuilder.toString();
	    int index = htmlOutput.indexOf("<tr bgcolor=\"#FFCC66\" class=\"HeadText\" ><td colspan=\"8\" bordercolor=\"#FFCC66\" style=\"color:#FF0000\"><h3>Train Schedules");
	    String htmlTable = "<html><head>";
	    htmlTable += "<style type=\"text/css\">tr.HeadText{font-size:10px} tr.detailText{font-size:10px}</style></head><body>";
	    if(index != -1) {
	    	String tmp = htmlOutput.substring(index - 35);
	    	int	tmpIndex = tmp.indexOf("</table>");
	    	if( tmpIndex  != -1 )
	    	{
	    		htmlTable += tmp.substring(0, tmpIndex + 8);
	    	}
	    	else
	    	{
	    		htmlTable += "<a>Results Not Found</a>";
	    	}
	    }
	    else
	    {
	    	htmlTable += "<a>Results Not Found</a>";
	    }
	    htmlTable += "</body></html>";
	    return htmlTable;
	}
}
