/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;
import static org.openjena.fuseki.HttpNames.paramQuery ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Enumeration ;
import java.util.Set ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.migrate.WebIO ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;

public abstract class SPARQL_Query extends SPARQL_ServletBase
{
    private static Logger log = LoggerFactory.getLogger(SPARQL_Query.class) ;
    
    protected class HttpActionQuery extends HttpAction {
        public HttpActionQuery(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, dsg, request, response, verbose) ;
        }
    }
    
    public SPARQL_Query(boolean verbose)
    { super(PlainRequestFlag.DIFFERENT, verbose) ; }

    
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
        response.setHeader(HttpNames.hAllow, "GET,OPTIONS,POST");
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }
    
    @Override
    protected void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response)
    {
        validate(request) ;
        HttpActionQuery action = new HttpActionQuery(id, dsg, request, response, verbose_debug) ;
        if ( request.getMethod().equals(HttpNames.METHOD_GET) )
        {
            executeForm(action) ;
            return ;
        }
        // POST
        String incoming = request.getContentType() ;
        if (WebContent.contentTypeSPARQLQuery.equals(incoming))
        {
            executeBody(action) ;
            return ;
        }
        if (WebContent.contentTypeForm.equals(incoming))
        {
            executeForm(action) ;
            return ;
        }

        error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: "+incoming) ;
    }

    @Override
    protected boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response)
    {
        if ( HttpNames.METHOD_POST.equals(request.getMethod().toUpperCase()) )
            return true ;
        
        if ( ! HttpNames.METHOD_GET.equals(request.getMethod().toUpperCase()) )
        {
            errorNotImplemented("Not a GET or POST request") ;
            return false ;
        }
        log.warn("Service Description / SPARQL Query") ;
        errorNotFound("Service Description") ;
        return false ;
    }

    // (1) Param to constructor.
    // (2) DRY : to super class.
    static String[] tails = { HttpNames.ServiceQuery, HttpNames.ServiceQueryAlt } ;
    
    @Override
    protected String mapRequestToDataset(String uri)
    {
        for ( String tail : tails )
        {
            String x = mapRequestToDataset(uri, tail) ;
            if ( x != null )
                return x ;
        }
        return uri ; 
    }
    
