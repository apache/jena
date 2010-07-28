/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */