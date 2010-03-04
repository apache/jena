/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.shared.LockMRSW ;
import com.hp.hpl.jena.sparql.lib.iterator.NullIterator ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

/** Graph-level dataset.
 *  @see com.hp.hpl.jena.sparql.core.DatasetGraph
 *  @see com.hp.hpl.jena.query.Dataset
 * 
 * @author Andy Seaborne
 */

public class DataSourceGraphImpl implements DataSourceGraph
{
    private Context context = new Context() ;
    private Graph defaultGraph = null ;
    private Map<Node, Graph> namedGraphs = null ;
    private Lock lock = null ;

    public DataSourceGraphImpl(Graph graph)
    { 
        defaultGraph = graph ;
        namedGraphs = new HashMap<Node, Graph>() ;
    }
    
    public DataSourceGraphImpl(Model model)
    { this(model.getGraph()) ; }

    // Copy over to own structures for later modification
    public DataSourceGraphImpl(Dataset dataset)
    { cloneDataset(dataset) ; } 

    public DataSourceGraphImpl(DatasetGraph dataset)
    { cloneDatasetGraph(dataset) ; }    // Clone - 

    public DataSourceGraphImpl()
    { this(GraphFactory.createDefaultGraph()) ; }
    
    /** Get the default graph as a Jena Graph */
    public Graph getDefaultGraph()
    { 
//        if ( defaultGraph == null )
//            System.err.println("** NULL default graph") ;
        return defaultGraph ;
    }

    /** Set the default graph.  Set the active graph if it was null */ 
    public void setDefaultGraph(Graph g)
    { 
        defaultGraph = g ;
    }

    public Graph getGraph(Node graphName)
    { 
        if ( namedGraphs == null )
            return null ;
        return namedGraphs.get(graphName) ;
    }

    public void addGraph(Node graphName, Graph graph)
    {
        if ( namedGraphs== null )
            namedGraphs = new HashMap<Node, Graph>() ;
        namedGraphs.put(graphName, graph) ;
    }
     
    public Graph removeGraph(Node graphName)
    {
        if ( namedGraphs == null )
            return null ;
        return namedGraphs.remove(graphName) ;
    }

    public boolean containsGraph(Node graphName)
    { 
        if ( namedGraphs == null ) return false ;
        return namedGraphs.containsKey(graphName) ;
    }

    public Iterator<Node> listGraphNodes()
    { 
        if ( namedGraphs == null )
            return new NullIterator<Node>() ;
        return namedGraphs.keySet().iterator() ;
    }
    
    public Context getContext()
    {
        return context ;
    }

    public int size()
    {
        return namedGraphs.size() ;
    }
    
    public Lock getLock()
    {   if ( lock == null )
            lock = new LockMRSW() ;
        return lock ;
    }

    // Shallow copy
    public void cloneDataset(Dataset dataset)
    {
        if ( dataset == null )
            return ;
        
        if ( dataset.getDefaultModel() != null )
            defaultGraph = dataset.getDefaultModel().getGraph() ;
        
        Iterator<String> iter = dataset.listNames() ;
        while(iter.hasNext())
        {
            String uri = iter.next() ;
            Node graphRef = Node.createURI(uri) ;
            Model m = dataset.getNamedModel(uri) ;
            if ( m == null )
                continue ;
            addGraph(graphRef, m.getGraph()) ;
        }
    }
    
    public DatasetGraph copy()
    {
        DataSourceGraphImpl ds = new DataSourceGraphImpl() ;
        ds.setDefaultGraph(getDefaultGraph()) ;
        ds.namedGraphs = new HashMap<Node, Graph>(namedGraphs) ;
        return ds ;
    }
    
    private void cloneDatasetGraph(DatasetGraph dataset)
    {
        if ( ! ( dataset instanceof DataSourceGraphImpl ) )
        {
            defaultGraph = dataset.getDefaultGraph() ;
            namedGraphs = new HashMap<Node, Graph>() ;
            for ( Iterator<Node> iter = dataset.listGraphNodes() ; iter.hasNext(); )
            {
                Node name = iter.next();
                this.addGraph(name, dataset.getGraph(name)) ;
            }
            return ;
        }            
        DataSourceGraphImpl ds = (DataSourceGraphImpl)dataset ;
        namedGraphs = new HashMap<Node, Graph>(ds.namedGraphs) ;
        defaultGraph = ds.defaultGraph ;
    }

    
    @Override
    public String toString()
    {
        String s = "{" ;
        if ( getDefaultGraph() == null )
            s = s+"<null>" ;
        else
            s = s+"["+getDefaultGraph().size()+"]" ;
        for ( Iterator<Node> iter = listGraphNodes() ; iter.hasNext() ; )
        {
            Node graphName = iter.next() ;
            String x = FmtUtils.stringForNode(graphName) ;
            s = s+", ("+x+", ["+getGraph(graphName).size()+"])" ;
        }
        s = s + "}" ;
        return s ;
    }

    public void close()
    {
        if ( getDefaultGraph() != null )
            getDefaultGraph().close() ;
        
        for ( Iterator<Node> iter = listGraphNodes() ; iter.hasNext() ; )
        {
            Node graphName = iter.next() ;
            Graph g = getGraph(graphName) ;
            g.close();
        }
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