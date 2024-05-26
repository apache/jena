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
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@State(Scope.Benchmark)
public class TestGraphFindByMatchAndGetFirst {

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
    java.util.function.Function<String, Object> graphFind;
    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;
    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triplesToFind480;

    @Benchmark
    public Object graphFindS__() {
        return graphFind.apply("S__");
    }

    @Benchmark
    public Object graphFind_P_() {
        return graphFind.apply("_P_");
    }

    @Benchmark
    public Object graphFindSP_() {
        return graphFind.apply("SP_");
    }

    @Benchmark
    public Object graphFindS_O() {
        return graphFind.apply("S_O");
    }

    @Benchmark
    public Object graphFind_PO() {
        return graphFind.apply("_PO");
    }

    @Benchmark
    public Object graphFind__O() {
        return graphFind.apply("__O");
    }

    private Object graphFindByMatchesAndGetFirstCurrent(String pattern) {
        var findFunction = getFindFunctionByPatternCurrent(pattern);
        Triple t = null;
        for (Triple sample : this.triplesToFindCurrent) {
            final Iterator<Triple> it = findFunction.apply(sample);
            assertTrue(it.hasNext());
            t = it.next();
            assertNotNull(t);
        }
        return t;
    }

    private Object graphFindByMatchesAndGetFirst480(String pattern) {
        var findFunction = getFindFunctionByPattern480(pattern);
        org.apache.shadedJena480.graph.Triple t = null;
        for (org.apache.shadedJena480.graph.Triple sample : this.triplesToFind480) {
            final Iterator<org.apache.shadedJena480.graph.Triple> it = findFunction.apply(sample);
            assertTrue(it.hasNext());
            t = it.next();
            assertNotNull(t);
        }
        return t;
    }

    Function<Triple, Iterator<Triple>> getFindFunctionByPatternCurrent(String pattern) {
        switch (pattern) {
            case "S__":
                return t -> sutCurrent.find(t.getSubject(), null, null);
            case "_P_":
                return t -> sutCurrent.find(null, t.getPredicate(), null);
            case "__O":
                return t -> sutCurrent.find(null, null, t.getObject());
            case "SP_":
                return t -> sutCurrent.find(t.getSubject(), t.getPredicate(), null);
            case "S_O":
                return t -> sutCurrent.find(t.getSubject(), null, t.getObject());
            case "_PO":
                return t -> sutCurrent.find(null, t.getPredicate(), t.getObject());
            default:
                throw new IllegalArgumentException("Unknown pattern: " + pattern);
        }
    }

    Function<org.apache.shadedJena480.graph.Triple, Iterator<org.apache.shadedJena480.graph.Triple>> getFindFunctionByPattern480(String pattern) {
        switch (pattern) {
            case "S__":
                return t -> sut480.find(t.getSubject(), null, null);
            case "_P_":
                return t -> sut480.find(null, t.getPredicate(), null);
            case "__O":
                return t -> sut480.find(null, null, t.getObject());
            case "SP_":
                return t -> sut480.find(t.getSubject(), t.getPredicate(), null);
            case "S_O":
                return t -> sut480.find(t.getSubject(), null, t.getObject());
            case "_PO":
                return t -> sut480.find(null, t.getPredicate(), t.getObject());
            default:
                throw new IllegalArgumentException("Unknown pattern: " + pattern);
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        Context trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                this.graphFind = this::graphFindByMatchesAndGetFirstCurrent;

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);

                /*clone the triples because they should not be the same objects*/
                this.triplesToFindCurrent = Releases.current.cloneTriples(triples);
                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToFindCurrent, new Random(4721));
            }
            break;
            case JENA_4_8_0: {
                this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                this.graphFind = this::graphFindByMatchesAndGetFirst480;

                var triples = Releases.v480.readTriples(param0_GraphUri);
                triples.forEach(this.sut480::add);

                /*clone the triples because they should not be the same objects*/
                this.triplesToFind480 = Releases.v480.cloneTriples(triples);
                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToFind480, new Random(4721));
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
        assertNotNull(results);
    }
}