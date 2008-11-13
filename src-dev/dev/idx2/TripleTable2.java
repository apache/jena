/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import iterator.NullIterator;

import static com.hp.hpl.jena.tdb.lib.TupleLib.* ;
import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.Closeable;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.lib.TupleLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;

/** TripleTable - a collection of TupleIndexes for 3-tuples
 *  together with a node table.
*   Normally, based on 3 indexes (SPO, POS, OSP) but other
*   indexing structures can be configured.
*   The node table form can map to and from NodeIds (longs)
*/

public class TripleTable2 implements Sync, Closeable
{
    private final NodeTable nodeTable ;
    private final Location location ;
    private TupleTable tupleTable ;

    public TripleTable2(TupleIndex[] indexes, RecordFactory indexRecordFactory, NodeTable nodeTable, Location location)
    {
        if ( indexes.length == 0 || indexes[0] == null )
            throw new TDBException("A primary index is required") ;
        this.tupleTable = new TupleTable(3, indexes, indexRecordFactory, location) ;
        this.nodeTable = nodeTable ;
        this.location = location ;
    }
    
    public boolean add( Triple triple ) 
    { 
        return tupleTable.add(tuple(triple, nodeTable)) ;
    }
    
    /** Delete a triple  - return true if it was deleted, false if it didn't exist */
    public boolean delete( Triple triple ) 
    { 
        return tupleTable.delete(tuple(triple, nodeTable)) ;
    }
    
    /** Find by node. */
    public Iterator<Triple> find(Node s, Node p, Node o)
    {
        NodeId subj = idForNode(s) ;
        if ( subj == NodeId.NodeDoesNotExist )
            return new NullIterator<Triple>() ;
        
        NodeId pred = idForNode(p) ;
        if ( pred == NodeId.NodeDoesNotExist )
            return new NullIterator<Triple>() ;
        
        NodeId obj = idForNode(o) ;
        if ( obj == NodeId.NodeDoesNotExist )
            return new NullIterator<Triple>() ;

        Tuple<NodeId> tuple = new Tuple<NodeId>(subj, pred, obj) ;
        Iterator<Tuple<NodeId>> _iter = tupleTable.find(tuple) ;
        Iterator<Triple> iter = TupleLib.convertToTriples(nodeTable, _iter) ;
        return iter ;
    }

    // ==== Node

    private final NodeId idForNode(Node node)
    {
        if ( node == null || node == Node.ANY )
            return NodeId.NodeIdAny ;
        return nodeTable.nodeIdForNode(node) ;
    }
    
    // Store node, return id.  Node may already be stored.
    //protected abstract int storeNode(Node node) ;
    
    protected final NodeId storeNode(Node node)
    {
        return nodeTable.storeNode(node) ;
    }
    
    protected final Node retrieveNode(NodeId id)
    {
        return nodeTable.retrieveNodeByNodeId(id) ;
    }
    
    /** Return the undelying tuple table - used with great care by tools
     * that directly manipulate internal structures. 
     */
    public TupleTable getTupleTable() { return tupleTable ; }
    
    /** Return the location of for the indexes of this triple table.
     *  Usually, all the indexes are in the same location.   
     *  May be null (e.g. in-memory testing) 
     */ 
    public Location getLocation() { return tupleTable.getLocation() ; }
    
    @Override
    final public void close()
    {
        tupleTable.close() ;
        nodeTable.close() ;
    }
    
    @Override
    public void sync(boolean force)
    {
        tupleTable.sync(force) ;
        nodeTable.sync(force) ;
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