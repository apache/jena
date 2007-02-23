/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.resultset.RSCompare;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileManager;

/** com.hp.hpl.jena.query.test.TestResultSet
 * 
 * @author Andy Seaborne
 * @version $Id: TestResultSet.java,v 1.21 2007/02/06 17:06:30 andy_seaborne Exp $
 */

public class TestResultSet extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestResultSet.class) ;
        ts.setName(Utils.classShortName(TestResultSet.class)) ;
        return ts ;
    }
    
    // Test reading and writing 
    public void test_RS_1()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsXML(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromXML(ins) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    public void test_RS_2()
    {
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsXML(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromXML(ins) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    // RDF
    
    public void test_RS_3()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        Model model = ResultSetFormatter.toModel(rs1) ;
        rs1.reset() ;
        ResultSet rs2 = ResultSetFactory.fromRDF(model) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    public void test_RS_4()
    {
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        Model model = ResultSetFormatter.toModel(rs1) ;
        rs1.reset() ;
        ResultSet rs2 = ResultSetFactory.fromRDF(model) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    // JSON
    
    public void test_RS_5()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsJSON(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromJSON(ins) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    public void test_RS_6()
    {
        ResultSetRewindable rs1 = make2Rewindable("x", Node.createURI("tag:local")) ;
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsJSON(arr, rs1) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromJSON(ins) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    // Test using the DAWG examples
    
    public void test_RS_7()
    {
        InputStream in = FileManager.get().open("testing/ResultSet/output.srx") ;
        ResultSetRewindable rs1 =  ResultSetFactory.makeRewindable(ResultSetFactory.fromXML(in)) ;
        
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.output(arr, rs1, ResultSetFormat.syntaxRDF_XML) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.load(ins, ResultSetFormat.syntaxRDF_XML) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    public void test_RS_8()
    {
        InputStream in = FileManager.get().open("testing/ResultSet/output.srx") ;
        ResultSetRewindable rs1 =  ResultSetFactory.makeRewindable(ResultSetFactory.fromXML(in)) ;
        
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        ResultSetFormatter.output(arr, rs1, ResultSetFormat.syntaxJSON) ;
        rs1.reset() ;
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.load(ins, ResultSetFormat.syntaxJSON) ;
        assertTrue(RSCompare.same(rs1, rs2)) ;
    }
    
    // Test comparison 
    public void test_RS_cmp_1()
    {
        ResultSetRewindable rs1 = new ResultSetMem() ;
        ResultSetRewindable rs2 = new ResultSetMem() ;
        assertTrue(RSCompare.sameOrdered(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;
        assertTrue(RSCompare.sameUnordered(rs1, rs2)) ;
    }
    
    public void test_RS_cmp_2()
    {
        ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
        ResultSet rs2 = new ResultSetMem() ;
        assertFalse(RSCompare.sameOrdered(rs1, rs2)) ;

    }

    public void test_RS_cmp_3()
    {
        ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
        ResultSet rs2 = new ResultSetMem() ;
        assertFalse(RSCompare.sameUnordered(rs1, rs2)) ;
    }


    
    public void test_RS_cmp_4()
    {
        ResultSet rs1 = make("x", Node.createURI("tag:local")) ;
        ResultSet rs2 = make("x", Node.createURI("tag:local")) ;
        assertTrue(RSCompare.sameOrdered(rs1, rs2)) ;
        assertTrue(RSCompare.sameUnordered(rs1, rs2)) ;
    }

    public void test_RS_cmp_5()
    {
        // Same variable, different values
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local:1")) ;
        ResultSetRewindable rs2 = makeRewindable("x", Node.createURI("tag:local:2")) ;
        assertFalse(RSCompare.sameOrdered(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;
        assertFalse(RSCompare.sameUnordered(rs1, rs2)) ;
    }

    public void test_RS_cmp_6()
    {
        // Different variable, same values
        ResultSetRewindable rs1 = makeRewindable("x", Node.createURI("tag:local")) ;
        ResultSetRewindable rs2 = makeRewindable("y", Node.createURI("tag:local")) ;
        assertFalse(RSCompare.sameOrdered(rs1, rs2)) ;
        rs1.reset() ;
        rs2.reset() ;
        assertFalse(RSCompare.sameUnordered(rs1, rs2)) ;
    }
    
    
    
    // -------- Support functions
    
    private ResultSet make(String var, Node val)
    {
        Binding b = new BindingMap() ;
        b.add(Var.alloc(var), val) ;
        List vars = new ArrayList() ;
        vars.add(var) ;
        QueryIterator qIter = new QueryIterSingleton(b, null) ;
        ResultSet rs = new ResultSetStream(vars, null, qIter) ;
        return rs ;
    }

    private ResultSet make2(String var, Node val)
    {
        Binding b1 = new BindingMap() ;
        b1.add(Var.alloc(var), val) ;
        Binding b2 = new BindingMap() ;
        b2.add(Var.alloc(var), val) ;
        
        List vars = new ArrayList() ;
        vars.add(var) ;

        List solutions = new ArrayList() ;
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
        Binding b = new BindingMap() ;
        
        b.add(Var.alloc(var1), val1) ;
        b.add(Var.alloc(var2), val2) ;
        
        List vars = new ArrayList() ;
        vars.add(var1) ;
        vars.add(var2) ;
        
        QueryIterator qIter = new QueryIterSingleton(b, null) ;
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

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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