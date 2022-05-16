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

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

/**
 * Turns a ServiceExecutor into a chaining one.
 * Mainly used by {@link ServiceExecutorRegistry} for wrapping
 * non-chaining service executors.
 * If the executor returns null then the next link in the chain will be tried.
 */
public class ChainingServiceExecutorWrapper
    implements ChainingServiceExecutor
{
    protected ServiceExecutor executor;

    public ChainingServiceExecutorWrapper(ServiceExecutor executor) {
        super();
        this.executor = executor;
    }

    public ServiceExecutor getDelegate() {
        return executor;
    }

    @Override
    public QueryIterator createExecution(OpService opExecute, OpService opOriginal, Binding binding,
            ExecutionContext execCxt, ServiceExecutor chain) {

        QueryIterator qIter = executor.createExecution(opExecute, opOriginal, binding, execCxt);
        QueryIterator result = qIter != null
                ? qIter
                : chain.createExecution(opExecute, opOriginal, binding, execCxt);

        return result;
    }

}
