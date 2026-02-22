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

package org.apache.jena.sparql.exec;

import java.util.Map.Entry;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;
import org.apache.jena.sparql.util.Context;

/**
 * Deferred QueryExecBuilder that during build creates the target builder.
 * The settings of this builder are then transferred to the target builder.
 */
public abstract class QueryExecDatasetBuilderDeferredBase<X extends QueryExecDatasetBuilderDeferredBase<X>>
    extends QueryExecDatasetBuilderBase<X>
{
    protected QueryExecDatasetBuilderDeferredBase<X> graph(Graph graph) {
        DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        dataset(dsg);
        return thisBuilder();
    }

    /** This method must be implemented. */
    protected abstract QueryExecBuilder newActualExecBuilder();

    @Override
    public QueryExec build() {
        QueryExecBuilder qeb = newActualExecBuilder();
        qeb = applySettings(qeb);
        QueryExec qe = qeb.build();
        return qe;
    }

    /** Transfer settings from this builder to to the destination. */
    protected QueryExecBuilder applySettings(QueryExecBuilder dest) {
        // Make sure to set parseCheck before setting the query string.
        if (parseCheck != null) {
            dest = dest.parseCheck(parseCheck);
        }

        if (query != null) {
            dest = dest.query(query);
        } else if (queryString != null) {
            dest = dest.query(queryString, syntax);
        }

        // Transfer context settings.
        // Because of QueryExecCompat we just transfer the built context:
        Context cxt = contextAcc.context();
        dest = dest.context(cxt);

        if (substitutionMap != null) {
            for (Entry<Var, Node> e : substitutionMap.entrySet()) {
                dest = dest.substitution(e.getKey(), e.getValue());
            }
        }

        Timeout timeout = timeoutBuilder.build();
        if (timeout.hasInitialTimeout()) {
            dest = dest.initialTimeout(timeout.initialTimeout().amount(), timeout.initialTimeout().unit());
        }

        if (timeout.hasOverallTimeout()) {
            dest = dest.overallTimeout(timeout.overallTimeout().amount(), timeout.overallTimeout().unit());
        }

        for (QueryExecTransform execTransform : queryExecTransforms) {
            dest = dest.transformExec(execTransform);
        }

        return dest;
    }
}
