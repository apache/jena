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
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

public abstract class AbstractTestUnionTransform extends BaseTest
{
    // Tests of patterns over the implicit union of all named graphs.
    // Two sets - for triples and for quads
    
    // TODO need multiple triple pattern BGPs. 
    
    @Test public void union_1()
    {
        List<Binding> results = exec("(bgp (?s ?p ?o))") ;
        assertEquals(2, results.size()) ;
    }
    
    @Test public void union_2()
    {
        List<Binding> results = exec("(bgp (<s2> ?p ?o))") ;
        assertEquals(1, results.size()) ;
    }
    
    @Test public void union_3()
    {
        List<Binding> results = exec("(graph <g1> (bgp (<s1> ?p ?o)))") ;
        assertEquals(1, results.size()) ;
    }

    @Test public void union_4()
    {
        List<Binding> results = exec("(graph <g1> (bgp (<s2> ?p ?o)))") ;
        assertEquals(0, results.size()) ;
    }

    @Test public void union_5()
    {
        List<Binding> results = exec("(graph <g2> (bgp (<s2> ?p ?o)))") ;
        assertEquals(1, results.size()) ;
    }

    @Test public void union_6()
    {
        List<Binding> results = exec("(graph <"+Quad.defaultGraphIRI+"> (bgp (<s2> ?p ?o)))") ;
        assertEquals(0, results.size()) ;
    }

    @Test public void union_7()
    {
        List<Binding> results = exec("(graph <"+Quad.defaultGraphIRI+"> (bgp (<x2> ?p ?o)))") ;
        assertEquals(3, results.size()) ;
    }
 
//    @Test public void union_7()
//    {
//        List<Binding> results = exec("(graph <"+Quad.defaultGraphNodeGenerated+"> (bgp (<s2> ?p ?o)))") ;
//        assertEquals(1, results.size()) ;
//    }
    
    private List<Binding> exec(String pattern)
    {
        Op op = op(pattern) ;
        QueryIterator qIter = Algebra.exec(op, TestUnionGraph.dsg1) ;
        return  Iter.toList(qIter) ;
    }
    
    protected abstract Op op(String pattern) ;
}
