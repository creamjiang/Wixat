package com.wixet.wixat.database;

import android.provider.BaseColumns;

public class ChatMessage implements BaseColumns {
	public static final String TABLE_NAME = "chat_message";
    public static final String COLUMN_NAME_AUTHOR = "author";
    public static final String COLUMN_NAME_CHAT_ID = "chat_id";
    public static final String COLUMN_NAME_BODY = "body";
    public static final String COLUMN_NAME_CREATED_AT = "created_at";
    
    
}
