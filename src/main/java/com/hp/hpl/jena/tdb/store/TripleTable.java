/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;


import java.util.Iterator ;

import org.openjena.atlas.iterator.NullIterator ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableConcrete ;


/** TripleTable - a collection of TupleIndexes for 3-tuples
 *  together with a node table.
 *   Normally, based on 3 indexes (SPO, POS, OSP) but other
 *   indexing structures can be configured.
 *   The node table form can map to and from NodeIds (longs)
 */

public class TripleTable implements Sync, Closeable
{
    final NodeTupleTable table ;
    
    public TripleTable(TupleIndex[] indexes, NodeTable nodeTable)
    {
        table = new NodeTupleTableConcrete(3, indexes, nodeTable) ;
    }
    
    public boolean add( Triple triple ) 
    { 
        return add(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    public boolean add(Node s, Node p, Node o) 
    { 
        return table.addRow(s, p, o) ;
    }
    
    /** Delete a triple  - return true if it was deleted, false if it didn't exist */
    public boolean delete( Triple triple ) 
    { 
        return table.deleteRow(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    /** Delete a triple  - return true if it was deleted, false if it didn't exist */
    public boolean delete(Node s, Node p, Node o) 
    { 
        return table.deleteRow(s, p, o) ;
    }

    /** Find matching triples */
    public Iterator<Triple> find(Node s, Node p, Node o)
    {
        Iterator<Tuple<NodeId>> iter = table.findAsNodeIds(s, p, o) ;
        if ( iter == null )
            return new NullIterator<Triple>() ;
        Iterator<Triple> iter2 = TupleLib.convertToTriples(table.getNodeTable(), iter) ;
        return iter2 ;
    }
    
    private static Transform<Tuple<Node>, Triple> action = new Transform<Tuple<Node>, Triple>(){
        //@Override
        public Triple convert(Tuple<Node> item)
        {
            return new Triple(item.get(0), item.get(1), item.get(2)) ;
        }} ; 
    
    public NodeTupleTable getNodeTupleTable() { return table ; }

    //@Override
    public void sync() { sync(true) ; }

    //@Override
    public void sync(boolean force)
    { table.sync(force) ; }

    //@Override
    public void close()
    { table.close() ; }
    
    public boolean isEmpty()        { return table.isEmpty() ; }
    
    /** Clear - does not clear the associated node tuple table */
    public void clearTriples()
    { table.clear() ; }

//    /** Clear - including the associated node tuple table */
//    public void clear()
//    { 
//        table.getTupleTable().clear() ;
//        table.getNodeTable().clear() ;
//    }
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