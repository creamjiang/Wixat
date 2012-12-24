package com.wixet.wixat.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.philippheckel.service.AbstractService;
import com.wixet.wixat.ConversationActivity;
import com.wixet.wixat.ConversationList;
import com.wixet.wixat.R;
import com.wixet.wixat.database.DataBaseHelper;

public class SomeService1 extends AbstractService {
	
	  private NotificationManager mNM;
	  private XMPPManager t;
	  private DataBaseHelper db;

	  
	  @Override 
	  public void onStartService() {
		  
		  SharedPreferences settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
		  boolean configured = settings.getBoolean(ConversationList.CONFIGURED, false);
			
		  if(configured){
			  db = new DataBaseHelper(this);
			  mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			  t = new XMPPManager(this, mNM, db);
			  t.start();
		  }
		  
		  //showNotification("hola que pasa");

	        /*Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
	    	
	        while (phones.moveToNext())
	        {
	             String Name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
	             String Number=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	             System.out.println(Name+ " "+Number);
	
	        }*/
		  
	  }
	 
	    /**
	     * Show a notification while this service is running.
	     */
	    private void showMessageNotification(String text, String conversationId) {
	        // In this sample, we'll use the same text for the ticker and the expanded notification

	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(R.drawable.stat_sample, text,
	                System.currentTimeMillis());
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;

	        // The PendingIntent to launch our activity if the user selects this notification
	        Intent i =new Intent(this, ConversationActivity.class);
	        i.putExtra(ConversationActivity.CONVERSATION_ID, conversationId);
	        i.putExtra(ConversationActivity.FORCE_LOAD_DATASET, "1");
	        
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                i, 0);
	        		
	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
	                       text, contentIntent);

	        // Send the notification.
	        // We use a layout id because it is a unique number.  We use it later to cancel.
	        mNM.notify(R.string.local_service_started, notification);
	    }
	    
	  @Override
	  public void onStopService() {
		  //showNotification("Service stopped");
		  Log.d("SERVICE","STOPPED");
	  }   
	 
	  @Override
	  public void onReceiveMessage(Message msg) {

	    if (msg.what == XMPPManager.COMMAND_SEND_MESSAGE) {
	    	t.send((String[]) msg.obj);
	    }else if (msg.what == XMPPManager.COMMAND_REMOVE_CHAT) {
	    	t.removeChat((String) msg.obj);
	    }else if (msg.what == XMPPManager.EVENT_NEW_MESSAGE) {
	    	db.setAsRead(msg.arg1+"", false);
	    	showMessageNotification("new message para "+msg.arg1, msg.arg1+"");
	    }else if (msg.what == XMPPManager.EVENT_NEW_CHAT) {
	    	t.startChat((String)msg.obj);
	    }
	    
	    
	    
	  }
	}