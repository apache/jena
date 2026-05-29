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

import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterConvert;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.service.enhancer.impl.util.BindingUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.VarUtilsExtra;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterator;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterators;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;

/**
 * This class is an iterator that handles partitioning bulk requests and caching.
 * The class {@link QueryIterServiceBulkAndCache} is responsible to process a concrete batch.
 */
public class RequestExecutorBulkAndCache
    extends RequestExecutorSparqlBase
{
    protected OpServiceExecutor opExecutor;
    protected OpServiceInfo serviceInfo;

    protected ServiceResultSizeCache resultSizeCache;
    protected ServiceResponseCache cache;
    protected CacheMode cacheMode;

    protected Var globalIdxVar;

    public RequestExecutorBulkAndCache(
            AbortableIterator<GroupedBatch<Node, Long, Binding>> batchIterator,
            int maxConcurrentTasks,
            long concurrentSlotReadAheadCount,
            ExecutionContext execCxt,
            OpServiceExecutorImpl opExector,
            OpServiceInfo serviceInfo,
            ServiceResultSizeCache resultSizeCache,
            ServiceResponseCache cache,
            CacheMode cacheMode) {
        super(Granularity.ITEM, batchIterator, maxConcurrentTasks, concurrentSlotReadAheadCount, execCxt);
        this.opExecutor = opExector;
        this.serviceInfo = serviceInfo;
        this.resultSizeCache = resultSizeCache;
        this.cache = cache;
        this.cacheMode = cacheMode;

        // Allocate a fresh index var - services may be nested which results in
        // multiple injections of an idxVar which needs to be kept separate
        Set<Var> visibleServiceSubOpVars = serviceInfo.getVisibleSubOpVarsScoped();
        this.globalIdxVar = VarUtilsExtra.freshVar("__idx__", visibleServiceSubOpVars);
    }

    @Override
    public AbortableIterator<Binding> buildIterator(boolean runsOnNewThread, Node groupKey, List<Binding> inputs, List<Long> reverseMap, ExecutionContext batchExecCxt) {
        ServiceOpts so = ServiceOptsSE.getEffectiveService(serviceInfo.getOpService());
        Node targetServiceNode = so.getTargetService().getService();

        NodeTransform serviceNodeRemapper = node -> ServiceEnhancerInit.resolveServiceNode(node, batchExecCxt);

        Set<Var> inputVarsMentioned = BindingUtils.varsMentioned(inputs);
        ServiceCacheKeyFactory cacheKeyFactory = ServiceCacheKeyFactory.createCacheKeyFactory(serviceInfo, inputVarsMentioned, serviceNodeRemapper);

        BatchQueryRewriterBuilder builder = BatchQueryRewriterBuilder.from(serviceInfo, globalIdxVar);

        if (ServiceEnhancerConstants.SELF.equals(targetServiceNode)) {
            builder
                .setOrderRetainingUnion(true)
                .setSequentialUnion(true);
        }

        BatchQueryRewriter rewriter = builder.build();

        QueryIterServiceBulkAndCache baseIt = new QueryIterServiceBulkAndCache(
                serviceInfo, rewriter, cacheKeyFactory, opExecutor, batchExecCxt, inputs,
                resultSizeCache, cache, cacheMode);

        QueryIterator tmp = baseIt;

        // Remap the local input id of the batch to the global one here
        Var innerIdxVar = baseIt.getIdxVar();

        tmp = new QueryIterConvert(baseIt, b -> {
            int localId = BindingUtils.getNumber(b, innerIdxVar).intValue();
            long globalId = reverseMap.get(localId);

            Binding q = BindingUtils.project(b, b.vars(), innerIdxVar);
            Binding r = BindingFactory.binding(q, globalIdxVar, NodeValue.makeInteger(globalId).asNode());

            return r;
        }, batchExecCxt);

        return AbortableIterators.adapt(tmp);
    }

    @Override
    protected long extractInputOrdinal(Binding input) {
        // Even if the binding is otherwise empty the ID for globalIdxVar must never be null!
        long result = BindingUtils.getNumber(input, globalIdxVar).longValue();
        return result;
    }

    /** Extend super.moveToNext to exclude the internal globalIdxVar from the bindings. */
    @Override
    protected Binding moveToNext() {
        Binding tmp = super.moveToNext();
        Binding result = tmp == null ? null : BindingUtils.project(tmp, tmp.vars(), globalIdxVar);
        return result;
    }
}
