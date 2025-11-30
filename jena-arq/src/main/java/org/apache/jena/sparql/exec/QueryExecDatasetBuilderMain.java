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

import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;

/**
 * The main dataset-centric query exec builder of ARQ.
 * Chooses the appropriate query engine from the {@link QueryEngineRegistry}.
 */
public class QueryExecDatasetBuilderMain
    extends QueryExecBuilderBase<QueryExecDatasetBuilderMain>
    implements QueryExecDatasetBuilder
{
    static { JenaSystem.init(); }

    /** Create a new builder of {@link QueryExec} for a local dataset. */
    public static QueryExecDatasetBuilder create() { return new QueryExecDatasetBuilderMain(); }

    private QueryExecDatasetBuilderMain() {}

    @Override
    public QueryExecDatasetBuilderMain dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return thisBuilder();
    }

    /** Always parse queries regardless of the parse check hint. */
    @Override
    protected boolean effectiveParseCheck() {
        return true;
    }

    @Override
    public QueryExec build() {
        Objects.requireNonNull(query, "No query for QueryExec");
        // Queries can have FROM/FROM NAMED or VALUES to get data.
        //Objects.requireNonNull(dataset, "No dataset for QueryExec");
        query.ensureResultVars();
        Context cxt = getContext();

        QueryEngineFactory qeFactory = QueryEngineRegistry.findFactory(query, dataset, cxt);
        if ( qeFactory == null ) {
            Log.warn(QueryExecDatasetBuilder.class, "Failed to find a QueryEngineFactory");
            return null;
        }

        // Initial bindings / parameterized query
        Query queryActual = query;
        String queryStringActual = queryString;

        if ( substitutionMap != null && ! substitutionMap.isEmpty() ) {
            queryActual = QueryTransformOps.replaceVars(query, substitutionMap);
            queryStringActual = null;
        }

        Timeouts.applyDefaultQueryTimeoutFromContext(this.timeoutBuilder, cxt);

        if ( dataset != null )
            cxt.set(ARQConstants.sysCurrentDataset, DatasetFactory.wrap(dataset));
        if ( queryActual != null )
            cxt.set(ARQConstants.sysCurrentQuery, queryActual);

        Timeout timeout = timeoutBuilder.build();

        Binding initialBinding = null;
        QueryExec qExec = new QueryExecDataset(queryActual, queryStringActual, dataset, cxt, qeFactory,
                                               timeout, initialBinding);
        return qExec;
    }
}

