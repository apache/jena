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

package org.apache.jena.common;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.Item;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.builders.BuilderNode;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.IsoAlg;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sparql.util.NodeUtils;
import org.junit.Test;

public class TestIsoMatcher
{
    @Test public void iso_00() { testGraph("", "", true); }

    @Test public void iso_graph_01() { testGraph("(<x> <p> 1)",
                                                 "(<x> <p> 1)",
                                                 true); }

    @Test public void iso_graph_02() { testGraph("(<x> <p> 1)",
                                                 "(<x> <p> 2)",
                                                 false); }

    @Test public void iso_graph_03() { testGraph("(<x> <p> 1) (<x> <p> 2)",
                                                 "(<x> <p> 2)",
                                                 false); }

    @Test public void iso_graph_04() { testGraph("(<x> <p> _:a)",
                                                 "(<x> <p> 2)",
                                                 false); }

    @Test public void iso_graph_05() { testGraph("(<x> <p> _:a)",
                                                 "(<x> <p> _:b)",
                                                 true); }

    @Test public void iso_graph_06() { testGraph("(_:a <p> _:a)",
                                                   "(_:b <p> _:b)",
                                           true) ; }

    @Test public void iso_graph_07() { testGraph("(_:a1 <p> _:a2)",
                                                 "(_:bb <p> _:bb)",
                                                 false) ; }

    @Test public void iso_graph_10() { testGraph("(_:a _:a _:a)",
                                                 "(_:b _:b _:b)",
                                                 true) ; }

    @Test public void iso_graph_11() { testGraph("(_:a _:a _:a)",
                                                 "(_:z _:b _:b)",
                                                 false); }

    @Test public void iso_graph_12() { testGraph("(_:a _:a _:a)",
                                                 "(_:b _:z _:b)",
                                                 false) ; }

    @Test public void iso_graph_13() { testGraph("(_:a _:a _:a)",
                                                 "(_:b _:b _:z)",
                                                 false) ; }

    @Test public void iso_graph_14() { testGraph("(_:a _:a _:b)",
                                                 "(_:b _:b _:z)",
                                                 true) ; }

    @Test public void iso_graph_15() { testGraph("(_:a _:x _:a)",
                                                 "(_:b _:z _:b)",
                                                 true) ; }

    @Test public void iso_graph_16() { testGraph("(_:x _:a _:a)",
                                                 "(_:z _:b _:b)",
                                                 true) ; }

    @Test public void iso_graph_20() { testGraph("(<x> <p> _:a) (<z> <p> _:a)",
                                                 "(<x> <p> _:b) (<z> <p> _:b)",
                                                 true) ; }

    @Test public void iso_graph_21() { testGraph("(<x> <p> _:a1) (<z> <p> _:a2)",
                                                 "(<x> <p> _:b) (<z> <p> _:b)",
                                                 false) ; }

    @Test public void iso_graph_22() { testGraph("(_:a <p> _:a) (<s> <q> _:a)",
                                                 "(_:b <p> _:b) (<s> <q> _:b)",
                                                 true) ; }

    @Test public void iso_graph_23() { testGraph("(_:a <p> _:a) (<s> <q> _:a)",
                                                 "(_:b <p> _:b) (<s> <q> _:c)",
                                                 false) ; }

    @Test public void iso_graph_24() { testGraph("(_:a <p> _:a) (<s> <q> _:a) (_:b <q> _:b)",
                                                 "(_:b <p> _:b) (<s> <q> _:b) (_:b <q> _:b)",
                                                 false) ; }

    @Test public void iso_graph_30() { testGraphVar("(?A :p1 ?B)  (?B :p2 ?A)",
                                                    "(?A :p1 ?B1) (?A :p2 ?B1)",
                                                    false); }
    
    //JENA-1789
    @Test public void iso_graph_31() { testGraphVar("(?A :p1 ?B) (?B :p2 ?A)",
                                                    "(?A :p1 ?B) (?A :p2 ?B)",
                                                    false); }

    //JENA-1789
    @Test public void iso_graph_32() { testGraphVar("(?X :p1 ?Y) (?Y :p2 ?X)",
                                                    "(?A :p1 ?B) (?A :p2 ?B)",
                                                    false); }

    @Test public void iso_graph_33() { testGraphVar("(?X :p1 ?Y) (?Y :p2 ?X)",
                                                    "(?A :p1 ?B) (?B :p2 ?A)",
                                                    true); }

    @Test public void iso_graph_34() { testGraphVar("(?X :p1 ?Y) (?X :p2 ?Y)",
                                                    "(?A :p1 ?B) (?A :p2 ?B)",
                                                    true); }

