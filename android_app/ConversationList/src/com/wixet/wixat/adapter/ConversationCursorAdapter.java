package com.wixet.wixat.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wixet.wixat.R;
import com.wixet.wixat.database.ChatMessage;

public class ConversationCursorAdapter extends CursorAdapter{


    Context context;   
    ContentResolver contentResolver;
    LayoutInflater inflater;
    String me;
    
	public ConversationCursorAdapter(Context context, Cursor c, int flags, String me) {
		super(context, c, flags);
        this.context = context;
        inflater = ((Activity)context).getLayoutInflater();
        contentResolver = context.getContentResolver();
        this.me = me;
        
	}
	
	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup parent) {

        
        String author = cursor.getString(
				cursor.getColumnIndex(ChatMessage.COLUMN_NAME_AUTHOR));
        
        String body = cursor.getString(
				cursor.getColumnIndex(ChatMessage.COLUMN_NAME_BODY));
        
        String createdAt = cursor.getString(
				cursor.getColumnIndex(ChatMessage.COLUMN_NAME_CREATED_AT));

        View vi;

        	if(author.equals(me))
        		vi = inflater.inflate(R.layout.conversation_row_left, null);
        	else
        		vi = inflater.inflate(R.layout.conversation_row_right, null);
 
        TextView text = (TextView)vi.findViewById(R.id.text); 
        TextView time = (TextView)vi.findViewById(R.id.time); 
        
        
        
        text.setText(body);
        time.setText(createdAt);
        return vi;
        
       
	}
	
	
}
