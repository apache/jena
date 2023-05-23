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

package org.apache.jena.mem.graph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.List;


@State(Scope.Benchmark)
public class TestGraphContainsTriple {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMem (Jena 4.8.0)",
            "GraphMem (current)",
    })
    public String param1_GraphImplementation;

    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;

    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triplesToFind480;

    java.util.function.Supplier<Boolean> graphContains;

    @Benchmark
    public boolean graphContains() {
        return graphContains.get();
    }

    private boolean graphContainsCurrent() {
        var found = false;
        for(var t: triplesToFindCurrent) {
            found = sutCurrent.contains(t);
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean graphContains480() {
        var found = false;
        for(var t: triplesToFind480) {
            found = sut480.contains(t);
            Assert.assertTrue(found);
        }
        return found;
    }


    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        var trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT:
                {
                    this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                    this.graphContains = this::graphContainsCurrent;

                    var triples = Releases.current.readTriples(param0_GraphUri);
                    triples.forEach(this.sutCurrent::add);

                    /*clone the triples because they should not be the same objects*/
                    this.triplesToFindCurrent = Releases.current.cloneTriples(triples);
                }
                break;
            case JENA_4_8_0:
                {
                    this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                    this.graphContains = this::graphContains480;

                    var triples = Releases.v480.readTriples(param0_GraphUri);
                    triples.forEach(this.sut480::add);

                    /*clone the triples because they should not be the same objects*/
                    this.triplesToFind480 = Releases.v480.cloneTriples(triples);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + trialContext.getJenaVersion());
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
