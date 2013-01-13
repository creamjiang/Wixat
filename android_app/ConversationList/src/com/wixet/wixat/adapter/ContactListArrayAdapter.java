package com.wixet.wixat.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wixet.wixat.LoadingScreenActivity;
import com.wixet.wixat.R;

public class ContactListArrayAdapter extends ArrayAdapter<HashMap<String, String>> {


    Context context;
    int layoutResourceId;
    ContentResolver contentResolver;
    ArrayList<HashMap<String, String>> data = null;
    
	public ContactListArrayAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> objects) {
		super(context, textViewResourceId, objects);
        this.layoutResourceId = textViewResourceId;
        this.context = context;
        this.data = objects;
        contentResolver = context.getContentResolver();
        
	}


	public void updateList(ArrayList<HashMap<String, String>> objects){
		this.data = objects;
	}


		
		public View getView(int position, View convertView, ViewGroup parent) {
			
				HashMap<String, String> telefono = new HashMap<String, String>();
	        	telefono = data.get(position);
				View vi;
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
				vi = inflater.inflate(layoutResourceId, parent, false);

		        
		        TextView title = (TextView)vi.findViewById(R.id.title);
		         

		        
		        
		        // Setting all values in listview
		        String name = telefono.get(LoadingScreenActivity.TELEPHONE);
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


		        return vi;

	    }

		
	
	
	
}
