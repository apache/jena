/**
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
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.NotImplemented ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.resultset.XMLInput ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileManager ;


public class QueryEngineHTTP implements QueryExecution
{
    private static Logger log = LoggerFactory.getLogger(QueryEngineHTTP.class) ;
    
    public static final String QUERY_MIME_TYPE = "application/sparql-query" ;
    String queryString ;
    String service ;
    Context context = null ;
    
    //Params
    Params params = null ;
    
    // Protocol
    List<String> defaultGraphURIs = new ArrayList<String>() ;
    List<String> namedGraphURIs  = new ArrayList<String>() ;
    private String user = null ;
    private char[] password = null ;
    
    private boolean finished = false ;
    
    // Releasing HTTP input streams is important. We remember this for SELECT,
    // and will close when the engine is closed
    private InputStream retainedConnection = null;
    
    public QueryEngineHTTP(String serviceURI, Query query)
    { 
        this(serviceURI, query.toString()) ;
    }
    
    public QueryEngineHTTP(String serviceURI, String queryString)
    { 
        this.queryString = queryString ;
        service = serviceURI ;
        // Copy the global context to freeze it.
        context = new Context(ARQ.getContext()) ;
    }

//    public void setParams(Params params)
//    { this.params = params ; }
    
    // Meaning-less
    @Override
    public void setFileManager(FileManager fm)
    { throw new UnsupportedOperationException("FileManagers do not apply to remote query execution") ; }  

    @Override
    public void setInitialBinding(QuerySolution binding)
    { throw new UnsupportedOperationException("Initial bindings not supported for remote queries") ; }
    
    public void setInitialBindings(ResultSet table)
    { throw new UnsupportedOperationException("Initial bindings not supported for remote queries") ; }
    
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
        // TODO Allow other content types.
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        
        if ( false )
        {
            byte b[] = IO.readWholeFile(in) ;
            String str = new String(b) ;
            System.out.println(str) ;
            in = new ByteArrayInputStream(b) ; 
        }
        
        ResultSet rs = ResultSetFactory.fromXML(in) ;
        retainedConnection = in; // This will be closed on close()
        return rs ;
    }

    @Override
    public Model execConstruct()             { return execConstruct(GraphFactory.makeJenaDefaultModel()) ; }
    
    @Override
    public Model execConstruct(Model model)  { return execModel(model) ; }

    @Override
    public Model execDescribe()              { return execDescribe(GraphFactory.makeJenaDefaultModel()) ; }
    
    @Override
    public Model execDescribe(Model model)   { return execModel(model) ; }

    private Model execModel(Model model)
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeRDFXML) ;
        InputStream in = httpQuery.exec() ;
        model.read(in, null) ;
        return model ;
    }
    
    @Override
    public boolean execAsk()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        boolean result = XMLInput.booleanFromXML(in) ;
        // Ensure connection is released
        try { in.close(); }
        catch (java.io.IOException e) { log.warn("Failed to close connection", e); }
        return result;
    }

    @Override
    public Context getContext() { return context ; }
    
    @Override
    public void setTimeout(long timeout)
    {
        throw new NotImplemented("Not implemented yet - please send a patch to the Apache Jena project : https://issues.apache.org/jira/browse/JENA-56") ;
    }

    @Override
    public void setTimeout(long timeout1, long timeout2)
    {
        throw new NotImplemented("Not implemented yet - please send a patch to the Apache Jena project : https://issues.apache.org/jira/browse/JENA-56") ;
    }


    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits)
    {
        throw new NotImplemented("Not implemented yet - please send a patch to the Apache Jena project : https://issues.apache.org/jira/browse/JENA-56") ;
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2)
    {
        throw new NotImplemented("Not implemented yet - please send a patch to the Apache Jena project : https://issues.apache.org/jira/browse/JENA-56") ;
    }


    
    private HttpQuery makeHttpQuery()
    {
        // Also need to tie to ResultSet returned which is streamed back if StAX.
        if ( finished )
            throw new ARQException("HTTP execution already closed") ;
        
        HttpQuery httpQuery = new HttpQuery(service) ;
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
        
        httpQuery.setBasicAuthentication(user, password) ;
        return httpQuery ;
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

    @Override
    public Dataset getDataset()
    {
        return null ;
    }
}
