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
public class TestSetAdd {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "HashSet",
            "HashCommonTripleSet",
            "FastHashTripleSet",
    })
    public String param1_SetImplementation;
    java.util.function.Supplier<Object> addToSet;
    private List<Triple> triples;

    @Benchmark
    public Object addToSet() {
        return addToSet.get();
    }

    private Object addToHashSet() {
        var sut = new HashSet<Triple>();
        triples.forEach(sut::add);
        Assert.assertEquals(triples.size(), sut.size());
        return sut;
    }

    private Object addToHashCommonTripleSet() {
        var sut = new HashCommonTripleSet();
        triples.forEach(sut::tryAdd);
        Assert.assertEquals(triples.size(), sut.size());
        return sut;
    }

    private Object addToFastHashTripleSet() {
        var sut = new FastHashTripleSet();
        triples.forEach(sut::tryAdd);
        Assert.assertEquals(triples.size(), sut.size());
        return sut;
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        triples = Releases.current.readTriples(param0_GraphUri);
        switch (param1_SetImplementation) {
            case "HashSet":
                this.addToSet = this::addToHashSet;
                break;
            case "HashCommonTripleSet":
                this.addToSet = this::addToHashCommonTripleSet;
                break;
            case "FastHashTripleSet":
                this.addToSet = this::addToFastHashTripleSet;
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
