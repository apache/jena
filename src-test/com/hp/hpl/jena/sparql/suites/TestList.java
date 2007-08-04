/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;

import java.io.StringReader;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.GNode;
import com.hp.hpl.jena.sparql.util.GraphList;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.vocabulary.RDF;


import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestList extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestList.class) ;
        ts.setName(Utils.classShortName(TestList.class)) ;
        return ts ;
    }
    
    GNode emptyList = parse(listStr_1) ; 
    GNode list4 = parse(listStr_2) ;
    GNode list22 = parse(listStr_3) ;
    Node NIL = RDF.nil.asNode() ;
    
    public void testListLength_1()  { assertEquals(0, GraphList.length(emptyList)) ; }
    
    public void testListLength_2()  { assertEquals(4, GraphList.length(list4)) ; } 
    
    public void testListLength_3()  { assertEquals(4, GraphList.length(list22)) ; }
    
    //public void testListlength_3()  { assertEquals(-1, GraphList.length(gnode(node1))) ; } 
    

    public void testListIndex_1()   { assertEquals(0, GraphList.index(list4, node1)) ; }

    public void testListIndex_2()   { assertEquals(1, GraphList.index(list4, node2)) ; }

    public void testListIndex_3()   { assertEquals(2, GraphList.index(list4, node3)) ; }
    
    public void testListIndex_4()   { assertEquals(3, GraphList.index(list4, node4)) ; }

    public void testListIndex_5()   { assertEquals(-1, GraphList.index(list4, node0)) ; }

    public void testListIndex_6()   { assertEquals(-1, GraphList.index(emptyList, node1)) ; }

    public void testListIndex_7()   { assertEquals(0, GraphList.index(list22, node1)) ; }

    public void testListIndex_8()   { assertEquals(1, GraphList.index(list22, node2)) ; }

    
    public void testListIndexes_1()   
    { 
        List x = GraphList.indexes(emptyList, node0) ;
        assertEquals(0, x.size()) ;
    }
    
    public void testListIndexes_2()   
    { 
        List x = GraphList.indexes(list4, node0) ;
        assertEquals(0, x.size()) ;
    }

    public void testListIndexes_3()   
    { 
        List x = GraphList.indexes(list4, node1) ;
        assertEquals(1, x.size()) ;
        assertEquals(0, ((Integer)x.get(0)).intValue()) ;
    }
    
    public void testListIndexes_4()   
    { 
        List x = GraphList.indexes(list4, node2) ;
        assertEquals(1, x.size()) ;
        assertEquals(1, ((Integer)x.get(0)).intValue()) ;
    }

    public void testListIndexes_5()   
    { 
        List x = GraphList.indexes(list4, node4) ;
        assertEquals(1, x.size()) ;
        assertEquals(3, ((Integer)x.get(0)).intValue()) ;
    }
    
    public void testListIndexes_6()   
    { 
        List x = GraphList.indexes(list22, node1) ;
        assertEquals(2, x.size()) ;
        assertEquals(0, ((Integer)x.get(0)).intValue()) ;
        assertEquals(2, ((Integer)x.get(1)).intValue()) ;
    }

    public void testListTriples_1() { assertEquals(0, GraphList.allTriples(emptyList).size()) ; }

    public void testListTriples_2() { assertEquals(4*2, GraphList.allTriples(list4).size()) ; }
    
    
    public void testListContains_1()    { assertFalse(GraphList.contains(emptyList, node0)) ; }
    
    public void testListContains_2()    { assertFalse(GraphList.contains(emptyList, node1)) ; }

    public void testListContains_3()    { assertTrue(GraphList.contains(list4, node1)) ; }

    public void testListContains_4()    { assertTrue(GraphList.contains(list4, node2)) ; }
    
    public void testListContains_5()    { assertTrue(GraphList.contains(list4, node4)) ; }

    public void testListOccurs_1()      { assertEquals(0, GraphList.occurs(emptyList, node0)) ; }
    
    public void testListOccurs_2()      { assertEquals(0, GraphList.occurs(emptyList, node1)) ; }
    
    public void testListOccurs_3()      { assertEquals(0, GraphList.occurs(list4, node0)) ; }
    
    public void testListOccurs_4()      { assertEquals(0, GraphList.occurs(emptyList, node1)) ; }
    
    public void testListOccurs_5()      { assertEquals(0, GraphList.occurs(emptyList, NIL)) ; }
    
    public void testListOccurs_6()      { assertEquals(0, GraphList.occurs(list4, NIL)) ; }
    
    public void testListOccurs_7()      { assertEquals(1, GraphList.occurs(list4, node1)) ; }
    
    public void testListOccurs_8()      { assertEquals(1, GraphList.occurs(list4, node2)) ; }
    
    public void testListOccurs_9()      { assertEquals(1, GraphList.occurs(list4, node3)) ; }
    
    public void testListOccurs_10()     { assertEquals(1, GraphList.occurs(list4, node4)) ; }
    
    public void testListOccurs_11()     { assertEquals(2, GraphList.occurs(list22, node1)) ; }
    
    public void testListOccurs_12()     { assertEquals(2, GraphList.occurs(list22, node2)) ; }
    
    public void testListGet_1()         { assertNull(GraphList.get(emptyList, 0)) ; }

    public void testListGet_2()         { assertNull(GraphList.get(emptyList, -1)) ; }

    public void testListGet_3()         { assertNull(GraphList.get(list4, -1)) ; }

    public void testListGet_4()         { assertNull(GraphList.get(list4, 9)) ; }

    public void testListGet_5()         { assertEquals(node1, GraphList.get(list4, 0)) ; }

    public void testListGet_6()         
    { 
        assertEquals(node1, GraphList.get(list4, 0)) ;
        assertEquals(node2, GraphList.get(list4, 1)) ;
        assertEquals(node3, GraphList.get(list4, 2)) ;
        assertEquals(node4, GraphList.get(list4, 3)) ;
    }

    public void testListGet_7()         
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
        Triple t = (Triple)graph.find(r, p, Node.ANY).next() ;
        return new GNode(graph, t.getObject()) ;
    }
    
    private static Node node1 = Node.createLiteral("1", "", XSDDatatype.XSDinteger) ;
    private static Node node2 = Node.createLiteral("2", "", XSDDatatype.XSDinteger) ;
    private static Node node3 = Node.createLiteral("3", "", XSDDatatype.XSDinteger) ;
    private static Node node4 = Node.createLiteral("4", "", XSDDatatype.XSDinteger) ;
    
    private static Node node0 = Node.createLiteral("0", "", XSDDatatype.XSDinteger) ;
    
    private static Node r = Node.createURI("http://example/r") ;
    private static Node p = Node.createURI("http://example/p") ;
    private static String preamble = "@prefix : <http://example/> . :r :p " ;
    
    private static String listStr_1 = preamble + "() ." ;
    private static String listStr_2 = preamble + "(1 2 3 4) ." ;
    private static String listStr_3 = preamble + "(1 2 1 2) ." ;
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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