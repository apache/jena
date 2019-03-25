/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage;

import java.util.Iterator;
import java.util.stream.Stream;

import static org.apache.jena.atlas.iterator.Iter.*;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.system.StorageLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

// Need to incorporate prefixes so transactions are announced atomically.

/**
 * A minimal interface for RDF storage. This is less that {@link DatasetGraph} or any of it's
 * derived classes and it just concerned with {@link Triple}s and {@link Quad}s, not
 * {@link Graph}s nor prefixes.
 * <p>
 * Storage is split into the triples for the default graph and quads for the named graphs.
 * In {@link #find(Node, Node, Node, Node)} ({@code find} on the named graphs),
 * {@code null} for the graph slot does not match the default graph.
 * <p>
 * <b>Concrete and Pattern Operations.</b>
 * </p>
 * <p>
 * Various API operations work on "concrete" terms. For example, {@code add}. These are
 * marked "concrete operation" in their javadoc. They are not matching operations. A
 * concrete term is one of a URI, blank node or literal. It is not {@code null},
 * {@code Node.ANY} nor a named variable. Any {@code Triple} or {@code Quad} must be
 * composed of concrete terms.
 * <p>
 * A pattern operation is one where the arguments are concrete terms or wildcard
 * {@code.ANY}. Such an operation will match zero or more triples or quads.
 * Any {@code Triple} or {@code Quad} can use {@code.ANY}.
 * <p>Pattern operations do not match named variables.
 * Using {@code Node.ANY} rather than {@code null} is preferred in pattern operations but
 * both are acceptable.
 */
public interface StorageRDF {
    /** Add a triple to the default graph.
     * <p>Concrete operation.
     */
    public default void add(Triple triple)
    { add(triple.getSubject(), triple.getPredicate(), triple.getObject()); }

    /** Add a quad.
     * <p>Concrete operation.
     */
    public default void add(Quad quad)
    { add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }

    /** Delete a triple from the default graph.
     * <p>Concrete operation.
     */
    public default void delete(Triple triple)
    { delete(triple.getSubject(), triple.getPredicate(), triple.getObject()); }

    /** Delete a quad from the default graph. All terms are concrete, and not {@code Node.ANY}.
     * For delete-by-pattern, see {@link #removeAll(Node, Node, Node, Node)}.
     * <p>Concrete operation.
     */
    public default void delete(Quad quad)
    { delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }

    /** Add a triple to the default graph.
     * <p>Concrete operation.
     */
    public void add(Node s, Node p, Node o);

    /** Add to a named graph.
     * <p>Concrete operation.
     */
    public void add(Node g, Node s, Node p, Node o);

    /**
     * Delete from the default graph. {@code s}, {@code p}, {@code o} are all concrete.
     * <p>Concrete operation.
     * <p>See {@link #removeAll(Node, Node, Node)} for remove by pattern.
     */
    public void delete(Node s, Node p, Node o);

    /**
     * Delete from a named graph. {@code s}, {@code p}, {@code o} are all concrete.
     * <p>Concrete operation.
     * <p>See {@link #removeAll(Node, Node, Node, Node)} for remove by pattern.
     */
    public void delete(Node g, Node s, Node p, Node o);

    /** Delete all triples matching a {@code find}-like pattern.
     *  <p>Pattern operation.
     */
    public default void removeAll(Node s, Node p, Node o) { StorageLib.removeTriples(this, s, p, o); }

    /** Delete all quads matching a {@code find}-like pattern.
     *  <p>Pattern operation.
     */
    public default void removeAll(Node g, Node s, Node p, Node o) { StorageLib.removeTriples(this, s, p, o); }

