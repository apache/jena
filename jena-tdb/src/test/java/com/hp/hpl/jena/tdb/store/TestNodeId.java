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

package com.hp.hpl.jena.tdb.store;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TestNodeId extends BaseTest
{
//    @BeforeClass public static void beforeClass() {
//        // If running just this test suite, then this happenes before SystemTDB initialization.    
//        System.getProperties().setProperty("tdb:store.enableInlineLiterals", "true") ;
//    }
    
    @Test public void nodeId_01()
    {
        NodeId nodeId = NodeId.create(37) ;
        assertEquals(37L, nodeId.getId()) ;
    }
    
    @Test public void nodeId_02()
    {
        NodeId nodeId = NodeId.create(-1L) ;
        assertEquals(-1L, nodeId.getId()) ;
    }
    
    // Inlines
    
    @Test public void nodeId_int_01()
    { test("1", NodeFactoryExtra.parseNode("1")) ; }

    @Test public void nodeId_int_02()
    { test("2", NodeFactoryExtra.parseNode("2")) ; }

    @Test public void nodeId_int_03()
    { test("'3'^^xsd:int", NodeFactoryExtra.parseNode("3")) ; }

    @Test public void nodeId_int_04()
    { test("'3'", (Node)null) ; }

    @Test public void nodeId_int_05()
    { test("-1",  NodeFactoryExtra.parseNode("-1")) ; }
    
    @Test public void nodeId_int_06()
    { test("-180",  NodeFactoryExtra.parseNode("-180")) ; }

    @Test public void nodeId_int_07()
    { test("01",  NodeFactoryExtra.parseNode("1")) ; }
    
    @Test public void nodeId_int_08()
    { test("+01",  NodeFactoryExtra.parseNode("1")) ; }
    
    @Test public void nodeId_int_09()
    // More than Long.MAX_VALUE
    { test("92233720368547758070",  (Node)null) ; }

    // On the edge.
    
    static long X = 1L<<55 ;        // Just too large 
    static long Y = -((1L<<55) +1) ;   // Just too small 
    
    @Test public void nodeId_int_10()
    { test("\""+Long.toString(X)+"\"^^xsd:integer",  (Node)null) ; }

    @Test public void nodeId_int_11()
    { 
        Node n = NodeValue.makeInteger(X-1).asNode() ;
        test("\""+Long.toString(X-1)+"\"^^xsd:integer",  n) ; 
    }

    @Test public void nodeId_int_12()
    { test("\""+Long.toString(Y)+"\"^^xsd:integer",  (Node)null) ; }

    @Test public void nodeId_int_13()
    { 
        Node n = NodeValue.makeInteger(Y+1).asNode() ;
        test("\""+Long.toString(Y+1)+"\"^^xsd:integer",  n) ; 
    }

    @Test public void nodeId_int_20()
    { test("'300'^^xsd:byte",  (Node)null) ; }
    

    @Test public void nodeId_decimal_1()
    { test("3.14", NodeFactoryExtra.parseNode("3.14")) ; }

    @Test public void nodeId_decimal_2()
    { test("123456789.123456789", (Node)null) ; }
    
    // Just this once, directly create the Node.
    @Test public void nodeId_decimal_3()
    { test("12.89", NodeFactory.createLiteral("12.89", null, XSDDatatype.XSDdecimal)) ; }

    @Test public void nodeId_decimal_4()
    { test("-1.0",  NodeFactoryExtra.parseNode("-1.0")) ; }
    
    // This number has > 47 bits of value : 2412.80478192688
    @Test public void nodeId_decimal_5()
    { test("2412.80478192688",  (Node)null) ; }
    
    // This number has > 47 bits of value : -2412.80478192688
    @Test public void nodeId_decimal_6()
    { test("-2412.80478192688",  (Node)null) ; }

    @Test public void nodeId_decimal_7()
    { test("'0.00000001'^^xsd:decimal",  
           NodeFactory.createLiteral("0.00000001", null, XSDDatatype.XSDdecimal)) ; 
    }

    @Test public void nodeId_decimal_8()
    { test("0.00000001", NodeFactory.createLiteral("0.00000001", null, XSDDatatype.XSDdecimal)) ; }

    @Test public void nodeId_dateTime_01()
    { test("'2008-04-28T15:36:15+01:00'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_02()
    { test("'2008-04-28T15:36:15Z'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_03()
    { test("'2008-04-28T15:36:15+00:00'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_04()
    { test("'2008-04-28T15:36:15-05:00'^^xsd:dateTime") ; }

    // No timezone.
    @Test public void nodeId_dateTime_05()
    { test("'2008-04-28T15:36:15'^^xsd:dateTime") ; }

    // Note the trailing zero - system does not preserve perfect lexical forms. 
    @Test public void nodeId_dateTime_06()
    { test("'2008-04-28T15:36:05.450'^^xsd:dateTime", "'2008-04-28T15:36:05.45'^^xsd:dateTime") ; }

    // Java bug: T24:00:00 not accepted by DatatypeFactory.newXMLGregorianCalendar(lex)
//    @Test public void nodeId_dateTime_07()
//    { test("'2008-04-28T24:00:00'^^xsd:dateTime", NodeFactory.parseNode("'2008-04-29T00:00:00'^^xsd:dateTime")) ; }
    
    // Out of range.
    @Test public void nodeId_dateTime_08()
    { test("'8008-04-28T15:36:05.45'^^xsd:dateTime", (Node)null) ; }

    @Test public void nodeId_dateTime_09()
    { test("'2008-04-28T15:36:05.001'^^xsd:dateTime") ; }
    
    @Test public void nodeId_dateTime_10()
    { test("'2008-04-28T15:36:05.01'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_11()
    { test("'2008-04-28T15:36:05.1'^^xsd:dateTime") ; }

    // Canonicalization test - fractional seconds.
    @Test public void nodeId_dateTime_12()
    { test("'2008-04-28T15:36:05.010'^^xsd:dateTime", "'2008-04-28T15:36:05.01'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_13()
    { test("'2008-04-28T15:36:05.100'^^xsd:dateTime", "'2008-04-28T15:36:05.1'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_14()
    { test("'2012-07-29T20:39:11.100+01:15'^^xsd:dateTime", "'2012-07-29T20:39:11.1+01:15'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_15()
    { test("'2012-07-29T20:39:11.100-01:15'^^xsd:dateTime", "'2012-07-29T20:39:11.1-01:15'^^xsd:dateTime") ; }


    @Test public void nodeId_dateTime_16()
    { test("'2012-07-29T20:39:11.100+01:30'^^xsd:dateTime", "'2012-07-29T20:39:11.1+01:30'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_17()
    { test("'2012-07-29T20:39:11.100-01:45'^^xsd:dateTime", "'2012-07-29T20:39:11.1-01:45'^^xsd:dateTime") ; }

    @Test public void nodeId_dateTime_18()
    { test("'2012-07-29T20:39:11.100Z'^^xsd:dateTime", "'2012-07-29T20:39:11.1Z'^^xsd:dateTime") ; }

    @Test public void nodeId_date_1()
    { test("'2008-04-28Z'^^xsd:date", NodeFactoryExtra.parseNode("'2008-04-28Z'^^xsd:date")) ; }

    @Test public void nodeId_date_2()
    { test("'2008-04-28+00:00'^^xsd:date", NodeFactoryExtra.parseNode("'2008-04-28+00:00'^^xsd:date")) ; }

    @Test public void nodeId_date_3()
    { test("'2008-04-28-05:00'^^xsd:date", NodeFactoryExtra.parseNode("'2008-04-28-05:00'^^xsd:date")) ; }

    @Test public void nodeId_date_4()
    { test("'2008-04-28+02:00'^^xsd:date", NodeFactoryExtra.parseNode("'2008-04-28+02:00'^^xsd:date")) ; }

    @Test public void nodeId_date_5()
    { test("'8008-04-28'^^xsd:date", (Node)null) ; }

    @Test public void nodeId_date_6()
    { test("'2012-07-29+06:15'^^xsd:date", "'2012-07-29+06:15'^^xsd:date") ; }

    @Test public void nodeId_date_7()
    { test("'2012-07-29-06:30'^^xsd:date", "'2012-07-29-06:30'^^xsd:date") ; }

    @Test public void nodeId_boolean_1()
    { test("'true'^^xsd:boolean", NodeFactoryExtra.parseNode("'true'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_2()
    { test("'false'^^xsd:boolean", NodeFactoryExtra.parseNode("'false'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_3()
    { test("'1'^^xsd:boolean", NodeFactoryExtra.parseNode("'true'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_4()
    { test("'0'^^xsd:boolean", NodeFactoryExtra.parseNode("'false'^^xsd:boolean")) ; }

    private void test(String x)
    {
        test(x, x) ;
    }
    
    private void test(String x, String expected)
    {
        test(x, NodeFactoryExtra.parseNode(expected)) ;
    }

    private void test(String x, Node correct)
    {
        Node n = NodeFactoryExtra.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        if ( correct == null )
        {
            assertNull("Expected no encoding: got: "+nodeId, nodeId) ;
            return ;
        }
        assertNotNull("Expected inlining: "+n, nodeId) ;
        Node n2 = NodeId.extract(nodeId) ;
        assertNotNull("Expected recovery", n2) ;
        
        String s = "("+correct.getLiteralLexicalForm()+","+n2.getLiteralLexicalForm()+")" ;
        
        assertTrue("Not same value: "+s, correct.sameValueAs(n2)) ;
        
        // Term equality.
        assertEquals("Not same term", correct, n2) ;
    }
}
