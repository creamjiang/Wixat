package com.wixet.wixat;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wixet.utils.ServerConfiguration;
import com.wixet.wixat.adapter.ContactListArrayAdapter;
import com.wixet.wixat.service.WixatService;
import com.wixet.wixat.service.XMPPManager;
import com.wixet.wixat.service.WixatService.LocalBinder;


//TODO REVISAR LA CLASE COMPLETA
public class NewConversationActivity extends Activity {
 
	private static WixatService service;

    
    
    private boolean mBound;
    
    @Override
    protected void onStart(){
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, WixatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

	 @Override
	 protected void onStop() {
		 super.onStop();
		 
			// Unbind from the service
	        if (mBound) {
	            unbindService(mConnection);
	            mBound = false;
	        }
	 }
	 
private ServiceConnection mConnection = new ServiceConnection() {
		
		
		
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder localService) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) localService;
            service = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_contacts_select);
		
		
		Intent i = new Intent(getApplicationContext(), LoadContactsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	    startActivityForResult(i, 1);

	    
	    
	}

	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (requestCode == 1) {
    		/* Eliminar aviso de mensajes nuevos ya que se acaban de leer */
    	     if(resultCode == RESULT_OK){

    	    	 if(data.hasExtra(LoadContactsActivity.MESSAGE)){
    	    		 String[] phoneNumbers = data.getExtras().getStringArray(LoadContactsActivity.MESSAGE);
	    	    	 ArrayList<HashMap<String,String>> numbers = new ArrayList<HashMap<String,String>>();
	    	    	 for(String number: phoneNumbers){
	    	    	 	HashMap<String,String> num = new HashMap<String,String>();
	    	    	 	num.put(LoadingScreenActivity.TELEPHONE, number);
	    	    	 	numbers.add(num);
	    	    	 }
	
	    	    	 ListView listview = (ListView) findViewById(R.id.contactlist);
	    	    	 final ContactListArrayAdapter adapter = new ContactListArrayAdapter(this, R.layout.contact_list_row, numbers);
	    	    	 listview.setAdapter(adapter);
	
	
	    	    	 listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	    	 	  public void onItemClick(AdapterView<?> parent, View view,
	    	    	 	    int position, long id) {
	    	    	 		    
	    	    	 		  	setContentView(R.layout.activity_loading_screen);
	    	    	 		  	int conversationId = service.startConversation(adapter.getItem(position).get(LoadingScreenActivity.TELEPHONE)+"@"+ServerConfiguration.HOSTNAME);
	    	    	 		  	service.notifyRefresh();
	
	    	    	 		    Intent conversationIntent = new Intent(getApplicationContext(), ConversationActivity.class);
	    	    	 		    conversationIntent.putExtra(ConversationActivity.CONVERSATION_ID, conversationId);
	    	    	 		    
	    	    	 		    conversationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
	    	    	 		    // Intent.FLAG_ACTIVITY_NO_HISTORY To finish activity
	    	    	 		    startActivity(conversationIntent);
	    	    	 		    finish();

	    	    	 		    
	    	    	 		    
	    	    	 	    
	    	    	 	  }
	    	    	 	  
	    	    	 	});
	    	    	 }else{
	    	    		//No matches
	    	    		 setContentView(R.layout.activity_load_contacts_no_matches);
	    	    	 }
    	    	
    	      
    	}
    	}
    	
    }
	


}
