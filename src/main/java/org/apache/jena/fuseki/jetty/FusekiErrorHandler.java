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

package org.apache.jena.fuseki.jetty;

import static java.lang.String.format ;

import java.io.* ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.web.HttpSC ;
import org.eclipse.jetty.http.HttpMethod ;
import org.eclipse.jetty.http.MimeTypes ;
import org.eclipse.jetty.server.Request ;
import org.eclipse.jetty.server.Response ;
import org.eclipse.jetty.server.handler.ErrorHandler ;

public class FusekiErrorHandler extends ErrorHandler
{
    public FusekiErrorHandler() {}
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String method = request.getMethod();
     
        if ( !method.equals(HttpMethod.GET.asString())
             && !method.equals(HttpMethod.POST.asString())
             && !method.equals(HttpMethod.HEAD.asString()) )
            return ;
        
        response.setContentType(MimeTypes.Type.TEXT_PLAIN_UTF_8.asString()) ;
        ServletOps.setNoCache(response) ;
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024) ;
        Writer writer = IO.asUTF8(bytes) ;
        String reason=(response instanceof Response)?((Response)response).getReason():null;
        handleErrorPage(request, writer, response.getStatus(), reason) ;
        
        if ( ! Fuseki.VERSION.equalsIgnoreCase("development") &&
             ! Fuseki.VERSION.equals("${project.version}") )
        {
            writer.write("\n") ;
            writer.write("\n") ;
            writer.write(format("Fuseki - version %s (Build date: %s)\n", Fuseki.VERSION, Fuseki.BUILD_DATE)) ;
        }
        writer.flush();
        response.setContentLength(bytes.size()) ;
        // Copy :-(
        response.getOutputStream().write(bytes.toByteArray()) ;
        writer.close() ;
    }
    
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
