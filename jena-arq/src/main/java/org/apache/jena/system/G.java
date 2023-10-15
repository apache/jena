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

package org.apache.jena.system;

import static org.apache.jena.graph.Node.ANY;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.util.IteratorCollection;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A library of functions for working with {@link Graph}. Internally all
 * {@link ExtendedIterator ExtendedIterators} used, run to completion or have
 * {@code .close()} called. Any {@link ExtendedIterator ExtendedIterators} returned
 * by functions in this library must be used in the same way
 */
public class G {
    private G() {}

    private static Node rdfType = NodeConst.nodeRDFType;

    /** Return the subject of a triple, or null if the triple is null. */
    public static Node subject(Triple triple) {
        return triple == null ? null : triple.getSubject();
    }

    /** Return the predicate of a triple, or null if the triple is null. */
    public static Node predicate(Triple triple) {
        return triple == null ? null : triple.getPredicate();
    }

    /** Return the object of a triple, or null if the triple is null. */
    public static Node object(Triple triple) {
        return triple == null ? null : triple.getObject();
    }

    // ---- Node filter tests.
    public static boolean isURI(Node n)         { return n != null && n.isURI(); }
    public static boolean isBlank(Node n)       { return n != null && n.isBlank(); }
    public static boolean isLiteral(Node n)     { return n != null && n.isLiteral(); }
    public static boolean isResource(Node n)    { return n != null && (n.isURI()||n.isBlank()); }
    public static boolean isNodeTriple(Node n)  { return n != null && n.isNodeTriple(); }
    public static boolean isNodeGraph(Node n)   { return n != null && n.isNodeGraph(); }
    public static boolean isNullOrAny(Node n)   { return n == null || Node.ANY.equals(n); }

    /**
     * Return true if n is a literal and it has the
     * given datatype, else return false.
     */
    public static boolean hasDatatype(Node n, RDFDatatype datatype) {
        Objects.requireNonNull(n, "hasDatatype: first argument is null");
        Objects.requireNonNull(datatype, "hasDatatype: second argument is null");
        return isLiteral(n) && datatype.equals(n.getLiteralDatatype());
    }

    // -- Literal and datatypes
    public static boolean isString(Node n)      { return Util.isSimpleString(n); }
    public static boolean isBoolean(Node n)     {
        if ( ! n.isLiteral() )
            throw new RDFDataException("Not a literal: "+NodeFmtLib.strTTL(n));
        return hasDatatype(n, XSDDatatype.XSDboolean);
    }

    /** Convert null to Node.ANY */
    public static Node nullAsAny(Node x)        { return nullAsDft(x, Node.ANY); }

    /** Convert null to some default Node */
    public static Node nullAsDft(Node x, Node dft) { return x==null ? dft : x; }

    /** Get a string, assuming the node is an xsd:string literal. */
    public static String asString(Node x) {
        if ( ! isString(x) )
            throw new RDFDataException("Expected a string: found: "+NodeFmtLib.strTTL(x));
        return x.getLiteralLexicalForm();
    }

    /** Does the graph match the s/p/o pattern? */
    public static boolean contains(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return graph.contains(subject, predicate, object);
    }

    /** Does the graph use the node anywhere as a subject, predicate or object? */
    public static boolean containsNode(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        return GraphUtil.containsNode(graph, node);
//        return
//            contains(graph, node, Node.ANY, Node.ANY) ||
//            contains(graph, Node.ANY, Node.ANY, node) ||
//            contains(graph, Node.ANY, node, Node.ANY);
    }

    /** Test whether the node has the type or one of its types is an rdfs:subclassOf the type. */
    public static boolean isOfType(Graph graph, Node node, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(type, "type");
        List<Node> allClasses = listSubClasses(graph, type);
        for ( Node c : allClasses ) {
            if ( hasType(graph, node, c) )
                return true;
        }
        return false;
    }

    /** Does the node x have the given type (non-RDFS - no rdfs:subclassOf considered)? */
    public static boolean hasType(Graph graph, Node node, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(type, "type");
        return contains(graph, node, NodeConst.nodeRDFType, type);
    }

