package com.wixet.wixat.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.jivesoftware.smack.packet.Message;

import com.wixet.wixat.service.XMPPManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "WixatMessenger.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String VARCHAR_TYPE = " VARCHAR(50)";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String BOOLEAN_TYPE = " BOOLEAN";
    private static final String COMMA_SEP = ",";
    private static final String UNIQUE = " UNIQUE";
    private SQLiteDatabase database;

    private final String PENDING_QUEUE_QUERY = "SELECT a."+PendingQueue._ID+" as "+PendingQueue._ID+", b."+ChatMessage.COLUMN_NAME_AUTHOR+" as "+ChatMessage.COLUMN_NAME_AUTHOR+", c."+Chat.COLUMN_NAME_PARTICIPANT+" as "+Chat.COLUMN_NAME_PARTICIPANT+", b."+ChatMessage.COLUMN_NAME_BODY+" as "+ChatMessage.COLUMN_NAME_BODY+" FROM "+PendingQueue.TABLE_NAME+" a INNER JOIN "+ChatMessage.TABLE_NAME+" b ON a."+PendingQueue._ID+"=b."+ChatMessage._ID+" INNER JOIN "+Chat.TABLE_NAME+" c ON b."+ChatMessage.COLUMN_NAME_CHAT_ID+"= c."+Chat._ID;
    
    public static final String SQL_CREATE_NODE =     
    		"CREATE TABLE " + Node.TABLE_NAME + " (" +
    		Node._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
    		Node.COLUMN_NAME_HOST + VARCHAR_TYPE + UNIQUE + COMMA_SEP +
    		Node.COLUMN_NAME_USERNAME + VARCHAR_TYPE + COMMA_SEP +
    	    Node.COLUMN_NAME_PASSWORD + VARCHAR_TYPE +
    	    " )";
    
    public static final String SQL_CREATE_CHAT =     
    		"CREATE TABLE " + Chat.TABLE_NAME + " (" +
    		Chat._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
    		Chat.COLUMN_NAME_PARTICIPANT + VARCHAR_TYPE + UNIQUE + COMMA_SEP +
    		Chat.COLUMN_NAME_NEW_MESSAGES + BOOLEAN_TYPE +
    		//Chat.COLUMN_NAME_NODE_ID + INTEGER_TYPE + COMMA_SEP +
    	    //" FOREIGN KEY ("+ Chat.COLUMN_NAME_NODE_ID +") REFERENCES "+Node.TABLE_NAME+" ("+Node._ID+")" +
    	    " )";
    
    public static final String SQL_PENDING_QUEUE_CHAT =     
    		"CREATE TABLE " + PendingQueue.TABLE_NAME + " (" +
    		PendingQueue._ID + TEXT_TYPE + " PRIMARY KEY " + COMMA_SEP +
    	    " FOREIGN KEY ("+ PendingQueue._ID +") REFERENCES "+ChatMessage.TABLE_NAME+" ("+ChatMessage._ID+") ON DELETE CASCADE" +
    	    " )";
    
    public static final String SQL_CREATE_CHAT_MESSAGE =     
    		"CREATE TABLE " + ChatMessage.TABLE_NAME + " (" +
    		//ChatMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
    		ChatMessage._ID + TEXT_TYPE + " PRIMARY KEY " + COMMA_SEP +
    		ChatMessage.COLUMN_NAME_AUTHOR + VARCHAR_TYPE + COMMA_SEP +
    		ChatMessage.COLUMN_NAME_CHAT_ID + INTEGER_TYPE + COMMA_SEP +
    		ChatMessage.COLUMN_NAME_BODY + TEXT_TYPE + COMMA_SEP +
    		ChatMessage.COLUMN_NAME_SENT + BOOLEAN_TYPE + COMMA_SEP +
    		ChatMessage.COLUMN_NAME_CONFIRMED + BOOLEAN_TYPE + COMMA_SEP +
    		ChatMessage.COLUMN_NAME_CREATED_AT + DATETIME_TYPE + COMMA_SEP +
    	    " FOREIGN KEY ("+ ChatMessage.COLUMN_NAME_CHAT_ID +") REFERENCES "+ Chat.TABLE_NAME+" ("+Chat._ID+")" +
    	    " )";
    
    
    
    
    
    public static final String SQL_CHAT_MESSAGE_INDEX = "CREATE INDEX chat_message_date_idx ON "+ ChatMessage.TABLE_NAME+"("+ ChatMessage._ID +")";
    
    /*public static final String SQL_CREATE_CONTACT =     
    		"CREATE TABLE " + Contact.TABLE_NAME + " (" +
    		Contact._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
    		Contact.COLUMN_NAME_PHONE + VARCHAR_TYPE + COMMA_SEP +
    		Contact.COLUMN_NAME_NAME + VARCHAR_TYPE +
    	    " )";
    */
    public static final String SQL_INSERT_NODE =     
    		"INSERT INTO "+Node.TABLE_NAME+" ("+Node.COLUMN_NAME_HOST+") VALUES('46.137.8.44')";
    
    
    public static final String SQL_CREATE_COMMENT = "";
    public static final String SQL_DELETE_NODE = "";
    public static final String SQL_DELETE_COMMENT = "";
    public static final String SQL_DELETE_CHAT = "";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NODE);
        db.execSQL(SQL_CREATE_CHAT);
        db.execSQL(SQL_CREATE_CHAT_MESSAGE);
        db.execSQL(SQL_INSERT_NODE);
        db.execSQL(SQL_PENDING_QUEUE_CHAT);
        

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
    	if(oldVersion == 1 || oldVersion == 2 || oldVersion == 3){
    		db.execSQL("DROP TABLE "+ChatMessage.TABLE_NAME+";");
    		db.execSQL(SQL_CREATE_CHAT_MESSAGE);
    		db.execSQL(SQL_PENDING_QUEUE_CHAT);
    	}
    	//Log.d("UPGRADE","DATABASE");
        //onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    
    public void insertNode(String host, String username, String password){
    	// Create a new map of values, where column names are the keys
    	ContentValues values = new ContentValues();
    	values.put(Node.COLUMN_NAME_HOST, host);
    	values.put(Node.COLUMN_NAME_USERNAME, username);
    	values.put(Node.COLUMN_NAME_PASSWORD, password);
    	database.insert( Node.TABLE_NAME, null, values);
    }
    

    public boolean existsChat(String participant){

    	/* Se podría utilizar count(*) de sql pero como sólo hay un resultado o 0 pues así es más sencillo */
    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    Chat._ID,
    	    Chat.COLUMN_NAME_PARTICIPANT
    	    };
    	
    	String selection = Chat.COLUMN_NAME_PARTICIPANT + " =  ?";
    	String[] selectionArgs = { participant };
    	
    	Cursor c = database.query(
    		    Chat.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    selection,                                // The columns for the WHERE clause
    		    selectionArgs,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	boolean exists = c.getCount() > 0;
    	c.close();

    	return exists;
    	
    }
    
    public void insertMessage(int conversationId, String author, String text, String packetId ){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
    	Date date = new Date();
    	ContentValues values = new ContentValues();
    	values.put(ChatMessage.COLUMN_NAME_CHAT_ID, conversationId+"");
    	values.put(ChatMessage.COLUMN_NAME_AUTHOR, author);
    	values.put(ChatMessage.COLUMN_NAME_BODY, text);
    	values.put(ChatMessage.COLUMN_NAME_CREATED_AT, dateFormat.format(date));
    	values.put(ChatMessage._ID, packetId);
    	database.insert( ChatMessage.TABLE_NAME, null, values);
    	
    }
    
    
    public void removeConversation(int id ){

    	database.delete(ChatMessage.TABLE_NAME, ChatMessage.COLUMN_NAME_CHAT_ID + "= ?", new String[] { id+"" } );
    	database.delete(Chat.TABLE_NAME, Chat._ID + "= ?", new String[] { id+"" } );
    }
    
    public long insertChat(String participant, boolean notify){
    	//Get the id of the node
    	
    	// Insert conversation
    	ContentValues values = new ContentValues();
    	//values.put(Chat.COLUMN_NAME_NODE_ID, getNodeId(node));
    	values.put(Chat.COLUMN_NAME_PARTICIPANT, participant);
    	values.put(Chat.COLUMN_NAME_NEW_MESSAGES, notify?"1":"0");

    	return database.insert( Chat.TABLE_NAME, null, values);
    }
    
    public String getParticipant(int conversationId){
    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    Chat.COLUMN_NAME_PARTICIPANT
    	    };

    	String selection = Chat._ID + " =  ?";
    	String[] selectionArgs = { conversationId+"" };
    	
    	Cursor c = database.query(
    		    Chat.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    selection,                                // The columns for the WHERE clause
    		    selectionArgs,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	c.moveToFirst();
    	
    	String participant = c.getString(
    	    c.getColumnIndexOrThrow(Chat.COLUMN_NAME_PARTICIPANT)
    	);
    	c.close();
    	return participant;
    }
    
    public long getNodeId(String node){
    	// Define a projection that specifies which columns from the database
    	// you will actually use after this query.
    	String[] projection = {
    	    Node._ID,
    	    Node.COLUMN_NAME_HOST
    	    };
    	
    	String selection = Node.COLUMN_NAME_HOST + " =  ?";
    	String[] selectionArgs = { node };
    	
    	Cursor c = database.query(
    		    Node.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    selection,                                // The columns for the WHERE clause
    		    selectionArgs,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	c.moveToFirst();
    	
    	long nodeId = c.getLong(
    	    c.getColumnIndexOrThrow(Node._ID)
    	);
    	c.close();
    	return nodeId;
    }
    
    
    public HashMap<String,String> getConversation(String conversationId){
    	HashMap<String,String> data = new HashMap<String,String>();
    	String[] projection = {
    	    Chat._ID,
    	    Chat.COLUMN_NAME_PARTICIPANT,
    	    Chat.COLUMN_NAME_NEW_MESSAGES,
    	    };
    	
    	Cursor c = database.query(
    		    Chat.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    Chat._ID+" = ?",                                // The columns for the WHERE clause
    		    new String[] {conversationId},                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	c.moveToFirst();
    	
    	if (c.isAfterLast() == false) 
    	{
    		data.put(Chat._ID,c.getString(c.getColumnIndexOrThrow(Chat._ID)));
    		data.put(Chat.COLUMN_NAME_PARTICIPANT,c.getString(c.getColumnIndexOrThrow(Chat.COLUMN_NAME_PARTICIPANT)));
    		data.put(Chat.COLUMN_NAME_NEW_MESSAGES,c.getString(c.getColumnIndexOrThrow(Chat.COLUMN_NAME_NEW_MESSAGES)));
    	}
    	c.close();
    	return data;
    }
    public int getConversationId(String participant){
    	/*Returns string to make easier build queries. May change in future*/
		// Get the entries
    	//TODO hacer que vaya para node
        String conversationId = null;
    	String[] projection = {
    	    Chat._ID,
    	    };
    	
    	Cursor c = database.query(
    		    Chat.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    Chat.COLUMN_NAME_PARTICIPANT+" = ?",                                // The columns for the WHERE clause
    		    new String[] {participant},                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	c.moveToFirst();
    	
    	if (c.isAfterLast() == false) 
    	{
    		conversationId = c.getString(c.getColumnIndexOrThrow(Chat._ID));    	    
    	}
    	c.close();
    	//Log.d("CONVERSATION","VALE "+conversationId);
    	return Integer.parseInt(conversationId);
    }
    
    
    public Cursor getConversations(){
		// Get the entries
    	String[] projection = {
    	    Chat._ID,
    	    Chat.COLUMN_NAME_PARTICIPANT,
    	    Chat.COLUMN_NAME_NEW_MESSAGES
    	    };
    	
    	Cursor c = database.query(
    		    Chat.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    null,                                // The columns for the WHERE clause
    		    null,                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );

		
		return c;
    }
    
    public ArrayList<HashMap<String, String>> getMessagesData(int id){
    	
    	ArrayList<HashMap<String, String>> conversationList = new ArrayList<HashMap<String, String>>();
        
		// Get the entries
    	String[] projection = {
    	    ChatMessage._ID,
    	    ChatMessage.COLUMN_NAME_AUTHOR,
    	    ChatMessage.COLUMN_NAME_BODY,
    	    ChatMessage.COLUMN_NAME_CONFIRMED,
    	    ChatMessage.COLUMN_NAME_SENT,
    	    ChatMessage.COLUMN_NAME_CREATED_AT,
    	    };
    	
    	if(!database.isOpen()){
    		database = getWritableDatabase();
    	}
    	Cursor c = database.query(
    		    ChatMessage.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    ChatMessage.COLUMN_NAME_CHAT_ID+" = ?",                                // The columns for the WHERE clause
    		    new String[] {id+""},                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	c.moveToFirst();
    	while (c.isAfterLast() == false) 
    	{
    		HashMap<String, String> map = new HashMap<String, String>();
    		
    		map.put(ChatMessage._ID, c.getString(c.getColumnIndexOrThrow(ChatMessage._ID)));
    		map.put(ChatMessage.COLUMN_NAME_AUTHOR, c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_AUTHOR)));
    		map.put(ChatMessage.COLUMN_NAME_BODY, c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_BODY)));
    		map.put(ChatMessage.COLUMN_NAME_CONFIRMED, c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_CONFIRMED)));
    		map.put(ChatMessage.COLUMN_NAME_SENT, c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_SENT)));
    		map.put(ChatMessage.COLUMN_NAME_CREATED_AT, c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_CREATED_AT)));

    	    conversationList.add(map);
    	    c.moveToNext();
    	}
    	c.close();
		
		return conversationList;
    }
     
    
    public Cursor getMessages(int id){

		// Get the entries
    	String[] projection = {
    	    ChatMessage._ID,
    	    ChatMessage.COLUMN_NAME_AUTHOR,
    	    ChatMessage.COLUMN_NAME_BODY,
    	    ChatMessage.COLUMN_NAME_CONFIRMED,
    	    ChatMessage.COLUMN_NAME_SENT,
    	    ChatMessage.COLUMN_NAME_CREATED_AT,
    	    };
    	
    	Cursor c = database.query(
    		    ChatMessage.TABLE_NAME,  // The table to query
    		    projection,                               // The columns to return
    		    ChatMessage.COLUMN_NAME_CHAT_ID+" = ?",                                // The columns for the WHERE clause
    		    new String[] {id+""},                            // The values for the WHERE clause
    		    null,                                     // don't group the rows
    		    null,                                     // don't filter by row groups
    		    null                                 // The sort order
    		    );
    	
		
		return c;
    }

    public boolean setAsRead(int conversationId, boolean read){
    	
    	ContentValues values = new ContentValues();
    	values.put(Chat.COLUMN_NAME_NEW_MESSAGES, read?"0":"1");
    	
    	return database.update(Chat.TABLE_NAME, values, Chat._ID+" = ? AND "+Chat.COLUMN_NAME_NEW_MESSAGES+" = ?", new String[] {conversationId+"", read?"1":"0"})>0;
    }

	public boolean updateMessageState(String packetId, int event_type) {
    	ContentValues values = new ContentValues();
    	
		if(event_type == XMPPManager.EVENT_MESSAGE_SENT){
			values.put(ChatMessage.COLUMN_NAME_SENT, "1");
		}else if(event_type == XMPPManager.EVENT_MESSAGE_CONFIRMED){
			values.put(ChatMessage.COLUMN_NAME_CONFIRMED, "1");
		}
		
		return database.update(ChatMessage.TABLE_NAME, values, Chat._ID+" = ?", new String[] {packetId}) > 0;
		
	}

	public void addToPendingQueue(Message m) {
		ContentValues values = new ContentValues();
    	values.put(PendingQueue._ID, m.getPacketID());
    	database.insert( PendingQueue.TABLE_NAME, null, values);
		
	}

	public ArrayList<Message> getPendingQueue() {
		
		ArrayList<Message> al = new ArrayList<Message>();
		
		Cursor c = database.rawQuery(PENDING_QUEUE_QUERY, null);
		c.moveToFirst();
    	while (c.isAfterLast() == false) 
    	{
    		
    		
    		Message m = new Message(); 
    		m.setPacketID(c.getString(c.getColumnIndexOrThrow(PendingQueue._ID)));
    		m.setFrom(c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_AUTHOR)));
    		m.setTo(c.getString(c.getColumnIndexOrThrow(Chat.COLUMN_NAME_PARTICIPANT)));
    		m.setBody(c.getString(c.getColumnIndexOrThrow(ChatMessage.COLUMN_NAME_BODY)));
    		
    		
    	    al.add(m);
    	    c.moveToNext();
    	}
    	c.close();
		
		return al;
	}


	public void pollFromPendingQueue(String value) {
		database.delete(PendingQueue.TABLE_NAME, PendingQueue._ID+"= ?", new String[] {value});
		
	}


    

}
