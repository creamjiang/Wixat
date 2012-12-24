package com.wixet.wixat;


import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.DialogAction;
import com.philippheckel.service.ServiceManager;
import com.wixet.utils.ContactObserver;
import com.wixet.wixat.adapter.ConversationListArrayAdapter;
import com.wixet.wixat.database.Chat;
import com.wixet.wixat.database.DataBaseHelper;
import com.wixet.wixat.service.SomeService1;
import com.wixet.wixat.service.XMPPManager;



public class ConversationList extends FragmentActivity  {

	public static String REMOVE_CONFIRMATION = "remove-confirmation";
	public static String MESSAGE = "message";
	public static String ASK = "ask";
	public static String REMOVE = "remove";
	public static String CONVERSATION_MENU = "menu";
	public static String CONVERSATION_NODES = "nodes";
	public static String TELEPHONE = "telephone";
	public static String PASSWORD = "password";
	public static String CONFIGURED = "configured";
	public static int lastIndex = 0;
	public static String CONFIGURATION = "config";
	
	boolean updatedDataSet;

	private View selectedView = null;
	private static ArrayList<HashMap<String, String>> conversationList;
	private static ConversationListArrayAdapter adapter;
	private ActionBar actionBar;
	private ListView listView;
	private static DataBaseHelper db;
	private SharedPreferences settings;
	
