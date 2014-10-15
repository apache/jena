/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.store.nodetupletable;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.ArrayUtils ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleTable ;

/** (Read-only?) projection of another NodeTupleTable. 
 * This will not reduce a N-wide tuple to N-1 when find*() used. 
 */ 
public class NodeTupleTableView extends NodeTupleTableWrapper
{
    private Node prefix ;
    private NodeId prefixId ;
    //private boolean readOnly = false ;

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

//    @Override
//    public boolean isReadOnly() { return readOnly ; }
//
//    @Override
//    public void setReadOnly(boolean mode)   { readOnly = mode ; }

    @Override
    public TupleTable getTupleTable()
    // Need a projection of this?
    { return super.getTupleTable() ; }
}
