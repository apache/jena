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

package org.apache.jena.fuseki.access;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

/** A {@link SecurityContext} is the things actor (user, role) is allowed to do.
 * Currently version: the set of graphs, by graph name, they can access.
 * It can be inverted into a "deny" policy with {@link Predicate#negate()}.
 */
public interface SecurityContext {
    public static final SecurityContext NONE = new SecurityContextAllowNone();
    public static final SecurityContext ALL = new SecurityContextAllowAll();
    public static SecurityContext ALL_NG(DatasetGraph dsg) {
        Collection<Node> names = Iter.toList(dsg.listGraphNodes());
        //return new SecurityContextAllowNamedGraphs(dsg);
        return new SecurityContextView(names);
    }

    public static final Node allGraphs = NodeFactory.createURI("urn:jena:accessAllGraphs");
    public static final Node allNamedGraphs = NodeFactory.createURI("urn:jena:accessAllNamedGraphs");
    public static final Node allNamedGraphsStr = NodeFactory.createLiteral("*");
    public static final Node allGraphsStr = NodeFactory.createLiteral("**");

    /**
     * Collection of visible graph names.
     * <p>
     * This method returns null for "all" to avoid needing to calculate the current
     * set of named graph names. A collection of no elements means no named graphs
     * are visible.
     */
    public Collection<Node> visibleGraphs();

    /**
     * Collection of visible graph URI names.
     * <p>
     * This method returns null for "all" to avoid needing to calculate the current
     * set of named graph names. A collection of no elements means no named graphs
     * are visible.
     */
    public default Collection<String> visibleGraphNames() {
        if ( visibleGraphs() == null )
            return null;
        return visibleGraphs().stream()
                .filter(Node::isURI)
                .map(Node::getURI)
                .collect(toList());
    }

    public boolean visableDefaultGraph();

    public default QueryExecution createQueryExecution(String queryString, DatasetGraph dsg) {
        return createQueryExecution(QueryFactory.create(queryString), dsg);
    }

    public QueryExecution createQueryExecution(Query query, DatasetGraph dsg);

    /**
     * Quad filter to reflect the security policy of this {@link SecurityContext}. It is
     * better to call {@link #createQueryExecution(Query, DatasetGraph)} which may be more
     * efficient.
     */
    public Predicate<Quad> predicateQuad();

    /**
     * Apply a filter suitable for the TDB-backed {@link DatasetGraph}, to the {@link Context} of the
     * {@link QueryExecution}. This does not modify the {@link DatasetGraph}.
     * Throws {@link IllegalArgumentException} if {@link DatasetGraph} is not a TDB1 or TDB2 backed dataset.
     * May throw {@link UnsupportedOperationException}.
     */
    public default void filterTDB(DatasetGraph dsg, QueryExecution qExec) {
        if ( ! org.apache.jena.tdb1.sys.TDBInternal.isTDB1(dsg) || ! org.apache.jena.tdb2.sys.TDBInternal.isTDB2(dsg) )
            throw new IllegalArgumentException("Not a TDB database");
        throw new UnsupportedOperationException();
    }
}
