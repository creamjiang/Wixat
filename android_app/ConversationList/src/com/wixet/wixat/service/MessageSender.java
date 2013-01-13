package com.wixet.wixat.service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

public class MessageSender extends Thread{
private ExecutorService executor = Executors.newFixedThreadPool(1);
	
	public Task t;
	private Queue<Message> cola = new LinkedList<Message>();
	private boolean startUp = true;
	private WixatService service;
	
	private  Connection connection;
	public MessageSender( Connection connection, WixatService service){
		this.connection = connection;
		this.service = service;
	}
	
	public synchronized void despierta(){
		notify();
		
	}
	public void run() {

		synchronized(this){
			while (startUp)
			{
			   if (cola.size() == 0){
					try {
						System.out.println("Esperando a se despertado");
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			   	}else{
			   		//System.out.println(cola.poll());
			   		
			   		t = new Task();
			   		t.message =cola.peek();
			   		Future<String> future = executor.submit(t);

			        try {
			            //Log.d("QUEUE","Sending message...");
			            //Enviar
			            String to = t.message.getTo();
			            //String body = t.message.getBody();
			            
			            //Add the reply to
			            DefaultPacketExtension replyTo = new DefaultPacketExtension(XMPPManager.CONFIRMATION_NAME, XMPPManager.CONFIRMATION_NAMESPACE);
			            replyTo.setValue(XMPPManager.PACKET_ID, t.message.getPacketID());
			            t.message.addExtension(replyTo);
			            connection.sendPacket(t.message);
			            //chatList.get(to).sendMessage(t.message);
			            //
			            //Log.d("QUEUE","Waiting response (5 sec timeout)...");
			            System.out.println(future.get(5, TimeUnit.SECONDS));
			            //Log.d("QUEUE","Confirmation received!");
			            cola.poll();//Remove from queue
			            service.notifyMessageDeliver(t.message.getPacketID(), to, XMPPManager.EVENT_MESSAGE_SENT);
			        } catch (TimeoutException e) {
			        	//Log.d("QUEUE","TIMEOUT, maybe reconnect or something");
			        	//Add the message to the queue (to resend)
			        	service.getXMPPManager().connect();
			        	//Log.d("QUEUE","End connect");
			        	//cola.add(t.message);
			            future.cancel(true);
			            //e.printStackTrace();
			        } catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
			        
			   		
			   	}
			}
			executor.shutdownNow();
		}
		    

    }
	
	public synchronized void startShutdown(){
		startUp = false;
	}
	
	public synchronized  void checkConfirmation(String m){
		//Log.d("QUEUE","SE VA A Comprobar "+m);
		this.t.confirmation(m);
	}
	
	public synchronized void add(Message m){
		cola.add(m);
		this.notify();

	}
	
	public class Task implements Callable<String> {
		public Message message;
		public synchronized void confirmation(String confirmationPacketId){
			notify();
			//Log.d("QUEUE","Checking confirmation for "+confirmationPacketId);

		}
	    @Override 
	    public synchronized String call() throws Exception {
	    	
	    	//Log.d("QUEUE","Waiting for confirmation (task)");
	    	wait();
	    	//Log.d("QUEUE","Confirmation received succesfully");
	        return "Ready!";
	    }
	}

}
