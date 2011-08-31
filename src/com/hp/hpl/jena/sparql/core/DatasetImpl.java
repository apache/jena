/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

/** Wrapper around a DatasetGraph. See also DataSourceImpl. */

public class DatasetImpl implements Dataset
{
    protected DatasetGraph dsg = null ;
    private Object lock = new Object() ;
    
    // A small cache so that calls getDefaultModel()/getNamedModel() are
    // cheap when used repeatedly in code.  This is not an excuse for
    // DatasetGraph not to cache if appropriate for the storage technology.
    private Model defaultModel = null ;
    // read synchronised in this class, not need for a sync wrapper.
    private Cache<String, Model> cache = CacheFactory.createCache(0.75f, 20) ;      

    public DatasetImpl(Model model)
    {
        defaultModel = model ;
        this.dsg = DatasetGraphFactory.create(model.getGraph()) ;
    }
    
    public DatasetImpl(DatasetGraph dsg)
    {
        this.dsg = dsg ;
    }

    /** Return the default model */
    public Model getDefaultModel() 
    { 
        synchronized(lock)
        {
            if ( defaultModel == null )
                defaultModel = graph2model(dsg.getDefaultGraph()) ;
            return defaultModel ;
        }
    }

    public Lock getLock() { return dsg.getLock() ; }
    
    public DatasetGraph asDatasetGraph() { return dsg ; }

    /** Return a model for the named graph - repeated calls so not guarantee to return the same Java object */
    public Model getNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        
        // synchronized because we need to read and possible update the cache atomically 
        synchronized(lock)
        {
            Model m = cache.get(uri) ;
            if ( m == null )
            {
                m = graph2model(dsg.getGraph(Node.createURI(uri))) ;
                cache.put(uri, m) ;
            }
            return m ;
        }
    }

    private static void checkGraphName(String uri)
    {
        if ( uri == null )
            throw new ARQException("null for graph name") ; 
    }

    public boolean containsNamedModel(String uri)
    {
        checkGraphName(uri) ;

        // Don't look in the cache - just ask the DSG which either caches graphs
        // or asks the storage as needed. The significant case is whether an
        // empty graph is contained in a dataset.  If it's pure quad storage,
        // the answer is usually better as "no"; if it's an in-memory
        // dataset the answer is "yes". 
        return dsg.containsGraph(Node.createURI(uri)) ;
    }

    public void close()
    {
        cache = null ;
        dsg.close();
    }

    public Iterator<String> listNames()
    { 
        return NodeUtils.nodesToURIs(dsg.listGraphNodes()) ;
    }

    private Model graph2model(Graph graph)
    { 
        return ModelFactory.createModelForGraph(graph) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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