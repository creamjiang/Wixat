package com.wixet.wixat.xmpp.packet;

import org.jivesoftware.smack.packet.PacketExtension;

public class ChatStateMessage implements PacketExtension {

	    // see http://xmpp.org/extensions/xep-0085.html
		public static String COMPOSING = "composing";
		public ChatStateMessage (String state){
			
		}
		@Override
		public String getElementName() {
			return "composing";
		}

		@Override
		public String getNamespace() {
			return "http://jabber.org/protocol/chatstates";
		}

		@Override
		public String toXML() {
			return "<" + getElementName() + " xmlns=\"" + getNamespace() + "\" />";
		}
    	
    }