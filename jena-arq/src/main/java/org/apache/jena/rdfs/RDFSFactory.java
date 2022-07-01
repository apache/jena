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

package org.apache.jena.rdfs;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;

/** Factory for data+RDFS inference. */
public class RDFSFactory {

    /**
     * Create an RDFS inference graph with split A-box (data) and T-box (RDFS schema).
     */
    public static Graph graphRDFS(Graph data, Graph vocab) {
        return graphRDFS(data, new SetupRDFS(vocab));
    }

    /**
     * Create an RDFS inference graph over a graph with both A-box (data) and T-box (RDFS schema).
     */
    public static Graph graphRDFS(Graph data) {
        return graphRDFS(data, new SetupRDFS(data));
    }

    /**
     * Create an RDFS inference graph over a graph according to an {@link SetupRDFS}.
     */
    public static Graph graphRDFS(Graph data, SetupRDFS setup) {
        return new GraphRDFS(data, setup);
    }

    /** Create an RDFS inference dataset. */
    public static DatasetGraph datasetRDFS(DatasetGraph data, SetupRDFS setup) {
        return new DatasetGraphRDFS(data, setup);
    }

    /** Create an RDFS inference dataset. */
    public static DatasetGraph datasetRDFS(DatasetGraph data, Graph vocab ) {
        SetupRDFS setup = setupRDFS(vocab);
        return new DatasetGraphRDFS(data, setup);
    }

    /** Create an RDFS inference dataset. */
    public static Dataset datasetRDFS(Dataset data, Graph vocab ) {
        SetupRDFS setup = setupRDFS(vocab);
        return DatasetFactory.wrap(new DatasetGraphRDFS(data.asDatasetGraph(), setup));
    }

    /** Create an {@link SetupRDFS} */
    public static SetupRDFS setupRDFS(Graph vocab) {
        return new SetupRDFS(vocab);
    }

    /** Stream expand data based on a separate vocabulary */
    public static StreamRDF streamRDFS(StreamRDF data, Graph vocab) {
        SetupRDFS setup = new SetupRDFS(vocab);
        return streamRDFS(data, setup);
    }

    /** Expand a stream of RDF using RDFS */
    public static StreamRDF streamRDFS(StreamRDF data, SetupRDFS setup) {
        return new InfStreamRDFS(data, setup);
    }
}
