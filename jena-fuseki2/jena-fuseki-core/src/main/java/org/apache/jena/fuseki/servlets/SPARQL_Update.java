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
import static org.apache.jena.fuseki.server.CounterName.UpdateExecErrors ;
import static org.apache.jena.riot.WebContent.charsetUTF8 ;
import static org.apache.jena.riot.WebContent.contentTypeHTMLForm ;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLUpdate ;
import static org.apache.jena.riot.WebContent.ctSPARQLUpdate ;
import static org.apache.jena.riot.WebContent.isHtmlForm ;
import static org.apache.jena.riot.WebContent.matchContentType ;
import static org.apache.jena.riot.web.HttpNames.paramRequest ;
import static org.apache.jena.riot.web.HttpNames.paramUpdate ;
import static org.apache.jena.riot.web.HttpNames.paramUsingGraphURI ;
import static org.apache.jena.riot.web.HttpNames.paramUsingNamedGraphURI ;

import java.io.ByteArrayInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.Arrays ;
import java.util.Collection ;
import java.util.Enumeration ;
import java.util.List ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.sparql.modify.UsingList ;
import org.apache.jena.update.UpdateAction ;
import org.apache.jena.update.UpdateException ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.web.HttpSC ;

public class SPARQL_Update extends SPARQL_Protocol
{
    // Base URI used to isolate parsing from the current directory of the server.
    private static final String UpdateParseBase = Fuseki.BaseParserSPARQL ;
    private static final IRIResolver resolver = IRIResolver.create(UpdateParseBase) ;

