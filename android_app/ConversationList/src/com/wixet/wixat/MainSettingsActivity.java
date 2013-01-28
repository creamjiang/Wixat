package com.wixet.wixat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class MainSettingsActivity extends Activity {

	
	CheckBox notifications;
	CheckBox enableSound;
	CheckBox enableVibration;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	public final static String SHOW_NOTIFICATION = "showNotification";
	public final static String ENABLE_SOUND = "enableSound";
	public final static String ENABLE_VIBRATION = "enableVibration";
	    	      
	    	      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_settings);
		
		settings = getSharedPreferences(ConversationList.CONFIGURATION, 0);
		editor = settings.edit();
		
		
		notifications = (CheckBox) findViewById(R.id.show_nofifications);
		enableSound = (CheckBox) findViewById(R.id.enable_sound);
		enableVibration = (CheckBox) findViewById(R.id.enable_vibration);
		
		notifications.setChecked(settings.getBoolean(SHOW_NOTIFICATION, true));
		enableSound.setChecked(settings.getBoolean(ENABLE_SOUND, true));
		enableVibration.setChecked(settings.getBoolean(ENABLE_VIBRATION, true));
		
		enableSound.setEnabled(settings.getBoolean(SHOW_NOTIFICATION, true));
		enableVibration.setEnabled(settings.getBoolean(SHOW_NOTIFICATION, true));
	
		
		
	}


	
	public void showNotificationClicked (View view){
		boolean checked = ((CheckBox)view).isChecked();
		enableSound.setEnabled(checked);
		enableVibration.setEnabled(checked);
		
		editor.putBoolean(SHOW_NOTIFICATION, checked);
		editor.commit();
			
		
	}
	public void enableSoundClicked (View view){
		editor.putBoolean(ENABLE_SOUND, ((CheckBox)view).isChecked());
		editor.commit();
		
	}
	public void enableVibrationClicked (View view){
		editor.putBoolean(ENABLE_VIBRATION, ((CheckBox)view).isChecked());
		editor.commit();
		
	}
}
