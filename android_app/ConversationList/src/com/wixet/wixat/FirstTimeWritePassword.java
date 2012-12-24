package com.wixet.wixat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class FirstTimeWritePassword extends Activity {

	private String phone;
	private EditText passwordTxt;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time_write_password);
		phone = getIntent().getExtras().getString(LoadingScreenActivity.TELEPHONE);
		passwordTxt = (EditText) findViewById(R.id.loginPasswordTxt);
		
	}
	
	
	public void checkPassword(View view){
		Intent i=new Intent(this,FirstTimeWritePasswordLoadingActivity.class);
		 i.putExtra(FirstTimeWritePasswordLoadingActivity.TELEPHONE, phone);
		 String pass = passwordTxt.getText()+"";
		 //Log.d("PASSWORDINI",pass);
		 i.putExtra(FirstTimeWritePasswordLoadingActivity.PASSWORD, pass);
		 startActivityForResult(i, 1);
		
		//@+id/loginPasswordTxt
	}

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (requestCode == 1) {
    	     if(resultCode == RESULT_OK){
    	    	 	SharedPreferences settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(ConversationList.CONFIGURED, true);
					editor.putString(ConversationList.PASSWORD, passwordTxt.getText()+"");
					editor.commit();
					
			        Intent inew;
			        inew = new Intent(getApplicationContext(),ConversationList.class);
			        inew.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(inew);
					finish();
    	     }else{
    	    	 passwordTxt.setText("");
    	     }

    	}
    	
    }


}
