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

import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
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

        QueryIterator result;
        Node node = opService.getService();
        List<Entry<String, String>> opts = ServiceOpts.parseAsOptions(node);

        boolean enableBulk = false;

        int bulkSize = 1;

        CacheMode cacheMode = null;
        Context cxt = execCxt.getContext();
        int n = opts.size();
        int i = 0;
        outer: for (; i < n; ++i) {
            Entry<String, String> opt = opts.get(i);
            String key = opt.getKey();
            String val = opt.getValue();

            switch (key) {
            case ServiceOpts.SO_LOOP:
                // Loop (lateral join) is handled on the algebra level
                // nothing to do here except for suppressing forward to
                // to the remainder of the chain
                break;
            case ServiceOpts.SO_CACHE: // Enables caching
                String v = val == null ? "" : val.toLowerCase();

                switch (v) {
                case "off": cacheMode = CacheMode.OFF; break;
                case "clear": cacheMode = CacheMode.CLEAR; break;
                default: cacheMode = CacheMode.DEFAULT; break;
                }

                break;
            case ServiceOpts.SO_BULK: // Enables bulk requests
                enableBulk = true;

                int maxBulkSize = cxt.get(ServiceEnhancerConstants.serviceBulkMaxBindingCount, ChainingServiceExecutorBulkCache.MAX_BULK_SIZE);
                bulkSize = cxt.get(ServiceEnhancerConstants.serviceBulkBindingCount, ChainingServiceExecutorBulkCache.DEFAULT_BULK_SIZE);
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
            default:
                break outer;
            }
        }

        List<Entry<String, String>> subList = opts.subList(i, n);
        String serviceStr = ServiceOpts.unparse(subList);
        OpService newOp = null;
        if (serviceStr.isEmpty()) {
            Op subOp = opService.getSubOp();
            if (subOp instanceof OpService) {
                newOp = (OpService)subOp;
            } else {
                serviceStr = ServiceEnhancerConstants.SELF.getURI();
            }
        }

        if (newOp == null) {
            node = NodeFactory.createURI(serviceStr);
            newOp = new OpService(node, opService.getSubOp(), opService.getSilent());
        }

        CacheMode effCacheMode = CacheMode.effectiveMode(cacheMode);

        boolean enableSpecial = effCacheMode != CacheMode.OFF || enableBulk; // || enableLoopJoin; // || !overrides.isEmpty();

        if (enableSpecial) {
            ChainingServiceExecutorBulkCache exec = new ChainingServiceExecutorBulkCache(bulkSize, effCacheMode);
            result = exec.createExecution(newOp, input, execCxt, chain);
        } else {
            result = chain.createExecution(newOp, input, execCxt);
        }

        return result;
    }
}
