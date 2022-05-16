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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterConvert;
import org.apache.jena.sparql.engine.iterator.QueryIterPeek;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.service.enhancer.impl.util.BindingUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.QueryIterSlottedBase;
import org.apache.jena.sparql.service.enhancer.impl.util.VarUtilsExtra;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;

/**
 * Prepare and execute bulk requests
 */
public class RequestExecutor
    extends QueryIterSlottedBase
{
    protected OpServiceInfo serviceInfo;

    /**  Ensure that at least there are active requests to serve the next n input bindings */
    protected int fetchAhead = 5;
    protected int maxRequestSize = 2000;

    protected OpServiceExecutor opExecutor;
    protected ExecutionContext execCxt;
    protected ServiceResultSizeCache resultSizeCache;
    protected ServiceResponseCache cache;
    protected CacheMode cacheMode;

    protected IteratorCloseable<GroupedBatch<Node, Long, Binding>> batchIterator;
    protected Var globalIdxVar;

    // Input iteration
    protected long currentInputId = -1;
    protected QueryIterPeek activeIter;

    protected Map<Long, Binding> inputToBinding = new HashMap<>();
    protected Map<Long, QueryIterPeek> inputToOutputIt = new LinkedHashMap<>();
    protected Set<Long> inputToClose = new HashSet<>(); // Whether an iterator can be closed once the input is processed

    public RequestExecutor(
            OpServiceExecutorImpl opExector,
            // boolean useLoopJoin,
            OpServiceInfo serviceInfo,
            ServiceResultSizeCache resultSizeCache,
            ServiceResponseCache cache,
            CacheMode cacheMode,
            IteratorCloseable<GroupedBatch<Node, Long, Binding>> batchIterator) {
        this.opExecutor = opExector;
        // this.useLoopJoin = useLoopJoin;
        this.serviceInfo = serviceInfo;
        this.resultSizeCache = resultSizeCache;
        this.cache = cache;
        this.cacheMode = cacheMode;
        this.batchIterator = batchIterator;

        // Allocate a fresh index var - services may be nested which results in
        // multiple injections of an idxVar which need to be kept separate
        Set<Var> visibleServiceSubOpVars = serviceInfo.getVisibleSubOpVarsScoped();
        this.globalIdxVar = VarUtilsExtra.freshVar("__idx__", visibleServiceSubOpVars);
        this.execCxt = opExector.getExecCxt();
        this.activeIter = QueryIterPeek.create(QueryIterPlainWrapper.create(Collections.<Binding>emptyList().iterator(), execCxt), execCxt);
    }

    @Override
    protected Binding moveToNext() {

        Binding parentBinding = null;
        Binding childBindingWithIdx = null;

        // Peek the next binding on the active iterator and verify that it maps to the current
        // partition key
        while (true) {
          if (activeIter.hasNext()) {
              Binding peek = activeIter.peek();
              long peekOutputId = BindingUtils.getNumber(peek, globalIdxVar).longValue();

              boolean matchesCurrentPartition = peekOutputId == currentInputId;

              if (matchesCurrentPartition) {
                  parentBinding = inputToBinding.get(currentInputId);
                  childBindingWithIdx = activeIter.next();
                  break;
              }
          }

          // Cleanup of no longer needed resources
          boolean isClosePoint = inputToClose.contains(currentInputId);
          if (isClosePoint) {
              QueryIterPeek it = inputToOutputIt.get(currentInputId);
              it.close();
              inputToClose.remove(currentInputId);
          }

          inputToBinding.remove(currentInputId);

          // Increment rangeId/inputId until we reach the end
          ++currentInputId;

          // Check if we need to load the next batch
          // If there are missing (=non-loaded) rows within the read ahead range then load them
          if (!inputToOutputIt.containsKey(currentInputId)) {
              if (batchIterator.hasNext()) {
                  prepareNextBatchExec();
              }
          }

          // If there is still no further batch then we assume we reached the end
          if (!inputToOutputIt.containsKey(currentInputId)) {
              break;
          }

          activeIter = inputToOutputIt.get(currentInputId);
      }

      // Remove the idxVar from the childBinding
      Binding result = null;
      if (childBindingWithIdx != null) {
          Binding childBinding = BindingUtils.project(childBindingWithIdx, childBindingWithIdx.vars(), globalIdxVar);
          result = BindingFactory.builder(parentBinding).addAll(childBinding).build();
      }

      if (result == null) {
          freeResources();
      }

      return result;
    }

    /** Prepare the lazy execution of the next batch and register all iterators with {@link #inputToOutputIt} */
    // seqId = sequential number injected into the request
    // inputId = id (index) of the input binding
    // rangeId = id of the range w.r.t. to the input binding
    // partitionKey = (inputId, rangeId)
    public void prepareNextBatchExec() {

        GroupedBatch<Node, Long, Binding> batchRequest = batchIterator.next();

        // TODO Support ServiceOpts from Node directly
        ServiceOpts so = ServiceOpts.getEffectiveService(serviceInfo.getOpService());

        Node targetServiceNode = so.getTargetService().getService();

        // Refine the request w.r.t. the cache
        Batch<Long, Binding> batch = batchRequest.getBatch();

        // This block sets up the execution of the batch
        // For aesthetics, bindings are re-numbered starting with 0 when creating the backend request
        // These ids are subsequently mapped back to the offset of the input iterator
        {
            NavigableMap<Long, Binding> batchItems = batch.getItems();

            List<Binding> inputs = new ArrayList<>(batchItems.values());

            NodeTransform serviceNodeRemapper = node -> ServiceEnhancerInit.resolveServiceNode(node, execCxt);

            Set<Var> inputVarsMentioned = BindingUtils.varsMentioned(inputs);
            ServiceCacheKeyFactory cacheKeyFactory = ServiceCacheKeyFactory.createCacheKeyFactory(serviceInfo, inputVarsMentioned, serviceNodeRemapper);

            Set<Var> visibleServiceSubOpVars = serviceInfo.getVisibleSubOpVarsScoped();
            Var batchIdxVar = VarUtilsExtra.freshVar("__idx__", visibleServiceSubOpVars);

            BatchQueryRewriterBuilder builder = BatchQueryRewriterBuilder.from(serviceInfo, batchIdxVar);

            if (ServiceEnhancerConstants.SELF.equals(targetServiceNode)) {
                builder.setOrderRetainingUnion(true)
                    .setSequentialUnion(true);
            }

            BatchQueryRewriter rewriter = builder.build();

            QueryIterServiceBulk baseIt = new QueryIterServiceBulk(
                    serviceInfo, rewriter, cacheKeyFactory, opExecutor, execCxt, inputs,
                    resultSizeCache, cache, cacheMode);

            QueryIterator tmp = baseIt;

            // Remap the local input id of the batch to the global one here
            Var innerIdxVar = baseIt.getIdxVar();
            List<Long> reverseMap = new ArrayList<>(batchItems.keySet());

            tmp = new QueryIterConvert(baseIt, b -> {
                int localId = BindingUtils.getNumber(b, innerIdxVar).intValue();
                long globalId = reverseMap.get(localId);

                Binding q = BindingUtils.project(b, b.vars(), innerIdxVar);
                Binding r = BindingFactory.binding(q, globalIdxVar, NodeValue.makeInteger(globalId).asNode());

                return r;
            }, execCxt);


            QueryIterPeek queryIter = QueryIterPeek.create(tmp, execCxt);
            // Register the iterator with the input ids
            // for (int i = 0; i < batchItems.size(); ++i) {
            for (Long e : batchItems.keySet()) {
                inputToOutputIt.put(e, queryIter);
            }

            long lastKey = batch.getItems().lastKey();
            inputToClose.add(lastKey);
        }
    }

    protected void freeResources() {
        for (long inputId  : inputToClose) {
            Closeable closable = inputToOutputIt.get(inputId);
            closable.close();
        }
        batchIterator.close();
    }

    @Override
    protected void closeIterator() {
        freeResources();
        super.closeIterator();
    }
}

