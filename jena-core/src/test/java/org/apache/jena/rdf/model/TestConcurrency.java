/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.shared.Lock;

/** Test suite to exercise the locking. */
public class TestConcurrency {

    static long SLEEP = 100;
    static int threadCount = 0;

    // Note : reuse the model across tests.
    final static Model model1 = ModelFactory.createDefaultModel();
    final static Model model2 = ModelFactory.createDefaultModel();

    /**
     * The lock nesting cases: outer model and lock, inner model and lock, and whether
     * entering the inner critical section is expected to fail. Lock promotion (READ
     * then WRITE) fails only on the same model, inner and outer.
     */
    static Stream<Arguments> nestingCases() {
        return Stream.of(
            // Same model: inner and outer
            nesting("Lock nesting 1 - same model", model1, Lock.READ,  model1, Lock.READ,  false),
            nesting("Lock nesting 2 - same model", model1, Lock.WRITE, model1, Lock.WRITE, false),
            nesting("Lock nesting 3 - same model", model1, Lock.READ,  model1, Lock.WRITE, true),
            nesting("Lock nesting 4 - same model", model1, Lock.WRITE, model1, Lock.READ,  false),

            // Different model: inner and outer
            nesting("Lock nesting 1 - different models", model1, Lock.READ,  model2, Lock.READ,  false),
            nesting("Lock nesting 2 - different models", model1, Lock.WRITE, model2, Lock.WRITE, false),
            nesting("Lock nesting 3 - different models", model1, Lock.READ,  model2, Lock.WRITE, false),
            nesting("Lock nesting 4 - different models", model1, Lock.WRITE, model2, Lock.READ,  false));
    }

    private static Arguments nesting(String testName, Model outerModel, boolean outerLock,
                                     Model innerModel, boolean innerLock, boolean exceptionExpected) {
        return Arguments.of(Named.of(testName, testName), outerModel, outerLock, innerModel, innerLock, exceptionExpected);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nestingCases")
    public void testLockNesting(String testName, Model outerModel, boolean outerLock,
                                Model innerModel, boolean innerLock, boolean exceptionExpected) {
        boolean gotException = false;
        try {
            outerModel.enterCriticalSection(outerLock);

            try {
                try {
                    // Should fail if outerLock is READ and innerLock is WRITE
                    // and its on the same model, inner and outer.
                    innerModel.enterCriticalSection(innerLock);

                } finally {
                    innerModel.leaveCriticalSection();
                }
            } catch (Exception ex) {
                gotException = true;
            }

        } finally {
            outerModel.leaveCriticalSection();
        }

        if ( exceptionExpected )
            assertTrue(gotException, "Failed to get expected lock promotion error");
        else
            assertTrue(!gotException, "Got unexpected lock promotion error");
    }

    // Crude test
    int threadTotal = 10;

    @Test
    public void testParallel() {
        Model model = ModelFactory.createDefaultModel();
        Thread threads[] = new Thread[threadTotal];

        boolean getReadLock = Lock.READ;
        for ( int i = 0 ; i < threadTotal ; i++ ) {
            String nextId = "T" + Integer.toString(++threadCount);
            threads[i] = new Operation(model, getReadLock);
            threads[i].setName(nextId);
            threads[i].start();

            getReadLock = !getReadLock;
        }

        boolean problems = false;
        for ( int i = 0 ; i < threadTotal ; i++ ) {
            try {
                threads[i].join(200 * SLEEP);
            } catch (InterruptedException intEx) {}
        }

        // Try again for any we missed.
        for ( int i = 0 ; i < threadTotal ; i++ ) {
            if ( threads[i].isAlive() )
                try {
                    threads[i].join(200 * SLEEP);
                } catch (InterruptedException intEx) {}
            if ( threads[i].isAlive() ) {
                System.out.println("Thread " + threads[i].getName() + " failed to finish");
                problems = true;
            }
        }

        assertTrue(!problems, "Some thread failed to finish");
    }

    class Operation extends Thread {
        Model model;
        boolean readLock;

        Operation(Model m, boolean withReadLock) {
            model = m;
            readLock = withReadLock;
        }

        @Override
        public void run() {
            for ( int i = 0 ; i < 2 ; i++ ) {
                try {
                    model.enterCriticalSection(readLock);
                    if ( readLock )
                        readOperation(false);
                    else
                        writeOperation(false);
                } finally {
                    model.leaveCriticalSection();
                }
            }
        }
    }
    // Operations ----------------------------------------------

    volatile int writers = 0;

    // The example model operations
    void doStuff(String label, boolean doThrow) {
        String id = Thread.currentThread().getName();
        // Puase a while to cause other threads to (try to) enter the region.
        try {
            Thread.sleep(SLEEP);
        } catch (InterruptedException intEx) {}
        if ( doThrow )
            throw new RuntimeException(label);
    }

    // Example operations

    public void readOperation(boolean doThrow) {
        if ( writers > 0 )
            System.err.println("Concurrency error: writers around!");
        doStuff("read operation", false);
        if ( writers > 0 )
            System.err.println("Concurrency error: writers around!");
    }

    public void writeOperation(boolean doThrow) {
        writers++;
        doStuff("write operation", false);
        writers--;

    }
}
