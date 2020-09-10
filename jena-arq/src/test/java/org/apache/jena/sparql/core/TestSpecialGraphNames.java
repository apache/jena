/**
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

package org.apache.jena.sparql.core;

import static org.apache.jena.sparql.core.TestSpecialGraphNames.Mode.QUADBLOCKS ;
import static org.apache.jena.sparql.core.TestSpecialGraphNames.Mode.QUADS ;
import static org.apache.jena.sparql.core.TestSpecialGraphNames.Mode.TRIPLES ;
import static org.junit.Assert.assertEquals ;

import java.util.Arrays ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Creator ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.builders.BuilderGraph ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

/** Test special graph names. */
@RunWith(Parameterized.class)
public class TestSpecialGraphNames {
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Creator<DatasetGraph> datasetGeneralMaker = ()-> DatasetGraphFactory.createGeneral() ; 
        Creator<DatasetGraph> datasetTxnMemMaker  = ()-> DatasetGraphFactory.createTxnMem() ;
        Creator<DatasetGraph> datasetMemMaker     = ()-> DatasetGraphFactory.create() ;
        return Arrays.asList(new Object[][] {
            { "General",  datasetGeneralMaker },
            { "Plain",    datasetMemMaker} ,
            { "TxnMem",   datasetTxnMemMaker}
            });
    }
    
    static enum Mode { TRIPLES, QUADS, QUADBLOCKS } 
    
    static String x1 = StrUtils.strjoinNL("(dataset",
                                          "  (graph (<s> <p> <x>) (<x> <p> <o>) (<x2> <p> <o1>) (<x2> <p> <o3>) (<x2> <p> <o4>))",
                                          "  (graph <g1> (<s1> <p1> <s1>) (<s1> <p1> <s2>))",
                                          "  (graph <g2> (triple <s2> <p2> <o2>) (triple <s2> <p3> <o3>))",
                                          "  (graph <g3> (triple <s2> <p2> <o2>))", // Duplicate triple
                                          ")") ;
    private DatasetGraph dsg ;
    
    public TestSpecialGraphNames(String label, Creator<DatasetGraph> maker) {
        this.dsg = BuilderGraph.buildDataset(maker.create(), SSE.parse(x1)) ;
    }

    @Test
    public void union_dft_1t() {
        union_dft_1(TRIPLES) ;
    }
    
    @Test
    public void union_dft_1q() {
        union_dft_1(QUADS) ;
    }
    
    private void union_dft_1(Mode mode) {
        Op op = op("(bgp (?s ?p ?o))", mode) ;
        List<Binding> results = exec(op) ;
        assertEquals(5, results.size()) ;
        @SuppressWarnings("deprecation")
        Op op2 = Algebra.unionDefaultGraph(op) ;
        List<Binding> results2 = exec(op2) ;
        assertEquals(4, results2.size()) ;
    }
    
    @Test
    public void graph_union_1t() {
        List<Binding> results = exec("(graph <" + Quad.unionGraph.getURI() + "> (bgp (<s2> ?p ?o)))", TRIPLES) ;
        assertEquals(2, results.size()) ;
    }

    @Test
    public void graph_union_1q() {
        List<Binding> results = exec("(graph <" + Quad.unionGraph.getURI() + "> (bgp (<s2> ?p ?o)))", QUADS) ;
        assertEquals(2, results.size()) ;
    }

    @Test
    public void graph_union_2t() {
        List<Binding> results = exec("(graph <" + Quad.unionGraph.getURI() + "> (bgp (<s1> ?p ?o) (?o ?q ?z)  ))", TRIPLES) ;
        assertEquals(4, results.size()) ;
    }

    @Test
    public void graph_union_2q() {
        List<Binding> results = exec("(graph <" + Quad.unionGraph.getURI() + "> (bgp (<s1> ?p ?o) (?o ?q ?z)  ))", QUADS) ;
        assertEquals(4, results.size()) ;
    }

    @Test
    public void graph_dft_1t() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphIRI.getURI() + "> (bgp (<s2> ?p ?o)))", TRIPLES) ;
        assertEquals(0, results.size()) ;
    }

    @Test
    public void graph_dft_1q() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphIRI.getURI() + "> (bgp (<s2> ?p ?o)))", QUADS) ;
        assertEquals(0, results.size()) ;
    }

    @Test
    public void graph_dft_2t() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphIRI.getURI() + "> (bgp (<s> ?p ?o)))", TRIPLES) ;
        assertEquals(1, results.size()) ;
    }

    @Test
    public void graph_dft_2q() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphIRI.getURI() + "> (bgp (<s> ?p ?o)))", QUADS) ;
        assertEquals(1, results.size()) ;
    }

    @Test
    public void graph_dft_3t() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphIRI.getURI() + "> (bgp (?x ?p ?z) (?z ?q ?y) ))", TRIPLES) ;
        assertEquals(1, results.size()) ;
    }

    @Test
    public void graph_dft_3q() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphIRI.getURI() + "> (bgp (?x ?p ?z) (?z ?q ?y) ))", QUADS) ;
        assertEquals(1, results.size()) ;
    }

    @Test
    public void graph_dftg_1t() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphNodeGenerated.getURI() + "> (bgp (<s2> ?p ?o)))", TRIPLES) ;
        assertEquals(0, results.size()) ;
    }
    
    @Test
    public void graph_dftg_2t() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphNodeGenerated.getURI() + "> (bgp (<x2> ?p ?o)))", TRIPLES) ;
        assertEquals(3, results.size()) ;
    }
    
    @Test
    public void graph_dftg_1q() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphNodeGenerated.getURI() + "> (bgp (<s2> ?p ?o)))", QUADS) ;
        assertEquals(0, results.size()) ;
    }

    @Test
    public void graph_dftg_2q() {
        List<Binding> results = exec("(graph <" + Quad.defaultGraphNodeGenerated.getURI() + "> (bgp (<x2> ?p ?o)))", QUADS) ;
        assertEquals(3, results.size()) ;
    }

    @Test
    public void direct_3t() {
        List<Binding> results = exec("(graph <g1> (bgp (<s1> ?p ?o)))", TRIPLES) ;
        assertEquals(2, results.size()) ;
    }

    @Test
    public void direct_3q() {
        List<Binding> results = exec("(graph <g1> (bgp (<s1> ?p ?o)))", QUADS) ;
        assertEquals(2, results.size()) ;
    }

    @Test
    public void direct_4() {
        List<Binding> results = exec("(graph <g1> (bgp (<s2> ?p ?o)))", TRIPLES) ;
        assertEquals(0, results.size()) ;
    }

    @Test
    public void direct_5() {
        List<Binding> results = exec("(graph <g2> (bgp (<s2> ?p ?o)))", TRIPLES) ;
        assertEquals(2, results.size()) ;
    }
    
    @Test
    public void minus_1() {
        List<Binding> results = exec("(minus (bgp (?s ?p ?o)) (bgp (<x2> ?p ?o)))", TRIPLES) ;
        assertEquals(2, results.size()) ;
    }
    
    @Test
    public void minus_2() {
        List<Binding> results = exec("(minus (bgp (?s ?p ?o)) (bgp (<x2> ?p ?o)))", QUADS) ;
        assertEquals(2, results.size()) ;
    }
    
    @Test
    public void minus_3() {
        List<Binding> results = exec("(minus (bgp (?s ?p ?o)) (bgp (<x2> ?p ?o)))", QUADBLOCKS) ;
        assertEquals(2, results.size()) ;
    }
    
    @Test
    public void filter_exists_1() {
        List<Binding> results = exec("(filter (exists (bgp (?s <p> <o>))) (bgp (?s ?p ?o)))", TRIPLES) ;
        assertEquals(1, results.size()) ;
    }
    
    @Test
    public void filter_exists_2() {
        List<Binding> results = exec("(filter (exists (bgp (?s <p> <o>))) (bgp (?s ?p ?o)))", QUADS) ;
        assertEquals(1, results.size()) ;
    }
    
    @Test
    public void filter_exists_3() {
        List<Binding> results = exec("(filter (exists (bgp (?s <p> <o>))) (bgp (?s ?p ?o)))", QUADBLOCKS) ;
        assertEquals(1, results.size()) ;
    }
    
    @Test
    public void filter_notexists_1() {
        List<Binding> results = exec("(filter (notexists (bgp (?s <p> <o>))) (bgp (?s ?p ?o)))", TRIPLES) ;
        assertEquals(4, results.size()) ;
    }
    
    @Test
    public void filter_notexists_2() {
        List<Binding> results = exec("(filter (notexists (bgp (?s <p> <o>))) (bgp (?s ?p ?o)))", QUADS) ;
        assertEquals(4, results.size()) ;
    }
    
    @Test
    public void filter_notexists_3() {
        List<Binding> results = exec("(filter (notexists (bgp (?s <p> <o>))) (bgp (?s ?p ?o)))", QUADBLOCKS) ;
        assertEquals(4, results.size()) ;
    }

    private List<Binding> exec(String string, Mode mode) {
        Op op = op(string, mode) ;
        return exec(op) ;
    }
    
    private List<Binding> exec(Op op) {
        QueryIterator qIter = Algebra.exec(op, dsg) ;
        return Iter.toList(qIter) ;
    }

    protected Op op(String pattern, Mode mode) {
        Op op = SSE.parseOp(pattern) ;
        if ( mode == Mode.QUADS )
            op = Algebra.toQuadForm(op) ;
        else if ( mode == Mode.QUADBLOCKS )
            op = Algebra.toQuadBlockForm(op) ;
        return op ;
    }
}
