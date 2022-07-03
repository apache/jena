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

import java.util.List;

import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.apache.jena.sparql.service.single.ServiceExecutorOverRegistry;

/**
 * Factory for service executions w.r.t. a {@link ServiceExecutorRegistry}.
 * The {@link #createExecution(OpService, QueryIterator, ExecutionContext)} method
 * delegates the request to all executors in order of their registration.
 */
public class ServiceExecutorBulkOverRegistry
    implements ServiceExecutorBulk
{
    protected ServiceExecutorRegistry registry;

    /** Position in the chain */
    protected int pos;

    public ServiceExecutorBulkOverRegistry(ServiceExecutorRegistry registry) {
        this(registry, 0);
    }

    public ServiceExecutorBulkOverRegistry(ServiceExecutorRegistry registry, int pos) {
        super();
        this.registry = registry;
        this.pos = pos;
    }

    @Override
    public QueryIterator createExecution(OpService opService, QueryIterator input, ExecutionContext execCxt) {
        if (registry == null) {
            throw new QueryException("No service executor registry configured");
        }

        QueryIterator result;

        List<ChainingServiceExecutorBulk> factories = registry.getBulkChain();
        int n = factories.size();
        if (pos >= n) {
            // Chain to the single registry
            ServiceExecutor singleExecutor = new ServiceExecutorOverRegistry(registry);
            ServiceExecutorBulk bridge = new ServiceExecutorBulkToSingle(singleExecutor);
            result = bridge.createExecution(opService, input, execCxt);

            // Alternatively we could require for the bridge to be explicitly registered
            // throw new QueryException("No more elements in service executor chain (pos=" + pos + ", chain size=" + n + ")");
        } else {
            ChainingServiceExecutorBulk factory = factories.get(pos);
            ServiceExecutorBulk next = new ServiceExecutorBulkOverRegistry(registry, pos + 1);
            result = factory.createExecution(opService, input, execCxt, next);
        }
        return result;
    }
}
