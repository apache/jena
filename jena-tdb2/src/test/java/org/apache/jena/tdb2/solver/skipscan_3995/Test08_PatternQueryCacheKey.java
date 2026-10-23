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

package org.apache.jena.tdb2.solver.skipscan_3995;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.tdb2.solver.skipscan.PatternQuery;

/**
 * Finding 8 - {@code PatternQuery:59}.
 *
 * <pre>{@code
 * public record PatternQuery(boolean distinct, List<Var> project, Node[] tuple) { ... }
 * }</pre>
 * <p>A record with an array component gets array-identity {@code equals} and {@code hashCode},
 * so the index cache keyed on it - {@code Map<PatternQuery, SkipScanCandidate>}, keyed by
 * {@code PatternQuery.toCacheKey(subst)} - never hits. {@code findBestCandidate} (which
 * enumerates every index) runs again for every input binding, and the cache in the execution
 * context grows by one entry per binding and is never evicted.</p>
 */
public class Test08_PatternQueryCacheKey {

    private static final Node p = NodeFactory.createURI("http://example/p");

    private static PatternQuery patternQuery() {
        Node[] tuple = new Node[] {Var.alloc("s"), p, Var.alloc("o")};
        return new PatternQuery(true, List.of(Var.alloc("s")), tuple);
    }

    /** Two PatternQuery values with the same contents must be equal. */
    @Test
    public void equalContentsAreEqual() {
        PatternQuery a = patternQuery();
        PatternQuery b = patternQuery();
        assertEquals(a, b, "PatternQuery uses array identity for its tuple component");
        assertEquals(a.hashCode(), b.hashCode(), "PatternQuery hashCode uses array identity");
    }

    /** {@code toCacheKey} exists to canonicalise a pattern, so its results must be equal. */
    @Test
    public void cacheKeysAreEqual() {
        PatternQuery pq = patternQuery();
        PatternQuery k1 = PatternQuery.toCacheKey(pq);
        PatternQuery k2 = PatternQuery.toCacheKey(pq);
        assertEquals(k1, k2, "toCacheKey does not produce equal keys for the same pattern");
        assertEquals(k1.hashCode(), k2.hashCode(), "toCacheKey does not produce equal hash codes");
    }

    /** What the cache actually needs: a second lookup with an equal key must hit. */
    @Test
    public void cacheHits() {
        Object candidate = new Object();
        Map<PatternQuery, Object> cache = new HashMap<>();
        cache.put(PatternQuery.toCacheKey(patternQuery()), candidate);

        assertSame(candidate, cache.get(PatternQuery.toCacheKey(patternQuery())),
                   "The index cache never hits, so the best index is re-planned for every input binding");
        assertEquals(1, cache.size());
    }
}
