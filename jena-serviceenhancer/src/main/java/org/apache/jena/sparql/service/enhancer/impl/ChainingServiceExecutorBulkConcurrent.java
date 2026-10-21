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
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.service.ServiceExec;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.enhancer.impl.RequestExecutorBase.Granularity;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterator;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterators;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;

public class ChainingServiceExecutorBulkConcurrent
    implements ChainingServiceExecutorBulk
{
    public static final String OPTION_NAME = "concurrent";

    public record Config(int concurrentSlots, int bindingsPerSlot, long readAhead) {}

    private final String name;

    /** Create a service executor that performs concurrent execution, configurable via the option {@value #OPTION_NAME}.
     *  For example SERVICE &lt;concurrent:&gt; { }.*/
    public ChainingServiceExecutorBulkConcurrent() {
        this(OPTION_NAME);
    }

    public ChainingServiceExecutorBulkConcurrent(String optionName) {
        super();
        this.name = optionName;
    }

    @Override
    public QueryIterator createExecution(OpService opService, QueryIterator input, ExecutionContext execCxt, ServiceExecutorBulk chain) {
//        ServiceOpts opts = ServiceOpts.getEffectiveService(opService, ServiceEnhancerConstants.SELF.getURI(),
//                key -> key.equals(name));
        List<Entry<String, String>> list = ServiceOpts.parseEntries(opService.getService());

        QueryIterator result;
        Entry<String, String> opt = list.isEmpty() ? null : list.get(0);
        if (opt != null && opt.getKey().equals(name)) {
            list = list.subList(1, list.size());
            // Remove a trailing colon separator
            // FIXME: This should be handled more elegantly
            if (!list.isEmpty() && list.get(0).getKey().equals("")) {
                list = list.subList(1, list.size());
            }

            Context cxt = execCxt.getContext();
            // String key = opt.getKey();
            String val = opt.getValue();
            Config config = parseConfig(val, cxt);

            OpService newOp = ChainingServiceExecutorBulkServiceEnhancer.toOpService(list, opService, ServiceEnhancerConstants.SELF_BULK);

            // OpServiceInfo serviceInfo = new OpServiceInfo(opService);
            Node serviceNode = opService.getService();
            // OpServiceInfo serviceInfo = new OpServiceInfo(newOp);
            Function<Binding, Node> groupKeyFn = binding -> Var.lookup(binding, serviceNode);
            // Function<Binding, Node> groupKeyFn = serviceInfo::getSubstServiceNode;

            Batcher<Node, Binding> scheduler = new Batcher<>(groupKeyFn, config.bindingsPerSlot(), 0);
            AbortableIterator<GroupedBatch<Node, Long, Binding>> inputBatchIterator = scheduler.batch(AbortableIterators.adapt(input));

            RequestExecutorSparqlBase exec = new RequestExecutorSparqlBase(Granularity.BATCH, inputBatchIterator, config.concurrentSlots(), config.readAhead(), execCxt) {
                @Override
                protected AbortableIterator<Binding> buildIterator(boolean runsOnNewThread, Node groupKey, List<Binding> inputs, List<Long> reverseMap, ExecutionContext batchExecCxt) {
//                    Iterator<Binding> indexedBindings = IntStream.range(0, inputs.size()).mapToObj(i ->
//                        BindingFactory.binding(inputs.get(i), globalIdxVar, NodeValue.makeInteger(reverseMap.get(i)).asNode()))
//                        .iterator();

                    QueryIterator subIter = QueryIterPlainWrapper.create(inputs.iterator(), batchExecCxt);

                    // QueryIterator tmp = chain.createExecution(newOp, QueryIterPlainWrapper.create(indexedBindings, execCxt), execCxt);
                    // Pass the adapted request through the whole service executor chain again.
                    QueryIterator tmp = ServiceExec.exec(newOp, subIter, batchExecCxt);
                    return AbortableIterators.adapt(tmp);
                }

                @Override
                protected long extractInputOrdinal(Binding targetItem) {
                    // This iterator operates on batch granularity
                    // No need to relate individual bindings to their ordinal.
                    throw new IllegalStateException("Should never be called.");
                }
            };
            result = AbortableIterators.asQueryIterator(exec);
        } else {
            result = chain.createExecution(opService, input, execCxt);
        }
        return result;
    }

    /** Parse the settings of format [concurrentSlots[-maxBindingsPerSlot[-maxReadaheadOfBindingsPerSlot]]]. */
    public static Config parseConfig(String val, Context cxt) {
        int concurrentSlots = 0;
        long readaheadOfBindingsPerSlot = ChainingServiceExecutorBulkCache.DFT_CONCURRENT_READAHEAD;

        int maxConcurrentSlotCount = cxt.get(ServiceEnhancerConstants.serviceConcurrentMaxSlotCount, ChainingServiceExecutorBulkCache.DFT_MAX_CONCURRENT_SLOTS);

        String v = val == null ? "" : val.toLowerCase().trim();
        int bindingsPerSlot = 1;

        // [{concurrentSlotCount}[-{bindingsPerSlotCount}[-{readAheadPerSlotCount}]]]
        if (!v.isEmpty()) {
            String[] parts = v.split("-", 3);
            if (parts.length > 0) {
                concurrentSlots = parseInt(parts[0], 0);
                if (parts.length > 1) {
                    bindingsPerSlot = parseInt(parts[1], 0);
                    // There must be at least 1 binding per slot
                    bindingsPerSlot = Math.max(1, bindingsPerSlot);
                    if (parts.length > 2) {
                        int maxReadaheadOfBindingsPerSlot = cxt.get(ServiceEnhancerConstants.serviceConcurrentMaxReadaheadCount, ChainingServiceExecutorBulkCache.DFT_MAX_CONCURRENT_READAHEAD);
                        readaheadOfBindingsPerSlot = parseInt(parts[2], 0);
                        readaheadOfBindingsPerSlot = Math.max(Math.min(readaheadOfBindingsPerSlot, maxReadaheadOfBindingsPerSlot), 0);
                    }
                }
            }
        } else {
            concurrentSlots = Runtime.getRuntime().availableProcessors();
        }
        concurrentSlots = Math.max(Math.min(concurrentSlots, maxConcurrentSlotCount), 0);

        return new Config(concurrentSlots, bindingsPerSlot, readaheadOfBindingsPerSlot);
    }

    private static int parseInt(String str, int fallbackValue) {
        return str.isEmpty() ? fallbackValue : Integer.parseInt(str);
    }
}
