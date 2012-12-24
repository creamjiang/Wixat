package com.wixet.wixat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;



public class ConversationMenuDialogFragment extends DialogFragment {

	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    //builder.setTitle(R.string.pick_color);
	      builder.setItems(R.array.conversationMenu, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   if(which == 0){
	            		   //Go to view info
	            		   Intent i = new Intent(getActivity(), ContactInfoActivity.class);    
	       		        	getActivity().startActivity(i);  
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
