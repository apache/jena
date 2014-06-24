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

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.path.eval.PathEval ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderPath ;
import com.hp.hpl.jena.sparql.sse.writers.WriterPath ;

public class TestPath extends BaseTest
{
    static Graph graph1 = GraphFactory.createDefaultGraph() ;
    static Graph graph2 = GraphFactory.createDefaultGraph() ;
    static Graph graph3 = GraphFactory.createDefaultGraph() ;
    static Graph graph4 = GraphFactory.createDefaultGraph() ;
    static Graph graph5 = GraphFactory.createDefaultGraph() ;
    static Graph graph6 = GraphFactory.createDefaultGraph() ;
    static Graph graph7 = GraphFactory.createDefaultGraph() ;
    
    static Node n1 = NodeFactory.createURI("n1") ;
    static Node n2 = NodeFactory.createURI("n2") ;
    static Node n3 = NodeFactory.createURI("n3") ;
    static Node n4 = NodeFactory.createURI("n4") ;
    static Node n5 = NodeFactory.createURI("n5") ;
    static Node n6 = NodeFactory.createURI("n6") ;
    static Node p = NodeFactory.createURI("http://example/p") ;
    static Node q = NodeFactory.createURI("http://example/q") ;
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
        
        // A DAG, one property
        graph3.add(new Triple(n1, p, n2)) ;
        graph3.add(new Triple(n1, p, n3)) ;
        graph3.add(new Triple(n2, p, n4)) ;
        graph3.add(new Triple(n3, p, n4)) ;

        // Linear path with spurs
        graph4.add(new Triple(n1, p, n2)) ;
        graph4.add(new Triple(n2, p, n3)) ;
        graph4.add(new Triple(n3, p, n4)) ;
        graph4.add(new Triple(n2, q, n5)) ;
        graph4.add(new Triple(n4, q, n6)) ;
        
        //
        graph5.add(new Triple(n1, p, n2)) ;
        graph5.add(new Triple(n1, p, n3)) ;
        graph5.add(new Triple(n2, q, n4)) ;
        graph5.add(new Triple(n3, q, n5)) ;
        
        // Loop
        graph6.add(new Triple(n1, p, n2)) ;
        graph6.add(new Triple(n2, p, n1)) ;
        
