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

package org.apache.jena.shacl.compact;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.compact.reader.ShaclcParseException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

/**
 * Test compact reading and writing by round-tripping. Unlike
 * AbstractTestShaclCompact, this test suite is concerned with
 * round-trip of compact syntax.
 */
public class TestCompactSyntax {
    protected final String DIR = "src/test/files/local/shaclc-syntax/";

    @Test public void roundTrip_01() {
        rttTest("nodeParams.shc");
    }

    @Test public void roundTrip_02() {
        rttTest("propertyParams.shc");
    }

    @Test(expected=ShaclcParseException.class)
    public void badSyntax_01() {
        badSyntax("nodeParam-bad-01.shc");
    }

    private void rttTest(String fn) {
        fn = DIR+fn;
        //String uri = IRILib.filenameToIRI(fn);
        Graph graph1 = RDFDataMgr.loadGraph(fn);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFDataMgr.write(bout, graph1, Lang.SHACLC);

        Graph graph2 = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(graph2, new ByteArrayInputStream(bout.toByteArray()), Lang.SHACLC);

        assertTrue(graph1.isIsomorphicWith(graph2));
    }

    private void badSyntax(String fn) {
        fn = DIR+fn;
        RDFDataMgr.loadGraph(fn);
    }


}
