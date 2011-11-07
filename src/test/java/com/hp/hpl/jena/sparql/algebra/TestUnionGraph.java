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

package com.hp.hpl.jena.sparql.algebra;

import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;

public class TestUnionGraph extends BaseTest
{
    static String x = StrUtils.strjoinNL("(dataset",
                                         "  (graph (<s> <p> <o>) (<x> <p> <o>) (<x2> <p> <o1>) (<x2> <p> <o3>) (<x2> <p> <o3>) (<x2> <p> <o4>))",
                                         "  (graph <g1> (triple <s1> <p1> <o1>))",
                                         "  (graph <g2> (triple <s2> <p2> <o2>))",
                                         "  (graph <g3> (triple <s2> <p2> <o2>))", // Duplicate triple
                                         ")") ;
    static DatasetGraph dsg = BuilderGraph.buildDataset(SSE.parse(x)) ;
    
    @Test public void union_graph_triples_1()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<s2> ?p ?o)))", false) ;
        assertEquals(1, results.size()) ;
    }

    @Test public void union_graph_triples_2()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<x2> ?p ?o)))", false) ;
        assertEquals(0, results.size()) ;
    }

    @Test public void union_graph_quads_1()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<s2> ?p ?o)))", true) ;
        assertEquals(1, results.size()) ;
    }

    @Test public void union_graph_quads_2()
    {
        List<Binding> results = exec("(graph <"+Quad.unionGraph+"> (bgp (<x2> ?p ?o)))", true) ;
        assertEquals(0, results.size()) ;
    }

    private List<Binding> exec(String pattern, boolean applyQuad)
    {
        Op op = SSE.parseOp(pattern) ;
        if ( applyQuad )
            op = Algebra.toQuadForm(op) ;
        Op op2 = Algebra.unionDefaultGraph(op) ;
        QueryIterator qIter = Algebra.exec(op, TestUnionGraph.dsg) ;
        return Iter.toList(qIter) ;
    }

}
