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

package com.hp.hpl.jena.sparql.util;

import java.io.StringReader ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.other.GLib ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.graph.GNode ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** Test the graph-level RDF list support used in SPARQL */
public class TestList extends BaseTest
{
    private GNode emptyList = parse(listStr_1) ; 
    private GNode list4 = parse(listStr_2) ;
    private GNode list22 = parse(listStr_3) ;
    private Node NIL = RDF.nil.asNode() ;
    
    @Test public void testListLength_1()  { assertEquals(0, GraphList.length(emptyList)) ; }
    
    @Test public void testListLength_2()  { assertEquals(4, GraphList.length(list4)) ; } 
    
    @Test public void testListLength_3()  { assertEquals(4, GraphList.length(list22)) ; }
    
    //@Test public void testListlength_3()  { assertEquals(-1, GraphList.length(gnode(node1))) ; } 
    

    @Test public void testListIndex_1()   { assertEquals(0, GraphList.index(list4, node1)) ; }

    @Test public void testListIndex_2()   { assertEquals(1, GraphList.index(list4, node2)) ; }

    @Test public void testListIndex_3()   { assertEquals(2, GraphList.index(list4, node3)) ; }
    
    @Test public void testListIndex_4()   { assertEquals(3, GraphList.index(list4, node4)) ; }

    @Test public void testListIndex_5()   { assertEquals(-1, GraphList.index(list4, node0)) ; }

    @Test public void testListIndex_6()   { assertEquals(-1, GraphList.index(emptyList, node1)) ; }

    @Test public void testListIndex_7()   { assertEquals(0, GraphList.index(list22, node1)) ; }

    @Test public void testListIndex_8()   { assertEquals(1, GraphList.index(list22, node2)) ; }

    
    @Test public void testListIndexes_1()   
    { 
        List<Integer> x = GraphList.indexes(emptyList, node0) ;
        assertEquals(0, x.size()) ;
    }
    
    @Test public void testListIndexes_2()   
    { 
        List<Integer> x = GraphList.indexes(list4, node0) ;
        assertEquals(0, x.size()) ;
    }

    @Test public void testListIndexes_3()   
    { 
        List<Integer> x = GraphList.indexes(list4, node1) ;
        assertEquals(1, x.size()) ;
        assertEquals(0, x.get(0).intValue()) ;
    }
    
    @Test public void testListIndexes_4()   
    { 
        List<Integer> x = GraphList.indexes(list4, node2) ;
        assertEquals(1, x.size()) ;
        assertEquals(1, x.get(0).intValue()) ;
    }

    @Test public void testListIndexes_5()   
    { 
        List<Integer> x = GraphList.indexes(list4, node4) ;
        assertEquals(1, x.size()) ;
        assertEquals(3, x.get(0).intValue()) ;
    }
    
    @Test public void testListIndexes_6()   
    { 
        List<Integer> x = GraphList.indexes(list22, node1) ;
        assertEquals(2, x.size()) ;
        assertEquals(0, x.get(0).intValue()) ;
        assertEquals(2, x.get(1).intValue()) ;
    }

    @Test public void testListTriples_1() { assertEquals(0, GraphList.allTriples(emptyList).size()) ; }

    @Test public void testListTriples_2() { assertEquals(4*2, GraphList.allTriples(list4).size()) ; }
    
    
    @Test public void testListContains_1()    { assertFalse(GraphList.contains(emptyList, node0)) ; }
    
    @Test public void testListContains_2()    { assertFalse(GraphList.contains(emptyList, node1)) ; }

    @Test public void testListContains_3()    { assertTrue(GraphList.contains(list4, node1)) ; }

    @Test public void testListContains_4()    { assertTrue(GraphList.contains(list4, node2)) ; }
    
    @Test public void testListContains_5()    { assertTrue(GraphList.contains(list4, node4)) ; }

    @Test public void testListOccurs_1()      { assertEquals(0, GraphList.occurs(emptyList, node0)) ; }
    
    @Test public void testListOccurs_2()      { assertEquals(0, GraphList.occurs(emptyList, node1)) ; }
    
    @Test public void testListOccurs_3()      { assertEquals(0, GraphList.occurs(list4, node0)) ; }
    
    @Test public void testListOccurs_4()      { assertEquals(0, GraphList.occurs(emptyList, node1)) ; }
    
    @Test public void testListOccurs_5()      { assertEquals(0, GraphList.occurs(emptyList, NIL)) ; }
    
    @Test public void testListOccurs_6()      { assertEquals(0, GraphList.occurs(list4, NIL)) ; }
    
    @Test public void testListOccurs_7()      { assertEquals(1, GraphList.occurs(list4, node1)) ; }
    