//    // All the params we support
//    private static String[] params_ = { paramQuery, paramDefaultGraphURI, paramNamedGraphURI, 
//                                        paramQueryRef,
//                                        paramStyleSheet,
//                                        paramAccept,
//                                        paramOutput1, paramOutput2, 
//                                        paramCallback, 
//                                        paramForceAccept } ;
//    private static Set<String> params = new HashSet<String>(Arrays.asList(params_)) ;
//    
    /** Called to validate arguments */
    protected abstract void validate(HttpServletRequest request) ;
    
    /** Helper for validating request */
    protected void validate(HttpServletRequest request, Set<String> params)
    {
        String incoming = request.getContentType() ;
        if ( incoming != null )
        {
        
            if ( WebContent.contentTypeSPARQLQuery.equals(incoming) )
                error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Unofficial "+WebContent.contentTypeSPARQLQuery+" not supported") ;
                
            if ( ! WebContent.contentTypeForm.equals(incoming) )
                error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Unsupported: "+incoming) ;
        }
        
        // GET/POST of a form at this point.
        String queryStr = request.getParameter(HttpNames.paramQuery) ;
        
        if ( queryStr == null )
            errorBadRequest("SPARQL Query: No query specificied (no 'query=' found)") ;

        if ( params != null )
        {
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getParameterNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement() ;
                if ( ! params.contains(name) )
                    errorBadRequest("SPARQL Query: Unrecognize request parameter: "+name) ;
            }
        }
    }

    private void executeForm(HttpActionQuery action)
    {
        String queryString = action.request.getParameter(paramQuery) ;
        execute(queryString, action) ;
    }

    private void executeBody(HttpActionQuery action)
    {
        String queryString = null ;
        try { 
            InputStream input = action.request.getInputStream() ; 
            queryString = IO.readWholeFileAsUTF8(input) ;
        }
        catch (IOException ex) { errorOccurred(ex) ; }
        execute(queryString, action) ;
    }

    
    private void execute(String queryString, HttpActionQuery action)
    {
        String queryStringLog = formatForLog(queryString) ;
        serverlog.info(format("[%d] Query = %s", action.id, queryString));
        Query query = null ;
        try {
            // NB syntax is ARQ (a superset of SPARQL)
            query = QueryFactory.create(queryString, Syntax.syntaxARQ) ;
            queryStringLog = formatForLog(query) ;
        } catch (QueryException ex)
        {
            errorBadRequest("Parse error: \n"+queryString +"\n\r" + ex.getMessage()) ;
        }
        
        validateQuery(action, query) ;

        if ( query.hasDatasetDescription() )
            errorBadRequest("Query has FROM/FROM NAMED") ;
        
        // Assumes finished whole thing by end of sendResult. 
        action.beginRead() ;
        try {
            SPARQLResult result = executeQuery(action, query, queryStringLog) ;
            sendResults(action, result) ;
        } finally { action.endRead() ; }
    }

    /** Check the query - throw ActionErrorException or call super.error* */
    protected abstract void validateQuery(HttpActionQuery action, Query query) ;

    protected QueryExecution createQueryExecution(Query query, Dataset dataset)
    {
        return QueryExecutionFactory.create(query, dataset) ;
    }

    private SPARQLResult executeQuery(HttpActionQuery action, Query query, String queryStringLog)
    {
        Dataset dataset = decideDataset(action, query, queryStringLog) ; 
        QueryExecution qexec = createQueryExecution(query, dataset) ;

        if ( query.isSelectType() )
        {
            ResultSet rs = qexec.execSelect() ;
            
            // Force some query execution now.
            // Do this to force the query to do something that should touch any underlying database,
            // and hence ensure the communications layer is working.  MySQL can time out after  
            // 8 hours of an idle connection
            rs.hasNext() ;

//            // Not necessary if we are inside readlock under end of sending results. 
//            rs = ResultSetFactory.copyResults(rs) ;

            serverlog.info(format("[%d] OK/select", action.id)) ;
            return new SPARQLResult(rs) ;
        }

        if ( query.isConstructType() )
        {
            Model model = qexec.execConstruct() ;
            serverlog.info(format("[%d] OK/construct", action.id)) ;
            return new SPARQLResult(model) ;
        }

        if ( query.isDescribeType() )
        {
            Model model = qexec.execDescribe() ;
            serverlog.info(format("[%d] OK/describe",action.id)) ;
            return new SPARQLResult(model) ;
        }

        if ( query.isAskType() )
        {
            boolean b = qexec.execAsk() ;
            serverlog.info(format("[%d] OK/ask",action.id)) ;
            return new SPARQLResult(b) ;
        }

        errorBadRequest("Unknown query type - "+queryStringLog) ;
        return null ;
    }

    protected abstract Dataset decideDataset(HttpActionQuery action, Query query, String queryStringLog) ;

    protected void sendResults(HttpActionQuery action, SPARQLResult result)
    {
        if ( result.isResultSet() )
            ResponseQuery.doResponseResultSet(result.getResultSet(), null, action.request, action.response) ;
        else if ( result.isGraph() )
            ResponseQuery.doResponseModel(result.getModel(), action.request, action.response) ;
        else if ( result.isBoolean() )
            // Make different?
            ResponseQuery.doResponseResultSet(null, result.getBooleanResult(), action.request, action.response) ;
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
        
    /**
     * @param queryURI
     * @return
     */
    private String getRemoteString(String queryURI)
    {
        return WebIO.exec_get(queryURI) ;
    }

}

/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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