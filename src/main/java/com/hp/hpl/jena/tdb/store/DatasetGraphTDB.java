/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;


import java.util.Iterator;
import java.util.Properties;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.PropertyUtils ;
import org.openjena.atlas.lib.Tuple ;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphBase;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.migrate.DatasetPrefixStorage ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.sys.TDBMaker;
import com.hp.hpl.jena.update.GraphStore;

/** TDB Dataset, updatable with SPARQL/Update */
public class DatasetGraphTDB extends DatasetGraphBase 
                             implements DatasetGraph, Sync, Closeable, GraphStore
{
    private TripleTable tripleTable ;
    private QuadTable quadTable ;
    private DatasetPrefixStorage prefixes ;
    private final ReorderTransformation transform ;
    private final Location location ;
    private final Properties config ;
    
    private GraphTDB effectiveDefaultGraph ;

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
    
    @Override
    public boolean containsGraph(Node graphNode)
    {
        return _containsGraph(graphNode) ;
    }
    
    @Override
    protected boolean _containsGraph(Node graphNode)
    {
        NodeId graphNodeId = quadTable.getNodeTupleTable().getNodeTable().getNodeIdForNode(graphNode) ;
        Tuple<NodeId> pattern = Tuple.create(graphNodeId, null, null, null) ;
        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().getTupleTable().find(pattern) ;
        boolean result = x.hasNext() ;
        return result ;
    }

    @Override
    protected GraphTDB _createDefaultGraph()
    {
        return new GraphTriplesTDB(this, tripleTable, prefixes) ; 
    }

    public GraphTDB getDefaultGraphTDB()
    {
        return (GraphTDB)getDefaultGraph() ;
    }

    public GraphTDB getGraphTDB(Node graphNode)
    {
        return (GraphTDB)getGraph(graphNode) ;
    }

    // The effective graph may not be the concrete storage one (e.g. union)
    
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

    @Override
    protected GraphTDB _createNamedGraph(Node graphNode)
    {
        return new GraphNamedTDB(this, graphNode) ;
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
    

    //@Override
    public Iterator<Node> listGraphNodes()
    {
        Iterator<Tuple<NodeId>> x = quadTable.getNodeTupleTable().getTupleTable().getIndex(0).all() ;
        Iterator<NodeId> z =  Iter.iter(x).map(project0).distinct() ;
        return NodeLib.nodes(quadTable.getNodeTupleTable().getNodeTable(), z) ;
    }

    //@Override
    public int size()                   { return (int)Iter.count(listGraphNodes()) ; }

    //@Override
    public boolean isEmpty()            { return getTripleTable().isEmpty() && getQuadTable().isEmpty() ; }

    
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

    // --- GraphStore
    //@Override
    public void startRequest()      {}

    //@Override
    public void finishRequest()     { this.sync(true) ; } 

    //@Override
    public Dataset toDataset()      { return new DatasetImpl(this) ; }

    // ---- DataSourceGraph
    
    //@Override
    public void addGraph(Node graphName, Graph graph)
    {
        Graph g = getGraph(graphName) ;
        g.getBulkUpdateHandler().add(graph) ;
    }

    //@Override
    public Graph removeGraph(Node graphName)
    {
        Graph g = getGraph(graphName) ;
        g.getBulkUpdateHandler().removeAll() ;
        // Return null (it's empty!)
        return null ;
    }

    
    //@Override
    public void setDefaultGraph(Graph g)
    { 
        throw new UnsupportedOperationException("Can't set default graph via GraphStore on a TDB-backed dataset") ;
    }    
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