/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;
import static org.openjena.fuseki.HttpNames.paramRequest ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Enumeration ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateException ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class SPARQL_Update extends SPARQL_ServletBase
{
    private static Logger log = LoggerFactory.getLogger(SPARQL_Update.class) ;
    
    private class HttpActionUpdate extends HttpAction {
        public HttpActionUpdate(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, dsg, request, response, verbose) ;
        }
    }
    
    public SPARQL_Update(boolean verbose)
    { super(PlainRequestFlag.REGULAR, verbose) ; }

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
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        if ( HttpNames.METHOD_POST.equals(request.getMethod().toUpperCase()) )
            return true ;
        errorOccurred("Bad!") ;
        return false ;
    }

    @Override
    protected String mapRequestToDataset(String uri)
    {
        String uri2 = mapRequestToDataset(uri, HttpNames.ServiceUpdate) ;
        return (uri2 != null) ? uri2 : uri ; 
    }

    @Override
    protected void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response)
    {
        validate(request) ;
        HttpActionUpdate action = new HttpActionUpdate(id, dsg, request, response, verbose_debug) ;
        
        String incoming = request.getContentType() ;
        if ( WebContent.contentSPARQLUpdate.equals(incoming) )
        { executeBody(action) ; return ; }
        if ( WebContent.contentTypeForm.equals(incoming) )
        { executeForm(action) ;  return ; }
    }

    private void validate(HttpServletRequest request)
    {
        String incoming = request.getContentType() ;
        if ( WebContent.contentSPARQLUpdate.equals(incoming) )
        {
            // For now, all query string stuff is not allowed.
            if ( request.getQueryString() != null )
                errorBadRequest("No query string allowed: found: "+request.getQueryString()) ;
            // For later...
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames() ;
            if ( en.hasMoreElements() )
                errorBadRequest("No request parameters allowed") ;
            return ;
        }
        
        if ( WebContent.contentTypeForm.equals(incoming) )
        {
            String requestStr = request.getParameter(paramRequest) ;
            if ( requestStr == null )
                errorBadRequest("SPARQL Update: No request= in HTML form") ;
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement() ;
                if ( !name.equals(paramRequest) )
                    errorBadRequest("SPARQL Update: Unrecognize request parameter: "+name) ;
            }
            
            return ;
        }
        
        error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Must be "+WebContent.contentSPARQLUpdate+" or "+WebContent.contentTypeForm) ;
    }

    private void executeBody(HttpActionUpdate action)
    {
        InputStream input = null ;
        try { input = action.request.getInputStream() ; }
        catch (IOException ex) { errorOccurred(ex) ; }

        UpdateRequest req ;
        try {
            if ( action.verbose )
            {
                //String requestStr = IO.readWholeFileAsUTF8(action.request.getInputStream()) ;
                // (fixed)Bug in atlas.IO
                byte[] b = IO.readWholeFile(input) ;
                String requestStr = Bytes.bytes2string(b) ;
                String requestStr1 = formatForLog(requestStr) ;
                serverlog.info(format("[%d] Update = %s", action.id, requestStr1)) ;
                req = UpdateFactory.create(requestStr) ;
            }    
            else
                req = UpdateFactory.read(input) ;
        } catch (UpdateException ex) { errorBadRequest(ex.getMessage()) ; req = null ; }
        execute(action, req) ;
    }

    private void executeForm(HttpActionUpdate action)
    {
        //error(HttpSC.NOT_IMPLEMENTED_501, "SPARQL Update: POST of HTML form not supported yet") ;
        String requestStr = action.request.getParameter(paramRequest) ;
        if ( action.verbose )
            serverlog.info(format("[%d] Form update = %s", action.id, formatForLog(requestStr))) ;
        
        UpdateRequest req ; 
        try {
            req = UpdateFactory.create(requestStr) ;
        } catch (UpdateException ex) { errorBadRequest(ex.getMessage()) ; req = null ; }
        execute(action, req) ;
    }
    
    private void execute(HttpActionUpdate action, UpdateRequest updateRequest)
    {
        //GraphStore graphStore = GraphStoreFactory.create(action.dsg) ;
        action.beginWrite() ;
        try {
            UpdateAction.execute(updateRequest, action.dsg) ;
            successNoContent(action) ;
        }
        catch ( UpdateException ex) { errorBadRequest(ex.getMessage()) ; }
        finally { action.endWrite() ; }
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