    /* Stream vs Iterator.
     * Yes.
     * Streams are nicer but they come with a performance cost. They create small intermediate objects.
     * "find" operations are an important performance point and in API use often return one triple;
     * the costs of a stream are a potential issue.
     *
     * Currently:
     * stream(...) are streams.
     * find(...) are iterators.
     */

//    // ??
//    /** Find in the default graph - return as quads (graph name {@link Quad#defaultGraphIRI}) */
//    default Stream<Quad> streamDftGraph(Node s, Node p, Node o) {
//        return stream(s, p, o).map(t -> Quad.create(Quad.defaultGraphIRI, t));
//    Iterator<Quad> findDftGraph(
//        return Iter.map(storage.find(s, p, o), t -> Quad.create(Quad.defaultGraphIRI, t));
//    }
//
    /** Find in the union graph (union of all named graphs, not the default graph).
     * An RDF graph is a set of triples - the union graph does not shows duplicates even
     * if more than one named graph contains a given triple.
     * @implNote
     * The default implementation of this operation involves the use of {@link Stream#distinct()}
     * which is a <i><a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps">stateful
     * intermediate operation</a></i>. Without additional internal knowledge,
     * it is necessary to remember all triples in the stream
     * so far to know whether the next triple is a duplicate or not.
     * This can be a signiifcant amount of intermediate space.
     * <p>
     * An implmentation may be able to exploit its internal representation to
     * means that this operation can be implemented more efficient, for example,
     * knowing that duplicate triples (same triple, from different graphs) will
     * be adjacent in the stream so not requires the full cost of {@code distinct}
     * to remove duplicates.
     * <p>Pattern operation.
     */
    public default Iterator<Triple> findUnionGraph(Node s, Node p, Node o) {
        return distinct(
                map(find(Node.ANY, s, p, o), Quad::asTriple)
               );
    }

    /** Find in named graphs: does not look in the default graph.
     * <p>Pattern operation.
     */
    public default Iterator<Quad> find(Quad quad) {
        return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    /** Find in named graphs: does not look in the default graph.
     * <p>Pattern operation.
     */
    public Iterator<Quad> find(Node g, Node s, Node p, Node o);

    /** Find in the default graph.
     * <p>Pattern operation.
     */
    public default Iterator<Triple> find(Triple triple) {
        return find(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Find in the default graph.
     * <p>Pattern operation.
     */
    public Iterator<Triple> find(Node s, Node p, Node o);

    /** See {@link #findUnionGraph}.
     * <p>Pattern operation.
     */
    public default Stream<Triple> streamUnionGraph(Node s, Node p, Node o) {
        return stream(Node.ANY, s, p, o).map(Quad::asTriple).distinct();
    }

    /** Find in named graphs: does not look in the default graph.
     * <p>Pattern operation.
     */
    public default Stream<Quad> stream(Quad quad) {
        return stream(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    /** Find in named graphs: does not look in the default graph.
     * <p>Pattern operation.
     */
    public default Stream<Quad> stream(Node g, Node s, Node p, Node o) {
        // Root of stream operation.
        return Iter.asStream(find(g, s, p, o));
    }

    /** Find in the default graph.
     * <p>Pattern operations.
     */
    public default Stream<Triple> stream(Triple triple) {
        return stream(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Find in the default graph.
     * <p>Pattern operation.
     */
    public default Stream<Triple> stream(Node s, Node p, Node o) {
        // Root of stream operations.
        return Iter.asStream(find(s, p, o));
    }

    // Leave one "contains" for triple and quads as abstract, to suggest direct implementation.

    /** Test whether the default graph contains the triple.
     * <p>Pattern operation.
     * <p>Equivalent to {@code find(triple).hasNext()} or {@code stream(triple).findAny().isPresent()}.
     */
    public default boolean contains(Triple triple) {
        return contains(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Test whether the default graph contains the triple.
     * <p>Pattern operation.
     * <p>Equivalent to {@code find(s,p,o).hasNext()}.
     */
    public boolean contains(Node s, Node p, Node o);

    /** Test whether any named graph matches the quad.
     * <p>Pattern operation.
     * <p>Equivalent to {@code find(quad).hasNext()} or {@code stream(quad).findAny().isPresent()}.
     */
    public default boolean contains(Quad quad) {
        return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    /** Test whether any named graph matches the quad.
     * <p>Pattern operation.
     * <p>Equivalent to {@code find(g,s,p,o).hasNext()}.
     */
    boolean contains(Node g, Node s, Node p, Node o);
}
