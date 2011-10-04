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

package com.hp.hpl.jena.tdb.store;


import java.util.Iterator ;

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
    private TripleTable tripleTable ;
    private QuadTable quadTable ;
    private DatasetPrefixesTDB prefixes ;
    private final ReorderTransformation transform ;
    private final StoreConfig config ;
    
    private GraphTDB effectiveDefaultGraph ;
    private boolean closed = false ;
    private boolean readOnly = false ;

    public DatasetGraphTDB(TripleTable tripleTable, QuadTable quadTable, DatasetPrefixesTDB prefixes, 
                           ReorderTransformation transform, StoreConfig config)
    {
        // ?? Change to 3 nodetables and add TripleTable/QuadTable/PrefixTable wrappers.
        
        this.tripleTable = tripleTable ;
        this.quadTable = quadTable ;
        this.prefixes = prefixes ;
        this.transform = transform ;
        this.config = config ;
        this.effectiveDefaultGraph = getDefaultGraphTDB() ;
    }
    
    protected DatasetGraphTDB(DatasetGraphTDB other)
    {
        this(other.tripleTable, other.quadTable, other.prefixes, other.transform, other.config) ;
    }

    public DatasetGraphTDB duplicate()
    {
        return new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, config) ;
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
        return triples2quads(Quad.defaultGraphIRI, iter) ;
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

    public void setEffectiveDefaultGraph(GraphTDB g)    { effectiveDefaultGraph = g ; }

    public GraphTDB getEffectiveDefaultGraph()          { return effectiveDefaultGraph ; }

    public StoreConfig getConfig()                       { return config ; }
    
    public String getConfigValue(String key)
    {
        if ( config == null )
            return null ;
        return config.properties.getProperty(key) ;
    }
    
    public int getConfigValueAsInt(String key, int dftValue)
    {
        if ( config == null )
            return dftValue ;
        return PropertyUtils.getPropertyAsInteger(config.properties, key, dftValue) ;
    }

    public ReorderTransformation getTransform()     { return transform ; }
    
    public DatasetPrefixesTDB getPrefixes()       { return prefixes ; }

    static private Transform<Tuple<NodeId>, NodeId> project0 = new Transform<Tuple<NodeId>, NodeId>()
    {
        @Override
        public NodeId convert(Tuple<NodeId> item)
        {
            return item.get(0) ;
        }
    } ;
    
    @Override
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

    public void clear()
    {
        // Leave the node table alone.
        getTripleTable().clearTriples() ;
        getQuadTable().clearQuads() ;
    }
    
    public Location getLocation()       { return config.location ; }

    @Override
    public void sync()
    {
        tripleTable.sync() ;
        quadTable.sync() ;
        prefixes.sync() ;
    }
    
    // Done by superclass that then call _close. public void close()

    // --- GraphStore
    @Override
    public void startRequest()      {}

    @Override
    public void finishRequest()     { this.sync() ; } 

    @Override
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

    @Override
    public void startUpdate()
    {}

    @Override
    public void finishUpdate()
    {}

    @Override
    public void startRead()
    {}

    @Override
    public void finishRead()
    {}
}
