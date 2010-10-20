/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.mgt;

import java.io.IOException ;
import java.util.Iterator ;

import javax.servlet.ServletConfig ;
import javax.servlet.ServletException ;
import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.server.DatasetRegistry ;
import org.slf4j.Logger ;

public class Manager extends HttpServlet 
{
    private static Logger log = Fuseki.serverlog ;
    
    public static final String cssFile          = "/fuseki.css" ;
    public static final String respService      = "X-Service" ;

    public Manager() {}

    @Override
    public void init() throws ServletException
    { super.init() ; }

    @Override
    public void init(ServletConfig config) throws ServletException
    { super.init(config) ; }

    @Override
    public void destroy()
    { }

    @Override
    public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    { execute(httpRequest, httpResponse) ; }

    @Override
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    { execute(httpRequest, httpResponse) ; }

    protected void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
            // Headers
            setHeaders(httpResponse) ;

            ServletOutputStream outStream = httpResponse.getOutputStream() ;

            outStream.println("<html>") ;

            printHead(outStream, "Fuseki Server Manager") ;

            outStream.println("<body>") ;
            outStream.println("<h1>Fuseki Server Manager</h1>") ;
            
            outStream.println("<p>Registered datasets</p>") ;
            outStream.println("<ul>") ;
            Iterator<String> iter = DatasetRegistry.get().keys() ;
            for ( ; iter.hasNext() ; )
            {
                String name = iter.next() ;
                outStream.print("<li>") ;
                outStream.print(name) ;
                outStream.println("</li>") ;
            }
            outStream.println("</ul>") ;
            
            outStream.println("</body>") ;
            outStream.println("</html>") ;
        
        } catch (Exception ex)
        {
            try {
                httpResponse.sendError(HttpSC.INTERNAL_SERVER_ERROR_500) ;
            } catch (Exception ex2) {}
            
        }
    }

    protected static void printHead(ServletOutputStream outStream, String title) throws IOException
    {
        outStream.println("<head>") ;
        outStream.println(" <title>"+title+"</title>") ;
        outStream.println(" <link rel=\"stylesheet\" type=\"text/css\" href=\""+cssFile+"\" />") ;
        //outStream.println() ;
        outStream.println("</head>") ;
    }
    
    protected static void setHeaders(HttpServletResponse httpResponse)
    {
        httpResponse.setCharacterEncoding("UTF-8") ;
        httpResponse.setContentType("text/html") ;
        httpResponse.setHeader(respService, "Fuseki : http://openjena.org/wiki/Fuseki") ;
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