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
package org.apache.jena.sparql.engine.join;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/** Benchmark implementation for hash joins. The junit runner is {@link TestBenchmarkHashJoin}. */
@State(Scope.Benchmark)
public class BenchmarkHashJoin {
    @Param({
        "current",
        "4.8.0"
    })
    public String param0_jenaVersion;

    // TODO Can we pass expected results to jmh?
    @Param({
            "join/join_2columns_simple_a_10.rq", // expected count:  10000
            "join/join_2columns_simple_a_15.rq", // expected count:  50625
            "join/join_2columns_simple_a_20.rq", // expected count: 160000
            // "join/perf_30.rq",
            "join/join_2columns_skewed_a_1.rq",  // expected count: 2736
            "join/join_matrix_skewed_a_10.rq"    // expected count: 100
    })
    public String param1_queryFile;

    private Callable<QueryExecResultData> task;

    @Benchmark
    public void runTask() throws Exception {
        QueryExecResultData data = task.call();
        System.err.println(String.join("\n",
            "Query exec result:",
            // "Query:", data.getOptimizedOpString(),
            "Result count:", Long.toString(data.getResultCount())));
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        URL url = getClass().getClassLoader().getResource(param1_queryFile);
        Objects.requireNonNull(url, "Resource not found: " + param1_queryFile);
        String queryString = Files.readString(Paths.get(url.toURI()));
        QueryTaskBuilder taskBuilder = QueryTaskBuilderRegistry.get().get(param0_jenaVersion).get();
        task = taskBuilder.query(queryString).build();
    }

    public static ChainedOptionsBuilder getDefaults(Class<?> c) {
        return new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(c.getName())
                // Set the following options as needed
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.NONE)
                .warmupIterations(1)
                .measurementIterations(1)
                .measurementTime(TimeValue.NONE)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                .jvmArgs("-Xmx12G")
                //.addProfiler(WinPerfAsmProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result(c.getSimpleName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".json");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = getDefaults(BenchmarkHashJoin.class).build();
        new Runner(opt).run();
    }
}
