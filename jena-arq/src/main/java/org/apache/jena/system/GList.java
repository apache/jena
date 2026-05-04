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

package org.apache.jena.system;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Operations on RDF Collections (RDF Lists).
 * <p>
 * Operations may throw {@link RDFDataException} if the list is not well-formed.
 * <p>
 * Operations check for exactly {@code rdf:first}, and exactly one {@code rdf:rest},
 * but do not check for cycles unless noted in their javadoc.
 * <p>
 * To get a list of all the items in a list, use {@link #members} operations, which
 * does check for cycles, or if the list is known to be well-formed, one of the {@link #elements}
 * operations, which do not check for cycles.
 * <p>
 * The {@link #contains} operation does not check for cycles so that repeated use on
 * a list does not perform duplicate checking work repeatedly.
 * <p>
 * If a list is potentially mal-formed by having a cycle, check first with
 * {@link #isWellformedList(Graph, Node)} or
 * {@link #isWellformedListEx(Graph, Node)}.
 * <p>
 * List validation only needs to be performed once.
 * <p>
 * List arising from parsing Turtle or TriG syntax for lists will be cycle-free.
 * <p>
 * <b>This class is <em>not</em> public API</b>.
 *
 * @see GraphList - uses a findable abstaction ({@link GNode}to work on a graph or lists of triples.
 */
public class GList {

    static { JenaSystem.init(); }

    private static final Node CAR = RDF.Nodes.first;
    private static final Node CDR = RDF.Nodes.rest;
    private static final Node NIL = RDF.Nodes.nil;
    private static final Node RDF_TYPE = RDF.Nodes.type;
    private static final long BAD_LIST = -1;
    private static final Function<String, RuntimeException> stdExceptionMaker = RDFDataException::new;

    // forEach (unchecked).

//    occurs(GNode, Node)
//    contains(GNode, Node)
//    get(GNode, int)
//    index(GNode, Node)
//    indexes(GNode, Node)
//    triples(GNode, Collection<Triple>)
//    allTriples(GNode)
//    allTriples(GNode, Collection<Triple>)
//    findAllLists(Graph)
//    listToTriples(List<Node>, BasicPattern)


    /**
     * Check for a valid list; throw a {@link RDFDataException} if the list is bad in some way.
     * The {@link RDFDataException} message indicates the problem with the list.
     * @param graph
     * @param list
     */
    public static void isWellformedListEx(Graph graph, Node list) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        forEachMember(graph, list, false, null, stdExceptionMaker);
    }

    /** Check for a valid list; throw a {@link RDFDataException} if the list is bad in some way.
     * The {@link RDFDataException} message indicates the problem with the list.
     * @param graph
     * @param list
     * @param closedCells - whether to check that only {@code rdf:first} and {@code rdf:next} are used on a list cell
     */
    public static void isWellformedListEx(Graph graph, Node list, boolean closedCells) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        forEachMember(graph, list, closedCells, null, stdExceptionMaker);
    }

    /**
     * Check for a valid list; return true if well-formed else return false.
     * Use {@link #isWellformedListEx(Graph, Node)} to get an except with an
     * informational message about the problem detected.
     *
     * @param graph
     * @param list
     */
    public static boolean isWellformedList(Graph graph, Node list) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        return forEachMember(graph, list, false, null, null) != BAD_LIST;
    }

    /** Check for a valid list; return true if well-formed else return false.
     * Use {@link #isWellformedListEx(Graph, Node, boolean)} to get an except with an
     * informational message about the problem detected.
     * @param graph
     * @param list
     * @param closedCells - whether to check that only rdf:first and rdf:next are used on a list cell
     */
    public static boolean isWellformedList(Graph graph, Node list, boolean closedCells) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        return forEachMember(graph, list, closedCells, null, null) != BAD_LIST;
    }

    /**
     * Return the members of a well-formed RDF list.
     * @throws RDFDataException if the list is not well-formed, including if it has a cycle.
     */
    public static List<Node> members(Graph graph, Node list) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        if ( list.equals(NIL) )
            return List.of();
        List<Node> acc = new ArrayList<>();
        forEachMember(graph, list, false, acc::add, stdExceptionMaker);
        return acc;
    }

    /**
     * Accumulate the members of a well-formed RDF list.
     * @throws RDFDataException if the list is not well-formed, including if it has a cycle.
     */
    public static void members(Graph graph, Node list, Collection<Node> acc) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        if ( list.equals(NIL) )
            return;
        forEachMember(graph, list, false, acc::add, stdExceptionMaker);
    }

    /**
     * Return the elements of a well-formed RDF list.
     * This operation does not check for cycles.
     * {@link #members} is has the same result and incorporates a well-formness cycle check.
     */
    public static List<Node> elements(Graph graph, Node list) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);

        if ( list.equals(NIL) )
            return List.of();
        List<Node> acc = new ArrayList<>();
        elements(graph, list, acc);
        return acc;
    }

    /**
     * Accumulate the elements of a well-formed RDF list.
     * This operation does not check for cycles.
     */
    public static void elements(Graph graph, Node list, Collection<Node> acc) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        Objects.requireNonNull(acc);
        if ( list.equals(NIL) )
            return;
        Node cell = list;
        while ( ! listEnd(graph, cell) ) {
            Node elt = G.getOneSP(graph, cell, CAR);
            Node next = G.getOneSP(graph, cell, CDR);
            acc.add(elt);
            cell = next;
        }
    }

    /**
     * Run an action on each element of a list, in order.
     * @see #iterator(Graph, Node)
     */
    public static void forEach(Graph graph, Node list, Consumer<Node> action) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        Objects.requireNonNull(action);
        if ( list.equals(NIL) )
            return;
        // No cycle checking.
        long countElts = 0;
        Node cell = list;
        while(!listEnd(graph, cell) ) {
            Node elt = G.getOneSP(graph, cell, CAR);
            Node next = G.getOneSP(graph, cell, CDR);
            cell = next;
            action.accept(elt);
        }
    }

    /**
     * Return the length of a well-formed list.
     * This operations assumes the list is well-formed.
     * See {@link #isWellformedList(Graph, Node)} for check a list.
     */
    public static long listLength(Graph graph, Node list) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        if ( list.equals(NIL) )
            return 0;
        // No cycle checking.
        long countElts = 0;
        Node cell = list;
        while(!listEnd(graph, cell) ) {
            // Check integrity.
            G.hasOneSP(graph, cell, CDR);
            Node next = G.getOneSP(graph, cell, CDR);
            cell = next;
            countElts++;
        }
        return countElts;
    }

    // --------

    /**
     * Return an iterator over the list.
     * The iterator does not check for well-formed lists.
     * Ensure the list is well-formed - {@link #isWellformedListEx(Graph, Node)}.
     *
     * @see #forEach
     */
    public static Iterator<Node> iterator(Graph graph, Node list) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        return new RDFListIterator(graph, list);
    }

    /**
     * Return the first index of an occurrence of a node in a list.
     * Return -1 for not in list.
     * Indexes start at 0.
     * @param graph
     * @param list
     * @param item to check
     */
    public static int indexOf(Graph graph, Node list, Node item) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        Objects.requireNonNull(item);
        Node cell = list;
        int index = 0;
        while( !listEnd(graph, cell) ) {
            Node elt = G.getOneSP(graph, cell, CAR);
            Node next = G.getOneSP(graph, cell, CDR);
            cell = next;
            if ( item.sameTermAs(elt) )
                return index;
            index++;
        }
        return -1;
    }

    /**
     * Return whether an item is in the list.
     * Return for not in list.
     * @param graph
     * @param list
     * @param item to check for
     */
    public static boolean contains(Graph graph, Node list, Node item) {
        return indexOf(graph, list, item) >= 0;
    }

    /**
     * Return the list element at an index (indexes start at 0).
     * <p>
     * Do not use this to iterate over a list - consider using {@link #members} to
     * collect the list in a single pass.
     */
    public static Node get(Graph graph, Node list, int index) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(list);
        if ( index < 0 )
            return null;
        Node cell = list;
        Node elt = null ;
        for ( int i = 0 ; i <= index ; i++ ) {
            if ( listEnd(graph, cell) )
                return null;
            elt = G.getOneSP(graph, cell, CAR);
            Node next = G.getOneSP(graph, cell, CDR);
            cell = next;
        }
        return elt;
    }

    public static boolean isListNode(Graph graph, Node node) {
        if ( node.equals(NIL) )
            return true;
        // Well-formedness check.
        return isCons(graph, node);
    }

    /**
     * Run over a list, checking for cycles and well-formed list cons cells.
     * Call an action on each element if {@ocde elementAction} is not null.
     * For a bad list, throw an exception or return -1 if {@code exceptionMaker} is null.
     */
    private static long forEachMember(Graph graph, Node node,
                                      boolean closedCells,
                                      Consumer<Node> elementAction,
                                      Function<String, RuntimeException> exceptionMaker) {
        Node cell = node;
        Set<Node> visited = new HashSet<>();
        long numListElements = 0 ;
        while (!listEnd(graph, cell)) {
            if ( visited.contains(cell) ) {
                badListEx("Cyclic list", cell, exceptionMaker);
                return -1;
            }
            visited.add(cell);
            // rdf:first elt;  rdf:rest next;
            Node elt = null;
            Node next = null;
            ExtendedIterator<Triple> iterSP = G.find(graph, cell, null, null);
            try {
                while(iterSP.hasNext()) {
                    Triple t = iterSP.next();
                    Node predicate = t.getPredicate();

                    if ( CAR.equals(predicate) ) {
                        if ( elt != null ) {
                            badListEx("List contains an element with two rdf:first", cell, exceptionMaker);
                            return -1;
                        }
                        elt = t.getObject();
                        continue;
                    }
                    if ( CDR.equals(predicate) ) {
                        if ( next != null ) {
                            badListEx("List contains an element with two rdf:next", cell, exceptionMaker);
                            return -1;
                        }
                        next = t.getObject();
                        continue;
                    }
                    if ( RDF_TYPE.equals(predicate) ) {
                        // Allow rdf:type on a list cons cell.
                        continue;
                    }

                    if ( closedCells ) {
                        badListEx("List contains non-list triples", cell, exceptionMaker);
                        return -1;
                    }
                }

                if ( elt == null ) {
                    badListEx("List contains an element with no rdf:first", node, exceptionMaker);
                    return -1;
                }
                if ( next == null ) {
                    badListEx("List contains an element with no rdf:next", node, exceptionMaker);
                    return -1;
                }
                // Valid list element
                numListElements++;
                if ( elementAction != null )
                    elementAction.accept(elt);
                cell = next;
            } finally { iterSP.close(); }
        }
        return numListElements;
    }

    private static class RDFListIterator implements Iterator<Node> {

        private Function<String, RuntimeException> exceptionMaker = RDFDataException::new;
        private final Graph graph;
        private Node current = null;

        RDFListIterator(Graph graph, Node node) {
            this.graph = graph;
            this.current = node;
            //isWellformedListEx(graph, node);
        }

        @Override
        public boolean hasNext() {
            return ! listEnd(graph, current);
        }

        @Override
        public Node next() {
            // Assume well-formed.
            Node elt = G.getOneSP(graph, current, CAR);
            Node next = G.getOneSP(graph, current, CDR);
            current = next;
            return elt;
        }

        // Very complicated for the sake of walking the list once.
        // Instead, expect the caller to have validated the list once before list operations.
//        @Override
//        public Node next() {
//            if ( listEnd(graph, current) )
//                throw new NoSuchElementException();
//            if ( true ) {
//                // Checking.
//                ExtendedIterator<Triple> iterSP = G.find(graph, current, null, null);
//                try {
//                    Node elt = null;
//                    Node next = null;
//                    boolean closedCells = true;
//
//                    while(iterSP.hasNext()) {
//                        Triple t = iterSP.next();
//                        Node predicate = t.getPredicate();
//
//                        if ( CAR.equals(predicate) ) {
//                            if ( elt != null ) {
//                                badListEx("List contains an element with two rdf:first", null, exceptionMaker);
//                                throw new NoSuchElementException();
//                            }
//                            elt = t.getObject();
//                            continue;
//                        }
//                        if ( CDR.equals(predicate) ) {
//                            if ( next != null ) {
//                                badListEx("List contains an element with two rdf:next", null, exceptionMaker);
//                                throw new NoSuchElementException();
//                            }
//                            next = t.getObject();
//                            continue;
//                        }
//                        if ( RDF_TYPE.equals(predicate) ) {
//                            // Allow rdf:type on a list cons cell.
//                            continue;
//                        }
//
//                        if ( closedCells ) {
//                            badListEx("List contains non-list triples", null, exceptionMaker);
//                            throw new NoSuchElementException();
//                        }
//                    }
//                    current = next;
//                    return elt;
//                } finally { iterSP.close(); }
//
//            } else {
//                // Assume well-formed.
//                Node elt = G.getOneSP(graph, current, CAR);
//                Node next = G.getOneSP(graph, current, CDR);
//                current = next;
//                return elt;
//            }
//        }
    }

    private static boolean isCons (Graph graph, Node node) {
        return G.hasOneSP(graph, node, CDR) && G.hasOneSP(graph, node, CAR);
    }

    private static boolean listEnd(Graph graph, Node node) {
        return node == null || node.equals(NIL);
    }

    /** Check for a valid list; return true if the list is well-formed else return false
     * @param graph
     * @param node
     * @param closedCells - whether to check that only rdf:first and rdf:next are used on a list cell
     */
    private static void badListEx(String msg, Node node, Function<String, RuntimeException> exceptionMaker ) {
        if ( exceptionMaker != null )
            throw exceptionMaker.apply(msg);
    }
}
