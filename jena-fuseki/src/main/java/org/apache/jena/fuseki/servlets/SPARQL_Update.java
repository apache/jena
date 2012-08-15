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
import static org.apache.jena.fuseki.HttpNames.paramUsingGraphURI ;
import static org.apache.jena.fuseki.HttpNames.paramUsingNamedGraphURI ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Arrays ;
import java.util.Collection ;
import java.util.Enumeration ;
import java.util.List ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.http.HttpSC ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.iri.IRI ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.web.MediaType ;
import org.openjena.riot.WebContent ;
import org.openjena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWithUsing ;
import com.hp.hpl.jena.update.* ;

public class SPARQL_Update extends SPARQL_Protocol
{
    private static String updateParseBase = "http://example/base/" ;
    private static IRIResolver resolver = IRIResolver.create(updateParseBase) ;
    
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

    protected static List<String> paramsForm = Arrays.asList(paramRequest, paramUpdate, 
                                                             paramUsingGraphURI, paramUsingNamedGraphURI) ;
    protected static List<String> paramsPOST = Arrays.asList(paramUsingGraphURI, paramUsingNamedGraphURI) ;
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        if ( ! HttpNames.METHOD_POST.equals(request.getMethod().toUpperCase()) )
            errorMethodNotAllowed("SPARQL Update : use POST") ;
        
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
            String charset = request.getCharacterEncoding() ;
            if ( charset != null && ! charset.equalsIgnoreCase(WebContent.charsetUTF8) )
                errorBadRequest("Bad charset: "+charset) ;
            validate(request, paramsPOST) ;
            return ;
        }
        
        if ( WebContent.contentTypeForm.equals(ctStr) )
        {
            String requestStr = request.getParameter(paramUpdate) ;
            if ( requestStr == null )
                requestStr = request.getParameter(paramRequest) ;
            if ( requestStr == null )
                errorBadRequest("SPARQL Update: No update= in HTML form") ;
            validate(request, paramsForm) ;
            return ;
        }
        
        
        error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Must be "+WebContent.contentTypeSPARQLUpdate+" or "+WebContent.contentTypeForm+" (got "+ctStr+")") ;
    }
    
    protected void validate(HttpServletRequest request, Collection<String> params)
    {
        if ( params != null )
        {
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement() ;
                if ( ! params.contains(name) )
                    warning("SPARQL Update: Unrecognize request parameter (ignored): "+name) ;
            }
        }
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
                String requestStr = null ;
                try { requestStr = IO.readWholeFileAsUTF8(action.request.getInputStream()) ; }
                catch (IOException ex) { IO.exception(ex) ; }

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
        processProtocol(action.request, updateRequest) ;
        
        action.beginWrite() ;
        try {
            UpdateAction.execute(updateRequest, action.getActiveDSG()) ;
            action.commit() ;
        }
        catch ( UpdateException ex) { action.abort() ; errorBadRequest(ex.getMessage()) ; }
        finally { action.endWrite() ; }
    }

    /* [It is an error to supply the using-graph-uri or using-named-graph-uri parameters 
     * when using this protocol to convey a SPARQL 1.1 Update request that contains an 
     * operation that uses the USING, USING NAMED, or WITH clause.]
     */
    
    private void processProtocol(HttpServletRequest request, UpdateRequest updateRequest)
    {
        String[] usingArgs = request.getParameterValues(paramUsingGraphURI) ;
        String[] usingNamedArgs = request.getParameterValues(paramUsingNamedGraphURI) ;
        if ( usingArgs == null && usingNamedArgs == null )
            return ;
        if ( usingArgs == null )
            usingArgs = new String[0] ;
        if ( usingNamedArgs == null )
            usingNamedArgs = new String[0] ;
        // Impossible.
//        if ( usingArgs.length == 0 && usingNamedArgs.length == 0 )
//            return ;
        // ---- check USING/USING NAMED/WITH not used.
        // ---- update request to have USING/USING NAMED 
        for ( Update up : updateRequest.getOperations() )
        {
            if ( up instanceof UpdateWithUsing )
            {
                UpdateWithUsing upu = (UpdateWithUsing)up ;
                if ( upu.getUsing().size() != 0 || upu.getUsingNamed().size() != 0 || upu.getWithIRI() != null )
                    errorBadRequest("SPARQL Update: Protocol using-graph-uri or using-named-graph-uri present where update request has USING, USING NAMED or WITH") ;
                for ( String a : usingArgs )
                    upu.addUsing(createNode(a)) ;
                for ( String a : usingNamedArgs )
                    upu.addUsingNamed(createNode(a)) ;
            }
        }
    }
    
    private static Node createNode(String x)
    {
        try {
            IRI iri = resolver.resolve(x) ;
            return Node.createURI(iri.toString()) ;
        } catch (Exception ex)
        {
            errorBadRequest("SPARQL Update: bad IRI: "+x) ;
            return null ;
        }
        
    }
}
