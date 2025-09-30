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
package org.apache.jena.geosparql.query;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator.GeometryType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.system.G;
import org.apache.jena.vocabulary.RDF;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;
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
 * Benchmarking of spatial queries against test data.
 */
@State(Scope.Benchmark)
public class BenchmarkSpatialQueries {

    private static Map<String, String> idToQuery = new LinkedHashMap<>();

    private Node featureNode = NodeFactory.createURI("urn:test:geosparql:feature1");
    private Node geometryNode = NodeFactory.createURI("urn:test:geosparql:geometry1");

    private static final String q1 = """
        PREFIX geo: <http://www.opengis.net/ont/geosparql#>
        PREFIX ogcsf: <http://www.opengis.net/ont/sf#>

        SELECT *
        WHERE {
          ?s geo:sfWithin <urn:test:geosparql:geometry1> .
        }
        """;

    private static final String q2 = """
        PREFIX geo: <http://www.opengis.net/ont/geosparql#>
        PREFIX ogcsf: <http://www.opengis.net/ont/sf#>

        SELECT *
        WHERE {
          ?s a ogcsf:Point .
          ?s geo:sfWithin <urn:test:geosparql:geometry1> .
        }
        """;

    static {
        idToQuery.put("q1", q1);
        idToQuery.put("q2", q2);
    }

    /** Essentially the size of the data. One geometry mix includes every WKT geometry type once (with different coordinates). */
    @Param({
        "10000",
    })
    public long p1_geoMixes;

    @Param({
        "q1",
        "q2",
    })
    public String p2_queryId;

    @Param({
        "off",
        "virtual",
        "materialized"
    })
    public String p3_inferences;

    @Param({
        "false",
        "true"
    })
    public boolean p4_index;

    @Param({
        "current",
        "5.5.0"
    })
    public String p5_jenaVersion;

    private SpatialQueryTask task;

    @Benchmark
    public void run() throws Exception {
        long count = task.exec();
        if (true) {
            System.out.println("Counted: " + count);
        }
    }

    private static GeometryWrapper toWrapperWkt(Geometry geometry) {
        GeometryWrapper result = new GeometryWrapper(geometry, Geo.WKT);
        return result;
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        Envelope dataBbox = new Envelope(-175, 175, -85, 85);
        Map<GeometryType, Number> config = GeometryGenerator.createConfig(p1_geoMixes);
        Graph graph = GraphFactory.createDefaultGraph();
        GeometryGenerator.generateGraph(graph, dataBbox, config);

        // Build a search-bbox by scaling the data-generation-bbox down.
        Geometry dataBboxGeom = CustomGeometryFactory.theInstance().toGeometry(dataBbox);
        double x = dataBboxGeom.getCentroid().getX();
        double y = dataBboxGeom.getCentroid().getY();
        Geometry searchBboxGeom = AffineTransformation.scaleInstance(0.25, 0.25, x, y).transform(dataBboxGeom);

        // Add search bbox and feature/resource to the benchmark data.
        Node searchBboxNode = toWrapperWkt(searchBboxGeom).asNode();
        graph.add(featureNode, Geo.HAS_GEOMETRY_NODE, geometryNode);
        graph.add(geometryNode, Geo.AS_WKT_NODE, searchBboxNode);

        // Post process test data:
        // - Add "geom a Point" triples to geometry resources with a Point WKT literal.
        // - Add explicit Geometry type to all geometry resources (required by jena-geosparql 5.5.0 and earlier).
        Node Point = NodeFactory.createURI("http://www.opengis.net/ont/sf#Point");
        Graph extraGraph = GraphFactory.createDefaultGraph();
        try (Stream<Triple> stream = graph.stream(null, Geo.AS_WKT_NODE, null)) {
            stream.forEach(t -> {
                GeometryWrapper gw = GeometryWrapper.extract(t.getObject());
                String geoType = gw.getGeometryType();
                if (geoType.equals("Point")) {
                    extraGraph.add(t.getSubject(), RDF.Nodes.type, Point);
                }

                extraGraph.add(t.getSubject(), RDF.Nodes.type, Geo.GEOMETRY_NODE);
            });
        }
        G.addInto(graph, extraGraph);

        String data;
        RDFFormat fmt = RDFFormat.TURTLE_PRETTY;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFDataMgr.write(out, graph, fmt);
            out.flush();
            data = new String(out.toByteArray(), StandardCharsets.UTF_8);
        }

        task = switch (p5_jenaVersion) {
        case "current" -> new SpatialQueryTaskCurrent();
        case "5.5.0" -> new SpatialQueryTask550();
        default -> throw new RuntimeException("No task registered for this jena version:" + p5_jenaVersion);
        };

        task.setData(data);

        switch (p3_inferences) {
        case "off": task.setInferenceMode(false, false); break;
        case "virtual": task.setInferenceMode(true, false); break;
        case "materialized": task.setInferenceMode(true, true); break;
        default:
            throw new IllegalArgumentException("Unsupported inference mode: " + p3_inferences);
        }

        task.setIndex(p4_index);

        String queryString = idToQuery.get(p2_queryId);
        task.setQuery(queryString);
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() throws Exception {
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
        Options opt = getDefaults(BenchmarkSpatialQueries.class).build();
        new Runner(opt).run();
    }
}
