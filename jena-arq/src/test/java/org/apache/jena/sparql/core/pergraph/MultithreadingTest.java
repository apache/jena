/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.jena.sparql.core.pergraph;

import static com.jayway.awaitility.Awaitility.await;
import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphGraphPerTxn;
import org.apache.jena.sparql.core.JenaTransactionRegionException;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

public class MultithreadingTest extends BaseTest {

    private static final Node after = createLiteral("after");

    private static final Node before = createLiteral("before");

    private static final Node dummy = createURI("info:test");

    private static final Node graph1 = createURI("info:graph1");

    private static final Node graph2 = createURI("info:graph2");

    private static final Node graph3 = createURI("info:graph3");

    private static final Quad[] QUADS = new Quad[] { Quad.create(defaultGraphIRI, dummy, dummy, dummy),
            Quad.create(graph1, dummy, dummy, dummy), Quad.create(graph2, dummy, dummy, dummy),
            Quad.create(graph3, dummy, dummy, dummy) };

    @Test
    public void writeTwoGraphsAtOnce() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();

        // We start a thread loading a graph, then wait for the main thread to start loading a different graph. The
        // first thread must wait to see that the main thread has successfully started loading its graph to finish its
        // load. So when the first thread does finish, this proves that two graphs were being loaded simultaneously.

        AtomicBoolean startMain = new AtomicBoolean(), baton = new AtomicBoolean(), finishLine = new AtomicBoolean();

        new Thread(() -> {
            dataset.begin(WRITE);
            try {
                dataset.add(graph1, dummy, dummy, before);
                // wait for the baton
                startMain.set(true);
                await().untilTrue(baton);
                dataset.add(graph1, dummy, dummy, after);
                dataset.commit();
                finishLine.set(true);
            } finally {
                dataset.end();
            }
        }).start();

        await().untilTrue(startMain);
        dataset.begin(WRITE);
        try {
            dataset.add(graph2, dummy, dummy, before);
            // pass the baton
            baton.set(true);
            dataset.add(graph2, dummy, dummy, after);
            dataset.commit();
        } finally {
            dataset.end();
        }
        await().untilTrue(finishLine);
        dataset.begin(READ);
        try {
            assertTrue("Failed to find the triple that proves that the first thread finished!",
                    dataset.contains(graph1, dummy, dummy, after));
        } finally {
            dataset.end();
        }
    }

    @Test
    public void readSeveralGraphsInOneTxn() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();
        // write some quads into different graphs, using a txn-per-graph
        for (Quad q : QUADS) {
            dataset.begin(WRITE);
            try {
                dataset.add(q);
                dataset.commit();
            } finally {
                dataset.end();
            }
        }
        // check that they are all there in one big txn
        dataset.begin(READ);
        try {
            for (Quad q : QUADS) {
                assertTrue("Couldn't find quad in its graph!", dataset.contains(q));
                assertTrue("Couldn't find quad in its graph!", dataset.getGraph(q.getGraph()).contains(q.asTriple()));
            }
        } finally {
            dataset.end();
        }
    }

    @Test(expected = JenaTransactionRegionException.class)
    public void onlyOneGraphWritableInATxn() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();
        dataset.begin(WRITE);
        try {
            dataset.add(graph2, dummy, dummy, before);
            dataset.add(graph1, dummy, dummy, after);
            fail("Should not have been able to write to two different graphs!");
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @Test
    public void readFromOneGraphWhileWritingToAnother() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();
        // set up some data to read
        dataset.begin(WRITE);
        try {
            dataset.add(graph1, dummy, dummy, dummy);
            dataset.commit();
        } finally {
            dataset.end();
        }
        // try and read the data while writing some more elsewhere
        dataset.begin(WRITE);
        try {
            dataset.add(graph2, dummy, dummy, dummy);
            assertTrue("Couldn't find triple in non-written graph!", dataset.contains(graph1, dummy, dummy, dummy));
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @Test
    public void snapshotIsolation() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();

        // We start a thread loading a graph, then wait for the main thread to start loading a different graph. The
        // first thread must wait to see that the main thread has successfully started loading its graph to finish its
        // load. So when the first thread does finish, this proves that two graphs were being loaded simultaneously.

        AtomicBoolean startMain = new AtomicBoolean(), baton = new AtomicBoolean(), finishLine = new AtomicBoolean();

        new Thread(() -> {
            dataset.begin(WRITE);
            try {
                dataset.add(graph1, dummy, dummy, dummy);
                // wait for the baton
                startMain.set(true);
                await().untilTrue(baton);
                // confirm that the mutation in the other thread is invisible
                assertFalse("Mutation from another thread was visible!", dataset.contains(graph2, dummy, dummy, dummy));
                dataset.commit();
                finishLine.set(true);
            } finally {
                dataset.end();
            }
        }).start();

        await().untilTrue(startMain);
        dataset.begin(WRITE);
        try {
            dataset.add(graph2, dummy, dummy, dummy);
            // pass the baton
            baton.set(true);
            await().untilTrue(finishLine);
            dataset.commit();
        } finally {
            dataset.end();
        }

    }
}
