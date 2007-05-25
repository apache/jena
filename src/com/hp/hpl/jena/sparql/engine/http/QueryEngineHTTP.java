/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.resultset.XMLInput;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.util.FileManager;


public class QueryEngineHTTP implements QueryExecution
{
    //TODO Check return content type.
    String queryString ;
    String service ;
    Context parameters = new Context() ;
    
    // Protocol
    List defaultGraphURIs = new ArrayList() ;
    List namedGraphURIs  = new ArrayList() ;
    
    public QueryEngineHTTP(String serviceURI, Query query)
    { 
        queryString = query.toString() ;
        service = serviceURI ;
    }
    
    public QueryEngineHTTP(String serviceURI, String queryString)
    { 
        this.queryString = queryString ;
        service = serviceURI ;
    }
    
    //public Query getQuery() { return query ; }
    
    public void setFileManager(FileManager fm) { return ; } 

    public void setInitialBinding(QuerySolution binding) { throw new QueryExecException("Initial bindings not supportd for remote queries") ; }

    
    /**  @param defaultGraphURIs The defaultGraphURIs to set. */
    public void setDefaultGraphURIs(List defaultGraphURIs)
    {
        this.defaultGraphURIs = defaultGraphURIs ;
    }

    /**  @param namedGraphURIs The namedGraphURIs to set. */
    public void setNamedGraphURIs(List namedGraphURIs)
    {
        this.namedGraphURIs = namedGraphURIs ;
    }

    /** @param defaultGraph The defaultGraph to add. */
    public void addDefaultGraph(String defaultGraph)
    {
        if ( defaultGraphURIs == null )
            defaultGraphURIs = new ArrayList() ;
        defaultGraphURIs.add(defaultGraph) ;
    }

    /** @param name The URI to add. */
    public void addNamedGraph(String name)
    {
        if ( namedGraphURIs == null )
            namedGraphURIs = new ArrayList() ;
        namedGraphURIs.add(name) ;
    }
    
    public ResultSet execSelect()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        ResultSet rs = ResultSetFactory.fromXML(in) ;
        return rs ;
    }

    public Model execConstruct()             { return execConstruct(GraphUtils.makeJenaDefaultModel()) ; }
    
    public Model execConstruct(Model model)  { return execModel(model) ; }

    public Model execDescribe()              { return execDescribe(GraphUtils.makeJenaDefaultModel()) ; }
    
    public Model execDescribe(Model model)   { return execModel(model) ; }

    private Model execModel(Model model)
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeRDFXML) ;
        InputStream in = httpQuery.exec() ;
        model.read(in, null) ;
        return model ;
    }
    
    public boolean execAsk()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        return XMLInput.booleanFromXML(in) ;
    }

    public Context getContext() { return parameters ; }
    
    private HttpQuery makeHttpQuery()
    {
        HttpQuery httpQuery = new HttpQuery(service) ;
        httpQuery.addParam(HttpParams.pQuery, queryString );
        
        for ( Iterator iter = defaultGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String dft = (String)iter.next() ;
            httpQuery.addParam(HttpParams.pDefaultGraph, dft) ;
        }
        for ( Iterator iter = namedGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String name = (String)iter.next() ;
            httpQuery.addParam(HttpParams.pNamedGraph, name) ;
        }
        return httpQuery ;
    }
    
    public void abort() { }

    public void close() { }

//    public boolean isActive() { return false ; }
    
    public String toString()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        return "GET "+httpQuery.toString() ;
    }

    public Dataset getDataset()
    {
        return null ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