        // Loop + tail
        graph7.add(new Triple(n1, p, n2)) ;
        graph7.add(new Triple(n2, p, n1)) ;
        graph7.add(new Triple(n2, p, n3)) ;
    }
    
    // ----
    
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
    @Test public void parsePath_14()           { parse(":p{,1}") ; }
    @Test public void parsePath_15()           { parse(":p{1,2}") ; }
    
    @Test public void parsePath_20()           { parse(":p^:q") ; }
    @Test public void parsePath_21()           { parse("^:p^:q") ; }
    @Test public void parsePath_22()           { parse("^:p/:q") ; }
    @Test public void parsePath_23()           { parse("^(:p/:q)") ; }
    @Test public void parsePath_24()           { parse("^(:p^:q)") ; }
    @Test public void parsePath_25()           { parse(":p^(:q/:r)") ; }

    @Test public void parsePath_30()           { parse("!(:q|:r)") ; }
    @Test public void parsePath_31()           { parse(":p/!:q/:r") ; }
    @Test public void parsePath_32()           { parse("!:q/:r") ; }
    
    @Test public void parsePathErr_01()        { parse("", false) ; }
    @Test public void parsePathErr_02()        { parse("()", false) ; }
    @Test public void parsePathErr_03()        { parse(":p :q", false) ; }
    
    // Check we get the form on the right for the expression on the left.
    @Test public void parseEquals_1()         {  parse("(:p)",        ":p") ; }
    @Test public void parseEquals_2()         {  parse(":p/:q/:r",    "(:p/:q)/:r") ; }
    @Test public void parseEquals_3()         {  parse("^:p^:q^:r",   "(^:p^:q)^:r") ; }
    @Test public void parseEquals_4()         {  parse(":p/(:q/:r)",  ":p/(:q/:r)") ; }
    @Test public void parseEquals_5()         {  parse("(:p/:q)|:r",  ":p/:q|:r") ; }
    @Test public void parseEquals_6()         {  parse(":p|(:q/:r)",  ":p|:q/:r") ; }
    @Test public void parseEquals_7()         {  parse("^:p/:q",      "(^:p)/:q") ; }
    @Test public void parseEquals_8()         {  parse("!:q/:r",      "(!:q)/:r") ; }
    @Test public void parseEquals_9()         {  parse("!:q/:r",      "(!:q)/:r") ; }

    @Test public void parsePathDistinct1()    {  parse("distinct(:p)", "distinct(:p)") ; }
    @Test public void parsePathDistinct2()    {  parse("distinct(:p*)", "distinct(:p*)") ; }
    @Test public void parsePathDistinct3()    {  parse("distinct((:p)*)", "distinct(:p*)") ; }
    @Test public void parsePathDistinct4()    {  parse(":p/distinct(:p*)/:q", ":p/distinct(:p*)/:q") ; }
    @Test public void parsePathDistinct5()    {  parse(":p/distinct(:p)*/:q", ":p/distinct(:p)*/:q") ; }
    
    @Test public void parsePathShortest1()    {  parse("shortest(:p)", "shortest(:p)") ; }
    @Test public void parsePathShortest2()    {  parse("shortest(:p*)", "shortest(:p*)") ; }
    @Test public void parsePathShortest3()    {  parse("shortest(:p+)", "shortest(:p+)") ; }
    @Test public void parsePathShortest4()    {  parse("shortest((:p)*)", "shortest(:p*)") ; }
    @Test public void parsePathShortest5()    {  parse(":p/shortest(:p*)/:q", ":p/shortest(:p*)/:q") ; }
    @Test public void parsePathShortest6()    {  parse(":p/shortest(:p)*/:q", ":p/shortest(:p)*/:q") ; }

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

    @Test public void path_01()   { test(graph1, n1,   ":p",          n2) ; }
    @Test public void path_02()   { test(graph1, n1,   ":p{0}",       n1) ; }
    @Test public void path_03()   { test(graph1, n1,   ":p{1}",       n2) ; }
    @Test public void path_04()   { test(graph1, n1,   ":p{2}",       n3) ; }
    @Test public void path_05()   { test(graph1, n1,   ":p{0,1}",     n1, n2) ; }
    @Test public void path_06()   { test(graph1, n1,   ":p{0,2}",     n1,n2,n3) ; }
    @Test public void path_07()   { test(graph1, n1,   ":p{1,2}",     n2, n3) ; }
    @Test public void path_08()   { test(graph1, n1,   ":p{9,9}"      ) ; }
    @Test public void path_09()   { test(graph1, n1,   ":p{0,9}",     n1,n2,n3,n4) ; }
    @Test public void path_10()   { test(graph1, n1,   ":p*",         n1,n2,n3,n4) ; }
    @Test public void path_11()   { test(graph1, n1,   ":p+",         n2,n3,n4) ; }
    @Test public void path_12()   { test(graph1, n1,   ":p?",         n1,n2) ; }
    @Test public void path_13()   { test(graph1, n1,   ":p/:p",       n3) ; }
    @Test public void path_14()   { test(graph1, n2,   "^:p",         n1) ; }
    @Test public void path_15()   { test(graph1, n2,   "^:p^:p"       ) ; }
    @Test public void path_16()   { test(graph1, n4,   "^:p^:p",      n2) ; }
    @Test public void path_17()   { test(graph1, n4,   "^(:p/:p)",    n2) ; }
    
    @Test public void path_18()   { test(graph1, n2,   "^:p/:p",      n2) ; }

    @Test public void path_20()   { test(graph2, n1,   ":p",          n2,n3) ; }
    @Test public void path_21()   { test(graph2, n1,   ":p/:q",       n4, n4) ; }
    @Test public void path_22()   { test(graph2, n2,   "^:p|:q",      n1,n4) ; }
    @Test public void path_23()   { test(graph2, n2,   "^(:p|^:q)*",  n1,n2,n4) ; }

    @Test public void path_24()   { testReverse(graph1, n2,   ":p",          n1) ; }
    @Test public void path_25()   { testReverse(graph1, n3,   ":p/:p",       n1) ; }

    @Test public void path_30()   { test(graph1, n1,   ":p*",       n1,n2,n3,n4) ; }
    @Test public void path_31()   { test(graph2, n1,   ":p*",       n1,n2,n3) ; }
    
