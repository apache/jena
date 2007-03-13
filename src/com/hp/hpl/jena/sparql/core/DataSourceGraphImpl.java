/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.shared.Lock; 
import com.hp.hpl.jena.shared.LockMRSW;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.graph.Graph ;
import java.util.* ;

import org.apache.commons.logging.LogFactory;

/** Graph-level dataset.
 *  @see com.hp.hpl.jena.sparql.core.DatasetGraph
 *  @see com.hp.hpl.jena.query.Dataset
 * 
 * @author Andy Seaborne
 * @version $Id: DataSourceGraphImpl.java,v 1.7 2007/01/02 11:20:30 andy_seaborne Exp $
 */

public class DataSourceGraphImpl implements DataSourceGraph
{
    Graph defaultGraph = null ;
    Map namedGraphs = null ;
    Lock lock = null ;

    public DataSourceGraphImpl(Graph graph)
    { 
        defaultGraph = graph ;
        namedGraphs = new HashMap() ;
    }

    
    // Copy over to own structures for later modification
    public DataSourceGraphImpl(Dataset dataset)
    { cloneDataset(dataset) ; } 

    public DataSourceGraphImpl(DatasetGraph dataset)
    { cloneDatasetGraph(dataset) ; } 

    public DataSourceGraphImpl()
    { } 
    
    /** Get the default graph as a Jena Graph */
    public Graph getDefaultGraph()
    { return defaultGraph ; }

    /** Set the default graph.  Set the active graph if it was null */ 
    public void setDefaultGraph(Graph g)
    { 
        defaultGraph = g ;
    }

    public Graph getNamedGraph(String uri)
    { 
        if ( namedGraphs == null )
            return null ;
        return (Graph)namedGraphs.get(uri) ;
    }

    public void addNamedGraph(String uri, Graph graph)
    {
        if ( namedGraphs== null )
            namedGraphs = new HashMap() ;
        namedGraphs.put(uri, graph) ;
    }
     
    public Graph removeNamedGraph(String uri)
    {
        if ( namedGraphs == null )
            return null ;
        return (Graph)namedGraphs.remove(uri) ;
    }

    public boolean containsNamedGraph(String uri)
    { 
        if ( namedGraphs == null ) return false ;
        return namedGraphs.containsKey(uri) ;
    }

    public Iterator listNames()
    { 
        if ( namedGraphs == null )
            return new NullIterator() ; 
        return namedGraphs.keySet().iterator() ;
    }
    
    public Lock getLock()
    {   if ( lock == null )
            lock = new LockMRSW() ;
        return lock ;
    }

    // Shallow copy
    private void cloneDataset(Dataset dataset)
    {
        if ( dataset == null )
            return ;
        
        if ( dataset.getDefaultModel() != null )
            defaultGraph = dataset.getDefaultModel().getGraph() ;
        
        Iterator iter = dataset.listNames() ;
        while(iter.hasNext())
        {
            String uri = (String)iter.next() ;
            Model m = dataset.getNamedModel(uri) ;
            if ( m == null )
                continue ;
            addNamedGraph(uri, m.getGraph()) ;
        }
    }
    
    // Shallow copy
    private void cloneDatasetGraph(DatasetGraph dataset)
    {
        if ( ! ( dataset instanceof DataSourceGraphImpl ) )
        {
            LogFactory.getLog(DataSourceGraphImpl.class).fatal("Clone DatasetGraph: only DataSourceGraphImpl supported") ;
            return ;
        }            
        DataSourceGraphImpl ds = (DataSourceGraphImpl)dataset ;
        namedGraphs = new HashMap(ds.namedGraphs) ;
        defaultGraph = ds.defaultGraph ;
    }

    
    public String toString()
    {
        String s = "{" ;
        if ( getDefaultGraph() == null )
            s = s+"<null>" ;
        else
            s = s+"["+getDefaultGraph().size()+"]" ;
        for ( Iterator iter = listNames() ; iter.hasNext() ; )
        {
            String name = (String)iter.next() ;
            s = s+", ("+name+", ["+getNamedGraph(name).size()+"])" ;
        }
        s = s + "}" ;
        return s ;
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