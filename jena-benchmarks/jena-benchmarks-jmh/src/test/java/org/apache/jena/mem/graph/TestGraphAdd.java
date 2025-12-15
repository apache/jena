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

import org.apache.jena.jmh.JmhDefaultOptions;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

@State(Scope.Benchmark)
public class TestGraphAdd {

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
    java.util.function.Supplier<Object> graphAdd;
    private Context trialContext;
    private List<Triple> triplesCurrent;
    private List<org.apache.shadedJena560.graph.Triple> triples560;

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

    private Object graphAdd560() {
        var sut560 = Releases.v560.createGraph(trialContext.getGraphClass());
        triples560.forEach(sut560::add);
        Assert.assertEquals(triples560.size(), sut560.size());
        return sut560;
    }


    @Setup(Level.Trial)
    public void setupTrial() {
        this.trialContext = new Context(param1_GraphImplementation);
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                triplesCurrent = Releases.current.readTriples(param0_GraphUri);
                this.graphAdd = this::graphAddCurrent;
                break;
            case JENA_5_6_0:
                triples560 = Releases.v560.readTriples(param0_GraphUri);
                this.graphAdd = this::graphAdd560;
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
