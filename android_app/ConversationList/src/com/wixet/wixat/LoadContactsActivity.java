package com.wixet.wixat;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;

import com.wixet.utils.Encryption;
import com.wixet.wixat.database.DataBaseHelper;

public class LoadContactsActivity extends Activity {

	
	public static final String MESSAGE = "message";
	
	private class GetContacts extends AsyncTask<Intent, Integer, Long> {
		private DataBaseHelper db;
		public GetContacts(DataBaseHelper db) {
			this.db = db;
		}

		@Override
		protected Long doInBackground(Intent... params) {

			HttpClient httpclient = new DefaultHttpClient();
		    HttpResponse response;
		    Intent i = new Intent();
		    /* Get all phones */
		    
			try {
				
				String telephones ="";
		    	Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		    	while (phones.moveToNext())
		    	{
		    	  String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		    	  phoneNumber = phoneNumber.replaceAll("\\s+", "");
		    	  
		    	  if(phoneNumber.indexOf("+")==0){
		    		  phoneNumber = phoneNumber.substring(2, phoneNumber.length());
		    	  }
		    	  
		    	  if(phoneNumber.length() == 9 && !db.existsChat(phoneNumber+"@46.137.8.44")){
		    		  telephones += phoneNumber+",";

		    	  }
		    	  

		    	}
		    	//remove last comma
		    	phones.close();
		    	if(telephones.length() > 0){
			    	telephones.substring(0, telephones.length()-1);
			    	
					Key sharedKey = new SecretKeySpec(Encryption.decode(LoadingScreenActivity.KEY), "DESede");
					String url = "http://46.137.8.44:9090/plugins/wixat/contacts";
					
					HttpPost p = new HttpPost(url);
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("phones", Encryption.encrypt(sharedKey, telephones)));
			        nameValuePairs.add(new BasicNameValuePair("secret", LoadingScreenActivity.SECRET));
			        p.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
					
					response = httpclient.execute(p);
					StatusLine statusLine = response.getStatusLine();
					
				    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
				        ByteArrayOutputStream out = new ByteArrayOutputStream();
				        response.getEntity().writeTo(out);
				        out.close();
				        String responseString = out.toString().trim();
				        	//Number already exists
				        
				        String contacts = Encryption.decrypt(sharedKey, responseString);
				        
						i.putExtra(MESSAGE, contacts.split(","));
	
						setResult(RESULT_OK,i);
	
				    } else{
				        //Closes the connection.
				        response.getEntity().getContent().close();
				        throw new IOException(statusLine.getReasonPhrase());
				    }
		    	}else{
		    		
		    	}
			} catch (IOException e) {
				Log.d("EXCEPTION","Error"+e.getMessage());
				i.putExtra(MESSAGE, e.getMessage());
				setResult(RESULT_CANCELED,i);
			    
				
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("ECEPTION","Exception");
			} 
			
        	
        	finish();
			return null;
		}

}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_loading_screen);
		//Intent i = getIntent();
		DataBaseHelper db = new DataBaseHelper(this);
		new GetContacts(db).execute();
		
		
	}
	
/*	private void showContacts(){
		setContentView(R.layout.activity_load_contacts);
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_load_contacts, menu);
		return true;
	}

}
