package com.wixet.wixat.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wixet.wixat.R;
import com.wixet.wixat.database.ChatMessage;

public class ConversationArrayAdapter extends ArrayAdapter<HashMap<String, String>> {


    Context context;
    String me;
    ArrayList<HashMap<String, String>> data = null;
    HashMap<String, View> messageIndex = new HashMap<String, View>();
    LayoutInflater inflater;
    //SparseArray<View> cache = null;
    
	public ConversationArrayAdapter(Context context, ArrayList<HashMap<String, String>> objects, String me) {
		super(context, 0, objects);
        this.context = context;
        this.data = objects;
        this.me = me;
        inflater = ((Activity)context).getLayoutInflater();
        //cache =  new SparseArray<View>();
	}




		
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<String, String> message;
			message = data.get(position);
			View vi = messageIndex.get(message.get(ChatMessage._ID));
			if(vi == null){
		        //View vi = cache.get(position);
		        //if(convertView==null){
		        	if(message.get(ChatMessage.COLUMN_NAME_AUTHOR).equals(me))
		        		vi = inflater.inflate(R.layout.conversation_row_right, null);
		        	else{
		        		vi = inflater.inflate(R.layout.conversation_row_left, null);
		        		 
		        		ImageView state = (ImageView)vi.findViewById(R.id.message_state);
		        		if(message.get(ChatMessage.COLUMN_NAME_CONFIRMED) != null){
		        			state.setImageResource(R.drawable.glyphicons_152_check);
		        		}else if (message.get(ChatMessage.COLUMN_NAME_SENT) != null){
		        			state.setImageResource(R.drawable.glyphicons_206_ok_2);
		        		}
		        	}
		        //}
		        //TextView title = (TextView)vi.findViewById(R.id.name); 
		        TextView text = (TextView)vi.findViewById(R.id.text); 
		        TextView time = (TextView)vi.findViewById(R.id.time); 
		        
		        
		        
		        // Setting all values in listview
		        //title.setText(song.get(ChatMessage.COLUMN_NAME_AUTHOR));
		        text.setText(message.get(ChatMessage.COLUMN_NAME_BODY));
		        time.setText(message.get(ChatMessage.COLUMN_NAME_CREATED_AT));
		        messageIndex.put(message.get(ChatMessage._ID), vi);
			}
	        
	        
	        return vi;
	    }

		
		public void setAsSent(String obj) {
			View vi = messageIndex.get(obj);
			if(vi != null){
				
				ImageView state = (ImageView)vi.findViewById(R.id.message_state);
				state.setImageResource(R.drawable.glyphicons_206_ok_2);
				
				
			}
			
		}
		
		public void setAsConfirmed(String obj) {
			View vi = messageIndex.get(obj);
			if(vi != null){
				
				ImageView state = (ImageView)vi.findViewById(R.id.message_state);
				state.setImageResource(R.drawable.glyphicons_152_check);
				
				
			}
			
		}
	
	
	
}
