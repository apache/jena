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

package org.apache.jena.rdflink;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfconnection.Isolation;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestLocalIsolation {

    private static Node subject     = NodeFactory.createBlankNode();
    private static Node property    = NodeFactory.createURI("http://example/p");
    private static Node object      = NodeFactory.createURI("http://example/o");

    @Test public void localIsolation_model_1() {
        isolationModel(Isolation.COPY,false);
    }

    @Test public void localIsolation_model_2() {
        isolationModel(Isolation.NONE, true);
    }

    @Test(expected=JenaException.class)
    public void localIsolation_model_3() {
        isolationModel(Isolation.READONLY, true);
    }

    @Test public void localIsolation_dataset_1() {
        isolationDataset(Isolation.COPY,false);
    }

    @Test public void localIsolation_dataset_2() {
        isolationDataset(Isolation.NONE,true);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void localIsolation_dataset_3() {
        isolationDataset(Isolation.READONLY, true);
    }

    private void isolationDataset(Isolation isolation, boolean expected) {
        DatasetGraph base = DatasetGraphFactory.createTxnMem();
        RDFLink link1 = RDFLinkFactory.connect(base, isolation);
        Quad quad = SSE.parseQuad("(:g :s :p :o)") ;
        try (RDFLink link2 = link1;) {
            DatasetGraph dsg = link2.getDataset();
            dsg.add(quad);
        }
        assertEquals(expected, base.contains(quad));
    }

    private void isolationModel(Isolation level, boolean expected) {
        DatasetGraph base = DatasetGraphFactory.createTxnMem();
        Triple triple = Triple.create(subject, property, object);
        RDFLink link1 = RDFLinkFactory.connect(base, level);
        try (RDFLink link2 = link1;) {
            Graph m = link2.get();
            m.add(triple);
        }
        assertEquals(expected, base.getDefaultGraph().contains(triple));
    }
}
