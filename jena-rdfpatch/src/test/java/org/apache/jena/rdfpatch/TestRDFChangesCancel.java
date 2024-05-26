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

package org.apache.jena.rdfpatch;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdfpatch.changes.PatchSummary;
import org.apache.jena.rdfpatch.changes.RDFChangesCounter;
import org.apache.jena.rdfpatch.system.DatasetGraphChanges;
import org.apache.jena.rdfpatch.system.RDFChangesSuppressEmpty;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestRDFChangesCancel {

    private final RDFChangesCounter counter;
    private final DatasetGraph dsg;
    {
        counter = new RDFChangesCounter();
        RDFChanges c = new RDFChangesSuppressEmpty(counter);
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();
        dsg = new DatasetGraphChanges(dsg0, c);
    }

    @Before public void beforeTest() { }

    @After public void afterTest() { }

    private static Quad quad1 = SSE.parseQuad("(:g _:s <p> 1)");
    private static Quad quad2 = SSE.parseQuad("(:g _:s <p> 2)");

    private static Triple triple1 = SSE.parseTriple("(_:sx <p1> 11)");
    private static Triple triple2 = SSE.parseTriple("(_:sx <p2> 22)");

    @Test public void changeSuppressEmptyCommit_1() {
        Txn.executeRead(dsg, ()->{});

        PatchSummary s1 = counter.summary();
        assertEquals(0, s1.getCountTxnBegin());
        assertEquals(0, s1.getCountTxnCommit());
        assertEquals(0, s1.getCountTxnAbort());
    }

    @Test public void changeSuppressEmptyCommit_2() {
        Txn.executeWrite(dsg, ()->{});

        PatchSummary s1 = counter.summary();
        assertEquals(1, s1.getCountTxnBegin());
        assertEquals(0, s1.getCountTxnCommit());
        assertEquals(1, s1.getCountTxnAbort());
    }

    @Test public void changeSuppressEmptyCommit_3() {
        Txn.executeWrite(dsg, ()->dsg.add(quad1));

        PatchSummary s1 = counter.summary();
        assertEquals(1, s1.getCountTxnBegin());
        assertEquals(1, s1.getCountTxnCommit());
        assertEquals(0, s1.getCountTxnAbort());
    }


    @Test public void changeSuppressEmptyCommit_4() {
        Quad q = SSE.parseQuad("(_ :s :p 'object')");
        Triple t = SSE.parseTriple("(:t :p 'object')");

        Txn.executeRead(dsg,   ()->{});
        testCounters(counter.summary(), 0, 0);

        Txn.executeWrite(dsg,  ()->{dsg.add(q);});
        testCounters(counter.summary(), 1, 0);

        Txn.executeWrite(dsg,  ()->{dsg.getDefaultGraph().add(t);});
        testCounters(counter.summary(), 2, 0);

        Txn.executeWrite(dsg,  ()->{dsg.getDefaultGraph().getPrefixMapping().setNsPrefix("", "http://example/");});
        testCounters(counter.summary(), 2, 1);

        Txn.executeWrite(dsg,  ()->{});
        testCounters(counter.summary(), 2, 1);
    }

    private static void testCounters(PatchSummary s, long dataAddCount, long prefixAddCount) {
        assertEquals("dataAddCount",   dataAddCount,   s.getCountAddData());
        assertEquals("prefixAddCount", prefixAddCount, s.getCountAddPrefix());
    }

}
