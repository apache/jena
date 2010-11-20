/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.mgt;

import java.io.IOException ;
import java.util.Date ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;
import javax.servlet.http.HttpSession ;

import org.openjena.fuseki.http.HttpSC ;

public class PageDataset extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // Possible actions:
        // Upload to this DS.
        
        HttpSession session = request.getSession(false) ;
        if ( session == null )
        {
            // Redirect to starting point?.
            response.setContentType("text/html");
            response.setStatus(HttpSC.OK_200) ;
            ServletOutputStream out = response.getOutputStream() ;
            out.println("<p>No session</p>") ;
            out.println("<p>Start <a href=\""+PageNames.pageDataset+"\">here</a></p>") ;
            return ;
        }
        
        String dataset = (String)session.getAttribute("dataset") ;
        
        response.setContentType("text/html");
        response.setStatus(HttpSC.OK_200) ;
        ServletOutputStream out = response.getOutputStream() ;
        out.print("<p>Current: "+dataset+"</p>") ;

        out.println(
                    "<H2>Information on Your Session:</H2>\n" +
                    "<TABLE BORDER=1 ALIGN=CENTER>\n" +
                    "<TR BGCOLOR=\"#FFAD00\">\n" +
                    "  <TH>Info Type<TH>Value\n" +
                    "<TR>\n" +
                    "  <TD>ID\n" +
                    "  <TD>" + session.getId() + "\n" +
                    "</TR>\n"+
                    "<TR>\n" +
                    "  <TD>Creation Time\n" +
                    "  <TD>" + new Date(session.getCreationTime()) + "\n" +
                    "</TR>\n"+
                    "<TR>\n" +
                    "  <TD>Time of Last Access\n" +
                    "  <TD>" + new Date(session.getLastAccessedTime()) + "\n" +
                    "</TABLE>\n" +
        "</BODY></HTML>");

//        Cookie cookie = new Cookie("org.openjena.fuseki.session", dataset) ;
//        // 24 hours.
//        cookie.setMaxAge(24*60*60) ;
        
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