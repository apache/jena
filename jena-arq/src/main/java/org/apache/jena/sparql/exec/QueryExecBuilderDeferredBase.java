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

package org.apache.jena.sparql.exec;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.util.Context;

/**
 * Deferred QueryExecBuilder that during build creates the target builder.
 * The settings of this builder are then transferred to the target builder.
 */
public abstract class QueryExecBuilderDeferredBase<X extends QueryExecBuilderDeferredBase<X>>
    extends QueryExecBuilderBase<X>
{
    /** This method must be implemented. */
    protected abstract QueryExecBuilder newActualExecBuilder(Context cxt);

    @Override
    public QueryExec build() {
        // Transfer context settings.
        // Because of QueryExecCompat we just transfer the built context:
        Context cxt = contextAcc.context();
        QueryExecBuilder qeb = newActualExecBuilder(cxt);
        qeb = applySettings(qeb);
        QueryExec qe = qeb.build();
        return qe;
    }

    /** Transfer settings from this builder to the destination. */
    protected QueryExecBuilder applySettings(QueryExecBuilder dest) {
        // Make sure to set parseCheck before setting the query string.
        applyParseCheck(dest, parseCheck);
        if (query != null) {
            applyQuery(dest, query);
        } else if (queryString != null) {
            applyQueryString(dest, queryString, syntax);
        }
        applySubstitutionMap(dest, substitutionMap);
        Timeout timeout = timeoutBuilder.build();
        applyTimeout(dest, timeout);
        applyExecTransforms(dest, queryExecTransforms);
        return dest;
    }

    protected void applyParseCheck(QueryExecBuilder dest, Boolean parseCheck) {
        if (parseCheck != null) {
            dest.parseCheck(parseCheck);
        }
    }

    protected void applyQuery(QueryExecBuilder dest, Query query) {
        dest.query(query);
    }

    protected void applyQueryString(QueryExecBuilder dest, String queryString, Syntax syntax) {
        dest.query(queryString, syntax);
    }

    protected void applySubstitutionMap(QueryExecBuilder dest, Map<Var, Node> substitutionMap) {
        if (substitutionMap != null) {
            for (Entry<Var, Node> e : substitutionMap.entrySet()) {
                dest = dest.substitution(e.getKey(), e.getValue());
            }
        }
    }

    protected void applyTimeout(QueryExecBuilder dest, Timeout timeout) {
        if (timeout.hasInitialTimeout()) {
            dest.initialTimeout(timeout.initialTimeout().amount(), timeout.initialTimeout().unit());
        }

        if (timeout.hasOverallTimeout()) {
             dest.overallTimeout(timeout.overallTimeout().amount(), timeout.overallTimeout().unit());
        }
    }

    protected void applyExecTransforms(QueryExecBuilder dest, List<QueryExecTransform> queryExecTransforms) {
        if (queryExecTransforms != null) {
            for (QueryExecTransform execTransform : queryExecTransforms) {
                dest = dest.transformExec(execTransform);
            }
        }
    }
}
