/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.test.suites;

import junit.framework.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.expr.*;
import com.hp.hpl.jena.query.expr.nodevalue.*;
import com.hp.hpl.jena.query.util.Utils;


/** com.hp.hpl.jena.query.test.TestNodeValue
 * 
 * @author Andy Seaborne
 * @version $Id: TestFunctions.java,v 1.12 2007/01/02 11:18:17 andy_seaborne Exp $
 */

public class TestFunctions extends TestCase
{
    private static final double accuracyExact_D = 0.0d ;
    private static final double accuracyExact_F = 0.0d ;
    private static final double accuracyClose_D = 0.000001d ;
    private static final double accuracyClose_F = 0.000001f ;
    
    // Many tests are in TestExpressionARQ by using the parser to build NodeValues
    // Just some basic testing here.
    
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestFunctions.class) ;
        ts.setName(Utils.classShortName(TestFunctions.class)) ;
        return ts ;
    }

    // These add tests also test that the right kind of operation was done.
    
    public void testAddIntegerInteger()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeInteger(7) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 12, r.getInteger().longValue()) ;
    }

    public void testAddDecimalDecimal()
    {
        NodeValue nv1 = NodeValue.makeDecimal(4.3) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.7) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 8, r.getDecimal().doubleValue(), accuracyExact_D ) ;
    }
    
    public void testAddFloatFloat()
    {
        NodeValue nv1 = NodeValue.makeFloat(7.5f) ;
        NodeValue nv2 = NodeValue.makeFloat(2.5f) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a float: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 10, r.getFloat(), accuracyExact_F ) ;
        assertEquals("Wrong result (as doubles)", 10, r.getDouble(), accuracyExact_D ) ;
    }

    public void testAddDoubleDouble()
    {
        NodeValue nv1 = NodeValue.makeDouble(7.5) ;
        NodeValue nv2 = NodeValue.makeDouble(2.5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 10, r.getDouble(), accuracyExact_D ) ;
    }

    
    public void testAddIntegerDecimal()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeDecimal(7) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 12, r.getDecimal().longValue()) ;
    }
    
    public void testAddDecimalInteger()
    {
        NodeValue nv1 = NodeValue.makeDecimal(7) ;
        NodeValue nv2 = NodeValue.makeInteger(5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 12, r.getDecimal().longValue()) ;
    }
    
    public void testAddIntegerFloat()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeFloat(7) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_F ) ;
    }
    
    public void testAddFloatInteger()
    {
        NodeValue nv1 = NodeValue.makeFloat(7) ;
        NodeValue nv2 = NodeValue.makeInteger(5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_F ) ;
    }

    public void testAddIntegerDouble()
    {
        NodeValue nv1 = NodeValue.makeInteger(5) ;
        NodeValue nv2 = NodeValue.makeDouble(7) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_D ) ;
    }
    
    public void testAddDoubleInteger()
    {
        NodeValue nv1 = NodeValue.makeDouble(7) ;
        NodeValue nv2 = NodeValue.makeInteger(5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 12, r.getDouble(), accuracyExact_D ) ;
    }

    public void testAddDecimalFloat()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeFloat(4.5f) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a Float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 8, r.getFloat(), accuracyExact_F) ;
    }
    
    public void testAddFloatDecimal()
    {
        NodeValue nv1 = NodeValue.makeFloat(4.5f) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a Float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 8, r.getFloat(), accuracyExact_F) ;
    }
    public void testAddDecimalDouble()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeDouble(4.5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D) ;
    }
    
    public void testAddDoubleDecimal()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D ) ;
    }

    public void testAddDoubleFloat()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeFloat(3.5f) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D ) ;
    }

    public void testAddFloatDouble()
    {
        NodeValue nv1 = NodeValue.makeFloat(4.5f) ;
        NodeValue nv2 = NodeValue.makeDouble(3.5d) ;
        NodeValue r = Functions.add(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 8, r.getDouble(), accuracyExact_D ) ;
    }

    // divide(integer, integer) => decimal
    public void testDivideIntegerInteger()
    {
        NodeValue nv1 = NodeValue.makeInteger(25) ;
        NodeValue nv2 = NodeValue.makeInteger(2) ;
        NodeValue r = Functions.divide(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 12.5, r.getDecimal().doubleValue(), accuracyExact_D) ;
    }

    // divide errors
    public void testDivideByZero1()
    {
        NodeValue nv1 = NodeValue.makeInteger(1) ;
        NodeValue nv2 = NodeValue.makeInteger(0) ;
        try {
            NodeValue r = Functions.divide(nv1, nv2) ;
            fail("No expection from .divide") ;
        } catch (ExprEvalException ex)
        { }
    }
    
    public void testDivideByZero2()
    {
        NodeValue nv1 = NodeValue.makeInteger(1) ;
        NodeValue nv2 = NodeValue.makeDouble(0) ;
        NodeValue r = Functions.divide(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a +INF: "+r, r.getDouble()==Double.POSITIVE_INFINITY) ;
    }
    
    public void testDivideByZero4()
    {
        NodeValue nv1 = NodeValue.makeInteger(-1) ;
        NodeValue nv2 = NodeValue.makeDouble(-0) ;
        NodeValue r = Functions.divide(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a -INF: "+r, r.getDouble()==Double.NEGATIVE_INFINITY) ;
    }
    
    public void testSubtractDoubleDecimal()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = Functions.subtract(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 1d, r.getDouble(), accuracyExact_D ) ;
    }
    
    public void testSubtractDecimalInteger()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeInteger(2) ;
        NodeValue r = Functions.subtract(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertTrue("Wrong result", NodeValue.sameAs(NodeValue.makeDecimal(1.5), r) ) ;
    }
    
    public void testMultiplyDoubleDecimal()
    {
        NodeValue nv1 = NodeValue.makeDouble(4.5) ;
        NodeValue nv2 = NodeValue.makeDecimal(3.5) ;
        NodeValue r = Functions.multiply(nv1, nv2) ;
        assertTrue("Not a double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 4.5d*3.5d, r.getDouble(), accuracyExact_D ) ;
    }
    
    public void testMultiplyDecimalInteger()
    {
        NodeValue nv1 = NodeValue.makeDecimal(3.5) ;
        NodeValue nv2 = NodeValue.makeInteger(2) ;
        NodeValue r = Functions.multiply(nv1, nv2) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 7L, r.getDecimal().longValue()) ;
    }
    
    public void testCompare1()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeInteger(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7)) ;

        NodeValue nv5b = NodeValue.makeInteger(5) ;
        assertEquals("Does not compare "+nv5+" & "+nv5b, NodeValue.CMP_EQUAL, NodeValue.compare(nv5, nv5b)) ;
    }
    
    public void testCompare2()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeNodeInteger(7) ; 
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;

        NodeValue nv5b = NodeValue.makeNodeInteger(5) ;
        assertEquals("Does not compare "+nv5+" & "+nv5b, NodeValue.CMP_EQUAL, NodeValue.compare(nv5, nv5b) ) ;
    }
    
    public void testCompare3()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeDouble(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;
    }

    public void testCompare4()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeFloat(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;
    }

    public void testCompare5()
    {
        NodeValue nv5 = NodeValue.makeInteger(5) ;
        NodeValue nv7 = NodeValue.makeDecimal(7) ;
        assertEquals("Does not compare "+nv5+" & "+nv7, NodeValue.CMP_LESS, NodeValue.compare(nv5, nv7) ) ;
    }
    
    
    public void testCompare10()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2005-10-14T13:09:43Z") ;
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T14:09:43Z") ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compare(nv1, nv2) ) ;
    }

    public void testCompare11()
    {
        NodeValue nv1 = NodeValue.makeDateTime("2005-10-14T13:09:43-08:00") ; // Different timezones
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T13:09:43+01:00") ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2) ) ;
    }

    public void testCompare12()
    {
        if ( ! ARQ.isTrue(ARQ.strictSPARQL) )
        {
            NodeValue nv1 = NodeValue.makeDate("2006-07-21-08:00") ; // Different timezones
            NodeValue nv2 = NodeValue.makeNodeDate("2006-07-21+01:00") ;
            assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2) ) ;
        }
    }


    public void testCompare15()
    {
        NodeValue nv1 = NodeValue.makeDate("2005-10-14Z") ;
        NodeValue nv2 = NodeValue.makeNodeDateTime("2005-10-14T14:09:43Z") ;
        try {
            NodeValue.compare(nv1, nv2) ;
            assertFalse("Compared the uncomparable: "+nv1+" & "+nv2, true) ;
        } catch (ExprNotComparableException ex)
        {}
    }

    
    
    public void testCompare20()
    {
        NodeValue nv1 = NodeValue.makeString("abcd") ;
        NodeValue nv2 = NodeValue.makeNodeString("abc") ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compare(nv1, nv2) ) ;
    }

    public void testCompare21()
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
    
    public void testCompare22()
    {
        NodeValue nv1 = NodeValue.makeNodeString("aaa") ;
        NodeValue nv2 = NodeValue.makeString("aaabbb") ;
        
        int x = NodeValue.compare(nv1, nv2) ;
        assertEquals("Not CMP_LESS", x, Expr.CMP_LESS) ;
        assertTrue("It's CMP_GREATER", x != Expr.CMP_GREATER) ;
        assertTrue("It's CMP_EQUAL", x != Expr.CMP_EQUAL) ;
    }

    public void testCompare23()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createAnon()) ;
        NodeValue nv2 = NodeValue.makeString("5") ;
        
        try {
            NodeValue.compare(nv1, nv2) ;
            fail("Should not compare (but did) "+nv1+" & "+nv2) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
    }
    

    public void testSame1()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createURI("test:abc")) ; 
        NodeValue nv2 = NodeValue.makeNode(Node.createURI("test:abc")) ;
        
        assertTrue(NodeValue.sameAs(nv1, nv2)) ; 
        assertFalse(NodeValue.notSameAs(nv1, nv2)) ;
        try {
            NodeValue.compare(nv1, nv2) ;
            fail("Should not compare (but did) "+nv1+" & "+nv2) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
            
    }
    
    public void testSame2()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createAnon()) ; 
        NodeValue nv2 = NodeValue.makeNode(Node.createURI("test:abc")) ;
        
        assertFalse(NodeValue.sameAs(nv1, nv2)) ;
        assertTrue(NodeValue.notSameAs(nv1, nv2)) ;
        try {
            NodeValue.compare(nv1, nv2) ;
            fail("Should not compare (but did) "+nv1+" & "+nv2) ; 
        } catch (ExprEvalException ex)
        { /* expected */}
    }

    // General comparisons for sorting.
    
    // bnodes < URIs < literals
    
    public void testCompareGeneral1()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createAnon()) ;
        NodeValue nv2 = NodeValue.makeString("5") ;
        
        // bNodes before strings
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2) ) ;
    }
    
    public void testCompareGeneral2()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createAnon()) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createURI("test:abc")) ;
        
        // bNodes before URIs
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2) ) ;
    }

    public void testCompareGeneral3()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("test:abc")) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createURI("test:abc")) ;
        
        // URIs before literals
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_GREATER, NodeValue.compareAlways(nv1, nv2) ) ;
    }

    public void testCompareGeneral4()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createURI("test:abc")) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createURI("test:xyz")) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertEquals("Does not compare "+nv1+" & "+nv2, NodeValue.CMP_LESS, NodeValue.compareAlways(nv1, nv2) ) ;
    }

    // abs is a test of Function.unaryOp machinary 
    public void testAbs1()
    {
        NodeValue nv = NodeValue.makeInteger(2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 2, r.getInteger().longValue() ) ;
    }
    
    public void testAbs2()
    {
        NodeValue nv = NodeValue.makeInteger(-2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 2, r.getInteger().longValue() ) ;
    }
    
    public void testAbs3()
    {
        NodeValue nv = NodeValue.makeDecimal(2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 2, r.getDecimal().doubleValue(), accuracyExact_D ) ;
    }
    
    public void testAbs4()
    {
        NodeValue nv = NodeValue.makeDecimal(-2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 2, r.getDecimal().doubleValue(), accuracyExact_D ) ;
    }
    
    public void testAbs5()
    {
        NodeValue nv = NodeValue.makeFloat(2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not an float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 2, r.getFloat(), accuracyExact_F ) ;
    }

    public void testAbs6()
    {
        NodeValue nv = NodeValue.makeFloat(-2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not an float: "+r, r.isFloat()) ;
        assertTrue("Not a NodeValueFloat: "+r, r instanceof NodeValueFloat) ;
        assertEquals("Wrong result", 2, r.getFloat(), accuracyExact_F ) ;
    }

    public void testAbs7()
    {
        NodeValue nv = NodeValue.makeDouble(2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not an double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 2, r.getDouble(), accuracyExact_D ) ;
    }

    public void testAbs8()
    {
        NodeValue nv = NodeValue.makeDouble(-2) ;
        NodeValue r = Functions.abs(nv) ;
        assertTrue("Not an double: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 2d, r.getDouble(), accuracyExact_D) ;
    }

    public void testCeiling1()
    {
        NodeValue nv = NodeValue.makeDecimal(2.6) ;
        NodeValue r = Functions.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 3, r.getDecimal().longValue()) ;
    }
    
    public void testCeiling2()
    {
        NodeValue nv = NodeValue.makeDecimal(-3.6) ;
        NodeValue r = Functions.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", -3, r.getDecimal().longValue() ) ;
    }
    
    public void testCeiling3()
    {
        NodeValue nv = NodeValue.makeDouble(2.6) ;
        NodeValue r = Functions.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 3, r.getDouble(), accuracyExact_D ) ;
    }
    
    public void testCeiling4()
    {
        NodeValue nv = NodeValue.makeDouble(-3.6) ;
        NodeValue r = Functions.ceiling(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", -3, r.getDouble(), accuracyExact_D ) ;
    }

    public void testCeiling5()
    {
        NodeValue nv = NodeValue.makeInteger(3) ;
        NodeValue r = Functions.ceiling(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 3, r.getInteger().longValue() ) ;
    }
    
    public void testFloor1()
    {
        NodeValue nv = NodeValue.makeDecimal(2.6) ;
        NodeValue r = Functions.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", 2, r.getDecimal().longValue()) ;
    }
    
    public void testFloor2()
    {
        NodeValue nv = NodeValue.makeDecimal(-3.6) ;
        NodeValue r = Functions.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDecimal()) ;
        assertTrue("Not a NodeValueDecimal: "+r, r instanceof NodeValueDecimal) ;
        assertEquals("Wrong result", -4, r.getDecimal().longValue() ) ;
    }
    
    public void testFloor3()
    {
        NodeValue nv = NodeValue.makeDouble(2.6) ;
        NodeValue r = Functions.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", 2, r.getDouble(), accuracyExact_D ) ;
    }
    
    public void testFloor4()
    {
        NodeValue nv = NodeValue.makeDouble(-3.6) ;
        NodeValue r = Functions.floor(nv) ;
        assertTrue("Not a decimal: "+r, r.isDouble()) ;
        assertTrue("Not a NodeValueDouble: "+r, r instanceof NodeValueDouble) ;
        assertEquals("Wrong result", -4, r.getDouble(), accuracyExact_D ) ;
    }
    
    public void testFloor5()
    {
        NodeValue nv = NodeValue.makeInteger(3) ;
        NodeValue r = Functions.floor(nv) ;
        assertTrue("Not an integer: "+r, r.isInteger()) ;
        assertTrue("Not a NodeValueInteger: "+r, r instanceof NodeValueInteger) ;
        assertEquals("Wrong result", 3, r.getInteger().longValue() ) ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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