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

package com.hp.hpl.jena.sparql.graph;

import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.graph.GraphUnionRead ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;

public class TestGraphUnionRead extends BaseTest
{
    private static String dataStr = StrUtils.strjoinNL(
      "(dataset" ,
      "  (graph" ,
      "   (triple <http://example/s> <http://example/p> 'dft')" ,
      "   (triple <http://example/s> <http://example/p> <http://example/o>)" ,
      " )" ,
      " (graph <http://example/g1>",
      "   (triple <http://example/s> <http://example/p> 'g1')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " )",
      " (graph <http://example/g2>", 
      "   (triple <http://example/s> <http://example/p> 'g2')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " )",
      " (graph <http://example/g3>",
      "   (triple <http://example/s> <http://example/p> 'g3')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " ))") ;
    private static DatasetGraph dsg = null ;
    static {
        Item item = SSE.parse(dataStr) ;
        dsg = BuilderGraph.buildDataset(item) ;
    }
    private static Node gn1 = SSE.parseNode("<http://example/g1>") ;
    private static Node gn2 = SSE.parseNode("<http://example/g2>") ;
    private static Node gn3 = SSE.parseNode("<http://example/g3>") ;
    private static Node gn9 = SSE.parseNode("<http://example/g9>") ;
    
    @Test public void gr_union_01()
    {
        List<Node> gnodes = list(gn1, gn2) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(3, x) ;
    }
    
    @Test public void gr_union_02()
    {
        List<Node> gnodes = list(gn1, gn2) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        Node s = NodeFactory.createURI("http://example/s") ; 
        long x = Iter.count(g.find(s, null, null)) ;
        assertEquals(3, x) ;
    }

    @Test public void gr_union_03()
    {
        List<Node> gnodes = list(gn1, gn2, gn9) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        Node o = NodeFactory.createLiteral("g2") ; 
        long x = Iter.count(g.find(null, null, o)) ;
        assertEquals(1, x) ;
    }
    
    @Test public void gr_union_04()
    {
        List<Node> gnodes = list(gn9) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(0, x) ;
    }

    @Test public void gr_union_05()
    {
        List<Node> gnodes = list() ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(0, x) ;
    }
    
    @Test public void gr_union_06()
    {
        List<Node> gnodes = list(gn1, gn1) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(2, x) ;
    }

    static <T> List<T> list(@SuppressWarnings("unchecked") T...x)
    {
        return Arrays.asList(x) ;
    }
}
