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
import static org.apache.jena.fuseki.HttpNames.* ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.* ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.web.HttpSC ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;

public abstract class SPARQL_Query extends SPARQL_Protocol
{
    public SPARQL_Query(boolean verbose)
    { super(verbose) ; }

    
    public SPARQL_Query()
    { this(false) ; }

    // Choose REST verbs to support.
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    // HEAD
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
    {
        //response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS,POST");
        setCommonHeaders(response) ;
        response.setHeader(HttpNames.hAllow, "GET,OPTIONS,POST");
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }
    
    @Override
    protected final void perform(HttpAction action)
    {
        // GET
        if ( action.request.getMethod().equals(HttpNames.METHOD_GET) )
        {
            executeWithParameter(action) ;
            return ;
        }

        MediaType ct = FusekiLib.contentType(action.request) ;
        String incoming = ct.getContentType() ;

        // POST application/sparql-query
        if (WebContent.contentTypeSPARQLQuery.equals(incoming))
        {
            executeBody(action) ;
            return ;
        }
        // POST application/x-www-form-url
        if (WebContent.contentTypeForm.equals(incoming))
        {
            executeWithParameter(action) ;
            return ;
        }

        error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: "+incoming) ;
    }

    // All the params we support

    protected static List<String> allParams  = Arrays.asList(paramQuery, 
                                                             paramDefaultGraphURI, paramNamedGraphURI, 
                                                             paramQueryRef,
                                                             paramStyleSheet,
                                                             paramAccept,
                                                             paramOutput1, paramOutput2, 
                                                             paramCallback, 
                                                             paramForceAccept,
                                                             paramTimeout) ;
    
    @Override
    protected void startRequest(HttpAction action)
    {
    }
    
    /** Called to validate arguments */
    @Override
    protected void validate(HttpAction action)
    {
        String method = action.request.getMethod().toUpperCase(Locale.ENGLISH) ;
        
        if ( ! HttpNames.METHOD_POST.equals(method) && ! HttpNames.METHOD_GET.equals(method) )
            errorMethodNotAllowed("Not a GET or POST request") ;
            
        if ( HttpNames.METHOD_GET.equals(method) && action.request.getQueryString() == null )
        {
            warning("Service Description / SPARQL Query / "+action.request.getRequestURI()) ;
            errorNotFound("Service Description: "+action.request.getRequestURI()) ;
        }
        
        // Use of the dataset describing parameters is check later.
        try {
            validateParams(action.request, allParams) ;
            validateRequest(action) ; 
        } catch (ActionErrorException ex) { inc(action.desc.countQueryBadSyntax) ; throw ex ; } 
        // Query not yet parsed.
    }
    
    /** Validate the request after checking HTTP method and HTTP Parameters */ 
    protected abstract void validateRequest(HttpAction action) ;
    