    //---- get/list/iter

    /** Does node {@code s} have property {@code p} in graph {@code g}? */
    public static boolean hasProperty(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return graph.contains(subject, predicate, null);
    }

    /** Contains exactly one. */
    public static boolean containsOne(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        ExtendedIterator<Triple> iter = graph.find(subject, predicate, object);
        try {
            if ( ! iter.hasNext() )
                return false;
            iter.next();
            return !iter.hasNext();
        } finally { iter.close(); }
    }

    /**
     * Get object, given subject and predicate. Returns one (non-deterministically) or null.
     * See also {@link #getOneSP} and {@link #getZeroOrOneSP}.
     */
    public static Node getSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return object(first(find(graph, subject, predicate, Node.ANY)));
    }

    // --- Graph walking.

    /**
     * Get object for subject-predicate. Must be exactly one object; exception
     * {@linkplain RDFDataException} thrown when none or more than one.
     */
    public static Node getOneSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return object(findUniqueTriple(graph, subject, predicate, Node.ANY));
    }

    /**
     * Test whether there is exactly one object for the subject-predicate.
     */
    public static boolean hasOneSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        // Not contains!
        return findZeroOneTriple(graph, subject, predicate, Node.ANY) != null;
    }

    /**
     * Get object for subject-predicate. Return null for none, object for one, and
     * exception {@linkplain RDFDataException} if more than one.
     */
    public static Node getZeroOrOneSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return object(findZeroOneTriple(graph, subject, predicate, Node.ANY));
    }

    /**
     *  Get the subject, given predicate and object. Returns one (non-deterministically) or null.
     *  See also {@link #getOnePO} and {@link #getZeroOrOnePO}.
     */
    public static Node getPO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return object(first(find(graph, Node.ANY, predicate, object)));
    }

    /**
     * Get the subject for predicate-object. Must be exactly one subject; exception
     * {@linkplain RDFDataException} thrown when none or more than one.
     */
    public static Node getOnePO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return subject(findUniqueTriple(graph, Node.ANY, predicate, object));
    }

    /**
     * Test whether there is exactly one subject for the predicate-object.
     */
    public static boolean hasOnePO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        // Not contains!
        return findUniqueTriple(graph, Node.ANY, predicate, object) != null;
    }

    /**
     * Get the subject for predicate-object. Return null for none, subject for one, throw
     * exception {@linkplain RDFDataException} if more than one.
     */
    public static Node getZeroOrOnePO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return subject(findZeroOneTriple(graph, Node.ANY, predicate, object));
    }

    /**
     * Get triple if there is exactly one to match the s/p/o, else throw
     * {@linkplain RDFDataException}.
     */
    public static Triple getOne(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return findUniqueTriple(graph, subject, predicate, object);
    }

    /**
     * Get triple if there is exactly one to match the s/p/o; return null if none;
     * throw {@linkplain RDFDataException} if more than one.
     */
    public static Triple getZeroOrOne(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return findZeroOneTriple(graph, subject, predicate, object);
    }

    /**
     * Get triple if there is exactly one to match the s/p/o; else return null
     * if none or more than one.
     */
    public static Triple getOneOrNull(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return findTripleOrNull(graph, subject, predicate, object);
    }

    /**
     * Get quad if there is exactly one to match the s/p/o, else throw
     * {@linkplain RDFDataException}.
     */
    public static Quad getOne(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(dsg, "DatasetGraph");
        return findUniqueQuad(dsg, graph, subject, predicate, object);
    }

    /**
     * Get triple if there is exactly one to match the s/p/o; return null if none;
     * throw {@linkplain RDFDataException} if more than one.
     */
    public static Quad getZeroOrOne(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(dsg, "DatasetGraph");
        return findZeroOneQuad(dsg, graph, subject, predicate, object);
    }

    // ---- Multiple matches.

    /**
     * Get triple if there is exactly one to match the s/p/o; else return null
     * if none or more than one.
     */
    public static Quad getOneOrNull(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(dsg, "DatasetGraph");
        return findQuadOrNull(dsg, graph, subject, predicate, object);
    }

    /**
     * {@link ExtendedIterator} of objects where the triple matches for subject and
     * predicate (which can be wildcards). The {@link ExtendedIterator} must be fully
     * used or explicitly closed. It is preferable use {@link #listSP} which handles
     * this condition.
     */
    public static ExtendedIterator<Node> iterSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return find(graph, subject, predicate, null).mapWith(Triple::getObject);
    }

    /**
     * List of objects matching the subject-predicate (which can be wildcards).
     */
    public static List<Node> listSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return iterSP(graph, subject, predicate).toList();
    }

    /** Count matches of subject-predicate (which can be wildcards). */
    public static long countSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return Iter.count(iterSP(graph, subject, predicate));
    }

    /**
     * {@link ExtendedIterator} of subjects where the triple matches for predicate
     * and object (which can be wildcards). The {@link ExtendedIterator} must be
     * fully used or explicitly closed. It is preferable use {@link #listSP} which
     * handles this condition.
     */
    public static ExtendedIterator<Node> iterPO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return find(graph, null, predicate, object).mapWith(Triple::getSubject);
    }

    /**
     * List of subjects matching the predicate-object (which can be wildcards).
     */
    public static List<Node> listPO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return iterPO(graph, predicate, object).toList();
    }

    /** Count matches of predicate-object (which can be wildcards). */
    public static long countPO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return Iter.count(iterPO(graph, predicate, object));
    }

    // DISTINCT means these are space using.

    /** List the subjects in a graph (no duplicates) */
    public static Iterator<Node> iterSubjects(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY);
        return Iter.iter(iter).map(Triple::getSubject).distinct();
    }

    /** List the predicates in a graph (no duplicates) */
    public static Iterator<Node> iterPredicates(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY);
        return Iter.iter(iter).map(Triple::getPredicate).distinct();
    }

    /** List the objects in a graph (no duplicates) */
    public static Iterator<Node> iterObjects(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY);
        return Iter.iter(iter).map(Triple::getObject).distinct();
    }

    /**
     * List the subjects of a predicate in a graph (no duplicates)
     * <p>
     * Use {@code iterPO(predicate, null)} for "with duplicates."
     */
    public static Iterator<Node> iterSubjectsOfPredicate(Graph graph, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, predicate, Node.ANY);
        return Iter.iter(iter).map(Triple::getSubject).distinct();
    }

    /**
     * List the objects in a graph (no duplicates)
     * <p>
     * Use {@code iterSP(null, predicate)} for "with duplicates."
     */
    public static Iterator<Node> iterObjectsOfPredicate(Graph graph, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, predicate, Node.ANY);
        return Iter.iter(iter).map(Triple::getObject).distinct();
    }

    // ---- rdf:type, not RDFS

    /**
     * List the subjects with exactly {@code type}.
     * See {@link #listNodesOfTypeRDFS(Graph, Node)}, which does include sub-classes.
     */
    public static List<Node> nodesOfTypeAsList(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        return find(graph, null, rdfType, type).mapWith(Triple::getSubject).toList();
    }

    /**
     * List the types of a node/subject.
     * See {@link #listTypesOfNodeRDFS(Graph, Node)}, which does include super-classes.
     */
    public static List<Node> typesOfNodeAsList(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        return find(graph, node, rdfType, null).mapWith(Triple::getObject).toList();
    }

    /**
     * Set of nodes with exactly {@code type}.
     * See {@link #allNodesOfTypeRDFS(Graph, Node)}, which does include sub-classes.
     */
    public static Set<Node> nodesOfTypeAsSet(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        return find(graph, null, rdfType, type).mapWith(Triple::getSubject).toSet();
    }

    /**
     * Set of exact types of a node.
     * <p>See {@link #allTypesOfNodeRDFS(Graph, Node)},
     * which does include super-classes.
     */
    public static Set<Node> typesOfNodeAsSet(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        return find(graph, node, rdfType, null).mapWith(Triple::getObject).toSet();
    }

    // ---- RDF list.

    /** Return a java list for an RDF list of data. */
    public static List<Node> rdfList(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        GNode gNode = GNode.create(graph, node);
        if ( ! GraphList.isListNode(gNode) )
            return null;
        return GraphList.members(gNode);
    }

    /**
     * Return a java list where the {@code node} is an RDF list of nodes or a single
     * node (returned a singleton list).
     */
    public static List<Node> getOneOrList(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        GNode gNode = GNode.create(graph, node);
        // An element on its own is a list of one
        if ( ! GraphList.isListNode(gNode) )
            return List.of(node);
        return GraphList.members(gNode);
    }

    // Sub-class / super-class

    /**
     * List the subclasses of a type, including itself.
     * This is {@code ?x rdfs:subClassOf* type}.
     * The list does not contain duplicates.
     */
    public static List<Node> listSubClasses(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        List<Node> acc = new ArrayList<>();
        // Subclasses are follow rdfs:subClassOf in reverse - object to subject.
        // Transitive.transitive is "visit once".
        Transitive.transitiveInc(graph, false, type, NodeConst.rdfsSubclassOf, acc);
        return acc;
    }

    /**
     * List the super-classes of a type, including itself.
     * This is {@code type rdfs:subClassOf* ?x}.
     * The list does not contain duplicates.
     */
    public static List<Node> listSuperClasses(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        List<Node> acc = new ArrayList<>();
        // Super classes are "follow rdfs:subclassOf" - subject to object.
        // Transitive.transitive is "visit once".
        Transitive.transitiveInc(graph, true, type, NodeConst.rdfsSubclassOf, acc);
        return acc;
    }

    /**
     * Set of the subclasses of a type, including itself.
     * This is {@code ?x rdfs:subClassOf* type}.
     */
    public static Set<Node> subClasses(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        Set<Node> acc = new HashSet<>();
        // Subclasses are follow rdfs:subclassOf in reverse - object to subject.
        Transitive.transitiveInc(graph, false, type, NodeConst.rdfsSubclassOf, acc);
        return acc;
    }

    /**
     * Set of the subclasses of a type, including itself.
     * This is {@code ?x rdfs:subClassOf* type}.
     */
    public static Set<Node> superClasses(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        Set<Node> acc = new HashSet<>();
        Transitive.transitiveInc(graph, true, type, NodeConst.rdfsSubclassOf, acc);
        return acc;
    }

    // ---- RDFS

    /**
     * List the types of a node, following rdfs:subClassOf for super classes.
     */
    public static List<Node> listTypesOfNodeRDFS(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        List<Node> types = typesOfNodeAsList(graph, node);
        List<Node> types2 = new ArrayList<>();
        types.forEach(t->{
            List<Node> subClasses = listSuperClasses(graph, t);
            types2.addAll(subClasses);
        });
        return types2;
    }

    /**
     * List all the nodes of type, including nodes of sub-classes.
     */
    public static List<Node> listNodesOfTypeRDFS(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        List<Node> types = listSubClasses(graph, type);
        List<Node> nodes = new ArrayList<>();
        accNodesOfTypes(nodes, graph, types);
        return nodes;
    }

    /**
     * List all the types of a node, including super-classes.
     */
    public static Set<Node> allTypesOfNodeRDFS(Graph graph, Node node) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(node, "node");
        Set<Node> types = typesOfNodeAsSet(graph, node);
        Set<Node> types2 = new HashSet<>();
        types.forEach(t->{
            List<Node> subClasses = listSuperClasses(graph, t);
            types2.addAll(subClasses);
        });
        return types2;
    }

    /** List all the node of type, including considering rdfs:subClassOf */
    public static Set<Node> allNodesOfTypeRDFS(Graph graph, Node type) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(type, "type");
        Set<Node> types = subClasses(graph, type);
        Set<Node> nodes = new HashSet<>();
        accNodesOfTypes(nodes, graph, types);
        return nodes;
    }

    /** For each type, find nodes of that type and accumulate */
    private static void accNodesOfTypes(Collection<Node> acc, Graph graph, Collection<Node> types) {
        types.forEach(t->
            find(graph, null, rdfType, t).mapWith(Triple::getSubject).forEach(acc::add)
            );
    }

    /** Return a set of all objects for subject-predicate */
    public static Set<Node> allSP(Graph graph, Node subject, Node predicate) {
        Objects.requireNonNull(graph, "graph");
        return find(graph, subject, predicate, null).mapWith(Triple::getObject).toSet();
    }

    /** Return a set of all subjects for predicate-object */
    public static Set<Node> allPO(Graph graph, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return find(graph, null, predicate, object).mapWith(Triple::getSubject).toSet();
    }

    // --- Graph walking.

    /** Count the number of in-arc to an object */
    public static long objectConnectiveness(Graph graph, Node object) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(object, "object");
        return Iter.count(find(graph, null, null, object));
    }

    /** Test whether an object has exactly one in-arc. */
    public static boolean oneConnected(Graph graph, Node object) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(object, "object");
        ExtendedIterator<Triple> iter = find(graph, null, null, object);
        try {
            if ( ! iter.hasNext() )
                // Zero.
                return false;
            iter.next();
            if ( iter.hasNext() )
                // more than one
                return false;
            return true;
        } finally { iter.close(); }
    }

    /** Count occurrences of the pattern. */
    public static long count(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return Iter.count(graph.find(subject, predicate, object));
    }

    /** {@link Graph#find(Node, Node, Node)} as a function. */
    public static ExtendedIterator<Triple> find(Graph graph, Node subject, Node predicate, Node object) {
        Objects.requireNonNull(graph, "graph");
        return graph.find(subject, predicate, object);
    }

    /** {@link Graph#find()} as a function. */
    public static ExtendedIterator<Triple> findAll(Graph graph) {
        Objects.requireNonNull(graph, "graph");
        return graph.find();
    }

    private static Triple findUniqueTriple(Graph graph, Node subject, Node predicate, Node object) {
        ExtendedIterator<Triple> iter = graph.find(subject, predicate, object);
        try {
            if ( ! iter.hasNext() )
                throw new RDFDataException("No match : "+matchStr(subject, predicate, object));
            Triple x = iter.next();
            if ( iter.hasNext() )
                throw new RDFDataException("More than one match : "+matchStr(subject, predicate, object));
            return x;
        } finally { iter.close(); }
    }

    /** Find one triple matching subject-predicate-object. Return quad or throw {@link RDFDataException}. */
    private static Quad findUniqueQuad(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        // Better stack trace and error messages if done explicitly.
        Iterator<Quad> iter = dsg.find(graph, subject, predicate, object);
        if ( ! iter.hasNext() )
            throw new RDFDataException("No match : "+matchStr(graph, subject, predicate, object));
        Quad x = iter.next();
        if ( iter.hasNext() )
            throw new RDFDataException("More than one match : "+matchStr(graph, subject, predicate, object));
        return x;
    }

    /** Find one triple matching subject-predicate-object else return null. */
    private static Triple findTripleOrNull(Graph graph, Node subject, Node predicate, Node object) {
        ExtendedIterator<Triple> iter = graph.find(subject, predicate, object);
        try {
            if ( ! iter.hasNext() )
                return null;
            Triple x = iter.next();
            if ( iter.hasNext() )
                return null;
            return x;
        } finally { iter.close(); }
    }

    /** Find one quad matching graph-subject-predicate-object else return null. */
    private static Quad findQuadOrNull(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        // Better stack trace and error messages if done explicitly.
        Iterator<Quad> iter = dsg.find(graph, subject, predicate, object);
        if ( ! iter.hasNext() )
            return null;
        Quad x = iter.next();
        if ( iter.hasNext() )
            return null;
        return x;
    }

    /** Find one triple matching subject-predicate-object. Return null for zero, triple for one or throw {@link RDFDataException} for many. */
    private static Triple findZeroOneTriple(Graph graph, Node subject, Node predicate, Node object) {
        ExtendedIterator<Triple> iter = graph.find(subject, predicate, object);
        try {
            if ( ! iter.hasNext() )
                return null;
            Triple x = iter.next();
            if ( iter.hasNext() )
                throw new RDFDataException("More than one match : "+matchStr(subject, predicate, object));
            return x;
        } finally { iter.close(); }
    }

    /** Find one quad matching graph-subject-predicate-object. Return null for zero, quad for one or throw {@link RDFDataException} for many. */
    private static Quad findZeroOneQuad(DatasetGraph dsg, Node graph, Node subject, Node predicate, Node object) {
        Iterator<Quad> iter = dsg.find(graph, subject, predicate, object);
        if ( ! iter.hasNext() )
            return null;
        Quad x = iter.next();
        if ( iter.hasNext() )
            throw new RDFDataException("More than one match : "+matchStr(subject, predicate, object));
        return x;
    }

    private static String matchStr(Node subject, Node predicate, Node object) {
        return "("+NodeFmtLib.strNodesTTL(subject, predicate, object)+")";
    }

    private static String matchStr(Node graph, Node subject, Node predicate, Node object) {
        return "("+NodeFmtLib.strNodesTTL(graph, subject, predicate, object)+")";
    }

    private static Triple first(ExtendedIterator<Triple> iter) {
        try {
            if ( ! iter.hasNext() )
                return null;
            return iter.next();
        } finally { iter.close(); }
    }

    /** Are all the arguments non-null? */
    @SafeVarargs
    public static <X> boolean allNonNull(X ... objects) {
        return countNonNulls(objects) == objects.length;
    }

    /** Is one and only one argument non-null? */
    @SafeVarargs
    public static <X> boolean exactlyOneSet(X ... objects) {
        return countNonNulls(objects) == 1;
    }

    /** Is one or none of the arguments non-null? */
    @SafeVarargs
    public static <X> X atMostOne(X ... objects) {
        int c = 0;
        X x = null;
        for ( X obj : objects ) {
            if ( obj != null ) {
                c++;
                if ( c > 1 )
                    throw new RDFDataException("atMostOne:"+Arrays.asList(objects));
                if ( x == null )
                    x = obj;
            }
        }
        return x;
    }

    /** Count non-nulls */
    @SafeVarargs
    public static <X> int countNonNulls(X ... objects) {
        int x = 0;
        for ( Object obj : objects ) {
            if ( obj != null )
                x++;
        }
        return x;
    }

    // ---- Project

    /** Project quads to triples */
    public static Iter<Triple> quads2triples(Iterator<Quad> iter)
    { return Iter.iter(iter).map(Quad::asTriple); }

    /** Project quad to graph name */
    public static Iterator<Node> quad2graphName(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getGraph); }

    /** Project quad to subject */
    public static Iterator<Node> quad2subject(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getSubject); }

    /** Project quad to predicate */
    public static Iterator<Node> quad2predicate(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getPredicate); }

    /** Project quad to object */
    public static Iterator<Node> quad2object(Iterator<Quad> iter)
    { return Iter.map(iter, Quad::getObject); }

    /** Project triple to subject */
    public static Iterator<Node> triple2subject(Iterator<Triple> iter)
    { return Iter.map(iter, Triple::getSubject); }

    /** Project triple to predicate */
    public static Iterator<Node> triple2predicate(Iterator<Triple> iter)
    { return Iter.map(iter, Triple::getPredicate); }

    /** Project triple to object */
    public static Iterator<Node> triple2object(Iterator<Triple> iter)
    { return Iter.map(iter, Triple::getObject); }

    // Graph operations.

    /**
     * Add src to dst - assumes transaction.
     * src and dst must not overlap.
     * Copies "left to right" -- {@code src into dst}
     * @param src
     * @param dst
     */
    public static void copyGraphSrcToDst(Graph src, Graph dst) {
        apply(src, dst::add);
    }

    /**
     * Clear graph.
     */
    public static void clear(Graph graph) {
        graph.clear();
    }

    /**
     * Apply an action to every triple of a graph. The action must not attempt to
     * modify the graph but it can read it.
     */
    public static void apply(Graph src, Consumer<Triple> action) {
        ExtendedIterator<Triple> iter = src.find();
        apply(iter, action);
    }

    /**
     * Apply an action to every triple of an iterator.
     * If the iterator is attracted to a graph, the action must not attempt to
     * modify the graph but it can read it.
     */
    public static void apply(ExtendedIterator<Triple> iter, Consumer<Triple> action) {
        try {
            while(iter.hasNext()) {
                Triple t = iter.next();
                action.accept(t);
            }
        } finally { iter.close(); }
    }

    /**
     * Delete triples in the graph-to-modify (arg 1) that are in the source (arg 2).
     * @param modify
     * @param srcGraph
     */
    public static void deleteModify(Graph modify, Graph srcGraph) {
        // NB order of arguments.
        GraphUtil.deleteFrom(modify, srcGraph);
    }

    /** Convert an iterator of triples into quads for the specified graph name. */
    public static Iter<Quad> triples2quads(Node graphNode, Iterator<Triple> iter) {
        return Iter.iter(iter).map(t -> new Quad(graphNode, t));
    }

    /**
     * Convert an iterator of triples into quads for the default graph. This is
     * {@link Quad#defaultGraphIRI}, not {@link Quad#defaultGraphNodeGenerated}, which is
     * for quads outside a dataset, usually the output of parsers.
     */
    public static Iter<Quad> triples2quadsDftGraph(Iterator<Triple> iter) {
        return triples2quads(Quad.defaultGraphIRI, iter);
    }

    /**
     * Execute a graph transaction if the graph supports transactions else apply
     * without a transaction wrapper.
     */
    public static void execTxn(Graph graph, Runnable action) {
        TransactionHandler th = graph.getTransactionHandler();
        if ( th.transactionsSupported() )
            th.execute(action);
        else
            action.run();
    }

    /**
     * Execute a graph transaction and result result if the graph supports
     * transactions else execute and return without a transaction wrapper.
     */
    public static <X> X calcTxn(Graph graph, Supplier<X> action) {
        TransactionHandler th = graph.getTransactionHandler();
        if ( th.transactionsSupported() )
            return th.calculate(action);
        else
            return action.get();
    }


    /**
     * Find triples and execute an action on each triple found.
     */
    public static void findExec(Graph graph, Consumer<Triple> action, Node s, Node p , Node o) {
        ExtendedIterator<Triple> eIter = G.find(graph, s, p, o);
        try {
            eIter.forEach(action);
        } finally { eIter.close(); }
    }

    // From GraphUtils.

    /** Add triples into the destination (arg 1) from the source (arg 2)*/
    public static void addInto(Graph dstGraph, Graph srcGraph ) {
        if ( dstGraph == srcGraph && ! dstGraph.getEventManager().listening() )
            return ;
        dstGraph.getPrefixMapping().setNsPrefixes(srcGraph.getPrefixMapping()) ;
        findExec(srcGraph, dstGraph::add, ANY, ANY, ANY);
        dstGraph.getEventManager().notifyAddGraph( dstGraph, srcGraph );
    }

    private static void addIteratorWorker( Graph graph, Iterator<Triple> it ) {
        List<Triple> s = IteratorCollection.iteratorToList( it );
        addIteratorWorkerDirect(graph, s.iterator());
    }

    private static void addIteratorWorkerDirect( Graph graph, Iterator<Triple> it ) {
        it.forEachRemaining(graph::add);
//        if ( OldStyle && graph instanceof GraphWithPerform ) {
//            GraphWithPerform g = (GraphWithPerform)graph;
//            it.forEachRemaining(g::performAdd);
//        } else {
//            it.forEachRemaining(graph::add);
//        }
    }

    /**
     * Test whether a graph contains by "same term", regardless of whether the graph
     * implements "same value" or "same term".
     */
    public static boolean containsBySameTerm(Graph graph, Triple triple) {
        return containsBySameTerm(graph, triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /**
     * Test whether a graph contains by "same term", regardless of whether the graph
     * implements "same value" or "same term".
     */
    public static boolean containsBySameTerm(Graph graph, Node s, Node p, Node o) {
        // Do direct for efficiency.
        if ( ! graph.contains(s,p,o) )
            return false;
        // May have matched by value.  Do a term test find to restrict to RDF terms.
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        // Unless generalized RDF, only need to test object.
        Predicate<Triple> predicate = (dataTriple) -> sameTermMatch(s, p, o, dataTriple);
        iter = iter.filterKeep(predicate);
        try {
            return iter.hasNext();
        } finally { iter.close(); }

//    // For reference - just object
//    if ( !o.isConcrete() || o.isLiteral() ) {
//        Predicate<Triple> predicate = (t) -> sameTermMatch(o, t.getObject()) ;
//        iter = iter.filterKeep(predicate) ;
//    }
    }

    /**
     * Match S/P/O which can be wildcards (ANY or null), against a ground triple
     * (even ANY and variables are considered ground terms in the data triple).
     */
    public static boolean sameTermMatch(Node matchSubj, Node matchPred, Node matchObj, Triple dataTriple) {
        return sameTermMatch(matchSubj, dataTriple.getSubject()) &&
               sameTermMatch(matchPred, dataTriple.getPredicate()) &&
               sameTermMatch(matchObj,  dataTriple.getObject());
    }

    /**
     * Match a node, which can be a wildcard (ANY or null)
     * against a ground RDF Term (ANY and variables are considered ground terms in the
     * data term).
     * Language tags compare case-insensitively.
     */
    public static boolean sameTermMatch(Node match, Node data) {
        if ( isNullOrAny(match) )
            return true;
        if ( ! match.isLiteral() )
            // Only literals have values
            return match.equals(data);

        // Allow for case-insensitive language tag comparison.
        if ( ! Util.isLangString(data) || ! Util.isLangString(match) )
            // Any mix of node types except both strings with lang tags.
            return match.equals(data);

        // Both nodes with language tags : compare languages case insensitively.
        String lex1 = match.getLiteralLexicalForm();
        String lex2 = data.getLiteralLexicalForm();
        String lang1 = match.getLiteralLanguage();
        String lang2 = data.getLiteralLanguage();
        return lex1.equals(lex2) && lang1.equalsIgnoreCase(lang2);
    }


    /** Contains, and language tags compare case-insentively */
    public static boolean containsByLang(Graph g, Node s, Node p, Node o) {
        // Try exact.
        if ( g.contains(s, p, o) )
            return true;
        // If object is a wildcard, no further test needed.
        if ( G.isNullOrAny(o) )
            return false;
        // Only objects have languages
        if ( ! G.hasLang(o) )
            return false;
        // No term match - o has a language - check all s-p-?
        ExtendedIterator<Triple> iter = g.find(s, p, Node.ANY).filterKeep(triple->G.sameTermMatch(o, triple.getObject()));
        try {
            return iter.hasNext();
        } finally {
            iter.close();
        }
    }

    /** Contains, and language tags match case-insentively */
    public static ExtendedIterator<Triple> findByLang(Graph g, Node s, Node p, Node o) {
        // No specific value given.
        if ( G.isNullOrAny(o) )
            return g.find(s, p, o);
        if ( ! G.hasLang(o) )
            // Not a language literal. find(s,p,o) is enough.
            return g.find(s,p,o);
        // Filter by language value.
        ExtendedIterator<Triple> iter = g.find(s, p, Node.ANY)
                                         .filterKeep(triple->G.sameTermMatch(o, triple.getObject()));
        return iter;
    }

    /**
     * Test whether a node has a language tag.
     * If the node is null or any "match" (non-concrete) node, return false.
     */
    public static boolean hasLang(Node node) {
        if ( node == null )
            return false ;
        if ( ! node.isLiteral() )
            return false ;
        String x = node.getLiteralLanguage() ;
        if ( x == null )
            return false ;
        if ( x.equals("") )
            return false ;
        return true ;
    }
}
