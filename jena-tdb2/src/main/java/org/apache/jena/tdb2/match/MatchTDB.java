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

import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdfs.engine.MapperX;
import org.apache.jena.rdfs.engine.Match;
import org.apache.jena.tdb2.store.GraphTDB;
import org.apache.jena.tdb2.store.GraphViewSwitchable;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

public class MatchTDB
    implements Match<NodeId, Tuple3<NodeId>>
{
    private GraphTDB graph;
    private MapperX<NodeId, Tuple3<NodeId>> mapper;

    protected MatchTDB(GraphTDB graph, MapperX<NodeId, Tuple3<NodeId>> mapper) {
        super();
        this.graph = graph;
        this.mapper = mapper;
    }

    public static MatchTDB wrap(Graph g) {
        // Same pattern used in stage generator - move to TDBInternal?
        if ( g instanceof GraphViewSwitchable gvs )
            g = gvs.getBaseGraph();

        if (!(g instanceof GraphTDB tdbGraph)) {
            throw new IllegalArgumentException("Not a TDB2 graph");
        }

        NodeTable nodeTable = tdbGraph.getNodeTupleTable().getNodeTable();
        MapperX<NodeId, Tuple3<NodeId>> mapper = new MapperXTDB(nodeTable);
        return new MatchTDB(tdbGraph, mapper);
    }

    @Override
    public Stream<Tuple3<NodeId>> match(NodeId s, NodeId p, NodeId o) {
        return Iter.asStream(graph.getNodeTupleTable().find(s, p, o))
            .filter(t -> {
                boolean b = NodeId.isDoesNotExist(t.get(0)) || NodeId.isDoesNotExist(t.get(1)) || NodeId.isDoesNotExist(t.get(2));
                return !b;
            })
            .map(t -> TupleFactory.create3(t.get(0), t.get(1), t.get(2)));
    }

    @Override
    public MapperX<NodeId, Tuple3<NodeId>> getMapper() {
        return mapper;
    }
}
