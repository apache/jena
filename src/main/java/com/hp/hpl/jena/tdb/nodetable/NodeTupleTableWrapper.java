/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import java.util.Iterator ;

import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.index.TupleTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;

public class NodeTupleTableWrapper implements NodeTupleTable
{
    protected NodeTupleTable nodeTupleTable ;

    public NodeTupleTableWrapper(NodeTupleTable ntt)
    { 
        setNodeTupleTable(ntt) ;
    }
    
    protected NodeTupleTable setNodeTupleTable(NodeTupleTable ntt)
    {
        NodeTupleTable old = nodeTupleTable ;
        nodeTupleTable = ntt ;
        return old ;
    }
     
    @Override
    public boolean addRow(Node... nodes)
    { return nodeTupleTable.addRow(nodes) ; }

    @Override
    public boolean deleteRow(Node... nodes)
    { return nodeTupleTable.deleteRow(nodes) ; }

    @Override
    public Iterator<Tuple<Node>> find(Node... nodes)
    { return nodeTupleTable.find(nodes) ; }
    
    @Override
    public Iterator<Tuple<NodeId>> find(NodeId... ids)
    { return nodeTupleTable.find(ids) ; }
    
    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> tuple)
    { return nodeTupleTable.find(tuple) ; }
    
    @Override
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes)
    { return nodeTupleTable.findAsNodeIds(nodes) ; }

    @Override
    public Iterator<Tuple<NodeId>> findAll()
    { return nodeTupleTable.findAll() ; }

    @Override
    public NodeTable getNodeTable()
    { return nodeTupleTable.getNodeTable() ; }

    @Override
    public TupleTable getTupleTable()
    { return nodeTupleTable.getTupleTable() ; }

    @Override
    public DatasetControl getPolicy()
    { return nodeTupleTable.getPolicy() ; }
    
    @Override
    public boolean isEmpty()
    { return nodeTupleTable.isEmpty() ; }
    
    @Override
    public void clear()
    { nodeTupleTable.clear(); }

    @Override
    public long size()
    { return nodeTupleTable.size() ; }

    @Override
    public void sync()
    { nodeTupleTable.sync() ; }

    @Override
    public void close()
    { nodeTupleTable.close() ; }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
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