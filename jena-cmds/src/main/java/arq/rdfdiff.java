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

package arq;

import static org.apache.jena.atlas.logging.LogCtl.setLogging;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Closure;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sys.JenaSystem;

/**
 * A program which read two RDF models and provides a basic triple level diff
 *
 * <p>
 * This program will read two RDF models, in a variety of languages, and compare
 * them providing a basic triple level diff output. Since blank nodes are a
 * complicating factor diffs for blank node containing portions of the graph are
 * reported in terms of sub-graphs rather than individual triples.
 * </p>
 * <p>
 * Input can be read either from a URL or from a file. The program writes its
 * results to the standard output stream and sets its exit code to 0 if the
 * models are equal, to 1 if they are not and to -1 if it encounters an error.
 * </p>
 * <p>
 * When <code>--quads</code> is given the program reads two datasets (quads),
 * compares them for isomorphism and outputs a quad level diff.
 * Blank node containing portions of a dataset are reported in terms of
 * sub-datasets rather than individual quads.
 * </p>
 *
 * <pre>
 * java jena.rdfdiff [--quads] source1 source2 [lang1 [lang2 [base1 [base2]]]]
 * </pre>
 */
public class rdfdiff extends java.lang.Object {

    static {
        setLogging();
        JenaSystem.init();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String... args) {
        int exitCode = exec(args);
        System.exit(exitCode);
    }

    /**
     * Execute rdfdiff and return the exit code without calling System.exit.
     *
     * @param args the command line arguments
     * @return 0 if equal, 1 if unequal, -1 on error
     */
    public static int exec(String... args) {
        boolean quadsMode = false;
        int argOffset = 0;
        if (args.length > 0 && "--quads".equals(args[0])) {
            quadsMode = true;
            argOffset = 1;
        }
        args = Arrays.copyOfRange(args, argOffset, args.length);

        if (args.length < 2 || args.length > 6) {
            usage();
            return -1;
        }

        String in1 = args[0];
        String in2 = args[1];
        String lang1 = "RDF/XML";
        if (args.length >= 3) {
            lang1 = args[2];
        } else if (quadsMode) {
            lang1 = null;
        }
        String lang2 = "N-TRIPLE";
        if (args.length >= 4) {
            lang2 = args[3];
        } else if (quadsMode) {
            lang2 = null;
        }
        String base1 = null;
        if (args.length >= 5) {
            base1 = args[4];
        }
        String base2 = base1;
        if (args.length >= 6) {
            base2 = args[5];
        }

        //System.out.println(in1 + " " + in2 + " " + lang1 + " " + lang2 + " " + base1 + " " + base2);
        try {
            if (quadsMode) {
                return diffDatasets(in1, in2, lang1, lang2, base1, base2);
            } else {
                return diffModels(in1, in2, lang1, lang2, base1, base2);
            }
        } catch (Exception e) {
            System.err.println("Unhandled exception:");
            System.err.println("    " + e.toString());
            return -1;
        }
    }

    /**
     * Compare two RDF datasets (quads mode).
     */
    private static int diffDatasets(String in1, String in2, String lang1, String lang2, String base1, String base2) throws Exception {
        Lang l1 = RDFLanguages.nameToLang(lang1);
        Lang l2 = RDFLanguages.nameToLang(lang2);

        Dataset ds1 = DatasetFactory.createTxnMem();
        Dataset ds2 = DatasetFactory.createTxnMem();

        read(ds1, in1, l1, base1);
        read(ds2, in2, l2, base2);

        DatasetGraph dsg1 = ds1.asDatasetGraph();
        DatasetGraph dsg2 = ds2.asDatasetGraph();

        if (IsoMatcher.isomorphic(dsg1, dsg2)) {
            System.out.println("datasets are equal");
            System.out.println();
            return 0;
        } else {
            System.out.println("datasets are unequal");
            System.out.println();

            long size1 = dsg1.stream().count();
            long size2 = dsg2.stream().count();
            if (size1 != size2) {
                System.out.println(String.format("< %,d quads", size1));
                System.out.println(String.format("> %,d quads", size2));
            }

            // Calculate differences
            Map<Node, List<Quad>> m1SubDatasets = new HashMap<>();
            Iterator<Quad> iter = dsg1.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
            while (iter.hasNext()) {
                Quad quad = iter.next();
                if (!hasBlankNodes(quad)) {
                    if (!dsg2.contains(quad)) {
                        System.out.print("< ");
                        System.out.println(quad.toString());
                    }
                } else {
                    // Handle blank nodes via sub-datasets
                    addToSubDataset(quad, dsg1, m1SubDatasets);
                }
            }

            Map<Node, List<Quad>> m2SubDatasets = new HashMap<>();
            iter = dsg2.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
            while (iter.hasNext()) {
                Quad quad = iter.next();
                if (!hasBlankNodes(quad)) {
                    if (!dsg1.contains(quad)) {
                        System.out.print("> ");
                        System.out.println(quad.toString());
                    }
                } else {
                    // Handle blank nodes via sub-datasets
                    addToSubDataset(quad, dsg2, m2SubDatasets);
                }
            }

            // Compute sub-dataset differences

            // Reduce to sets
            Set<List<Quad>> m1SubDS = Collections.newSetFromMap(new IdentityHashMap<>());
            m1SubDS.addAll(m1SubDatasets.values());
            Set<List<Quad>> m2SubDS = Collections.newSetFromMap(new IdentityHashMap<>());
            m2SubDS.addAll(m2SubDatasets.values());

            if (m1SubDS.size() != m2SubDS.size()) {
                System.out.println("< " + m1SubDS.size() + " sub-datasets");
                System.out.println("> " + m2SubDS.size() + " sub-datasets");
            }
            if (m1SubDS.size() > 0) {
                diffSubDatasets(m1SubDS, m2SubDS, "< ");
            }
            if (m2SubDS.size() > 0) {
                diffSubDatasets(m2SubDS, m1SubDS, "> ");
            }

            return 1;
        }
    }