    public SPARQL_Update()
    { super() ; }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpSC.BAD_REQUEST_400, "Attempt to perform SPARQL update by GET.  Use POST") ;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doCommon(request, response) ;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCommonHeadersForOptions(response) ;
        response.setHeader(HttpNames.hAllow, "OPTIONS,POST") ;
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }

    protected void doOptions(HttpAction action) {
        doOptions(action.request, action.response) ;
    }

    @Override
    protected void perform(HttpAction action) {
        ContentType ct = ActionLib.getContentType(action) ;
        if ( ct == null )
            ct = ctSPARQLUpdate ;

        if ( matchContentType(ctSPARQLUpdate, ct) ) {
            executeBody(action) ;
            return ;
        }
        if ( isHtmlForm(ct) ) {
            executeForm(action) ;
            return ;
        }
        ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: " + action.request.getContentType()) ;
    }

    protected static List<String> paramsForm = Arrays.asList(paramRequest, paramUpdate,
                                                             paramUsingGraphURI, paramUsingNamedGraphURI) ;
    protected static List<String> paramsPOST = Arrays.asList(paramUsingGraphURI, paramUsingNamedGraphURI) ;

    @Override
    protected void validate(HttpAction action) {
        HttpServletRequest request = action.request ;

        if ( HttpNames.METHOD_OPTIONS.equals(request.getMethod()) )
            return ;

        if ( ! HttpNames.METHOD_POST.equalsIgnoreCase(request.getMethod()) )
            ServletOps.errorMethodNotAllowed("SPARQL Update : use POST") ;

        ContentType ct = ActionLib.getContentType(action) ;
        if ( ct == null )
            ct = ctSPARQLUpdate ;

        if ( matchContentType(ctSPARQLUpdate, ct) ) {
            String charset = request.getCharacterEncoding() ;
            if ( charset != null && !charset.equalsIgnoreCase(charsetUTF8) )
                ServletOps.errorBadRequest("Bad charset: " + charset) ;
            validate(action, paramsPOST) ;
            return ;
        }

        if ( isHtmlForm(ct) ) {
            int x = countParamOccurences(request, paramUpdate) + countParamOccurences(request, paramRequest) ;
            if ( x == 0 )
                ServletOps.errorBadRequest("SPARQL Update: No 'update=' parameter") ;
            if ( x != 1 )
                ServletOps.errorBadRequest("SPARQL Update: Multiple 'update=' parameters") ;

            String requestStr = request.getParameter(paramUpdate) ;
            if ( requestStr == null )
                requestStr = request.getParameter(paramRequest) ;
            if ( requestStr == null )
                ServletOps.errorBadRequest("SPARQL Update: No update= in HTML form") ;
            validate(action, paramsForm) ;
            return ;
        }

        ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Must be "+contentTypeSPARQLUpdate+" or "+contentTypeHTMLForm+" (got "+ct.getContentType()+")") ;
    }

    protected void validate(HttpAction action, Collection<String> params) {
        if ( params != null ) {
            Enumeration<String> en = action.request.getParameterNames() ;
            for ( ; en.hasMoreElements() ; ) {
                String name = en.nextElement() ;
                if ( !params.contains(name) )
                    ServletOps.warning(action, "SPARQL Update: Unrecognized request parameter (ignored): "+name) ;
            }
        }
    }

    private void executeBody(HttpAction action) {
        InputStream input = null ;
        try { input = action.request.getInputStream() ; }
        catch (IOException ex) { ServletOps.errorOccurred(ex) ; }

        if ( action.verbose ) {
            // Verbose mode only .... capture request for logging (does not scale).
            byte[] bytes = IO.readWholeFile(input);
            input = new ByteArrayInputStream(bytes);
            try {
                String requestStr = Bytes.bytes2string(bytes) ;
                action.log.info(format("[%d] Update = %s", action.id, ServletOps.formatForLog(requestStr))) ;
            } catch (Exception ex) {
                action.log.info(format("[%d] Update = <failed to decode>", action.id)) ;
            }
        }

        execute(action, input) ;
        ServletOps.successNoContent(action) ;
    }

    private void executeForm(HttpAction action) {
        String requestStr = action.request.getParameter(paramUpdate) ;
        if ( requestStr == null )
            requestStr = action.request.getParameter(paramRequest) ;

        if ( action.verbose )
            action.log.info(format("[%d] Form update = \n%s", action.id, requestStr)) ;
        // A little ugly because we are taking a copy of the string, but hopefully shouldn't be too big if we are in this code-path
        // If we didn't want this additional copy, we could make the parser take a Reader in addition to an InputStream
        byte[] b = StrUtils.asUTF8bytes(requestStr) ;
        ByteArrayInputStream input = new ByteArrayInputStream(b);
        requestStr = null;  // free it early at least
        execute(action, input);
        ServletOps.successPage(action,"Update succeeded") ;
    }

    protected void execute(HttpAction action, InputStream input) {
        // OPTIONS
        if ( action.request.getMethod().equals(HttpNames.METHOD_OPTIONS) ) {
            // Share with update via SPARQL_Protocol.
            doOptions(action) ;
            return ;
        }

        UsingList usingList = processProtocol(action.request) ;

        // If the dsg is transactional, then we can parse and execute the update in a streaming fashion.
        // If it isn't, we need to read the entire update request before performing any updates, because
        // we have to attempt to make the request atomic in the face of malformed updates
        UpdateRequest req = null ;
        if (!action.isTransactional()) {
            try {
                req = UpdateFactory.read(usingList, input, UpdateParseBase, Syntax.syntaxARQ);
            }
            catch (UpdateException ex) { ServletOps.errorBadRequest(ex.getMessage()) ; return ; }
            catch (QueryParseException ex) { ServletOps.errorBadRequest(messageForQueryException(ex)) ; return ; }
        }

        action.beginWrite() ;
        try {
            if (req == null )
                UpdateAction.parseExecute(usingList, action.getActiveDSG(), input, UpdateParseBase, Syntax.syntaxARQ);
            else
                UpdateAction.execute(req, action.getActiveDSG()) ;
            action.commit() ;
        } catch (UpdateException ex) {
            action.abort() ;
            incCounter(action.getEndpoint().getCounters(), UpdateExecErrors) ;
            ServletOps.errorOccurred(ex.getMessage()) ;
        } catch (QueryParseException|QueryBuildException ex) {
            action.abort() ;
            // Counter inc'ed further out.
            ServletOps.errorBadRequest(messageForQueryException(ex)) ;
        } catch (Throwable ex) {
            if ( ! ( ex instanceof ActionErrorException ) )
            {
                try { action.abort() ; } catch (Exception ex2) {}
                ServletOps.errorOccurred(ex.getMessage(), ex) ;
            }
        } finally { action.end(); }
    }

    /* [It is an error to supply the using-graph-uri or using-named-graph-uri parameters
     * when using this protocol to convey a SPARQL 1.1 Update request that contains an
     * operation that uses the USING, USING NAMED, or WITH clause.]
     *
     * We will simply capture any using parameters here and pass them to the parser, which will be
     * responsible for throwing an UpdateException if the query violates the above requirement,
     * and will also be responsible for adding the using parameters to update queries that can
     * accept them.
     */
    private UsingList processProtocol(HttpServletRequest request) {
        UsingList toReturn = new UsingList();

        String[] usingArgs = request.getParameterValues(paramUsingGraphURI) ;
        String[] usingNamedArgs = request.getParameterValues(paramUsingNamedGraphURI) ;
        if ( usingArgs == null && usingNamedArgs == null )
            return toReturn;
        if ( usingArgs == null )
            usingArgs = new String[0] ;
        if ( usingNamedArgs == null )
            usingNamedArgs = new String[0] ;
        // Impossible.
//        if ( usingArgs.length == 0 && usingNamedArgs.length == 0 )
//            return ;

        for ( String nodeUri : usingArgs ) {
            toReturn.addUsing(createNode(nodeUri)) ;
        }
        for ( String nodeUri : usingNamedArgs ) {
            toReturn.addUsingNamed(createNode(nodeUri)) ;
        }

        return toReturn ;
    }

    private static Node createNode(String x) {
        try {
            IRI iri = resolver.resolve(x) ;
            return NodeFactory.createURI(iri.toString()) ;
        } catch (Exception ex)
        {
            ServletOps.errorBadRequest("SPARQL Update: bad IRI: "+x) ;
            return null ;
        }

    }
}
