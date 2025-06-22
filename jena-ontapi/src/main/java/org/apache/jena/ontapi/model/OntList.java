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

package org.apache.jena.ontapi.model;

import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SWRL;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A parameterized analogue of the {@link RDFList Jena []-List} that behaves like a java collection.
 * Please note: currently it is not a Personality resource and, therefore, Jena Polymorphism is not supported for it.
 * The latter means that attempt to cast any {@link RDFNode RDF Node} to this view
 * will cause {@link org.apache.jena.enhanced.UnsupportedPolymorphismException UnsupportedPolymorphismException},
 * but it is possible do the opposite: cast an instance of this interface to the {@link RDFList Jena []-List} view
 * using the expression {@code OntList.as(RDFList.class)}.
 * Also note: switching to nil-list (by any of the add/remove/clean operations) from a not-empty list and vice verse
 * violates a Jena invariant, this means that this {@link OntResource} behaves not always like pure {@link Resource Jena Resource}
 * and all the methods may throw {@link OntJenaException.IllegalState}
 * in case of usage different instances encapsulating the same resource-list.
 * <p>
 * Unlike the standard {@link RDFList []-List} implementation, ONT-List can be typed.
 * This means that each resource-member of []-List may have an {@link RDF#type rdf:type} declaration,
 * while the standard RDF []-List impl does not support typing.
 * See below for an example of a typed []-list in Turtle format:
 * <pre>{@code
 * [ rdf:type   <type> ;
 *   rdf:first  <A> ;
 *   rdf:rest   [ rdf:type   <type> ;
 *                rdf:first  <B> ;
 *                rdf:rest   rdf:nil
 *              ]
 * ] .
 * }</pre>
 * Note, that an empty []-list (i.e. {@link RDF#nil nil}-list) cannot be typed.
 * <p>
 * Using the method {@link #getMainStatement()} it is possible to add annotations with any nesting depth.
 *
 * @param <E> the type of {@link RDFNode rdf-node}s in this list
 * @see RDFNodeList
 */
public interface OntList<E extends RDFNode> extends RDFNodeList<E>, OntResource {

    /**
     * Adds the given value to the end of the list.
     *
     * @param e {@code E} rdf-node
     * @return this list instance
     * @see #add(RDFNode)
     */
    OntList<E> addLast(E e);

    /**
     * Removes the last element from this list.
     * No-op in case of nil-list.
     * Note: the removed element can be of any type, not necessarily of the type {@code E}.
     *
     * @return this list instance
     * @see #remove()
     */
    OntList<E> removeLast();

    /**
     * Inserts the specified element at the beginning of this list.
     * As a rule, this operation is faster than {@link #addLast(RDFNode)},
     * since it does not require iteration to the end of the list.
     *
     * @param e {@code E} rdf-node
     * @return this list instance
     */
    OntList<E> addFirst(E e);

    /**
     * Removes and the first element from this list.
     * No-op in case of empty list.
     * Note: the last element can be of any type, not necessarily of type {@code E}.
     * As a rule, this operation is faster than {@link #removeLast()} ,
     * since the last one requires iteration to the end of the list.
     *
     * @return the first element from this list
     */
    OntList<E> removeFirst();

    /**
     * Removes all elements from this list.
     * The list will be empty (nil) after this call returns.
     *
     * @return this (empty) instance
     */
    OntList<E> clear();

    /**
     * Answers the list that is the tail of this list starting from the given position.
     * Note: the returned list cannot be annotated.
     * This method can be used to insert/remove/clear the parent list at any position,
     * e.g. the operation {@code get(1).addFirst(e)} will insert the element {@code e} at second position.
     *
     * @param index int, not negative
     * @return new {@code OntList} instance
     * @throws OntJenaException.IllegalArgument if the specified index is out of list bounds
     */
    OntList<E> get(int index) throws OntJenaException;

    /**
     * Answers the resource-type of this ONT-list, if it is typed.
     * A standard RDF-list does not require any {@link RDF#type rdf:type}
     * in its RDF-deeps, since predicates {@link RDF#first rdf:first},
     * {@link RDF#rest rdf:rest}
     * and {@link RDF#nil rdf:nil} are sufficient for its description.
     * In this case the method returns {@link Optional#empty() empty} result.
     * But in some rare semantics (e.g. see {@link SWRL}),
     * the []-list must to be typed.
     * In that case this method returns a URI-{@code Resource} (that is wrapped as {@code Optional})
     * describing the []-list's type
     * (for SWRL it is {@link SWRL#AtomList swrl:AtomList}).
     *
     * @return {@link Optional} around the URI-{@link Resource}, can be empty.
     */
    Optional<Resource> type();

    /**
     * Lists all statements related to this list.
     * For nil-list an empty stream is expected.
     * Note: it returns all statements even if the list contains incompatible types.
     *
     * @return Stream of {@link OntStatement Ontology Statement}s that does not support annotations
     */
    @Override
    Stream<OntStatement> spec();

    /**
     * Returns the root statement plus spec.
     * Please note: only the first item (root) is allowed to be annotated.
     *
     * @return {@code Stream} of {@link OntStatement Ontology Statement}s
     */
    default Stream<OntStatement> content() {
        return Stream.concat(Stream.of(getMainStatement()), spec());
    }

    /**
     * Adds the given value to the end of the list.
     * This is a synonym for the {@code this.addLast(e)}.
     *
     * @param e {@code E} rdf-node
     * @return this list instance
     * @see #addLast(RDFNode)
     */
    default OntList<E> add(E e) {
        return addLast(e);
    }

    /**
     * Removes the last element from this list.
     * This is a synonym for the {@code this.removeLast(e)}.
     *
     * @return this list instance
     * @see #removeLast()
     */
    default OntList<E> remove() {
        return removeLast();
    }

    /**
     * Appends all the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified collection's iterator.
     *
     * @param c Collection of {@code E}-elements
     * @return this list instance
     */
    default OntList<E> addAll(Collection<? extends E> c) {
        c.forEach(this::add);
        return this;
    }

    /**
     * Determines if this Ontology List is locally defined.
     * This means that the resource definition (i.e., a the {@link #getMainStatement() root statement})
     * belongs to the base ontology graph.
     * If the ontology contains subgraphs (which should match {@code owl:imports} in OWL)
     * and the resource is defined in one of them,
     * than this method called from top-level interface will return {@code false}.
     */
    @Override
    default boolean isLocal() {
        return getMainStatement().isLocal();
    }

}
