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

package org.apache.jena.tdb2.sys;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.CollectorStreamTriples;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdInline;
import org.junit.Before;
import org.junit.Test;

/**
 * This specifically tests by putting a node into a TDB2 database and getting them
 * back again. It checks that the standalone normalization {@link NormalizeTermsTDB2}
 * and the outcome of adding, then reading from the database, result in the same
 * (Java .equals) {@link Node}.
 */
public class TestNormalizedTermsTDB2 {

    private static Node s = SSE.parseNode(":s");
    private static Node p = SSE.parseNode(":p");

    static DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
    @Before public void beforeTest() {
        dsg.executeWrite(()->dsg.clear());
    }

    // '50.8162899'^^xsd:float rounds to 50.816288
    // '50.8162900'^^xsd:float rounds to 50.81629
    // '50.8162864123456789'^^xsd:double rounds to ""50.816286412345676e0"^^xsd:double

    @Test public void tdb_float_01() { test("'1'^^xsd:float"); }
    @Test public void tdb_float_02() { test("'50.8162899'^^xsd:float"); }   // 50.816288
    @Test public void tdb_float_03() { test("'50.8162900'^^xsd:float"); }   // 50.81629
    @Test public void tdb_float_04() { test("'-0'^^xsd:float"); }
    @Test public void tdb_float_05() { test("'-INF'^^xsd:float"); }
    @Test public void tdb_float_06() { test("'NaN'^^xsd:float"); }

    @Test public void tdb_double_01() { test("'1'^^xsd:double"); }
    @Test public void tdb_double_02() { test("'50.8162864123456789'^^xsd:double"); }

    @Test public void tdb_double_04() { test("'-0'^^xsd:double"); }
    @Test public void tdb_double_05() { test("'-INF'^^xsd:double"); }
    @Test public void tdb_double_06() { test("'NaN'^^xsd:double"); }

    @Test public void tdb_blanknode_01() { test("_:a"); }

    private void test(String nodeString) {
        Node nInput = SSE.parseNode(nodeString);
        // As TDB2 sees it.
        Node nNorm = NormalizeTermsTDB2.normalizeTDB2(nInput);

        if ( nInput.isLiteral() ) {
            assertEquals(nInput.getLiteralValue(), nNorm.getLiteralValue());
            assertEquals(nInput.getLiteralDatatype(), nNorm.getLiteralDatatype());
        }

        testViaDatabase(nInput, nNorm);
        testViaStream(nInput, nNorm);
        testDirect(nInput, nNorm);
    }

    private void testViaDatabase(Node nInput, Node nNorm) {
        // Write-read to a database.
        Node nDB = dsg.calculateWrite(()->{
            Triple triple = Triple.create(s, p, nInput);
            dsg.getDefaultGraph().add(triple);

            Node objDB = dsg.getDefaultGraph().find().next().getObject();
            return objDB;

        });
        assertEquals(nNorm,nDB);
    }

    private void testViaStream(Node nInput, Node nNorm) {
        CollectorStreamTriples collector = new CollectorStreamTriples();
        StreamRDF stream = NormalizeTermsTDB2.stream(collector);
        stream.triple(Triple.create(s, p, nInput));
        Triple triple2 = collector.getCollected().get(0);
        Node streamObj = triple2.getObject();
        assertEquals(nNorm, streamObj);
    }

    private void testDirect(Node nInput, Node nNorm) {
        NodeId id = NodeIdInline.inline(nInput);
        if ( id != null ) {
            Node nRTT = NodeIdInline.extract(id);
            assertEquals(nNorm, nRTT);
        }
    }
}
