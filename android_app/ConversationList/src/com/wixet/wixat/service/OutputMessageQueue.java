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

public class OutputMessageQueue {

    private Queue<String> outMessageQueue = new LinkedList<String>();
    
    
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    public OutputMessageQueue(){

    	
    }
    
	public boolean add(String packetId){
		
    	Future<String> future = executor.submit(new Task());

        try {
            System.out.println("Started..");
            System.out.println(future.get(3, TimeUnit.SECONDS));
            System.out.println("Finished!");
            //Message sent successfully
        } catch (TimeoutException e) {
        	//Timeout, reconnect or whatever
            System.out.println("Timeout");
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        executor.shutdownNow();
        
		return outMessageQueue.add(packetId);
		//
		//outMessageQueue.add
        
	}
	
	class Task implements Callable<String> {
	    @Override
	    public String call() throws Exception {
	        Thread.sleep(10000); // Just to demo a long running task of 4 seconds.
	        return "Ready!";
	    }
	}
	
}
