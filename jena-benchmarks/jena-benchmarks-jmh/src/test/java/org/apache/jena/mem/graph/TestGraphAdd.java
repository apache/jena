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
public class TestGraphAdd {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMem (current)",
            "GraphMem2Fast (current)",
            "GraphMem2Legacy (current)",
            "GraphMem2Roaring (current)",
            "GraphMem (Jena 4.8.0)",
    })
    public String param1_GraphImplementation;
    java.util.function.Supplier<Object> graphAdd;
    private Context trialContext;
    private List<Triple> triplesCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triples480;

    @Benchmark
    public Object graphAdd() {
        return graphAdd.get();
    }

    private Object graphAddCurrent() {
        var sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
        triplesCurrent.forEach(sutCurrent::add);
        Assert.assertEquals(triplesCurrent.size(), sutCurrent.size());
        return sutCurrent;
    }

    private Object graphAdd480() {
        var sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
        triples480.forEach(sut480::add);
        Assert.assertEquals(triples480.size(), sut480.size());
        return sut480;
    }


    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.trialContext = new Context(param1_GraphImplementation);
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                triplesCurrent = Releases.current.readTriples(param0_GraphUri);
                this.graphAdd = this::graphAddCurrent;
                break;
            case JENA_4_8_0:
                triples480 = Releases.v480.readTriples(param0_GraphUri);
                this.graphAdd = this::graphAdd480;
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
