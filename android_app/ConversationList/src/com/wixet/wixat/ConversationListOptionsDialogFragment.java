package com.wixet.wixat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConversationListOptionsDialogFragment extends DialogFragment {

	private OnClickListener listener;
	public ConversationListOptionsDialogFragment(){
		super();
	}
	
	public ConversationListOptionsDialogFragment(OnClickListener listener){
		super();
		this.listener = listener;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    //builder.setTitle(R.string.pick_color);
	      builder.setItems(R.array.conversationListOptions, listener);
	    return builder.create();
	}
	
}