    /**
     * Compare two RDF models (triples mode - original behavior).
     */
    private static int diffModels(String in1, String in2, String lang1, String lang2, String base1, String base2) throws Exception {
        // Term-equality.
        Model m1 = ModelFactory.createModelForGraph(GraphMemFactory.createDefaultGraphSameTerm());
        Model m2 = ModelFactory.createModelForGraph(GraphMemFactory.createDefaultGraphSameTerm());

        read(m1, in1, lang1, base1);
        read(m2, in2, lang2, base2);

        if (m1.isIsomorphicWith(m2)) {
            System.out.println("models are equal");
            System.out.println();
            return 0;
        } else {
            System.out.println("models are unequal");
            System.out.println();

            if (m1.size() != m2.size()) {
                System.out.println(String.format("< %,d triples", m1.size()));
                System.out.println(String.format("> %,d triples", m2.size()));
            }

            // Calculate differences
            Map<AnonId, Model> m1SubGraphs = new HashMap<>();
            StmtIterator iter = m1.listStatements();
            while (iter.hasNext()) {
                Statement stmt = iter.next();
                if (!hasBlankNodes(stmt)) {
                    if (!m2.contains(stmt)) {
                        System.out.print("< ");
                        System.out.println(stmt.toString());
                    }
                } else {
                    // Handle blank nodes via sub-graphs
                    addToSubGraph(stmt, m1SubGraphs);
                }
            }

            Map<AnonId, Model> m2SubGraphs = new HashMap<>();
            iter = m2.listStatements();
            while (iter.hasNext()) {
                Statement stmt = iter.next();
                if (!hasBlankNodes(stmt)) {
                    if (!m1.contains(stmt)) {
                        System.out.print("> ");
                        System.out.println(stmt.toString());
                    }
                } else {
                    // Handle blank nodes via sub-graphs
                    addToSubGraph(stmt, m2SubGraphs);
                }
            }

            // Compute sub-graph differences

            // Reduce to sets
            Set<Model> m1SubGraphSet = Collections.newSetFromMap(new IdentityHashMap<>());
            m1SubGraphSet.addAll(m1SubGraphs.values());
            Set<Model> m2SubGraphSet = Collections.newSetFromMap(new IdentityHashMap<>());
            m2SubGraphSet.addAll(m2SubGraphs.values());

            if (m1SubGraphSet.size() != m2SubGraphSet.size()) {
                System.out.println("< " + m1SubGraphSet.size() + " sub-graphs");
                System.out.println("> " + m2SubGraphSet.size() + " sub-graphs");
            }
            if (m1SubGraphSet.size() > 0) {
                diffSubGraphs(m1SubGraphSet, m2SubGraphSet, "< ");
            }
            if (m2SubGraphSet.size() > 0) {
                diffSubGraphs(m2SubGraphSet, m1SubGraphSet, "> ");
            }

            return 1;
        }
    }

    // ---- Dataset (quads) helpers ----

