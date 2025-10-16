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

package org.apache.jena.sparql.engine.dispatch;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

/**
 * A query dispatcher is responsible for taking a query and routing it to the
 * appropriate component or system for preparing the execution against a dataset.
 * The result is a {@linkplain QueryExec} instance.
 * Queries may be passed to dispatchers in syntactic or string representation.
 *
 * Query dispatchers form a chain, and a ChainingQueryDispatcher acts as a link in such a chain.
 * A ChainingQueryDispatcher instance can choose to process a query by itself or to delegate processing to the
 * remainder of the chain.
 *
 * @see SparqlDispatcherRegistry
 */
public interface ChainingQueryDispatcher {
    QueryExec create(Query query, DatasetGraph dsg, Binding initialBinding, Context context, QueryDispatcher chain);
    QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context, QueryDispatcher chain);
}
