package com.wixet.utils;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.IQ;

/* DEPRECATED */
public class ConfirmationIQ extends IQ{

	public static final String NAMESPACE = "jabber:iq:confirmation";
	public static final String PACKET_ID = "id";
	public ConfirmationIQ(String from, String packetId){
		setType(IQ.Type.SET);
		setTo(from);
		DefaultPacketExtension ext = new DefaultPacketExtension("confirmation", NAMESPACE);
		ext.setValue(PACKET_ID, packetId);
		addExtension(ext);
	}
	
	
	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub 
		
		
		return getExtension(NAMESPACE).toXML();
	}

}
