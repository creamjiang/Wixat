package com.wixet.wixat;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.DialogAction;
import com.philippheckel.service.ServiceManager;
import com.wixet.wixat.adapter.ConversationArrayAdapter;
import com.wixet.wixat.database.Chat;
import com.wixet.wixat.database.ChatMessage;
import com.wixet.wixat.database.DataBaseHelper;
import com.wixet.wixat.service.SomeService1;
import com.wixet.wixat.service.XMPPManager;

public class ConversationActivity extends FragmentActivity {


	public static final String VALUE = "value";
	public static final String CONVERSATION_ID = "conversationId";
	public static final String FORCE_LOAD_DATASET = "force";
	Bitmap participantPhoto;

	//private ArrayAdapter<String> adapter;
	private ActionBar actionBar;
	
	private static String conversationId = null;
	private ListView list;
    private static ConversationArrayAdapter adapter;
    private static ServiceManager service; 
    private EditText userInput;
    private DataBaseHelper db;
    private static String me = null;
    private static SharedPreferences settings;
    private static String participantDisplayName = null;
    private static String participant = null;
    private static Handler messageHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
          // Receive message from service
    		if(msg.what == XMPPManager.EVENT_NEW_MESSAGE){
    			org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) msg.obj;
    			String from = message.getFrom().split("/")[0];
    			if(participant.equals(from)){
	        		HashMap <String,String> map = new HashMap <String,String>();
	        		
	        		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
	            	Date date = new Date();
	            	
	            	map.put(ChatMessage._ID, conversationId);
	        		map.put(ChatMessage.COLUMN_NAME_AUTHOR, message.getFrom().split("@")[0]);
	        		map.put(ChatMessage.COLUMN_NAME_BODY, message.getBody());
	        		map.put(ChatMessage.COLUMN_NAME_CREATED_AT, dateFormat.format(date));
	        		
	        		adapter.add(map);
    			}else{
    				//Not for me, say to the service shoNotification
    				try {
						service.send(Message.obtain(null, XMPPManager.EVENT_NEW_MESSAGE, msg.arg1, 0));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				//Force update conversationList to view "new message"
    				SharedPreferences.Editor editor = settings.edit();
    	    		editor.putString(FORCE_LOAD_DATASET, from);
    	    		editor.putString(VALUE, "1");
    	    		editor.commit();
    			}
    		}

        }
    };
	
    /* Para cuando se pulsa a enviar */
    EditText.OnEditorActionListener exampleListener = new EditText.OnEditorActionListener(){

		@Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	        boolean handled = false;
	        Log.d("ESTO",actionId+"");
	        if (actionId == EditorInfo.IME_ACTION_SEND) {
	            // Send the user message
	            handled = true;
	            sendButtonClick(null);
	        }
	        
	        return handled;
	    }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        
        /* Cargar telefono del dueño y demás datos*/
        settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
        me = settings.getString(ConversationList.TELEPHONE, null);
        
        conversationId = getIntent().getExtras().getString(CONVERSATION_ID);
        String forceLoad = getIntent().getExtras().getString(FORCE_LOAD_DATASET);

        

        actionBar = (ActionBar) findViewById(R.id.actionbarConversation);
        userInput = (EditText) findViewById(R.id.userInput);
        
        userInput.setOnEditorActionListener(exampleListener);

        

        
        final Action menuAction = new DialogAction(this, showMenu(), R.drawable.ic_title_menu_default);
        actionBar.addAction(menuAction);
        


        /* Servicio para manejar los mensajes entrantes y salientes */
        this.service = new ServiceManager(this, SomeService1.class, messageHandler);
        service.start();
        db = new DataBaseHelper(this);
        
        
        HashMap<String,String> conversation = db.getConversation(conversationId);
        
        // Load contact name and thumbnail (de momento no se usa el thumbnail)
        participant = conversation.get(Chat.COLUMN_NAME_PARTICIPANT);
        
        if(forceLoad != null){
        	//Activity initiated from notification
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString(FORCE_LOAD_DATASET, participant);
    		editor.commit();
        }
        
        participantDisplayName = conversation.get(Chat.COLUMN_NAME_PARTICIPANT).split("@")[0];
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( participantDisplayName));
        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_ID }, null, null, null);
        
        
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                participantDisplayName = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                
                int photoId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID));
                if (photoId > 0) {
                	participantPhoto = com.wixet.utils.Contacts.fetchThumbnail(photoId,contentResolver);
                }
                
                
                
                contactLookup.close();
            }
        
        actionBar.setTitle("Conversation with "+participantDisplayName);
		list=(ListView)findViewById(R.id.conversation);
		
		// Getting adapter by passing xml data ArrayList
        adapter=new ConversationArrayAdapter(this, R.layout.conversation_row_left ,db.getMessages(conversationId), me);
        list.setAdapter(adapter);
        
        if(db.setAsRead(conversationId, true) > 0){
        	//Before unread, now is read. Update conversationList
        	Intent returnIntent = new Intent();
        	setResult(RESULT_OK,returnIntent);     
        }

        
        
        
        
    }


    private DialogFragment showMenu() {
  		DialogFragment nw = new ConversationMenuDialogFragment();
  		return nw;
    }
    
    public void sendButtonClick(View view) {
    	HashMap<String, String> map = new HashMap<String, String>();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
    	Date date = new Date();
    	String text = userInput.getText()+"";
    	map.put(ChatMessage._ID, conversationId);
		map.put(ChatMessage.COLUMN_NAME_AUTHOR, me);
		map.put(ChatMessage.COLUMN_NAME_BODY, text);
		map.put(ChatMessage.COLUMN_NAME_CREATED_AT, dateFormat.format(date));
		String[] data  = {participant,text};
		try {
			service.send(Message.obtain(null, XMPPManager.COMMAND_SEND_MESSAGE, data));
			db.insertMessage(conversationId, me, text);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		userInput.setText("");
		
		
		adapter.add(map);
    }
    
    
	@Override
	protected void onStop() {
	  service.unbind();
  

	  super.onStop();
	}

}
