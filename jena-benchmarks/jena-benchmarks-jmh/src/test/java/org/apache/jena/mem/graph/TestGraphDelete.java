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

package org.apache.jena.mem.graph;

import java.util.List;
import java.util.Random;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.jmh.JmhDefaultOptions;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

@State(Scope.Benchmark)
public class TestGraphDelete {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/data.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMemFast (current)",
            "GraphMemValue (current)",
//            "GraphMemRoaring EAGER (current)",
//            "GraphMemRoaring LAZY (current)",
//            "GraphMemRoaring LAZY_PARALLEL (current)",
//            "GraphMemRoaring MINIMAL (current)",
//            "GraphMemValue (Jena 5.6.0)",
            "GraphMemFast (Jena 5.6.0)",
            "GraphMemValue (Jena 5.6.0)",
    })
    public String param1_GraphImplementation;
    java.util.function.Supplier<Integer> graphDelete;
    private Context trialContext;
    private Graph sutCurrent;
    private org.apache.shadedJena560.graph.Graph sut560;
    private List<Triple> allTriplesCurrent;
    private List<org.apache.shadedJena560.graph.Triple> allTriples560;
    private List<Triple> triplesToDeleteFromSutCurrent;
    private List<org.apache.shadedJena560.graph.Triple> triplesToDeleteFromSut560;

    @Benchmark
    public int graphDelete() {
        return graphDelete.get();
    }

    private int graphDeleteCurrent() {
        triplesToDeleteFromSutCurrent.forEach(t -> this.sutCurrent.delete(t));
        Assert.assertTrue(this.sutCurrent.isEmpty());
        return this.sutCurrent.size();
    }

    private int graphDelete560() {
        triplesToDeleteFromSut560.forEach(t -> this.sut560.delete(t));
        Assert.assertTrue(this.sut560.isEmpty());
        return this.sut560.size();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                this.sutCurrent = Releases.current.createGraph(this.trialContext.getGraphClass());
                this.allTriplesCurrent.forEach(this.sutCurrent::add);
                /*cloning is important so that the triples are not reference equal */
                this.triplesToDeleteFromSutCurrent = Releases.current.cloneTriples(this.allTriplesCurrent);
                /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToDeleteFromSutCurrent, new Random(4721));
                break;

            case JENA_5_6_0:
                this.sut560 = Releases.v560.createGraph(this.trialContext.getGraphClass());
                this.allTriples560.forEach(this.sut560::add);
                /*cloning is important so that the triples are not reference equal */
                this.triplesToDeleteFromSut560 = Releases.v560.cloneTriples(this.allTriples560);
                /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToDeleteFromSut560, new Random(4721));
                break;

            default:
                throw new IllegalArgumentException("Unknown Jena version: " + this.trialContext.getJenaVersion());
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() {
        this.trialContext = new Context(param1_GraphImplementation);
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                this.allTriplesCurrent = Releases.current.readTriples(param0_GraphUri);
                this.graphDelete = this::graphDeleteCurrent;
                break;
            case JENA_5_6_0:
                this.allTriples560 = Releases.v560.readTriples(param0_GraphUri);
                this.graphDelete = this::graphDelete560;
                break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + this.trialContext.getJenaVersion());
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JmhDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
