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

package org.apache.jena.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.DatasetGraphMapLink;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.transaction.TDBTransactionException;
import org.apache.jena.tdb2.DatabaseMgr;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Tests go simple -> complex
// Does not matter as to their execution, it does help pinpoint what has broken.
/**
 * Test of dataset prefixes.
 * See {@code AbstractTestPrefixMap} for tests of prefix maps in general.
 */

@FixMethodOrder(MethodSorters.JVM)
@RunWith(Parameterized.class)
public class TestDatasetPrefixes {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data(){
        Creator<DatasetGraph> c1 = ()->DatasetGraphFactory.createTxnMem();
        Creator<DatasetGraph> c2 = ()->TDBFactory.createDatasetGraph();
        Creator<DatasetGraph> c3 = ()->DatabaseMgr.createDatasetGraph();
        Creator<DatasetGraph> c4 = ()->new DatasetGraphMap();     //DatasetGraphFactory.create();
        Creator<DatasetGraph> c5 = ()->new DatasetGraphMapLink(GraphFactory.createDefaultGraph()); //DatasetGraphFactory.createGeneral();

        Object[] x1 = { "TIM",  c1 , false, true, true };
        Object[] x2 = { "TDB1", c2 , true, true, true };
        Object[] x3 = { "TDB2", c3 , true, true, true };
        Object[] x4 = { "Map",  c4 , false, false, false };
        Object[] x5 = { "MapLink", c5 , false, false, false };
        return Arrays.asList(x1, x2, x3, x4,x5);
    }

    private final Creator<DatasetGraph> cdsg;
    private final boolean txnIsolation;
    private final boolean supportsPromote;
    private final boolean unifiedPrefixMaps;

    public TestDatasetPrefixes(String name, Creator<DatasetGraph> cdsg,
                               // Do the prefixes provide full isolation?
                               boolean txnIsolation,
                               // Do the prefixes work with transaction promote?
                               boolean supportsPromote,
                               // Single shared prefix map for all graphs and the dataset?
                               boolean unifiedPrefixMaps) {
        this.cdsg = cdsg;
        this.txnIsolation = txnIsolation;
        this.supportsPromote = supportsPromote;
        this.unifiedPrefixMaps = unifiedPrefixMaps;
    }

    private DatasetGraph create() {
        DatasetGraph dsg = cdsg.create();
        // Force into transactional (TDB1)
        Txn.executeWrite(dsg, () -> {});
        return dsg;
    }

    @Test
    public void dsg_prefixes_basic_1() {
        DatasetGraph dsg = create();
        PrefixMap pmap = dsg.prefixes();
        Txn.executeRead(dsg, ()->{
            assertEquals(0, pmap.size());
            assertTrue(pmap.isEmpty());
        });
    }

    @Test
    public void dsg_prefixes_basic_2() {
        DatasetGraph dsg = create();
        Txn.executeWrite(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/");
            String x = pmap.get("ex");
            assertEquals("http://example/", x);
            assertEquals(1, pmap.size());
            assertFalse(pmap.isEmpty());
        });
    }

    @Test
    public void dsg_prefixes_basic_3() {
        DatasetGraph dsg = create();
        Txn.executeWrite(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/");
            pmap.add("ex", "http://example/1");
            String x = pmap.get("ex");
            assertEquals("http://example/1", x);
            assertEquals(1, pmap.size());
            assertFalse(pmap.isEmpty());
        });
    }

    @Test
    public void dsg_prefixes_basic_4() {
        DatasetGraph dsg = create();
        Txn.executeWrite(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/");
            pmap.delete("ex");
            String x = pmap.get("ex");
            assertNull(x);
            assertEquals(0, pmap.size());
            assertTrue(pmap.isEmpty());
        });
    }

    @Test
    public void dsg_prefixes_basic_5() {
        Assume.assumeTrue(unifiedPrefixMaps);
        DatasetGraph dsg = create();
        Txn.executeWrite(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/");
            PrefixMap pmapDft = Prefixes.adapt(dsg.getDefaultGraph().getPrefixMapping());
            String x1 = pmapDft.get("ex");
            assertEquals("http://example/", x1);
            pmapDft.add("ex", "http://example/ns2");

            PrefixMap pmapUnion = Prefixes.adapt(dsg.getUnionGraph().getPrefixMapping());
            String x2 = pmapUnion.get("ex");
            assertEquals("http://example/ns2", x2);

            String x3 = pmap.get("ex");
            assertEquals("http://example/ns2", x2);
        });
    }

    @Test
    public void dsg_prefixes_txn_1() {
        DatasetGraph dsg = create();
        Txn.executeWrite(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/");
        });
        Txn.executeRead(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            String x = pmap.get("ex");
            assertEquals("http://example/", x);
        });
    }

    // Legacy: TDBTransactionException is not under JenaTransactionException.
    @Test(expected = JenaException.class)
    public void dsg_prefixes_txn_2() {
        Assume.assumeTrue(txnIsolation);
        DatasetGraph dsg = create();
        Txn.executeRead(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            try {
                // Write inside read.
                // TIM prefixes are standalone, MRSW so they are thread safe but not tied to the TIM transaction lifecycle.
                // No Isolation.
                pmap.add("ex", "http://example/2");
            } catch (JenaTransactionException | TDBTransactionException ex) {
                throw ex;
            }
        });
    }

    @Test
    public void dsg_prefixes_txn_3() {
        Assume.assumeTrue(supportsPromote);
        DatasetGraph dsg = create();
        Assume.assumeTrue(dsg.supportsTransactionAbort());
        Txn.exec(dsg, TxnType.READ_PROMOTE, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/2");
        });
    }

    @Test
    public void dsg_prefixes_txn_4() {
        Assume.assumeTrue(txnIsolation);
        DatasetGraph dsg = create();
        Txn.executeWrite(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            pmap.add("ex", "http://example/2");
            dsg.abort();
        });
        Txn.executeRead(dsg, () -> {
            PrefixMap pmap = dsg.prefixes();
            String x = pmap.get("ex");
            assertNull(x);
        });
    }
}

