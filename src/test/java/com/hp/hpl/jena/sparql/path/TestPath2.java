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
import java.util.List ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.path.PathFactory ;
import com.hp.hpl.jena.sparql.path.eval.PathEval ;
import com.hp.hpl.jena.sparql.path.eval.PathEval_OLD ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestPath2 extends BaseTest
{
    @BeforeClass static public void beforeClass() {}
    @AfterClass static public void afterClass() {}
    
    // SEE format listy of pairs
    static String prefix = "http://example/" ;
    static String prefixes = "((: <"+prefix+">))" ;
    static String gs = StrUtils.strjoinNL(
           "(prefix "+prefixes,
           "  (graph",

           // A simple directed loop + a tail
           "     (:x :p :y)" ,
           "     (:y :p :z)" ,
           "     (:z :p :x)" ,
           "     (:x :p :a)" ,

           // Diamond
           "     (:x :q :y1)" ,
           "     (:x :q :y2)" ,
           "     (:y1 :q :z)" ,
           "     (:y2 :q :z)" ,
           
           // 2 Diamonds :x - :z - :b
           "     (:x :r :y1)" ,
           "     (:x :r :y2)" ,
           "     (:y1 :r :z)" ,
           "     (:y2 :r :z)" ,
           "     (:z :r :a1)" ,
           "     (:z :r :a2)" ,
           "     (:a1 :r :b)" ,
           "     (:a2 :r :b)" ,
           "))"
        ) ;
    static Node parse(String str) { return SSE.parseNode("<"+prefix+str.substring(1)+">") ; }
    static Graph graph =  SSE.parseGraph(gs) ;
    static Node x = parse(":x") ;
    
    
	@Test public void path_01() { test(x, "(path :p)",            ":a", ":y") ; } 
	@Test public void path_02() { test(x, "(alt :q :r)",          ":y2", ":y1", ":y2", ":y1") ; } 
	@Test public void path_03() { test(x, "(seq :p :p)",          ":z") ; } 
	@Test public void path_04() { test(x, "(seq :q :q)",          ":z", ":z") ; } 
	@Test public void path_05() { test(x, "(path? :p)",           ":x", ":a", ":y") ; } 
	@Test public void path_06() { test(x, "(path? :X)",           ":x") ; } 
	@Test public void path_07() { test(x, "(notoneof :p)",        ":y2", ":y1", ":y2", ":y1") ; } 
	@Test public void path_08() { test(x, "(notoneof :Z)",        ":y2", ":y1", ":y2", ":y1", ":a", ":y") ; } 
	@Test public void path_09() { test(x, "(notoneof (rev :p))" ) ; } 
	@Test public void path_10() { test(x, "(notoneof (rev :Z))",  ":z") ; } 
	@Test public void path_11() { test(x, "(notoneof :q (rev :p))",      ":y2", ":y1", ":a", ":y") ; } 
	@Test public void path_12() { test(x, "(notoneof :q (rev :Z))",      ":y2", ":y1", ":a", ":y", ":z") ; } 
	@Test public void path_13() { test(x, "(path* :p)",           ":x", ":a", ":y", ":z") ; } 
	@Test public void path_14() { test(x, "(path+ :p)",           ":a", ":y", ":z", ":x", ":a") ; } 
	@Test public void path_15() { test(x, "(mod 0 2 :q)",         ":x", ":y2", ":z", ":y1", ":z") ; } 
	@Test public void path_16() { test(x, "(mod 0 1 :q)",         ":x", ":y2", ":y1") ; } 
	@Test public void path_17() { test(x, "(mod 0 1 :r)",         ":x", ":y2", ":y1") ; } 
	@Test public void path_18() { test(x, "(mod 0 3 :r)",         ":x", ":y2", ":z", ":a2", ":a1", ":y1", ":z", ":a2", ":a1") ; } 
	@Test public void path_19() { test(x, "(mod 0 99 :r)",        ":x", ":y2", ":z", ":a2", ":b", ":a1", ":b", ":y1", ":z", ":a2", ":b", ":a1", ":b") ; } 
	@Test public void path_20() { test(x, "(path+ :r)",           ":y2", ":z", ":a2", ":b", ":a1", ":b", ":y1", ":z", ":a2", ":b", ":a1", ":b") ; } 
	@Test public void path_21() { test(x, "(mod 0 2 :q)",         ":x", ":y2", ":z", ":y1", ":z") ; } 
	@Test public void path_22() { test(x, "(pathN 2 :p)",         ":z") ; } 
	@Test public void path_23() { test(x, "(pathN 2 :q)",         ":z", ":z") ; } 
	@Test public void path_24() { test(x, "(path* :p)",           ":x", ":a", ":y", ":z") ; } 
	@Test public void path_25() { test(x, "(path+ :p)",           ":a", ":y", ":z", ":x", ":a") ; } 
	@Test public void path_26() { test(x, "(path* :r)",           ":x", ":y2", ":z", ":a2", ":b", ":a1", ":b", ":y1", ":z", ":a2", ":b", ":a1", ":b") ; } 
	@Test public void path_27() { test(x, "(path+ :r)",           ":y2", ":z", ":a2", ":b", ":a1", ":b", ":y1", ":z", ":a2", ":b", ":a1", ":b") ; } 
	
//	static int i = 0 ;
	
	private void test(Node start, String pathStr, String ... results)
    {
	    //System.out.println(pathStr) ;
	    String ps = "(prefix "+prefixes+" "+pathStr+")" ;
	    Path path = SSE.parsePath(ps) ;
	    
        List<Node> nodes1 = Iter.toList(PathEval.eval(graph, start, PathFactory.pathDistinct(path))) ;
        List<Node> nodes2 = Iter.toList(PathEval.eval(graph, start, path)) ;
        List<Node> expected = new ArrayList<Node>() ;
        for ( String n : results )
            expected.add(parse(n)) ;
        List<Node> expected1 = Iter.iter(expected).distinct().toList() ;
        

        // Generate the tests!
//        i++ ;
//        Transform<Node, String> t = new Transform<Node, String> (){
//
//            @Override
//            public String convert(Node item)
//            {
//                String x = item.getURI() ;
//                x = x.replace("http://example/", ":") ;
//                return "\""+x+"\"" ;
//            }} ;
//        
//        String x = Iter.iter(nodes2).map(t).asString(", ") ;
//        System.out.printf("@Test public void path_%02d() { test(x, \"%s\",      %s) ; } ", i, pathStr, x) ;
//        System.out.println() ;
        
        assertSameArray(expected, nodes2) ;
        assertSameArray(expected1, nodes1) ;

        // Check against old code.
        List<Node> nodes3 = Iter.toList(PathEval_OLD.eval(graph, start, path)) ;
        List<Node> nodes4 = Iter.iter(nodes3).distinct().toList() ;

        assertSameArray(expected, nodes3) ;
        assertSameArray(expected1, nodes4) ;
    }
        
	private static void assertSameArray(List<Node> expected, List<Node> actual)
	{
	    assertEquals(expected.size(), actual.size()) ;
	    List<Node> x = new ArrayList<Node>(expected) ;
	    for ( Node n : actual )
	    {
	        if ( x.contains(n) )
	            x.remove(n) ;
	    }
	    if ( x.size() != 0 )
	        fail("Different: Expected: "+expected+", actual: "+actual) ;
	    
	}
}

