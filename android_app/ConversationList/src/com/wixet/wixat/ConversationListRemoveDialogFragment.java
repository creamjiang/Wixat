package com.wixet.wixat;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConversationListRemoveDialogFragment extends DialogFragment {
	
	private OnClickListener listener;
	public ConversationListRemoveDialogFragment(){
		super();
	}
	
	ConversationListRemoveDialogFragment(OnClickListener listener){
		super();
		this.listener = listener;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_conversation_remove)
               .setPositiveButton(R.string.accept, listener).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });;
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
