/**
 * $RCSfile$
 * $Revision: 1710 $
 * $Date: 2005-07-26 15:56:14 -0300 (Tue, 26 Jul 2005) $
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

package org.jivesoftware.openfire.plugin.wixat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.WixatPlugin;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Presence;

import java.io.PrintWriter;

/**
 * Servlet that provides information about the presence status of the users in the system.
 * The information may be provided in XML format or in graphical mode. Use the <b>type</b>
 * parameter to specify the type of information to get. Possible values are <b>image</b> and
 * <b>xml</b>. If no type was defined then an image representation is assumed.<p>
 * <p/>
 * The request <b>MUST</b> include the <b>jid</b> parameter. This parameter will be used
 * to locate the local user in the server. If this parameter is missing from the request then
 * an error will be logged and nothing will be returned.
 *
 * @author Gaston Dombiak
 */
public class WixatCheckServlet extends HttpServlet {

	private static final Logger Log = LoggerFactory.getLogger(WixatCheckServlet.class);
	
    private WixatPlugin plugin;


    @Override
	public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        plugin =
                (WixatPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("wixat");

        // Exclude this servlet from requering the user to login
        AuthCheckFilter.addExclude("wixat/check");
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
			
		response.setContentType("text/plain");
		
		String phoneEncrypted = request.getParameter("phone");
        String secret = request.getParameter("secret");
        
		PrintWriter out = response.getWriter();
			        
        
          // Check that our plugin is enabled.
        if (!plugin.isEnabled()) {
            out.println("WixatServiceDisabled");
            return;
        }
       
        // Check this request is authorised
        if (secret == null || !secret.equals(plugin.getSecret())){
            out.println("WixatServiceSecretException");
            return;
         }
        // Check the request type and process accordingly
        
        
        try{
			String phone = plugin.decrypt(phoneEncrypted);
			if(plugin.exists(phone)){
				out.println("true");
			}else{
				out.println("false");
			}
		}catch (Exception e){
			out.println("InvalidCryptedData");
		}
        
        
        out.flush();
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
	public void destroy() {
        super.destroy();
        // Release the excluded URL
        AuthCheckFilter.removeExclude("wixat/check");
    }


}
