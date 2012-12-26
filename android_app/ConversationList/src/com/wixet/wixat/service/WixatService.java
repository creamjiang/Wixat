package com.wixet.wixat.service;

import com.wixet.wixat.ConversationActivity;
import com.wixet.wixat.ConversationList;
import com.wixet.wixat.R;
import com.wixet.wixat.database.DataBaseHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;


public class WixatService extends Service{

	public static final int TYPE_NONE = 0;
	public static final int TYPE_CONVERSATION = 1;
	public static final int TYPE_CONVERSATION_LIST = 2;
	public static final int REFRESH = 3;
    // Binder given to clients
	
	private Messenger messenger;
	private int bindedType;
	//private Messenger[] activityMessengers = new Messenger[2];
	//private boolean[] notifyRefreshData = new boolean[2];
	
	//For conversationList
	private boolean notifyRefreshData;
    private final IBinder mBinder = new LocalBinder();
    private XMPPManager t;
    Messenger mMessenger;
    private DataBaseHelper db; 
    private NotificationManager mNotificationManager;
    
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	public WixatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WixatService.this;
        }
    }
    
	@Override
    public void onCreate() {
        super.onCreate();
        
		
		  SharedPreferences settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
		  String username = settings.getString(ConversationList.TELEPHONE, null);
		  String password = settings.getString(ConversationList.PASSWORD, null);
		  
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		db = new DataBaseHelper(this);
		t = new XMPPManager(this, username, password);
		t.start();
            
	}
	
	@Override
    public void onDestroy() {
		db.close();
        super.onDestroy();
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Always active
		return START_STICKY;
	}
	
	public DataBaseHelper getDataBaseHelper(){
		return db;
	}
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    
    public void showNotification(int conversationId){
        
    	
    	//Log.d("NOTIFICATION","FOR: "+conversationId);
    	
    	// Get display name
        String name = db.getParticipant(conversationId).split("@")[0];
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( name));
        

        // Load contact name and thumbnail
        Cursor contactLookup = getContentResolver().query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        
        
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                
                contactLookup.close();
            }
            
    	
    	final Notification notifyDetails = new Notification(R.drawable.ic_launcher,name,System.currentTimeMillis());

    	long[] vibrate = {100,100,200,300};
    	notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
    	notifyDetails.vibrate = vibrate;
    	notifyDetails.defaults =Notification.DEFAULT_ALL;
    	Context context = getApplicationContext();
    	
    	

            
    	CharSequence contentTitle = name;
    	CharSequence contentText = "Nuevos mensajes";

    	Intent notifyIntent = new Intent(context, ConversationActivity.class);
    	notifyIntent.putExtra(ConversationActivity.CONVERSATION_ID, conversationId);

    	PendingIntent intent =
    	PendingIntent.getActivity(this, 0,
    	notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

    	notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);

    	mNotificationManager.notify(conversationId, notifyDetails);
    	//notifyDetails.
    	
    	/*Intent notificationIntent = new Intent(this, ConversationActivity.class);
    	notificationIntent.putExtra(ConversationActivity.CONVERSATION_ID, conversationId);
    	notificationIntent.putExtra("MIERDA", "ESTOOO");
    	notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	// Get display name
        String name = db.getParticipant(conversationId).split("@")[0];
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( name));
        

        // Load contact name and thumbnail
        Cursor contactLookup = getContentResolver().query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        
        
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                
                contactLookup.close();
            }
            
    	
    	
        Builder mNotifyBuilder = new NotificationCompat.Builder(this)
            .setContentTitle(name)
            .setContentText("Nuevos mensajes 2")
            .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
            .setSmallIcon(R.drawable.stat_sample);

            
        	Notification notif = mNotifyBuilder.build();
        	notif.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(
                    conversationId,
                    notif
                    );*/
    }
    
    /* Return true if sent */
    /* For conversationList */
    public boolean notifyRefresh(){
    	boolean sent = false;
    
    	
    	
    	if(bindedType == TYPE_CONVERSATION_LIST){
    		//Notify now
    		try {
				messenger.send(Message.obtain(null, REFRESH, 0, 0));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		sent = true;
    		notifyRefreshData = false;
    	}else notifyRefreshData = true;
    	
    	return sent;
    	
    }
    
    /* Return true if sent */
    public boolean notifyNewMessageReceived(int conversationId, org.jivesoftware.smack.packet.Message message){
    	
    	boolean sent = false;
    	if(bindedType == TYPE_CONVERSATION){
    		//Notify now
    		try {
				messenger.send(Message.obtain(null, XMPPManager.EVENT_NEW_MESSAGE, 0, 0, message));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		sent = true;
    	}else if(bindedType == TYPE_CONVERSATION_LIST){
    		db.setAsRead(conversationId, false);
    		sent = notifyRefresh();
    		
    	}
    	
    	return sent;
    	
    }
    
    public int startConversation(String jid){
    	return t.startChat(jid);
    }
    
	public void setHandler(int type, Handler h){
		messenger = new Messenger(h);
		bindedType = type;
		
		//If there are any notification, send it
		if(notifyRefreshData && bindedType == TYPE_CONVERSATION_LIST){
			notifyRefresh();
		}
		
	}
	
    public void unsetHandler(int type){
    	
    	/* Sometimes onStop is called before onStart of other acitivity wich produces that unset is called before set*/
    	if(type == bindedType){
    		messenger = null;
    		bindedType = TYPE_NONE;
    	}
    }
	
	public boolean sendMessage(String from, String to, String body){
		if(t.send(to, body)){
			db.insertMessage(db.getConversationId(to), from, body);
			return true;
		}else return false;
	}
	
	public void removeConversation(int conversationId){
		db.removeConversation(conversationId);
	}

}