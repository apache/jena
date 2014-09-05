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

package com.hp.hpl.jena.tdb.store.nodetupletable ;

import static java.lang.String.format ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.NullIterator ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleTable ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;

/** Group a tuple table and node table together to provide a real NodeTupleTable */
public class NodeTupleTableConcrete implements NodeTupleTable
{
    protected final NodeTable  nodeTable ;
    protected final TupleTable tupleTable ;
    private final DatasetControl dsPolicy ;

    /*
     * Concurrency checking: Everything goes through one of 
     * addRow, deleteRow or find*
     */

    public NodeTupleTableConcrete(int N, TupleIndex[] indexes, NodeTable nodeTable, DatasetControl dsControl)
    {
        if (indexes.length == 0 || indexes[0] == null) throw new TDBException("A primary index is required") ;
        for (TupleIndex index : indexes)
        {
            if (N != index.getTupleLength())
                throw new TDBException(format("Inconsistent: TupleTable width is %d but index %s is %d",
                                              N, index.getMapping(), index.getTupleLength())) ;
        }

        this.dsPolicy = dsControl ;
        this.tupleTable = new TupleTable(N, indexes) ;
        this.nodeTable = nodeTable ;
    }

    private void startWrite()   { dsPolicy.startUpdate() ; }

    private void finishWrite()  { dsPolicy.finishUpdate() ; }

    private void startRead()    { dsPolicy.startRead() ; }

    private void finishRead()   { dsPolicy.finishRead() ; }

    @Override
    public DatasetControl getPolicy()
    { return dsPolicy ; }
    
    @Override
    public boolean addRow(Node... nodes)
    {
        try
        {
            startWrite() ;
            NodeId n[] = new NodeId[nodes.length] ;
            for (int i = 0; i < nodes.length; i++)
                n[i] = nodeTable.getAllocateNodeId(nodes[i]) ;

            Tuple<NodeId> t = Tuple.create(n) ;
            return tupleTable.add(t) ;
        } finally
        {
            finishWrite() ;
        }
    }

    @Override
    public boolean deleteRow(Node... nodes)
    {
        try
        {
            startWrite() ;
            NodeId n[] = new NodeId[nodes.length] ;
            for (int i = 0; i < nodes.length; i++)
            {
                NodeId id = idForNode(nodes[i]) ;
                if (NodeId.isDoesNotExist(id)) return false ;
                n[i] = id ;
            }

            Tuple<NodeId> t = Tuple.create(n) ;
            return tupleTable.delete(t) ;
        } finally
        {
            finishWrite() ;
        }
    }

    /** Find by node. */
    @Override
    public Iterator<Tuple<Node>> find(Node... nodes)
    {
        try {
            startRead() ;
            Iterator<Tuple<NodeId>> iter1 = findAsNodeIds(nodes) ; // **public call
            if (iter1 == null) return new NullIterator<>() ;
            Iterator<Tuple<Node>> iter2 = TupleLib.convertToNodes(nodeTable, iter1) ;
            return iteratorControl(iter2) ;
        } finally { finishRead() ; }
    }

    /**
     * Find by node - return an iterator of NodeIds. Can return "null" (when a
     * node is known to be unknown) for not found as well as NullIterator (when
     * no tuples are found (unknown unknown).
     */
    @Override
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes)
    {
        NodeId n[] = new NodeId[nodes.length] ;
        try {
            startRead() ;
            for (int i = 0; i < nodes.length; i++)
            {
                NodeId id = idForNode(nodes[i]) ;
                if (NodeId.isDoesNotExist(id)) return Iter.nullIterator() ;
                n[i] = id ;
            }
            return find(n) ; // **public call
        } finally { finishRead() ; }
    }

    /** Find by NodeId. */
    @Override
    public Iterator<Tuple<NodeId>> find(NodeId... ids)
    {
        Tuple<NodeId> tuple = Tuple.create(ids) ;
        return find(tuple) ;
    }

    /** Find by NodeId. */
    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> tuple)
    {
        // All find/*, except findAll, comes through this operation so startRead/finishRead/checkIterator only needs to happen here.
        try {
            startRead() ;
            // find worker - need also protect iterators that access the node table.
            Iterator<Tuple<NodeId>> iter = tupleTable.find(tuple) ;
            return iteratorControl(iter) ;
        } finally { finishRead() ; }
    }

    @Override
    public Iterator<Tuple<NodeId>> findAll()
    {
        try {
            startRead() ;
            return iteratorControl(tupleTable.getIndex(0).all()) ;
        } finally { finishRead() ; }
    }

    // ==== Node

    protected final NodeId idForNode(Node node)
    {
        if (node == null || node == Node.ANY) return NodeId.NodeIdAny ;
        if (node.isVariable()) throw new TDBException("Can't pass variables to NodeTupleTable.find*") ;
        return nodeTable.getNodeIdForNode(node) ;
    }

    // ==== Accessors

    /**
     * Return the undelying tuple table - used with great care by tools that
     * directly manipulate internal structures.
     */
    @Override
    public final TupleTable getTupleTable()
    {
        return tupleTable ;
    }

    /** Return the node table */
    @Override
    public final NodeTable getNodeTable()
    {
        return nodeTable ;
    }

    @Override
    public boolean isEmpty()
    {
        return tupleTable.isEmpty() ;
    }

    /** Clear the tuple table - does not clear the node table */
    @Override
    public void clear()
    {
        try {
            startWrite() ;
            tupleTable.clear() ;
        } finally {
            finishWrite() ;
        }
    }

    @Override
    public long size()
    {
        return tupleTable.size() ;
    }

    // @Override
    @Override
    public final void close()
    {
        try
        {
            startWrite() ;
            tupleTable.close() ;
            nodeTable.close() ;
        } 
        finally { finishWrite() ; }
    }

    // @Override
    @Override
    public final void sync()
    {
        try {
            startWrite() ;
            tupleTable.sync() ;
            nodeTable.sync() ;
        } finally { finishWrite() ; }
    }

    private <T> Iterator<T> iteratorControl(Iterator<T> iter) { return dsPolicy.iteratorControl(iter) ; }
}
