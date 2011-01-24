/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import java.util.Iterator ;

import org.openjena.atlas.lib.ArrayUtils ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.index.TupleTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Read-only projection of another NodeTupleTable. 
 * This will not reduce a N-wide tuple to N-1 when find*() used. 
 */ 
public class NodeTupleTableView extends NodeTupleTableWrapper
{
    private Node prefix ;
    private NodeId prefixId ;

    public NodeTupleTableView(NodeTupleTable ntt, Node prefix)
    {
        super(ntt) ;
        this.prefix = prefix ;
        this.prefixId = ntt.getNodeTable().getNodeIdForNode(prefix) ;
    }
    
    @Override
    public boolean addRow(Node... nodes)
    { 
        nodes = push(Node.class, prefix, nodes) ;
        return super.addRow(nodes) ;
    }

    @Override
    public boolean deleteRow(Node... nodes)
    {
        nodes = push(Node.class, prefix, nodes) ;
        return super.deleteRow(nodes) ;
    }
    
    @Override
    public Iterator<Tuple<Node>> find(Node... nodes)
    { 
        nodes = push(Node.class, prefix, nodes) ;
        return nodeTupleTable.find(nodes) ;
    }
    
    private static <T> T[] push(Class<T> cls, T x,  T[] array)
    {
        T[] array2 = ArrayUtils.alloc(cls, array.length+1) ;
        System.arraycopy(array, 0, array2, 1, array.length) ;
        array2[0] = x ;
        return array2 ;
    }

    @Override
    public Iterator<Tuple<NodeId>> find(NodeId... ids)
    {
        ids = push(NodeId.class, prefixId, ids) ;
        return nodeTupleTable.find(ids) ;
    }
    
    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> ids)
    {
        NodeId[] ids2 = push(NodeId.class, prefixId, ids.tuple()) ;
        return nodeTupleTable.find(ids2) ;
    }

    @Override
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes)
    {
        nodes = push(Node.class, prefix, nodes) ;
        return nodeTupleTable.findAsNodeIds(nodes) ;
    }

//    public NodeTable getNodeTable()
//    { return nodeTupleTable.getNodeTable() ; }

    @Override
    public TupleTable getTupleTable()
    // Need a projection of this?
    { return super.getTupleTable() ; }
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