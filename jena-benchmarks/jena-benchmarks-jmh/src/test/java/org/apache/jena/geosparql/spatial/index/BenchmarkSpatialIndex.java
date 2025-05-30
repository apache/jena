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
package org.apache.jena.geosparql.spatial.index;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator.GeometryType;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;
import org.locationtech.jts.geom.Envelope;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Benchmarking of the spatial index.
 * Evaluates the time it takes to load an index from disk.
 */
@State(Scope.Benchmark)
public class BenchmarkSpatialIndex {
    @Param({
        "current",
        "5.1.0"
    })
    public String param0_jenaVersion;

    @Param({
          "1000",
         "10000",
        "100000",
    })
    public long param1_geometryMixes;

    @Param({
        // "",
        SRS_URI.DEFAULT_WKT_CRS84
    })
    public String param2_srs;

    private SpatialIndexLifeCycle spatialIndexLifeCycle;

    @Benchmark
    public void load() throws Exception {
        spatialIndexLifeCycle.load();
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        Envelope envelope = new Envelope(-175, 175, -85, 85);
        Map<GeometryType, Number> config = GeometryGenerator.createConfig(param1_geometryMixes);
        Graph graph = GraphFactory.createDefaultGraph();
        GeometryGenerator.generateGraph(graph, envelope, config);

        String data;
        RDFFormat fmt = RDFFormat.TURTLE_PRETTY;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFDataMgr.write(out, graph, fmt);
            out.flush();
            data = new String(out.toByteArray(), StandardCharsets.UTF_8);
        }

        String srs = param2_srs.isEmpty() ? null : param2_srs;

        switch (param0_jenaVersion) {
        case "current":
            spatialIndexLifeCycle = SpatialIndexCurrent.setup(data, envelope, srs, false);
            break;
        case "5.1.0":
            spatialIndexLifeCycle = SpatialIndex510.setup(data, envelope, srs, false);
            break;
        default:
            throw new RuntimeException("No task registered for this jena version:" + param0_jenaVersion);
        }

        spatialIndexLifeCycle.init();
        spatialIndexLifeCycle.findSrs();
        spatialIndexLifeCycle.build();
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() throws Exception {
        spatialIndexLifeCycle.close();
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
                .warmupIterations(5)
                .measurementIterations(5)
                .measurementTime(TimeValue.NONE)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                .jvmArgs("-Xmx8G")
                //.addProfiler(WinPerfAsmProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result(c.getSimpleName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".json");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = getDefaults(BenchmarkSpatialIndex.class).build();
        new Runner(opt).run();
    }
}
