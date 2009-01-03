/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import junit.TestBase;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.sse.SSE;

import com.hp.hpl.jena.tdb.store.NodeId;

public class TestNodeId extends TestBase
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
    { test("1", SSE.parseNode("1")) ; }

    @Test public void nodeId_int_2()
    { test("2", SSE.parseNode("2")) ; }

    @Test public void nodeId_int_3()
    { test("'3'^^xsd:int", SSE.parseNode("3")) ; }

    @Test public void nodeId_int_4()
    { test("'3'", null) ; }

    @Test public void nodeId_int_5()
    { test("-1",  SSE.parseNode("-1")) ; }
    
    @Test public void nodeId_int_6()
    { test("-180",  SSE.parseNode("-180")) ; }

    @Test public void nodeId_int_7()
    { test("01",  SSE.parseNode("1")) ; }
    
    @Test public void nodeId_int_8()
    { test("+01",  SSE.parseNode("1")) ; }
    
    @Test public void nodeId_decimal_1()
    { test("3.14", SSE.parseNode("3.14")) ; }

    @Test public void nodeId_decimal_2()
    { test("123456789.123456789", null) ; }
    
    // Just this once, directly create the Node.
    @Test public void nodeId_decimal_3()
    { test("12.89", Node.createLiteral("12.89", null, XSDDatatype.XSDdecimal)) ; }

    @Test public void nodeId_decimal_4()
    { test("-1.0",  SSE.parseNode("-1.0")) ; }
    
    @Test public void nodeId_dateTime_1()
    { test("'2008-04-28T15:36:15+01:00'^^xsd:dateTime",  SSE.parseNode("'2008-04-28T15:36:15+01:00'^^xsd:dateTime")) ; }

    @Test public void nodeId_dateTime_2()
    { test("'2008-04-28T15:36:15Z'^^xsd:dateTime",  SSE.parseNode("'2008-04-28T15:36:15Z'^^xsd:dateTime")) ; }

    @Test public void nodeId_dateTime_3()
    { test("'2008-04-28T15:36:15+00:00'^^xsd:dateTime",  SSE.parseNode("'2008-04-28T15:36:15+00:00'^^xsd:dateTime")) ; }

    @Test public void nodeId_dateTime_4()
    { test("'2008-04-28T15:36:15-05:00'^^xsd:dateTime",  SSE.parseNode("'2008-04-28T15:36:15-05:00'^^xsd:dateTime")) ; }

    // No timezone.
    @Test public void nodeId_dateTime_5()
    { test("'2008-04-28T15:36:15'^^xsd:dateTime",  SSE.parseNode("'2008-04-28T15:36:15'^^xsd:dateTime")) ; }

    // Note the trailing zero - system does not preserve perfect lexical forms. 
    @Test public void nodeId_dateTime_6()
    { test("'2008-04-28T15:36:05.450'^^xsd:dateTime",  SSE.parseNode("'2008-04-28T15:36:05.450'^^xsd:dateTime")) ; }

    // Java bug: T24:00:00 not accepted by DatatypeFactory.newXMLGregorianCalendar(lex)
//    @Test public void nodeId_dateTime_7()
//    { test("'2008-04-28T24:00:00'^^xsd:dateTime", SSE.parseNode("'2008-04-29T00:00:00'^^xsd:dateTime")) ; }
    
    @Test public void nodeId_dateTime_8()
    { test("'8008-04-28T15:36:05.450'^^xsd:dateTime", null) ; }

    @Test public void nodeId_date_1()
    { test("'2008-04-28Z'^^xsd:date", SSE.parseNode("'2008-04-28Z'^^xsd:date")) ; }

    @Test public void nodeId_date_2()
    { test("'2008-04-28+00:00'^^xsd:date", SSE.parseNode("'2008-04-28+00:00'^^xsd:date")) ; }

    @Test public void nodeId_date_3()
    { test("'2008-04-28-05:00'^^xsd:date", SSE.parseNode("'2008-04-28-05:00'^^xsd:date")) ; }

    @Test public void nodeId_date_4()
    { test("'2008-04-28+02:00'^^xsd:date", SSE.parseNode("'2008-04-28+02:00'^^xsd:date")) ; }

    @Test public void nodeId_date_5()
    { test("'8008-04-28'^^xsd:date", null) ; }

    @Test public void nodeId_boolean_1()
    { test("'true'^^xsd:boolean", SSE.parseNode("'true'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_2()
    { test("'false'^^xsd:boolean", SSE.parseNode("'false'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_3()
    { test("'1'^^xsd:boolean", SSE.parseNode("'true'^^xsd:boolean")) ; }

    @Test public void nodeId_boolean_4()
    { test("'0'^^xsd:boolean", SSE.parseNode("'false'^^xsd:boolean")) ; }

    private void test(String x, Node correct)
    {
        Node n = SSE.parseNode(x) ;
        NodeId nodeId = NodeId.inline(n) ;
        
        if ( correct == null )
        {
            assertNull(nodeId) ;
            return ;
        }
        Node n2 = NodeId.extract(nodeId) ;
        assertNotNull(n2) ;
        assertEquals(correct, n2) ;
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