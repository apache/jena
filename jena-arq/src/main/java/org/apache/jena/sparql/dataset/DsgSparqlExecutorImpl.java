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

package org.apache.jena.sparql.dataset;

import static org.apache.jena.sparql.dataset.DsgSparqlExecUtils.isWildcard;

import java.util.List;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateCopy;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class DsgSparqlExecutorImpl implements DsgSparqlExecutor {

    public enum GraphRemovalStrategy {
        /* Remove graphs using {@code DELETE WHERE { GRAPH :g { ?s ?p ?o } }}. */
        DELETE,

        /* Remove graphs using {@code CLEAR GRAPH SILENT :g}.
         * The {@code SILENT} prevents failures when clearing absent graphs.*/
        CLEAR_SILENT,

        /* Remove graphs using {@code DROP GRAPH :g}.
         * The {@code SILENT} prevents failures when dropping absent graphs.*/
        DROP_SILENT;
    }

    private GraphRemovalStrategy graphRemovalStrategy;
    private boolean detectGraphRemovalByDeleteAny;

    public DsgSparqlExecutorImpl() {
        this(GraphRemovalStrategy.DROP_SILENT, true);
    }

    /**
     * @param graphRemovalStrategy The strategy for {@code clearGraph(g)}, such as
     *        {@code CLEAR GRAPH}, {@code DROP GRAPH}, or {@code DELETE}.
     * @param detectGraphRemovalByDeleteAny Whether {@code deleteAny(g, ANY, ANY, ANY)} should call removeGraph.
     */
    public DsgSparqlExecutorImpl(GraphRemovalStrategy graphRemovalStrategy, boolean detectGraphRemovalByDeleteAny) {
        super();
        this.graphRemovalStrategy = graphRemovalStrategy;
        this.detectGraphRemovalByDeleteAny = detectGraphRemovalByDeleteAny;
    }

    @Override
    public void add(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes, Quad quad) {
        if (!quad.isConcrete()) {
            throw new IllegalArgumentException("Concrete quad expected.");
        }
        Update upd = new UpdateDataInsert(new QuadDataAcc(List.of(quad)));
        UpdateExec uExec = executor.apply(new UpdateRequest(upd));
        uExec.execute();
    }

    @Override
    public void delete(
            Function<UpdateRequest, ? extends UpdateExec> executor, PrefixMapping prefixes,
            Quad quad) {
        if (!quad.isConcrete()) {
            throw new IllegalArgumentException("Concrete quad expected.");
        }
        Update upd = new UpdateDataDelete(new QuadDataAcc(List.of(quad)));
        UpdateExec uExec = executor.apply(new UpdateRequest(upd));
        uExec.execute();
    }

    @Override
    public void deleteAny(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o) {
        // Call removeGraph if s, p and o are wildcards and detectGraphRemovalByDeleteAny is true.
        // However, prevent infinite recursion: don't call removeGraph if the strategy is PATTERN,
        // because then removeGraph will call deleteAny again.
        if (detectGraphRemovalByDeleteAny && !graphRemovalStrategy.equals(GraphRemovalStrategy.DELETE)
                && (isWildcard(s) && isWildcard(p) && isWildcard(o))) {
            removeGraph(executor, prefixes, g);
        } else {
            UpdateRequest updateRequest = DsgSparqlExecUtils.buildDeleteByPattern(g, s, p, o);
            UpdateExec uExec = executor.apply(updateRequest);
            uExec.execute();
        }
    }

    @Override
    public void removeGraph(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Node g) {
        if (GraphRemovalStrategy.DELETE.equals(graphRemovalStrategy)) {
            deleteAny(executor, prefixes, g, Node.ANY, Node.ANY, Node.ANY);
        } else {
            Update update = switch(graphRemovalStrategy) {
            case CLEAR_SILENT-> DsgSparqlExecUtils.buildGraphRemoval(g, false, true);
            case DROP_SILENT -> DsgSparqlExecUtils.buildGraphRemoval(g, true, true);
            default -> throw new IllegalStateException("Unexpected value: " + graphRemovalStrategy);
            };
            UpdateRequest updateRequest = new UpdateRequest(update);
            UpdateExec uExec = executor.apply(updateRequest);
            uExec.execute();
        }
    }

    @Override
    public void copy(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Node source, Node destination) {
        Target src = DsgSparqlExecUtils.chooseTarget(source);
        Target dst = DsgSparqlExecUtils.chooseTarget(destination);
        Update update = new UpdateCopy(src, dst);
        UpdateRequest updateRequest = new UpdateRequest(update);
        UpdateExec uExec = executor.apply(updateRequest);
        uExec.execute();
    }

    @Override
    public IteratorCloseable<Node> listGraphNodes(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes) {
        return DsgSparqlExecUtils.listGraphNodes(executor);
    }

    @Override
    public IteratorCloseable<Quad> find(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o) {
        if (g == null || Node.ANY.equals(g)) {
            return findTriplesOrQuads(executor, prefixes, s, p, o);
        } else if (Quad.isDefaultGraph(g)) {
            // Default graph -> triples wrapped as quads.
            IteratorCloseable<Triple> base = findTriples(executor, prefixes, s, p, o);
            return (IteratorCloseable<Quad>)Iter.map(base, t -> Quad.create(null, t));
        } else {
            return findNG(executor, prefixes, g, s, p, o);
        }
    }

    @Override
    public IteratorCloseable<Quad> findNG(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o) {
        return DsgSparqlExecUtils.findQuads(executor, g, s, p, o);
    }

    @Override
    public boolean contains(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o) {
        return DsgSparqlExecUtils.contains(executor, g, s, p, o);
    }

    @Override
    public boolean containsGraph(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g) {
        return DsgSparqlExecUtils.containsGraph(executor, g);
    }

    protected IteratorCloseable<Triple> findTriples(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node s, Node p, Node o) {
        return DsgSparqlExecUtils.findTriples(executor, s, p, o);
    }

    protected IteratorCloseable<Quad> findTriplesOrQuads(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node s, Node p, Node o) {
        return DsgSparqlExecUtils.findTriplesOrQuads(executor, s, p, o);
    }

    @Override
    public long fetchGraphCount(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes) {
        return DsgSparqlExecUtils.fetchGraphCount(executor);
    }

    @Override
    public long fetchDefaultGraphSize(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes) {
        return DsgSparqlExecUtils.fetchDefaultGraphSize(executor);
    }

    @Override
    public long fetchGraphSize(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g) {
        return DsgSparqlExecUtils.fetchGraphSize(executor, g);
    }
}
