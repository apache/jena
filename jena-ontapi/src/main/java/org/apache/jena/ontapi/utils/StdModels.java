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

package org.apache.jena.ontapi.utils;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.impl.objects.OntListImpl;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class-helper to work with {@link Model Jena Model}s and its related objects and components:
 * {@link RDFNode Jena RDF Node}, {@link Literal Jena Literal}, {@link Resource Jena Resource} and
 * {@link Statement Jena Statement}.
 */
@SuppressWarnings("WeakerAccess")
public class StdModels {
    public static final Comparator<RDFNode> RDF_NODE_COMPARATOR = (r1, r2) -> NodeCmp.compareRDFTerms(r1.asNode(), r2.asNode());
    public static final Comparator<Statement> STATEMENT_COMPARATOR = Comparator
            .comparing(Statement::getSubject, RDF_NODE_COMPARATOR)
            .thenComparing(Statement::getPredicate, RDF_NODE_COMPARATOR)
            .thenComparing(Statement::getObject, RDF_NODE_COMPARATOR);
    public static final RDFNode BLANK = new ResourceImpl();
    public static final Comparator<Statement> STATEMENT_COMPARATOR_IGNORE_BLANK = Comparator
            .comparing((Function<Statement, RDFNode>) s -> s.getSubject().isAnon() ? BLANK : s.getSubject(),
                    RDF_NODE_COMPARATOR)
            .thenComparing(s -> s.getPredicate().isAnon() ? BLANK : s.getPredicate(), RDF_NODE_COMPARATOR)
            .thenComparing(s -> s.getObject().isAnon() ? BLANK : s.getObject(), RDF_NODE_COMPARATOR);

    public static final Literal TRUE = ResourceFactory.createTypedLiteral(Boolean.TRUE);
    public static final Literal FALSE = ResourceFactory.createTypedLiteral(Boolean.FALSE);

    /**
     * Creates a typed []-list with the given type containing the resources from the given collection.
     *
     * @param model   {@link Model model} in which the []-list is created
     * @param type    {@link Resource} the type for new []-list
     * @param members Collection of {@link RDFNode}s
     * @return anonymous resource - the header of the typed []-list
     * @see OntList
     */
    public static RDFList createTypedList(Model model, Resource type, Collection<? extends RDFNode> members) {
        return createTypedList(model, type, members.iterator());
    }

    /**
     * Creates a typed list with the given type containing the resources from the given iterator.
     * A typed list is an anonymous resource
     * created using the same rules as the standard {@link RDFList []-list}
     * (that is, using {@link RDF#first rdf:first}, {@link RDF#rest rdf:rest} and {@link RDF#nil rdf:nil} predicates),
     * but each item of this []-list has the specified type on predicate {@link RDF#type rdf:type}.
     *
     * @param model   {@link Model model} in which the []-list is created
     * @param type    {@link Resource} the type for new []-list
     * @param members {@link Iterator} of {@link RDFNode}s
     * @return anonymous resource - the header of the typed []-list
     * @see OntList
     */
    public static RDFList createTypedList(Model model, Resource type, Iterator<? extends RDFNode> members) {
        return OntListImpl.createTypedList((EnhGraph) model, type, members);
    }

    /**
     * Determines is s specified resource belongs to a list.
     *
     * @param model     Model
     * @param candidate Resource to test
     * @return true if specified resource is a member of some rdf:List
     */
    public static boolean isInList(Model model, Resource candidate) {
        return model.contains(null, RDF.first, candidate);
    }

    /**
     * Answers {@code true} iff the given statement belongs to some []-list.
     *
     * @param s {@link Statement}, not {@code null}
     * @return boolean
     */
    public static boolean isInList(Statement s) {
        return RDF.first.equals(s.getPredicate()) || RDF.rest.equals(s.getPredicate()) || RDF.nil.equals(s.getObject());
    }

    /**
     * Answers a set of all the RDF statements whose subject is one of the cells of the given list.
     *
     * @param list []-list, not {@code null}
     * @return a {@code Set} of {@link Statement}s
     */
    public static Set<Statement> getListStatements(RDFList list) {
        return ((RDFListImpl) list).collectStatements();
    }

    /**
     * Replaces namespaces' map with new one.
     *
     * @param mapping  {@link PrefixMapping Prefix Mapping} to modify
     * @param prefixes java Map of new prefixes to set
     * @return a {@code Map} of previously associated prefixes
     */
    public static Map<String, String> setNsPrefixes(PrefixMapping mapping, Map<String, String> prefixes) {
        Map<String, String> init = mapping.getNsPrefixMap();
        init.keySet().forEach(mapping::removeNsPrefix);
        prefixes.forEach((p, u) -> mapping.setNsPrefix(p.replaceAll(":$", ""), u));
        return init;
    }

    /**
     * Returns a string representation of the given Jena statement taking into account PrefixMapping.
     *
     * @param st {@link Statement}, not {@code null}
     * @param pm {@link PrefixMapping}, not {@code null}
     * @return {@code String}
     */
    public static String toString(Statement st, PrefixMapping pm) {
        return String.format("[%s, %s, %s]",
                st.getSubject().asNode().toString(pm),
                st.getPredicate().asNode().toString(pm),
                st.getObject().asNode().toString(pm));
    }

