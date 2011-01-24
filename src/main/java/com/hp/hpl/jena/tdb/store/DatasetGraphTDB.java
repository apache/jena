/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;


import java.util.Iterator ;
import java.util.Properties ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.PropertyUtils ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphCaching ;
import com.hp.hpl.jena.sparql.core.DatasetImpl ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.sys.Session ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;
import com.hp.hpl.jena.update.GraphStore ;

/** TDB Dataset, updateable with SPARQL/Update */
public class DatasetGraphTDB extends DatasetGraphCaching
                             implements DatasetGraph, Sync, Closeable, GraphStore, Session
{
    // or DatasetGraphCaching
    private TripleTable tripleTable ;
    private QuadTable quadTable ;
    private DatasetPrefixStorage prefixes ;
    private final ReorderTransformation transform ;
    private final Location location ;
    private final Properties config ;
    
    private GraphTDB effectiveDefaultGraph ;
    private boolean closed = false ;

    public DatasetGraphTDB(TripleTable tripleTable, QuadTable quadTable, DatasetPrefixStorage prefixes, 
                           ReorderTransformation transform, Location location, Properties config)
    {
        this.tripleTable = tripleTable ;
        this.quadTable = quadTable ;
        this.prefixes = prefixes ;
        this.transform = transform ;
        this.location = location ;
        this.config = config ;
        this.effectiveDefaultGraph = getDefaultGraphTDB() ;
    }
    
    protected DatasetGraphTDB(DatasetGraphTDB other)
    {
        this(other.tripleTable, other.quadTable, other.prefixes, other.transform, other.location, other.config) ;
    }

    public DatasetGraphTDB duplicate()
    {
        return new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, location, config) ;
    }
    
    public QuadTable getQuadTable()         { return quadTable ; }
    public TripleTable getTripleTable()     { return tripleTable ; } 
    
//    private Lock lock = new MRSWLite() ;
//    @Override 
//    public Lock getLock()                   { return lock ; }
    
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    {
        return triples2quadsDftGraph(getTripleTable().find(s, p, o)) ;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    {
        return getQuadTable().find(g, s, p, o) ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    {
        return getQuadTable().find(Node.ANY, s, p, o) ;
    }

    protected static Iterator<Quad> triples2quadsDftGraph(Iterator<Triple> iter)
    {
        return triples2quads(Quad.tripleInQuad, iter) ;
    }
    
//    @Override
//    public void add(Quad quad)
//    {
//        if ( quad.isDefaultGraph() )
//            getTripleTable().add(quad.asTriple()) ;
//        else
//            getQuadTable().add(quad) ;
//    } 
//    
//    @Override
//    public void delete(Quad quad)
//    {
//        if ( quad.isDefaultGraph() )
//            getTripleTable().delete(quad.asTriple()) ;
//        else
//            getQuadTable().delete(quad) ;
//    }
    
    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    { getTripleTable().add(s,p,o) ; }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    { getQuadTable().add(g, s, p, o) ; }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    { getTripleTable().delete(s,p,o) ; }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    { getQuadTable().delete(g, s, p, o) ; }
    
    public GraphTDB getDefaultGraphTDB()
    {
        return (GraphTDB)getDefaultGraph() ;
    }

    public GraphTDB getGraphTDB(Node graphNode)
    {
        return (GraphTDB)getGraph(graphNode) ;
    }

    // The effective graph may not be the concrete storage one (e.g. union)
    
    @Override
    protected void _close()
    {
        tripleTable.close() ;
        quadTable.close() ;
        prefixes.close();
        
        // Which will cause reuse to throw exceptions early.
        tripleTable = null ;
        quadTable = null ;
        prefixes = null ;
        
        TDBMaker.releaseDataset(this) ;
    }
    
    @Override
    // Empty graphs don't "exist" 
    public boolean containsGraph(Node graphNode) { return _containsGraph(graphNode) ; }

    @Override
    protected boolean _containsGraph(Node graphNode)
    {
        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().findAsNodeIds(graphNode, null, null, null) ;
        if ( x == null )
            return false ; 
        
//        NodeId graphNodeId = quadTable.getNodeTupleTable().getNodeTable().getNodeIdForNode(graphNode) ;
//        Tuple<NodeId> pattern = Tuple.create(graphNodeId, null, null, null) ;
//        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().getTupleTable().find(pattern) ;
        boolean result = x.hasNext() ;
        return result ;
    }

    @Override
    protected Graph _createDefaultGraph()
    {
        return new GraphTriplesTDB(this, tripleTable, prefixes) ; 
    }

    @Override
    protected Graph _createNamedGraph(Node graphNode)
    {
        return new GraphNamedTDB(this, graphNode) ;
    }

    //@Override
    public void setEffectiveDefaultGraph(GraphTDB g) { effectiveDefaultGraph = g ; }

    //@Override
    public GraphTDB getEffectiveDefaultGraph() 
    {
        return effectiveDefaultGraph ;
    }

    public Properties getConfig()
    {
        return config ;
    }
    
    public String getConfigValue(String key)
    {
        if ( config == null )
            return null ;
        return config.getProperty(key) ;
    }
    
    public int getConfigValueAsInt(String key, int dftValue)
    {
        if ( config == null )
            return dftValue ;
        return PropertyUtils.getPropertyAsInteger(config, key, dftValue) ;
    }

    public ReorderTransformation getTransform()     { return transform ; }
    
    public DatasetPrefixStorage getPrefixes()       { return prefixes ; }

    static private Transform<Tuple<NodeId>, NodeId> project0 = new Transform<Tuple<NodeId>, NodeId>()
    {
        //@Override
        public NodeId convert(Tuple<NodeId> item)
        {
            return item.get(0) ;
        }
    } ;
    

    public Iterator<Node> listGraphNodes()
    {
        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().findAll() ;
        Iterator<NodeId> z =  Iter.iter(x).map(project0).distinct() ;
        return NodeLib.nodes(quadTable.getNodeTupleTable().getNodeTable(), z) ;
    }

    @Override
    public long size()                   { return Iter.count(listGraphNodes()) ; }

    @Override
    public boolean isEmpty()            { return getTripleTable().isEmpty() && getQuadTable().isEmpty() ; }

    //@Override
    public void clear()
    {
        // Leave the node table alone.
        getTripleTable().clearTriples() ;
        getQuadTable().clearQuads() ;
    }
    
    public Location getLocation()       { return location ; }

    //@Override
    public void sync() { sync(true) ; }
    
    //@Override
    public void sync(boolean force)
    {
        tripleTable.sync(force) ;
        quadTable.sync(force) ;
        prefixes.sync(force) ;
    }
    
    // Done by superclass that then call _close. public void close()

    // --- GraphStore
    //@Override
    public void startRequest()      {}

    //@Override
    public void finishRequest()     { this.sync(true) ; } 

    //@Override
    public Dataset toDataset()      { return new DatasetImpl(this) ; }

    // ---- DataSourceGraph
    
    @Override
    public void addGraph(Node graphName, Graph graph)
    {
        Graph g = getGraph(graphName) ;
        g.getBulkUpdateHandler().add(graph) ;
    }
    
    @Override
    public void setDefaultGraph(Graph g)
    { 
        throw new UnsupportedOperationException("Can't set default graph via GraphStore on a TDB-backed dataset") ;
    }

    public void startUpdate()
    {}

    public void finishUpdate()
    {}

    public void startRead()
    {}

    public void finishRead()
    {}
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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