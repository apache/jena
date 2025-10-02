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

package org.apache.jena.rdflink.dataset;

import java.util.Optional;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispatch.ChainingQueryDispatcher;
import org.apache.jena.sparql.engine.dispatch.QueryDispatcher;
import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;

public class ChainingQueryDispatcherForDatasetGraphOverRDFLink
    implements ChainingQueryDispatcher {

    @Override
    public QueryExec create(Query query, DatasetGraph dsg, Binding initialBinding, Context context,
            QueryDispatcher chain) {
        QueryExec result = dsg instanceof DatasetGraphOverRDFLink d
            ? newQuery(d, initialBinding, context).query(query).build()
            : chain.create(query, dsg, initialBinding, context);
        return result;
    }

    @Override
    public QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding,
            Context context, QueryDispatcher chain) {
        QueryExec result = dsg instanceof DatasetGraphOverRDFLink d
                ? newQuery(d, initialBinding, context).query(queryString, syntax).build()
                : chain.create(queryString, syntax, dsg, initialBinding, context);
            return result;
    }

    private static QueryExecBuilder newQuery(DatasetGraphOverRDFLink d, Binding binding, Context requestCxt) {
        RDFLink link = d.newLink();
        try {
            QueryExecBuilder qeBuilder = link.newQuery().context(requestCxt);

            if (binding != null) {
                qeBuilder.substitution(binding);
            }

            Optional<Boolean> parseCheck = SparqlDispatcherRegistry.getParseCheck(requestCxt);
            if (parseCheck.isPresent()) {
                qeBuilder.parseCheck(parseCheck.get());
            }

            Timeout timeout = Timeouts.extractQueryTimeout(requestCxt);
            applyTimeouts(qeBuilder, timeout);

            return new QueryExecBuilderWrapperCloseRDFLink(qeBuilder, link);
        } catch (Throwable t) {
            link.close();
            t.addSuppressed(new RuntimeException("Failed to create query execution builder."));
            throw t;
        }
    }

    private static void applyTimeouts(QueryExecMod mod, Timeout t) {
        if (t != null) {
            if (t.hasInitialTimeout()) {
                mod.initialTimeout(t.initialTimeout().amount(), t.initialTimeout().unit());
            }
            if (t.hasOverallTimeout()) {
                mod.overallTimeout(t.overallTimeout().amount(), t.overallTimeout().unit());
            }
        }
    }
}