	public OnClickListener removeChatListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
     	   if(which == 0){
     		   
     	   }else if(which == 1){

     		   DialogFragment nw = new ConversationListRemoveDialogFragment(confirmRemoveChatListener);
               nw.show(getSupportFragmentManager(), "NoticeDialogFragment");
     	   }
        }
	};
	
	
	public OnClickListener confirmRemoveChatListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
     	   db.removeChat(selectedView.getTag(R.id.CONVERSATION_ID).toString());
     	   try {
			service.send(Message.obtain(null, XMPPManager.COMMAND_REMOVE_CHAT, 1, 1,selectedView.getTag(R.id.CONVERSATION_ID).toString()));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
     	  

		   Toast.makeText(ConversationList.this, R.string.conversation_removed, Toast.LENGTH_SHORT).show();
		   
		   conversationList = db.getConversations();
		   if(conversationList.size() > 0){
			   adapter.updateList(conversationList);
		   }else{
			   adapter.clear();
		   }
	       
     	   selectedView = null;
        }
	};
	
	/*********** El servicio ***********/
    private static ServiceManager service; 
    private static Handler serviceHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
          // Receive message from service
    		if(msg.what == XMPPManager.EVENT_NEW_CHAT){
    			//Refresh conversations
        		HashMap <String,String> map = new HashMap <String,String>();
        		map.put(Chat._ID, msg.arg2 + "");
        		map.put(Chat.COLUMN_NAME_PARTICIPANT, (String) msg.obj);
        		adapter.add(map);

        		conversationList = db.getConversations();
     		    adapter.updateList(conversationList);
    		}
    		else if(msg.what == XMPPManager.EVENT_NEW_MESSAGE){
    			//Update the chat entry and say to the service showNotification
    			//Send notification request
    			try {
					service.send(Message.obtain(null, XMPPManager.EVENT_NEW_MESSAGE, msg.arg1, 0));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			//Refresh conversations
    			
    			org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message)msg.obj;
        		HashMap<String,String> conversation = adapter.getConversation(message.getFrom().split("/")[0]);
        		conversation.put(Chat.COLUMN_NAME_NEW_MESSAGES, "1");
     		    adapter.notifyDataSetChanged();
    		}

        }
    };
    
    /////////////////////////////////
	
	/*private void startProgress(){
		actionBar.setProgressBarVisibility(View.VISIBLE);
	}
	
	private void stopProgress(){
		actionBar.setProgressBarVisibility(View.GONE);
	}*/
	
	private void checkFirstTime(){
		
		//SharedPreferences settings = getSharedPreferences(CONFIGURATION, 0);

		  //SharedPreferences.Editor editor = settings.edit();
					//Detección de número automática desactivada
		    	  //TelephonyManager tMgr =(TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			  	  //String mPhoneNumber = tMgr.getLine1Number();
			  	  Intent i;
			  	  //if(mPhoneNumber.length() == 0){
		    	      i = new Intent(getApplicationContext(), FirstTimeAskActivity.class);
			  	  /*}else{
		    	      editor.putString(TELEPHONE, mPhoneNumber);
		    	      editor.commit();
		    	      i = new Intent(getApplicationContext(), FirstTimeActivity.class);
			  	  }*/
      	    	  startActivity(i);
	}
	

    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	
    	settings = getSharedPreferences(CONFIGURATION, 0);
    	


		
    	
    	if(settings.getBoolean(CONFIGURED, false)){
  	  
	        setContentView(R.layout.activity_converstaion_list);
	        
	    	updatedDataSet = false;
	        /**********************************/
	    	/* Lo del jambo */ 
	        this.service = new ServiceManager(this, SomeService1.class, serviceHandler);

	    	
	        //TODO actualizar cuando actualiza contacto
	       /* ContactObserver contentObserver = new ContactObserver();
	        */
	        //this.getApplicationContext().getContentResolver().registerContentObserver (ContactsContract.Contacts.CONTENT_URI, true, contentObserver);
	       /* ContentResolver contentResolver = getContentResolver();
	        for(int i =0; i <50; i++){
	        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( "979 15 33 84"));
	        String name = "?";

	        
	        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
	        
	        
	            if (contactLookup != null && contactLookup.getCount() > 0) {
	                contactLookup.moveToNext();
	                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	                Log.d("NOMBRE",name+" "+i);
	                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	            }
	            if (contactLookup != null) {
	                contactLookup.close();
	            }
	        }
	        */
	        
	        
	        
	        
	        //service.send(Message.obtain(null, SomeService1.MSG_VALUE, 12345, 0));
	
	        
	        actionBar = (ActionBar) findViewById(R.id.actionbar);
	        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
	        actionBar.setTitle("Home");
	
	        final Action menuAction = new DialogAction(this, showMenu(), R.drawable.ic_title_menu_default);
	        actionBar.addAction(menuAction);
	        
	        final Action newConversation = new DialogAction(this, showMenu(), R.drawable.glyphicons_150_edit);
	        actionBar.addAction(newConversation);
	        
	        //final Action otherAction = new IntentAction(this, new Intent(this, NewNodeActivity.class), R.drawable.ic_title_export_default);
	        //actionBar.addAction(otherAction);
	
	
	       
	        
 
        
	        /*Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
	
	        while (phones.moveToNext())
	        {
	             String Name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
	             String Number=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	             System.out.println(Name+ " "+Number);
	
	        }*/
	        
	        
	        
	        //////////////////////////////
	        db = new DataBaseHelper(this);
	        conversationList = db.getConversations();
	//        conversationList.add(object)
	        
	        // Assign adapter to ListView
	        //adapter=new ConversationListAdapter(this, conversationList); 
	        adapter=new ConversationListArrayAdapter(this,R.layout.conversation_list_row, conversationList);
	        
	        listView = (ListView) findViewById(R.id.converstationlist);
	
	        listView.setAdapter(adapter); 
	        
	        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        	  public void onItemClick(AdapterView<?> parent, View view,
	        	    int position, long id) {
	        		    Intent i = new Intent(getApplicationContext(), ConversationActivity.class);
	        		    i.putExtra(ConversationActivity.CONVERSATION_ID, view.getTag(R.id.CONVERSATION_ID)+"");
	        		    startActivityForResult(i, 1);
	        		    lastIndex = position;
	        	    	//startActivity(i);
	        		  /*
	        	    Toast.makeText(getApplicationContext(),
	        	      "Click ListItem Number " + position, Toast.LENGTH_LONG)
	        	      .show();
	        	    */
	        	    
	        	  }
	        	}); 
	        
	        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
	      	  public boolean onItemLongClick(AdapterView<?> parent, View view,
	      	    int position, long id) {
	      		  
	      		selectedView = view;
	      		DialogFragment nw = new ConversationListOptionsDialogFragment(removeChatListener);
	      		//selectedId = (String)view.getTag();
	        	nw.show(getSupportFragmentManager(), "listDialogFragment");
	        	
	      	    return false;
	      	  }
	      	});
	        
	        getContentResolver()
	        .registerContentObserver(
	                ContactsContract.Contacts.CONTENT_URI, true,
	                new ContactObserver(this));
	        
    	}
    	else{
    		Log.d("CONVERSATIONLIST","checking first time");
    		checkFirstTime();
    	}
    }
    
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, ConversationList.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private DialogFragment showMenu() {
  		DialogFragment nw = new ConversationListMenuDialogFragment();
  		return nw;
    	//nw.show(getSupportFragmentManager(), "listDialogFragment");
    }

    
    public void updatedDataSet(){
    	updatedDataSet = true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_converstaion_list, menu);
        return true;
    }
    
     
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (requestCode == 1) {
    		/* Eliminar aviso de mensajes nuevos ya que se acaban de leer */
    	     if(resultCode == RESULT_OK){
    	    	 adapter.getItem(lastIndex).put(Chat.COLUMN_NAME_NEW_MESSAGES, "0");
    	    	 adapter.notifyDataSetChanged();
    	    	 
    	    	
    	      
    	}
    	}
    	
    }
    
    @Override
    protected void onStart(){
    	String updateParticipant = settings.getString(ConversationActivity.FORCE_LOAD_DATASET, null);
    	if(updateParticipant != null){
    		
    		HashMap<String,String> conversation = adapter.getConversation(updateParticipant);
    		if(conversation == null){
    			//New conversation

    			adapter.add(db.getConversation(db.getConversationId(updateParticipant)));
    		}else{
    			/* At the moment, it only can be caused because messages has been read */
    			conversation.put(Chat.COLUMN_NAME_NEW_MESSAGES, settings.getString(ConversationActivity.VALUE, "0"));
    		}
    		SharedPreferences.Editor editor = settings.edit();
    		editor.remove(ConversationActivity.FORCE_LOAD_DATASET);
    		editor.remove(ConversationActivity.VALUE);
    		editor.commit();
    		adapter.notifyDataSetChanged();
    	
    		
    	}
    	if(service !=  null)
    		service.start();
        super.onStart();
    }
    @Override
	 protected void onResume() {
    	if(updatedDataSet){
    		adapter.notifyDataSetChanged();
    		updatedDataSet = false;
    	}
    	super.onResume();
    }
	 @Override
	 protected void onStop() {
		 if(service!= null)
			 service.unbind();
		 Log.d("STOP","parado");
	   super.onStop();
	 }

}



	