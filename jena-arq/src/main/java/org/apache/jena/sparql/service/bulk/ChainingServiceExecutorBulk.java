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

package org.apache.jena.sparql.service.bulk;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;

/** Interface for custom service execution extensions that handle
 *  the iterator over the input bindings themselves */
public interface ChainingServiceExecutorBulk {
    /**
     * If this executor cannot handle the createExecution request then it should delegate
     * to the chain's @{code createExecution} method and return its result.
     * In any case, a {@link QueryIterator} needs to be returned.
     *
     * @return A non-null {@link QueryIterator} for the execution of the given OpService expression.
     */
    public QueryIterator createExecution(OpService opService, QueryIterator input, ExecutionContext execCxt, ServiceExecutorBulk chain);
}
