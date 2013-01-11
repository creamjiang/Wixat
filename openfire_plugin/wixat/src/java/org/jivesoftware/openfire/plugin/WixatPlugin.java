/**
 * $RCSfile$
 * $Revision: 1722 $
 * $Date: 2005-07-28 19:19:16 -0300 (Thu, 28 Jul 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.jivesoftware.openfire.MessageRouter;
import org.xmpp.packet.PacketExtension;
import org.dom4j.Element;

/**
 * Plugin that includes a servlet that provides information about users' and components'
 * presence in the server. For security reasons, the XMPP spec does not allow anyone to see
 * the presence of any user. Only the users that are subscribed to the presence of other
 * users may see their presences.<p/>
 *
 * However, in order to make the servlet more useful it is possible to configure this plugin
 * so that anyone or only the users that are subscribed to a user presence may see the presence
 * of other users.<p/>
 *
 * Currently, the servlet provides presence information in two formats: 1) In XML format
 * and 2) using images.<p>
 *
 * The presence plugin is also a component so that it can probe presences of other components.
 * The new component will use <tt>presence</tt> as the subdomain subdomain.
 *
 * @author Gaston Dombiak
 */
public class WixatPlugin implements Plugin, Component, PacketInterceptor {


	private static final String EXTENSION_NAME = "confirmation";
	private static final String EXTENSION_NAMESPACE = "jabber:confirmation";
	private static final String SERVER = "server";
	private static final String TYPE = "type";
	private static final String PACKET_ID = "packet_id";
    ////////////////////
	private UserManager userManager;
	private PluginManager pluginManager;
	
    private XMPPServer server;

    private String secret;
    private boolean enabled;
    private String cryptoKey;
    
        
	private MessageRouter messageRouter;
	private InterceptorManager interceptorManager;
    

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
		pluginManager = manager;
        server = XMPPServer.getInstance();
        userManager = server.getUserManager();
        
		/**/
		interceptorManager = InterceptorManager.getInstance();
		messageRouter = server.getMessageRouter();
		interceptorManager.addInterceptor(this);
				
		/**/


        secret = JiveGlobals.getProperty("plugin.wixat.secret", "");
        // If no secret key has been assigned to the user service yet, assign a random one.
        if (secret.equals("")){
            secret = StringUtils.randomString(8);
            setSecret(secret);
        }
        
        // See if the service is enabled or not.
        enabled = JiveGlobals.getBooleanProperty("plugin.wixat.enabled", false);

        // Get the list of IP addresses that can use this service. An empty list means that this filter is disabled.
        cryptoKey = JiveGlobals.getProperty("plugin.wixat.cryptoKey", "");

