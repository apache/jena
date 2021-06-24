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

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphSink;
import org.apache.jena.sparql.core.Quad;

/** A {@link SecurityContext} that does not allow any access. */
public class SecurityContextAllowNone implements SecurityContext {

    public SecurityContextAllowNone() {}

    @Override
    public Collection<Node> visibleGraphs() {
        return Collections.emptyList();
    }

    @Override
    public boolean visableDefaultGraph() { return false; }

    @Override
    public QueryExecution createQueryExecution(Query query, DatasetGraph dsg) {
        return QueryExecutionFactory.create(query, DatasetGraphSink.create());
    }

    @Override
    public Predicate<Quad> predicateQuad() { return q -> false; }

    @Override
    public void filterTDB(DatasetGraph dsg, QueryExecution qExec) {
        Predicate<?> pred = tuple->false;
        qExec.getContext().set(GraphFilter.getContextKey(dsg), pred);
    }
}
