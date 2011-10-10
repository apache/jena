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

package com.hp.hpl.jena.tdb.store;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

public class TestNodeId extends BaseTest
{
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
    
    @Test public void nodeId_int_1()
    { test("1", NodeFactory.parseNode("1")) ; }

    @Test public void nodeId_int_2()
    { test("2", NodeFactory.parseNode("2")) ; }

    @Test public void nodeId_int_3()
    { test("'3'^^xsd:int", NodeFactory.parseNode("3")) ; }

    @Test public void nodeId_int_4()
    { test("'3'", (Node)null) ; }

    @Test public void nodeId_int_5()
    { test("-1",  NodeFactory.parseNode("-1")) ; }
    
    @Test public void nodeId_int_6()
    { test("-180",  NodeFactory.parseNode("-180")) ; }

    @Test public void nodeId_int_7()
    { test("01",  NodeFactory.parseNode("1")) ; }
    
    @Test public void nodeId_int_8()
    { test("+01",  NodeFactory.parseNode("1")) ; }
    
    @Test public void nodeId_decimal_1()
    { test("3.14", NodeFactory.parseNode("3.14")) ; }

    @Test public void nodeId_decimal_2()
    { test("123456789.123456789", (Node)null) ; }
    
    // Just this once, directly create the Node.
    @Test public void nodeId_decimal_3()
    { test("12.89", Node.createLiteral("12.89", null, XSDDatatype.XSDdecimal)) ; }

    @Test public void nodeId_decimal_4()
    { test("-1.0",  NodeFactory.parseNode("-1.0")) ; }
    
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

    // Out of range.
    // But XMLGregorian calendar truncates.
//    @Test public void nodeId_dateTime_14()
//    { test("'2008-04-28T15:36:05.0001'^^xsd:dateTime") ; }
//
//    @Test public void nodeId_dateTime_15()
//    { test("'2008-04-28T15:36:05.00010'^^xsd:dateTime", "'2008-04-28T15:36:05.0001'^^xsd:dateTime") ; }

    @Test public void nodeId_date_1()
    { test("'2008-04-28Z'^^xsd:date", NodeFactory.parseNode("'2008-04-28Z'^^xsd:date")) ; }

    @Test public void nodeId_date_2()
    { test("'2008-04-28+00:00'^^xsd:date", NodeFactory.parseNode("'2008-04-28+00:00'^^xsd:date")) ; }

    @Test public void nodeId_date_3()
    { test("'2008-04-28-05:00'^^xsd:date", NodeFactory.parseNode("'2008-04-28-05:00'^^xsd:date")) ; }

    @Test public void nodeId_date_4()
    { test("'2008-04-28+02:00'^^xsd:date", NodeFactory.parseNode("'2008-04-28+02:00'^^xsd:date")) ; }

    @Test public void nodeId_date_5()
    { test("'8008-04-28'^^xsd:date", (Node)null) ; }

    @Test public void nodeId_boolean_1()
    { test("'true'^^xsd:boolean", NodeFactory.parseNode("'true'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_2()
    { test("'false'^^xsd:boolean", NodeFactory.parseNode("'false'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_3()
    { test("'1'^^xsd:boolean", NodeFactory.parseNode("'true'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_4()
    { test("'0'^^xsd:boolean", NodeFactory.parseNode("'false'^^xsd:boolean")) ; }

    private void test(String x)
    {
        test(x, x) ;
    }
    
    private void test(String x, String expected)
    {
        test(x, NodeFactory.parseNode(expected)) ;
    }

    private void test(String x, Node correct)
    {
        Node n = NodeFactory.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        if ( correct == null )
        {
            assertNull(nodeId) ;
            return ;
        }

        Node n2 = NodeId.extract(nodeId) ;
        assertNotNull(n2) ;
        
        String s = "("+correct.getLiteralLexicalForm()+","+n2.getLiteralLexicalForm()+")" ;
        
        assertTrue("Not same value: "+s, correct.sameValueAs(n2)) ;
        
        // Term equality.
        assertEquals("Not same term", correct, n2) ;
    }
}
