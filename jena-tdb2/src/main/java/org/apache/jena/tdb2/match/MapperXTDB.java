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

package org.apache.jena.tdb2.match;

import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.engine.MapperX;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.sys.TDBInternal;

public class MapperXTDB
    implements MapperX<NodeId, Tuple3<NodeId>>
{
    private NodeTable nodeTable;

    protected MapperXTDB(NodeTable nodeTable) {
        super();
        this.nodeTable = nodeTable;
    }

    public static MapperX<NodeId, Tuple3<NodeId>> create(DatasetGraph dsg) {
        DatasetGraphTDB tdb = TDBInternal.getDatasetGraphTDB(dsg);
        if (tdb == null) {
            throw new IllegalArgumentException("Argument must be a TDB2 dataset graph.");
        }
        return new MapperXTDB(tdb.getQuadTable().getNodeTupleTable().getNodeTable());
    }

    @Override public NodeId fromNode(Node n)   { return nodeTable.getNodeIdForNode(n); }
    @Override public Node   toNode  (NodeId x) { return nodeTable.getNodeForNodeId(x); }

    @Override public NodeId subject  (Tuple3<NodeId> tuple) { return tuple.get(0); }
    @Override public NodeId predicate(Tuple3<NodeId> tuple) { return tuple.get(1); }
    @Override public NodeId object   (Tuple3<NodeId> tuple) { return tuple.get(2); }

    @Override public Tuple3<NodeId> tuple(NodeId s, NodeId p, NodeId o) { return TupleFactory.create3(s, p, o); }
}
