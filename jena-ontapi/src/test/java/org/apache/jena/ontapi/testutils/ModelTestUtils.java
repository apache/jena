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

package org.apache.jena.ontapi.testutils;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelTestUtils {
    public static final String RECURSIVE_GRAPH_IDENTIFIER = "Recursion";
    public static final String ANONYMOUS_ONTOLOGY_IDENTIFIER = "AnonymousOntology";

    /**
     * Recursively deletes all resource children.
     *
     * @param inModel Resource from a model
     */
    public static void deleteAll(Resource inModel) {
        deleteAll(inModel, new HashSet<>());
    }

    private static void deleteAll(Resource r, Set<Node> viewed) {
        if (!viewed.add(r.asNode())) {
            return;
        }
        r.listProperties().toSet().forEach(s -> {
            RDFNode o = s.getObject();
            if (o.isAnon()) {
                deleteAll(o.asResource(), viewed);
            }
            r.getModel().remove(s);
        });
    }

    /**
     * Recursively gets all statements related to the specified subject.
     * Note: {@code rdf:List} may content a large number of members (1000+),
     * which may imply heavy calculation.
     *
     * @param inModel Resource with associated model inside.
     * @return a {@code Set} of {@link Statement}s
     */
    public static Set<Statement> getAssociatedStatements(Resource inModel) {
        Set<Statement> res = new HashSet<>();
        calcAssociatedStatements(inModel, res);
        return res;
    }

    private static void calcAssociatedStatements(Resource root, Set<Statement> res) {
        if (root.canAs(RDFList.class)) {
            RDFList list = root.as(RDFList.class);
            if (list.isEmpty()) return;
            StdModels.getListStatements(list).forEach(statement -> {
                res.add(statement);
                if (!RDF.first.equals(statement.getPredicate())) return;
                RDFNode obj = statement.getObject();
                if (obj.isAnon())
                    calcAssociatedStatements(obj.asResource(), res);
            });
            return;
        }
        root.listProperties().forEachRemaining(statement -> {
            try {
                if (!statement.getObject().isAnon() ||
                        res.stream().anyMatch(s -> statement.getObject().equals(s.getSubject()))) // to avoid cycles
                    return;
                calcAssociatedStatements(statement.getObject().asResource(), res);
            } finally {
                res.add(statement);
            }
        });
    }

    /**
     * Recursively lists all ascending statements for the given {@link RDFNode RDF Node}.
     * <p>
     * More specifically, this function returns all statements,
     * which have either the specified node in an object position,
     * or its indirect ascendant in a graph tree, found by the same method.
     * Consider, the specified node {@code r} belongs to the following RDF:
     * <pre>{@code
     * <a>  p0 _:b0 .
     * _:b0 p1 _:b1 .
     * _:b1 p2  <x> .
     * _:b1 p3  r .
     * }</pre>
     * In this case the method will return three statements:
     * {@code _:b1 p3 r}, {@code _:b0 p1 _:b1} and {@code <a> p0 _:b0}.
     * The statement {@code _:b1 p2  <x>} is skipped since uri resource {@code <x>} is not an ascendant of {@code r}.
     * <p>
     * This is the opposite of the method {@link #listDescendingStatements(RDFNode)}.
     * <p>
     * Note: there is a danger of {@code StackOverflowError} in case graph contains a recursion.
     *
     * @param object not {@code null} must be attached to a model
     * @return {@link ExtendedIterator} of {@link Statement}s
     */
    public static ExtendedIterator<Statement> listAscendingStatements(RDFNode object) {
        return Iterators.flatMap(object.getModel().listStatements(null, null, object),
                s -> s.getSubject().isAnon() ?
                        Iterators.concat(Iterators.of(s), listAscendingStatements(s.getSubject())) : Iterators.of(s));
    }

    /**
     * Recursively lists all descending statements for the given {@link RDFNode RDF Node}.
     * <p>
     * More specifically, this function returns all statements,
     * which have either the specified node in a subject position,
     * or its indirect descendant in a graph tree (if the node is anonymous resource), found by the same method.
     * Consider, the specified node {@code <a>} belongs to the following RDF:
     * <pre>{@code
     * <a>  p0 _:b0 .
     * _:b0 p1 _:b1 .
     * _:b1 p2  <x> .
     * <x> p3  <b> .
     * }</pre>
     * In this case the method will return three statements:
     * {@code <a>  p0 _:b0}, {@code :b0 p1 _:b1} and {@code _:b1 p2  <x>}.
     * The last statement is skipped, since {@code <x>} is uri resource.
     * <p>
     * This is the opposite of the method {@link #listAscendingStatements(RDFNode)}.
     * <p>
     * Note: there is a danger of {@code StackOverflowError} in case graph contains a recursion.
     *
     * @param subject not {@code null} must be attached to a model
     * @return {@link ExtendedIterator} of {@link Statement}s
     */
    public static ExtendedIterator<Statement> listDescendingStatements(RDFNode subject) {
        if (!subject.isResource()) return NullIterator.instance();
        return Iterators.flatMap(subject.asResource().listProperties(),
                s -> s.getObject().isAnon() ?
                        Iterators.concat(Iterators.of(s), listDescendingStatements(s.getResource())) : Iterators.of(s));
    }

    /**
     * Prints a graph hierarchy tree.
     * For a valid ontology it should match an imports ({@code owl:imports}) tree also.
     * For debugging.
     * <p>
     * An examples of possible output:
     * <pre> {@code
     * <http://imports.test.Main.ttl>
     *      <http://imports.test.C.ttl>
     *          <http://imports.test.A.ttl>
     *          <http://imports.test.B.ttl>
     *      <http://imports.test.D.ttl>
     * }, {@code
     * <http://imports.test.D.ttl>
     *      <http://imports.test.C.ttl>
     *          <http://imports.test.A.ttl>
     *          <http://imports.test.B.ttl>
     *              <http://imports.test.Main.ttl>
     * } </pre>
     *
     * @param graph {@link Graph}
     * @return hierarchy tree as String
     */
    public static String importsTreeAsString(Graph graph) {
        Function<Graph, String> printDefaultGraphName = g -> g.getClass().getSimpleName() + "@" + Integer.toHexString(g.hashCode());
        return makeImportsTree(graph, g -> {
            if (g.isClosed()) {
                return "Closed(" + printDefaultGraphName.apply(g) + ")";
            }
            String res = getOntologyGraphPrintName(g);
            if (ANONYMOUS_ONTOLOGY_IDENTIFIER.equals(res)) {
                res += "(" + printDefaultGraphName.apply(g) + ")";
            }
            return res;
        }, "\t", "\t", new HashSet<>()).toString();
    }

    private static StringBuilder makeImportsTree(Graph graph,
                                                 Function<Graph, String> getName,
                                                 String indent,
                                                 String step,
                                                 Set<Graph> seen) {
        StringBuilder res = new StringBuilder();
        Graph base = Graphs.getPrimary(graph);
        String name = getName.apply(base);
        try {
            if (!seen.add(graph)) {
                return res.append(RECURSIVE_GRAPH_IDENTIFIER).append(": ").append(name);
            }
            res.append(name).append("\n");
            Graphs.directSubGraphs(graph)
                    .sorted(Comparator.comparingLong(o -> Graphs.directSubGraphs(o).count()))
                    .forEach(sub -> res.append(indent)
                            .append(makeImportsTree(sub, getName, indent + step, step, seen)));
            return res;
        } finally {
            seen.remove(graph);
        }
    }

    public static Optional<Graph> findSubGraphByIri(UnionGraph graph, String name) {
        return graph.subGraphs().filter(it -> getOntologyGraphIri(it).equals(name)).findFirst();
    }

    public static List<String> getSubGraphsIris(UnionGraph graph) {
        return graph.subGraphs().map(ModelTestUtils::getOntologyGraphIri).sorted().collect(Collectors.toList());
    }

    /**
     * Gets the "name" of the base graph: uri, blank-node-id as string or null string if there is no ontology at all.
     *
     * @param graph {@link Graph}
     * @return String (uri or blank-node label) or {@code null}
     */
    public static String getOntologyGraphPrintName(Graph graph) {
        if (graph.isClosed()) {
            return "(closed)";
        }
        Optional<Node> id = Graphs.ontologyNode(Graphs.getPrimary(graph));
        if (id.isEmpty()) {
            return ANONYMOUS_ONTOLOGY_IDENTIFIER;
        }
        ExtendedIterator<String> versions = graph.find(id.get(), OWL2.versionIRI.asNode(), Node.ANY)
                .mapWith(Triple::getObject).mapWith(Node::toString);
        try {
            Set<String> res = versions.toSet();
            if (res.isEmpty()) {
                return String.format("<%s>", id.get());
            }
            return String.format("<%s%s>", id.get(), res);
        } finally {
            versions.close();
        }
    }

    public static String getOntologyGraphIri(Graph graph) {
        return Graphs.findOntologyNameNode(Graphs.getPrimary(graph)).map(Node::toString).orElse(null);
    }

    /**
     * Recursively lists all models that are associated with the given model in the form of a flat stream.
     * In normal situation, each of the models must have {@code owl:imports} statement in the overlying graph.
     * In this case the returned stream must correspond the result of the {@link Graphs#dataGraphs(Graph)} method.
     *
     * @param m {@link OntModel}, not {@code null}
     * @return {@code Stream} of models, cannot be empty: must contain at least the input (root) model
     * @throws StackOverflowError in case the given model has a recursion in the hierarchy
     * @see Graphs#dataGraphs(Graph)
     * @see OntID#getImportsIRI()
     */
    public static Stream<OntModel> importsClosure(OntModel m) {
        return Stream.concat(Stream.of(m), m.imports().flatMap(ModelTestUtils::importsClosure));
    }

    /**
     * Synchronizes the import declarations with the graph hierarchy.
     * Underling graph tree may content named graphs which are not included to the {@code owl:imports} declaration.
     * This method tries to fix such a situation by modifying base graph.
     *
     * @param m {@link OntModel}, not {@code null}
     */
    public static void syncImports(OntModel m) {
        Deque<Graph> queue = new ArrayDeque<>();
        queue.add(m.getGraph());
        m.getID();
        Set<Node> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            Graph next = queue.removeFirst();
            Graph base = Graphs.getPrimary(next);
            Node id = Graphs.findOntologyNameNode(base).orElse(null);
            if (id == null || !seen.add(id)) {
                continue;
            }
            Node ont = Graphs.ontologyNode(base).orElseThrow();
            base.remove(Node.ANY, OWL2.imports.asNode(), Node.ANY);
            if (!(next instanceof UnionGraph)) {
                continue;
            }
            ((UnionGraph) next).subGraphs().forEach(it -> {
                Node uri = Graphs.findOntologyNameNode(Graphs.getPrimary(it)).filter(Node::isURI).orElse(null);
                if (uri != null) {
                    next.add(ont, OWL2.imports.asNode(), uri);
                    queue.add(it);
                }
            });
        }
    }

    /**
     * Lists all literal string values (lexical forms) with the given language tag
     * for the specified subject and predicate.
     *
     * @param subject   {@link Resource}, not {@code null}
     * @param predicate {@link Property}, can be {@code null}
     * @param lang      String lang, maybe {@code null} or empty
     * @return {@code Stream} of {@code String}s
     */
    public static Stream<String> langValues(Resource subject, Property predicate, String lang) {
        return Iterators.asStream(subject.listProperties(predicate)
                .mapWith(s -> {
                    if (!s.getObject().isLiteral())
                        return null;
                    if (!filterByLangTag(s.getLiteral(), lang))
                        return null;
                    return s.getString();
                })
                .filterDrop(Objects::isNull));
    }

    /**
     * Answers {@code true} if the literal has the given language tag.
     * The comparison is case-insensitive and ignores trailing spaces,
     * so two tags {@code  en } and {@code En} are considered as equaled.
     *
     * @param literal {@link Literal}, not {@code null}
     * @param tag     String, possible {@code null}
     * @return {@code true} if the given literal has the given tag
     */
    public static boolean filterByLangTag(Literal literal, String tag) {
        String other = literal.getLanguage();
        if (StringUtils.isEmpty(tag))
            return StringUtils.isEmpty(other);
        return tag.trim().equalsIgnoreCase(other);
    }

    /**
     * Lists all direct subjects for the given object.
     *
     * @param object {@link RDFNode}, not {@code null}
     * @return <b>distinct</b> {@code Stream} of {@link Resource}s
     * @see Model#listResourcesWithProperty(Property, RDFNode)
     * @see org.apache.jena.graph.GraphUtil#listSubjects(Graph, Node, Node)
     */
    public static Stream<Resource> subjects(RDFNode object) {
        Model m = Objects.requireNonNull(object.getModel(), "No model for a resource " + object);
        return Iterators.fromSet(() -> m.getGraph().find(Node.ANY, Node.ANY, object.asNode())
                .mapWith(t -> m.wrapAsResource(t.getSubject())).toSet());
    }
}
