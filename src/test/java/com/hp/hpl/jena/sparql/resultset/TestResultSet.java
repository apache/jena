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

package com.hp.hpl.jena.sparql.resultset;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.ResultSetRewindable ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.ResultSetUtils ;

public class TestResultSet extends BaseTest
{
    // Test reading, writing and comparison
    @Test public void test_RS_1()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsXML(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromXML(ins) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    @Test public void test_RS_1_str()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        String x = ResultSetFormatter.asXMLString(rs1) ;
        rs1.reset() ;
        ResultSet rs2 = ResultSetFactory.fromXML(x) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }

    @Test public void test_RS_2()
    {
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsXML(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromXML(ins) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    @Test public void test_RS_2_str()
    {
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        String x = ResultSetFormatter.asXMLString(rs1) ;
        rs1.reset() ;
        ResultSet rs2 = ResultSetFactory.fromXML(x) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }

    // RDF
    
    @Test public void test_RS_3()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        Model model = ResultSetFormatter.toModel(rs1) ;
        rs1.reset() ;
        ResultSet rs2 = ResultSetFactory.fromRDF(model) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    @Test public void test_RS_4()
    {
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        Model model = ResultSetFormatter.toModel(rs1) ;
        rs1.reset() ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.fromRDF(model)) ;
        boolean b = ResultSetCompare.equalsByTerm(rs1, rs2) ;
        if ( ! b )
        {
            rs1.reset() ;
            rs2.reset() ;
            ResultSetFormatter.out(rs1) ;
            ResultSetFormatter.out(rs2) ;
        }
        
        assertTrue(b) ;
    }
    
    // JSON
    
    @Test public void test_RS_5()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsJSON(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromJSON(ins) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    @Test public void test_RS_6()
    {
        ResultSetRewindable rs1 = make2Rewindable("x", Node.createURI("tag:local")) ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsJSON(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromJSON(ins) ;    // Test using the DAWG examples
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    // Into some format.
    
    @Test public void test_RS_7()
    {
        ResultSet rs = ResultSetFactory.load("testing/ResultSet/output.srx") ;
        test_RS_fmt(rs, ResultsFormat.FMT_RS_XML, true) ;
    }
    
    @Test public void test_RS_8()
    {
        ResultSet rs = ResultSetFactory.load("testing/ResultSet/output.srx") ;
        test_RS_fmt(rs, ResultsFormat.FMT_RS_JSON, true) ;
    }
    
    @Test public void test_RS_9()
    {
        ResultSet rs = ResultSetFactory.load("testing/ResultSet/output.srx") ;
        test_RS_fmt(rs, ResultsFormat.FMT_RDF_XML, false) ;
    }
    
    @Test public void test_RS_10()
    {
        ResultSet rs = ResultSetFactory.load("testing/ResultSet/output.srx") ;
        for ( ; rs.hasNext(); rs.next()) { }
        // We should be able to call hasNext() as many times as we want!
        assertFalse(rs.hasNext());
    }

    @Test public void test_RS_union_1() 
    {
    	ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
    	ResultSet rs2 = make("x", Node.createURI("tag:local")) ;
    	ResultSet rs3 = make2("x", Node.createURI("tag:local")) ;
    	assertTrue(ResultSetCompare.equalsByTerm(rs3, ResultSetUtils.union(rs1, rs2))) ;
    }

    @Test(expected = ResultSetException.class) 
    public void test_RS_union_2() 
    {
    	ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
    	ResultSet rs2 = make("y", Node.createURI("tag:local")) ;
    	ResultSetUtils.union(rs1, rs2) ;
    }

    private void test_RS_fmt(ResultSet rs, ResultsFormat fmt, boolean ordered)
    {
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(rs) ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.output(arr, rs1, fmt) ;
        byte bytes[] = arr.toByteArray() ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(bytes) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(ins, fmt)) ;

        // Ordered? Unordered?
        boolean b = ResultSetCompare.equalsByTerm(rs1, rs2) ;
        if ( ordered )
        {
            rs1.reset() ;
            rs2.reset() ;
            b = b & ResultSetCompare.equalsByTerm(rs1, rs2) ;
        }
        
        if ( !b )
        {
            System.out.println(new String(bytes)) ;
            rs1.reset() ;
            rs2.reset() ;
            ResultSetFormatter.out(rs1) ;
            ResultSetFormatter.out(rs2) ;
        }

        assertTrue(b) ;
    }
    // Test comparison 
    @Test public void test_RS_cmp_1()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        ResultSetRewindable rs2 = new ResultSetMem() ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    @Test public void test_RS_cmp_2()
    {
        ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
        ResultSet rs2 = new ResultSetMem() ;
        assertFalse(ResultSetCompare.equalsByTerm(rs1, rs2)) ;

    }

    @Test public void test_RS_cmp_3()
    {
        ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
        ResultSet rs2 = new ResultSetMem() ;
        assertFalse(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }

    @Test public void test_RS_cmp_4()
    {
        ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
        ResultSet rs2 = make("x", Node.createURI("tag:local")) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }

    @Test public void test_RS_cmp_5()
    {
        // Same variable, different values
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local:1")) ;
        ResultSetRewindable rs2 = makeRewindable("x", Node.createURI("tag:local:2")) ;
        assertFalse(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;
        assertFalse(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }

    @Test public void test_RS_cmp_6()
    {
        // Different variable, same values
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        ResultSetRewindable rs2 = makeRewindable("y", Node.createURI("tag:local")) ;
        assertFalse(ResultSetCompare.equalsByTermAndOrder(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;
        assertFalse(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
    }
    
    // Value based 
    @Test public void test_RS_cmp_value_1()
    {
        ResultSetRewindable rs1 = makeRewindable("x", NodeFactory.parseNode("123")) ;
        ResultSetRewindable rs2 = makeRewindable("x", NodeFactory.parseNode("0123")) ;
        assertFalse(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
        assertTrue(ResultSetCompare.equalsByValue(rs1, rs2)) ;
    }
    
    // ---- Isomorphism.
    
    /* This is from the DAWG test suite.
     * Result set 1: 
     *   ---------------
     *   | x    | y    |
     *   ===============
     *   | _:b0 | _:b1 |
     *   | _:b2 | _:b3 |
     *   | _:b1 | _:b0 |
     *   ---------------
     * Result set 2: 
     *   ---------------
     *   | x    | y    |
     *   ===============
     *   | _:b1 | _:b0 |
     *   | _:b3 | _:b2 |
     *   | _:b2 | _:b3 |
     *   ---------------
     */
    
    // nasty result set.
    // These are the same but the first row of rs2$ throws in a wrong mapping of b0/c1

    // Right mapping is:
    // b0->c3, b1->c2, b2->c1, b3->c0
    // Currently we get, workign simply top to bottom, no backtracking:
    // b0->c1, b1->c0, b2->c3, b3->c2, then last row fails as _:b1 is mapped to c0, b0 to c1 not (c2, c3) 
    
    private static String[] rs1$ = {
        "(resultset (?x ?y)",
        "   (row (?x _:b0) (?y _:b1))",
        "   (row (?x _:b2) (?y _:b3))",
        "   (row (?x _:b1) (?y _:b0))",
        ")"} ;
    private static String[] rs2$ = {
        "(resultset (?x ?y)",
        "   (row (?x _:c1) (?y _:c0))",
        "   (row (?x _:c3) (?y _:c2))",
        "   (row (?x _:c2) (?y _:c3))",
        ")"} ;
   
    @Test public void test_RS_iso_1()       { isotest(rs1$, rs2$) ; }
    
    private void isotest(String[] rs1$2, String[] rs2$2)
    {
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(BuilderResultSet.build(SSE.parseItem(StrUtils.strjoinNL(rs1$)))) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(BuilderResultSet.build(SSE.parseItem(StrUtils.strjoinNL(rs2$)))) ;
        assertTrue(ResultSetCompare.isomorphic(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;   
        assertTrue(ResultSetCompare.equalsByTerm(rs1, rs2)) ;
        assertTrue(ResultSetCompare.equalsByValue(rs1, rs2)) ;
    }

    // -------- Support functions
    
    private ResultSet make(String var, Node val)
    {
        BindingMap b = BindingFactory.create() ;
        b.add(Var.alloc(var), val) ;
        List<String> vars = new ArrayList<String>() ;
        vars.add(var) ;
        QueryIterator qIter = QueryIterSingleton.create(b, null) ;
        ResultSet rs = new ResultSetStream(vars, null, qIter) ;
        return rs ;
    }

    private ResultSet make2(String var, Node val)
    {
        BindingMap b1 = BindingFactory.create() ;
        b1.add(Var.alloc(var), val) ;
        BindingMap b2 = BindingFactory.create() ;
        b2.add(Var.alloc(var), val) ;
        
        List<String> vars = new ArrayList<String>() ;
        vars.add(var) ;

        List<Binding> solutions = new ArrayList<Binding>() ;
        solutions.add(b1) ;
        solutions.add(b2) ;
        
        QueryIterator qIter = new QueryIterPlainWrapper(solutions.iterator(), null) ;
        ResultSet rs = new ResultSetStream(vars, null, qIter) ;
        return rs ;
    }

    
    private ResultSetRewindable makeRewindable(String var, Node val)
    {
        ResultSet rs = make(var, val) ;
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs) ;
        return rsw ;
    }
    
    private ResultSetRewindable make2Rewindable(String var, Node val)
    {
        ResultSet rs = make2(var, val) ;
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs) ;
        return rsw ;
    }
    
    private ResultSet make(String var1, Node val1, String var2, Node val2 )
    {
        BindingMap b = BindingFactory.create() ;
        
        b.add(Var.alloc(var1), val1) ;
        b.add(Var.alloc(var2), val2) ;
        
        List<String> vars = new ArrayList<String>() ;
        vars.add(var1) ;
        vars.add(var2) ;
        
        QueryIterator qIter = QueryIterSingleton.create(b, null) ;
        ResultSet rs = new ResultSetStream(vars, null, qIter) ;
        return rs ; 
    }

    private ResultSetRewindable makeRewindable(String var1, Node val1, String var2, Node val2 )
    {
        ResultSet rs = make(var1, val1, var2, val2) ;
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs) ;
        return rsw ;
    }
    
}
