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
import com.wixet.utils.Encryption;
import com.wixet.utils.ServerConfiguration;


public class LoadingScreenActivity extends Activity {

	public static final String TELEPHONE = "telephone";
	public static final String ERROR = "error";
	public static final String FIRST_TIME = "firstTime";
	
	private class CheckUser extends AsyncTask<Intent, Integer, Long> {
			@Override
			protected Long doInBackground(Intent... params) {

				HttpClient httpclient = new DefaultHttpClient();
			    HttpResponse response;
			    Intent i = params[0];
			    String telephone = i.getStringExtra(TELEPHONE);

				try {
					
					Key sharedKey = new SecretKeySpec(Encryption.decode(ServerConfiguration.KEY), "DESede");
					String url = ServerConfiguration.CHECK_URL +"?phone="+Encryption.encrypt(sharedKey, telephone)+"&secret="+ServerConfiguration.SECRET;
					response = httpclient.execute(new HttpGet(url));
					StatusLine statusLine = response.getStatusLine();
					
				    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				        ByteArrayOutputStream out = new ByteArrayOutputStream();
				        response.getEntity().writeTo(out);
				        out.close();
				        String responseString = out.toString().trim();
				        Intent inew;
				        if(responseString.equals("true")){
				        	//Number already exists
				        	inew = new Intent(getApplicationContext(),FirstTimeWritePassword.class);
				        }else{
				        	//Creating account
				        	inew = new Intent(getApplicationContext(),FirstTimeCreateAccount.class);
				        }
				        
						inew.putExtra(LoadingScreenActivity.TELEPHONE, telephone);
						startActivity(inew);
						 

				    } else{
				        //Closes the connection.
				        response.getEntity().getContent().close();
				        throw new IOException(statusLine.getReasonPhrase());
				    }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					
					i.putExtra(ERROR, true);
					setResult(1, i);
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
		new CheckUser().execute(i);
	}


	 


}
