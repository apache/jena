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

import java.util.function.Function;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

/**
 * Helper that knows how to turn a {@link Query}/{@link UpdateRequest} into a
 * {@link QueryExec}/{@link UpdateExec} and then transform the result as
 * needed by {@link DatasetGraphSparql}.
 */
public interface DsgSparqlExecutor {

    public static final DsgSparqlExecutor DEFAULT = new DsgSparqlExecutorImpl();

    /* ------------  Query execution  ------------ */

    public IteratorCloseable<Node> listGraphNodes(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes);

    public IteratorCloseable<Quad> find(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o);

    public IteratorCloseable<Quad> findNG(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o);

    public boolean contains(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o);

    public boolean containsGraph(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g);

    public long fetchGraphCount(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes);

    public long fetchDefaultGraphSize(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes);

    public long fetchGraphSize(
            Function<Query, ? extends QueryExec> executor,
            PrefixMapping prefixes,
            Node g);

    /* ------------  Update execution  ------------ */

    public void add(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Quad quad);

    public void delete(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Quad quad);

    public void deleteAny(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Node g, Node s, Node p, Node o);

    public void removeGraph(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Node g);

    public void copy(
            Function<UpdateRequest, ? extends UpdateExec> executor,
            PrefixMapping prefixes,
            Node source, Node destination);
}
