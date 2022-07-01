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

package org.apache.jena.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAsyncHttpRDF {

    static String data[] = { "(_ :s :p :o)", "(:g1 :s :p 1)", "(:g2 :s :p 2)"};
    private static EnvTest env;
    @BeforeClass public static void beforeClass() {
        env = EnvTest.create("/ds");
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->{
            for(String s : data )
                dsg.add(SSE.parseQuad(s));
        });
    }

    @Before public void before() {}

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    private String url(String path) { return env.datasetPath(path); }

    @Test public void asyncHttpRDF_01() {
        var cf = AsyncHttpRDF.asyncGetGraph(url("/ds?default"));
        var graph = AsyncHttpRDF.getOrElseThrow(cf);

        assertNotNull(graph);
        assertEquals(1, graph.size());
    }

    @Test public void asyncHttpRDF_02() {
        var cf0 = AsyncHttpRDF.asyncGetGraph(url("/ds?default"));
        var cf1 = AsyncHttpRDF.asyncGetGraph(url("/ds?graph=http://example/g1"));
        var cf2 = AsyncHttpRDF.asyncGetGraph(url("/ds?graph=http://example/g2"));
        Graph graph = GraphFactory.createDefaultGraph();
        GraphUtil.addInto(graph, AsyncHttpRDF.getOrElseThrow(cf0));
        GraphUtil.addInto(graph, AsyncHttpRDF.getOrElseThrow(cf1));
        GraphUtil.addInto(graph, AsyncHttpRDF.getOrElseThrow(cf2));
        assertEquals(3, graph.size());
    }

    @Test public void asyncHttpRDF_03() {
        var cf0 = AsyncHttpRDF.asyncGetDatasetGraph(url("/ds"));
        DatasetGraph dsg = AsyncHttpRDF.getOrElseThrow(cf0);
        assertEquals(3, Iter.count(dsg.find()));
    }

    @Test public void asyncHttpRDF_04() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        var cf = AsyncHttpRDF.asyncLoadDatasetGraph(url("/ds"), dsg);
        AsyncHttpRDF.getOrElseThrow(cf);
        assertEquals(3, Iter.count(dsg.find()));
    }
}
