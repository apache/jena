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

package org.apache.jena.atlas.lib.cache;

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.Iterator;
import java.util.function.Function;

@State(Scope.Benchmark)
public class TestCaches {

    final static int DEFAULT_CACHE_SIZE = 1_024_000;

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
//            "../testing/BSBM/bsbm-5m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "Caffeine",
            "Simple",
            "Jena510.Caffeine",
            "Jena510.Simple"
    })
    public String param1_Cache;

    private Graph graph;

    private Cache<String, Node> cache;

    private static int calculateRealCacheSize(int minCacheSize) {
        // the cache size is a power of 2 --> that is a requirement for the CacheSimpleFast
        // to start with fair conditions for the caches, we use the same size for all caches
        var cacheSize = Integer.highestOneBit(minCacheSize);
        if (cacheSize < minCacheSize) {
            cacheSize <<= 1;
        }
        return cacheSize;
    }

    private static Cache<String, Node> createCache(String cacheName) {
        var cacheSize = calculateRealCacheSize(DEFAULT_CACHE_SIZE);
        switch (cacheName) {
            case "Caffeine":
                return CacheFactory.createCache(cacheSize);
            case "Simple":
                return CacheFactory.createSimpleCache(cacheSize);
            case "Jena510.Caffeine":
                return new CacheFromJena510Wrapped<>(org.apache.shadedJena510.atlas.lib.CacheFactory.createCache(cacheSize));
            case "Jena510.Simple":
                return new CacheFromJena510Wrapped<>(org.apache.shadedJena510.atlas.lib.CacheFactory.createSimpleCache(cacheSize));
            default:
                throw new IllegalArgumentException("Unknown Cache: " + cacheName);
        }
    }

    @Benchmark
    public int getFromFilledCache() {
        final int[] hash = {0};
        graph.find().forEachRemaining(t -> {
            if(t.getSubject().isURI()) {
                hash[0] += cache.get(t.getSubject().getURI(),
                        s -> t.getSubject()).getURI().hashCode();

            }
            if(t.getPredicate().isURI()) {
                hash[0] += cache.get(t.getPredicate().getURI(),
                        s -> t.getPredicate()).getURI().hashCode();

            }
            if(t.getObject().isURI()) {
                hash[0] += cache.get(t.getObject().getURI(),
                        s -> t.getObject()).getURI().hashCode();

            }
        });
        return hash[0];
    }

    @Benchmark
    public int getIfPresentFromFilledCache() {
        final int[] hash = {0};
        graph.find().forEachRemaining(t -> {
            if(t.getSubject().isURI()) {
                final var value = cache.getIfPresent(t.getSubject().getURI());
                if(value != null)
                    hash[0] += value.getURI().hashCode();

            }
            if(t.getPredicate().isURI()) {
                final var value = cache.getIfPresent(t.getPredicate().getURI());
                if(value != null)
                    hash[0] += value.getURI().hashCode();
            }
            if(t.getObject().isURI()) {
                final var value = cache.getIfPresent(t.getObject().getURI());
                if(value != null)
                    hash[0] += value.getURI().hashCode();
            }
        });
        return hash[0];
    }

    @Benchmark
    public Cache<String, Node> createAndFillCacheByGet() {
        var c = createCache(param1_Cache);
        fillCacheByGet(c, graph);
        return c;
    }

    @Benchmark
    public Cache<String, Node> createAndFillCacheByPut() {
        var c = createCache(param1_Cache);
        fillCacheByPut(c, graph);
        return c;
    }

    private static void fillCacheByGet(Cache<String, Node> cacheToFill, Graph g) {
        g.find().forEachRemaining(t -> {
            if(t.getSubject().isURI()) {
                cacheToFill.get(t.getSubject().getURI(), s -> t.getSubject());
            }
            if(t.getPredicate().isURI()) {
                cacheToFill.get(t.getPredicate().getURI(), s -> t.getPredicate());
            }
            if(t.getObject().isURI()) {
                cacheToFill.get(t.getObject().getURI(), s -> t.getObject());
            }
        });
    }

    private static void fillCacheByPut(Cache<String, Node> cacheToFill, Graph g) {
        g.find().forEachRemaining(t -> {
            if(t.getSubject().isURI()) {
                cacheToFill.put(t.getSubject().getURI(), t.getSubject());
            }
            if(t.getPredicate().isURI()) {
                cacheToFill.put(t.getPredicate().getURI(), t.getPredicate());
            }
            if(t.getObject().isURI()) {
                cacheToFill.put(t.getObject().getURI(), t.getObject());
            }
        });
    }

    private static class CacheFromJena510Wrapped<K, V> implements Cache<K, V> {

        private final org.apache.shadedJena510.atlas.lib.Cache<K, V> wrappedCache;

        public CacheFromJena510Wrapped(org.apache.shadedJena510.atlas.lib.Cache<K, V> cacheFromJena510) {
            this.wrappedCache = cacheFromJena510;
        }


        @Override
        public boolean containsKey(K k) {
            return wrappedCache.containsKey(k);
        }

        @Override
        public V getIfPresent(K k) {
            return wrappedCache.getIfPresent(k);
        }

        @Override
        public V get(K k, Function<K, V> callable) {
            return wrappedCache.get(k, callable);
        }

        @Override
        public void put(K k, V thing) {
            wrappedCache.put(k, thing);
        }

        @Override
        public void remove(K k) {
            wrappedCache.remove(k);
        }

        @Override
        public Iterator<K> keys() {
            return wrappedCache.keys();
        }

        @Override
        public boolean isEmpty() {
            return wrappedCache.isEmpty();
        }

        @Override
        public void clear() {
            wrappedCache.clear();
        }

        @Override
        public long size() {
            return wrappedCache.size();
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.graph = GraphFactory.createGraphMem();
        RDFDataMgr.read(this.graph, this.param0_GraphUri);
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        this.cache = createCache(param1_Cache);
        fillCacheByGet(this.cache, this.graph);
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
