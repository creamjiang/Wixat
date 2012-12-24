package com.wixet.wixat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class FirstTimeCreateAccount extends FragmentActivity {

	
	EditText password;
	EditText passwordConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time_create_account);
		
		password = (EditText) findViewById(R.id.createPassword);
		passwordConfirm = (EditText) findViewById(R.id.createPasswordConfirm);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_first_time_create_account,
				menu);
		return true;
	}

	public void createPassword(View view){
		String p1 = (password.getText()+"").trim();
		String p2 = (passwordConfirm.getText()+"").trim();

		if(p1.equals(p2) && p1.length() > 0){
			SharedPreferences settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(ConversationList.PASSWORD, p1);
			editor.commit();
			
	        Intent inew;
	        inew = new Intent(getApplicationContext(),CreatingAccountLoadingScreenActivity.class);
	        inew.putExtra(ConversationList.TELEPHONE, settings.getString(ConversationList.TELEPHONE, null));
	        inew.putExtra(ConversationList.PASSWORD, p1);
			startActivity(inew);
			
		}else{
			//Error
      		DialogFragment nw = new FirstTimePasswordDialogFragment();
      		//selectedId = (String)view.getTag();
        	nw.show(getSupportFragmentManager(), "listDialogFragment");
		}
	}
}

