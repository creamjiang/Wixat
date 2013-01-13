package com.wixet.wixat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;



public class ConversationMenuDialogFragment extends DialogFragment {

	String phone;
	ContentResolver c;

	public void setPhone(String phone){
		this.phone = phone;
	}
	
	public void setContentResolver(ContentResolver c){
		this.c = c;;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    //builder.setTitle(R.string.pick_color);
	      builder.setItems(R.array.conversationMenu, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   
	            	   
	            	   if(which == 0){
	            		   //Go to view info
	            		   //Intent i = new Intent(getActivity(), ContactInfoActivity.class);    
	       		        	//getActivity().startActivity(i);
	            		   Intent call = new Intent(Intent.ACTION_CALL);
	            		   
	            		   call.setData(Uri.parse("tel:" + phone ));
	            		   startActivity(call);
	            		   /*Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode( phone));
	            		   
	            		   
	            		   Cursor contactLookup = c.query(uri, new String[] {BaseColumns._ID,
	            				   PhoneLookup._ID}, null, null, null);
	            		   
	            		   String id ="";
	            		   if (contactLookup != null && contactLookup.getCount() > 0) {
	                           contactLookup.moveToNext();
	                           id = contactLookup.getString(contactLookup.getColumnIndex(PhoneLookup._ID));
	
	                           
	                           
	                           
	                           contactLookup.close();
	                       }
	            		   
	            		   Log.d("ID",id);
	            		   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people/"+id));
	            		    //nativeInfo = tabHost.newTabSpec("native info").setIndicator("N Info").setContent(intent);
	            		   startActivity(intent);*/
	            	   }else if(which == 1){
	            		   //sendGoToNodesMessage();
	            		   
	            		   //DialogFragment nw = new ConversationOptionsDialogFragment();
	                   		//nw.show(getSupportFragmentManager(), "NoticeDialogFragment");
	            	   }
	           }
	    });
	    return builder.create();
	}
	

	// Send an Intent with an action named "custom-event-name". The Intent sent should 
	// be received by the ReceiverActivity.
	/*private void sendGoToNodesMessage() {
	  Intent intent = new Intent(ConversationList.CONVERSATION_MENU);
	  // You can also include some extra data.
	  
	  //Cambiar ask por lo que se
	  //intent.putExtra(ConversationList.MESSAGE, ConversationList.NODES);
	  LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
	}*/
}
