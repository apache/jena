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

package org.apache.jena.sparql.service.enhancer.function;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.service.enhancer.assembler.ServiceEnhancerVocab;
import org.apache.jena.sparql.service.enhancer.impl.ServiceCacheKey;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResponseCache;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;

/**
 * Invalidate the given keys (or all if none are given). Returns the number of invalidated cache entries.
 * This function only works if {@link ServiceEnhancerConstants#enableMgmt} is set to true in the context.
 */
public class cacheRm
    extends FunctionBase
{
    public static final String DEFAULT_IRI = ServiceEnhancerVocab.NS + "cacheRm";

    /** This method must be implemented but it is only called from the base implementation of
     * {@link #exec(List, FunctionEnv)} which is overridden here too */
    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new IllegalStateException("Should never be called");
    }

    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        Context cxt = env.getContext();

        if (!cxt.isTrue(ServiceEnhancerConstants.enableMgmt)) {
            throw new QueryExecException("Service enhancer management functions have not been enabled for this dataset");
        }

        ServiceResponseCache cache = ServiceResponseCache.get(cxt);

        long resultCount = 0;

        if (cache != null) {
            Map<Long, ServiceCacheKey> idToKey = cache.getIdToKey();

            Collection<ServiceCacheKey> keys;

            if (!args.isEmpty()) {
                keys = args.stream()
                        .filter(Objects::nonNull)
                        .filter(NodeValue::isInteger)
                        .map(NodeValue::getInteger)
                        .map(BigInteger::longValue)
                        .map(idToKey::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
            } else {
                keys = cache.getCache().getPresentKeys();
            }

            resultCount = keys.size();
            cache.getCache().invalidateAll(keys);

        } else {
            // If there is no cache always return 0
            // Alternatively: throw new ExprEvalException("");
        }

        return NodeValue.makeInteger(resultCount);
    }


    @Override
    public void checkBuild(String uri, ExprList args) {
        // Nothing to do
    }
}
