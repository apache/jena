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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Optional;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;

/** Do not register directly - use {@link ChainingServiceExecutorBulkServiceEnhancer} which gives more control over
 * when to use this in a service executor chain */
public class ChainingServiceExecutorBulkCache
    implements ChainingServiceExecutorBulk {

    public static final int DEFAULT_BULK_SIZE = 30;
    public static final int MAX_BULK_SIZE = 100;
    public static final int DEFAULT_MAX_BYTE_SIZE = 5000;

    protected int bulkSize;
    protected CacheMode cacheMode;

    public ChainingServiceExecutorBulkCache(int bulkSize, CacheMode cacheMode) {
        super();
        this.cacheMode = cacheMode;
        this.bulkSize = bulkSize;
    }

    @Override
    public QueryIterator createExecution(OpService original, QueryIterator input, ExecutionContext execCxt,
            ServiceExecutorBulk chain) {

        Context cxt = execCxt.getContext();
        // int bulkSize = cxt.getInt(InitServiceEnhancer.serviceBulkMaxBindingCount, DEFAULT_BULK_SIZE);
        ServiceResponseCache serviceCache = CacheMode.OFF.equals(cacheMode)
                ? null
                : ServiceResponseCache.get(cxt);

        OpServiceInfo serviceInfo = new OpServiceInfo(original);

        ServiceResultSizeCache resultSizeCache = Optional.ofNullable(cxt.<ServiceResultSizeCache>
                get(ServiceEnhancerConstants.serviceResultSizeCache))
                .orElseGet(ServiceResultSizeCache::new);

        OpServiceExecutorImpl opExecutor = new OpServiceExecutorImpl(serviceInfo.getOpService(), execCxt, chain);

        RequestScheduler<Node, Binding> scheduler = new RequestScheduler<>(serviceInfo::getSubstServiceNode, bulkSize);
        IteratorCloseable<GroupedBatch<Node, Long, Binding>> inputBatchIterator = scheduler.group(input);

        RequestExecutor exec = new RequestExecutor(opExecutor, serviceInfo, resultSizeCache, serviceCache, cacheMode, inputBatchIterator);

        return exec;
    }
}
