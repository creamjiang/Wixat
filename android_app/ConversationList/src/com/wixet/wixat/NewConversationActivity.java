package com.wixet.wixat;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.philippheckel.service.ServiceManager;
import com.wixet.wixat.adapter.ContactListArrayAdapter;
import com.wixet.wixat.service.SomeService1;
import com.wixet.wixat.service.XMPPManager;

public class NewConversationActivity extends Activity {
 
	private static ServiceManager service;
	private static Context context;
	private static Activity activity;
    private static Handler serviceHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
          // Receive message from service
    		if(msg.what == XMPPManager.EVENT_NEW_CHAT){
	 		    service.unbind();
	 		    Log.d("RECIBIDO","CREAT INENT");
	 		    Intent conversationIntent = new Intent(context, ConversationActivity.class);
	 		    conversationIntent.putExtra(ConversationActivity.CONVERSATION_ID, msg.arg1+"");
	 		    conversationIntent.putExtra(ConversationActivity.FORCE_LOAD_DATASET, "1");
	 		    conversationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
	 		    // Intent.FLAG_ACTIVITY_NO_HISTORY To finish activity
	 		    context.startActivity(conversationIntent);
	 		    activity.finish();
	 		    
    		}
    		//TODO tal vez sea necesario hacer que no ignore los mensajes

        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_contacts_select);
		
		Intent i = new Intent(getApplicationContext(), LoadContactsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	    startActivityForResult(i, 1);
	    service = new ServiceManager(this, SomeService1.class, serviceHandler);
	    service.start();
	    activity = this;
	    
	    
	}

	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (requestCode == 1) {
    		/* Eliminar aviso de mensajes nuevos ya que se acaban de leer */
    	     if(resultCode == RESULT_OK){
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
    	    	 		    try {
								service.send(Message.obtain(null, XMPPManager.EVENT_NEW_CHAT, 0, 0, adapter.getItem(position).get(LoadingScreenActivity.TELEPHONE)));
							} catch (RemoteException e) {
								//TODO manage error
							}
    	    	 		    context = getApplicationContext();
    	    	 		    
    	    	 		    
    	    	 	    
    	    	 	  }
    	    	 	});
    	    	
    	      
    	}
    	}
    	
    }
	
	@Override
	protected void onStop(){
		service.unbind();
		super.onStop();
	}
	
	@Override
	protected void onStart(){
			service.start();
		super.onStart();
	}

}
