package com.wixet.wixat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ConversationSettingsActivity extends Activity {

	
	CheckBox notifications;
	CheckBox enableSound;
	CheckBox enableVibration;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	String conversationId = "";
	
	    	      
	    	      
	public static final String CONVERSATION ="conversation";
	public static final String CONVERSATION_NAME ="name";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation_settings);
		
		conversationId = getIntent().getExtras().getString(CONVERSATION);
		String conversationName = getIntent().getExtras().getString(CONVERSATION_NAME);
		
		settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
		editor = settings.edit();
		
		
		notifications = (CheckBox) findViewById(R.id.show_nofifications);
		enableSound = (CheckBox) findViewById(R.id.enable_sound);
		enableVibration = (CheckBox) findViewById(R.id.enable_vibration);
		TextView txtName = (TextView) findViewById(R.id.conversation_name);
		txtName.setText(conversationName);
		
		notifications.setChecked(settings.getBoolean(MainSettingsActivity.SHOW_NOTIFICATION+":"+conversationId, true));
		enableSound.setChecked(settings.getBoolean(MainSettingsActivity.ENABLE_SOUND+":"+conversationId, true));
		enableVibration.setChecked(settings.getBoolean(MainSettingsActivity.ENABLE_VIBRATION+":"+conversationId, true));
		
		enableSound.setEnabled(settings.getBoolean(MainSettingsActivity.SHOW_NOTIFICATION+":"+conversationId, true));
		enableVibration.setEnabled(settings.getBoolean(MainSettingsActivity.SHOW_NOTIFICATION+":"+conversationId, true));
	
		
		
	}


	
	public void showNotificationClicked (View view){
		boolean checked = ((CheckBox)view).isChecked();
		enableSound.setEnabled(checked);
		enableVibration.setEnabled(checked);
		
		editor.putBoolean(MainSettingsActivity.SHOW_NOTIFICATION+":"+conversationId, checked);
		editor.commit();
			
		
	}
	public void enableSoundClicked (View view){
		editor.putBoolean(MainSettingsActivity.ENABLE_SOUND+":"+conversationId, ((CheckBox)view).isChecked());
		editor.commit();
		
	}
	public void enableVibrationClicked (View view){
		editor.putBoolean(MainSettingsActivity.ENABLE_VIBRATION+":"+conversationId, ((CheckBox)view).isChecked());
		editor.commit();
		
	}
}