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

package org.apache.jena.sparql.service.enhancer.pfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Range;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunctionEval;
import org.apache.jena.sparql.service.enhancer.assembler.ServiceEnhancerVocab;
import org.apache.jena.sparql.service.enhancer.claimingcache.RefFuture;
import org.apache.jena.sparql.service.enhancer.impl.ServiceCacheKey;
import org.apache.jena.sparql.service.enhancer.impl.ServiceCacheValue;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResponseCache;
import org.apache.jena.sparql.service.enhancer.impl.util.PropFuncArgUtils;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.service.enhancer.slice.api.Slice;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;


/**
 * A property function for listing the cache's content.
 * Accessible via IRI {@value org.apache.jena.sparql.service.enhancer.pfunction.cacheLs#DEFAULT_IRI}.
 * <br />
 * Alternatively via:
 * {@code ?id <java:org.apache.jena.sparql.service.enhancer.pfunction.cacheLs> (?serviceIri ?queryStr ?joinBindingStr ?start ?end}
 */
public class cacheLs
    extends PropertyFunctionEval
{
    public static final String DEFAULT_IRI = ServiceEnhancerVocab.NS + "cacheLs";

    public cacheLs() {
        super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_EITHER);
    }

    private static Optional<BindingBuilder> processArg(Optional<BindingBuilder> builderOpt, List<Node> nodes, int i, Supplier<Node> valueSupplier) {
        Optional<BindingBuilder> result = builderOpt;
        if (builderOpt.isPresent()) {
            BindingBuilder builder = builderOpt.get();
            int n = nodes.size();
            if (i < n) {
                Node key = nodes.get(i);

                Node value = valueSupplier.get();
                if (key.isVariable()) {
                    builder.add((Var)key, value);
                } else if (!Objects.equals(key, value)) {
                    result = Optional.empty();
                }
            }
        }

        return result;
    }

    @Override
    public QueryIterator execEvaluated(Binding inputBinding, PropFuncArg subject, Node predicate, PropFuncArg object,
            ExecutionContext execCxt) {

        Context context = execCxt.getContext();
        ServiceResponseCache cache = context.get(ServiceEnhancerConstants.serviceCache);

        Node s = subject.getArg();
        Var sv = s instanceof Var ? (Var)s : null;

        Set<Long> subset = null;
        if (sv == null) {
            NodeValue snv = NodeValue.makeNode(s);
            if (snv.isInteger()) {
                long v = snv.getInteger().longValue();
                subset = Collections.singleton(v);
            }
        }

        List<Node> objectArgs = PropFuncArgUtils.getAsList(object);

        Map<Long, ServiceCacheKey> idToKey = cache.getIdToKey();
        Set<Long> baseIds = idToKey.keySet();

        Collection<Long> ids = subset == null
                ? baseIds
                : Sets.intersection(subset, baseIds);

        Iterator<Binding> it = ids.stream()
            .flatMap(id -> {
                Node idNode = NodeValue.makeInteger(id).asNode();

                Optional<BindingBuilder> parentBuilder = Optional.of(BindingFactory.builder(inputBinding));
                if (sv != null) {
                    parentBuilder.get().add(sv, idNode);
                }

                ServiceCacheKey key = idToKey.get(id);

                parentBuilder = processArg(parentBuilder, objectArgs, 0, () -> key.getServiceNode());
                parentBuilder = processArg(parentBuilder, objectArgs, 1, () -> {
                    Op normOp = key.getOp();
                    Op op = Rename.reverseVarRename(normOp, true);
                    Query query = OpAsQuery.asQuery(op);
                    return NodeFactory.createLiteral(query.toString());
                });

                parentBuilder = processArg(parentBuilder, objectArgs, 2, () -> NodeFactory.createLiteral(key.getBinding().toString()));

                Optional<Binding> parentBindingOpt = parentBuilder.map(BindingBuilder::build);

                Stream<Binding> r = parentBindingOpt.stream();

                // Join in the range information if more than 3 arguments were supplied
                if (objectArgs.size() > 3) {
                    r = r.flatMap(parentBinding -> {

                        Collection<Range<Long>> ranges;
                        try (RefFuture<ServiceCacheValue> refFuture = cache.getCache().claimIfPresent(key)) {
                            if (refFuture != null) {
                                ServiceCacheValue entry = refFuture.await();
                                Slice<Binding[]> slice = entry.getSlice();
                                Lock lock = slice.getReadWriteLock().readLock();
                                lock.lock();
                                try {
                                    ranges = new ArrayList<>(entry.getSlice().getLoadedRanges().asRanges());
                                } finally {
                                    lock.unlock();
                                }

                                if (ranges.isEmpty()) {
                                    ranges = Collections.singletonList(Range.closedOpen(0l, 0l));
                                }
                            } else {
                                // Flat-mapping an empty collection prevents the cache key from showing up
                                // This should be ok when the future is not ready yet
                                ranges = Collections.emptyList();
                            }
                        }

                        return ranges.stream().flatMap(range -> {
                            Optional<BindingBuilder> bb = Optional.of(BindingBuilder.create(parentBinding));

                            if (range.hasLowerBound()) {
                                bb = processArg(bb, objectArgs, 3, () -> NodeFactoryExtra.intToNode(range.lowerEndpoint()));
                            }

                            if (range.hasUpperBound()) {
                                bb = processArg(bb, objectArgs, 4, () -> NodeFactoryExtra.intToNode(range.upperEndpoint()));
                            }

                            return bb.map(BindingBuilder::build).stream();
                        });
                    });
                }
                return r;
            })
            .iterator();

        return QueryIterPlainWrapper.create(it, execCxt);
    }
}
