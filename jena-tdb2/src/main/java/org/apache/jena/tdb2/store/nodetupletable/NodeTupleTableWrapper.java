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

package org.apache.jena.tdb2.store.nodetupletable;

import java.util.Iterator;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.tupletable.TupleTable;

public class NodeTupleTableWrapper implements NodeTupleTable
{
    protected NodeTupleTable nodeTupleTable;

    public NodeTupleTableWrapper(NodeTupleTable ntt) {
        setNodeTupleTable(ntt);
    }

    protected NodeTupleTable setNodeTupleTable(NodeTupleTable ntt) {
        NodeTupleTable old = nodeTupleTable;
        nodeTupleTable = ntt;
        return old;
    }

    @Override
    public void addRow(Node... nodes)
    { nodeTupleTable.addRow(nodes); }

    @Override
    public void deleteRow(Node... nodes)
    { nodeTupleTable.deleteRow(nodes); }

    @Override
    public int getTupleLen()
    { return nodeTupleTable.getTupleLen(); }

    @Override
    public Iterator<Tuple<Node>> find(Node... nodes)
    { return nodeTupleTable.find(nodes); }

    @Override
    public Iterator<Tuple<NodeId>> find(NodeId... ids)
    { return nodeTupleTable.find(ids); }

    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> tuple)
    { return nodeTupleTable.find(tuple); }

    @Override
    public Iterator<Tuple<NodeId>> findAsNodeIds(Node... nodes)
    { return nodeTupleTable.findAsNodeIds(nodes); }

    @Override
    public Iterator<Tuple<NodeId>> findAll()
    { return nodeTupleTable.findAll(); }

    @Override
    public NodeTable getNodeTable()
    { return nodeTupleTable.getNodeTable(); }

    @Override
    public TupleTable getTupleTable()
    { return nodeTupleTable.getTupleTable(); }

    @Override
    public boolean isEmpty()
    { return nodeTupleTable.isEmpty(); }

    @Override
    public void clear()
    { nodeTupleTable.clear(); }

    @Override
    public long size()
    { return nodeTupleTable.size(); }

    @Override
    public void sync()
    { nodeTupleTable.sync(); }

    @Override
    public void close()
    { nodeTupleTable.close(); }
}
