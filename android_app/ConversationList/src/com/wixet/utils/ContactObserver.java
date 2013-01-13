package com.wixet.utils;

import android.database.ContentObserver;
import com.wixet.wixat.ConversationList;


public class ContactObserver extends ContentObserver {

	ConversationList context;
	public ContactObserver() {
        super(null);
    }
	
	
    public ContactObserver(ConversationList c) {
        super(null);
    	context = c;
    }



	@Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
       // Log.d("CAMBIO","Algo de un contacto ha cambiado");
//        context.updatedDataSet();
    }


}
