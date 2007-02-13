/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.test.suites;

import junit.framework.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.expr.ExprTypeException;
import com.hp.hpl.jena.query.expr.NodeValue;
import com.hp.hpl.jena.query.expr.nodevalue.*;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author Andy Seaborne
 * @version $Id: TestNodeFunctions.java,v 1.7 2007/01/02 11:18:18 andy_seaborne Exp $
 */

public class TestNodeFunctions extends TestCase
{
    private static final double accuracyExact = 0.0d ;
    private static final double accuracyClose = 0.000001d ;
    
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestNodeFunctions.class) ;
        ts.setName(Utils.classShortName(TestNodeFunctions.class)) ;
        return ts ;
    }

    public void testStr1()
    {
        NodeValue nv = NodeValue.makeNodeInteger(56) ;
        NodeValue s = NodeFunctions.str(nv) ;
        assertEquals("56", s.getString()) ;
    }
    
    public void testStr2()
    {
        NodeValue nv = NodeValue.makeInteger(56) ;
        NodeValue s = NodeFunctions.str(nv) ;
        assertEquals("56", s.getString()) ;
    }

    public void testStr3()
    {
        NodeValue nv = NodeValue.makeNode("abc", "fr", (String)null) ;
        NodeValue s = NodeFunctions.str(nv) ;
        assertEquals("abc", s.getString()) ;
    }

    public void testStr4()
    {
        Node n = Node.createAnon() ;
        try {
            String s = NodeFunctions.str(n) ;
            fail("Expect a type exception but call succeeded") ;
        }
        catch (ExprTypeException ex) {} 
    }
    
    public void testDatatype1()
    {
        NodeValue nv = NodeValue.makeInteger(5) ;
        Node n = nv.asNode() ;
        Node r = NodeFunctions.datatype(n) ;
        assertEquals(XSD.integer.asNode(), r) ;
    }

    public void testDatatype2()
    {
        NodeValue nv = NodeValue.makeInteger(5) ;
        NodeValue r = NodeFunctions.datatype(nv) ;
        NodeValue e = NodeValue.makeNode(XSD.integer.asNode()) ;
        assertEquals(e, r) ;
    }

    public void testDatatype3()
    {
        NodeValue nv = NodeValue.makeString("abc") ;
        NodeValue r = NodeFunctions.datatype(nv) ;
        NodeValue e = NodeValue.makeNode(XSD.xstring.asNode()) ;
        assertEquals(e, r) ;
    }

    public void testDatatype4()
    {
        NodeValue nv = NodeValue.makeNode("abc", "fr", (String)null) ;
        try {
            NodeValue r = NodeFunctions.datatype(nv) ;
            fail("Expect a type exception but call succeeded") ;
        }
        catch (ExprTypeException ex) {} 
    }

    public void testDatatype5()
    {
        try {
            NodeValue nv = NodeValue.makeNode(Node.createURI("http://example")) ;
            NodeValue r = NodeFunctions.datatype(nv) ;
            fail("Expect a type exception but call succeeded") ;
        }
        catch (ExprTypeException ex) {} 
    }

    public void testDatatype6()
    {
        NodeValue nv = NodeValue.makeNode(Node.createAnon()) ;
        try {
            NodeValue r = NodeFunctions.datatype(nv) ;
            fail("Expect a type exception but call succeeded") ;
        }
        catch (ExprTypeException ex) {} 
    }

    public void testLang1()
    {
        Node n = Node.createLiteral("abc", "en-gb", null) ;
        assertEquals("en-gb", NodeFunctions.lang(n)) ;
    }

    public void testLang2()
    {
        NodeValue nv = NodeValue.makeNode("abc", "en", (String)null) ;
        NodeValue r = NodeFunctions.lang(nv) ;
        NodeValue e = NodeValue.makeString("en") ;
        assertEquals(e, r) ;
    }

    public void testLang3()
    {
        NodeValue nv = NodeValue.makeInteger(5) ;
        NodeValue r = NodeFunctions.lang(nv) ;
        NodeValue e = NodeValue.makeString("") ;
        assertEquals(e, r) ;
    }
    
    public void testLang4()
    {
        NodeValue nv = NodeValue.makeNode(Node.createLiteral("simple")) ;
        NodeValue r = NodeFunctions.lang(nv) ;
        NodeValue e = NodeValue.makeString("") ;
        assertEquals(e, r) ;
    }
    
    public void testLang5()
    {
        NodeValue nv = NodeValue.makeNode(Node.createURI("http://example/")) ;
        try {
            NodeValue r = NodeFunctions.lang(nv) ;
            fail("Expect a type exception but call succeeded") ;
        }
        catch (ExprTypeException ex) {} 
    }
    
    public void testLangMatches1()
    {
        NodeValue nv = NodeValue.makeString("en") ;
        NodeValue pat = NodeValue.makeString("en") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
        assertFalse(NodeValue.FALSE.equals(r)) ;
    }

    public void testLangMatches2()
    {
        NodeValue nv = NodeValue.makeString("en") ;
        NodeValue pat = NodeValue.makeString("fr") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
        assertFalse(NodeValue.TRUE.equals(r)) ;
    }

    public void testLangMatches3()
    {
        NodeValue nv = NodeValue.makeString("en-gb") ;
        NodeValue pat = NodeValue.makeString("en-gb") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    public void testLangMatches4()
    {
        NodeValue nv = NodeValue.makeString("en-gb") ;
        NodeValue pat = NodeValue.makeString("en") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    public void testLangMatches5()
    {
        NodeValue nv = NodeValue.makeString("abc") ;
        NodeValue pat = NodeValue.makeString("*") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    public void testLangMatches6()
    {
        NodeValue nv = NodeValue.makeString("x-y-z") ;
        NodeValue pat = NodeValue.makeString("x") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    public void testLangMatches7()
    {
        NodeValue nv = NodeValue.makeString("x") ;
        NodeValue pat = NodeValue.makeString("x-y-z") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    public void testLangMatches8()
    {
        // The language tag of a plain literal is ""
        // A language tag is not allowed to be the empty string (by RFC 3066)
        NodeValue nv = NodeValue.makeString("") ;
        NodeValue pat = NodeValue.makeString("*") ;
        NodeValue r = NodeFunctions.langMatches(nv, pat) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    public void testIsIRI_1()
    {
        NodeValue nv = NodeValue.makeNode(Node.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.isIRI(nv) ;
        assertEquals(NodeValue.TRUE, r) ;
    }
    
    public void testIsIRI_2()
    {
        NodeValue nv = NodeValue.makeNode(Node.createLiteral("http://example/")) ;
        NodeValue r = NodeFunctions.isIRI(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
    }
    
    public void testIsBlank1()
    {
        NodeValue nv = NodeValue.makeNode(Node.createAnon());
        NodeValue r = NodeFunctions.isBlank(nv) ;
        assertEquals(NodeValue.TRUE, r) ;
        
    }

    public void testIsBlank2()
    {
        NodeValue nv = NodeValue.makeNode(Node.createLiteral("xyz")) ;
        NodeValue r = NodeFunctions.isBlank(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
    }

    public void testIsBlank3()
    {
        NodeValue nv = NodeValue.makeNode(Node.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.isBlank(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
        
    }
    
    public void testIsLiteral1()
    {
        NodeValue nv = NodeValue.makeNode(Node.createLiteral("xyz")) ;
        NodeValue r = NodeFunctions.isLiteral(nv) ;
        assertEquals(NodeValue.TRUE, r) ;
    }

    public void testIsLiteral2()
    {
        NodeValue nv = NodeValue.makeNode(Node.createURI("http://example/")) ;
        NodeValue r = NodeFunctions.isLiteral(nv) ;
        assertEquals(NodeValue.FALSE, r) ;
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