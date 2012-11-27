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

package com.hp.hpl.jena.sparql.algebra.uniongraph;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;

public class TestUnionGraph extends BaseTest
{
    static String x1 = StrUtils.strjoinNL("(dataset",
                                         "  (graph (<s> <p> <o>) (<x> <p> <o>) (<x2> <p> <o1>) (<x2> <p> <o3>) (<x2> <p> <o4>))",
                                         "  (graph <g1> (triple <s1> <p1> <o1>))",
                                         "  (graph <g2> (triple <s2> <p2> <o2>))",
                                         "  (graph <g3> (triple <s2> <p2> <o2>))", // Duplicate triple
                                         ")") ;
    static DatasetGraph dsg1 = BuilderGraph.buildDataset(SSE.parse(x1)) ;

    static String x2 = StrUtils.strjoinNL("(dataset",
                                         "  (graph (<s> <p1> 10) (<s> <p1> 11) (<s> <p2> 20) )",
                                         "  (graph <g1> (<s1> <p1> <z>) (<z> <p2> 1) )",
                                         "  (graph <g2> (<s1> <p1> <z>) (<z> <p2> 2) )",
                                         "  (graph <g3> (<s>  <p1> 10))",
                                         ")") ;
    
    static DatasetGraph dsg2 = BuilderGraph.buildDataset(SSE.parse(x2)) ;

    
    @Test public void union_graph_triples_1()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<s2> ?p ?o)))", false, dsg1) ;
        assertEquals(1, results.size()) ;
    }

    @Test public void union_graph_triples_2()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<x2> ?p ?o)))", false, dsg1) ;
        assertEquals(0, results.size()) ;
    }

    @Test public void union_graph_quads_1()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<s2> ?p ?o)))", true, dsg1) ;
        assertEquals(1, results.size()) ;
    }

    @Test public void union_graph_quads_2()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<x2> ?p ?o)))", true, dsg1) ;
        assertEquals(0, results.size()) ;
    }

    // Patterns
    @Test public void union_graph_triples_10()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<s1> ?p ?z) (?z ?q ?o) ))", false, dsg2) ;
        assertEquals(2, results.size()) ;
    }

    @Test public void union_graph_quads_10()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<s1> ?p ?z) (?z ?q ?o) ))", false, dsg2) ;
        assertEquals(2, results.size()) ;
    }

    
    
    static private List<Binding> exec(String pattern, boolean applyQuad, DatasetGraph dsg)
    {
        Op op = SSE.parseOp(pattern) ;
        if ( applyQuad )
            op = Algebra.toQuadForm(op) ;
        Op op2 = Algebra.unionDefaultGraph(op) ;
        QueryIterator qIter = Algebra.exec(op, TestUnionGraph.dsg1) ;
        return Iter.toList(qIter) ;
    }
    
    
}
