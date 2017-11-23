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

package org.apache.jena.graph;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.test.GraphTestBase;
import org.junit.Test;

// Test for the compare by src.size and step dst case.
public class TestGraphUtil {
    private static Graph graph0 = make(0);
    private static Graph graph1 = make(1);
    private static Graph graph2 = make(2);

    private static Graph make(int N) {
        Graph graph = Factory.createGraphMem();
        for ( int i = 0 ; i < N ; i++ ) {
            Triple t = GraphTestBase.triple("a P 'x"+i+"'");
            graph.add(t);
        }
        return graph ;
    }
    
    @Test public void compareSizeTo_1() {
        assertEquals(1, GraphUtil.compareSizeTo(graph2, 0));
        assertEquals(1, GraphUtil.compareSizeTo(graph2, 1));
        assertEquals(0, GraphUtil.compareSizeTo(graph2, 2));
        assertEquals(-1, GraphUtil.compareSizeTo(graph2, 3));
        assertEquals(-1, GraphUtil.compareSizeTo(graph2, 4));
    }
    
    @Test public void compareSizeTo_2() {
        assertEquals(1, GraphUtil.compareSizeTo(graph1, 0));
        assertEquals(0, GraphUtil.compareSizeTo(graph1, 1));
        assertEquals(-1, GraphUtil.compareSizeTo(graph1, 2));
    }

    @Test public void compareSizeTo_3() {
        assertEquals(0, GraphUtil.compareSizeTo(graph0, 0));
        assertEquals(-1, GraphUtil.compareSizeTo(graph0, 1));
    }
}
