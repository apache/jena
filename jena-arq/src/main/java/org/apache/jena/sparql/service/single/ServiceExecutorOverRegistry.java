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

package org.apache.jena.sparql.service.single;

import java.util.List;

import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

/** Abstraction of a registry's single chain as a service executor */
public class ServiceExecutorOverRegistry
    implements ServiceExecutor
{
    protected ServiceExecutorRegistry registry;

    /** Position in the chain */
    protected int pos;

    public ServiceExecutorOverRegistry(ServiceExecutorRegistry registry) {
        this(registry, 0);
    }

    public ServiceExecutorOverRegistry(ServiceExecutorRegistry registry, int pos) {
        super();
        this.registry = registry;
        this.pos = pos;
    }

    @Override
    public QueryIterator createExecution(OpService opExecute, OpService original, Binding binding, ExecutionContext execCxt) {
        List<ChainingServiceExecutor> factories = registry.getSingleChain();
        int n = factories.size();
        if (pos >= n) {
            if (opExecute.getSilent()) {
                return QueryIterRoot.create(execCxt);
            } else {
                throw new QueryException("No more elements in service executor chain (pos=" + pos + ", chain size=" + n + ")");
            }
        }

        ChainingServiceExecutor factory = factories.get(pos);

        ServiceExecutor next = new ServiceExecutorOverRegistry(registry, pos + 1);
        QueryIterator result = factory.createExecution(opExecute, original, binding, execCxt, next);

        return result;
    }
}
