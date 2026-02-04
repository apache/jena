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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterator;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterators;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.QueryIterOverAbortableIterator;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;

/**
 * Do not register this class directly in a service executor chain.
 * Instead, register {@link ChainingServiceExecutorBulkServiceEnhancer} which
 * internally creates appropriately configured instances of this class
 * during query execution.
 */
public class ChainingServiceExecutorBulkCache
    implements ChainingServiceExecutorBulk {

    public static final int DFT_BULK_SIZE = 30;
    public static final int DFT_MAX_BULK_SIZE = 100;
    public static final int DFT_MAX_OUT_OUF_BAND_SIZE = 30;

    public static final int DFT_MAX_CONCURRENT_SLOTS = 100;

    public static final int DFT_CONCURRENT_READAHEAD = 10000;
    public static final int DFT_MAX_CONCURRENT_READAHEAD = 10000;

    protected int bulkSize;
    protected CacheMode cacheMode;

    protected int concurrentSlotCount;
    protected long concurrentSlotReadaheadCount;

    public ChainingServiceExecutorBulkCache(int bulkSize, CacheMode cacheMode, int concurrentSlotCount, long concurrentSlotReadAheadCount) {
        super();
        this.cacheMode = cacheMode;
        this.bulkSize = bulkSize;
        this.concurrentSlotCount = concurrentSlotCount;
        this.concurrentSlotReadaheadCount = concurrentSlotReadAheadCount;
    }

    @Override
    public QueryIterator createExecution(OpService original, QueryIterator input, ExecutionContext execCxt, ServiceExecutorBulk chain) {
        Context cxt = execCxt.getContext();

        ServiceResponseCache serviceCache = CacheMode.OFF.equals(cacheMode)
            ? null
            : ServiceResponseCache.get(cxt);

        OpServiceInfo serviceInfo = new OpServiceInfo(original);

        ServiceResultSizeCache resultSizeCache = Optional.ofNullable(cxt.<ServiceResultSizeCache>get(ServiceEnhancerConstants.serviceResultSizeCache))
            .orElseGet(ServiceResultSizeCache::new);

        OpServiceExecutorImpl opExecutor = new OpServiceExecutorImpl(serviceInfo.getOpService(), chain);

        int maxOutOfBandItemCount = cxt.getInt(ServiceEnhancerConstants.serviceBulkMaxOutOfBandBindingCount, DFT_MAX_OUT_OUF_BAND_SIZE);
        Batcher<Node, Binding> scheduler = new Batcher<>(serviceInfo::getSubstServiceNode, bulkSize, maxOutOfBandItemCount);
        AbortableIterator<GroupedBatch<Node, Long, Binding>> inputBatchIterator = scheduler.batch(AbortableIterators.adapt(input));

        RequestExecutorBulkAndCache exec = new RequestExecutorBulkAndCache(inputBatchIterator, concurrentSlotCount, concurrentSlotReadaheadCount, execCxt, opExecutor, serviceInfo, resultSizeCache, serviceCache, cacheMode);
        return new QueryIterOverAbortableIterator(execCxt, exec);
    }
}