    private static void diffSubDatasets(Set<List<Quad>> m1SubDatasets, Set<List<Quad>> m2SubDatasets, String prefix) {
        for (List<Quad> subDataset : m1SubDatasets) {
            // Find candidate matches by size
            List<List<Quad>> candidates = new ArrayList<>();
            for (List<Quad> subDatasetCandidate : m2SubDatasets) {
                if (subDataset.size() == subDatasetCandidate.size()) {
                    candidates.add(subDatasetCandidate);
                }
            }

            if (candidates.isEmpty()) {
                // No match
                printNonMatchingSubDataset(prefix, subDataset);
            } else if (candidates.size() == 1) {
                // Precisely 1 candidate
                if (!isIsomorphic(subDataset, candidates.get(0))) {
                    printNonMatchingSubDataset(prefix, subDataset);
                } else {
                    m2SubDatasets.remove(candidates.get(0));
                }
            } else {
                // Multiple candidates
                boolean matched = false;
                for (List<Quad> subDatasetCandidate : candidates) {
                    if (isIsomorphic(subDataset, subDatasetCandidate)) {
                        // Found a match
                        matched = true;
                        m2SubDatasets.remove(subDatasetCandidate);
                        break;
                    }
                }

                if (!matched) {
                    // Didn't find a match
                    printNonMatchingSubDataset(prefix, subDataset);
                }
            }
        }
    }

    private static boolean isIsomorphic(List<Quad> quads1, List<Quad> quads2) {
        DatasetGraph dsg1 = DatasetGraphFactory.create();
        DatasetGraph dsg2 = DatasetGraphFactory.create();
        for (Quad q : quads1) {
            dsg1.add(q);
        }
        for (Quad q : quads2) {
            dsg2.add(q);
        }
        return IsoMatcher.isomorphic(dsg1, dsg2);
    }

    private static void printNonMatchingSubDataset(String prefix, List<Quad> subDataset) {
        for (Quad quad : subDataset) {
            System.out.print(prefix);
            System.out.println(quad.toString());
        }
    }

    private static void addToSubDataset(Quad quad, DatasetGraph dsg, Map<Node, List<Quad>> subDatasets) {
        Set<Node> bnodes = collectBlankNodes(quad);

        // Check if we've already seen any of these blank nodes
        for (Node id : bnodes) {
            if (subDatasets.containsKey(id)) {
                return;
            }
        }

        List<Quad> subDataset = quadClosure(quad, dsg);

        // Find any further blank nodes that occur in the sub-dataset
        for (Quad q : subDataset) {
            collectBlankNodes(q, bnodes);
        }

        // Associate the sub-dataset with all mentioned blank node IDs
        for (Node id : bnodes) {
            subDatasets.put(id, subDataset);
        }
    }

    private static List<Quad> quadClosure(Quad startQuad, DatasetGraph dsg) {
        Set<Node> bnodes = collectBlankNodes(startQuad);
        Set<Node> visitedBnodes = new HashSet<>();
        List<Quad> closureQuads = new ArrayList<>();
        Set<Quad> closureSet = new HashSet<>();

        closureQuads.add(startQuad);
        closureSet.add(startQuad);

        Queue<Node> bnodeQueue = new LinkedList<>(bnodes);

        while (!bnodeQueue.isEmpty()) {
            Node bnode = bnodeQueue.poll();
            if (!visitedBnodes.add(bnode))
                continue;

            addQuadsForNode(dsg.find(bnode, Node.ANY, Node.ANY, Node.ANY), closureQuads, closureSet, bnodeQueue, visitedBnodes);
            addQuadsForNode(dsg.find(Node.ANY, bnode, Node.ANY, Node.ANY), closureQuads, closureSet, bnodeQueue, visitedBnodes);
            addQuadsForNode(dsg.find(Node.ANY, Node.ANY, Node.ANY, bnode), closureQuads, closureSet, bnodeQueue, visitedBnodes);
        }
        return closureQuads;
    }

    private static void addQuadsForNode(Iterator<Quad> iter, List<Quad> closureQuads, Set<Quad> closureSet,
                                       Queue<Node> bnodeQueue, Set<Node> visitedBnodes) {
        while (iter.hasNext()) {
            Quad candidate = iter.next();
            if (closureSet.add(candidate)) {
                closureQuads.add(candidate);
                for (Node n : collectBlankNodes(candidate)) {
                    if (!visitedBnodes.contains(n)) {
                        bnodeQueue.add(n);
                    }
                }
            }
        }
    }

    private static boolean hasBlankNodes(Quad quad) {
        return isBlank(quad.getGraph())
            || isBlank(quad.getSubject())
            || isBlank(quad.getPredicate())
            || isBlank(quad.getObject());
    }

    private static boolean isBlank(Node node) {
        return node != null && node.isBlank();
    }

    private static Set<Node> collectBlankNodes(Quad quad) {
        Set<Node> ids = new HashSet<>();
        addBNodeToList(quad.getGraph(), ids);
        addBNodeToList(quad.getSubject(), ids);
        addBNodeToList(quad.getPredicate(), ids);
        addBNodeToList(quad.getObject(), ids);
        return ids;
    }

    private static void addBNodeToList(Node node, Set<Node> ids) {
        if (node != null && node.isBlank()) {
            ids.add(node);
        }
    }

