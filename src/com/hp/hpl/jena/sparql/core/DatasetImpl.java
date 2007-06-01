/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.MapFilter;
import com.hp.hpl.jena.util.iterator.MapFilterIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import com.hp.hpl.jena.query.Dataset;

/**Wrapper around a DatasetGraph. See also DataSourceImpl.
 * 
 * @author Andy Seaborne
 * @version $Id: DataSourceImpl.java,v 1.14 2007/02/05 17:11:39 andy_seaborne Exp $
 */

public class DatasetImpl implements Dataset
{
    protected DatasetGraph dsg = null ;
    // Cache graph => model so returned models are the same (==)
    private Map cache = new HashMap() ;      

    public DatasetImpl(Model model)
    {
        addToCache(model);
        this.dsg = new DataSourceGraphImpl(model.getGraph()) ;
    }
    
    public DatasetImpl(DatasetGraph dsg)
    {
        this.dsg = dsg ;
    }

    //  Does it matter if this is not the same model each time?
    public Model getDefaultModel() 
    { 
        return graph2model(dsg.getDefaultGraph()) ;
    }

    public Lock getLock() { return dsg.getLock() ; }
    
    public DatasetGraph asDatasetGraph() { return dsg ; }

    public Model getNamedModel(String uri)
    { 
        return graph2model(dsg.getGraph(Node.createURI(uri))) ;
    }

    public boolean containsNamedModel(String uri)
    { 
        return dsg.containsGraph(Node.createURI(uri)) ;
    }

    public Iterator listNames()
    { 
        List x = new ArrayList(dsg.size()) ;
        MapFilter mapper = new MapFilter(){
            public Object accept(Object x)
            {
                Node n = (Node)x ;
                return n.getURI() ;  
            }} ;
        
        ExtendedIterator eIter = WrappedIterator.create(dsg.listGraphNodes()) ;
        MapFilterIterator conv = new MapFilterIterator(mapper, eIter) ;
        return conv ;
    }

//  -------
//  Cache models wrapping graph

    private void removeFromCache(Graph graph)
    {
        if ( graph == null )
            return ;
        cache.remove(graph) ;
    }

    private void addToCache(Model model)
    {
        cache.put(model.getGraph(), model) ;
    }

    private Model graph2model(Graph graph)
    { 
        Model model = (Model)cache.get(graph) ;
        if ( model == null )
        {
            model = ModelFactory.createModelForGraph(graph) ;
            cache.put(graph, model) ;
        }
        return model ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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