        // Listen to system property events
        //PropertyEventDispatcher.addListener(this);
    }

    public boolean exists(String phone){
		return userManager.isRegisteredUser(phone);
	}
	
	public boolean create(String phone, String password){
		
		boolean ok = true;
		try{
			userManager.createUser(phone, password, null, null);
		}catch(Exception e){
			ok = false;
		}
		
		return ok;
	}
	
    public void destroyPlugin() {
        userManager = null;
        // Stop listening to system property events
        //PropertyEventDispatcher.removeListener(this);
        interceptorManager.removeInterceptor(this);
    }

	
    
    /**
     * Returns the secret key that only valid requests should know.
     *
     * @return the secret key.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret key that grants permission to use the rostermanager.
     *
     * @param secret the secret key.
     */
    public void setSecret(String secret) {
        JiveGlobals.setProperty("plugin.wixat.secret", secret);
        this.secret = secret;
    }

    public String getCryptoKey() {
        return cryptoKey;
    }

    public void setCryptoKey(String cryptoKey) {
        JiveGlobals.setProperty("plugin.wixat.cryptoKey", cryptoKey);
        this.cryptoKey = cryptoKey;
    }

    /**
     * Returns true if the user service is enabled. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @return true if the user service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the user service. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @param enabled true if the user service should be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        JiveGlobals.setProperty("plugin.wixat.enabled",  enabled ? "true" : "false");
    }

    public void propertySet(String property, Map<String, Object> params) {
        if (property.equals("plugin.wixat.secret")) {
            this.secret = (String)params.get("value");
        }
        else if (property.equals("plugin.wixat.enabled")) {
            this.enabled = Boolean.parseBoolean((String)params.get("value"));
        }
        else if (property.equals("plugin.wixat.cryptoKey")) {
            this.cryptoKey = (String)params.get("value");
        }
    }

    public void propertyDeleted(String property, Map<String, Object> params) {
        if (property.equals("plugin.wixat.secret")) {
            this.secret = "";
        }
        else if (property.equals("plugin.wixat.enabled")) {
            this.enabled = false;
        }
        else if (property.equals("plugin.wixat.cryptoKey")) {
            this.cryptoKey = "";
        }
    }

    public void xmlPropertySet(String property, Map<String, Object> params) {
        // Do nothing
    }

    public void xmlPropertyDeleted(String property, Map<String, Object> params) {
        // Do nothing
    }
    
    
    
    //Base64 functions from http://stackoverflow.com/questions/469695/decode-base64-data-in-java
    private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static int[]  toInt   = new int[128];

    static {
        for(int i=0; i< ALPHABET.length; i++){
            toInt[ALPHABET[i]]= i;
        }
    }

    /**
     * Translates the specified byte array into Base64 string.
     *
     * @param buf the byte array (not null)
     * @return the translated Base64 string (not null)
     */
    public static String encode(byte[] buf){
        int size = buf.length;
        char[] ar = new char[((size + 2) / 3) * 4];
        int a = 0;
        int i=0;
        while(i < size){
            byte b0 = buf[i++];
            byte b1 = (i < size) ? buf[i++] : 0;
            byte b2 = (i < size) ? buf[i++] : 0;

            int mask = 0x3F;
            ar[a++] = ALPHABET[(b0 >> 2) & mask];
            ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
            ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
            ar[a++] = ALPHABET[b2 & mask];
        }
        switch(size % 3){
            case 1: ar[--a]  = '=';
            case 2: ar[--a]  = '=';
        }
        return new String(ar);
    }

    /**
     * Translates the specified Base64 string into a byte array.
     *
     * @param s the Base64 string (not null)
     * @return the byte array (not null)
     */
    public static byte[] decode(String s){
        int delta = s.endsWith( "==" ) ? 2 : s.endsWith( "=" ) ? 1 : 0;
        byte[] buffer = new byte[s.length()*3/4 - delta];
        int mask = 0xFF;
        int index = 0;
        for(int i=0; i< s.length(); i+=4){
            int c0 = toInt[s.charAt( i )];
            int c1 = toInt[s.charAt( i + 1)];
            buffer[index++]= (byte)(((c0 << 2) | (c1 >> 4)) & mask);
            if(index >= buffer.length){
                return buffer;
            }
            int c2 = toInt[s.charAt( i + 2)];
            buffer[index++]= (byte)(((c1 << 4) | (c2 >> 2)) & mask);
            if(index >= buffer.length){
                return buffer;
            }
            int c3 = toInt[s.charAt( i + 3 )];
            buffer[index++]= (byte)(((c2 << 6) | c3) & mask);
        }
        return buffer;
    } 
    
        public String encrypt(String message) throws Exception{
			Key sharedKey = new SecretKeySpec(decode(cryptoKey), "DESede");
            
            Cipher c = Cipher.getInstance("DESede");
            c.init(Cipher.ENCRYPT_MODE, sharedKey);
            byte[] input = message.getBytes();
            byte[] encrypted = c.doFinal(input);
            return encode(encrypted);
        }

        public String decrypt(String message) throws Exception{
			Key sharedKey = new SecretKeySpec(decode(cryptoKey), "DESede");

            Cipher c = Cipher.getInstance("DESede");
            c.init(Cipher.DECRYPT_MODE, sharedKey);
            return new String(c.doFinal(decode(message)));
        }
        
        
        public String getName() {
        return pluginManager.getName(this);
    }

    public String getDescription() {
        return pluginManager.getDescription(this);
    }

    public void initialize(JID jid, ComponentManager componentManager) {
    }

    public void start() {
    }

    public void shutdown() {
    }
    
        public void processPacket(Packet packet) {
        // Check that we are getting an answer to a presence probe
        /*if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            if (presence.isAvailable() || presence.getType() == Presence.Type.unavailable ||
                    presence.getType() == Presence.Type.error) {
                // Store answer of presence probes
                probedPresence.put(presence.getFrom().toString(), presence);
            }
        }*/
    }
    
    
    /* Confirm packet received to the client */



        		
 
	

	/* Send a response to client notifying message received by server */
    public void interceptPacket(Packet packet, Session session, boolean read, boolean processed) throws PacketRejectedException {
		if(processed && read && packet instanceof Message && ((Message) packet).getBody() != null){
			
		        Message confirmation = new Message();
		        PacketExtension ext = new PacketExtension(EXTENSION_NAME,EXTENSION_NAMESPACE);
		        Element e = ext.getElement();
		        e.addElement(TYPE).setText(SERVER);
		        e.addElement(PACKET_ID).setText(packet.getID());
		        confirmation.addExtension(ext);
				confirmation.setFrom(packet.getTo());
				confirmation.setTo(packet.getFrom());
				messageRouter.route(confirmation);
		}
				
	}
}
