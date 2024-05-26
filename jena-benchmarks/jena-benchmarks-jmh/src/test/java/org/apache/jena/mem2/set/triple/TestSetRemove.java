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

package org.apache.jena.mem2.set.triple;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Releases;
import org.apache.jena.mem2.helper.JMHDefaultOptions;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.HashSet;
import java.util.List;

@State(Scope.Benchmark)
public class TestSetRemove {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "HashSet",
            "HashCommonTripleSet",
            "FastHashTripleSet"
    })
    public String param1_SetImplementation;
    java.util.function.Supplier<Integer> removeFromSet;
    private List<Triple> triples;
    private List<Triple> triplesToRemove;
    private HashSet<Triple> hashSet;
    private HashCommonTripleSet hashCommonTripleSet;
    private FastHashTripleSet fastHashTripleSet;

    @Benchmark
    public int setRemove() {
        return removeFromSet.get();
    }

    private int removeFromHashSet() {
        triplesToRemove.forEach(t -> this.hashSet.remove(t));
        Assert.assertTrue(this.hashSet.isEmpty());
        return this.hashSet.size();
    }

    private int removeFromHashCommonTripleSet() {
        triplesToRemove.forEach(t -> this.hashCommonTripleSet.removeUnchecked(t));
        Assert.assertTrue(this.hashCommonTripleSet.isEmpty());
        return this.hashCommonTripleSet.size();
    }

    private int removeFromFastHashTripleSet() {
        triplesToRemove.forEach(t -> this.fastHashTripleSet.removeUnchecked(t));
        Assert.assertTrue(this.fastHashTripleSet.isEmpty());
        return this.fastHashTripleSet.size();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        switch (param1_SetImplementation) {
            case "HashSet":
                this.hashSet = new HashSet<>(triples.size());
                this.triples.forEach(hashSet::add);
                break;
            case "HashCommonTripleSet":
                this.hashCommonTripleSet = new HashCommonTripleSet(triples.size());
                this.triples.forEach(hashCommonTripleSet::addUnchecked);
                break;
            case "FastHashTripleSet":
                this.fastHashTripleSet = new FastHashTripleSet(triples.size());
                this.triples.forEach(fastHashTripleSet::addUnchecked);
                break;
            default:
                throw new IllegalArgumentException("Unknown set implementation: " + param1_SetImplementation);
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.triples = Releases.current.readTriples(param0_GraphUri);
        this.triplesToRemove = Releases.current.cloneTriples(triples);
        switch (param1_SetImplementation) {
            case "HashSet":
                this.removeFromSet = this::removeFromHashSet;
                break;
            case "HashCommonTripleSet":
                this.removeFromSet = this::removeFromHashCommonTripleSet;
                break;
            case "FastHashTripleSet":
                this.removeFromSet = this::removeFromFastHashTripleSet;
                break;
            default:
                throw new IllegalArgumentException("Unknown set implementation: " + param1_SetImplementation);
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
