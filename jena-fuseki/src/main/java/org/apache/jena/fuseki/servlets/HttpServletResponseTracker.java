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

package org.apache.jena.fuseki.servlets;

import java.io.IOException ;

import javax.servlet.http.HttpServletResponse ;
import javax.servlet.http.HttpServletResponseWrapper ;

import org.apache.jena.atlas.logging.Log ;

/** Intercepting wrapper so we can track the response settings for logging purposes */

public class HttpServletResponseTracker extends HttpServletResponseWrapper
{
    private final WebRequest webRequest ;

    public HttpServletResponseTracker(WebRequest webRequest, HttpServletResponse response)
    {
        super(response) ;
        this.webRequest = webRequest ;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        webRequest.statusCode = sc ;
        webRequest.message = msg ;
        super.sendError(sc, msg) ;
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        webRequest.statusCode = sc ;
        webRequest.message = null ;
        super.sendError(sc) ;
    }

    @Override
    public void setHeader(String name, String value)
    {
        super.setHeader(name, value) ;
        webRequest.headers.put(name, value) ;
    }

    @Override
    public void addHeader(String name, String value)
    {
        Log.warn(this, "Unexpected addHeader - not recorded in log") ;
        super.addHeader(name, value) ;
    }
    @Override
    public void setStatus(int sc) 
    {
        webRequest.statusCode = sc ;
        webRequest.message = null ;
        super.setStatus(sc) ;
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String sm)
    {
        webRequest.statusCode = sc ;
        webRequest.message = sm ;
        super.setStatus(sc, sm) ;
    }

    @Override
    public void setContentLength(int len)
    {
        webRequest.contentLength = len ;
        super.setContentLength(len) ;
    }

    @Override
    public void setContentType(String type)
    {
        webRequest.contentType = type ;
        super.setContentType(type) ;
    }
      
      // From HttpServletResponse
//      public void addCookie(Cookie cookie) {}
//      public boolean containsHeader(String name) {}
//      public String encodeURL(String url) { }
//      public String encodeRedirectURL(String url) {}
//      public String encodeUrl(String url) {}
//      public String encodeRedirectUrl(String url) {}
//      public void sendError(int sc, String msg) throws IOException
//      public void sendError(int sc) throws IOException
//      public void sendRedirect(String location) throws IOException {}
//      public void setDateHeader(String name, long date) {}
//      public void addDateHeader(String name, long date) {}
//      public void setHeader(String name, String value)
//      public void addHeader(String name, String value)
//      public void setIntHeader(String name, int value) {}
//      public void addIntHeader(String name, int value) {}
//      public void setStatus(int sc) 
//      public void setStatus(int sc, String sm)
//      public void sendRedirect(String location) throws IOException {}
//      public void setDateHeader(String name, long date) {}
//      public void addDateHeader(String name, long date) {}
        
        // From ServletResponse.
//         public ServletResponse getResponse() {}
//         public void setResponse(ServletResponse response) {}
//         public void setCharacterEncoding(String charset) {}
//         public String getCharacterEncoding() {}
//         public ServletOutputStream getOutputStream() throws IOException {}
//         public PrintWriter getWriter() throws IOException {}
//         public void setContentLength(int len) {}
//         public void setContentType(String type) {}
//         public String getContentType() {
//         public void setBufferSize(int size) {}
//         public int getBufferSize() {}
//         public void flushBuffer() throws IOException {}
//         public boolean isCommitted() {}
//         public void reset() {}
//         public void resetBuffer() {}
//         public void setLocale(Locale loc) {}
//         public Locale getLocale() {}
}