    //JENA-1789
    @Test public void iso_graph_35() { testGraph("(<_:a> :p1 <_:b>) (<_:b> :p2 <_:a>)",
                                                 "(<_:a> :p1 <_:b>) (<_:a> :p2 <_:b>)",
                                                 false); }

    //JENA-1789
    @Test public void iso_graph_36() { testGraph("(<_:a> :p1 <_:b>) (<_:b> :p2 <_:a>)",
                                                 "(<_:a> :p1 <_:b>) (<_:b> :p2 <_:a>)",
                                                 true); }

    
    // RDF-star terms
    @Test public void iso_graph_40() { testGraphIso("(<<:s :p :o>> :q1 :z1) (<<_:a :p :o>> :q2 :z2)",
                                                    "(<<_:a :p :o>> :q2 :z2) (<<:s :p :o>> :q1 :z1)",
                                                    true); }

    @Test public void iso_graph_41() { testGraphIso("(<< _:a :p :o>> :q1 :z1) (<< _:a :p :o>> :q2 :z2)",
                                                    "(<< _:a :p :o>> :q2 :z2) (<< _:a :p :o>> :q1 :z1)",
                                                    true); }

    @Test public void iso_graph_42() { testGraphIso("(<<<_:a> :p :o>> :q1 :z1) (<<<_:a> :p :o>> :q2 :z2)",
                                                    "(<<<_:a> :p :o>> :q2 :z2) (<<<_:b> :p :o>> :q1 :z1)",
                                                    false); }

    @Test public void iso_triple_terms_1() { testTripleTerms("<< <_:a1> :p :o>>",
                                                             "<< <_:a2> :p :o>>",
                                                             true);}

    @Test public void iso_triple_terms_2() { testTripleTerms("<< <_:a1> :p :o>>",
                                                             "<<:s :p <_:a3> >>",
                                                             false);}
    
    @Test public void iso_triple_terms_3() { testTripleTerms("<< <_:a4> :p <_:a4> >>",
                                                             "<< <_:a5> :p <_:a5> >>",
                                                             true ); }

    @Test public void iso_triple_terms_4() { testTripleTerms("<< <_:a4> :p <_:a4> >>",
                                                             "<< <_:a6> :p <_:b6> >>",
                                                             false); }

    @Test public void iso_triple_terms_5() { testTripleTerms("<< <_:a7> :p << <_:b7> :q :o >> >>",
                                                             "<< <_:a8> :p << <_:b8> :q :o >> >>",
                                                             true); } 

    @Test public void iso_triple_terms_6() { testTripleTerms("<< <_:a7> :p << <_:b7> :q :o >> >>",
                                                             "<< <_:a9> :p << :s :q :o >>>>",
                                                             false); } 
    
    @Test public void iso_dsg_50() { testDSG("(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:a))" ,
                                         "(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:a))" ,
                                         true) ; }

    // Graphs separately isomorphic.
    @Test public void iso_dsg_51() { testDSG("(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:a))" ,
                                         "(graph (_:a <p> _:a)) (graph <g> (<s> <q> _:b))" ,
                                         false) ; }
    
    
    // List based tests
    @Test public void iso_tuples_61() { 
        String[] x1 = {};
        String[] x2 = {};
        testTuples(x1, x2, true);
    }

    @Test public void iso_tuples_62() { 
        String[] x1 = {"(<x> <p> 1)"};
        String[] x2 = {};
        testTuples(x1, x2, false);
    }

    @Test public void iso_tuples_63() { 
        String[] x1 = {"(_:x <p> 1)"};
        String[] x2 = {"(_:y <p> 1)"};
        testTuples(x1, x2, true);
    }
    
    @Test public void iso_tuples_64() { 
        String[] x1 = {"(_:x <p> 1)", "(_:x <p> 1)"};
        String[] x2 = {"(_:y <p> 1)", "(_:x <p> 1)"};
        testTuples(x1, x2, true);
    }

    @Test public void iso_tuples_65() { 
        String[] x1 = {"(_:x <p> 1)","(_:y <p> 1)"};
        String[] x2 = {"(_:y <p> 1)","(_:x <p> 1)"};
        testTuples(x1, x2, true);
    }

    // Backtracking. _:a -> _y needs unwinding.
    @Test public void iso_tuples_66() { 
        String[] x1 = {"(_:a <p> 1)","(_:b <p> 1)","(_:a <p> 2)"};
        String[] x2 = {"(_:y <p> 1)","(_:z <p> 1)","(_:z <p> 2)"};
        testTuples(x1, x2, true);
    }

