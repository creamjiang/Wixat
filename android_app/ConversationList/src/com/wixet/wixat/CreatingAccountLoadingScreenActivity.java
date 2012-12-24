package com.wixet.wixat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.wixet.utils.Encryption;


public class CreatingAccountLoadingScreenActivity extends Activity {

	private static String SERVER_URL = "http://46.137.8.44:9090/plugins/wixat/create";

	
	private class CreateAccount extends AsyncTask<Intent, Integer, Long> {
			@Override
			protected Long doInBackground(Intent... params) {

				HttpClient httpclient = new DefaultHttpClient();
			    HttpResponse response;
			    Intent i = params[0];
			    String telephone = i.getStringExtra(ConversationList.TELEPHONE);
			    String password = i.getStringExtra(ConversationList.PASSWORD);
			    
			    
				try {
					
					Key sharedKey = new SecretKeySpec(Encryption.decode(LoadingScreenActivity.KEY), "DESede");
					String url = SERVER_URL+"?phone="+Encryption.encrypt(sharedKey, telephone)+"&password="+Encryption.encrypt(sharedKey, password)+"&secret="+LoadingScreenActivity.SECRET;
					response = httpclient.execute(new HttpGet(url));
					StatusLine statusLine = response.getStatusLine();
					
				    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				        
				    	SharedPreferences settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(ConversationList.CONFIGURED, true);
						editor.commit();
						
				        Intent inew;
				        inew = new Intent(getApplicationContext(),ConversationList.class);
				        inew.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(inew);
						 

				    } else{
				        //Closes the connection.
				        response.getEntity().getContent().close();
				        throw new IOException(statusLine.getReasonPhrase());
				    }
				} catch (Exception e) {
					// TODO Auto-generated catch block

				    finish(); 
					
				} 
				
			    
				return null;
			}

	}
	     
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading_screen);
		Intent i = getIntent();
		new CreateAccount().execute(i);
	}


	 


}
