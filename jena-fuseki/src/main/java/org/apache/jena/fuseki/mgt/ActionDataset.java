/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.fuseki.mgt;

import java.io.IOException ;
import java.io.UnsupportedEncodingException ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;
import javax.servlet.http.HttpSession ;

import org.apache.commons.codec.binary.Base64 ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.web.HttpSC ;

/** Log-in and choose dataset */
public class ActionDataset extends HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
//        request.getRemoteUser() ;
//        request.getUserPrincipal() ;
        
        String dataset = FusekiLib.safeParameter(request, "dataset") ;
        HttpSession session = request.getSession(true) ;
        session.setAttribute("dataset", dataset) ;
        session.setMaxInactiveInterval(15*60) ; // 10 mins
        
        boolean known = DatasetRegistry.get().isRegistered(dataset) ;
        if ( !known )
        {
            response.sendError(HttpSC.BAD_REQUEST_400, "No such dataset: "+dataset) ;
            return ;
        }
        
        if ( true )
        {
            // Redirect to GET page.
            response.setHeader(HttpNames.hLocation, PageNames.pageAfterLogin) ;
            response.setStatus(HttpSC.SEE_OTHER_303) ;
        }
        else
        {
            // Welcome style - but HTML inline :-(
            response.setContentType("text/html");
            response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = response.getOutputStream() ;
            out.print("<p>"+dataset+"("+known+")</p>") ;

            for ( String name : DatasetRegistry.get().keys() ) {
                out.print("<li>") ;
                out.print(name) ;
                out.println("</li>") ;
            }
            out.println("</ul>") ;
            out.println("<p><a href=\"info\">Next</a></p>") ;
        }
        
//        Cookie cookie = new Cookie("org.apache.jena.fuseki.session", dataset) ;
//        // 24 hours.
//        cookie.setMaxAge(24*60*60) ;
        
    }
    
    /**
     * This method returns true if the HttpServletRequest contains a valid
     * authorisation header
     * @param req The HttpServletRequest to test
     * @return true if the Authorisation header is valid
     */

    private boolean authenticate(HttpServletRequest req)
    {
        String authhead=req.getHeader("Authorization");

        if(authhead!=null)
        {
            byte[] up = Base64.decodeBase64(authhead.substring(6)) ;
            // Decode the authorisation String
            String usernpass ;
            try
            {
                usernpass = new String(up, "ascii") ;
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                usernpass = null ;
            }
            // Split the username from the password
            String user=usernpass.substring(0,usernpass.indexOf(":"));
            String password=usernpass.substring(usernpass.indexOf(":")+1);

            if (user.equals("user") && password.equals("pass"))
                return true;
        }

        return false;
    }

}
