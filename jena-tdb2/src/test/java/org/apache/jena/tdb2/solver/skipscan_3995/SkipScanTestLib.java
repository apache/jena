/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver.skipscan_3995;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.solver.skipscan.OpExecutorTDB2SkipScan;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TDBInternal;

/**
 * Helpers for the PR #3995 review tests.
 *
 * <p>The central operation is {@link #assertSkipScanAgrees}: run the same query twice over the
 * same TDB2 database, once with {@link SystemTDB#symSkipScan} off (the reference answer produced
 * by the general machinery) and once with it on, and require the two to agree. It also requires
 * that the skip scan was actually engaged, so a test cannot pass vacuously because the rewrite
 * declined to fire.</p>
 */
public class SkipScanTestLib {

    public static final String PREFIXES = """
        PREFIX :    <http://example/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        """;

    /** Create a TDB2 database and load the given TriG data (the {@link #PREFIXES} are prepended). */
    public static DatasetGraphTDB tdb2(String trigData) {
        DatasetGraphTDB dsg = TDBInternal.getDatasetGraphTDB(TDB2Factory.createDataset());
        Txn.executeWrite(dsg, () -> RDFParser.fromString(PREFIXES + trigData, Lang.TRIG).parse(dsg));
        return dsg;
    }

    /** Execute a query (the {@link #PREFIXES} are prepended) and return the rows. */
    public static List<Binding> rows(DatasetGraphTDB dsg, String queryString, boolean skipScan,
                                     LongAdder okCounter, LongAdder failoverCounter) {
        Query query = QueryFactory.create(PREFIXES + queryString);
        Table table = Txn.calculateRead(dsg, () -> {
            QueryExecBuilder builder = QueryExec.dataset(dsg).query(query).set(SystemTDB.symSkipScan, skipScan);
            if ( okCounter != null )
                builder = builder.set(OpExecutorTDB2SkipScan.symSkipScanOkCounter, okCounter);
            if ( failoverCounter != null )
                builder = builder.set(OpExecutorTDB2SkipScan.symSkipScanFailoverCounter, failoverCounter);
            return builder.table();
        });
        List<Binding> result = new ArrayList<>();
        table.rows().forEachRemaining(result::add);
        return result;
    }

    /** The reference answer: the same query with the skip scan disabled. */
    public static List<Binding> reference(DatasetGraphTDB dsg, String queryString) {
        return rows(dsg, queryString, false, null, null);
    }

    /**
     * Require that the skip scan answers the query exactly as the general machinery does.
     * Also requires that the skip scan was used at least once and never failed over,
     * so that the comparison really does exercise the new code path.
     */
    public static void assertSkipScanAgrees(DatasetGraphTDB dsg, String queryString) {
        List<Binding> expected = reference(dsg, queryString);

        LongAdder ok = new LongAdder();
        LongAdder failover = new LongAdder();
        List<Binding> actual = rows(dsg, queryString, true, ok, failover);

        assertTrue(ok.longValue() > 0,
                   () -> "Skip scan was not used - this test does not exercise the skip scan path: " + queryString);
        assertEquals(0L, failover.longValue(),
                     () -> "Skip scan failed over to the general machinery: " + queryString);
        assertTrue(ListUtils.equalsUnordered(expected, actual),
                   () -> "Skip scan answered differently from the general machinery."
                       + "\n  Query:    " + queryString.strip()
                       + "\n  Expected " + expected.size() + " bindings: " + expected
                       + "\n  Actual   " + actual.size()   + " bindings: " + actual);
    }

    /** The {@link NodeTupleTable} of the default graph (triples). */
    public static NodeTupleTable tripleTable(DatasetGraphTDB dsg) {
        return dsg.getTripleTable().getNodeTupleTable();
    }

    /** Find a triple index by name, e.g. {@code "SPO"}. */
    public static TupleIndex tripleIndex(DatasetGraphTDB dsg, String name) {
        for ( TupleIndex index : tripleTable(dsg).getTupleTable().getIndexes() ) {
            if ( name.equals(index.getName()) )
                return index;
        }
        throw new IllegalArgumentException("No such index: " + name);
    }

    /** The NodeId a term is stored under, or {@link NodeId#NodeDoesNotExist}. */
    public static NodeId nodeId(DatasetGraphTDB dsg, org.apache.jena.graph.Node node) {
        NodeTable nodeTable = tripleTable(dsg).getNodeTable();
        return Txn.calculateRead(dsg, () -> nodeTable.getNodeIdForNode(node));
    }
}
