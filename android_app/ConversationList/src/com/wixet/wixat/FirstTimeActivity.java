package com.wixet.wixat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public class FirstTimeActivity extends FragmentActivity {

	//private static String SERVER_URL = "http://wixet.com/activate";
	private String telephone = "";
	//private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time);
		
		SharedPreferences s = getSharedPreferences(ConversationList.CONFIGURATION, 0);
		TextView t = (TextView) findViewById(R.id.phonenumber);
		telephone = s.getString(ConversationList.TELEPHONE, "No number");
		t.setText(telephone);
	}

	/*
	private void nextActivity(){
		finish();
	}
	
private void showError(){
	
	
	DialogFragment nw = new FirstTimeNumberErrorDialogFragment();
	nw.show(getSupportFragmentManager(), "NoticeDialogFragment");
	}
*/

/*protected void onActivityResult (int requestCode, int resultCode, Intent i){
	TextView t = (TextView) findViewById(R.id.phonenumber);
	 t.setText("Esperando");
	 if(i.getBooleanExtra(LoadingScreenActivity.ERROR, true)){
		 
		 t.setText("ERROR");	 
	 }else{
		 t.setText("BIEN");
	 }
	
}*/
	 public void connect(View view) {
		 Intent i=new Intent(this,LoadingScreenActivity.class);
		 i.putExtra(LoadingScreenActivity.TELEPHONE, telephone);
		 //startActivityForResult(i,0);
		 startActivity(i);
		 
		 
	/*	 	
		
	    	final ProgressDialog pd = ProgressDialog.show(this,
	    			"",
	    			"",
	    			true, false);
	    	
	    			new Thread(new Runnable(){
	    			public void run(){
	    				
	    				HttpClient httpclient = new DefaultHttpClient();
	    			    HttpResponse response;
	    			    boolean error = false;
	    				try {
	    					Thread.sleep(2000);
	    					
	    					response = httpclient.execute(new HttpGet(SERVER_URL+"/telephone"));
	    					StatusLine statusLine = response.getStatusLine();
	    					
	    				    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	    				        ByteArrayOutputStream out = new ByteArrayOutputStream();
	    				        response.getEntity().writeTo(out);
	    				        out.close();
	    				        String responseString = out.toString();
	    				        Log.d("AAA", responseString);
	    				        //..more logic
	    				    } else{
	    				        //Closes the connection.
	    				        response.getEntity().getContent().close();
	    				        throw new IOException(statusLine.getReasonPhrase());
	    				    }
	    				} catch (Exception e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    					error = true;
	    				}
	    				
	    				pd.dismiss();
	    				if(error)
	    					showError();
	    				else
	    					nextActivity();
	    			}
	    			}).start();
	    			
	        
		 */
	        

		    
	 }


}