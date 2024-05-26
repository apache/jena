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
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestBufferingDatasetGraph {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;

        Creator<DatasetGraph> baseMem = ()->DatasetGraphFactory.createTxnMem();
        //Creator<DatasetGraph> baseTDB1 = ()->TDBFactory.createDatasetGraph();
        //Creator<DatasetGraph> baseTDB2 = ()->DatabaseMgr.createDatasetGraph();

        Function<DatasetGraph, DatasetGraphBuffering> buffering = BufferingDatasetGraph::new;
        //Function<DatasetGraph, DatasetGraphBuffering> bufferingQuads = BufferingDatasetGraphQuads::new;

        // Quads needs the txn machinery from normal.

        x.add(new Object[] {"DatasetGraphBuffering(TIM)", baseMem, buffering});
        //x.add(new Object[] {"DatasetGraphBuffering(TIM) Quads", baseMem, bufferingQuads});

        // [BUFFERING]

//        x.add(new Object[] {"DatasetGraphBuffering(TDB1)", baseTDB1, buffering});
//        //x.add(new Object[] {"DatasetGraphBuffering(TDB1) Quads", baseTDB1, bufferingQuads});
//
//        x.add(new Object[] {"DatasetGraphBuffering(TDB2)", baseTDB2, buffering});
//        //x.add(new Object[] {"DatasetGraphBuffering(TDB2) Quads", baseTDB2, bufferingQuads});

        return x ;
    }

    private final DatasetGraph base;
    private final DatasetGraphBuffering buffered;

    public TestBufferingDatasetGraph(String name, Creator<DatasetGraph> baseSupplier, Function<DatasetGraph, DatasetGraphBuffering> factory) {
        base = baseSupplier.create();
        buffered = factory.apply(base);
    }

    @Test public void basic_1() {
        buffered.executeRead(()->
            assertTrue(buffered.isEmpty())
            );
    }

    @Test public void basic_2() {
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        buffered.execute(()-> {
            // Base read
            buffered.add(q);
            // Base read
            assertTrue(base.isEmpty());
            assertFalse(buffered.isEmpty());
        });
    }

    @Test public void basic_3() {
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        buffered.execute(()-> {
            buffered.add(q);
            assertTrue(base.isEmpty());
            assertFalse(buffered.isEmpty());
            buffered.flush();
            assertFalse(base.isEmpty());
            assertFalse(buffered.isEmpty());
        });
    }

    @Test public void basic_4() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        buffered.executeWrite(() -> {
            base.add(q1);
            assertFalse(base.isEmpty());
            assertFalse(buffered.isEmpty());
        });
    }

    @Test public void basic_5() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");

        buffered.executeWrite(() -> {
            base.add(q1);
            buffered.delete(q1);

            assertFalse(base.isEmpty());
            assertTrue(buffered.isEmpty());

            buffered.flush();

            assertTrue(base.isEmpty());
            assertTrue(buffered.isEmpty());

            buffered.add(q1);
            buffered.delete(q1);
            buffered.flush();

            base.isEmpty();

            assertTrue(base.isEmpty());
            assertTrue(buffered.isEmpty());
        });
    }

    @Test public void buffered_1() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        buffered.add(q1);
        buffered.flush();
        boolean b = buffered.contains(q1);
        assertTrue(b);
        base.executeRead(()->{
            assertFalse(base.isEmpty());
        });
    }

    @Test public void buffered_2() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        Quad q2 = SSE.parseQuad("(:g :s :p 2)");
        base.executeWrite(()->base.add(q1));
        boolean b = buffered.contains(q1);
        buffered.add(q2);
        assertTrue(b);
    }

    @Test public void buffered_3() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        Quad q2 = SSE.parseQuad("(:g :s :p 2)");

        // Promotable
        base.execute(()->{
            buffered.add(q2);
            buffered.flush();
        });
    }

    @Test(expected=JenaTransactionException.class)
    public void buffered_4() {
        Quad q1 = SSE.parseQuad("(:g :s :p 1)");
        Quad q2 = SSE.parseQuad("(:g :s :p 2)");

        // Not promotable. Read then attempt to update.
        base.executeRead(()->{
            buffered.add(q2);
            buffered.flush();
        });
    }


}
