/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.NodeUtils;

public class TestOrdering extends TestCase
{
    NodeValue nvInt2 = NodeValue.makeNodeInteger(2) ;
    NodeValue nvInt3 = NodeValue.makeNodeInteger("3") ;
    NodeValue nvInt03 = NodeValue.makeNodeInteger("03") ;
    NodeValue nvStr3 = NodeValue.makeNodeString("3") ;
    NodeValue nvStr03 = NodeValue.makeNodeString("03") ;
    
    NodeValue nvInt9 = NodeValue.makeNodeInteger(9) ;
    NodeValue nvPosInt9 = NodeValue.makeNode("9", XSDDatatype.XSDpositiveInteger) ;
    NodeValue nvInt10 = NodeValue.makeNodeInteger(10) ;
    NodeValue nvDouble9 = NodeValue.makeNodeDouble(9.0) ;
    NodeValue nvFloat8 = NodeValue.makeNode("8.0", XSDDatatype.XSDfloat) ;
    
    NodeValue nvByte10 = NodeValue.makeNode("10", XSDDatatype.XSDbyte) ;
    
    Node nInt2 = nvInt2.getNode() ;
    Node nInt3 = nvInt3.getNode() ;
    Node nInt03 = nvInt03.getNode() ;
    Node nStr3 = nvStr3.getNode() ;
    Node nStr03 = nvStr03.getNode() ;
    
    Node nInt9 = nvInt9.getNode() ;
    Node nPosInt9 = nvPosInt9.getNode() ;
    Node nInt10 = nvInt10.getNode() ;
    Node nDouble9 = nvDouble9.getNode() ; 
    Node nFloat8 = nvFloat8.getNode() ;
    Node nByte10 = nvByte10.getNode() ;
    
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestOrdering.class) ;
        ts.setName("TestOrdering") ;
        return ts ;
    }
    
    public void testComp_2_3()
    {
        int x = NodeValue.compareAlways(nvInt2, nvInt3) ;
        assertTrue("2 should be value-less than 3", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nInt2, nInt3) ;
        assertTrue("2 should be strict-less than 3", Expr.CMP_LESS == y ) ;
    }
    
    public void testComp_3_str3()
    {
        int x = NodeValue.compareAlways(nvInt3, nvStr3) ;
        int y = NodeUtils.compareRDFTerms(nInt3, nStr3) ;
        
        assertTrue("3 should be compareAlways greater than \"3\"",  Expr.CMP_GREATER == x) ;
        assertTrue("3 should be syntactic-greater than to \"3\"", Expr.CMP_GREATER == y ) ;
    }

    public void testComp_03_str3()
    {
        int x = NodeValue.compareAlways(nvInt03, nvStr3) ;
        int y = NodeUtils.compareRDFTerms(nInt03, nStr3) ;
        
        assertTrue("03 (typed) should be compareAlways 'less than' than \"3\"",  Expr.CMP_LESS == x ) ;
        assertTrue("03 should be syntactic-less than to \"3\"", Expr.CMP_LESS == y ) ;
    }
    
    // Compare things of different types.
    public void testComp_int_double_1()
    {
        int x = NodeValue.compareAlways(nvInt10, nvDouble9) ;
        int y = NodeUtils.compareRDFTerms(nInt10, nDouble9) ;
        assertTrue("Int 10 less than double 9", Expr.CMP_GREATER == x ) ;
        assertTrue("Int 10 less than double 9 in syntactic compare", Expr.CMP_LESS == y ) ;
    }

    public void testComp_byte_double_1()
    {
        int x = NodeValue.compareAlways(nvByte10, nvDouble9) ;
        int y = NodeUtils.compareRDFTerms(nByte10, nDouble9) ;
        assertTrue("Byte 10 less than double 9", Expr.CMP_GREATER == x ) ;
        assertTrue("Byte 10 greater than double 9 in non-value compare (dataype URIs compare)", Expr.CMP_LESS == y ) ;
    }

    public void testComp_int_float_1()
    {
        int x = NodeValue.compareAlways(nvInt10, nvFloat8) ;
        int y = NodeUtils.compareRDFTerms(nInt10, nFloat8) ;
        assertTrue("Int 10 less than float 8", Expr.CMP_GREATER == x ) ;
        assertTrue("Int 10 less than float 8 in syntatic compare", Expr.CMP_LESS == y) ;
    }
    
    public void testComp_int_posint_1()
    {
        int x = NodeValue.compareAlways(nvInt9, nvPosInt9) ;
        assertTrue("Int 9 should be not equals to positive integer 9", Expr.CMP_EQUAL != x ) ;
    }
    
    public void testComp_int_posint_2()
    {
        int x = NodeValue.compareAlways(nvInt10, nvPosInt9) ;
        assertTrue("Int 10 not greater than positive integer 9", Expr.CMP_GREATER == x ) ;
    }
    
    public void test_xsd_string1()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "", XSDDatatype.XSDstring)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_EQUAL == x ) ;
    }
    
    public void test_xsd_string2()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("xyz", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "", XSDDatatype.XSDstring)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }

    public void test_xsd_string3()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("xyz", "", XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "", null)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }

    public void test_xsd_string4()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("xyz", "", XSDDatatype.XSDstring)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }

    public void test_xsd_string5()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "", XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("xyz", "", null)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }
    public void test_lang1()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "", null)) ;
        
        int x = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang tags should sort after plain literal", Expr.CMP_GREATER == x ) ;
    }

    public void test_lang2()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "EN", null)) ;
        
        int x = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang tags should sort by case", Expr.CMP_GREATER == x ) ;
    }

    public void test_lang3()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("ABC", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "EN", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical if tags value-same", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang nodes should sort by case (syntactically)", Expr.CMP_LESS == y ) ;
    }

    public void test_lang4()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("ABC", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical if tags the same", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang nodes should sort by lexical form if lang tags the same", Expr.CMP_LESS == x ) ;
    }
    
    public void test_lang5()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("xyz", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if one is plain", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }

    public void test_lang6()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("xyz", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if one is plain", Expr.CMP_GREATER == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }
    
    public void test_lang7()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("abc", "",  XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("xyz", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if other is XSD string", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }
    
    public void test_lang8()
    {
        NodeValue nv1 = NodeValue.makeNode(Node.createLiteral("xyz", "",  XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(Node.createLiteral("abc", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if other is XSD string", Expr.CMP_GREATER == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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