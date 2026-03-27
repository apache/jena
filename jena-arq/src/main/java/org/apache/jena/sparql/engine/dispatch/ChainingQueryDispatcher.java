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

package org.apache.jena.sparql.engine.dispatch;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

/**
 * A query dispatcher is responsible for taking a query and
 * preparing it for the execution against a dataset.
 * The result is a {@linkplain QueryExec} instance.
 *
 * Query dispatchers form a chain, and a {@link ChainingQueryDispatcher} acts as a link in such a chain.
 * A ChainingQueryDispatcher instance can choose to process a query by itself or to delegate processing to the
 * remainder of the chain.
 *
 * @see QueryDispatcherRegistry
 */
public interface ChainingQueryDispatcher {
    QueryExec create(Query query, DatasetGraph dsg, Context context, QueryDispatcher chain);
}