    /** Helper for validating request */
    protected void validateParams(HttpServletRequest request, Collection<String> params)
    {
        MediaType ct = FusekiLib.contentType(request) ;
        boolean mustHaveQueryParam = true ;
        if ( ct != null )
        {
            String incoming = ct.getContentType() ;
            
            if ( WebContent.contentTypeSPARQLQuery.equals(incoming) )
            {
                mustHaveQueryParam = false ;
                //error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Unofficial "+WebContent.contentTypeSPARQLQuery+" not supported") ;
            }
            else if ( WebContent.contentTypeForm.equals(incoming) ) {}
            else
                error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Unsupported: "+incoming) ;
        }
        
        // GET/POST of a form at this point.
        
        if ( mustHaveQueryParam )
        {
            int N = countParamOccurences(request, paramQuery) ; 
            
            if ( N == 0 ) errorBadRequest("SPARQL Query: No 'query=' parameter") ;
            if ( N > 1 ) errorBadRequest("SPARQL Query: Multiple 'query=' parameters") ;
            
            // application/sparql-query does not use a query param.
            String queryStr = request.getParameter(HttpNames.paramQuery) ;
            
            if ( queryStr == null )
                errorBadRequest("SPARQL Query: No query specified (no 'query=' found)") ;
            if ( queryStr.isEmpty() )
                errorBadRequest("SPARQL Query: Empty query string") ;
        }

        if ( params != null )
        {
            Enumeration<String> en = request.getParameterNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement() ;
                if ( ! params.contains(name) )
                    warning("SPARQL Query: Unrecognize request parameter (ignored): "+name) ;
            }
        }
    }

    private void executeWithParameter(HttpAction action)
    {
        String queryString = action.request.getParameter(paramQuery) ;
        execute(queryString, action) ;
    }

    private void executeBody(HttpAction action)
    {
        String queryString = null ;
        try { 
            InputStream input = action.request.getInputStream() ; 
            queryString = IO.readWholeFileAsUTF8(input) ;
        }
        catch (IOException ex) { errorOccurred(ex) ; }
        execute(queryString, action) ;
    }

    private void execute(String queryString, HttpAction action)
    {
        String queryStringLog = formatForLog(queryString) ;
        if ( super.verbose_debug || action.verbose )
            log.info(format("[%d] Query = \n%s", action.id, queryString));
        else
            log.info(format("[%d] Query = %s", action.id, queryStringLog));

        Query query = null ;
        try {
            // NB syntax is ARQ (a superset of SPARQL)
            query = QueryFactory.create(queryString, Syntax.syntaxARQ) ;
            queryStringLog = formatForLog(query) ;
            validateQuery(action, query) ;
        } catch (ActionErrorException ex) {
            inc(action.desc.countQueryBadSyntax) ;
            throw ex ;
        } catch (QueryParseException ex) {
            inc(action.desc.countQueryBadSyntax) ;
            errorBadRequest("Parse error: \n" + queryString + "\n\r" + messageForQPE(ex)) ;
        }
        // Should not happen.
        catch (QueryException ex) {
            inc(action.desc.countQueryBadSyntax) ;
            errorBadRequest("Error: \n" + queryString + "\n\r" + ex.getMessage()) ;
        }
        
        // Assumes finished whole thing by end of sendResult. 
        action.beginRead() ;
        QueryExecution qExec = null ;
        try {
            Dataset dataset = decideDataset(action, query, queryStringLog) ; 
            qExec = createQueryExecution(query, dataset) ;
            SPARQLResult result = executeQuery(action, qExec, query, queryStringLog) ;
            
            // Deals with exceptions itself.
            sendResults(action, result, query.getPrologue()) ;
            inc(action.desc.countQueryOK) ;
        } 
        catch (QueryCancelledException ex) { inc(action.desc.countQueryTimeout) ; throw ex ; } 
        catch (QueryExecException ex)      { inc(action.desc.countQueryBadExecution) ; throw ex ; } 
        finally { 
            if ( qExec != null )
                qExec.close() ;
            action.endRead() ;
        }
    }

    /** Check the query - if unacceptable, throw ActionErrorException or call super.error */
    protected abstract void validateQuery(HttpAction action, Query query) ;

    protected QueryExecution createQueryExecution(Query query, Dataset dataset)
    {
        return QueryExecutionFactory.create(query, dataset) ;
    }

    protected SPARQLResult executeQuery(HttpAction action, QueryExecution qExec, Query query, String queryStringLog)
    {
        setAnyTimeouts(qExec, action);

        if ( query.isSelectType() )
        {
            ResultSet rs = qExec.execSelect() ;
            
            // Force some query execution now.
            //
            // If the timeout-first-row goes off, the output stream has not 
            // been started so the HTTP error code is sent. 
            //
            // This also forces the query to do something that should touch any underlying database,
            // and hence ensure the communications layer is working.
            // MySQL can time out after 8 hours of an idle connection
            
            rs.hasNext() ;

            // If we wanted perfect query time cancellation, we could consume the result now
            // to see if the timeout-end-of-query goes off.  
            // Not necessary if we are inside a read transaction or lock until the end of sending results.
            
            //rs = ResultSetFactory.copyResults(rs) ;

            log.info(format("[%d] exec/select", action.id)) ;
            return new SPARQLResult(rs) ;
        }

        if ( query.isConstructType() )
        {
            Model model = qExec.execConstruct() ;
            log.info(format("[%d] exec/construct", action.id)) ;
            return new SPARQLResult(model) ;
        }

        if ( query.isDescribeType() )
        {
            Model model = qExec.execDescribe() ;
            log.info(format("[%d] exec/describe",action.id)) ;
            return new SPARQLResult(model) ;
        }

        if ( query.isAskType() )
        {
            boolean b = qExec.execAsk() ;
            log.info(format("[%d] exec/ask",action.id)) ;
            return new SPARQLResult(b) ;
        }

        errorBadRequest("Unknown query type - "+queryStringLog) ;
        return null ;
    }

    private void setAnyTimeouts(QueryExecution qexec, HttpAction action) {
        if (!(action.getDatasetRef().allowTimeoutOverride))
            return;

        long desiredTimeout = Long.MAX_VALUE;
        String timeoutHeader = action.request.getHeader("Timeout");
        String timeoutParameter = action.request.getParameter("timeout");
        if (timeoutHeader != null) {
            try {
                desiredTimeout = (int) Float.parseFloat(timeoutHeader) * 1000;
            } catch (NumberFormatException e) {
                throw new FusekiException("Timeout header must be a number", e);
            }
        } else if (timeoutParameter != null) {
            try {
                desiredTimeout = (int) Float.parseFloat(timeoutParameter) * 1000;
            } catch (NumberFormatException e) {
                throw new FusekiException("timeout parameter must be a number", e);
            }
        }

        desiredTimeout = Math.min(action.getDatasetRef().maximumTimeoutOverride, desiredTimeout);
        if (desiredTimeout != Long.MAX_VALUE)
            qexec.setTimeout(desiredTimeout);
    }

    protected abstract Dataset decideDataset(HttpAction action, Query query, String queryStringLog) ;

    protected void sendResults(HttpAction action, SPARQLResult result, Prologue qPrologue)
    {
        if ( result.isResultSet() )
            ResponseResultSet.doResponseResultSet(action, result.getResultSet(), qPrologue) ;
        else if ( result.isGraph() )
            ResponseModel.doResponseModel(action, result.getModel()) ;
        else if ( result.isBoolean() )
            ResponseResultSet.doResponseResultSet(action, result.getBooleanResult()) ;
        else
            errorOccurred("Unknown or invalid result type") ;
    }
    
    private String formatForLog(Query query)
    {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        out.setFlatMode(true) ;
        query.serialize(out) ;
        return out.asString() ;
    }
        
    private String getRemoteString(String queryURI)
    {
        return HttpOp.execHttpGet(queryURI, null) ;
    }

}
