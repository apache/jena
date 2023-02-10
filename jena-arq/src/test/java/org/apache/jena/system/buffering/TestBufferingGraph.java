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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestBufferingGraph {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        Function<Graph, BufferingGraph> buffering = BufferingGraph::new;
        Creator<Graph> base1 = ()->GraphFactory.createGraphMem();
        Creator<Graph> base2 = ()->DatasetGraphFactory.createTxnMem().getDefaultGraph();
        // [BUFFERING]
        //Creator<Graph> base3 = ()->TDBFactory.createDatasetGraph().getDefaultGraph();
        //Creator<Graph> base4 = ()->DatabaseMgr.createDatasetGraph().getDefaultGraph();

        x.add(new Object[] {"Graph", base1, buffering});
        x.add(new Object[] {"GraphView(TIM)", base2, buffering});
        // [BUFFERING]
        //x.add(new Object[] {"GraphView(TDB2)", base3, buffering});
        //x.add(new Object[] {"GraphView(TDB1)", base4, buffering});
        return x ;
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
}