//    // A DAG, one property
//    graph3.add(new Triple(n1, p, n2)) ;
//    graph3.add(new Triple(n1, p, n3)) ;
//    graph3.add(new Triple(n2, p, n4)) ;
//    graph3.add(new Triple(n3, p, n4)) ;

    
    @Test public void path_32()   { test(graph3, n1,   ":p{*}",     n1,n2,n3,n4,n4) ; }
    @Test public void path_33()   { test(graph3, n1,   ":p*",       n1,n2,n3,n4) ; }
    @Test public void path_34()   { test(graph3, n1,   ":p+",       n2,n3,n4) ; }

    
    private static List<Binding> eval(Graph graph, String start$, String pathStr, String finish$)
    {
        return eval(graph, SSE.parseNode(start$, pmap), pathStr, SSE.parseNode(finish$, pmap)) ;
    }
    
    private static List<Binding> eval(Graph graph, Node start, String pathStr, Node finish)
    {
        Path path = SSE.parsePath(pathStr, pmap) ;
        QueryIterator qIter = PathLib.execTriplePath(BindingFactory.root(), start, path, finish, new ExecutionContext(ARQ.getContext(), graph, null, null)) ;
        return Iter.toList(qIter) ;
    }
    
    @Test public void path_35()
    { 
        List<Binding> x = eval(graph6, "?x", "(path+ :p)", "?y" ) ;
        assertEquals(4, x.size()) ;
    }
    
    @Test public void path_36()
    { 
        // Same end points.
        List<Binding> x = eval(graph6, "?x", "(path+ :p)", "?x" ) ;
        assertEquals(2, x.size()) ;
    }

    @Test public void path_37()
    { 
        List<Binding> x = eval(graph6, "?x", "(path* :p)", "?x" ) ;
        assertEquals(2, x.size()) ;
        Node node1 = x.get(0).get(Var.alloc("x")) ;
        Node node2 = x.get(1).get(Var.alloc("x")) ;
        assertFalse(node1.equals(node2)) ;
        assertTrue(node1.equals(n1) || node1.equals(n2)) ;
        assertTrue(node2.equals(n1) || node2.equals(n2)) ;
    }

    @Test public void path_38()
    { 
        // Same end points.
        List<Binding> x = eval(graph6, "?x", "(pathN+ :p)", "?x" ) ;
        
        assertEquals(2, x.size()) ;
        Node node1 = x.get(0).get(Var.alloc("x")) ;
        Node node2 = x.get(1).get(Var.alloc("x")) ;
        assertFalse(node1.equals(node2)) ;
        assertTrue(node1.equals(n1) || node1.equals(n2)) ;
        assertTrue(node2.equals(n1) || node2.equals(n2)) ;
    }

    @Test public void path_39()
    { 
        List<Binding> x = eval(graph6, "?x", "(pathN* :p)", "?x" ) ;
        assertEquals(2, x.size()) ;
    }

    // TODO Shortest path is not implemented yet.  These also need to be verified that they are correct.
//    @Ignore @Test public void path_40()   { test(graph1, n1,   "shortest(:p*)",       n1) ; }
//    @Ignore @Test public void path_41()   { test(graph1, n1,   "shortest(:p+)",       n2) ; }
//    @Ignore @Test public void path_42()   { test(graph2, n1,   "shortest(:p*/:q)",    n4) ; }
//    @Ignore @Test public void path_43()   { test(graph2, n1,   "shortest(:p{*}/:q)",  n4, n4) ; }
//    @Ignore @Test public void path_44()   { test(graph4, n1,   "shortest(:p*/:q)",    n5) ; }
//    @Ignore @Test public void path_45()   { test(graph4, n1,   "shortest(:p{2,}/:q)", n6) ; }
//    @Ignore @Test public void path_46()   { test(graph5, n1,   "shortest(:p*/:q)",    n4, n5) ; }
    
    

    // ----
    private static void test(Graph graph, Node start, String string, Node... expectedNodes)
    {
       test(graph, start, string, expectedNodes, true) ;
    }
    
    private static void testReverse(Graph graph, Node start, String string, Node... expectedNodes)
    {
       test(graph, start, string, expectedNodes, false) ;
    }

    private static void test(Graph graph, Node start, String string, Node[] expectedNodes, boolean directionForward)
    {
        Path p = PathParser.parse(string, pmap) ;
        Iterator<Node> resultsIter = 
            directionForward ? PathEval.eval(graph, start, p, ARQ.getContext()) : PathEval.evalReverse(graph, start, p, ARQ.getContext()) ; 
        List<Node> results = Iter.toList(resultsIter) ;
        List<Node> expected = Arrays.asList(expectedNodes) ;
        // Unordered by counting equality.
        Assert.assertTrue("expected:"+expected+", got:"+results, sameUnorder(expected, results)) ;
    }
    
    static boolean sameUnorder(List<Node> expected, List<Node> results)
    {
        // Copy - this is modified.
        List<Node> x = new ArrayList<>(results) ;
        for ( Node n : expected )
        {
            if ( ! x.contains(n) )
                return false ;
            x.remove(n) ;
        }
        return x.isEmpty() ;
    }
}
