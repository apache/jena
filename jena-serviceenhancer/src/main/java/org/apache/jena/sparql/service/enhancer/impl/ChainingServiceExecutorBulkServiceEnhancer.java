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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExec;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;

public class ChainingServiceExecutorBulkServiceEnhancer
    implements ChainingServiceExecutorBulk
{
    @Override
    public QueryIterator createExecution(OpService opService, QueryIterator input, ExecutionContext execCxt,
            ServiceExecutorBulk chain) {

        // Don't interfere if service node is not an IRI
        Node node = opService.getService();
        if (node == null || !node.isURI()) {
            return chain.createExecution(opService, input, execCxt);
        }
        List<Entry<String, String>> opts = ServiceOpts.parseEntries(node);

        // The following variables will be updated based on the options
        boolean useLoop = false;

        boolean enableBulk = false;
        int bulkSize = 1;
        CacheMode requestedCacheMode = null;

        Context cxt = execCxt.getContext();
        int n = opts.size();
        int i = 0;

        String v;
        outer: for (; i < n; ++i) {
            Entry<String, String> opt = opts.get(i);
            String key = opt.getKey();
            String val = opt.getValue();

            switch (key) {
            case ServiceOptsSE.SO_LOOP:
                // Loop (lateral join) is handled on the algebra level
                // nothing to do here except for suppressing its forward
                // to the remainder of the chain
                useLoop = true;
                break;

            case ServiceOptsSE.SO_CACHE: // Enables caching
                v = val == null ? "" : val.toLowerCase();

                switch (v) {
                case "off": requestedCacheMode = CacheMode.OFF; break;
                case "clear": requestedCacheMode = CacheMode.CLEAR; break;
                default: requestedCacheMode = CacheMode.DEFAULT; break;
                }
                break;

            /*
            case ServiceOptsSE.SO_CONCURRENT:
                int maxConcurrentSlotCount = cxt.get(ServiceEnhancerConstants.serviceConcurrentMaxSlotCount, ChainingServiceExecutorBulkCache.DFT_MAX_CONCURRENT_SLOTS);
                // Value pattern is: [concurrentSlots][-maxReadaheadOfBindingsPerSlot]
                v = val == null ? "" : val.toLowerCase().trim();
                if (!v.isEmpty()) {
                    String[] parts = v.split("-", 2);
                    if (parts.length > 0) {
                        concurrentSlots = Integer.parseInt(parts[0]);
                        if (parts.length > 1) {
                            int maxReadaheadOfBindingsPerSlot = cxt.get(ServiceEnhancerConstants.serviceConcurrentMaxReadaheadCount, ChainingServiceExecutorBulkCache.DFT_MAX_CONCURRENT_READAHEAD);
                            readaheadOfBindingsPerSlot = Integer.parseInt(parts[1]);
                            readaheadOfBindingsPerSlot = Math.max(Math.min(readaheadOfBindingsPerSlot, maxReadaheadOfBindingsPerSlot), 0);
                        }
                    }
                } else {
                    concurrentSlots = Runtime.getRuntime().availableProcessors();
                }
                concurrentSlots = Math.max(Math.min(concurrentSlots, maxConcurrentSlotCount), 0);
                break;
            */
            case ServiceOptsSE.SO_BULK: // Enables bulk requests
                enableBulk = true;

                int maxBulkSize = cxt.get(ServiceEnhancerConstants.serviceBulkMaxBindingCount, ChainingServiceExecutorBulkCache.DFT_MAX_BULK_SIZE);
                bulkSize = cxt.get(ServiceEnhancerConstants.serviceBulkBindingCount, ChainingServiceExecutorBulkCache.DFT_CONCURRENT_READAHEAD);
                try {
                    if (val == null || val.isBlank()) {
                        // Ignored
                    } else {
                        bulkSize = Integer.parseInt(val);
                    }
                } catch (Exception e) {
                    throw new QueryExecException("Failed to configure bulk size", e);
                }
                bulkSize = Math.max(Math.min(bulkSize, maxBulkSize), 1);
                break;

            case "": // Skip over separator entries
                break;

            default:
                break outer;
            }
        }

        List<Entry<String, String>> subList = opts.subList(i, n);
//        String serviceStr = ServiceOpts.unparseEntries(subList);
//        OpService newOp = null;
//        if (serviceStr.isEmpty()) {
//            Op subOp = opService.getSubOp();
//            if (subOp instanceof OpService) {
//                newOp = (OpService)subOp;
//            } else {
//                serviceStr = ServiceEnhancerConstants.SELF.getURI();
//            }
//        }
//
//        if (newOp == null) {
//            node = NodeFactory.createURI(serviceStr);
//            newOp = new OpService(node, opService.getSubOp(), opService.getSilent());
//        }
        OpService newOp = toOpService(subList, opService, ServiceEnhancerConstants.SELF);

        QueryIterator result;
        CacheMode finalCacheMode = CacheMode.effectiveMode(requestedCacheMode);

        int concurrentSlots = 0; // FIXME Remove because concurrent is always disabled now; was factored out from here
        long readaheadOfBindingsPerSlot = ChainingServiceExecutorBulkCache.DFT_CONCURRENT_READAHEAD;

        boolean enableConcurrent = concurrentSlots > 0;
        boolean applySpecialProcessing =
            finalCacheMode != CacheMode.OFF ||
            enableBulk ||
            enableConcurrent;

        if (applySpecialProcessing) {
            ChainingServiceExecutorBulkCache exec = new ChainingServiceExecutorBulkCache(bulkSize, finalCacheMode, concurrentSlots, readaheadOfBindingsPerSlot);
            result = exec.createExecution(newOp, input, execCxt, ServiceExec::exec);
        } else if (useLoop) {
            // We don't need special bulk/cache processing, but we removed loop from the serviceIRI
            // So restart the chain
            result = ServiceExec.exec(newOp, input, execCxt);
        } else {
            result = chain.createExecution(newOp, input, execCxt);
        }
        return result;
    }

    public static OpService toOpService(List<Entry<String, String>> list, OpService originalOpService, Node fallbackServiceIri) {
        String serviceStr = ServiceOpts.unparseEntries(list);
        OpService newOp = null;
        if (serviceStr.isEmpty()) {
            Op subOp = originalOpService.getSubOp();
            if (subOp instanceof OpService subService) {
                newOp = subService;
            } else {
                serviceStr = fallbackServiceIri.getURI(); // ServiceEnhancerConstants.SELF.getURI();
            }
        }

        if (newOp == null) {
            Node node = NodeFactory.createURI(serviceStr);
            newOp = new OpService(node, originalOpService.getSubOp(), originalOpService.getSilent());
        }

        return newOp;
    }
}
