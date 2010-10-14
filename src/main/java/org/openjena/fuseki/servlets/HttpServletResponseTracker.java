/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import java.io.IOException ;
import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletResponse ;
import javax.servlet.http.HttpServletResponseWrapper ;

import org.openjena.atlas.logging.Log ;

/** Intercepting wrapper so we can track the response settings for logging purposes */

public class HttpServletResponseTracker extends HttpServletResponseWrapper
    {
        Map <String, String> headers = new HashMap<String, String>() ;
        int statusCode = -1 ;
        String message = null ;
        int contentLength = -1 ;
        String contentType = null ;
        
        public HttpServletResponseTracker(HttpServletResponse response)
        {
            super(response) ;
        }

      @Override
      public void sendError(int sc, String msg) throws IOException
      {
          statusCode = sc ;
          message = msg ;
          super.sendError(sc, msg) ;
      }
      
      @Override
      public void sendError(int sc) throws IOException
      {
          statusCode = sc ;
          message = null ;
          super.sendError(sc) ;
      }
      
      @Override
      public void setHeader(String name, String value)
      {
          super.setHeader(name, value) ;
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
          statusCode = sc ;
          message = null ;
          super.setStatus(sc) ;
      }
      
      @Override
      public void setStatus(int sc, String sm)
      {
          statusCode = sc ;
          message = sm ;
          super.setStatus(sc, sm) ;
      }
        
      @Override
      public void setContentLength(int len)
      {
          contentLength = len ;
          super.setContentLength(len) ;
      }
      
      @Override
      public void setContentType(String type)
      {
          contentType = type ;
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