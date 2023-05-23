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

import org.apache.jena.atlas.iterator.ActionCount;
import org.apache.jena.graph.Graph;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import static org.junit.Assert.assertEquals;


@State(Scope.Benchmark)
public class TestGraphFindAllWithForEachRemaining {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMem (current)",
            "GraphMem (Jena 4.8.0)",
    })
    public String param1_GraphImplementation;
    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;

    java.util.function.Supplier<Long> graphFindAll;

    @Benchmark
    public Long graphFindAll() {
        return graphFindAll.get();
    }

    private Long graphFindAllCurrent() {
        var actionCounter = new ActionCount<>();
        var iter = sutCurrent.find();
        iter.forEachRemaining(actionCounter::accept);
        iter.close();
        assertEquals(sutCurrent.size(), actionCounter.getCount());
        return actionCounter.getCount();
    }

    private Long graphFindAll480() {
        var actionCounter = new ActionCount<>();
        var iter = sut480.find();
        iter.forEachRemaining(actionCounter::accept);
        iter.close();
        assertEquals(sut480.size(), actionCounter.getCount());
        return actionCounter.getCount();
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        Context trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT:
                {
                    this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                    this.graphFindAll = this::graphFindAllCurrent;

                    var triples = Releases.current.readTriples(param0_GraphUri);
                    triples.forEach(this.sutCurrent::add);
                }
                break;
            case JENA_4_8_0:
                {
                    this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                    this.graphFindAll = this::graphFindAll480;

                    var triples = Releases.v480.readTriples(param0_GraphUri);
                    triples.forEach(this.sut480::add);
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