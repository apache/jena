/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import static com.hp.hpl.jena.tdb.lib.TupleLib.tuple;
import iterator.NullIterator;

import java.util.Iterator;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.lib.TupleLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;

/** Quad table - a collection of TupleIndexes for 4-tuples
 *  together with a node table.
 */

public class QuadTable extends NodeTupleTable implements Sync, Closeable
{

    public QuadTable(TupleIndex[] indexes, RecordFactory indexRecordFactory, NodeTable nodeTable, Location location)
    {
        super(4, indexes, indexRecordFactory, nodeTable, location);
    }

    /** Add a quad - return true if it was added, false if it already existed */
    public boolean add( Quad quad ) 
    { 
        return tupleTable.add(tuple(quad, nodeTable)) ;
    }

    /** Delete a quad - return true if it was deleted, false if it didn't exist */
    public boolean delete( Quad quad ) 
    { 
        return tupleTable.delete(tuple(quad, nodeTable)) ;
    }

    /** Find by node. */
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    {
        NodeId graph = idForNode(g) ;
        if ( graph == NodeId.NodeDoesNotExist )
            return new NullIterator<Quad>() ;
        
        NodeId subj = idForNode(s) ;
        if ( subj == NodeId.NodeDoesNotExist )
            return new NullIterator<Quad>() ;

        NodeId pred = idForNode(p) ;
        if ( pred == NodeId.NodeDoesNotExist )
            return new NullIterator<Quad>() ;

        NodeId obj = idForNode(o) ;
        if ( obj == NodeId.NodeDoesNotExist )
            return new NullIterator<Quad>() ;

        Tuple<NodeId> tuple = new Tuple<NodeId>(graph, subj, pred, obj) ;
        Iterator<Tuple<NodeId>> _iter = tupleTable.find(tuple) ;
        Iterator<Quad> iter = TupleLib.convertToQuads(nodeTable, _iter) ;
        return iter ;
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