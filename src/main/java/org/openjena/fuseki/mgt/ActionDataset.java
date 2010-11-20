/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.mgt;

import java.io.IOException ;
import java.io.UnsupportedEncodingException ;
import java.util.Iterator ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;
import javax.servlet.http.HttpSession ;

import org.openjena.atlas.lib.Base64 ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.server.DatasetRegistry ;

/** Log-in and choose dataset */
public class ActionDataset extends HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        request.getRemoteUser() ;
        request.getUserPrincipal() ;
        
        String dataset = request.getParameter("dataset") ;
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
            response.setHeader(HttpNames.hLocation, "info") ;
            response.setStatus(HttpSC.SEE_OTHER_303) ;
        }
        else
        {
            // Welcome style - but HTML inline :-(
            response.setContentType("text/html");
            response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = response.getOutputStream() ;
            out.print("<p>"+dataset+"("+known+")</p>") ;

            Iterator<String> iter = DatasetRegistry.get().keys() ;
            for ( ; iter.hasNext() ; )
            {
                String name = iter.next() ;
                out.print("<li>") ;
                out.print(name) ;
                out.println("</li>") ;
            }
            out.println("</ul>") ;
            out.println("<p><a href=\"info\">Next</a></p>") ;
        }
        
//        Cookie cookie = new Cookie("org.openjena.fuseki.session", dataset) ;
//        // 24 hours.
//        cookie.setMaxAge(24*60*60) ;
        
    }
    
    /**
     * This method returns true if the HttpServletRequest contains a valid
     * authorisation header
     * @param req The HttpServletRequest to test
     * @return true if the Authorisation header is valid
     **/

    private boolean authenticate(HttpServletRequest req)
    {
        String authhead=req.getHeader("Authorization");

        if(authhead!=null)
        {
            byte[] up = Base64.decode(authhead.substring(6)) ;
            //*****Decode the authorisation String*****
            String usernpass ;
            try
            {
                usernpass = new String(up, "ascii") ;
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                usernpass = null ;
            }
            //*****Split the username from the password*****
            String user=usernpass.substring(0,usernpass.indexOf(":"));
            String password=usernpass.substring(usernpass.indexOf(":")+1);

            if (user.equals("user") && password.equals("pass"))
                return true;
        }

        return false;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */