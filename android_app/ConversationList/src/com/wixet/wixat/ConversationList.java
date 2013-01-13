package com.wixet.wixat;


import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.DialogAction;
import com.wixet.wixat.adapter.ConversationListCursorAdapter;
import com.wixet.wixat.database.DataBaseHelper;
import com.wixet.wixat.service.WixatService;
import com.wixet.wixat.service.WixatService.LocalBinder;



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
	boolean configured = false;
	DataBaseHelper db;

	private View selectedView = null;
	private static ConversationListCursorAdapter adapter;
	private ActionBar actionBar;
	private ListView listView;
	private SharedPreferences settings;
	
	public OnClickListener removeChatListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
     	   if(which == 0){
     		  DialogFragment nw = new ConversationListRemoveDialogFragment(confirmRemoveChatListener);
              nw.show(getSupportFragmentManager(), "NoticeDialogFragment");
     	   }else if(which == 1){
     		   Intent call = new Intent(Intent.ACTION_CALL);
     		   call.setData(Uri.parse("tel:" + db.getParticipant(Integer.parseInt(""+selectedView.getTag())).split("@")[0] ));
     		   startActivity(call);
     		   
     	   }
        }
	};
	
	
	public OnClickListener confirmRemoveChatListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
		   /* Use the id provided in the view tag */
		   service.removeConversation(Integer.parseInt(selectedView.getTag().toString()));
     	  
		   Toast.makeText(ConversationList.this, R.string.conversation_removed, Toast.LENGTH_SHORT).show();
		   
		   /* Change cursor will close old cursor */
		   adapter.changeCursor(service.getDataBaseHelper().getConversations());
	       
     	   selectedView = null;
        }
	};
	
	/*********** El servicio ***********/
    private static WixatService service; 
    private boolean mBound;
	private ServiceConnection mConnection = new ServiceConnection() {
		
		
		
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder localService) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) localService;
            service = binder.getService();
            mBound = true;
            service.setHandler(WixatService.TYPE_CONVERSATION_LIST, serviceHandler);
            //service.showNotification(60);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    

    //TODO revisar el manejador
    private static Handler serviceHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
          // Receive message from service
    		if(msg.what == WixatService.REFRESH){
    			//Log.d("ACTUALIZANDO","REFRESCANDO");
     		    adapter.changeCursor(service.getDataBaseHelper().getConversations());
    		}

        }
    };

	
	private void firstTime(){
		
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
    	
    	
/*    	SharedPreferences.Editor editor = settings.edit();
	      editor.putString(TELEPHONE, "677077536");
	      editor.putString(PASSWORD, "123456");
	      editor.putBoolean(CONFIGURED, true);
	      editor.commit();
*/
		
    	configured = settings.getBoolean(CONFIGURED, false);
    	if(configured){
  	  
	        setContentView(R.layout.activity_converstaion_list);
	        
	        /**********************************/
	    	Intent intent = new Intent(this, WixatService.class);
	        startService(intent);

	
	        
	        actionBar = (ActionBar) findViewById(R.id.actionbar);
	        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
	        actionBar.setTitle("Home");
	
	        final Action menuAction = new DialogAction(this, showMenu(), R.drawable.ic_title_menu_default);
	        actionBar.addAction(menuAction);
	        
	        final Action newConversation = new DialogAction(this, showMenu(), R.drawable.glyphicons_150_edit);
	        actionBar.addAction(newConversation);
	        
	        
	        //Load conversations
	       // db = new DataBaseHelper(this);
	       // adapter=new ConversationListCursorAdapter(this,db.getConversations(), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	        
	        
	        listView = (ListView) findViewById(R.id.converstationlist);
	        
	        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        	  public void onItemClick(AdapterView<?> parent, View view,
	        	    int position, long id) {
	        		  	
	        		  	int conversationId =Integer.parseInt(view.getTag()+"");
	        		    Intent i = new Intent(getApplicationContext(), ConversationActivity.class);
	        		    i.putExtra(ConversationActivity.CONVERSATION_ID, conversationId);
	        		    startActivity(i);
	        		    
	        		  	//Remove message notification
	        		    ImageView img = (ImageView)view.findViewById(R.id.imageNewMessage);
	        		    if(img.getVisibility() == View.VISIBLE){
		        		    TextView title = (TextView)view.findViewById(R.id.title);
		        	        TextView text = (TextView)view.findViewById(R.id.text); 
		        	        
		        		    text.setVisibility(View.INVISIBLE);
		        	        img.setVisibility(View.INVISIBLE);
		        	        title.setTypeface(null, Typeface.NORMAL);
		        	        db.setAsRead(conversationId,true);
	        		    }
	        		    

	        	    
	        	  }
	        	}); 
	        
	        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
	      	  public boolean onItemLongClick(AdapterView<?> parent, View view,
	      	    int position, long id) {
	      		  
	      		selectedView = view;
	      			DialogFragment nw = new ConversationListOptionsDialogFragment(removeChatListener);
	      		//	selectedId = (String)view.getTag();
	      			nw.show(getSupportFragmentManager(), "listDialogFragment");

	        	
	      	    return false;
	      	  }
	      	});
	        
	        
	        //When a contact info is changed refresh the contactlist
	        getContentResolver()
	        .registerContentObserver(
	                ContactsContract.Contacts.CONTENT_URI, true,
	                new ContentObserver(null){
	                	@Override
	                    public void onChange(boolean selfChange) {
	                        super.onChange(selfChange);
	                        //adapter.changeCursor(service.getDataBaseHelper().getConversations());
	                    }
	                });
	        
    	}
    	else{
    		firstTime();
    	}
    }
    

    private DialogFragment showMenu() {
  		DialogFragment nw = new ConversationListMenuDialogFragment();
  		return nw;
    	//nw.show(getSupportFragmentManager(), "listDialogFragment");
    }


    
    
    @Override
    protected void onStart(){
        super.onStart();
        db = new DataBaseHelper(this);
        // Bind to LocalService
        if(configured){
            adapter=new ConversationListCursorAdapter(this,db.getConversations(), CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            listView.setAdapter(adapter);
            if(!mBound){
            	Intent intent = new Intent(this, WixatService.class);
        		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

	 @Override
	 protected void onStop() {
		 super.onStop();
	     db.close();
			// Unbind from the service
	        if (mBound) {
	        	service.unsetHandler(WixatService.TYPE_CONVERSATION_LIST);
	            unbindService(mConnection);
	            mBound = false;
	        }
	 }
	 
	 

}



	