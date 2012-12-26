package com.wixet.wixat.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wixet.wixat.R;
import com.wixet.wixat.database.ChatMessage;

public class ConversationArrayAdapter extends ArrayAdapter<HashMap<String, String>> {


    Context context;
    String me;
    ArrayList<HashMap<String, String>> data = null;
    //SparseArray<View> cache = null;
    
	public ConversationArrayAdapter(Context context, ArrayList<HashMap<String, String>> objects, String me) {
		super(context, 0, objects);
        this.context = context;
        this.data = objects;
        this.me = me;
        //cache =  new SparseArray<View>();
	}




		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			HashMap<String, String> song = new HashMap<String, String>();
	        song = data.get(position);
	        
	        //View vi = cache.get(position);
	        View vi;
	        //if(convertView==null){
	        	if(song.get(ChatMessage.COLUMN_NAME_AUTHOR).equals(me))
	        		vi = inflater.inflate(R.layout.conversation_row_left, null);
	        	else
	        		vi = inflater.inflate(R.layout.conversation_row_right, null);
	        //}
	        //TextView title = (TextView)vi.findViewById(R.id.name); 
	        TextView text = (TextView)vi.findViewById(R.id.text); 
	        TextView time = (TextView)vi.findViewById(R.id.time); 
	        
	        
	        
	        // Setting all values in listview
	        //title.setText(song.get(ChatMessage.COLUMN_NAME_AUTHOR));
	        text.setText(song.get(ChatMessage.COLUMN_NAME_BODY));
	        time.setText(song.get(ChatMessage.COLUMN_NAME_CREATED_AT));
	        return vi;
	    }
	
	
	
}
