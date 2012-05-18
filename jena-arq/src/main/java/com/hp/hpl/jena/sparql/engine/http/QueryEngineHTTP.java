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

package com.hp.hpl.jena.sparql.engine.http;

import java.io.ByteArrayInputStream ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.io.IO ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.resultset.CSVInput ;
import com.hp.hpl.jena.sparql.resultset.JSONInput ;
import com.hp.hpl.jena.sparql.resultset.XMLInput ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.FileManager ;


public class QueryEngineHTTP implements QueryExecution
{
    private static Logger log = LoggerFactory.getLogger(QueryEngineHTTP.class) ;
    
    public static final String QUERY_MIME_TYPE = WebContent.contentTypeSPARQLQuery ; // "application/sparql-query" ;
    private final Query query ;
    private final String queryString ;
    private final String service ;
    private final Context context ;
    
    //Params
    Params params = null ;
    
    // Protocol
    List<String> defaultGraphURIs = new ArrayList<String>() ;
    List<String> namedGraphURIs  = new ArrayList<String>() ;
    private String user = null ;
    private char[] password = null ;
    
    private boolean finished = false ;
    
    //Timeouts
    private long connectTimeout = 0;
    private TimeUnit connectTimeoutUnit = TimeUnit.MILLISECONDS;
    private long readTimeout = 0;
    private TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;
    
    //Compression Support
    private boolean allowGZip = true ;
    private boolean	allowDeflate = true;
    
    //Content Types
    private String selectContentType = WebContent.contentTypeResultsXML;
    private String askContentType = WebContent.contentTypeResultsXML;
    private String modelContentType = WebContent.contentTypeRDFXML;
    public static String[] supportedSelectContentTypes = new String []
    		{
    			WebContent.contentTypeResultsXML,
    			WebContent.contentTypeResultsJSON,
    			WebContent.contentTypeTextTSV,
    			WebContent.contentTypeTextCSV
    		};
    public static String[] supportedAskContentTypes = new String []
    		{
    			WebContent.contentTypeResultsXML,
    			WebContent.contentTypeJSON
    		};
    
    // Releasing HTTP input streams is important. We remember this for SELECT,
    // and will close when the engine is closed
    private InputStream retainedConnection = null;
    
    public QueryEngineHTTP(String serviceURI, Query query)
    { 
        this(serviceURI, query, query.toString()) ;
    }
    
    public QueryEngineHTTP(String serviceURI, String queryString)
    { 
        this(serviceURI, null, queryString) ;
    }

    private QueryEngineHTTP(String serviceURI, Query query, String queryString)
    { 
        this.query = query ;
        this.queryString = queryString ;
        this.service = serviceURI ;
        // Copy the global context to freeze it.
        this.context = new Context(ARQ.getContext()) ;
    }
    
//    public void setParams(Params params)
//    { this.params = params ; }
    
    // Meaning-less
    @Override
    public void setFileManager(FileManager fm)
    { throw new UnsupportedOperationException("FileManagers do not apply to remote query execution") ; }  

    @Override
    public void setInitialBinding(QuerySolution binding)
    { throw new UnsupportedOperationException("Initial bindings not supported for remote queries, consider using a ParameterizedSparqlString to prepare a query for remote execution") ; }
    
    public void setInitialBindings(ResultSet table)
    { throw new UnsupportedOperationException("Initial bindings not supported for remote queries, consider using a ParameterizedSparqlString to prepare a query for remote execution") ; }
    
    /**  @param defaultGraphURIs The defaultGraphURIs to set. */
    public void setDefaultGraphURIs(List<String> defaultGraphURIs)
    {
        this.defaultGraphURIs = defaultGraphURIs ;
    }

    /**  @param namedGraphURIs The namedGraphURIs to set. */
    public void setNamedGraphURIs(List<String> namedGraphURIs)
    {
        this.namedGraphURIs = namedGraphURIs ;
    }
    
    /**
     * Sets whether the HTTP request will specify Accept-Encoding: gzip
     */
    public void setAllowGZip(boolean allowed)
    {
    	allowGZip = allowed;
    }
    
    /**
     * Sets whether the HTTP requests will specify Accept-Encoding: deflate
     */
    public void setAllowDeflate(boolean allowed)
    {
    	allowDeflate = allowed;
    }

    public void addParam(String field, String value)
    {
        if ( params == null )
            params = new Params() ;
        params.addParam(field, value) ;
    }
    
    /** @param defaultGraph The defaultGraph to add. */
    public void addDefaultGraph(String defaultGraph)
    {
        if ( defaultGraphURIs == null )
            defaultGraphURIs = new ArrayList<String>() ;
        defaultGraphURIs.add(defaultGraph) ;
    }

    /** @param name The URI to add. */
    public void addNamedGraph(String name)
    {
        if ( namedGraphURIs == null )
            namedGraphURIs = new ArrayList<String>() ;
        namedGraphURIs.add(name) ;
    }
    
