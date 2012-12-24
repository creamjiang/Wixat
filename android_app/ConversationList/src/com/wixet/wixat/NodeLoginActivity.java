package com.wixet.wixat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public class NodeLoginActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_login);
        TextView tx = (TextView) findViewById(R.id.hostname);
        Intent i = getIntent();
        tx.setText(i.getStringExtra(NewNodeActivity.NODE_HOSTNAME));
    }


    private boolean validUser(){
    	return true;
    }
    
    
    private void showError(){
    	DialogFragment nw = new InvalidUserDialogFragment();
    	nw.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }
    
    private void nextActivity(){
    	Intent i = new Intent(this, NodeLoginActivity.class);
    	//EditText e = (EditText) findViewById(R.id.node_hostname);
    	//i.putExtra(NODE_HOSTNAME, e.getText().toString());
    	startActivity(i);
    }
    public void connectToNode(View view){
    	final ProgressDialog pd = ProgressDialog.show(this,
    			"Cargando",
    			"mensaje",
    			true, false);
    			new Thread(new Runnable(){
    			public void run(){
    				boolean error = validUser();
    				pd.dismiss();
    				if(error)
    					showError();
    				else
    					nextActivity();
    			}
    			}).start();
    }


}
