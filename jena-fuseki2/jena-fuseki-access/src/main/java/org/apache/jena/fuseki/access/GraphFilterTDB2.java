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

package org.apache.jena.fuseki.access;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

/** {@link GraphFilter} for TDB2 */
class GraphFilterTDB2 extends GraphFilter<NodeId> {

    private GraphFilterTDB2(Collection<NodeId> matches, boolean matchDefaultGraph) {
        super(matches, matchDefaultGraph);
    }

    @Override
    public Symbol getContextKey() {
        return SystemTDB.symTupleFilter;
    }

    /**
     * Create a graph filter for a TDB2 {@link DatasetGraph}. The filter matches (returns
     * true) for Tuples where the graph slot in quad is in the collection or for triples in the default
     * graph according the boolean.
     */
    public static GraphFilterTDB2 graphFilter(DatasetGraph dsg, Collection<Node> namedGraphs, boolean matchDefaultGraph) {
        if ( ! TDBInternal.isTDB2(dsg) )
            throw new IllegalArgumentException("DatasetGraph is not TDB2-backed");
        List<NodeId> x =
            Txn.calculateRead(dsg, ()->{
                NodeTable nt = TDBInternal.getDatasetGraphTDB(dsg).getQuadTable().getNodeTupleTable().getNodeTable();
                return
                    ListUtils.toList(
                        namedGraphs.stream()
                        .map(n->nt.getNodeIdForNode(n))
                        .filter(Objects::nonNull)
                        );
            });
        return new GraphFilterTDB2(x, matchDefaultGraph);
    }
}