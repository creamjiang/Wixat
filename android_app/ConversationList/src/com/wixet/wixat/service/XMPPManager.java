package com.wixet.wixat.service;

import java.util.HashMap;
import java.util.Iterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import android.content.SharedPreferences;
import android.util.Log;

import com.wixet.utils.ServerConfiguration;
import com.wixet.wixat.ConversationList;
import com.wixet.wixat.database.DataBaseHelper;

public class XMPPManager extends Thread {
	
	public static final String NAMESPACE_CHATSTATES = "http://jabber.org/protocol/chatstates";
	public static final int EVENT_EXTENSION = 0;
	public static final int EVENT_NEW_CHAT = 1;
	public static final int EVENT_NEW_MESSAGE = 2;
	public static final int COMMAND_REMOVE_CHAT = 3;
	public static final int COMMAND_SEND_MESSAGE = 4; 
	
	

	private WixatService service;
	private XMPPConnection connection;
	private DataBaseHelper db;
	private HashMap<String,Chat> chatList;
	private String username;
	private String password;
	public XMPPManager(WixatService service, String username, String password) {
		this.service = service;
		db = service.getDataBaseHelper();
		chatList = new HashMap<String,Chat>();
		this.username = username;
		this.password = password;
	}

	
	/*
	 * to: user@host not only user
	 */
	public boolean send(String to, String body){
		boolean sent = false;
		/*Log.d("ESTADO",""+connection.isConnected());
		Log.d("SECURE",""+connection.isSecureConnection());
		Log.d("COMPRESSION",""+connection.isUsingCompression());
		Log.d("CONECTADO",""+connection.isConnected());
		*/
		
		if(connection.isConnected()){
			try {
				if(chatList.get(to) == null){
					ChatManager chatmanager = connection.getChatManager();
					Chat newChat = chatmanager.createChat(to, new CustomMessageListener());
					chatList.put(to, newChat);
				}
				chatList.get(to).sendMessage(body);
				sent = true;
			} catch (IllegalStateException e) {
				//Probably connection its not connected (broken pipe or similar) 
				e.printStackTrace();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sent;
	}
	
	public int startChat(String to){
    	
		/* The chat could exists (for example if the chat is created on the other side just a short time before) */
		int id;
		Chat newChat = chatList.get(to);
		if( newChat == null){
			ChatManager chatmanager = connection.getChatManager();
			newChat = chatmanager.createChat(to, new CustomMessageListener());
	
	    	chatList.put(to, newChat);
		}
		
		
	    if(db.existsChat(to)){	
	    	id = db.getConversationId(to);
		}else{
			id = (int)db.insertChat(to, false);
		}
	    return id;
	    //TODO Notify new chat is created
    	//((AbstractService) context).send(Message.obtain(null, EVENT_NEW_CHAT, (int)id, 0, jid));
		
	}
	public void run() {

		
		  
		 ConnectionConfiguration config = new ConnectionConfiguration(ServerConfiguration.HOSTNAME,ServerConfiguration.PORT, ServerConfiguration.RESOURCE);

         
         
		  connection = new XMPPConnection(config);
		 
		    try {
				connection.connect();
				connection.login(username, password);
			    Presence presence = new Presence(Presence.Type.available);
			    presence.setStatus("Desde android");
			    connection.sendPacket(presence);
			    ChatManager chatmanager = connection.getChatManager();
			    chatmanager.addChatListener(new ChatManagerListenerImpl());
			    Log.d("CONEXION","Conectado");
			} catch (XMPPException e) {
				Log.d("CONEXION","Error al conectarse");
				e.printStackTrace();
			}
		    
		    

    }
	
	private class ChatManagerListenerImpl implements ChatManagerListener {

	    @Override
	    public void chatCreated(final Chat chat, final boolean createdLocally) {
	    	//Created locally chats are managed when created not here
	    	if(!createdLocally){
		    	String jid = chat.getParticipant();
		    	//Remove resource
		    	if(jid.contains("/"))
		    		jid = jid.split("/")[0];
		    	
		    	chat.addMessageListener(new CustomMessageListener());
		    	chatList.put(jid, chat);
		    	
		    	if(!db.existsChat(jid)){
	        		long id = db.insertChat(jid, true);
	        		if(!service.notifyRefresh()){
	        			//Activity is not visible, show notification
	        			service.showNotification((int)id);
	        		}
	    		}
	    	}
	    	
	    	
	    }
	    

	}
	
	private class CustomMessageListener implements MessageListener {
		
        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
        	//Remove the resource from jid
        	String from = message.getFrom().split("/")[0];
        	//Log.d("DATABASE",database==null?"NULL":"NO NULL");
        	int	conversationId = db.getConversationId(from);
        	if(message.getBody() == null){
        		Iterator<PacketExtension> i = message.getExtensions().iterator();
	        	while(i.hasNext()){
	        		PacketExtension p = i.next();
	        		if(p.getNamespace().equals(NAMESPACE_CHATSTATES)){
	        			//TODO notify
	        			//((AbstractService) context).send(Message.obtain(null, EVENT_EXTENSION, p));
	        		}
	        		
	        	}
        	}else{
        		if(!service.notifyNewMessageReceived(conversationId, message)){
        			//Activity is not visible, show notification
        			service.showNotification((int)conversationId);
        		}
        		db.insertMessage(conversationId, from, message.getBody());
        	}
            
        }

    }
	
	public void removeChat(String jid){
		chatList.remove(jid);
		db.removeConversation(db.getConversationId(jid));
	}
	


}