    @Test public void testListOccurs_8()      { assertEquals(1, GraphList.occurs(list4, node2)) ; }
    
    @Test public void testListOccurs_9()      { assertEquals(1, GraphList.occurs(list4, node3)) ; }
    
    @Test public void testListOccurs_10()     { assertEquals(1, GraphList.occurs(list4, node4)) ; }
    
    @Test public void testListOccurs_11()     { assertEquals(2, GraphList.occurs(list22, node1)) ; }
    
    @Test public void testListOccurs_12()     { assertEquals(2, GraphList.occurs(list22, node2)) ; }
    
    @Test public void testListGet_1()         { assertNull(GraphList.get(emptyList, 0)) ; }

    @Test public void testListGet_2()         { assertNull(GraphList.get(emptyList, -1)) ; }

    @Test public void testListGet_3()         { assertNull(GraphList.get(list4, -1)) ; }

    @Test public void testListGet_4()         { assertNull(GraphList.get(list4, 9)) ; }

    @Test public void testListGet_5()         { assertEquals(node1, GraphList.get(list4, 0)) ; }

    @Test public void testListGet_6()         
    { 
        assertEquals(node1, GraphList.get(list4, 0)) ;
        assertEquals(node2, GraphList.get(list4, 1)) ;
        assertEquals(node3, GraphList.get(list4, 2)) ;
        assertEquals(node4, GraphList.get(list4, 3)) ;
    }

    @Test public void testListGet_7()         
    { 
        assertEquals(node1, GraphList.get(list22, 0)) ;
        assertEquals(node2, GraphList.get(list22, 1)) ;
        assertEquals(node1, GraphList.get(list22, 2)) ;
        assertEquals(node2, GraphList.get(list22, 3)) ;
    }
// --------
    
    private static GNode gnode(Node n)  { return new GNode(Factory.createDefaultGraph(), n) ; }
    
    private static GNode parse(String str)
    { 
        Model m = ModelFactory.createDefaultModel() ;
        m.read(new StringReader(str), null, "TTL") ;
        Graph graph = m.getGraph() ;
        Triple t = graph.find(r, p, Node.ANY).next() ;
        return new GNode(graph, t.getObject()) ;
    }
    
    private static Node node1 = NodeFactory.createLiteral("1", "", XSDDatatype.XSDinteger) ;
    private static Node node2 = NodeFactory.createLiteral("2", "", XSDDatatype.XSDinteger) ;
    private static Node node3 = NodeFactory.createLiteral("3", "", XSDDatatype.XSDinteger) ;
    private static Node node4 = NodeFactory.createLiteral("4", "", XSDDatatype.XSDinteger) ;
    
    private static Node node0 = NodeFactory.createLiteral("0", "", XSDDatatype.XSDinteger) ;
    
    private static Node r = NodeFactory.createURI("http://example/r") ;
    private static Node p = NodeFactory.createURI("http://example/p") ;
    private static String preamble = "@prefix : <http://example/> . :r :p " ;
    
    private static String listStr_1 = preamble + "() ." ;
    private static String listStr_2 = preamble + "(1 2 3 4) ." ;
    private static String listStr_3 = preamble + "(1 2 1 2) ." ;
    
    private static String data = 
        "prefix : <http://example/>\n" +
        ":s1 :p (1 2 3) .\n"+
        ":s2 :p () .\n" + 
        ":s3 :p (8) .\n" ;
    private static Node s1 = NodeFactory.createURI("http://example/s1") ;
    private static Node s2 = NodeFactory.createURI("http://example/s2") ;
    private static Node s3 = NodeFactory.createURI("http://example/s3") ;
    
    private static Graph graph = Factory.createDefaultGraph() ;
    static { RDFDataMgr.read(graph, new StringReader(data), null, Lang.TTL); }
    
    
    
    @Test public void testGraphListMember_01() {
        testGraphListMember(s1, node1, node2, node3) ;
    }
    
    @Test public void testGraphListMember_02() {
        testGraphListMember(s2) ;
    }

    @Test public void testGraphListMember_03() {
        testGraphListMember(s3, SSE.parseNode("8")) ;
    }

    private static void testGraphListMember(Node s, Node...expected) {
        Node list = listOf(graph, s) ;
        Iterator<Triple> iter = GraphList.listMember(graph, list, Node.ANY) ;
        Iterator<Node> x = GLib.triple2object(iter) ;
        List<Node> z = Iter.toList(x) ;
        check(z, expected) ;
    }

    private static Node listOf(Graph graph, Node node) {
        return graph.find(node, p, Node.ANY).next().getObject() ;
    }
    
    private static void check(List<Node> z, Node...expected) {
        List<Node> x = Arrays.asList(expected) ;
        assertEqualsUnordered(x, z);
    }
    
}
