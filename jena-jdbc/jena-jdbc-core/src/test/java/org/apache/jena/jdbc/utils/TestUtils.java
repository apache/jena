/**
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

import java.util.Iterator;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Test utility methods
 */
public class TestUtils {

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

        Dataset ds = DatasetFactory.createMem();
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
        TestUtils.copyDataset(source, target, false);
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
            Iterator<Quad> quads = source.asDatasetGraph().find(Quad.defaultGraphIRI, Node.ANY, Node.ANY, Node.ANY);
            DatasetGraph targetDSG = target.asDatasetGraph();
            while (quads.hasNext()) {
                targetDSG.add(quads.next());
            }
        } else {
            target.setDefaultModel(source.getDefaultModel());
        }

        // Copy named graphs
        Iterator<String> uris = source.listNames();
        while (uris.hasNext()) {
            String uri = uris.next();
            target.addNamedModel(uri, source.getNamedModel(uri));
        }
    }

    /**
     * Copies a dataset to a remote service that provides SPARQL 1.1 Graph Store
     * protocol support
     * 
     * @param source
     *            Source Dataset
     * @param service
     *            Remote Graph Store protocol service
     */
    public static void copyToRemoteDataset(Dataset source, String service) {
        copyToRemoteDataset(source, service, null);
    }

    /**
     * Copies a dataset to a remote service that provides SPARQL 1.1 Graph Store
     * protocol support
     * 
     * @param source
     *            Source Dataset
     * @param service
     *            Remote Graph Store protocol service
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void copyToRemoteDataset(Dataset source, String service, HttpAuthenticator authenticator) {
        DatasetAccessor target = DatasetAccessorFactory.createHTTP(service, authenticator);
        target.putModel(source.getDefaultModel());
        Iterator<String> uris = source.listNames();
        while (uris.hasNext()) {
            String uri = uris.next();
            target.putModel(uri, source.getNamedModel(uri));
        }
        
    }

    /**
     * Renames a graph of a dataset producing a new dataset so as to not modify
     * the original dataset
     * 
     * @param ds
     *            Dataset
     * @param oldUri
     *            Old URI
     * @param newUri
     *            New URI
     * @return New Dataset
     */
    public static Dataset renameGraph(Dataset ds, String oldUri, String newUri) {
        Dataset dest = DatasetFactory.createMem();
        if (oldUri == null) {
            // Rename default graph
            dest.addNamedModel(newUri, ds.getDefaultModel());
        } else {
            // Copy across default graph
            dest.setDefaultModel(ds.getDefaultModel());
        }

        Iterator<String> uris = ds.listNames();
        while (uris.hasNext()) {
            String uri = uris.next();
            if (uri.equals(oldUri)) {
                // Rename named graph
                if (newUri == null) {
                    dest.setDefaultModel(ds.getNamedModel(oldUri));
                } else {
                    dest.addNamedModel(newUri, ds.getNamedModel(oldUri));
                }
            } else {
                // Copy across named graph
                dest.addNamedModel(uri, ds.getNamedModel(uri));
            }
        }

        return dest;
    }

}
