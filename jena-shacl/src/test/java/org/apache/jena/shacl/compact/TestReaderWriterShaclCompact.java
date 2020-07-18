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

import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;

/** RIOT reader-writer tests for SHACL Compact Syntax */
public class TestReaderWriterShaclCompact  extends AbstractTestShaclCompact{

    @Override
    protected void runTest(String fn, String ttl, String fileBaseName) {
        String uri = IRILib.filenameToIRI(fn);

        Graph graph1 = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(graph1, uri, BASE, Lang.SHACLC);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFDataMgr.write(bout, graph1, Lang.SHACLC);

        Graph graph2 = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(graph2, new ByteArrayInputStream(bout.toByteArray()), Lang.SHACLC);

        assertTrue(graph1.isIsomorphicWith(graph2));

        Graph graph0 = RDFDataMgr.loadGraph(ttl);
//        RDFDataMgr.write(System.out, graph1, Lang.SHACLC);
//        System.out.println("----");
//        RDFDataMgr.write(System.out, graph0, Lang.SHACLC);
//        System.out.println("----");
        assertTrue(graph0.isIsomorphicWith(graph2));

    }


}

