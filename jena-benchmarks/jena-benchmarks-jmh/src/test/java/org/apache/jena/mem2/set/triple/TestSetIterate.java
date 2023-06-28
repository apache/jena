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

import org.apache.jena.atlas.iterator.ActionCount;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Releases;
import org.apache.jena.mem2.helper.JMHDefaultOptions;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;


@State(Scope.Benchmark)
public class TestSetIterate {

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
    java.util.function.Supplier<Iterator<Triple>> getIterator;
    private List<Triple> triples;
    private HashSet<Triple> hashSet;
    private HashCommonTripleSet hashCommonTripleSet;
    private FastHashTripleSet fastHashTripleSet;

    @Benchmark
    public Object foreachRemaining() {
        var it = getIterator.get();
        ActionCount<Triple> counter = new ActionCount<>();
        it.forEachRemaining(counter);
        assertEquals(triples.size(), counter.getCount());
        return counter;
    }

    @Benchmark
    public Object hasNextNext() {
        var it = getIterator.get();
        int i = 0;
        while (it.hasNext()) {
            it.next();
            i++;
        }
        assertEquals(triples.size(), i);
        return i;
    }

    private Iterator<Triple> getIteratorFromHashSet() {
        return hashSet.iterator();
    }

    private Iterator<Triple> getIteratorFromHashCommonTripleSet() {
        return hashCommonTripleSet.keyIterator();
    }

    private Iterator<Triple> getIteratorFromFastHashTripleSet() {
        return fastHashTripleSet.keyIterator();
    }


    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.triples = Releases.current.readTriples(param0_GraphUri);
        switch (param1_SetImplementation) {
            case "HashSet":
                this.hashSet = new HashSet<>(triples.size());
                triples.forEach(hashSet::add);
                this.getIterator = this::getIteratorFromHashSet;
                break;
            case "HashCommonTripleSet":
                this.hashCommonTripleSet = new HashCommonTripleSet(triples.size());
                triples.forEach(hashCommonTripleSet::addUnchecked);
                this.getIterator = this::getIteratorFromHashCommonTripleSet;
                break;
            case "FastHashTripleSet":
                this.fastHashTripleSet = new FastHashTripleSet(triples.size());
                triples.forEach(fastHashTripleSet::addUnchecked);
                this.getIterator = this::getIteratorFromFastHashTripleSet;
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