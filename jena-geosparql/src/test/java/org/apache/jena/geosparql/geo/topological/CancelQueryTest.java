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

package org.apache.jena.geosparql.geo.topological;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.index.IndexConfiguration;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CancelQueryTest {

    private final int numGeometries;

    public CancelQueryTest(int numGeometries) {
        this.numGeometries = numGeometries;
    }

    @Parameterized.Parameters(name = "number of geometries: {0}")
    public static List<Integer> sizes() {
        return List.of(
            31_623 // The square root of 1 billion approximated as an integer.
        );
    }

    public static Graph createSpatialGraph(int numGeometries) {
        Graph graph = GraphFactory.createDefaultGraph();

        IntStream.range(0, numGeometries).forEach(i -> {
            // features
            graph.add(NodeFactory.createURI("http://www.example.org/r" + i), RDF.type.asNode(), Geo.FEATURE_NODE);
            // geometries
            graph.add(NodeFactory.createURI("http://www.example.org/r" + i),
                    Geo.HAS_GEOMETRY_PROP.asNode(),
                    NodeFactory.createURI("http://www.example.org/r" + i + "/geometry"));
            // geo:Geometry type triples
            graph.add(NodeFactory.createURI("http://www.example.org/r" + i + "/geometry"),
                    RDF.type.asNode(),
                    Geo.GEOMETRY_NODE);
            // geometry WKT literals
            graph.add(NodeFactory.createURI("http://www.example.org/r" + i + "/geometry"),
                    Geo.AS_WKT_PROP.asNode(),
                    NodeFactory.createLiteralDT("POINT(2 2)", WKTDatatype.INSTANCE));
        });
        // System.out.printf("created graph with %d triples and %d geometries\n", graph.size(), numGeometries);

        return graph;
    }

    @Test(timeout = 10000)
    public void test_cancel_spatial_property_function1() {
        GeoSPARQLConfig.setup(IndexConfiguration.IndexOption.MEMORY, Boolean.TRUE);

        long cancelDelayMillis = 1000;
        boolean useIndex = true;

        // Create a dataset with spatial triples
        Graph graph = createSpatialGraph(numGeometries);
        Model model = ModelFactory.createModelForGraph(graph);
        Dataset ds = DatasetFactory.create(model);

        // create spatial index
        if (useIndex){
            try {
                SpatialIndex index = SpatialIndexLib.buildSpatialIndex(ds.asDatasetGraph());
                SpatialIndexLib.setSpatialIndex(ds, index);
            } catch (SpatialIndexException e) {
                throw new RuntimeException(e);
            }
        }

        // Create a query that queries for spatial relation with both sides being unbound, thus, pairwise comparison would be needed
        Query query = QueryFactory.create("PREFIX geo: <http://www.opengis.net/ont/geosparql#> SELECT * { ?a geo:sfIntersects ?b . }");
        Callable<QueryExecution> qeFactory = () -> QueryExecution.dataset(ds).query(query).build();//.timeout(2000, TimeUnit.MILLISECONDS).build();

        runAsyncAbort(cancelDelayMillis, qeFactory, CancelQueryTest::doCount);
    }

    private static long doCount(QueryExecution qe) {
        // System.out.println("Executing query ...");
        long counter = 0;
        try (QueryExecution qe2 = qe) {
            ResultSet rs = qe2.execSelect();
            while (rs.hasNext()) {
                rs.next();
                ++counter;
            }
        } finally {
            // System.out.println("Aborted after seeing " + counter + " bindings");
        }
        return counter;
    }

    public static void runAsyncAbort(long cancelDelayMillis, Callable<QueryExecution> qeFactory, Function<QueryExecution, ?> processor) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try (QueryExecution qe = qeFactory.call()){
            Future<?> future = executorService.submit(() -> processor.apply(qe));
            try {
                Thread.sleep(cancelDelayMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // System.out.println("Aborting query execution: " + qe);
            qe.abort();
            try {
                future.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (!(cause instanceof QueryCancelledException)) {
                    e.printStackTrace();
                }
                Assert.assertEquals(QueryCancelledException.class, cause.getClass());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to build a query execution", e);
        } finally {
            // System.out.println("Completed: " + qe);
            executorService.shutdownNow();
        }
    }
}
