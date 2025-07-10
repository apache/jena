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

package org.apache.jena.system.buffering;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.G;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestBufferingGraph {

    public static Stream<Arguments> provideArgs() {
        Function<Graph, BufferingGraph> buffering = BufferingGraph::new;
        Creator<Graph> base1 = ()->GraphFactory.createGraphMem();
        Creator<Graph> base2 = ()->DatasetGraphFactory.createTxnMem().getDefaultGraph();
        //Creator<Graph> base3 = ()->TDBFactory.createDatasetGraph().getDefaultGraph();
        //Creator<Graph> base4 = ()->DatabaseMgr.createDatasetGraph().getDefaultGraph();

        List<Arguments> x = List.of
                (Arguments.of("Graph", base1, buffering),
                 Arguments.of("GraphView(TIM)", base2, buffering)
                 );
        // [BUFFERING]
        //Arguments.of("GraphView(TDB2)", base3, buffering);
        //Arguments.of("GraphView(TDB1)", base4, buffering);
        return x.stream() ;
    }

    private final Graph base;
    private final BufferingGraph buffered;

    public TestBufferingGraph(String name, Creator<Graph> factoryBase, Function<Graph, BufferingGraph> factoryBuffering) {
        this.base = factoryBase.create();
        this.buffered = factoryBuffering.apply(base);
    }

    @Test public void basic_1() {
        BufferingGraph graph = buffered;
        G.execTxn(graph, ()->graph.isEmpty());
    }

    @Test public void basic_2() {
        BufferingGraph graph = buffered;
        Triple t = SSE.parseTriple("(:s :p :o)");
        G.execTxn(graph, ()->{
            graph.add(t);
            assertTrue(base.isEmpty());
            assertFalse(graph.isEmpty());
        });
    }

    @Test public void basic_3() {
        BufferingGraph graph = buffered;
        Triple t = SSE.parseTriple("(:s :p :o)");
        G.execTxn(base, ()->{
            graph.add(t);
            assertTrue(base.isEmpty());
            assertFalse(graph.isEmpty());
            graph.flushDirect(); // Does a graph txn which does not nest.
            assertFalse(base.isEmpty());
            assertFalse(graph.isEmpty());
        });
    }

    @Test public void basic_4() {
        Triple t1 = SSE.parseTriple("(:s :p 1)");
        G.execTxn(base, ()->{
            base.add(t1);
            BufferingGraph graph = buffered;
            assertFalse(base.isEmpty());
            assertFalse(graph.isEmpty());
        });
    }

    @Test public void basic_5() {
        {
            Triple t1 = SSE.parseTriple("(:s :p 1)");
            G.execTxn(base, ()->base.add(t1));
        }
        BufferingGraph graph = buffered;
        // New object, same triple.
        Triple t2 = SSE.parseTriple("(:s :p 1)");
        G.execTxn(base, ()->graph.delete(t2));
        G.execTxn(base, ()-> {
            assertFalse(base.isEmpty());
            assertTrue(graph.isEmpty());
        });

        graph.flush();
        G.execTxn(graph, ()->{
            assertTrue(base.isEmpty());
            assertTrue(graph.isEmpty());
        });
    }

    @Test public void basic_6() {
        BufferingGraph graph = buffered;
        // New object, same triple.
        Triple t = SSE.parseTriple("(:s :p 1)");
        buffered.add(t);
        assertFalse(buffered.isEmpty());
        buffered.reset();
        assertTrue(buffered.isEmpty());
    }
}
