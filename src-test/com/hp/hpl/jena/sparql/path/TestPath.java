/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import static org.junit.Assert.* ;

//import junit.framework.Test;
//import junit.framework.TestCase;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.path.Path;
import com.hp.hpl.jena.sparql.path.PathEval;
import com.hp.hpl.jena.sparql.path.PathParser;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.builders.BuilderPath;
import com.hp.hpl.jena.sparql.sse.writers.WriterPath;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

public class TestPath
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestPath.class) ;
//        TestSuite ts = new TestSuite(TestPath.class) ;
//        ts.setName(Utils.classShortName(TestPath.class)) ;
//        return ts ;
    }

    static Graph graph1 = GraphUtils.makeDefaultGraph() ;
    static Graph graph2 = GraphUtils.makeDefaultGraph() ;
    
    static Node n1 = Node.createURI("n1") ;
    static Node n2 = Node.createURI("n2") ;
    static Node n3 = Node.createURI("n3") ;
    static Node n4 = Node.createURI("n4") ;
    static Node p = Node.createURI("http://example/p") ;
    static Node q = Node.createURI("http://example/q") ;
    static PrefixMapping pmap  = new PrefixMappingImpl() ;
    
    static {
        pmap.setNsPrefixes(PrefixMapping.Standard) ;
        pmap.setNsPrefix("", "http://example/") ;
        
        // A linear path in the graph
        graph1.add(new Triple(n1, p, n2)) ;
        graph1.add(new Triple(n2, p, n3)) ;
        graph1.add(new Triple(n3, p, n4)) ;
        
        // A DAG
        graph2.add(new Triple(n1, p, n2)) ;
        graph2.add(new Triple(n1, p, n3)) ;
        graph2.add(new Triple(n2, q, n4)) ;
        graph2.add(new Triple(n3, q, n4)) ;
    }
    
    // ----
    
    @Test public void parsePath_01()           { parse(":p") ; }
    @Test public void parsePath_02()           { parse("(:p)") ; }
    @Test public void parsePath_03()           { parse("^:p") ; }
    @Test public void parsePath_04()           { parse(":p*") ; }
    @Test public void parsePath_05()           { parse(":p+") ; }
    @Test public void parsePath_06()           { parse(":p?") ; }
    
    @Test public void parsePath_10()           { parse(":p/:q") ; }
    @Test public void parsePath_11()           { parse(":p|:q") ; }
    @Test public void parsePath_12()           { parse(":p{1}") ; }
    @Test public void parsePath_13()           { parse(":p{1,}") ; }
    @Test public void parsePath_14()           { parse(":p{1,2}") ; }
    
    @Test public void parsePath_15()           { parse(":p^:q") ; }
    @Test public void parsePath_16()           { parse("^:p^:q") ; }
    @Test public void parsePath_17()           { parse("^:p/:q") ; }
    @Test public void parsePath_18()           { parse("^(:p/:q)") ; }
    @Test public void parsePath_19()           { parse("^(:p^:q)") ; }
    @Test public void parsePath_20()           { parse(":p^(:q/:r)") ; }
    
    @Test public void parsePathErr_01()        { parse("", false) ; }
    @Test public void parsePathErr_02()        { parse("()", false) ; }
    @Test public void parsePathErr_03()        { parse(":p :q", false) ; }
    
    // Check we get the form on the right for the expression on the right.
    @Test public void parseEquals_1()         {  parse("(:p)",        ":p") ; }
    @Test public void parseEquals_2()         {  parse(":p/:q/:r",    "(:p/:q)/:r") ; }
    @Test public void parseEquals_3()         {  parse("^:p^:q^:r",   "(^:p^:q)^:r") ; }
    @Test public void parseEquals_4()         {  parse(":p/(:q/:r)",  ":p/(:q/:r)") ; }
    @Test public void parseEquals_5()         {  parse("(:p/:q)|:r",  ":p/:q|:r") ; }
    @Test public void parseEquals_6()         {  parse(":p|(:q/:r)",  ":p|:q/:r") ; }
    @Test public void parseEquals_7()         {  parse("^:p/:q",      "(^:p)/:q") ; }

    // ----
    
    private void parse(String string) { parse(string, true) ; }
    
    private void parse(String string, boolean expectLegal)
    {
        Prologue prologue = new Prologue(pmap) ;
        Path p = null ;
        try {
            p = PathParser.parse(string, prologue) ;
//            System.out.println(string+" ==> "+p.toString(new Prologue(pmap))) ;
//            System.out.println(PathWriterSSE.asString(p, new Prologue(pmap))) ;
            if ( ! expectLegal )
                fail("Expected error; "+string) ;
        } catch (QueryParseException ex)
        {
            if ( expectLegal )
                fail("Expected success: "+string+": "+ex.getMessage()) ;
            return ;
        }
        String x = p.toString(prologue) ;
        Path p2 = PathParser.parse(x, prologue) ;
        assertEquals(p, p2) ;
        
        String sse = WriterPath.asString(p, prologue) ;
        Item item = SSE.parseItem(sse, pmap) ;
        p2 = BuilderPath.buildPath(item) ;
        assertEquals(p, p2) ;
    }
    
    
    private static void parse(String path1, String path2)
    {
        Prologue prologue = new Prologue(pmap) ;
        Path p1 = PathParser.parse(path1, prologue) ;
        Path p2 = PathParser.parse(path2, prologue) ;
        assertEquals(p1, p2) ;
    }


    @Test public void path_01()   { test(graph1, n1,   ":p",          new Node[]{n2}) ; }
    @Test public void path_02()   { test(graph1, n1,   ":p{0}",       new Node[]{n1}) ; }
    @Test public void path_03()   { test(graph1, n1,   ":p{1}",       new Node[]{n2}) ; }
    @Test public void path_04()   { test(graph1, n1,   ":p{2}",       new Node[]{n3}) ; }
    @Test public void path_05()   { test(graph1, n1,   ":p{0,1}",     new Node[]{n1, n2}) ; }
    @Test public void path_06()   { test(graph1, n1,   ":p{0,2}",     new Node[]{n1,n2,n3}) ; }
    @Test public void path_07()   { test(graph1, n1,   ":p{1,2}",     new Node[]{n2, n3}) ; }
    @Test public void path_08()   { test(graph1, n1,   ":p{9,9}",     new Node[]{}) ; }
    @Test public void path_09()   { test(graph1, n1,   ":p{0,9}",     new Node[]{n1,n2,n3,n4}) ; }
    @Test public void path_10()   { test(graph1, n1,   ":p*",         new Node[]{n1,n2,n3,n4}) ; }
    @Test public void path_11()   { test(graph1, n1,   ":p+",         new Node[]{n2,n3,n4}) ; }
    @Test public void path_12()   { test(graph1, n1,   ":p?",         new Node[]{n1,n2}) ; }
    @Test public void path_13()   { test(graph1, n1,   ":p/:p",       new Node[]{n3}) ; }
    @Test public void path_14()   { test(graph1, n2,   "^:p",         new Node[]{n1}) ; }
    @Test public void path_15()   { test(graph1, n2,   "^:p^:p",      new Node[]{}) ; }
    @Test public void path_16()   { test(graph1, n4,   "^:p^:p",      new Node[]{n2}) ; }
    @Test public void path_17()   { test(graph1, n4,   "^(:p/:p)",    new Node[]{n2}) ; }
    @Test public void path_18()   { test(graph1, n2,   "^:p/:p",      new Node[]{n2}) ; }
    

    @Test public void path_20()   { test(graph2, n1,   ":p",          new Node[]{n2,n3}) ; }
    @Test public void path_21()   { test(graph2, n1,   ":p/:q",       new Node[]{n4}) ; }
    @Test public void path_22()   { test(graph2, n2,   "^:p|:q",      new Node[]{n1,n4}) ; }
    @Test public void path_23()   { test(graph2, n2,   "^(:p|^:q)*",  new Node[]{n1,n2,n4}) ; }

    @Test public void path_24()   { testReverse(graph1, n2,   ":p",          new Node[]{n1}) ; }
    @Test public void path_25()   { testReverse(graph1, n3,   ":p/:p",       new Node[]{n1}) ; }
    // ----
    private static void test(Graph graph, Node start, String string, Node[] expectedNodes)
    {
       test(graph, start, string, expectedNodes, true) ;
    }
    
    private static void testReverse(Graph graph, Node start, String string, Node[] expectedNodes)
    {
       test(graph, start, string, expectedNodes, false) ;
    }

    private static void test(Graph graph, Node start, String string, Node[] expectedNodes, boolean directionForward)
    {
        Path p = PathParser.parse(string, pmap) ;
        Iterator<Node> resultsIter = 
            directionForward ? PathEval.eval(graph, start, p) : PathEval.evalReverse(graph, start, p) ; 
        Set<Node> results = new HashSet<Node>() ;
        for ( ; resultsIter.hasNext() ; )
            results.add( resultsIter.next() ) ;

        Set<Node> expected = new HashSet<Node>(Arrays.asList(expectedNodes)) ;
        assertEquals(expected, results) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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