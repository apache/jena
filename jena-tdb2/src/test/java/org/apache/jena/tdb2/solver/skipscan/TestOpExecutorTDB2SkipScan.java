/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver.skipscan;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountDistinct;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.CopyDSG;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jspecify.annotations.NonNull;

public class TestOpExecutorTDB2SkipScan
    extends AbstractDatasetGraphCompare
{
    public TestOpExecutorTDB2SkipScan() {
        super("");
    }

    @FunctionalInterface
    interface TestGenerator {
        List<DynamicTest> createTests(DatasetGraph referenceDsg, DatasetGraph testDsg, DatasetGraph dataDsg);
    }

    @TestFactory
    public List<DynamicNode> staticTest01() {
        DatasetGraph referenceDsg = SSE.parseDatasetGraph(
        """
        (dataset
            (graph
              (:s :p :oa)
              (:s :p :ob)
            )
            (graph :g1
              (:s :p :oa)
              (:s :p :ob)
            )
            (graph :g2
              (:s :p :oa)
              (:s :p :ob)
            )
        )
        """);

        return containerForDataset(referenceDsg);
    }

    private List<DatasetSpec> dynamicDatasetSpecs() {
        return List.of(new DatasetSpec("dataset1", 3, 5, 100.0f, 5, 0.5f));
    }

    @TestFactory
    public List<DynamicNode> dynamicTests() {
        List<DynamicNode> result = new ArrayList<>();
        for (DatasetSpec datasetSpec : dynamicDatasetSpecs()) {
            DatasetGraph referenceDsg = generateData(datasetSpec, DatasetGraphFactory.createTxnMem());
            List<DynamicNode> children = containerForDataset(referenceDsg);
            result.add(dynamicContainer(datasetSpec.toString(), children));
        }
        return result;
    }

    private List<DynamicNode> containerForDataset(DatasetGraph referenceDsg) {
        DynamicNode distinctTests = containerForDataset("distinct", referenceDsg, this::createTestsDistinct);
        DynamicNode countVarDistinctTests = containerForDataset("countVarDistinct", referenceDsg, this::createTestsCountVarDistinct);
        return List.of(distinctTests, countVarDistinctTests);
    }

    private DynamicNode containerForDataset(String displayName, DatasetGraph referenceDsg, TestGenerator testGenerator) {
        // dataDsg contains the constants used for generating queries against referenceDsg.
        DatasetGraph dataDsg = createDataDsg(referenceDsg);
        DatasetGraph testDsg = TDBInternal.getDatasetGraphTDB(TDB2Factory.createDataset());
        CopyDSG.copy(referenceDsg, testDsg);
        expectNotEmpty(testDsg);

        addResourceObjectsAsSubjects(dataDsg);

        List<DynamicTest> tests = testGenerator.createTests(referenceDsg, testDsg, dataDsg);
        return dynamicContainer(displayName, tests);
    }

    /**
     * In-place addition of all non-literal objects as subjects using
     * {@code :o a rdfs:Resource} triples.
     * The purpose is more extensive testing of lookups.
     */
    public static void addResourceObjectsAsSubjects(DatasetGraph referenceDsg) {
        DatasetGraph dataDsg = DatasetGraphFactory.create();
        dataDsg.prefixes().putAll(referenceDsg.prefixes());
        dataDsg.addAll(referenceDsg);

        // Add all (non-literal) objects to the source Data for more extensive testing of lookups.
        try (Stream<Quad> stream = dataDsg.stream()) {
            List<Quad> extra = stream.flatMap(q -> Stream.of(q.getPredicate(), q.getObject())
                .map(x -> Quad.create(q.getGraph(), x, RDF.Nodes.type, RDFS.Nodes.Resource)))
                .toList();
            extra.forEach(dataDsg::add);
        }
    }

    /** Sub classes can use this method to generate dynamic tests. */
    public List<DynamicTest> createTestsDistinct(DatasetGraph referenceDsg, DatasetGraph testDsg, DatasetGraph dataDsg) {
        List<Quad> findQuads = createFindQuads(dataDsg).toList();

        // patternToQuery(q) is used to create the actual query from the quad template.
        List<@NonNull DynamicTest> tests = findQuads.stream().map(q -> {
            Query query = createQueryDistinct(q);
            return DynamicTest.dynamicTest(
                getTestLabel() + " " + q,
                new GraphCompareSelectResultExecutable(getTestLabel(), query, referenceDsg, testDsg));
        }).toList();
        return tests;
    }

    /** Sub classes can use this method to generate dynamic tests. */
    public List<DynamicTest> createTestsCountVarDistinct(DatasetGraph referenceDsg, DatasetGraph testDsg, DatasetGraph dataDsg) {
        List<Quad> findQuads = createFindQuads(dataDsg).toList();

        List<@NonNull DynamicTest> tests = findQuads.stream().flatMap(rawQuad -> {
            Quad quad = anyToVar(rawQuad);
            List<Var> vars = new ArrayList<>(4);
            Vars.addVarsFromQuad(vars, quad);

            if (vars.isEmpty()) {
                // Skip test cases with empty vars because they don't hit OpExecutorTDB2Index.tryExec(opGroup, input, execCxt)
                return Stream.of();
            }

            // Permutate all variables - the last one is used for
            // (COUNT(DISTINCT ?v) AS ?c)
            return Iter.asStream(new PermutationIterator<>(vars)).map(vs -> {
                Query query = createQueryCountDistinctVar(quad, vs);
                String queryStr = query.toString().replaceAll("\n", " ").replaceAll(" +", " ");
                return DynamicTest.dynamicTest(
                    getTestLabel() + " " + queryStr,
                    new GraphCompareSelectResultExecutable(getTestLabel(),query, referenceDsg, testDsg));
            });
        }).toList();
        return tests;
    }

    public static Quad anyToVar(Quad quad) {
        Node[] ns = quadToArray(quad);
        Var[] vs = new Var[] {Var.alloc("g"), Var.alloc("s"), Var.alloc("p"), Var.alloc("o")};
        for (int i = 0; i < ns.length; ++i) {
            if (Node.ANY.equals(ns[i])) {
                ns[i] = vs[i];
            }
        }
        return arrayToQuad(ns);
    }

    /** Turn a quad such as (:g :s ?p ?o) into a query SELECT DISTINCT ?p ?o { GRAPH :g { :s ?p ?o } }. */
    public static Query createQueryDistinct(Quad quad) {
        Node[] ns = quadToArray(quad);
        Node[] vs = new Node[] {Var.alloc("g"), Var.alloc("s"), Var.alloc("p"), Var.alloc("o")};

        for (int i = 0; i < ns.length; ++i) {
            if (Node.ANY.equals(ns[i])) {
                ns[i] = vs[i];
            }
        }
        Quad nq = arrayToQuad(ns);
        List<Triple> ts = List.of(nq.asTriple());

        Query q = new Query();
        q.setDistinct(true);
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(new ElementNamedGraph(ns[0], new ElementTriplesBlock(new BasicPattern(ts))));
        return q;
    }

    /**
     * Creates queries such as:
     * <pre>{@code
     * SELECT ?s ?p (COUNT(DISTINCT ?g) AS ?c) {
     *   GRAPH ?g { ?s ?p :o }
     * }
     * GROUP BY ?s ?p
     * }</pre>
     *
     * <p>If {@code vars} is empty then the query becomes:</p>
     * <pre>{@code
     * SELECT (COUNT(DISTINCT *) AS ?c) { GRAPH :g { :s :p :o } }
     * }</pre>
     *
     * @param vars the variables to include in the query.
     *        The last var is used for count distinct.
     *        All other vars are used for grouping and projection.
     */
    public static Query createQueryCountDistinctVar(Quad nq, List<Var> vars) {
        Node g = nq.getGraph();
        List<Triple> ts = List.of(nq.asTriple());

        Query q = new Query();
        Var countVar = Var.alloc("c");
        if (!vars.isEmpty()) {
            List<Var> groupVars = vars.subList(0, vars.size() - 1);
            Var v = vars.getLast();
            groupVars.forEach(q::addGroupBy);
            q.addProjectVars(groupVars);
            Expr countDistinctExpr = q.allocAggregate(new AggCountVarDistinct(new ExprVar(v)));
            q.getProject().add(countVar, countDistinctExpr);
        } else {
            Expr countDistinctExpr = q.allocAggregate(new AggCountDistinct());
            q.getProject().add(countVar, countDistinctExpr);
        }

        q.setQuerySelectType();
        q.setQueryPattern(new ElementNamedGraph(g, new ElementTriplesBlock(new BasicPattern(ts))));
        return q;
    }

    private static Node[] quadToArray(Quad quad) {
        return new Node[] {quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject() };
    }

    private static Quad arrayToQuad(Node[] arr) {
        return Quad.create(arr[0], arr[1], arr[2], arr[3]);
    }

    /* ----- Dataset Generation ----- */

    record DatasetSpec(String displayName, int numG, int numP, float sPerPFactor, int numO, float sameOsRatio) {
        @Override
        public String toString() {
            return "%s [g=%d, p=%d, ps=%.2f, o=%d, so=%.2f]".formatted(
                    displayName, numG, numP, sPerPFactor, numO, sameOsRatio);
        }
    }

    public static DatasetGraph generateData(DatasetSpec spec, DatasetGraph destDsg) {
        try (AutoTxn refTxn = Txn.autoTxn(destDsg, ReadWrite.WRITE)) {
            for (int v = 0; v < spec.numG(); ++v) {
                Node g = v == 0 ? Quad.defaultGraphIRI : NodeFactory.createURI("http://www.example.org/g" + v);
                for (int y = 0; y < spec.numP(); ++y) {
                    Node p = NodeFactory.createURI("http://www.example.org/p" + y);
                    int maxX = (int)((y + 1) * spec.sPerPFactor());
                    for (int x = 0; x < maxX; ++x) { // 100 -> sFactor
                        Node s = NodeFactory.createURI("http://www.example.org/s" + x);
                        for (int z = 0; z < spec.numO(); ++z) {
                            Node o1 = NodeFactory.createURI("http://www.example.org/s" + z);
                            destDsg.add(g, s, p, o1);
                        }

                        int maxZ = (int)(spec.numO() * spec.sameOsRatio());
                        for (int z = 0; z < maxZ; ++z) {
                            Node o2 = NodeFactory.createURI("http://www.example.org/o" + z);
                            destDsg.add(g, s, p, o2);
                        }
                    }
                }
            }
            refTxn.commit();
        }
        return destDsg;
    }

    /** Use a sample of the reference data for the parameters dataset. TODO consistent naming of that parameter-source dataset. */
    private DatasetGraph createDataDsg(DatasetGraph referenceDsg) {
        // dataDsg contains the constants used for generating queries against referenceDsg.
        DatasetGraph dataDsg = DatasetGraphFactory.createTxnMem();
        try (AutoTxn writeTxn = Txn.autoTxn(dataDsg, ReadWrite.WRITE);
             AutoTxn readTxn = Txn.autoTxn(referenceDsg, ReadWrite.READ)) {
            try (QueryExec qe = QueryExec.dataset(referenceDsg)
                 .query("CONSTRUCT { ?s ?p ?o } { ?s ?p ?o } ORDER BY ?s ?p ?o LIMIT 5").build()) {
                qe.constructDataset(dataDsg);
            }

            try (QueryExec qe = QueryExec.dataset(referenceDsg)
                    .query("CONSTRUCT { ?s ?p ?o } { ?s ?p ?o } ORDER BY DESC(?s) DESC(?p) DESC(?o) LIMIT 5").build()) {
                   qe.constructDataset(dataDsg);
               }

            try (QueryExec qe = QueryExec.dataset(referenceDsg)
                    .query("CONSTRUCT { GRAPH ?g { ?s ?p ?o } } { GRAPH ?g { ?s ?p ?o } } ORDER BY ?g ?s ?p ?o LIMIT 5").build()) {
                qe.constructDataset(dataDsg);
            }

            try (QueryExec qe = QueryExec.dataset(referenceDsg)
                    .query("CONSTRUCT { GRAPH ?g { ?s ?p ?o } } { GRAPH ?g { ?s ?p ?o } } ORDER BY DESC(?g) DESC(?s) DESC(?p) DESC(?o) LIMIT 5").build()) {
                qe.constructDataset(dataDsg);
            }

            expectNotEmpty(dataDsg);
            writeTxn.commit();
        }
        return dataDsg;
    }

    /* ----- Graph Utils ----- */

    private static void expectNotEmpty(DatasetGraph dsg) {
        if (Txn.calculateRead(dsg, dsg::isEmpty)) {
            throw new IllegalStateException("Unexpected empty dataset");
        }
    }
}
