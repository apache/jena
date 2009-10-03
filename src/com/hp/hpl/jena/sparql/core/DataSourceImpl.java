/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.util.NodeUtils;

/** A implementation of a DataSource, which is a mutable Dataset,
 *  a set of a single unnamed graph and a number (zero or
 *  more) named graphs with graphs as Models. 
 * 
 * @author Andy Seaborne
 */

public class DataSourceImpl implements DataSource
{
    /* 
     * synchronization: The ARQ policy is MRSW so read operations here
     * that cause internal datastructure changes need protecting to ensure
     * multiple read access does not corrupt those structures.  
     * Write operations (add/remove models) do not because there
     * should be only one writer by contract.
     */

    protected DataSourceGraph dsg = null ;
    private Map<Graph, Model> cache = new HashMap<Graph, Model>() ;      

    public DataSourceImpl()
    { this.dsg = new DataSourceGraphImpl() ; }

    public DataSourceImpl(DataSourceGraph otherDSG)
    {
        this.dsg = otherDSG ;
    }
    
//    public DataSourceImpl(DatasetGraph dSetGraph)
//    { 
//        // Must clone.
//    }
//    
    public DataSourceImpl(Model model)
    {
        addToCache(model) ;
        this.dsg = new DataSourceGraphImpl(model.getGraph()) ;
    }

    public DataSourceImpl(Dataset ds)
    {
        this.dsg = new DataSourceGraphImpl(ds) ;
    }

    //  Does it matter if this is not the same model each time?
    public Model getDefaultModel() 
    { 
        return graph2model(dsg.getDefaultGraph()) ;
    }

    public Lock getLock() { return dsg.getLock() ; }

    public DataSourceGraph getDataSourceGraph() { return dsg ; }
    
    public DatasetGraph asDatasetGraph() { return dsg ; }

    public Model getNamedModel(String uri)
    { 
        Node n = Node.createURI(uri) ;
        Graph g = dsg.getGraph(n) ;
        if ( g == null )
            return null ;
        return graph2model(g) ;
    }

    public void addNamedModel(String uri, Model model) throws LabelExistsException
    { 
        addToCache(model) ;
        Node n = Node.createURI(uri) ;
        dsg.addGraph(n, model.getGraph()) ;
    }

    public void removeNamedModel(String uri)
    { 
        Node n = Node.createURI(uri) ;
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
    }

    public void replaceNamedModel(String uri, Model model)
    {
        Node n = Node.createURI(uri) ;
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
        addToCache(model) ;
        dsg.addGraph(n, model.getGraph() ) ;
    }

    public void setDefaultModel(Model model)
    { 
        removeFromCache(dsg.getDefaultGraph()) ;
        addToCache(model) ;
        dsg.setDefaultGraph(model.getGraph()) ;
    }

    public boolean containsNamedModel(String uri)
    { 
        Node n = Node.createURI(uri) ;
        return dsg.containsGraph(n) ;
    }

    // Don't look in the cahe - go direct to source
    public Iterator<String> listNames()
    { 
        return NodeUtils.nodesToURIs(dsg.listGraphNodes()) ;
    }


//  -------
//  Cache models wrapping graph

    public void close()
    {
        dsg.close() ;
        cache = null ;
    }

    synchronized
    private void removeFromCache(Graph graph)
    {
        if ( graph == null )
            return ;
        cache.remove(graph) ;
    }

    synchronized
    private void addToCache(Model model)
    {
        cache.put(model.getGraph(), model) ;
    }

    synchronized
    private Model graph2model(Graph graph)
    { 
        Model model = cache.get(graph) ;
        if ( model == null )
        {
            model = ModelFactory.createModelForGraph(graph) ;
            cache.put(graph, model) ;
        }
        return model ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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