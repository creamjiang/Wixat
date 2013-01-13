package com.wixet.wixat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.markupartist.android.widget.ActionBar;

public class NodeManagerActivity extends FragmentActivity {

	private ActionBar actionBar;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_manager);
        
        
        actionBar = (ActionBar) findViewById(R.id.actionbarNode);
        //actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_demo));
        actionBar.setTitle(R.string.node_list);

        //final Action menuAction = new DialogAction(this, showMenu(), R.drawable.ic_title_menu_default);
        //actionBar.addAction(menuAction);
        
        
        
        ListView lv = (ListView)findViewById(R.id.nodelist);
        
     // create the grid item mapping
        String[] from = new String[] {"rowid", "col_1"};
        int[] to = new int[] { R.id.simpleitemtext1, R.id.simpleitemtext2};

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < 1; i++){
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put("rowid", "Wixet");
        	map.put("col_1", "xmpp.wixet.com");
        	fillMaps.add(map);
        }

        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.simpleitem, from, to);
        lv.setAdapter(adapter);

    }

/*

    private DialogFragment showMenu() {
  		DialogFragment nw = new NodeListMenuDialogFragment();
  		return nw;
    	//nw.show(getSupportFragmentManager(), "listDialogFragment");
    }*/

}
