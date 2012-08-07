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

import static java.lang.String.format ;
import static org.apache.jena.fuseki.Fuseki.requestLog ;
import static org.apache.jena.fuseki.HttpNames.paramRequest ;
import static org.apache.jena.fuseki.HttpNames.paramUpdate ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Enumeration ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.http.HttpSC ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.web.MediaType ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateException ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class SPARQL_Update extends SPARQL_Protocol
{
    private static String updateParseBase = "http://example/base/" ;
    
    private class HttpActionUpdate extends HttpActionProtocol {
        public HttpActionUpdate(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, desc, request, response, verbose) ;
        }
    }
    
    public SPARQL_Update(boolean verbose)
    { super(verbose) ; }

    public SPARQL_Update()
    { this(false) ; }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.sendError(HttpSC.BAD_REQUEST_400, "Attempt to perform SPARQL update by GET.  Use POST") ;
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        doCommon(request, response) ;
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
    {
        response.setHeader(HttpNames.hAllow, "OPTIONS,POST");
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }

    @Override
    protected void perform(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response)
    {
        // validate -> action.
        validate(request) ;
        HttpActionUpdate action = new HttpActionUpdate(id, desc, request, response, verbose_debug) ;
        
        // WebContent needs to migrate to using ContentType.
        String ctStr ;
        {
            MediaType incoming = FusekiLib.contentType(request) ;
            if ( incoming == null )
                ctStr = WebContent.contentTypeSPARQLUpdate ;
            else
                ctStr = incoming.getContentType() ;
        }
        // ----
        
        if (WebContent.contentTypeSPARQLUpdate.equals(ctStr))
        {
            executeBody(action) ;
            return ;
        }
        if (WebContent.contentTypeForm.equals(ctStr))
        {
            executeForm(action) ;
            return ;
        }
        error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: " + request.getContentType()) ;
    }

    @Override
    protected void validate(HttpServletRequest request)
    {
        if ( ! HttpNames.METHOD_POST.equals(request.getMethod().toUpperCase()) )
            errorMethodNotAllowed("SPARQL Update : use POST") ;
        
        // WebContent needs to migrate to using ContentType.
        String ctStr ;
        {
            MediaType incoming = FusekiLib.contentType(request) ;
            if ( incoming == null )
                ctStr = WebContent.contentTypeSPARQLUpdate ;
            else
                ctStr = incoming.getContentType() ;
        }
        // ----
        
        if ( WebContent.contentTypeSPARQLUpdate.equals(ctStr) )
        {
            // For now, all query string stuff is not allowed.
            if ( request.getQueryString() != null )
                errorBadRequest("No query string allowed: found: "+request.getQueryString()) ;
            // For later...
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames() ;
            if ( en.hasMoreElements() )
                errorBadRequest("No request parameters allowed") ;
            
            String charset = request.getCharacterEncoding() ;
            if ( charset != null && ! charset.equalsIgnoreCase(WebContent.charsetUTF8) )
                errorBadRequest("Bad charset: "+charset) ;
            return ;
        }
        
        if ( WebContent.contentTypeForm.equals(ctStr) )
        {
            String requestStr = request.getParameter(paramUpdate) ;
            if ( requestStr == null )
                requestStr = request.getParameter(paramRequest) ;
            if ( requestStr == null )
                errorBadRequest("SPARQL Update: No update= in HTML form") ;
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement() ;
                if ( !name.equals(paramRequest) && !name.equals(paramUpdate) )
                    errorBadRequest("SPARQL Update: Unrecognized update request parameter: "+name) ;
            }
            
            return ;
        }
        
        error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Must be "+WebContent.contentTypeSPARQLUpdate+" or "+WebContent.contentTypeForm+" (got "+ctStr+")") ;
    }

    private void executeBody(HttpActionUpdate action)
    {
        InputStream input = null ;
        try { input = action.request.getInputStream() ; }
        catch (IOException ex) { errorOccurred(ex) ; }

        UpdateRequest req ;
        try {
            
            if ( super.verbose_debug || action.verbose )
            {
                // Verbose mode only .... capture request for logging (does not scale). 
                // Content-Length.
                //String requestStr = IO.readWholeFileAsUTF8(action.request.getInputStream()) ;
                // (fixed)Bug in atlas.IO
                byte[] b = IO.readWholeFile(input) ;
                String requestStr = Bytes.bytes2string(b) ;
                String requestStrLog = formatForLog(requestStr) ;
                requestLog.info(format("[%d] Update = %s", action.id, requestStrLog)) ;
                req = UpdateFactory.create(requestStr, Syntax.syntaxARQ) ;
            }    
            else
                req = UpdateFactory.read(input, Syntax.syntaxARQ) ;
        } 
        catch (UpdateException ex) { errorBadRequest(ex.getMessage()) ; req = null ; }
        catch (QueryParseException ex)  { errorBadRequest(messageForQPE(ex)) ; req = null ; } 
        execute(action, req) ;
        successNoContent(action) ;
    }


    private void executeForm(HttpActionUpdate action)
    {
        String requestStr = action.request.getParameter(paramUpdate) ;
        if ( requestStr == null )
            requestStr = action.request.getParameter(paramRequest) ;
        
        if ( super.verbose_debug || action.verbose )
            requestLog.info(format("[%d] Form update = %s", action.id, formatForLog(requestStr))) ;
        
        UpdateRequest req ; 
        try {
            req = UpdateFactory.create(requestStr, updateParseBase) ;
        }
        catch (UpdateException ex) { errorBadRequest(ex.getMessage()) ; req = null ; }
        catch (QueryParseException ex) { errorBadRequest(messageForQPE(ex)) ; req = null ; }
        execute(action, req) ;
        successPage(action,"Update succeeded") ;
    }
    
    private void execute(HttpActionUpdate action, UpdateRequest updateRequest)
    {
        //GraphStore graphStore = GraphStoreFactory.create(action.dsg) ;
        action.beginWrite() ;
        try {
            UpdateAction.execute(updateRequest, action.getActiveDSG()) ;
            action.commit() ;
        }
        catch ( UpdateException ex) { action.abort() ; errorBadRequest(ex.getMessage()) ; }
        finally { action.endWrite() ; }
    }
}
