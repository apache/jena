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

package org.apache.jena.sparql.engine;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

/**
 * A {@code QueryEngineFactory} builds query {@link Plan}s via
 * {@link #create(Query, DatasetGraph, Binding, Context)} or
 * {@link #create(Op, DatasetGraph, Binding, Context)}. A {@link Plan} has a
 * query iterator for the results of executing the {@link Op}, a SPARQL algebra
 * expression with local extensions.
 * <p>
 *  * A {@code QueryEngineFactory} is registered with the
 * {@link QueryEngineRegistry}.
 * <p>
 *  * When determining which factory to use, the querye execution process calls
 * {@link #accept(Query, DatasetGraph, Context)} or
 * {@link #accept(Op, DatasetGraph, Context)} to determine whether the
 * particular type of query engine produced by this factory accepts the
 * particular request. 
 * <p>
 * A QueryEngineFactory can be registered for use with
 * {@link QueryEngineRegistry#addFactory(QueryEngineFactory)} and
 * unregistered with
 * {@link QueryEngineRegistry#removeFactory(QueryEngineFactory)}.
 */
public interface QueryEngineFactory {
	/**
	 * Detect appropriate requests for a particular query engine for a particular graph type.
	 * 
	 * @param query
	 *            a {@link Query} to be executed
	 * @param dataset
	 *            the {@link DatasetGraph} over which the query is to be executed
	 * @param context
	 *            the {@link Context} in which the query is to be executed
	 * @return whether the kind of query engine produced by this factory can handle this task
	 */
	public boolean accept(Query query, DatasetGraph dataset, Context context);

	/**
	 *  Call to create a {@link Plan} : the companion {@link #accept} will have returned {@code true}.
	 * @param query
	 * @param dataset
	 * @param inputBinding
	 * @param context
	 */
	public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context);

	/**
	 * Detect appropriate requests for a particular query engine for a particular graph type.
	 * 
	 * @param op
	 *            an {@link Op} to be executed
	 * @param dataset
	 *            the {@link DatasetGraph} over which the operation is to be executed
	 * @param context
	 *            the {@link Context} in which the operation is to be executed
	 * @return whether the kind of query engine produced by this factory can handle this task
	 */
	public boolean accept(Op op, DatasetGraph dataset, Context context);

    /**
     *  Call to create a {@link Plan} : the companion {@link #accept} wil have returned {@code true}.
     * @param op
     * @param dataset
     * @param inputBinding
     * @param context
     */
	public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context);
}
