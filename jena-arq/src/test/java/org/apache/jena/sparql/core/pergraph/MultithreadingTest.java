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

package org.apache.jena.sparql.core.pergraph;

import static com.jayway.awaitility.Awaitility.waitAtMost;
import static com.jayway.awaitility.Duration.TEN_SECONDS;
import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;
import org.junit.Test;
import org.slf4j.Logger;

public class MultithreadingTest extends BaseTest {

    private static final Logger log = getLogger(MultithreadingTest.class);

    private static final Node dummy = createURI("info:test");

    private static final Node graph1 = createURI("info:graph1");

    private static final Node graph2 = createURI("info:graph2");

    @Test
    public void loadTwoGraphsAtOnce() {
        DatasetGraphPerGraphLocking dataset = new DatasetGraphPerGraphLocking();

        // We start a thread loading a graph, then wait for the main thread to start loading a different graph. The
        // first thread must wait to see that the main thread has successfully started loading its graph to finish its
        // load. So when the first thread does finish, this proves that two graphs were being loaded simultaneously.

        AtomicBoolean startMain = new AtomicBoolean(), baton = new AtomicBoolean(), finishLine = new AtomicBoolean();

        new Thread(() -> {
            dataset.begin(WRITE);
            try {
                dataset.add(graph1, dummy, dummy, createLiteral("before"));
                // wait for the baton
                startMain.set(true);
                waitAtMost(TEN_SECONDS).untilTrue(baton);
                dataset.add(graph1, dummy, dummy, createLiteral("after"));
                dataset.commit();
                finishLine.set(true);
            } finally {
                dataset.end();
            }
        }).start();

        waitAtMost(TEN_SECONDS).untilTrue(startMain);
        dataset.begin(WRITE);
        try {
            dataset.add(graph2, dummy, dummy, createLiteral("before"));
            // pass the baton
            baton.set(true);
            dataset.add(graph2, dummy, dummy, createLiteral("after"));
            dataset.commit();
        } finally {
            dataset.end();
        }
        waitAtMost(TEN_SECONDS).untilTrue(finishLine);
        dataset.begin(READ);
        try {
            assertTrue("Failed to find the triple that proves that the first thread finished!",
                    dataset.contains(graph1, dummy, dummy, createLiteral("after")));
        } finally {
            dataset.end();
        }
    }

}