    private void testTuples(String[] x1, String[] x2, boolean iso) {
        List<Tuple<Node>> t1 = tuples(x1);
        List<Tuple<Node>> t2 = tuples(x2);
        test$(t1, t2, iso);
    }

    private void test$(List<Tuple<Node>> t1, List<Tuple<Node>> t2, boolean iso) {
        boolean b = IsoMatcher.isomorphic(t1, t2);
        if ( b != iso ) {
            System.out.println("====");
            System.out.println(t1);
            System.out.println("----");
            System.out.println(t2);
            System.out.println("Expected: " + iso + "; got: " + b);
        }
        assertEquals(iso, b);
    }

    private static Node[] T = new Node[0];
    private List<Tuple<Node>> tuples(String...strings) {
        List<Tuple<Node>> tuples = new ArrayList<>();
        for ( String s : strings ) {
            Item item = SSE.parse(s);
            List<Node> list = BuilderNode.buildNodeList(item);
            Tuple<Node> tuple = TupleFactory.tuple(list.toArray(T));
            tuples.add(tuple);
        }
        return tuples;
    }

    private static void testTripleTerms(String term1, String term2, boolean expected) {
        Node n1 = SSE.parseNode(term1);
        Node n2 = SSE.parseNode(term2);
        Collection<Tuple<Node>> x1 = Collections.singletonList(TupleFactory.create1(n1));
        Collection<Tuple<Node>> x2 = Collections.singletonList(TupleFactory.create1(n2));
    
        boolean b = IsoMatcher.isomorphic(x1, x2);
        if ( b != expected ) {
            System.out.println("====");
            System.out.println(n1);
            System.out.println("----");
            System.out.println(n2);
            System.out.println("Expected: "+expected+"; got: "+b);
        }
        assertEquals(expected, b);
    }

    // Without the Graph isomorphism code.
    private void testGraphIso(String s1, String s2, boolean result) {
        testGraph$(s1, s2, result, false);
        testGraph$(s2, s1, result, false);
    }

    private void testGraph(String s1, String s2, boolean result) {
        testGraph$(s1, s2, result, true);
        testGraph$(s2, s1, result, true);
    }
    
    private void testGraph$(String s1, String s2, boolean expected, boolean extraCheck) {
        s1 = "(graph "+s1+")";
        s2 = "(graph "+s2+")";

        Graph g1 = SSE.parseGraph(s1);
        Graph g2 = SSE.parseGraph(s2);

        boolean b = IsoMatcher.isomorphic(g1, g2);

        if ( b != expected ) {
            System.out.println("====");
            SSE.write(g1);
            System.out.println("----");
            SSE.write(g2);
            System.out.println("Expected: "+expected+"; got: "+b);
        }
        assertEquals(expected, b);
        if ( ! extraCheck )
            return;
        // Check with the other code.
        assertEquals(b, g1.isIsomorphicWith(g2));
    }

    private void testGraphVar(String s1, String s2, boolean result) {
        testGraphVar$(s1, s2, result);
        testGraphVar$(s2, s1, result);
    }

    private void testGraphVar$(String s1, String s2, boolean expected) {
        s1 = "(graph "+s1+")";
        s2 = "(graph "+s2+")";

        Graph g1 = SSE.parseGraph(s1);
        Graph g2 = SSE.parseGraph(s2);
        
        Collection<Tuple<Node>> x1 = IsoMatcher.tuplesTriples(g1.find());
        Collection<Tuple<Node>> x2 = IsoMatcher.tuplesTriples(g2.find());
        
        boolean b = IsoAlg.isIsomorphic(x1, x2, Iso.mappableBlankNodesVariables, NodeUtils.sameRdfTerm);
        if ( b != expected ) {
            System.out.println("====");
            SSE.write(g1);
            System.out.println("----");
            SSE.write(g2);
            System.out.println("Expected: "+expected+"; got: "+b);
        }
        assertEquals(expected, b);
    }

    private void testDSG(String s1, String s2, boolean iso) {
        testDSG$(s1, s2, iso);
        testDSG$(s2, s1, iso);
    }

    private void testDSG$(String s1, String s2, boolean iso) {
        s1 = "(dataset "+s1+")";
        s2 = "(dataset "+s2+")";
        
        DatasetGraph dsg1 = SSE.parseDatasetGraph(s1);
        DatasetGraph dsg2 = SSE.parseDatasetGraph(s2);
        boolean b = IsoMatcher.isomorphic(dsg1, dsg2);
        if ( b != iso ) {
            System.out.println("====");
            SSE.write(dsg1);
            System.out.println("----");
            SSE.write(dsg2);
            System.out.println("Expected: "+iso+"; got: "+b);
        }
        assertEquals(iso, b);
    }
}
