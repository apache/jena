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

package org.apache.jena.rdfpatch;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/** Interface for a stream of changes to to an RDF Dataset, or RDF Graph.
 * For an RDF graph, the "graph name" will be null.
 */
public interface RDFChanges {
    // Consider the triples versions.

    /** Header field.
     * Headers are metadata about the changes being made.
     */
    public void header(String field, Node value);

    /**
     * Notification that a quad or triple is added.
     * A stream of Triples outside a dataset will have null for the graph name.
     * Inside an RDF Dataset, it may be more natural to use "urn:x-arq:DefaultGraph" or "urn:x-arq:DefaultGraphNode"
     * in which case test with {@link Quad#isDefaultGraph(Node)}.
     * <p>
     * It is not defined whether the add happens before or after this notification all.
     */
    public void add(Node g, Node s, Node p, Node o);

//    /**
//     * Notification that a triple is added.
//     * A stream of Triples outside a dataset will have null for the graph name.
//     */
//    public default void add(Node s, Node p, Node o) {
//        add(null, s, p, o);
//    }

    /**
     * Notification that a quad or triple is deleted.
     * A stream of Triples outside a dataset will have null for the graph name.
     * Inside an RDF Dataset, it may be more natural to use "urn:x-arq:DefaultGraph" or "urn:x-arq:DefaultGraphNode"
     * in which case test with {@link Quad#isDefaultGraph(Node)}.
     * <p>
     * It is not defined whether the delete happens before or after this notification all.
     */
    public void delete(Node g, Node s, Node p, Node o);

//    /**
//     * Notification that a triple is deleted.
//     * A stream of Triples outside a dataset will have null for the graph name.
//     */
//    public default void delete(Node s, Node p, Node o) {
//        delete(null, s, p, o);
//    }

    /**
     * Add a prefix. The graph name follows the same rules as {@link #add}.
     */
    public void addPrefix(Node gn, String prefix, String uriStr);

    /**
     * Delete a prefix. The graph name follows the same rules as {@link #add}.
     */
    public void deletePrefix(Node gn, String prefix);

    /** Indicator that a transaction begins, or becomes a write transaction. */
    public void txnBegin();

    /** Indicator that a transaction commits.
     *  If this throws an exception, the transaction will be aborted locally and not commit after all.
     */
    public void txnCommit();

    /** Indicator that a transaction aborts */
    public void txnAbort();

    /** Segment marker.
     * <p>
     * A segment is a number of transactions; the grouping rationale is not defined by RDF Patch.
     * <p>
     * It might be used to indicate a logical collection of change transactions in a long stream of transactions.
     * <p>
     * There is no guarantee it will be used.
     * <p>
     * Segments must contain complete transactions.<br/>
     * Segments must not span start-finish pairs.
     */
    public void segment();

    /**
     * Start processing.
     * The exact meaning is implementation dependent.
     * This should be paired with a {@link #finish}.
     */
    public void start();

    /**
     * Finish processing.
     * The exact meaning is implementation dependent.
     * This should be paired with a {@link #start}.
     */
    public void finish();
}
