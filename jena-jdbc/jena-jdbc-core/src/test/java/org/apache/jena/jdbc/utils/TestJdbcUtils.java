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

package org.apache.jena.jdbc.utils;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Test utility methods
 */
public class TestJdbcUtils {
    /**
     * Generates a synthetic dataset for testing
     *
     * @param numGraphs
     *            Number of graphs to generate
     * @param triplesPerGraph
     *            Triples per graph
     * @param createDefaultGraph
     *            Whether to generate a default graph
     * @return Synthetic dataset
     */
    public static Dataset generateDataset(int numGraphs, int triplesPerGraph, boolean createDefaultGraph) {
        if (numGraphs <= 0)
            throw new IllegalArgumentException("Number of graphs must be >= 1");
        if (triplesPerGraph <= 0)
            throw new IllegalArgumentException("Number of triples per graph must be >= 1");

        Dataset ds = DatasetFactory.createTxnMem();
        if (createDefaultGraph) {
            numGraphs--;
            Model def = ModelFactory.createDefaultModel();
            for (int i = 1; i <= triplesPerGraph; i++) {
                def.add(def.createStatement(def.createResource("http://default/subject/" + i),
                        def.createProperty("http://default/predicate"), def.createTypedLiteral(i)));
            }
            ds.setDefaultModel(def);
        }

        for (int g = 1; g < numGraphs; g++) {
            Model named = ModelFactory.createDefaultModel();
            for (int i = 1; i <= triplesPerGraph; i++) {
                named.add(named.createStatement(named.createResource("http://named/subject/" + i),
                        named.createProperty("http://named/predicate"), named.createTypedLiteral(i)));
            }
            ds.addNamedModel("http://named/" + g, named);
        }

        return ds;
    }

    /**
     * Copies one dataset to another
     *
     * @param source
     *            Source Dataset
     * @param target
     *            Target Dataset
     */
    public static void copyDataset(Dataset source, Dataset target) {
        TestJdbcUtils.copyDataset(source, target, false);
    }

    /**
     * Copies one dataset to another
     *
     * @param source
     *            Source Dataset
     * @param target
     *            Target Dataset
     * @param copyDefaultAsQuads
     *            Whether the default graph should be copied as quads (required
     *            for TDB datasets)
     *
     */
    public static void copyDataset(Dataset source, Dataset target, boolean copyDefaultAsQuads) {
        // Copy the default graph
        if (copyDefaultAsQuads) {
            DatasetGraph targetDSG = target.asDatasetGraph();
            source.asDatasetGraph().find(defaultGraphIRI, ANY, ANY, ANY).forEachRemaining(targetDSG::add);
        } else {
            target.setDefaultModel(source.getDefaultModel());
        }

        // Copy named graphs
        source.listNames().forEachRemaining(uri->target.addNamedModel(uri, source.getNamedModel(uri)));

    }
}
