package com.wixet.wixat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class NodeListMenuDialogFragment extends DialogFragment {

	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    //builder.setTitle(R.string.pick_color);
	      builder.setItems(R.array.nodeMenu, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   if(which == 0){
	            		   Intent i = new Intent(getActivity(), NewNodeActivity.class);    
	       		        	getActivity().startActivity(i);  
	            	   }
	           }
	    });
	    return builder.create();
	}
	


}
