/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import java.util.Iterator ;

import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.index.TupleTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTupleTableWrapper implements NodeTupleTable
{
    protected final NodeTupleTable nodeTupleTable ;

    public NodeTupleTableWrapper(NodeTupleTable ntt) { this.nodeTupleTable = ntt ; }
     
    public boolean addRow(Node... nodes)
    { return nodeTupleTable.addRow(nodes) ; }

    public boolean deleteRow(Node... nodes)
    { return nodeTupleTable.deleteRow(nodes) ; }

    public Iterator<Tuple<Node>> find(Node... nodes)
    { return nodeTupleTable.find(nodes) ; }
    
    public Iterator<Tuple<NodeId>> find(NodeId... ids)
    { return nodeTupleTable.find(ids) ; }
    
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> tuple)
    { return nodeTupleTable.find(tuple) ; }
    
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes)
    { return nodeTupleTable.findAsNodeIds(nodes) ; }

    public Iterator<Tuple<NodeId>> findAll()
    { return nodeTupleTable.findAll() ; }

    public NodeTable getNodeTable()
    { return nodeTupleTable.getNodeTable() ; }

    public TupleTable getTupleTable()
    { return nodeTupleTable.getTupleTable() ; }

    public boolean isEmpty()
    { return nodeTupleTable.isEmpty() ; }
    
    public void clear()
    { nodeTupleTable.clear(); }

    public long size()
    { return nodeTupleTable.size() ; }

    public void sync()
    { nodeTupleTable.sync() ; }

    public void sync(boolean force)
    { nodeTupleTable.sync(force) ; }

    public void close()
    { nodeTupleTable.close() ; }

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