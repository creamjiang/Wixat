package com.wixet.wixat.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wixet.wixat.R;
import com.wixet.wixat.database.Chat;

public class ConversationListArrayAdapter extends ArrayAdapter<HashMap<String, String>> {


    Context context;
    int layoutResourceId;   
    ContentResolver contentResolver;
    ArrayList<HashMap<String, String>> data = null;
    HashMap<String,HashMap<String, String>> conversationIndex = new HashMap<String,HashMap<String, String>>();
    
	public ConversationListArrayAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> objects) {
		super(context, textViewResourceId, objects);
        this.layoutResourceId = textViewResourceId;
        this.context = context;
        this.data = objects;
        contentResolver = context.getContentResolver();
        
	}


	public void updateList(ArrayList<HashMap<String, String>> objects){
		this.data = objects;
	}

	/*public static String CONVERSATION_ID = "id";
	public static String CONVERSATION_PHOTO = "photo";
	public static String PARTICIPANT = "participant";
	public static String NEW_MESSAGES = "new_messages";*/


		
		public View getView(int position, View convertView, ViewGroup parent) {
			
				HashMap<String, String> conversation;
	        	conversation = data.get(position);
				View vi;
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
				vi = inflater.inflate(R.layout.conversation_list_row_new, parent, false);
		        //if(convertView==null){
		        	
		        	/*if(conversation.get(Chat.COLUMN_NAME_NEW_MESSAGES).equals("0")){
		        		vi = inflater.inflate(R.layout.conversation_list_row, parent, false);
		        	}else{
		        		vi = inflater.inflate(R.layout.conversation_list_row_new, parent, false);
		        	}*/
		        /*}else{
		        	vi=convertView;
		        }*/
		        
		        TextView title = (TextView)vi.findViewById(R.id.title);
		        TextView text = (TextView)vi.findViewById(R.id.text); 
		        ImageView img = (ImageView)vi.findViewById(R.id.imageNewMessage);
		        if(conversation.get(Chat.COLUMN_NAME_NEW_MESSAGES).equals("1")){ 
			         
			        text.setVisibility(View.VISIBLE);
			        img.setVisibility(View.VISIBLE);
			        title.setTypeface(null, Typeface.BOLD);
		        }else{
		        	text.setVisibility(View.INVISIBLE);
			        img.setVisibility(View.INVISIBLE);
			        title.setTypeface(null, Typeface.NORMAL);
		        }
		        
		        
		        // Setting all values in listview
		        String name = conversation.get(Chat.COLUMN_NAME_PARTICIPANT).split("@")[0];
		        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( name));
		        

		        // Load contact name and thumbnail
		        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
		                ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_ID }, null, null, null);
		        
		        
		            if (contactLookup != null && contactLookup.getCount() > 0) {
		                contactLookup.moveToNext();
		                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
		                
		                int photoId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID));
		                if (photoId > 0) {
		                	ImageView im = (ImageView)vi.findViewById(R.id.list_image); // title
		                	im.setImageBitmap(com.wixet.utils.Contacts.fetchThumbnail(photoId,contentResolver));
		                }
		                
		                
		                
		                contactLookup.close();
		            }

		            
		        title.setText(name);

		        
		        vi.setTag(R.id.CONVERSATION_ID,conversation.get(Chat._ID));
		        conversationIndex.put(conversation.get(Chat.COLUMN_NAME_PARTICIPANT), conversation);
		        return vi;

	    }
		

		public HashMap<String, String> getConversation(String participant){
			return conversationIndex.get(participant);
		}
	
	
	
}
