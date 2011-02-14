/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.server;

import static java.lang.String.format ;

import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.PrintWriter ;
import java.io.StringWriter ;
import java.io.Writer ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.eclipse.jetty.http.HttpHeaders ;
import org.eclipse.jetty.http.HttpMethods ;
import org.eclipse.jetty.http.MimeTypes ;
import org.eclipse.jetty.server.HttpConnection ;
import org.eclipse.jetty.server.Request ;
import org.eclipse.jetty.server.handler.ErrorHandler ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.http.HttpSC ;

public class FusekiErrorHandler extends ErrorHandler
{
    /* ------------------------------------------------------------ */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpConnection connection = HttpConnection.getCurrentConnection();
        connection.getRequest().setHandled(true);
        String method = request.getMethod();
     
        if(!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(HttpMethods.HEAD))
            return;
        
        response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8) ;
        response.setHeader(HttpHeaders.CACHE_CONTROL, "must-revalidate,no-cache,no-store") ;
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024) ;
        //String writer = IO.UTF8(null) ;
        Writer writer = new OutputStreamWriter(bytes, "UTF-8") ;
        
        handleErrorPage(request, writer, connection.getResponse().getStatus(), connection.getResponse().getReason());
        
        if ( ! Fuseki.VERSION.equalsIgnoreCase("development") )
        {
            writer.write("\n") ;
            writer.write("\n") ;
            writer.write(format("Fuseki - version %s (Date: %s)", Fuseki.VERSION, Fuseki.BUILD_DATE)) ;
        }
        writer.flush();
        response.setContentLength(bytes.size()) ;
        // Copy :-(
        response.getOutputStream().write(bytes.toByteArray()) ;
        writer.close() ;
    }
    
    /* ------------------------------------------------------------ */
    @Override
    protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message)
        throws IOException
    {
        if ( message == null )
            message = HttpSC.getMessage(code) ;
        writer.write(format("Error %d: %s\n", code, message)) ;
        
        Throwable th = (Throwable)request.getAttribute("javax.servlet.error.exception");
        while(th!=null)
        {
            writer.write("\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            pw.flush();
            writer.write(sw.getBuffer().toString());
            writer.write("\n");
            th = th.getCause();
        }
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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