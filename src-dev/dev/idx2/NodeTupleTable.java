/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;
import static java.lang.String.format ;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;

/** Support code to group tuple table and node table */ 
public class NodeTupleTable implements Sync, Closeable
{
    protected final NodeTable nodeTable ;
    protected final Location location ;
    protected final TupleTable tupleTable ;
    
    public NodeTupleTable(int N, TupleIndex[] indexes, RecordFactory indexRecordFactory, NodeTable nodeTable, Location location)
    {
        if ( indexes.length == 0 || indexes[0] == null )
            throw new TDBException("A primary index is required") ;
        for ( TupleIndex index : indexes )
        {
            if ( N != index.getTupleLength() )
                throw new TDBException(format("Inconsistent: TupleTable width is %d but index %s is %d",
                                              N, index.getLabel(), index.getTupleLength() )) ;   
        }
        
        this.tupleTable = new TupleTable(N, indexes, indexRecordFactory, location) ;
        this.nodeTable = nodeTable ;
        this.location = location ;
    }
    
    // ==== Node

    protected final NodeId idForNode(Node node)
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
    
    // ==== Accessors
    
    /** Return the undelying tuple table - used with great care by tools
     * that directly manipulate internal structures. 
     */
    public final TupleTable getTupleTable() { return tupleTable ; }
    
    /** Return the location of for the indexes of this triple table.
     *  Usually, all the indexes are in the same location.   
     *  May be null (e.g. in-memory testing) 
     */ 
    public final Location getLocation() { return tupleTable.getLocation() ; }
    
    @Override
    public final void close()
    {
        tupleTable.close() ;
        nodeTable.close() ;
    }
    
    @Override
    public final void sync(boolean force)
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