    private static void collectBlankNodes(Quad quad, Set<Node> ids) {
        addBNodeToList(quad.getGraph(), ids);
        addBNodeToList(quad.getSubject(), ids);
        addBNodeToList(quad.getPredicate(), ids);
        addBNodeToList(quad.getObject(), ids);
    }

    // ---- Model (triples) helpers (preserved from original) ----

    private static void diffSubGraphs(Set<Model> m1SubGraphSet, Set<Model> m2SubGraphSet, String prefix) {
        for (Model subGraph : m1SubGraphSet) {
            // Find candidate matches
            List<Model> candidates = new ArrayList<>();
            for (Model subGraphCandidate : m2SubGraphSet) {
                if (subGraph.size() == subGraphCandidate.size()) {
                    candidates.add(subGraphCandidate);
                }
            }

            if (candidates.isEmpty()) {
                // No match
                printNonMatchingSubGraph(prefix, subGraph);
            } else if (candidates.size() == 1) {
                // Precisely 1 candidate
                if (!subGraph.isIsomorphicWith(candidates.get(0))) {
                    printNonMatchingSubGraph(prefix, subGraph);
                } else {
                    m2SubGraphSet.remove(candidates.get(0));
                }
            } else {
                // Multiple candidates
                boolean matched = false;
                for (Model subGraphCandidate : candidates) {
                    if (subGraph.isIsomorphicWith(subGraphCandidate)) {
                        // Found a match
                        matched = true;
                        m2SubGraphSet.remove(subGraphCandidate);
                        break;
                    }
                }

                if (!matched) {
                    // Didn't find a match
                    printNonMatchingSubGraph(prefix, subGraph);
                }
            }
        }
    }

    private static void printNonMatchingSubGraph(String prefix, Model subGraph) {
        StmtIterator sIter = subGraph.listStatements();
        while (sIter.hasNext()) {
            System.out.print(prefix);
            System.out.println(sIter.next().toString());
        }
    }

    private static void addToSubGraph(Statement stmt, Map<AnonId, Model> subGraphs) {
        Set<AnonId> ids = new HashSet<>();

        addToIdList(stmt, ids);

        for (AnonId id : ids) {
            if (subGraphs.containsKey(id)) {
                return;
            }
        }

        Model subGraph = Closure.closure(stmt);
        subGraph.add(stmt);

        // Find any further IDs that occur in the sub-graph
        StmtIterator sIter = subGraph.listStatements();
        while (sIter.hasNext()) {
            addToIdList(sIter.next(), ids);
        }

        // Associate the sub-graph with all mentioned blank node IDs
        for (AnonId id : ids) {
            subGraphs.put(id, subGraph);
        }
    }

    private static boolean hasBlankNodes(Statement stmt) {
        return stmt.getSubject().isAnon() || stmt.getObject().isAnon();
    }

    private static void addToIdList(Statement stmt, Set<AnonId> ids) {
        if (stmt.getSubject().isAnon()) {
            ids.add(stmt.getSubject().getId());
        }
        if (stmt.getObject().isAnon()) {
            ids.add(stmt.getObject().asResource().getId());
        }
    }

    protected static void usage() {
        System.err.println("usage:");
        System.err.println("    java jena.rdfdiff [--quads] source1 source2 [lang1 [lang2 [base1 [base2]]]]");
        System.err.println();
        System.err.println("    source1 and source2 can be URL's or filenames");
        System.err.println("    --quads       compare datasets (quads) instead of models (triples)");
        System.err.println("    lang1 and lang2 can take values:");
        System.err.println("      RDF/XML");
        System.err.println("      N-TRIPLE");
        System.err.println("      TTL");
        System.err.println("      N-QUADS");
        System.err.println("      TRIG");
        System.err.println("      JSON-LD");
        System.err.println("      (any format supported by Jena including quads formats like N-QUADS, TRIG)");
        System.err.println("    lang1 defaults to RDF/XML, lang2 to N-TRIPLE");
        System.err.println("    base1 and base2 are URIs");
        System.err.println("    base1 defaults to null");
        System.err.println("    base2 defaults to base1");
        System.err.println("    If no base URIs are specified Jena determines the base URI based on the input source");
        System.err.println();
    }

    // ---- Read methods ----

    protected static void read(Dataset dataset, String in, Lang lang, String base) {
        RDFDataMgr.read(dataset, in, base, lang);
    }

    protected static void read(Model model, String in, String lang, String base) throws java.io.FileNotFoundException {
        try {
            URI u = new URI(in);
            if (u.isAbsolute()) {
                u.toURL();
                model.read(in, base, lang);
                return;
            }
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException ignored) {
        }
        model.read(new FileInputStream(in), base, lang);
    }
}
