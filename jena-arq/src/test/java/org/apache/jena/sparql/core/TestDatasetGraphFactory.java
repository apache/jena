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

package org.apache.jena.sparql.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.graph.NodeConst;

public class TestDatasetGraphFactory {
    // Not covered elsewhere.

    @Test public void withGraphMaker() {
        AtomicInteger makerCount = new AtomicInteger(0);

        DatasetGraphFactory.GraphMaker graphMaker = (name) -> {
            makerCount.incrementAndGet();
            return DatasetGraphFactory.graphMakerMem.create(name);
        };
        DatasetGraph dsg = DatasetGraphFactory.createWithGraphMaker(graphMaker);
        // Default graph.
        assertEquals(1, makerCount.get());

        Node gn = NodeFactory.createURI("http://example/");
        Node x = NodeFactory.createURI("http://example/x");
        dsg.add(gn,x,x,NodeConst.TRUE);
        assertEquals(2, makerCount.get());
        long c1 = Iter.count(dsg.find());
        assertEquals(1, c1);

        Node gn2 = dsg.listGraphNodes().next();
        assertEquals(gn, gn2);

        dsg.add(gn,x,x,NodeConst.FALSE);
        dsg.add(gn,x,x,NodeConst.nodeOne);
        assertEquals(2, makerCount.get());

        long c2 = Iter.count(dsg.find());
        assertEquals(3, c2);
    }
}
