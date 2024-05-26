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

package org.apache.jena.sparql.service;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulkOverRegistry;
import org.apache.jena.sparql.util.Context;

/**
 * Entry into the service executor from SPARQL queries.
 */
public class ServiceExec {
    /**
     * Execute an OpService w.r.t. the execCxt's service executor registry.
     * This is the route from OpExecutor.
     */
    public static QueryIterator exec(QueryIterator input, OpService opService, ExecutionContext execCxt) {
        Context cxt = execCxt.getContext();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.chooseRegistry(cxt);
        ServiceExecutorBulk serviceExecutor = new ServiceExecutorBulkOverRegistry(registry);
        QueryIterator qIter = serviceExecutor.createExecution(opService, input, execCxt);
        return qIter;
    }
}
