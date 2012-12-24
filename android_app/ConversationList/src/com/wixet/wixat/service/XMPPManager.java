package com.wixet.wixat.service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.spark.util.DummySSLSocketFactory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import com.philippheckel.service.AbstractService;
import com.wixet.wixat.ConversationActivity;
import com.wixet.wixat.ConversationList;
import com.wixet.wixat.R;
import com.wixet.wixat.database.DataBaseHelper;

public class XMPPManager extends Thread {
	
	public static String NAMESPACE_CHATSTATES = "http://jabber.org/protocol/chatstates";
	public static int EVENT_EXTENSION = 0;
	public static int EVENT_NEW_CHAT = 1;
	public static int EVENT_NEW_MESSAGE = 2;
	public static int COMMAND_REMOVE_CHAT = 3;
	public static int COMMAND_SEND_MESSAGE = 4; 
	
	private NotificationManager notificator;
	private DataBaseHelper db;
	private Context context;
	private XMPPConnection connection;
	private HashMap<String,Chat> chatList;
	public XMPPManager(Context context, NotificationManager mNM, DataBaseHelper db) {
		notificator = mNM;
		this.db = db;
		this.context = context;
		chatList = new HashMap<String,Chat>();
	}

	
	public void send(String[] data){
		try {
			/*Log.d("CHAT",data[0]);
			Log.d("CHAT2",chatList.get(data[0])+"");*/
			if(chatList.get(data[0]) == null){
				ChatManager chatmanager = connection.getChatManager();
				Chat newChat = chatmanager.createChat(data[0], new CustomMessageListener());
				chatList.put(data[0], newChat);
			}
			chatList.get(data[0]).sendMessage(data[1]);			
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startChat(String phone){
    	
		String jid = phone+"@46.137.8.44";
		/* The chat could exists (for example if the chat is created on the other side just a short time before) */
		int id;
		Chat newChat = chatList.get(jid);
		if( newChat == null){
			ChatManager chatmanager = connection.getChatManager();
			newChat = chatmanager.createChat(jid, new CustomMessageListener());
	
	    	chatList.put(jid, newChat);
		}
		
		
	    if(db.existsChat(jid)){	
	    	id = Integer.parseInt(db.getConversationId(jid));
		}else{
			id = (int)db.insertChat(jid);
		}
    	((AbstractService) context).send(Message.obtain(null, EVENT_NEW_CHAT, (int)id, 0, jid));
		
	}
	public void run() {

		
		SharedPreferences settings = context.getSharedPreferences(ConversationList.CONFIGURATION, 0);
		  String username = settings.getString(ConversationList.TELEPHONE, null);
		  String password = settings.getString(ConversationList.PASSWORD, null);
		  
		 ConnectionConfiguration config = new ConnectionConfiguration("xmpp.wixet.com",5222, "Wixat");
		 
		 /* Solo para versiones viejas de android (compatibilidad) Mirar http://stackoverflow.com/questions/11712671/smack-no-response-from-server-not-sure-why-am-i-getting-this-error*/
	/*	 config.setTruststoreType("BKS");
		    String path = System.getProperty("javax.net.ssl.trustStore");
		    if (path == null)
		        path = System.getProperty("java.home") + File.separator + "etc"
		            + File.separator + "security" + File.separator
		            + "cacerts.bks";
		    config.setTruststorePath(path);
		/////////////////////////////////
		    config.setCompressionEnabled(true);
		    config.setSASLAuthenticationEnabled(true); 
		    */
		// config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        // config.setSocketFactory(new DummySSLSocketFactory());
         
         
		    connection = new XMPPConnection(config);
		 
		    try {
		    	//showNotification("Connecting...");
		    	Log.d("CONEXTION","COnectando");
				connection.connect();
				connection.login(username, password);
				//showNotification("Connected as "+username);
			    Presence presence = new Presence(Presence.Type.available);
			    presence.setStatus("Desde android");
			    // Send the packet (assume we have a Connection instance called "con").
			    connection.sendPacket(presence);

			          
			    //
			    final Roster roster = connection.getRoster();
			    //roster.createEntry("677077536@46.137.8.44", "677077536", null);

			    roster.addRosterListener(new RosterListener() {

					@Override
					public void entriesAdded(Collection<String> arg0) {
						// TODO Auto-generated method stub
						for(String s: arg0){
							String[] l = s.split("@");
							try {
								roster.createEntry(s, l[0], new String[] {"Wixat"});
							} catch (XMPPException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

					@Override
					public void entriesDeleted(Collection<String> arg0) {
						// TODO Auto-generated method stub
						//Log.d("ROSTER","AQUI2");
					}

					@Override
					public void entriesUpdated(Collection<String> arg0) {
						// TODO Auto-generated method stub
						//Log.d("ROSTER","AQUI3");
					}

					@Override
					public void presenceChanged(Presence arg0) {
						// TODO Auto-generated method stub
						//Log.d("ROSTER","AQUI4");
					}

			    });
			    //
			    ChatManager chatmanager = connection.getChatManager();
			    chatmanager.addChatListener(new ChatManagerListenerImpl());

			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				Log.d("CONEXTION","Error al conectarse");
				e.printStackTrace();
			}
		    
		    

    }
	
	private class ChatManagerListenerImpl implements ChatManagerListener {

	    /** {@inheritDoc} */
	    @Override
	    public void chatCreated(final Chat chat, final boolean createdLocally) {
	    	//Create locally are managed when created not here
	    	if(!createdLocally){
		    	Log.d("AQUI","Se ha recibio nuevo");
		    	String jid = chat.getParticipant();
		    	if(jid.contains("/"))
		    		jid = jid.split("/")[0];
		    	
	
		    	
		    	
		    	chat.addMessageListener(new CustomMessageListener());
		    	Log.d("INSERTANDO COMO",jid);
		    	chatList.put(jid, chat);
		    	
		    	if(!db.existsChat(jid)){
	        		long id = db.insertChat(jid);
	        		((AbstractService) context).send(Message.obtain(null, EVENT_NEW_CHAT, (int)id, 0, jid));
	        		if(!createdLocally){
	        			showNotification("New chat: "+jid);
	        		}
	    		}
	    	}
	    	
	    	//chat.getPar
	        //
	    	
	    	
	    }
	    

	}
	
	private class CustomMessageListener implements MessageListener {
		private String conversationId = null;
        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
        	conversationId = db.getConversationId(message.getFrom().split("/")[0]);
        	Log.d("CHAT",message.getBody());
        	if(message.getBody() == null){
        		Iterator<PacketExtension> i = message.getExtensions().iterator();
	        	while(i.hasNext()){
	        		PacketExtension p = i.next();
	        		if(p.getNamespace().equals(NAMESPACE_CHATSTATES)){
	        			
	        			((AbstractService) context).send(Message.obtain(null, EVENT_EXTENSION, p));
	        		}
	        		
	        	}
        	}else{
        		if(((AbstractService) context).totalClients() > 0)
        			((AbstractService) context).send(Message.obtain(null, EVENT_NEW_MESSAGE, Integer.parseInt(conversationId), 0, message));
        		else{
        			db.setAsRead(conversationId, false);
        			//showMessageNotification(conversationId, message);
        		}
        		db.insertMessage(conversationId, message.getFrom().split("/")[0], message.getBody());
        	}
            
        }

    }
	
	public void removeChat(String chat){
		chatList.remove(chat);
	}
	
	private void showMessageNotification(String conversationId, org.jivesoftware.smack.packet.Message message) {

		NotificationManager notificationManager = (NotificationManager) 
				  context.getSystemService(context.NOTIFICATION_SERVICE);
		
		
		Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(ConversationActivity.CONVERSATION_ID, conversationId);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

		
		
		
		Log.d("NOTIF","mostrando");
        // Set the icon, scrolling text and timestamp
		String cosa = "a";
        Notification notification = new Notification(R.drawable.stat_sample, cosa,
                System.currentTimeMillis());


        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, context.getText(R.string.local_service_label),
        		cosa, pIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.string.local_service_started, notification);
    }
	
	private void showNotification(String text) {

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, ConversationList.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(context, context.getText(R.string.local_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificator.notify(R.string.local_service_started, notification);
    }

}
