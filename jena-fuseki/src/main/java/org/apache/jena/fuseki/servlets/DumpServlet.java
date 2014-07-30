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

/** A servlet that dumps its request
 */

// Could be neater - much, much neater!
package org.apache.jena.fuseki.servlets;

import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.PrintWriter ;
import java.io.StringWriter ;
import java.util.Date ;
import java.util.Enumeration ;
import java.util.Locale ;
import java.util.Properties ;

import javax.servlet.ServletContext ;
import javax.servlet.http.Cookie ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

public class DumpServlet extends HttpServlet
{
    private static final long serialVersionUID = 99L;  // Serilizable.


    public DumpServlet()
    {

    }

    @Override
    public void init()
    {
        return ;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            PrintWriter out = resp.getWriter() ;
            resp.setContentType("text/html");

            String now = new Date().toString() ;

            // HEAD
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("<Title>Dump @ "+now+"</Title>") ;
            // Reduce the desire to cache it.
            out.println("<meta CONTENT=now HTTP-EQUIV=expires>") ;
            out.println("</head>") ;

            // BODY
            out.println("<body>") ;
            out.println("<pre>") ;

            out.println("Dump : "+now);
            out.println() ;
            out.println("==== Request");
            out.println() ;
            out.print(dumpRequest(req)) ;
            out.println() ;
                        
            out.println(">>>> Body");
            out.println() ;
            printBody(out, req) ;
            out.println("<<<< Body");
            
            out.println("==== ServletContext");
            out.println() ;
            out.print(dumpServletContext());
            out.println() ;

            out.println("==== Environment");
            out.println() ;
            out.print(dumpEnvironment());
            out.println() ;

            out.println("</pre>") ;

            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
        } catch (IOException e)
        { }
    }

    // This resets the input stream

    static public String dumpRequest(HttpServletRequest req)
    { 
         StringWriter sw = new StringWriter() ;
         try( PrintWriter pw = new PrintWriter(sw) ) {
            // Standard environment
            pw.println("Method:                 "+req.getMethod());
            pw.println("getContentLength:       "+Integer.toString(req.getContentLength()));
            pw.println("getContentType:         "+req.getContentType());
            pw.println("getRequestURI:          "+req.getRequestURI());
            pw.println("getRequestURL:          "+req.getRequestURL());
            pw.println("getContextPath:         "+req.getContextPath());
            pw.println("getServletPath:         "+req.getServletPath());
            pw.println("getPathInfo:            "+req.getPathInfo());
            pw.println("getPathTranslated:      "+req.getPathTranslated());
            pw.println("getQueryString:         "+req.getQueryString());
            pw.println("getProtocol:            "+req.getProtocol());
            pw.println("getScheme:              "+req.getScheme());
            pw.println("getServerName:          "+req.getServerName());
            pw.println("getServerPort:          "+req.getServerPort());
            pw.println("getRemoteUser:          "+req.getRemoteUser());
            pw.println("getRemoteAddr:          "+req.getRemoteAddr());
            pw.println("getRemoteHost:          "+req.getRemoteHost());
            pw.println("getRequestedSessionId:  "+req.getRequestedSessionId());
            {
                Cookie c[] = req.getCookies() ;
                if ( c == null )
                    pw.println("getCookies:            <none>");
                else
                {
                    for ( Cookie aC : c )
                    {
                        pw.println( "Cookie:        " + aC.getName() );
                        pw.println( "    value:     " + aC.getValue() );
                        pw.println( "    version:   " + aC.getVersion() );
                        pw.println( "    comment:   " + aC.getComment() );
                        pw.println( "    domain:    " + aC.getDomain() );
                        pw.println( "    maxAge:    " + aC.getMaxAge() );
                        pw.println( "    path:      " + aC.getPath() );
                        pw.println( "    secure:    " + aC.getSecure() );
                        pw.println();
                    }
                }
            }
            
            {
                // To do: create a string for the output so can send to console and return it.
                Enumeration<String> en = req.getHeaderNames() ;

                for ( ; en.hasMoreElements() ; )
                {
                    String name = en.nextElement() ;
                    String value = req.getHeader(name) ;
                    pw.println("Head: "+name + " = " + value) ;
                }
            }
            
            Enumeration<String> en2 = req.getAttributeNames() ;
            if ( en2.hasMoreElements() )
                pw.println();
            for ( ; en2.hasMoreElements() ; )
            {
                String name = en2.nextElement() ;
                String value = req.getAttribute(name).toString() ;
                pw.println("Attr: "+name + " = " + value) ;
            }

            // Note that doing this on a form causes the forms content (body) to be read
            // and parsed as form variables.
//            en = req.getParameterNames() ;
//            if ( en.hasMoreElements() )
//                pw.println();
//            for ( ; en.hasMoreElements() ; )
//            {
//                String name = (String)en.nextElement() ;
//                String value = req.getParameter(name) ;
//                pw.println("Param: "+name + " = " + value) ;
//            }


            
//            MultiMap<String, String> map = WebLib.parseQueryString(req) ;
//            for ( String name : map.keys() )
//                for ( String value : map.get(name) )
//                    pw.println("Param: "+name + " = " + value) ;
            
            Enumeration<Locale> en = req.getLocales() ;
            if ( en.hasMoreElements() )
                pw.println();
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement().toString() ;
                pw.println("Locale: "+name) ;
            }

            pw.println() ;
            pw.flush();
            //printBody(pw, req) ;
            return sw.toString() ;
        }
         
    }

    static void printBody(PrintWriter pw, HttpServletRequest req) throws IOException
    {
        BufferedReader in = req.getReader() ;
        if ( req.getContentLength() > 0 )
            // Need +2 because last line may not have a CR/LF on it.
            in.mark(req.getContentLength()+2) ;
        else
            // This is a dump - try to do something that works, even if inefficient.
            in.mark(100*1024) ;

        while(true)
        {
            String x = in.readLine() ;
            if ( x == null )
                break ;
            x = x.replaceAll("&", "&amp;") ;
            x = x.replaceAll("<", "&lt;") ;
            x = x.replaceAll(">", "&gt;") ;
            pw.println(x) ;
        }
        try { in.reset() ;} catch (IOException e) { System.out.println("DumpServlet: Reset of content failed: "+e) ; }
    }
    
    /**
     * <code>dumpEnvironment</code>
     * @return String that is the HTML of the System properties as name/value pairs.
     * The values are with single quotes independent of whether or not the value has
     * single quotes in it.
     */
    static public String dumpEnvironment()
    {
        Properties properties = System.getProperties();

        StringWriter sw = new StringWriter() ;
        try(PrintWriter pw = new PrintWriter(sw) ) {
            Enumeration<Object> en = properties.keys();
            while(en.hasMoreElements())
            {
                String key = en.nextElement().toString();
                pw.println(key+": '"+properties.getProperty(key)+"'");
            }
            pw.println() ;
            pw.flush() ;
            return sw.toString() ; 
        }
    }

    public String dumpServletContext()
    {
        StringWriter sw = new StringWriter() ;
        try(PrintWriter pw = new PrintWriter(sw)) {

            ServletContext sc =  getServletContext();
            pw.println("majorVersion: '"+sc.getMajorVersion()+"'");
            pw.println("minorVersion: '"+sc.getMinorVersion()+"'");
            pw.println("contextName:  '"+sc.getServletContextName()+"'");
            pw.println("servletInfo:  '"+getServletInfo()+"'");
            pw.println("serverInfo:  '"+sc.getServerInfo()+"'");

            {
                Enumeration<String> en = sc.getInitParameterNames();
                if (en != null) {
                    pw.println("initParameters: ");
                    while(en.hasMoreElements())
                    {
                        String key = en.nextElement();
                        pw.println(key+": '"+sc.getInitParameter(key)+"'");
                    }
                }
            }

            {
                Enumeration<String> en = sc.getAttributeNames();
                if (en != null) {
                    pw.println("attributes: ");
                    while(en.hasMoreElements())
                    {
                        String key = en.nextElement();
                        pw.println(key+": '"+sc.getAttribute(key)+"'");
                    }
                }
            }
            pw.println() ;
            pw.close() ;
        }
        return sw.toString() ;      
    }

    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    {
        doGet(req, resp) ;
    }


    @Override
    public String getServletInfo()
    {
        return "Dump";
    }
}
