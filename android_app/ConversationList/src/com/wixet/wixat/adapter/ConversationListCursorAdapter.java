package com.wixet.wixat.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wixet.wixat.R;
import com.wixet.wixat.database.Chat;

public class ConversationListCursorAdapter extends CursorAdapter{


    Context context;   
    ContentResolver contentResolver;
    LayoutInflater inflater;
    
	public ConversationListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
        this.context = context;
        inflater = ((Activity)context).getLayoutInflater();
        contentResolver = context.getContentResolver();
        
	}
	
	@Override
	public void bindView(View vi, Context arg1, Cursor cursor) {

		
		String newMessages = cursor.getString(
				cursor.getColumnIndex(Chat.COLUMN_NAME_NEW_MESSAGES));

		
		TextView title = (TextView)vi.findViewById(R.id.title);
        TextView text = (TextView)vi.findViewById(R.id.text); 
        ImageView img = (ImageView)vi.findViewById(R.id.imageNewMessage);
        if(newMessages.equals("1")){ 
	        text.setVisibility(View.VISIBLE);
	        img.setVisibility(View.VISIBLE);
	        title.setTypeface(null, Typeface.BOLD);
        }else{
        	text.setVisibility(View.INVISIBLE);
	        img.setVisibility(View.INVISIBLE);
	        title.setTypeface(null, Typeface.NORMAL);
        }
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = inflater.inflate(R.layout.conversation_list_row_new, parent, false);

		String participant = cursor.getString(
				cursor.getColumnIndex(Chat.COLUMN_NAME_PARTICIPANT));
		
		String newMessages = cursor.getString(
				cursor.getColumnIndex(Chat.COLUMN_NAME_NEW_MESSAGES));
		
		String id  = cursor.getString(
				cursor.getColumnIndex(Chat._ID));
		
		TextView title = (TextView)vi.findViewById(R.id.title);
        TextView text = (TextView)vi.findViewById(R.id.text); 
        ImageView img = (ImageView)vi.findViewById(R.id.imageNewMessage);
        if(newMessages.equals("1")){ 
	        text.setVisibility(View.VISIBLE);
	        img.setVisibility(View.VISIBLE);
	        title.setTypeface(null, Typeface.BOLD);
        }else{
        	text.setVisibility(View.INVISIBLE);
	        img.setVisibility(View.INVISIBLE);
	        title.setTypeface(null, Typeface.NORMAL);
        }
        
        String conversationName = participant.split("@")[0];
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( conversationName));
        

        // Load contact name and thumbnail
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_ID }, null, null, null);
        
        
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                conversationName = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                
                int photoId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID));
                if (photoId > 0) {
                	ImageView im = (ImageView)vi.findViewById(R.id.list_image); // title
                	im.setImageBitmap(com.wixet.utils.Contacts.fetchThumbnail(photoId,contentResolver));
                }
                
                
                
                contactLookup.close();
            }

            
        title.setText(conversationName);
        vi.setTag(id);
		return vi;
	}
	
	
}
