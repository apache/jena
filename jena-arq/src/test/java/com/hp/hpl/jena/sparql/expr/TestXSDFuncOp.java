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

package com.hp.hpl.jena.sparql.expr;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.expr.nodevalue.* ;

public class TestXSDFuncOp extends BaseTest
{
    private static final double accuracyExact_D = 0.0d ;
    private static final double accuracyExact_F = 0.0d ;
    private static final double accuracyClose_D = 0.000001d ;
    private static final double accuracyClose_F = 0.000001f ;
    
    // These add tests also test that the right kind of operation was done.
    
    @Test public void testAddIntegerInteger()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeInteger(7) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 12, r.getInteger().longValue()) ;
    }

    @Test public void testAddDecimalDecimal()
    {
        NodeValue nv1 = NodeValue.makeDecimal(4.3) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.7) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 8, r.getDecimal().doubleValue(), accuracyExact_D ) ;
    }
    
    @Test public void testAddFloatFloat()
    {
        NodeValue nv1 = NodeValue.makeFloat(7.5f) ;
        NodeValue nv2 = NodeValue.makeFloat(2.5f) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a float: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 10, r.getFloat(), accuracyExact_F ) ;
        assertEquals("Wrong result (as doubles)", 10, r.getDouble(), accuracyExact_D ) ;
    }

    @Test public void testAddDoubleDouble()
    {
        NodeValue nv1 = NodeValue.makeDouble(7.5) ;
        NodeValue nv2 = NodeValue.makeDouble(2.5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 10, r.getDouble(), accuracyExact_D ) ;
    }

    
    @Test public void testAddIntegerDecimal()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeDecimal(7) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 12, r.getDecimal().longValue()) ;
    }
    
    @Test public void testAddDecimalInteger()
    {
        NodeValue nv1 = NodeValue.makeDecimal(7) ;
        NodeValue nv2 = NodeValue.makeInteger(5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 12, r.getDecimal().longValue()) ;
    }
    
    @Test public void testAddIntegerFloat()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeFloat(7) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_F ) ;
    }
    
    @Test public void testAddFloatInteger()
    {
        NodeValue nv1 = NodeValue.makeFloat(7) ;
        NodeValue nv2 = NodeValue.makeInteger(5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_F ) ;
    }

    @Test public void testAddIntegerDouble()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeDouble(7) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_D ) ;
    }
    
    @Test public void testAddDoubleInteger()
    {
        NodeValue nv1 = NodeValue.makeDouble(7) ;
        NodeValue nv2 = NodeValue.makeInteger(5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_D ) ;
    }

    @Test public void testAddDecimalFloat()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeFloat(4.5f) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a Float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 8, r.getFloat(), accuracyExact_F) ;
    }
    
    @Test public void testAddFloatDecimal()
    {
        NodeValue nv1 = NodeValue.makeFloat(4.5f) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a Float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 8, r.getFloat(), accuracyExact_F) ;
    }
    @Test public void testAddDecimalDouble()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeDouble(4.5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D) ;
    }
    
    @Test public void testAddDoubleDecimal()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D ) ;
    }

    @Test public void testAddDoubleFloat()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeFloat(3.5f) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D ) ;
    }

    @Test public void testAddFloatDouble()
    {
        NodeValue nv1 = NodeValue.makeFloat(4.5f) ;
        NodeValue nv2 = NodeValue.makeDouble(3.5d) ;
        NodeValue r = XSDFuncOp.numAdd(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D ) ;
    }

    // divide(integer, integer) => decimal
    @Test public void testDivideIntegerInteger()
    {
        NodeValue nv1 = NodeValue.makeInteger(25) ;
        NodeValue nv2 = NodeValue.makeInteger(2) ;
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 12.5, r.getDecimal().doubleValue(), accuracyExact_D) ;
    }

    // divide errors
    @Test public void testDivideByZero1()
    {
        NodeValue nv1 = NodeValue.makeInteger(1) ;
        NodeValue nv2 = NodeValue.makeInteger(0) ;
        try {
            NodeValue r = XSDFuncOp.numDivide(nv1, nv2) ;
            fail("No expection from .divide") ;
        } catch (ExprEvalException ex)
        { }
    }
    
    @Test public void testDivideByZero2()
    {
        NodeValue nv1 = NodeValue.makeInteger(1) ;
        NodeValue nv2 = NodeValue.makeDouble(0) ;
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a +INF: "+r, r.getDouble()==Double.POSITIVE_INFINITY) ;
    }
    
    @Test public void testDivideByZero4()
    {
        NodeValue nv1 = NodeValue.makeInteger(-1) ;
        NodeValue nv2 = NodeValue.makeDouble(-0) ;
        NodeValue r = XSDFuncOp.numDivide(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a -INF: "+r, r.getDouble()==Double.NEGATIVE_INFINITY) ;
    }
    
    @Test public void testSubtractDoubleDecimal()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = XSDFuncOp.numSubtract(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 1d, r.getDouble(), accuracyExact_D ) ;
    }
    
    @Test public void testSubtractDecimalInteger()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeInteger(2) ;
        NodeValue r = XSDFuncOp.numSubtract(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertTrue("Wrong result", NodeValue.sameAs(NodeValue.makeDecimal(1.5), r) ) ;
    }
    
    @Test public void testMultiplyDoubleDecimal()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = XSDFuncOp.numMultiply(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 4.5d*3.5d, r.getDouble(), accuracyExact_D ) ;
    }
    
    @Test public void testMultiplyDecimalInteger()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeInteger(2) ;
        NodeValue r = XSDFuncOp.numMultiply(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 7L, r.getDecimal().longValue()) ;
    }
    
    @Test public void testCompare1()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeInteger(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7)) ;

        NodeValue nv5b = NodeValue.makeInteger(5) ;
        assertEquals("Does not compare "+nv5+" & "+nv5b, NodeValue.CMP_EQUAL, NodeValue.compare(nv5, nv5b)) ;
    }
    
    @Test public void testCompare2()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeNodeInteger(7) ; 
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;

        NodeValue nv5b = NodeValue.makeNodeInteger(5) ;
        assertEquals("Does not compare "+nv5+" & "+nv5b, NodeValue.CMP_EQUAL, NodeValue.compare(nv5, nv5b) ) ;
    }
    
    @Test public void testCompare3()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeDouble(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;
    }

    @Test public void testCompare4()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeFloat(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;
    }

    @Test public void testCompare5()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeDecimal(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;
    }
    
    
    @Test public void testCompare10()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2005-10-14T13:09:43Z") ;
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T14:09:43Z") ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compare(nv1, nv2) ) ;
    }

    @Test public void testCompare11()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2005-10-14T13:09:43-08:00") ; // Different timezones
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T13:09:43+01:00") ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2) ) ;
    }

    @Test public void testCompare12()
    {
        if ( ! ARQ.isTrue(ARQ.strictSPARQL) )
        {
            NodeValue nv1 = NodeValue.makeDate("2006-07-21-08:00") ; // Different timezones
            NodeValue nv2 = NodeValue.makeNodeDate("2006-07-21+01:00") ;
            assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2) ) ;
        }
    }


    @Test public void testCompare15()
    {
        NodeValue nv1 = NodeValue.makeDate("2005-10-14Z") ;
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T14:09:43Z") ;
        try {
            NodeValue.compare(nv1, nv2) ;
            assertFalse("Compared the uncomparable: "+nv1+" & "+nv2, true) ;
        } catch (ExprNotComparableException ex)
        {}
    }
    
    @Test public void testCompare16()
    {
        // One in a timezone, one not.  Within +/- 14 hours.  Can't compare.
        NodeValue nv1 = NodeValue.makeDateTime("2007-08-31T16:20:03") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-08-31T16:20:03Z") ;
        try {
            NodeValue.compare(nv1, nv2) ;
            assertFalse("Compared the uncomparable: "+nv1+" & "+nv2, true) ;
        } catch (ExprNotComparableException ex)
        {}
    }

    @Test public void testCompare17()
    {
        // One in a timezone, one not.  Within +/- 14 hours.  Can't compare.
        NodeValue nv1 = NodeValue.makeDate("2007-08-31") ;
        NodeValue nv2 = NodeValue.makeDate("2007-08-31Z") ;
        try {
            NodeValue.compare(nv1, nv2) ;
            assertFalse("Compared the uncomparable: "+nv1+" & "+nv2, true) ;
        } catch (ExprNotComparableException ex)
        {}
    }
    
    @Test public void testCompare18()
    {
        // One in a timezone, one not.  More than +/- 14 hours.  Can compare.
        NodeValue nv1 = NodeValue.makeDateTime("2007-08-31T16:20:03") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-08-31T01:20:03Z") ;
        assertEquals(Expr.CMP_GREATER, NodeValue.compare(nv1, nv2)) ;
    }

    
    @Test public void testCompare20()
    {
        NodeValue nv1 = NodeValue.makeString("abcd") ;
        NodeValue nv2 = NodeValue.makeNodeString("abc") ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2) ) ;
    }

    @Test public void testCompare21()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeString("5") ;
        
        try {
            NodeValue.compare(nv5, nv7) ;
            fail("Should not compare (but did) "+nv5+" & "+nv7) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
            
        int x = NodeValue.compareAlways(nv5, nv7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_GREATER, NodeValue.compareAlways(nv5, nv7) ) ;
    }
    
    @Test public void testCompare22()
    {
        NodeValue nv1 = NodeValue.makeNodeString("aaa") ;
        NodeValue nv2 = NodeValue.makeString("aaabbb") ;
        
        int x = NodeValue.compare(nv1, nv2) ;
        assertEquals("Not CMP_LESS", x, Expr.CMP_LESS) ;
        assertTrue("It's CMP_GREATER", x != Expr.CMP_GREATER) ;
        assertTrue("It's CMP_EQUAL", x != Expr.CMP_EQUAL) ;
    }

    @Test public void testCompare23()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createAnon()) ;
        NodeValue nv2 = NodeValue.makeString("5") ;
        
        try {
            NodeValue.compare(nv1, nv2) ;
            fail("Should not compare (but did) "+nv1+" & "+nv2) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
    }
    

    @Test public void testSameUnknown_1()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createURI("test:abc")) ; 
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc")) ;
        
        assertTrue(NodeValue.sameAs(nv1, nv2)) ; 
        assertFalse(NodeValue.notSameAs(nv1, nv2)) ;
        try {
            NodeValue.compare(nv1, nv2) ;
            fail("Should not compare (but did) "+nv1+" & "+nv2) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
            
    }
    
    @Test public void testSameUnknown_2()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createAnon()) ; 
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc")) ;
        
        assertFalse(NodeValue.sameAs(nv1, nv2)) ;
        assertTrue(NodeValue.notSameAs(nv1, nv2)) ;
        try {
            NodeValue.compare(nv1, nv2) ;
            fail("Should not compare (but did) "+nv1+" & "+nv2) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
    }

    // ---- sameValueAs -- xsd:dateTime

    // SameValue and compare of date and dateTimes
    // Timezone tricknesses - if one has a TZ and the other has not, then a difference of 14 hours
    // is needed for a comparison.  

    @Test public void testSameDateTime_1()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T09:22:03") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T09:22:03") ;
        
        assertTrue(NodeValue.sameAs(nv1, nv2)) ;
        assertFalse(NodeValue.notSameAs(nv1, nv2)) ;
    }
    
    @Test public void testSameDateTime_2()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T09:22:03") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T19:00:00") ;
        
        assertFalse(NodeValue.sameAs(nv1, nv2)) ;
        assertTrue(NodeValue.notSameAs(nv1, nv2)) ;
    }

    
    @Test public void testSameDateTime_3()
    {
        // These are the same.
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T10:22:03+01:00") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T09:22:03Z") ;
        
        assertTrue(NodeValue.sameAs(nv1, nv2)) ;
        assertFalse(NodeValue.notSameAs(nv1, nv2)) ;
    }
    
    @Test public void testSameDateTime_4()
    {
        // These are not the same.
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T10:22:03+01:00") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T10:22:03Z") ;
        
        assertFalse(NodeValue.sameAs(nv1, nv2)) ;
        assertTrue(NodeValue.notSameAs(nv1, nv2)) ;
    }

    @Test public void testSameDateTime_5()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2007-09-04T10:22:03+01:00") ;
        NodeValue nv2 = NodeValue.makeDateTime("2007-09-04T09:22:03") ;     // No timezone
        
        try { 
            NodeValue.sameAs(nv1, nv2) ;
            fail("Should not sameValueAs (but did) "+nv1+" & "+nv2) ;
        } catch (ExprEvalException ex) {}
        
        try { 
            NodeValue.notSameAs(nv1, nv2) ;
            fail("Should not notSameValueAs (but did) "+nv1+" & "+nv2) ;
        } catch (ExprEvalException ex) {}
    }
    
    // ---- sameValueAs -- xsd:date
    
    @Test public void testSameDate_1()
    {
        NodeValue nv1 = NodeValue.makeDate("2007-09-04") ;
        NodeValue nv2 = NodeValue.makeDate("2007-09-04") ;
        
        assertTrue(NodeValue.sameAs(nv1, nv2)) ;
        assertFalse(NodeValue.notSameAs(nv1, nv2)) ;
    }
    
    @Test public void testSameDate_2()
    {
        NodeValue nv1 = NodeValue.makeDate("2007-09-04Z") ;
        NodeValue nv2 = NodeValue.makeDate("2007-09-04+00:00") ;
        
        assertTrue(NodeValue.sameAs(nv1, nv2)) ;
        assertFalse(NodeValue.notSameAs(nv1, nv2)) ;
    }
    
    
    @Test public void testSameDate_3()
    {
        NodeValue nv1 = NodeValue.makeDate("2007-09-04Z") ;
        NodeValue nv2 = NodeValue.makeDate("2007-09-04") ;     // No timezone
        
        try { 
            NodeValue.sameAs(nv1, nv2) ;
            fail("Should not sameValueAs (but did) "+nv1+" & "+nv2) ;
        } catch (ExprEvalException ex) {}
        
        try { 
            NodeValue.notSameAs(nv1, nv2) ;
            fail("Should not notSameValueAs (but did) "+nv1+" & "+nv2) ;
        } catch (ExprEvalException ex) {}
    }
    
    
    // General comparisons for sorting.
    
    // bnodes < URIs < literals
    
    @Test public void testCompareGeneral1()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createAnon()) ;
        NodeValue nv2 = NodeValue.makeString("5") ;
        
        // bNodes before strings
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2) ) ;
    }
    
    @Test public void testCompareGeneral2()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createAnon()) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc")) ;
        
        // bNodes before URIs
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2) ) ;
    }

    @Test public void testCompareGeneral3()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("test:abc")) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:abc")) ;
        
        // URIs before literals
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compareAlways(nv1, nv2) ) ;
    }

    @Test public void testCompareGeneral4()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createURI("test:abc")) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createURI("test:xyz")) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2) ) ;
    }

    // abs is a test of Function.unaryOp machinary 
    @Test public void testAbs1()
    {
        NodeValue nv = NodeValue.makeInteger(2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 2, r.getInteger().longValue() ) ;
    }
    
    @Test public void testAbs2()
    {
        NodeValue nv = NodeValue.makeInteger(-2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 2, r.getInteger().longValue() ) ;
    }
    
    @Test public void testAbs3()
    {
        NodeValue nv = NodeValue.makeDecimal(2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 2, r.getDecimal().doubleValue(), accuracyExact_D ) ;
    }
    
    @Test public void testAbs4()
    {
        NodeValue nv = NodeValue.makeDecimal(-2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 2, r.getDecimal().doubleValue(), accuracyExact_D ) ;
    }
    
    @Test public void testAbs5()
    {
        NodeValue nv = NodeValue.makeFloat(2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not an float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 2, r.getFloat(), accuracyExact_F ) ;
    }

    @Test public void testAbs6()
    {
        NodeValue nv = NodeValue.makeFloat(-2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not an float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 2, r.getFloat(), accuracyExact_F ) ;
    }

    @Test public void testAbs7()
    {
        NodeValue nv = NodeValue.makeDouble(2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not an double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 2, r.getDouble(), accuracyExact_D ) ;
    }

    @Test public void testAbs8()
    {
        NodeValue nv = NodeValue.makeDouble(-2) ;
        NodeValue r = XSDFuncOp.abs(nv) ;
        assertTrue("Not an double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 2d, r.getDouble(), accuracyExact_D) ;
    }

    @Test public void testCeiling1()
    {
        NodeValue nv = NodeValue.makeDecimal(2.6) ;
        NodeValue r = XSDFuncOp.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 3, r.getDecimal().longValue()) ;
    }
    
    @Test public void testCeiling2()
    {
        NodeValue nv = NodeValue.makeDecimal(-3.6) ;
        NodeValue r = XSDFuncOp.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", -3, r.getDecimal().longValue() ) ;
    }
    
    @Test public void testCeiling3()
    {
        NodeValue nv = NodeValue.makeDouble(2.6) ;
        NodeValue r = XSDFuncOp.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 3, r.getDouble(), accuracyExact_D ) ;
    }
    
    @Test public void testCeiling4()
    {
        NodeValue nv = NodeValue.makeDouble(-3.6) ;
        NodeValue r = XSDFuncOp.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", -3, r.getDouble(), accuracyExact_D ) ;
    }

    @Test public void testCeiling5()
    {
        NodeValue nv = NodeValue.makeInteger(3) ;
        NodeValue r = XSDFuncOp.ceiling(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 3, r.getInteger().longValue() ) ;
    }
    
    @Test public void testFloor1()
    {
        NodeValue nv = NodeValue.makeDecimal(2.6) ;
        NodeValue r = XSDFuncOp.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 2, r.getDecimal().longValue()) ;
    }
    
    @Test public void testFloor2()
    {
        NodeValue nv = NodeValue.makeDecimal(-3.6) ;
        NodeValue r = XSDFuncOp.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", -4, r.getDecimal().longValue() ) ;
    }
    
    @Test public void testFloor3()
    {
        NodeValue nv = NodeValue.makeDouble(2.6) ;
        NodeValue r = XSDFuncOp.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 2, r.getDouble(), accuracyExact_D ) ;
    }
    
    @Test public void testFloor4()
    {
        NodeValue nv = NodeValue.makeDouble(-3.6) ;
        NodeValue r = XSDFuncOp.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", -4, r.getDouble(), accuracyExact_D ) ;
    }
    
    @Test public void testFloor5()
    {
        NodeValue nv = NodeValue.makeInteger(3) ;
        NodeValue r = XSDFuncOp.floor(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 3, r.getInteger().longValue() ) ;
    }

    @Test public void testSqrt1()
    {
        NodeValue four = NodeValue.makeInteger(4) ;
        NodeValue two = NodeValue.makeInteger(2) ;
        NodeValue result = XSDFuncOp.sqrt( four ) ;
        
        assertTrue(result.isDecimal()) ;
        assertTrue( NodeValue.sameAs( two, result)) ;
        assertTrue( two.asNode().sameValueAs(result.asNode()) ) ;
    }
    
    @Test public void testSqrt2()
    {
        NodeValue four = NodeValue.makeDouble(4) ;
        NodeValue two = NodeValue.makeInteger(2) ;
        NodeValue result = XSDFuncOp.sqrt( four ) ;

        assertTrue(result.isDouble()) ;
        assertTrue( NodeValue.sameAs( two, result)) ;
        
        assertNotNull(result.asNode()) ;
        
        //assertTrue( two.asNode().sameValueAs(result.asNode()) ) ;
    }
    
    // All compatible - no timezone.
    private static NodeValue nv_dt = NodeValue.makeNode("2010-03-22T20:31:54.5", XSDDatatype.XSDdateTime) ;
    private static NodeValue nv_d = NodeValue.makeNode("2010-03-22", XSDDatatype.XSDdate) ;
    private static NodeValue nv_gy = NodeValue.makeNode("2010", XSDDatatype.XSDgYear) ;
    private static NodeValue nv_gym = NodeValue.makeNode("2010-03", XSDDatatype.XSDgYearMonth) ;
    
    private static NodeValue nv_gmd = NodeValue.makeNode("--03-22", XSDDatatype.XSDgMonthDay) ;
    private static NodeValue nv_gm = NodeValue.makeNode("--03", XSDDatatype.XSDgMonth) ;
    private static NodeValue nv_gd = NodeValue.makeNode("---22", XSDDatatype.XSDgDay) ;
    private static NodeValue nv_t = NodeValue.makeNode("20:31:54.5", XSDDatatype.XSDtime) ;
    
    private static void testDateTimeCast(NodeValue nv, XSDDatatype xsd, NodeValue nvResult )
    {
        NodeValue nv2 = XSDFuncOp.dateTimeCast(nv, xsd) ;
        Assert.assertEquals(nvResult, nv2) ;
    }
    
    // datetime to other
    @Test public void cast_gregorian_01() { testDateTimeCast(nv_dt, XSDDatatype.XSDdateTime, nv_dt) ; }
    @Test public void cast_gregorian_02() { testDateTimeCast(nv_dt, XSDDatatype.XSDdate, nv_d) ; }
    @Test public void cast_gregorian_03() { testDateTimeCast(nv_dt, XSDDatatype.XSDgYear, nv_gy) ; }
    @Test public void cast_gregorian_04() { testDateTimeCast(nv_dt, XSDDatatype.XSDgYearMonth, nv_gym) ; }
    @Test public void cast_gregorian_05() { testDateTimeCast(nv_dt, XSDDatatype.XSDgMonthDay, nv_gmd) ; }
    @Test public void cast_gregorian_06() { testDateTimeCast(nv_dt, XSDDatatype.XSDgMonth, nv_gm) ; }
    @Test public void cast_gregorian_07() { testDateTimeCast(nv_dt, XSDDatatype.XSDgDay, nv_gd) ; }
    
    @Test public void cast_gregorian_08() { testDateTimeCast(nv_dt, XSDDatatype.XSDtime, nv_t) ; }

    // date to other
    @Test public void cast_gregorian_10() { testDateTimeCast(nv_d, XSDDatatype.XSDdateTime, NodeValue.makeNode("2010-03-22T00:00:00", XSDDatatype.XSDdateTime)) ; }
    @Test public void cast_gregorian_11() { testDateTimeCast(nv_d, XSDDatatype.XSDdate, nv_d) ; }
    @Test public void cast_gregorian_12() { testDateTimeCast(nv_d, XSDDatatype.XSDgYear, nv_gy) ; }
    @Test public void cast_gregorian_13() { testDateTimeCast(nv_d, XSDDatatype.XSDgYearMonth, nv_gym) ; }
    @Test public void cast_gregorian_14() { testDateTimeCast(nv_d, XSDDatatype.XSDgMonthDay, nv_gmd) ; }
    @Test public void cast_gregorian_15() { testDateTimeCast(nv_d, XSDDatatype.XSDgMonth, nv_gm) ; }
    @Test public void cast_gregorian_16() { testDateTimeCast(nv_d, XSDDatatype.XSDgDay, nv_gd) ; }

    // G* to self
    @Test public void cast_gregorian_21() { testDateTimeCast(nv_gym, XSDDatatype.XSDgYearMonth, nv_gym) ; }
    @Test public void cast_gregorian_22() { testDateTimeCast(nv_gy, XSDDatatype.XSDgYear, nv_gy) ; }
    @Test public void cast_gregorian_23() { testDateTimeCast(nv_gmd, XSDDatatype.XSDgMonthDay, nv_gmd) ; }
    @Test public void cast_gregorian_24() { testDateTimeCast(nv_gm, XSDDatatype.XSDgMonth, nv_gm) ; }
    @Test public void cast_gregorian_25() { testDateTimeCast(nv_gd, XSDDatatype.XSDgDay, nv_gd) ; }

    // G* to date
    
    @Test(expected=ExprEvalTypeException.class)
    public void cast_gregorian_31()     { testDateTimeCast(nv_gym, XSDDatatype.XSDdate, nv_d) ; }
    
    @Test(expected=ExprEvalTypeException.class)
    public void cast_gregorian_32()     { testDateTimeCast(nv_gy, XSDDatatype.XSDdate, NodeValue.makeDate("2010-01-01")) ; }
    
    @Test(expected=ExprEvalTypeException.class)
    public void cast_gregorian_33()     { testDateTimeCast(nv_gmd, XSDDatatype.XSDdate, nv_d) ; }
    
    @Test(expected=ExprEvalTypeException.class)
    public void cast_gregorian_34()     { testDateTimeCast(nv_gm, XSDDatatype.XSDdate, nv_d) ; }
    
    @Test(expected=ExprEvalTypeException.class)
    public void cast_gregorian_35()     { testDateTimeCast(nv_gd, XSDDatatype.XSDdate, nv_d) ; }

    // Junk to date/time thing.
    @Test (expected=ExprEvalTypeException.class)
    public void cast_err_gregorian_01() { testDateTimeCast(NodeValue.makeBoolean(false), XSDDatatype.XSDgDay, nv_gd) ; }
    
    private static NodeValue nv_dt_tz1 = NodeValue.makeNode("2010-03-22T20:31:54.5+01:00", XSDDatatype.XSDdateTime) ;
    private static NodeValue nv_dt_tz2 = NodeValue.makeNode("2010-03-22T20:31:54.5-05:00", XSDDatatype.XSDdateTime) ;
    private static NodeValue nv_dt_tz3 = NodeValue.makeNode("2010-03-22T20:31:54.5Z", XSDDatatype.XSDdateTime) ;
    
    private static NodeValue nv_d_tz1 = NodeValue.makeNode("2010-03-22+01:00", XSDDatatype.XSDdate) ;
    private static NodeValue nv_d_tz2 = NodeValue.makeNode("2010-03-22-05:00", XSDDatatype.XSDdate) ;
    private static NodeValue nv_d_tz3 = NodeValue.makeNode("2010-03-22Z", XSDDatatype.XSDdate) ;
    
    private static NodeValue nv_t_tz1 = NodeValue.makeNode("20:31:54.5+01:00", XSDDatatype.XSDtime) ;
    private static NodeValue nv_t_tz2 = NodeValue.makeNode("20:31:54.5-05:00", XSDDatatype.XSDtime) ;
    private static NodeValue nv_t_tz3 = NodeValue.makeNode("20:31:54.5Z", XSDDatatype.XSDtime) ;
    
    @Test public void cast_date_tz_01() { testDateTimeCast(nv_dt_tz1, XSDDatatype.XSDdate, nv_d_tz1) ; }
    @Test public void cast_date_tz_02() { testDateTimeCast(nv_dt_tz2, XSDDatatype.XSDdate, nv_d_tz2) ; }
    @Test public void cast_date_tz_03() { testDateTimeCast(nv_dt_tz3, XSDDatatype.XSDdate, nv_d_tz3) ; }
    
    @Test public void cast_time_tz_01() { testDateTimeCast(nv_dt_tz1, XSDDatatype.XSDtime, nv_t_tz1) ; }
    @Test public void cast_time_tz_02() { testDateTimeCast(nv_dt_tz2, XSDDatatype.XSDtime, nv_t_tz2) ; }
    @Test public void cast_time_tz_03() { testDateTimeCast(nv_dt_tz3, XSDDatatype.XSDtime, nv_t_tz3) ; }
}

