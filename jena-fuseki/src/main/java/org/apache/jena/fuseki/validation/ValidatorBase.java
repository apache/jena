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

package org.apache.jena.fuseki.validation;

import java.io.IOException ;

import javax.servlet.ServletConfig ;
import javax.servlet.ServletException ;
import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.servlets.ServletBase ;
import org.slf4j.Logger ;

public abstract class ValidatorBase extends HttpServlet 
{
    protected static Logger serviceLog = Fuseki.requestLog ;

    public static final String cssFile          = "/fuseki.css" ;
    public static final String respService      = "X-Service" ;

    
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
    
    protected abstract void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse) ;

    protected static void setHeaders(HttpServletResponse httpResponse)
    {
        ServletBase.setCommonHeaders(httpResponse) ; 
        httpResponse.setCharacterEncoding("UTF-8") ;
        httpResponse.setContentType("text/html") ;
        httpResponse.setHeader(respService, "Fuseki/ARQ SPARQL Query Validator: http://jena.apache.org/") ;
    }
    
    protected static String htmlQuote(String str)
    {
        StringBuilder sBuff = new StringBuilder() ;
        for ( int i = 0 ; i < str.length() ; i++ )
        {
            char ch = str.charAt(i) ;
            switch (ch)
            {
                case '<': sBuff.append("&lt;") ; break ;
                case '>': sBuff.append("&gt;") ; break ;
                case '&': sBuff.append("&amp;") ; break ;
                default: 
                    // Work around Eclipe bug with StringBuffer.append(char)
                    //try { sBuff.append(ch) ; } catch (Exception ex) {}
                    sBuff.append(ch) ;
                    break ;  
            }
        }
        return sBuff.toString() ; 
    }

    protected static void startFixed(ServletOutputStream outStream) throws IOException
    {
        outStream.println("<pre class=\"box\">") ;
    }

    protected static void columns(String prefix, ServletOutputStream outStream) throws IOException
    {
        outStream.print(prefix) ;
        outStream.println("         1         2         3         4         5         6         7") ;
        outStream.print(prefix) ;
        outStream.println("12345678901234567890123456789012345678901234567890123456789012345678901234567890") ;
    }
    
    protected static void finishFixed(ServletOutputStream outStream) throws IOException
    {
        outStream.println("</pre>") ;
    }
    
    protected static void printHead(ServletOutputStream outStream, String title) throws IOException
    {
        outStream.println("<head>") ;
        outStream.println(" <title>"+title+"</title>") ;
        outStream.println("   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">") ;
        outStream.println("   <link rel=\"stylesheet\" type=\"text/css\" href=\""+cssFile+"\" />") ;
        //outStream.println() ;
        outStream.println("</head>") ;
    }
}
