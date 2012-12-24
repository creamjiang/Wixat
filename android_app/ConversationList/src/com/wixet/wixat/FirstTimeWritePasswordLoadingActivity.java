package com.wixet.wixat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;


public class FirstTimeWritePasswordLoadingActivity extends Activity {

	public static String TELEPHONE = "telephone";
	public static String PASSWORD = "error";
	
	private class CheckUser extends AsyncTask<Intent, Integer, Long> {
			@Override
			protected Long doInBackground(Intent... params) {


			    Intent i = params[0];
			    String username = i.getStringExtra(TELEPHONE);
			    String password = i.getStringExtra(PASSWORD);
			    
				 ConnectionConfiguration config = new ConnectionConfiguration("xmpp.wixet.com",5222, "Wixat");
				 
				 
				    XMPPConnection connection = new XMPPConnection(config);
				 
				    	
						try {
							connection.connect();
							connection.login(username, password);
							setResult(RESULT_OK,i);
							connection.disconnect();
						} catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							setResult(RESULT_CANCELED,i);
						}
						
						
						
						
						
						finish();
				
			    
				return null;
			}

	}
	     
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("LOGIN","PROBANDO");
		setContentView(R.layout.activity_loading_screen);
		Intent i = getIntent();
		new CheckUser().execute(i);
	}


	 


}