    /**
     * Returns a string representation of the given Jena statement.
     *
     * @param inModel {@link Statement}, not {@code null}
     * @return {@code String}
     */
    public static String toString(Statement inModel) {
        return toString(inModel, inModel.getModel());
    }

    /**
     * Answers {@code true} if the given {@code node} contains the specified {@code uri}.
     *
     * @param node {@link RDFNode}, not {@code null}
     * @param uri  {@code String}, not {@code null}
     * @return boolean
     */
    public static boolean containsURI(RDFNode node, String uri) {
        if (node.isURIResource()) {
            return uri.equals(node.asResource().getURI());
        }
        return node.isLiteral() && uri.equals(node.asLiteral().getDatatypeURI());
    }

    /**
     * Answers {@code true} if the given {@code uri} is a part of the given {@code statement}.
     *
     * @param statement {@link Statement}, not {@code null}
     * @param uri       {@code String}, not {@code null}
     * @return boolean
     */
    public static boolean containsURI(Statement statement, String uri) {
        if (uri.equals(statement.getSubject().getURI())) return true;
        if (uri.equals(statement.getPredicate().getURI())) return true;
        return containsURI(statement.getObject(), uri);
    }

    /**
     * Creates an iterator which returns RDF Statements based on the given extended iterator of triples.
     *
     * @param triples {@link ExtendedIterator} of {@link Triple}s
     * @param map     a Function to map {@link Triple} -&gt; {@link Statement}
     * @return {@link StmtIterator}
     * @see org.apache.jena.rdf.model.impl.IteratorFactory#asStmtIterator(Iterator, org.apache.jena.rdf.model.impl.ModelCom)
     */
    public static StmtIterator createStmtIterator(ExtendedIterator<Triple> triples, Function<Triple, Statement> map) {
        return new StmtIteratorImpl(triples.mapWith(map));
    }

    /**
     * Creates an unmodifiable Set of {@link Node}s from the collection of {@link RDFNode RDF Node}s.
     * Placed here as it is widely used.
     *
     * @param nodes Collection of {@link RDFNode}s
     * @return Set of {@link Node}
     */
    public static Set<Node> asUnmodifiableNodeSet(Collection<? extends RDFNode> nodes) {
        return nodes.stream().map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Answers {@code true} iff the given {@code SPO} corresponds {@link Triple#ANY}.
     *
     * @param s {@link Resource}, the subject
     * @param p {@link Property}, the predicate
     * @param o {@link RDFNode}, the object
     * @return boolean
     */
    public static boolean isANY(Resource s, Property p, RDFNode o) {
        if (s != null) return false;
        if (p != null) return false;
        return o == null;
    }

    /**
     * Answers the shortest path from the {@code start} resource to the {@code end} RDF node,
     * such that every step on the path is accepted by the given filter.
     * A path is a {@link List} of RDF {@link Statement}s.
     * The subject of the first statement in the list is {@code start},
     * and the object of the last statement in the list is {@code end}.
     * <p>
     * The {@code onPath} argument is a {@link Predicate}, which accepts a statement and returns
     * {@code true} if the statement should be considered to be on the path.
     * To search for an unconstrained path, pass {@code ()->true} or {@code null} as an argument.
     * If there is more than one path of minimal length from {@code start} to {@code end},
     * this method returns an arbitrary one.
     * The algorithm is blind breadth-first search, with loop detection.
     *
     * @param m      the model in which we are seeking a path, not {@code null}
     * @param start  the starting resource, not {@code null}
     * @param end    the end, or goal, node, not {@code null}
     * @param onPath a filter which determines whether a given statement can be considered part of the path
     * @return a path, consisting of a list of statements whose first subject is {@code start},
     * and whose last object is {@code end}, empty if no such path exists
     */
    public static List<Statement> findShortestPath(Model m, Resource start, RDFNode end, Predicate<Statement> onPath) {
        Objects.requireNonNull(m);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (onPath == null) {
            onPath = s -> true;
        }
        Deque<List<Statement>> bfs = new ArrayDeque<>();
        Set<RDFNode> seen = new HashSet<>();

        // initialize the paths
        for (Iterator<Statement> i = m.listStatements(start, null, (RDFNode) null).filterKeep(onPath); i.hasNext(); ) {
            List<Statement> statements = new ArrayList<>();
            statements.add(i.next());
            bfs.add(statements);
        }

        // search
        List<Statement> solution = new ArrayList<>();
        while (solution.isEmpty() && !bfs.isEmpty()) {
            List<Statement> candidate = bfs.removeFirst();

            RDFNode terminalNode = candidate.isEmpty() ? null : candidate.get(candidate.size() - 1).getObject();
            if (terminalNode == null) {
                continue;
            }
            if (end.equals(terminalNode)) {
                solution = candidate;
                continue;
            }
            if (!terminalNode.isResource()) {
                continue;
            }
            Resource terminalResource = terminalNode.asResource();
            seen.add(terminalResource);

            // breadth-first expansion
            for (Iterator<Statement> i = terminalResource.listProperties().filterKeep(onPath); i.hasNext(); ) {
                Statement link = i.next();

                // no looping allowed, so we skip this link if it takes us to a node we've seen
                if (!seen.contains(link.getObject())) {
                    List<Statement> statements = new ArrayList<>(candidate);
                    statements.add(link);
                    bfs.add(statements);
                }
            }
        }
        return solution;
    }
}
