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
public class TestGraphDelete {

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
    private Context trialContext;

    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;

    private List<Triple> allTriplesCurrent;
    private List<org.apache.shadedJena480.graph.Triple> allTriples480;

    private List<Triple> triplesToDeleteFromSutCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triplesToDeleteFromSut480;


    java.util.function.Supplier<Integer> graphDelete;

    @Benchmark
    public int graphDelete() {
        return graphDelete.get();
    }

    private int graphDeleteCurrent() {
        triplesToDeleteFromSutCurrent.forEach(t -> this.sutCurrent.delete(t));
        Assert.assertTrue(this.sutCurrent.isEmpty());
        return this.sutCurrent.size();
    }

    private int graphDelete480() {
        triplesToDeleteFromSut480.forEach(t -> this.sut480.delete(t));
        Assert.assertTrue(this.sut480.isEmpty());
        return this.sut480.size();
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                this.sutCurrent = Releases.current.createGraph(this.trialContext.getGraphClass());
                this.allTriplesCurrent.forEach(this.sutCurrent::add);
                /*cloning is important so that the triples are not reference equal */
                this.triplesToDeleteFromSutCurrent = Releases.current.cloneTriples(this.allTriplesCurrent);
                break;

            case JENA_4_8_0:
                this.sut480 = Releases.v480.createGraph(this.trialContext.getGraphClass());
                this.allTriples480.forEach(this.sut480::add);
                /*cloning is important so that the triples are not reference equal */
                this.triplesToDeleteFromSut480 = Releases.v480.cloneTriples(this.allTriples480);
                break;

            default:
                throw new IllegalArgumentException("Unknown Jena version: " + this.trialContext.getJenaVersion());
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.trialContext = new Context(param1_GraphImplementation);
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                this.allTriplesCurrent = Releases.current.readTriples(param0_GraphUri);
                this.graphDelete = this::graphDeleteCurrent;
                break;
            case JENA_4_8_0:
                this.allTriples480 = Releases.v480.readTriples(param0_GraphUri);
                this.graphDelete = this::graphDelete480;
                break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + this.trialContext.getJenaVersion());
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