    /** Set user and password for basic authentication.
     *  After the request is made (one of the exec calls), the application
     *  can overwrite the password array to remove details of the secret.
     * @param user
     * @param password
     */
    public void setBasicAuthentication(String user, char[] password)
    {
        this.user = user ;
        this.password = password ;
    }
    
    @Override
    public ResultSet execSelect()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(selectContentType) ;
        InputStream in = httpQuery.exec() ;
        
        if ( false )
        {
            byte b[] = IO.readWholeFile(in) ;
            String str = new String(b) ;
            System.out.println(str) ;
            in = new ByteArrayInputStream(b) ; 
        }
        
        retainedConnection = in; // This will be closed on close()
        
        //TODO: Find a way to auto-detect how to create the ResultSet based on the content type in use
        
        //Don't assume the endpoint actually gives back the content type we asked for
        String actualContentType = httpQuery.getContentType();
        
        //If the server fails to return a Content-Type then we will assume
        //the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals(""))
        {
        	actualContentType = selectContentType;
        }
        
        if (actualContentType.equals(WebContent.contentTypeResultsXML))
            return ResultSetFactory.fromXML(in);
        if (actualContentType.equals(WebContent.contentTypeResultsJSON))
            return ResultSetFactory.fromJSON(in); 
        if (actualContentType.equals(WebContent.contentTypeTextTSV))
            return ResultSetFactory.fromTSV(in);
        if (actualContentType.equals(WebContent.contentTypeTextCSV))
            return CSVInput.fromCSV(in);
        throw new QueryException("Endpoint returned Content-Type: " + actualContentType + " which is not currently supported for SELECT queries");
    }

    @Override
    public Model execConstruct()                   { return execConstruct(GraphFactory.makeJenaDefaultModel()) ; }
    
    @Override
    public Model execConstruct(Model model)        { return execModel(model) ; }
    
    @Override
    public Iterator<Triple> execConstructTriples() { return execTriples() ; }

    @Override
    public Model execDescribe()                    { return execDescribe(GraphFactory.makeJenaDefaultModel()) ; }
    
    @Override
    public Model execDescribe(Model model)         { return execModel(model) ; }
    
    @Override
    public Iterator<Triple> execDescribeTriples()  { return execTriples() ; }

    private Model execModel(Model model)
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(modelContentType) ;
        InputStream in = httpQuery.exec() ;
        
        //Don't assume the endpoint actually gives back the content type we asked for
        String actualContentType = httpQuery.getContentType();
        
        //If the server fails to return a Content-Type then we will assume
        //the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals(""))
        {
        	actualContentType = modelContentType;
        }
        
        //Try to select language appropriately here based on the model content type
        Lang lang = WebContent.contentTypeToLang(actualContentType);
        if (!lang.isTriples()) throw new QueryException("Endpoint returned Content Type: " + actualContentType + " which is not a valid RDF Graph syntax");
        model.read(in, null, lang.getName()) ; 
        
        return model ;
    }
    
    private Iterator<Triple> execTriples()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(modelContentType) ;
        InputStream in = httpQuery.exec() ;
        
        //Don't assume the endpoint actually gives back the content type we asked for
        String actualContentType = httpQuery.getContentType();
        
        //If the server fails to return a Content-Type then we will assume
        //the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals(""))
        {
            actualContentType = modelContentType;
        }
        
        //Try to select language appropriately here based on the model content type
        Lang lang = WebContent.contentTypeToLang(actualContentType);
        if (!lang.isTriples()) throw new QueryException("Endpoint returned Content Type: " + actualContentType + " which is not a valid RDF Graph syntax");
        
        return RiotReader.createIteratorTriples(in, lang, null);
    }
    
    @Override
    public boolean execAsk()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(askContentType) ;
        InputStream in = httpQuery.exec() ;

        try {
            //Don't assume the endpoint actually gives back the content type we asked for
            String actualContentType = httpQuery.getContentType();
            
            //If the server fails to return a Content-Type then we will assume
            //the server returned the type we asked for
            if (actualContentType == null || actualContentType.equals(""))
            {
            	actualContentType = askContentType;
            }
        	
            //Parse the result appropriately depending on the selected content type
            if (askContentType.equals(WebContent.contentTypeResultsXML))
                return XMLInput.booleanFromXML(in) ;
            if (askContentType.equals(WebContent.contentTypeResultsJSON))
                return JSONInput.booleanFromJSON(in) ;
            throw new QueryException("Endpoint returned Content-Type: " + actualContentType + " which is not currently supported for ASK queries");
        } finally {
            // Ensure connection is released
            try { in.close(); }
            catch (java.io.IOException e) { log.warn("Failed to close connection", e); }
        }
    }

    @Override
    public Context getContext() { return context ; }
    
    @Override public Dataset getDataset()   { return null ; }

    // This may be null - if we were created form a query string, 
    // we don't guarantee to parse it so we let through non-SPARQL
    // extensions to the far end. 
    @Override public Query getQuery()       { return query ; }
    
    @Override
    public void setTimeout(long readTimeout)
    {
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = TimeUnit.MILLISECONDS;
    }

    @Override
    public void setTimeout(long readTimeout, long connectTimeout)
    {
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = TimeUnit.MILLISECONDS;
        this.connectTimeout = connectTimeout;
        this.connectTimeoutUnit = TimeUnit.MILLISECONDS;
    }


    @Override
    public void setTimeout(long readTimeout, TimeUnit timeoutUnits)
    {
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = timeoutUnits;
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2)
    {
        this.readTimeout = timeout1;
        this.readTimeoutUnit = timeUnit1;
        this.connectTimeout = timeout2;
        this.connectTimeoutUnit = timeUnit2;
    }
    
    private HttpQuery makeHttpQuery()
    {
        // Also need to tie to ResultSet returned which is streamed back if StAX.
        if ( finished )
            throw new ARQException("HTTP execution already closed") ;
        
        HttpQuery httpQuery = new HttpQuery(service) ;
        httpQuery.merge(getServiceParams(service, context)) ;
        httpQuery.addParam(HttpParams.pQuery, queryString );
        
        for ( Iterator<String> iter = defaultGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String dft = iter.next() ;
            httpQuery.addParam(HttpParams.pDefaultGraph, dft) ;
        }
        for ( Iterator<String> iter = namedGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            httpQuery.addParam(HttpParams.pNamedGraph, name) ;
        }
        
        if ( params != null )
            httpQuery.merge(params) ;
        
        if (allowGZip)
        	httpQuery.setAllowGZip(true);

        if (allowDeflate)
        	httpQuery.setAllowDeflate(true);
        
        httpQuery.setBasicAuthentication(user, password) ;
        
        //Apply timeouts
        if (connectTimeout > 0)
        {
        	httpQuery.setConnectTimeout((int)connectTimeoutUnit.toMillis(connectTimeout));
        }
        if (readTimeout > 0)
        {
        	httpQuery.setReadTimeout((int)readTimeoutUnit.toMillis(readTimeout));
        }
        
        return httpQuery ;
    }

    
    // This is to allow setting additional/optional query parameters on a per SERVICE level, see: JENA-195
    protected static Params getServiceParams(String serviceURI, Context context) throws QueryExecException
    {
        Params params = new Params();
        @SuppressWarnings("unchecked")
        Map<String, Map<String,List<String>>> serviceParams = (Map<String, Map<String,List<String>>>)context.get(ARQ.serviceParams) ;
        if ( serviceParams != null ) 
        {
            Map<String,List<String>> paramsMap = serviceParams.get(serviceURI) ;
            if ( paramsMap != null )
            {
                for (String param : paramsMap.keySet()) 
                {   
                    if ( HttpParams.pQuery.equals(param) ) 
                        throw new QueryExecException("ARQ serviceParams overrides the 'query' SPARQL protocol parameter") ;

                    List<String> values = paramsMap.get(param) ;
                    for (String value : values) 
                        params.addParam(param, value) ;
                }
            }            
        }
        return params;
    }

    public void cancel() { finished = true ; }
    
    @Override
    public void abort() { try { close() ; } catch (Exception ex) {} }

    @Override
    public void close() {
        finished = false ;
        if (retainedConnection != null) {
            try { retainedConnection.close(); }
            catch (java.io.IOException e) { log.warn("Failed to close connection", e); }
            finally { retainedConnection = null; }
        }
    }

