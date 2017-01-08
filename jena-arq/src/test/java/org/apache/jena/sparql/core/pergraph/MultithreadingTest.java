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

import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private static final Quad quad1 = Quad.create(graph1, dummy, dummy, dummy);

    private static final Node graph2 = createURI("info:graph2");

    private static final Quad quad2 = Quad.create(graph2, dummy, dummy, dummy);

    private static final Node graph3 = createURI("info:graph3");

    private static final Quad[] QUADS = new Quad[] { Quad.create(defaultGraphIRI, dummy, dummy, dummy), quad1, quad2,
            Quad.create(graph3, dummy, dummy, dummy) };

    @Test
    public void writeTwoGraphsAtOnce() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();

        // We start a thread that begins by waiting for the main thread to start loading a graph. The thread then starts
        // and finishes loading a different graph. We wait for the thread to finish, load some into the first graph and
        // then check that the thread successfully loaded another graph. This proves that two graphs can be loaded by
        // two threads at the same time.

        final Lock baton = new ReentrantLock(true);
        baton.lock();

        Thread otherThread = new Thread(() -> {
            // wait to be allowed to start
            baton.lock();
            dataset.begin(WRITE);
            try {
                dataset.add(graph1, dummy, dummy, before);
                dataset.add(graph1, dummy, dummy, after);
                dataset.commit();
            } finally {
                dataset.end();
            }
            baton.unlock();
        });
        otherThread.start();

        dataset.begin(WRITE);
        try {
            dataset.add(graph2, dummy, dummy, before);
            // let the other thread/txn go forward
            baton.unlock();
            // make sure other thread has finished
            baton.lock();
            dataset.add(graph2, dummy, dummy, after);
            dataset.commit();
        } finally {
            dataset.end();
        }
        dataset.begin(READ);
        try {
            assertTrue("Failed to find a triple that the first thread should have loaded into its graph!",
                    dataset.contains(graph1, dummy, dummy, before));
            assertTrue("Failed to find a triple that the first thread should have loaded into its graph!",
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
            dataset.add(quad1);
            dataset.commit();
        } finally {
            dataset.end();
        }
        // try and read the data while writing some more elsewhere
        dataset.begin(WRITE);
        try {
            dataset.add(quad2);
            assertTrue("Couldn't find triple in non-written graph!", dataset.contains(graph1, dummy, dummy, dummy));
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @Test
    public void readCommittedIsolation() {
        DatasetGraphGraphPerTxn dataset = new DatasetGraphGraphPerTxn();

        final Lock baton = new ReentrantLock(true);
        baton.lock();
        
        Thread otherThread = new Thread(() -> {
            baton.lock();
            dataset.begin(WRITE);
            try {
                dataset.add(quad1);
                assertFalse("Mutation from another thread in another graph visible!", dataset.contains(quad2));
                dataset.commit();
                baton.unlock();
                baton.lock();
                assertTrue("Mutation from another thread in another graph invisible after commit!",
                        dataset.contains(quad2));
            } finally {
                dataset.end();
            }
            baton.unlock();
        });
        otherThread.start();

        dataset.begin(WRITE);
        try {
            dataset.add(quad2);
            baton.unlock();
            baton.lock();
            dataset.commit();
        } finally {
            dataset.end();
        }
        baton.unlock();
        baton.lock();
    }
}
