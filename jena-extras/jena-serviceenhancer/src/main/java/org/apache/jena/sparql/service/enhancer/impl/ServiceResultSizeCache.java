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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;

/**
 * A mapping of service IRIs to result set size limits.
 * A flag indicates whether the limit is a lower bound or exact.
 */
public class ServiceResultSizeCache {
    // The estimate should should never be higher than the actual limit
    protected Cache<Node, Estimate<Long>> serviceToLimit = CacheBuilder.newBuilder()
            .maximumSize(10000).build(); // new ConcurrentHashMap<>(); // new LinkedHashMap<>();

    public Estimate<Long> getLimit(Node service) {
        Estimate<Long> result = serviceToLimit.getIfPresent(service);
        if (result == null) {
            result = new Estimate<>(0l, false);
        }
        return result;
    }

    public void updateLimit(Node service, Estimate<Long> estimate) {
//        Log.debug(ServiceResultSizeCache.class, "Setting backend result set limit for " + service + " to " + estimate);
//        if (estimate.getValue() < 2) {
//            System.err.println("Should not happen");
//        }

        serviceToLimit.put(service, estimate);
    }

    public void invalidateAll() {
        serviceToLimit.invalidateAll();
    }

    /** Return the global instance (if any) in ARQ.getContex() */
    public static ServiceResultSizeCache get() {
        return get(ARQ.getContext());
    }

    public static ServiceResultSizeCache get(Context cxt) {
        return cxt.get(ServiceEnhancerConstants.serviceResultSizeCache);
    }

    public static void set(Context cxt, ServiceResultSizeCache cache) {
        cxt.put(ServiceEnhancerConstants.serviceResultSizeCache, cache);
    }
}
