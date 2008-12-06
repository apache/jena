/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator;

import lib.ColumnMap;
import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.graph.PrefixMappingPersistent;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord;
import com.hp.hpl.jena.tdb.index.TupleTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory;
import com.hp.hpl.jena.tdb.sys.Names;

public class PrefixMappingTDB extends PrefixMappingPersistent
{
    private final NodeTable nodes ;
    private final TupleTable index ;
    static final ColumnMap colMap = new ColumnMap("GPU", "GPU") ;
    static final RecordFactory factory = new RecordFactory(3*NodeId.SIZE, 0) ;
    
    // Testing version
    static PrefixMappingTDB mem(String graphName)
    {
        NodeTable nodeTable = NodeTableFactory.createMem(IndexBuilder.mem()) ;
        TupleIndex primary = new TupleIndexRecord(3, colMap, factory, IndexBuilder.mem().newRangeIndex(null, factory, "prefixIdx")) ;
        return new PrefixMappingTDB(graphName, nodeTable, primary, null) ;
    }

    
    public PrefixMappingTDB(String graphName, IndexBuilder indexBuilder, Location location)
    {
        // Cache?
        super(graphName) ;
        
        nodes = NodeTableFactory.create(indexBuilder, location, Names.prefixesData, Names.indexPrefix2Id, 10, 10) ;
        TupleIndex primary = new TupleIndexRecord(3, colMap, factory, indexBuilder.newRangeIndex(location, factory, "prefixIdx")) ;
        TupleIndex[] indexes = { primary } ;
        // Single index on the three columns.
        index = new TupleTable(3, indexes) ;
    }
    
    private PrefixMappingTDB(String graphName, NodeTable nodeTable, TupleIndex primary, Location location)
    {
        super(graphName) ;
        TupleIndex[] indexes = { primary } ;
        nodes = nodeTable ;
        index = new TupleTable(3, indexes) ; 
    }
    
    @Override
    protected synchronized void insertIntoPrefixMap(String graphName, String prefix, String uri)
    {
        NodeId id1 = nodes.storeNode(Node.createURI(graphName)) ; 
        NodeId id2 = nodes.storeNode(Node.createLiteral(prefix)) ; 
        NodeId id3 = nodes.storeNode(Node.createURI(uri)) ;
        
        // Unique by graphName/prefix. 
        Iterator<Tuple<NodeId>> iter = index.find(new Tuple<NodeId>(id1, id2, NodeId.NodeIdAny)) ;
        while ( iter.hasNext() )
            index.delete(iter.next()) ;
        
        index.add(new Tuple<NodeId>(id1, id2, id3)) ;
    }

    @Override
    protected synchronized String readFromPrefixMap(String graphName, String prefix)
    {
        Iterator<Tuple<NodeId>> iter = index.find(tuple(graphName, prefix, null)) ;
        if ( ! iter.hasNext() )
            return null ;
        NodeId uriId = iter.next().get(2) ;
        return nodes.retrieveNodeByNodeId(uriId).getURI() ; 
    }

    @Override
    protected synchronized void readPrefixMapping(String graphName)
    {}

    @Override
    protected synchronized void removeFromPrefixMap(String graphName, String prefix, String uri)
    {
        Iterator<Tuple<NodeId>> iter = index.find(tuple(graphName, prefix, uri)) ;
        while ( iter.hasNext() )
            index.delete(iter.next()) ;
    }

    private Tuple<NodeId> tuple(String graphName, String prefix, String uri)
    {
        NodeId id1 = (graphName==null) ? NodeId.NodeIdAny : nodes.nodeIdForNode(Node.createURI(graphName)) ; 
        NodeId id2 = (prefix==null) ? NodeId.NodeIdAny : nodes.nodeIdForNode(Node.createLiteral(prefix)) ; 
        NodeId id3 = (uri==null) ? NodeId.NodeIdAny : nodes.nodeIdForNode(Node.createURI(uri)) ;
        return new Tuple<NodeId>(id1, id2, id3) ;
    }


    @Override
    public void close()
    {
        if ( nodes != null ) nodes.close() ;
        if ( index != null ) index.close() ;
    }

    @Override
    public void sync(boolean force)
    { 
        if ( nodes != null ) nodes.sync(force) ;
        if ( index != null ) index.sync(force) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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