//    public boolean isActive() { return false ; }
    
    @Override
    public String toString()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        return "GET "+httpQuery.toString() ;
    }
    
    /**
     * Sets the Content Type for SELECT queries provided that the format is supported
     * @param contentType
     */
    public void setSelectContentType(String contentType)
    {
    	boolean ok = false;
    	for (String supportedType : supportedSelectContentTypes)
    	{
    		if (supportedType.equals(contentType))
    		{
    			ok = true;
    			break;
    		}
    	}
    	if (!ok) throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not a supported SELECT results format");
    	selectContentType = contentType;
    }
    
    /**
     * Sets the Content Type for ASK queries provided that the format is supported
     * @param contentType
     */
    public void setAskContentType(String contentType)
    {
    	boolean ok = false;
    	for (String supportedType : supportedAskContentTypes)
    	{
    		if (supportedType.equals(contentType))
    		{
    			ok = true;
    			break;
    		}
    	}
    	if (!ok) throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not a supported ASK results format");
    	askContentType = contentType;
    }
    
    /**
     * Sets the Content Type for CONSTRUCT/DESCRIBE queries provided that the format is supported
     * @param contentType
     */
    public void setModelContentType(String contentType)
    {
    	//Check that this is a valid setting
    	Lang lang = WebContent.contentTypeToLang(contentType);
    	if (lang == null) throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not supported by RIOT");
    	if (!lang.isTriples()) throw new IllegalArgumentException("Given Content Type '" + contentType + " is not a RDF Graph format");
    	
    	modelContentType = contentType;
    }
}
