/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable ;

import java.util.Iterator ;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.index.TupleTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public interface NodeTupleTable extends Sync, Closeable
{
    public boolean addRow(Node... nodes) ;

    public boolean deleteRow(Node... nodes) ;

    /** Find by node. */
    public Iterator<Tuple<Node>> find(Node... nodes) ;

    /** Find by node - return an iterator of NodeIds. Can return "null" for not found as well as NullIterator */
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes) ;

    /** Find by NodeId. */
    public Iterator<Tuple<NodeId>> find(NodeId... ids) ;
    
    /** Find by NodeId. */
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> ids) ;
    

    /** Find all tuples */ 
    public Iterator<Tuple<NodeId>> findAll() ;

    /** Return the undelying tuple table - used with great care by tools
     * that directly manipulate internal structures. 
     */
    public TupleTable getTupleTable() ;

    /** Return the node table */
    public NodeTable getNodeTable() ;

    public boolean isEmpty() ;
    
    /** Clear the tuple table.  After this operation, find* will find  nothing.
     * This does not mean all data has been removed - for example, it does not mean
     * that any node table has been emptied.
     */
    public void clear() ;

    // No clear operation - need to manage the tuple table 
    // and node tables separately.
    
    public long size() ;
}

/*
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