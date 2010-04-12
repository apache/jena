/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.graph.Graph ;

/** 
 * Graph-level dataset that contains a preset (but alterable) set of graphs.
 * 
 *  @see com.hp.hpl.jena.sparql.core.DatasetGraph
 *  @see com.hp.hpl.jena.query.Dataset
 * 
 * @author Andy Seaborne
 */
@Deprecated 
public class DataSourceGraphImpl extends DatasetGraphMap
{

    protected DataSourceGraphImpl(Graph graph)
    {
        super(graph) ;
    }
//    private Context context = new Context() ;
//    private Graph defaultGraph = null ;
//    private Map<Node, Graph> namedGraphs = null ;
//    private Lock lock = null ;
//
//    public DataSourceGraphImpl(Graph graph)
//    { 
//        defaultGraph = graph ;
//        namedGraphs = new HashMap<Node, Graph>() ;
//    }
//    
//    public DataSourceGraphImpl(Model model)
//    { this(model.getGraph()) ; }
//
////    // Copy over to own structures for later modification
////    DataSourceGraphImpl(Dataset dataset)
////    { cloneFromDataset(dataset) ; } 
//
//    protected DataSourceGraphImpl(DatasetGraph dataset)
//    { cloneFromDatasetGraph(dataset) ; }    // Clone - 
//
//    public DataSourceGraphImpl()
//    { this(GraphFactory.createDefaultGraph()) ; }
//    
//    // Shallow copy
//    public void cloneFromDataset(Dataset dataset)
//    {
//        if ( dataset == null )
//            return ;
//        
//        if ( dataset.getDefaultModel() != null )
//            defaultGraph = dataset.getDefaultModel().getGraph() ;
//        
//        Iterator<String> iter = dataset.listNames() ;
//        while(iter.hasNext())
//        {
//            String uri = iter.next() ;
//            Node graphRef = Node.createURI(uri) ;
//            Model m = dataset.getNamedModel(uri) ;
//            if ( m == null )
//                continue ;
//            addGraph(graphRef, m.getGraph()) ;
//        }
//    }
//
//    
//    private void cloneFromDatasetGraph(DatasetGraph dataset)
//    {
//        if ( ! ( dataset instanceof DataSourceGraphImpl ) )
//        {
//            defaultGraph = dataset.getDefaultGraph() ;
//            namedGraphs = new HashMap<Node, Graph>() ;
//            for ( Iterator<Node> iter = dataset.listGraphNodes() ; iter.hasNext(); )
//            {
//                Node name = iter.next();
//                this.addGraph(name, dataset.getGraph(name)) ;
//            }
//            return ;
//        }            
//        DataSourceGraphImpl ds = (DataSourceGraphImpl)dataset ;
//        namedGraphs = new HashMap<Node, Graph>(ds.namedGraphs) ;
//        defaultGraph = ds.defaultGraph ;
//    }
//
//    /** Get the default graph as a Jena Graph */
//    public Graph getDefaultGraph()
//    { 
////        if ( defaultGraph == null )
////            System.err.println("** NULL default graph") ;
//        return defaultGraph ;
//    }
//
//    /** Set the default graph.  Set the active graph if it was null */ 
//    public void setDefaultGraph(Graph g)
//    { 
//        defaultGraph = g ;
//    }
//
//    public Graph getGraph(Node graphName)
//    { 
//        if ( namedGraphs == null )
//            return null ;
//        return namedGraphs.get(graphName) ;
//    }
//
//    public void addGraph(Node graphName, Graph graph)
//    {
//        if ( namedGraphs== null )
//            namedGraphs = new HashMap<Node, Graph>() ;
//        namedGraphs.put(graphName, graph) ;
//    }
//     
//    public Graph removeGraph(Node graphName)
//    {
//        if ( namedGraphs == null )
//            return null ;
//        return namedGraphs.remove(graphName) ;
//    }
//
//    public boolean containsGraph(Node graphName)
//    { 
//        if ( namedGraphs == null ) return false ;
//        return namedGraphs.containsKey(graphName) ;
//    }
//
//    public Iterator<Node> listGraphNodes()
//    { 
//        if ( namedGraphs == null )
//            return new NullIterator<Node>() ;
//        return namedGraphs.keySet().iterator() ;
//    }
//    
//    public Context getContext()
//    {
//        return context ;
//    }
//
//    public int size()
//    {
//        return namedGraphs.size() ;
//    }
//    
//    public Lock getLock()
//    {   if ( lock == null )
//            lock = new LockMRSW() ;
//        return lock ;
//    }
//
//    public DatasetGraph copy()
//    {
//        DataSourceGraphImpl ds = new DataSourceGraphImpl() ;
//        ds.setDefaultGraph(getDefaultGraph()) ;
//        ds.namedGraphs = new HashMap<Node, Graph>(namedGraphs) ;
//        return ds ;
//    }
//    
//    @Override
//    public String toString()
//    {
//        String s = "{" ;
//        if ( getDefaultGraph() == null )
//            s = s+"<null>" ;
//        else
//            s = s+"["+getDefaultGraph().size()+"]" ;
//        for ( Iterator<Node> iter = listGraphNodes() ; iter.hasNext() ; )
//        {
//            Node graphName = iter.next() ;
//            String x = FmtUtils.stringForNode(graphName) ;
//            s = s+", ("+x+", ["+getGraph(graphName).size()+"])" ;
//        }
//        s = s + "}" ;
//        return s ;
//    }
//
//    public void close()
//    {
//        if ( getDefaultGraph() != null )
//            getDefaultGraph().close() ;
//        
//        for ( Iterator<Node> iter = listGraphNodes() ; iter.hasNext() ; )
//        {
//            Node graphName = iter.next() ;
//            Graph g = getGraph(graphName) ;
//            g.close();
//        }
//    }
//
//    public void add(Quad quad)
//    { throw new UnsupportedOperationException("DataSourceGraph.add") ; }
//
//    public boolean contains(Node g, Node s, Node p, Node o)
//    { throw new UnsupportedOperationException("DataSourceGraph.add") ; }
//
//    public boolean contains(Quad quad)
//    { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
//
//    public void delete(Quad quad)
//    { throw new UnsupportedOperationException("DataSourceGraph.delete") ; }
//
//    public Iterator<Quad> find(Quad quad)
//    { return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
//
//    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
//    {
//        return null ;
//    }
//
//    public boolean isEmpty()
//    {
//        return false ;
//    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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