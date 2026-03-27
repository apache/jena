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

package org.apache.jena.sparql.exec.tracker.core;

import java.util.Optional;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.dispatch.ChainingQueryDispatcher;
import org.apache.jena.sparql.engine.dispatch.QueryDispatcher;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

public class ChainingQueryDispatcherExecTracker
    implements ChainingQueryDispatcher
{
    @Override
    public QueryExec create(Query query, DatasetGraph dataset, Context context, QueryDispatcher chain) {
        QueryExec delegateExec = chain.create(query, dataset, context);

        String queryStr = Optional.ofNullable(delegateExec.getQuery()).map(Object::toString)
                .orElse(delegateExec.getQueryString());
        ThrowableTracker throwableTracker = new ThrowableTrackerFirst();

        return new QueryExecTracked<>(delegateExec, throwableTracker) {
            @Override
            public void beforeExec(QueryType queryType) {
                System.out.println("Query execution started: " + queryStr);
                super.beforeExec(queryType);
            }
            @Override
            public void afterExec() {
                Optional<Throwable> t = this.getThrowableTracker().getFirstThrowable();
                if (t.isPresent()) {
                    System.out.println("Query execution completed WITH EXCEPTION: " + queryStr + " " + t.get());
                } else {
                    System.out.println("Query execution completed successfully: " + queryStr);
                }
            }
        };
    }
}
