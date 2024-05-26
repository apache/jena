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

package org.apache.jena.tdb2.loader.main;

import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;
import org.apache.jena.tdb2.loader.base.CoLib;
import org.apache.jena.system.progress.MonitorOutput;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.StoragePrefixesTDB;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

/** Triple to Tuples, without chunking.
 *  Same thread version.
 *  This is a {@link StreamRDF}.
 *  Also loads prefixes.
 */
public class DataToTuplesInlineSingle implements StreamRDFCounting, BulkStartFinish {
    public static final int DataTickPoint   = 100_000;
    public static final int DataSuperTick   = 10;

    private final Consumer<Tuple<NodeId>> dest3;
    private final Consumer<Tuple<NodeId>> dest4;
    private final DatasetGraphTDB dsgtdb;
    private final NodeTable nodeTable;
    private final StoragePrefixesTDB prefixes;

    private final MonitorOutput output;
    // Chunk accumulators.
    private long countTriples = 0;
    private long countQuads = 0;
    private List<Tuple<NodeId>> quads = null;
    private List<Tuple<NodeId>> triples = null;

    public DataToTuplesInlineSingle(DatasetGraphTDB dsgtdb,
                                    Consumer<Tuple<NodeId>> dest3,
                                    Consumer<Tuple<NodeId>> dest4,
                                    MonitorOutput output) {
        this.dsgtdb = dsgtdb;
        this.dest3 = dest3;
        this.dest4 = dest4;
        this.output = output;
        this.nodeTable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
        this.prefixes = (StoragePrefixesTDB)dsgtdb.getStoragePrefixes();
        NodeTable nodeTable2 = dsgtdb.getQuadTable().getNodeTupleTable().getNodeTable();
        if ( nodeTable != nodeTable2 )
            throw new BulkLoaderException("Different node tables");
    }

    // StreamRDF
    private TransactionCoordinator coordinator;
    private Transaction transaction;
    @Override
    public void startBulk() {
        coordinator = CoLib.newCoordinator();
        CoLib.add(coordinator, nodeTable);

        // Prefixes
        NodeTupleTable p = prefixes.getNodeTupleTable();
        CoLib.add(coordinator, p.getNodeTable());
        CoLib.add(coordinator, p.getTupleTable().getIndexes());
        CoLib.start(coordinator);
        transaction = coordinator.begin(TxnType.WRITE);
    }

    @Override
    public void finishBulk() {
        transaction.commit();
        transaction.end();
        CoLib.finish(coordinator);
    }

    @Override public void start() {}

    @Override public void finish() {}

    @Override public long count()           { return countTriples() + countQuads(); }

    @Override public long countTriples()    { return countTriples; }

    @Override public long countQuads()      { return countQuads; }

    @Override
    public void triple(Triple triple) {
        countTriples++;
        Tuple<NodeId> tuple = nodes(nodeTable, triple);
        dest3.accept(tuple);
    }

    @Override
    public void quad(Quad quad) {
        if ( quad.isTriple() || quad.isDefaultGraph() ) {
            triple(quad.asTriple());
            return;
        }
        countQuads++;
        Tuple<NodeId> tuple = nodes(nodeTable, quad);
        dest4.accept(tuple);
    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {
        prefixes.add_ext(Prefixes.nodeDataset, prefix, iri);
    }

    private static Tuple<NodeId> nodes(NodeTable nt, Triple triple) {
        NodeId s = idForNode(nt, triple.getSubject());
        NodeId p = idForNode(nt, triple.getPredicate());
        NodeId o = idForNode(nt, triple.getObject());
        return TupleFactory.tuple(s,p,o);
    }

   private static Tuple<NodeId> nodes(NodeTable nt, Quad quad) {
        NodeId g = idForNode(nt, quad.getGraph());
        NodeId s = idForNode(nt, quad.getSubject());
        NodeId p = idForNode(nt, quad.getPredicate());
        NodeId o = idForNode(nt, quad.getObject());
        return TupleFactory.tuple(g,s,p,o);
    }

    private static final NodeId idForNode(NodeTable nodeTable, Node node) {
        return nodeTable.getAllocateNodeId(node);
    }
}
