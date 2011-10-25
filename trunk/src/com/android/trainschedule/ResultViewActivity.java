package com.android.trainschedule;



import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class ResultViewActivity extends Activity implements Runnable {
	String station_from;
	String station_to;
	String time_from;
	String time_to;
	String result;
	
	WebView mWebView;
	private ProgressDialog pd;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.result_table);
	
	    Bundle extras = getIntent().getExtras(); 
	    if(extras !=null) {
	    	station_from = extras.getString("station_from");
	    	station_to = extras.getString("station_to");
	    	time_from = extras.getString("time_from");
	    	time_to = extras.getString("time_to");
	    }
	    
	    mWebView = (WebView) findViewById(R.id.webview);
	    mWebView.getSettings().setJavaScriptEnabled(true);
	    
		pd = ProgressDialog.show(this, "Working..",
								 "Getting Results from www.railway.gov.lk",
								 true, false);
		Thread thread = new Thread(this);
		thread.start();   
	}
	private class myWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
	        mWebView.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public void run() {
		result= GetResultsFromSite.GetResults(station_from, station_to, time_from, time_to);
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
		    mWebView.loadDataWithBaseURL("", result,"text/html", "UTF-8", null);
		    mWebView.setWebViewClient(new myWebViewClient());
		}
	};
}
