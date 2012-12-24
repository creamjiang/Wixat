package com.wixet.wixat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class FirstTimeAskActivity extends Activity {

	EditText numberTxt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time_ask);
		numberTxt = (EditText) findViewById(R.id.phoneTxt);
		
		String locale = getResources().getConfiguration().locale.getCountry();
		TextView t = (TextView)findViewById(R.id.phone_pefix);
		t.setText(locale);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_first_time_ask, menu);

		return true;
	}
	
	
	public void continueButton(View view){
		

		String phone = numberTxt.getText()+"";
		if(phone.length()>0){
			SharedPreferences settings = getApplicationContext().getSharedPreferences(ConversationList.CONFIGURATION, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(ConversationList.TELEPHONE, phone);
		    editor.commit();
			 Intent i=new Intent(this,LoadingScreenActivity.class);
			 i.putExtra(LoadingScreenActivity.TELEPHONE, phone);
			 startActivity(i);
		}
	}

}
