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

import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.apache.jena.mem2.GraphMem2;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.function.Supplier;


@State(Scope.Benchmark)
public class TestGraphCopy {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMem2Fast (current)",
            "GraphMem2Roaring EAGER (current)",
            "GraphMem2Roaring LAZY (current)",
            "GraphMem2Roaring LAZY_PARALLEL (current)",
            "GraphMem2Roaring MINIMAL (current)",
    })
    public String param1_GraphImplementation;

    @Param({
            "copy",
            "findAndAddAll",
    })
    public String param2_CopyOrConstruct;

    Supplier<GraphMem2> copySupplier;

    Supplier<GraphMem2> newGraphSupplier;
    private GraphMem2 sutCurrent;

    @Benchmark
    public GraphMem2 copy() {
        return copySupplier.get();
    }

    private GraphMem2 nativeCopy() {
        return sutCurrent.copy();
    }

    private GraphMem2 findAndAddAll() {
        var copy = newGraphSupplier.get();
        sutCurrent.find().forEachRemaining(copy::add);
        return copy;
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        var trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.newGraphSupplier = () -> (GraphMem2) Releases.current.createGraph(trialContext.getGraphClass());
                this.sutCurrent = this.newGraphSupplier.get();

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);
            }
            break;
            default:
                throw new IllegalArgumentException("Unsupported Jena version: " + trialContext.getJenaVersion());
        }
        switch (param2_CopyOrConstruct) {
            case "copy":
                this.copySupplier = this::nativeCopy;
                break;
            case "findAndAddAll":
                this.copySupplier = this::findAndAddAll;
                break;
            default:
                throw new IllegalArgumentException("Unsupported copy or construct: " + param2_CopyOrConstruct);
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
