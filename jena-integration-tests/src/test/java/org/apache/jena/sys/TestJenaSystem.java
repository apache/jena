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
package org.apache.jena.sys;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;

/**
 * Tests for {@link JenaSystem#init()}.
 * <p>
 * These tests are placed in the integration tests module to ensure that the initialization
 * is successful when multiple modules are loaded.
 * <p>
 * These tests need to be run in a separate JVM to run with a not initialized JenaSystem.
 * This usually is the case when only the test method is directly executed in the IDE.
 * For the maven build, we call these test methods from {@link TestJenaSystemWithFreshJVM}
 * to ensure that the JVM is fresh.
 */
public class TestJenaSystem {

    /**
     * Tests that the first initialization of JenaSystem is successful.
     * <p>
     * For the maven build, we use this method in {@link TestJenaSystemWithFreshJVM#init()}
     * to ensure that the JVM is fresh.
     */
    @Test
    public void init() {
        assertDoesNotThrow(
                () -> JenaSystem.init());
    }

    /**
     * Tests that the first initialization of {@link JenaSystem} is even successful when called in parallel.
     * <p>
     * This code was able to reproduce a deadlock for <a href="https://github.com/apache/jena/issues/2787">GitHub issue 2787</a>,
     * which is now fixed.
     * <p>
     * The deadlock was observed when {@link JenaSystem} was initialized in parallel with
     * {@link ModelFactory#createDefaultModel()} or other classes that use a static initializer calling
     * {@link JenaSystem#init}. This test is used to reproduce the deadlock and ensure that it is fixed.
     * <p>
     * For the maven build, we use this method in  {@link org.apache.jena.sys.TestJenaSystemWithFreshJVM#initParallel()}
     * to ensure that the JVM is fresh.
     */
    @Test
    public void initParallel() {

        var pool = Executors.newFixedThreadPool(2);
        try {
            var futures = IntStream.range(1, 3)
                    .mapToObj(i -> pool.submit(() -> {
                        if (i % 2 == 0) {
                            ModelFactory.createDefaultModel();
                        } else {
                            JenaSystem.init();
                        }

                        return i;
                    }))
                    .toList();

            var intSet = new HashSet<Integer>();
            assertTimeoutPreemptively(
                    Duration.of(4, ChronoUnit.SECONDS),
                    () -> {
                        for (var future : futures) {
                            intSet.add(future.get());
                        }
                    });

            assertEquals(2, intSet.size());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * This test ensures that the initialization of {@link RDFConnectionFuseki} is successful.
     * This test is located here because the initialization of {@link RDFConnectionFuseki} depends on the proper
     * initialization of {@link JenaSystem}.
     * It is a regression test for <a href="https://github.com/apache/jena/issues/2787">GitHub issue 2787</a>.
     * <p>
     * The issue was:
     * When the static initialization of {@link JenaSystem} was missing in {@link org.apache.jena.rdflink.RDFLinkHTTPBuilder},
     * the initialization of {@link RDFConnectionFuseki} failed with a {@link java.lang.ExceptionInInitializerError}
     * caused by a {@link java.lang.NullPointerException}.
     * <p>
     * For the maven build, we use this method in {@link TestJenaSystemWithFreshJVM#initRDFConnectionFuseki()}
     */
    @Test
    public void initRDFConnectionFuseki() {
        try (RDFConnection conn = RDFConnectionFuseki.service("http://localhost:3030/ds").build()) {
            assertTrue(true);
        }
    }
}
