/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

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
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

public class TestPath extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestPath.class) ;
        ts.setName(Utils.classShortName(TestPath.class)) ;
        return ts ;
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
    
    public void testParsePath_01()           { parse(":p") ; }
    public void testParsePath_02()           { parse("(:p)") ; }
    public void testParsePath_03()           { parse("^:p") ; }
    public void testParsePath_04()           { parse(":p*") ; }
    public void testParsePath_05()           { parse(":p+") ; }
    public void testParsePath_06()           { parse(":p?") ; }
    
    public void testParsePath_10()           { parse(":p/:q") ; }
    public void testParsePath_11()           { parse(":p|:q") ; }
    public void testParsePath_12()           { parse(":p{1}") ; }
    public void testParsePath_13()           { parse(":p{1,}") ; }
    public void testParsePath_14()           { parse(":p{1,2}") ; }
    
    public void testParsePath_15()           { parse(":p^:q") ; }
    public void testParsePath_16()           { parse("^:p^:q") ; }
    public void testParsePath_17()           { parse("^:p/:q") ; }
    public void testParsePath_18()           { parse("^(:p/:q)") ; }
    public void testParsePath_19()           { parse("^(:p^:q)") ; }
    public void testParsePath_20()           { parse(":p^(:q/:r)") ; }
    
    public void testParsePathErr_01()        { parse("", false) ; }
    public void testParsePathErr_02()        { parse("()", false) ; }
    public void testParsePathErr_03()        { parse(":p :q", false) ; }
    
    // Bracketted form on the left
    public void testParseEquals_1()         {  parse("(:p)",        ":p") ; }
    public void testParseEquals_2()         {  parse(":p/(:q/:r)",  ":p/:q/:r") ; }
    public void testParseEquals_3()         {  parse("(:p/:q)|:r",  ":p/:q|:r") ; }
    public void testParseEquals_4()         {  parse(":p|(:q/:r)",  ":p|:q/:r") ; }
    public void testParseEquals_5()         {  parse("^:p/:q",       "(^:p)/:q") ; }
    

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


    public void testPath_01()   { test(graph1, n1,   ":p",          new Node[]{n2}) ; }
    public void testPath_02()   { test(graph1, n1,   ":p{0}",       new Node[]{n1}) ; }
    public void testPath_03()   { test(graph1, n1,   ":p{1}",       new Node[]{n2}) ; }
    public void testPath_04()   { test(graph1, n1,   ":p{2}",       new Node[]{n3}) ; }
    public void testPath_05()   { test(graph1, n1,   ":p{0,1}",     new Node[]{n1, n2}) ; }
    public void testPath_06()   { test(graph1, n1,   ":p{0,2}",     new Node[]{n1,n2,n3}) ; }
    public void testPath_07()   { test(graph1, n1,   ":p{1,2}",     new Node[]{n2, n3}) ; }
    public void testPath_08()   { test(graph1, n1,   ":p{9,9}",     new Node[]{}) ; }
    public void testPath_09()   { test(graph1, n1,   ":p{0,9}",     new Node[]{n1,n2,n3,n4}) ; }
    public void testPath_10()   { test(graph1, n1,   ":p*",         new Node[]{n1,n2,n3,n4}) ; }
    public void testPath_11()   { test(graph1, n1,   ":p+",         new Node[]{n2,n3,n4}) ; }
    public void testPath_12()   { test(graph1, n1,   ":p?",         new Node[]{n1,n2}) ; }
    public void testPath_13()   { test(graph1, n1,   ":p/:p",       new Node[]{n3}) ; }
    public void testPath_14()   { test(graph1, n2,   "^:p",         new Node[]{n1}) ; }
    public void testPath_15()   { test(graph1, n2,   "^:p^:p",      new Node[]{}) ; }
    public void testPath_16()   { test(graph1, n4,   "^:p^:p",      new Node[]{n2}) ; }
    public void testPath_17()   { test(graph1, n4,   "^(:p/:p)",    new Node[]{n2}) ; }
    public void testPath_18()   { test(graph1, n2,   "^:p/:p",      new Node[]{n2}) ; }
    

    public void testPath_20()   { test(graph2, n1,   ":p",          new Node[]{n2,n3}) ; }
    public void testPath_21()   { test(graph2, n1,   ":p/:q",       new Node[]{n4}) ; }
    public void testPath_22()   { test(graph2, n2,   "^:p|:q",      new Node[]{n1,n4}) ; }
    public void testPath_23()   { test(graph2, n2,   "^(:p|^:q)*",  new Node[]{n1,n2,n4}) ; }

    public void testPath_24()   { testReverse(graph1, n2,   ":p",          new Node[]{n1}) ; }
    public void testPath_25()   { testReverse(graph1, n3,   ":p/:p",       new Node[]{n1}) ; }
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
        Iterator resultsIter = 
            directionForward ? PathEval.eval(graph, start, p) : PathEval.evalReverse(graph, start, p) ; 
        Set results = new HashSet() ;
        for ( ; resultsIter.hasNext() ; )
            results.add( resultsIter.next() ) ;

        Set expected = new HashSet(Arrays.asList(expectedNodes)) ;
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