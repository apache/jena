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

import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Test consistency of DatasetGraph.find(g, s, p, o) w.r.t. an RDFS setup.
 * <p>
 *
 * <b>BEWARE:</b> find() is used to produce the reference data. Errors in the reference data
 * will likely cause otherwise correctly functioning tests to fail.
 * <p>
 *
 * (1) Materializes the result of dsg.find() into a reference dataset.
 * (2) Invokes find(g, s, p, o) with all combinations and compares the results.
 */
public abstract class AbstractTestRDFS_Find
    extends AbstractDatasetGraphCompare
{
    public AbstractTestRDFS_Find(String testLabel) {
        super(testLabel);
    }

    /** Some RDFS reasoners so far produce duplicates which fail cardinality tests. */
    @Override
    protected boolean defaultCompareAsSet() {
        return true;
    }

    /** Sub classes need to implement this method and return a DatasetGraph with RDFS inferencing. */
    protected abstract DatasetGraph applyRdfs(DatasetGraph dsg, ConfigRDFS<Node> configRDFS);

    /**
     * Prepare test cases.
     *
     * @param schemaStr SSE expression that parses as a {@link Graph}.
     * @param dataStr SSE expression that parses as a {@link DatasetGraph}.
     * @return A builder for the concrete test instances.
     */
    public GraphFindTestBuilder prepareRdfsFindTestsSSE(String schemaStr, String dataStr) {
        Graph graph = SSE.parseGraph(schemaStr);
        DatasetGraph inputDsg = SSE.parseDatasetGraph(dataStr);
        return prepareRdfsFindTests(graph, inputDsg);
    }

    /**
     * Prepare test cases.
     *
     * @param schemaStr RDF data in TRIG syntax that parses as a {@link Graph}.
     * @param dataStr RDF data in TRIG syntax that parses as a {@link DatasetGraph}.
     * @return A builder for the concrete test instances.
     */
    public GraphFindTestBuilder prepareRdfsFindTestsTrig(String schemaStr, String dataStr) {
        Graph schemaGraph = RDFParser.fromString(schemaStr, Lang.TTL).toGraph();
        SetupRDFS setup = RDFSFactory.setupRDFS(schemaGraph);

        DatasetGraph inputDsg = RDFParser.fromString(dataStr, Lang.TRIG).toDatasetGraph();
        return prepareRdfsFindTests(setup, inputDsg);
    }

    public GraphFindTestBuilder prepareRdfsFindTests(Graph schemaGraph, DatasetGraph inputDsg) {
        SetupRDFS setup = RDFSFactory.setupRDFS(schemaGraph);
        return prepareRdfsFindTests(setup, inputDsg);
    }

    public GraphFindTestBuilder prepareRdfsFindTests(ConfigRDFS<Node> configRDFS, DatasetGraph inputDsg) {
        DatasetGraph testDsg = applyRdfs(inputDsg, configRDFS);

        // Build reference data by materializing a copy of testDsg via findAll().
        DatasetGraph referenceDsg = DatasetGraphFactory.create();
        referenceDsg.prefixes().putAll(testDsg.prefixes());
        referenceDsg.addAll(testDsg);

        // dataDsg that is the source for substituting placeholders
        // in the quads that will be used to test find(quad) calls.
        DatasetGraph dataDsg = DatasetGraphFactory.create();
        dataDsg.prefixes().putAll(referenceDsg.prefixes());
        dataDsg.addAll(referenceDsg);

        boolean debugPrintReferenceData = false;
        if (debugPrintReferenceData) {
            RDFDataMgr.write(System.out, referenceDsg, RDFFormat.TRIG_PRETTY);
        }

        // Add all (non-literal) objects to the source Data for more extensive testing of lookups.
        try (Stream<Quad> stream = dataDsg.stream()) {
            List<Quad> extra = stream.flatMap(q -> Stream.of(q.getPredicate(), q.getObject())
                    .map(x -> Quad.create(q.getGraph(), x, RDF.Nodes.type, RDFS.Nodes.Resource)))
                    .toList();
            extra.forEach(dataDsg::add);
        }

        return prepareFindTests(referenceDsg, testDsg, referenceDsg);
    }
}
