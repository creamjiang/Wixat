package com.wixet.wixat;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.DialogAction;
import com.wixet.utils.ServerConfiguration;
import com.wixet.wixat.adapter.ConversationArrayAdapter;
import com.wixet.wixat.database.ChatMessage;
import com.wixet.wixat.database.DataBaseHelper;
import com.wixet.wixat.service.WixatService;
import com.wixet.wixat.service.WixatService.LocalBinder;
import com.wixet.wixat.service.XMPPManager;

public class ConversationActivity extends FragmentActivity {


	public static final String VALUE = "value";
	public static final String CONVERSATION_ID = "conversationId";
	public static final String FORCE_LOAD_DATASET = "force";
	Bitmap participantPhoto;

	//private ArrayAdapter<String> adapter;
	private ActionBar actionBar;
	
	private static int conversationId;
	private ListView list;
    private static ConversationArrayAdapter adapter;
    private static WixatService service; 
    private EditText userInput;
    private DataBaseHelper db;
    private static String me = null;
    private static SharedPreferences settings;
    private static String participantDisplayName = null;
    private static String participant = null;
    private boolean mBound;
    private static Handler messageHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
          // Receive message from service
    		if(msg.what == XMPPManager.EVENT_NEW_MESSAGE){
    			org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) msg.obj;
    			String from = message.getFrom().split("/")[0];
    			//if(participant.equals(from)){
	        		HashMap <String,String> map = new HashMap <String,String>();
	        		
	        		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
	            	Date date = new Date();
	            	
	            	map.put(ChatMessage._ID, conversationId+"");
	        		map.put(ChatMessage.COLUMN_NAME_AUTHOR, from);
	        		map.put(ChatMessage.COLUMN_NAME_BODY, message.getBody());
	        		map.put(ChatMessage.COLUMN_NAME_CREATED_AT, dateFormat.format(date));
	        		
	        		adapter.add(map);
    			/*}else{
    				//Not for me, say to the service shoNotification
    				try {
    					//TODO check that shit
						//service.send(Message.obtain(null, XMPPManager.EVENT_NEW_MESSAGE, msg.arg1, 0));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				//Force update conversationList to view "new message"
    				SharedPreferences.Editor editor = settings.edit();
    	    		editor.putString(FORCE_LOAD_DATASET, from);
    	    		editor.putString(VALUE, "1");
    	    		editor.commit();
    			}*/
    		}

        }
    };
	
    
    private ServiceConnection mConnection = new ServiceConnection() {
    	
    	
    	
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder localService) {

            LocalBinder binder = (LocalBinder) localService;
            service = binder.getService();
            mBound = true;
            service.setHandler(WixatService.TYPE_CONVERSATION, messageHandler);


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    /* Para cuando se pulsa a enviar */
    EditText.OnEditorActionListener exampleListener = new EditText.OnEditorActionListener(){

		@Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	        boolean handled = false;
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
        me = settings.getString(ConversationList.TELEPHONE, null) +"@"+ServerConfiguration.HOSTNAME;
        
        conversationId = getIntent().getExtras().getInt(CONVERSATION_ID);
        
        Log.d("Conversation","ID: "+conversationId);
        
        

        

        actionBar = (ActionBar) findViewById(R.id.actionbarConversation);
        userInput = (EditText) findViewById(R.id.userInput);
        
        userInput.setOnEditorActionListener(exampleListener);

        

        
        final Action menuAction = new DialogAction(this, showMenu(), R.drawable.ic_title_menu_default);
        actionBar.addAction(menuAction);

        
        
        
        db = new DataBaseHelper(this);

        
        /* Get the participant display name */
        // Load contact name and thumbnail (de momento no se usa el thumbnail)
        participant = db.getParticipant(conversationId);
        
        participantDisplayName = participant.split("@")[0];
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
        
        /****************************************************/
        actionBar.setTitle(participantDisplayName);
		list=(ListView)findViewById(R.id.conversation);
		
		// Getting adapter by passing xml data ArrayList
		adapter=new ConversationArrayAdapter(this, db.getMessagesData(conversationId), participant);
		
        //adapter=new ConversationCursorAdapter(this, R.layout.conversation_row_left ,db.getMessages(conversationId), me);
        list.setAdapter(adapter);

        
        
        
        
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
    	map.put(ChatMessage._ID, conversationId+"");
		map.put(ChatMessage.COLUMN_NAME_AUTHOR, me);
		map.put(ChatMessage.COLUMN_NAME_BODY, text);
		map.put(ChatMessage.COLUMN_NAME_CREATED_AT, dateFormat.format(date));
		
		if(service.sendMessage(me, participant, text)){
			userInput.setText("");
			adapter.add(map);
		}else{
            Toast.makeText(getApplicationContext(),
            		getApplicationContext().getText(R.string.notice_not_connected),
                    Toast.LENGTH_SHORT).show();
		}
    }
    
    
	@Override
	protected void onStop() {
	       super.onStop();
			// Unbind from the service
	        if (mBound) {
	        	//mServicio.unsetHandler();
	        	service.unsetHandler(WixatService.TYPE_CONVERSATION);
	            unbindService(mConnection);
	            mBound = false;
	        }
	        db.close();
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, WixatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
    }

}
