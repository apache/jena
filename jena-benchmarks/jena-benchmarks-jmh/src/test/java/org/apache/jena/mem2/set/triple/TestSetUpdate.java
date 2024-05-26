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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@State(Scope.Benchmark)
public class TestSetUpdate {

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
    java.util.function.Supplier<Integer> updateSet;
    private List<Triple> triples;
    private List<Triple> triplesToRemove;
    private HashSet<Triple> hashSet;
    private HashCommonTripleSet hashCommonTripleSet;
    private FastHashTripleSet fastHashTripleSet;

    @Benchmark
    public int updateSet() {
        return updateSet.get();
    }

    private int updateHashSet() {
        for (int i = 0; i < triplesToRemove.size(); i += 10) {
            triplesToRemove.subList(i, Math.min(i + 10, triplesToRemove.size()))
                    .forEach(t -> this.hashSet.remove(t));
            triplesToRemove.subList(i, Math.min(i + 10, triplesToRemove.size()))
                    .forEach(t -> this.hashSet.add(t));
            assert this.hashSet.size() == triples.size();
        }
        return this.hashSet.size();
    }

    private int updateHashCommonTripleSet() {
        for (int i = 0; i < triplesToRemove.size(); i += 10) {
            triplesToRemove.subList(i, Math.min(i + 10, triplesToRemove.size()))
                    .forEach(t -> this.hashCommonTripleSet.tryRemove(t));
            triplesToRemove.subList(i, Math.min(i + 10, triplesToRemove.size()))
                    .forEach(t -> this.hashCommonTripleSet.tryAdd(t));
            assert this.hashCommonTripleSet.size() == triples.size();
        }
        return this.hashCommonTripleSet.size();
    }


    private int updateFastHashTripleSet() {
        for (int i = 0; i < triplesToRemove.size(); i += 10) {
            triplesToRemove.subList(i, Math.min(i + 10, triplesToRemove.size()))
                    .forEach(t -> this.fastHashTripleSet.tryRemove(t));
            triplesToRemove.subList(i, Math.min(i + 10, triplesToRemove.size()))
                    .forEach(t -> this.fastHashTripleSet.tryAdd(t));
            assert this.fastHashTripleSet.size() == triples.size();
        }
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
        Collections.shuffle(triplesToRemove, new Random(4721));
        switch (param1_SetImplementation) {
            case "HashSet":
                this.updateSet = this::updateHashSet;
                break;
            case "HashCommonTripleSet":
                this.updateSet = this::updateHashCommonTripleSet;
                break;
            case "FastHashTripleSet":
                this.updateSet = this::updateFastHashTripleSet;
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
