package com.wixet.wixat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;


public class NewNodeActivity extends FragmentActivity {
	public final static String NODE_HOSTNAME = "com.wixet.wixat.NODE_HOSTNAME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_node);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_node, menu);
        return true;
    }
    
    private void showError(){
    	DialogFragment nw = new NodeNotFoundDialogFragment();
    	nw.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }
    
    private boolean nodeExists(){
    	return false;
    }
    
    private void nextActivity(){
    	Intent i = new Intent(this, NodeLoginActivity.class);
    	EditText e = (EditText) findViewById(R.id.node_hostname);
    	i.putExtra(NODE_HOSTNAME, e.getText().toString());
    	startActivity(i);
    }
    
    public void checkNode(View view){
    	final ProgressDialog pd = ProgressDialog.show(this,
    			"Cargando",
    			"mensaje",
    			true, false);
    	
    			new Thread(new Runnable(){
    			public void run(){
    				boolean error = nodeExists();
    				pd.dismiss();
    				if(error)
    					showError();
    				else
    					nextActivity();
    			}
    			}).start();
 	
    	
    }
}
