package com.wixet.wixat.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import com.wixet.utils.ServerConfiguration;
import com.wixet.wixat.database.DataBaseHelper;

public class XMPPManager extends Thread {
	
	
	/* To say Dalvic reconnect wich makes the connection more stable */
	static {
	    try {
	        Class.forName("org.jivesoftware.smack.ReconnectionManager");
	    } catch (ClassNotFoundException ex) {
	        // problem loading reconnection manager
	    }
	}
	
	public static final String NAMESPACE_CHATSTATES = "http://jabber.org/protocol/chatstates";
	public static final int EVENT_EXTENSION = 0;
	public static final int EVENT_NEW_CHAT = 1;
	public static final int EVENT_NEW_MESSAGE = 2;
	public static final int COMMAND_REMOVE_CHAT = 3;
	public static final int COMMAND_SEND_MESSAGE = 4;
	public static final int EVENT_MESSAGE_SENT = 5;
	public static final int EVENT_MESSAGE_CONFIRMED = 6;
	
	/* Sync with wixat plugin */
	public static final String PACKET_ID = "packet_id";
	public static final String CONFIRMATION_NAME = "confirmation";
	public static final String CONFIRMATION_NAMESPACE = "jabber:confirmation";
	public static final String TYPE = "type";
	public static final String TYPE_CLIENT = "client";
	public static final String TYPE_SERVER = "server";
	
	
	//private boolean connecting;
	private WixatService service;
	private XMPPConnection connection;
	private DataBaseHelper db;
	private HashMap<String,Chat> chatList;
	private String username;
	private String password;
	
	private Queue<Message> localQueue = new LinkedList<Message>();
	private MessageSender messageSender;
	int sleepTime = 5000;

        
	public XMPPManager(WixatService service, String username, String password) {
		this.service = service;
		db = service.getDataBaseHelper();
		
		
		chatList = new HashMap<String,Chat>();
		this.username = username;
		this.password = password;
	}

	/*public boolean reconnectionNeeded(){
		return !connecting & connection != null && !connection.isConnected();
	}*/
	
	public boolean isConnected(){
		return connection.isConnected();
	}
	
	/*
	 * to: user@host not only user
	 * return null if not sent
	 */
	public synchronized boolean send(Message m){
		boolean sent = false;
			try {
				String to = m.getTo();
				if(chatList.get(to) == null){
					ChatManager chatmanager = connection.getChatManager();
					Chat newChat = chatmanager.createChat(to, new CustomMessageListener());
					chatList.put(to, newChat);
				} 
				db.insertMessage(db.getConversationId(m.getTo()), m.getFrom(), m.getBody(), m.getPacketID());
				db.addToPendingQueue(m);
				localQueue.add(m);
				notify();
				sent = true;
				
				
			} catch (IllegalStateException e) {
				//Probably connection its not connected (broken pipe or similar) 
				e.printStackTrace();
			}
		
		return sent;
	}
	
	public void connect(){

		//Connection.DEBUG_ENABLED = true;

		 if(connection.isConnected()){
			 connection.disconnect();
		 }

		  while(!connection.isConnected()){
		  //connecting = true;
		    try {
				connection.connect();
				connection.login(username, password);
			    Presence presence = new Presence(Presence.Type.available);
			    presence.setStatus("Desde android");
			    connection.sendPacket(presence);
			    ChatManager chatmanager = connection.getChatManager();
			    chatmanager.addChatListener(new ChatManagerListenerImpl());
			    		    
			    //Log.d("CONEXION","Conectado");
			} catch (XMPPException e) {
				if(connection.isConnected()){
					connection.disconnect();
				}
				//Log.d("CONEXION","Error al conectarse");
				//Log.d("CONEXION","Reconectando en "+sleepTime+" milisegundos");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(sleepTime < 3600000 /*an hour as maximum time*/){
					sleepTime*=2;
				}
				e.printStackTrace();
			}
		  }
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

		
	}
	public void run() {

		//
		org.jivesoftware.smack.AndroidConnectionConfiguration config = new org.jivesoftware.smack.AndroidConnectionConfiguration(ServerConfiguration.HOSTNAME,ServerConfiguration.PORT, ServerConfiguration.RESOURCE);
		 
		config.setReconnectionAllowed(true);
        
		connection = new XMPPConnection(config);
		
		
		connect();
		//Init the sender thread
		messageSender = new MessageSender(connection, service);
		messageSender.start();
		
		for(Message m: db.getPendingQueue()){
			localQueue.add(m);
		}
		/* The thread should send messages to avoid freezes the service */
		synchronized(this){
			while(true){
				if(localQueue.size() > 0)
					messageSender.add(localQueue.poll());
				else
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
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
	        		if(p.getNamespace().equals(CONFIRMATION_NAMESPACE)){
	        			//Se ha recibido confirmacion
	        			DefaultPacketExtension dpe = (DefaultPacketExtension) p;
	        			if(dpe.getValue(TYPE).equals(TYPE_CLIENT)){
	        				
	        				//Log.d("CONFIRMATION","El otro usuario ha leido "+dpe.getValue(PACKET_ID));
	        				service.notifyMessageDeliver(dpe.getValue(PACKET_ID), from , XMPPManager.EVENT_MESSAGE_CONFIRMED);
	        			}else if(dpe.getValue(TYPE).equals(TYPE_SERVER)){
	        				//Log.d("CONFIRMATION","El server ha recibido "+dpe.getValue(PACKET_ID));
	        				//Notify the messageSender
	        				//Log.d("CONFIRMATION","Enviando al messageSender "+dpe.getValue(PACKET_ID));
	        				db.pollFromPendingQueue(dpe.getValue(PACKET_ID)); 
	        				//messageSender.checkConfirmation(dpe.getValue(PACKET_ID));
	        				messageSender.t.confirmation(dpe.getValue(PACKET_ID));
	        			}
	        			
	        		}
	        		
	        	}
        	}else{
        		//Log.d("INSERTANDO","NOficiaion");
        		db.insertMessage(conversationId, from, message.getBody(), message.getPacketID());
        		//ConfirmationIQ iq = new ConfirmationIQ(from, message.getPacketID());
        		//connection.sendPacket(iq);
        		//Send confirmation packet only if the reply to is specified
        		DefaultPacketExtension replyTo = (DefaultPacketExtension) message.getExtension(CONFIRMATION_NAMESPACE);
        		if(replyTo != null){
	        		org.jivesoftware.smack.packet.Message conf = new org.jivesoftware.smack.packet.Message();
	        		
	        		DefaultPacketExtension ext = new DefaultPacketExtension(CONFIRMATION_NAME, CONFIRMATION_NAMESPACE);
	        		ext.setValue(PACKET_ID, replyTo.getValue(PACKET_ID));
	        		ext.setValue(TYPE, TYPE_CLIENT);
	        		 
	        		
	        		conf.addExtension(ext);
	        		try {
	        			
						chat.sendMessage(conf);
						//Log.d("CONFIRMACION ENVIADA",conf.toXML());
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		//chatList.get(message.getf)
        		//TODO ENVIAR RESPUESTA DE RECIBIDO
        		//try{
        		if(!service.notifyNewMessageReceived(conversationId, message)){
        			//Activity is not visible, show notification
        			service.showNotification((int)conversationId);
        		}
        		/*}catch(Exception e){
        			e.printStackTrace();
        		}*/
        		
        	}
            
        }

    }
	
	public void removeChat(String jid){
		chatList.remove(jid);
		db.removeConversation(db.getConversationId(jid));
	}
	


}
