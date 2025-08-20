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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;

/**
 * This test is not a benchmark but a test to ensure that the test methods from {@link TestJenaSystem} are executed
 * in a fresh JVM.
 */
@State(Scope.Benchmark)
public class TestJenaSystemWithFreshJVM {

    static final TestJenaSystem testJenaSystem = new TestJenaSystem();

    /**
     * Test that the first initialization of JenaSystem is successful when called in parallel.
     * <p>
     * Executes test code of {@link TestJenaSystem#initParallel()}.
     */
    @Benchmark
    public void initParallel() {
        testJenaSystem.initParallel();
    }

    /**
     * Test that the first initialization of JenaSystem is successful.
     * <p>
     * Executes test code of {@link TestJenaSystem#init()}.
     */
    @Benchmark
    public void init() {
        testJenaSystem.init();
    }

    /**
     * This test ensures that the initialization of {@link RDFConnectionFuseki} is successful.
     * It is a regression test for <a href="https://github.com/apache/jena/issues/2787">GitHub issue 2787</a>.
     * <p>
     * Executes test code of {@link TestJenaSystem#initRDFConnectionFuseki()}.
     */
    @Benchmark
    public void initRDFConnectionFuseki() {
        testJenaSystem.initRDFConnectionFuseki();
    }

    /**
     * This test uses JMH to run the test methods from {@link TestJenaSystem} in a fresh JVM.
     * This is necessary for maven builds to ensure that the JVM is fresh.
     */
    @Test
    public void initInFreshJVM() throws Exception {
        var opt = new OptionsBuilder()
                .include(this.getClass().getName())
                .mode(Mode.SingleShotTime)
                .verbosity(VerboseMode.SILENT)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.NONE)
                .warmupIterations(0)        // we don't need warmup
                .measurementIterations(1)   // we only need one iteration
                .measurementTime(TimeValue.NONE)
                .threads(1)                 // we only need one thread
                .forks(1)                   // we only need one fork
                .shouldFailOnError(true)    // this is important to fail the test if the benchmark fails
                .jvmArgs("-Xmx1G")
                .timeout(TimeValue.seconds(6)) // 6 seconds should be enough, even on slow machines
                .build();
        var results = new Runner(opt).run();
        assertNotNull(results);
    }
}
