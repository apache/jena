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

import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

public abstract class UnionTransformTests extends BaseTest
{
    // Tests of patterns over the implicit union of al named graphs.
    // Two sets - for triples and for quads
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
        assertEquals(4, results.size()) ;
    }
 
//    @Test public void union_7()
//    {
//        List<Binding> results = exec("(graph <"+Quad.defaultGraphNodeGenerated+"> (bgp (<s2> ?p ?o)))") ;
//        assertEquals(1, results.size()) ;
//    }
    
    private List<Binding> exec(String pattern)
    {
        Op op = op(pattern) ;
        QueryIterator qIter = Algebra.exec(op, TestUnionGraph.dsg) ;
        return  Iter.toList(qIter) ;
    }
    
    protected abstract Op op(String pattern) ;
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