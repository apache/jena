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
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

public class TestOrdering extends BaseTest
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
    
//    public static TestSuite suite()
//    {
//        TestSuite ts = new TestSuite(TestOrdering.class) ;
//        ts.setName("TestOrdering") ;
//        return ts ;
//    }
    
    @Test public void testComp_2_3()
    {
        int x = NodeValue.compareAlways(nvInt2, nvInt3) ;
        assertTrue("2 should be value-less than 3", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nInt2, nInt3) ;
        assertTrue("2 should be strict-less than 3", Expr.CMP_LESS == y ) ;
    }
    
    @Test public void testComp_3_str3()
    {
        int x = NodeValue.compareAlways(nvInt3, nvStr3) ;
        int y = NodeUtils.compareRDFTerms(nInt3, nStr3) ;
        
        assertTrue("3 should be compareAlways greater than \"3\"",  Expr.CMP_GREATER == x) ;
        assertTrue("3 should be syntactic-greater than to \"3\"", Expr.CMP_GREATER == y ) ;
    }

    @Test public void testComp_03_str3()
    {
        int x = NodeValue.compareAlways(nvInt03, nvStr3) ;
        int y = NodeUtils.compareRDFTerms(nInt03, nStr3) ;
        
        assertTrue("03 (typed) should be compareAlways 'less than' than \"3\"",  Expr.CMP_LESS == x ) ;
        assertTrue("03 should be syntactic-less than to \"3\"", Expr.CMP_LESS == y ) ;
    }
    
    // Compare things of different types.
    @Test public void testComp_int_double_1()
    {
        int x = NodeValue.compareAlways(nvInt10, nvDouble9) ;
        int y = NodeUtils.compareRDFTerms(nInt10, nDouble9) ;
        assertTrue("Int 10 less than double 9", Expr.CMP_GREATER == x ) ;
        assertTrue("Int 10 less than double 9 in syntactic compare", Expr.CMP_LESS == y ) ;
    }

    @Test public void testComp_byte_double_1()
    {
        int x = NodeValue.compareAlways(nvByte10, nvDouble9) ;
        int y = NodeUtils.compareRDFTerms(nByte10, nDouble9) ;
        assertTrue("Byte 10 less than double 9", Expr.CMP_GREATER == x ) ;
        assertTrue("Byte 10 greater than double 9 in non-value compare (dataype URIs compare)", Expr.CMP_LESS == y ) ;
    }

    @Test public void testComp_int_float_1()
    {
        int x = NodeValue.compareAlways(nvInt10, nvFloat8) ;
        int y = NodeUtils.compareRDFTerms(nInt10, nFloat8) ;
        assertTrue("Int 10 less than float 8", Expr.CMP_GREATER == x ) ;
        assertTrue("Int 10 less than float 8 in syntatic compare", Expr.CMP_LESS == y) ;
    }
    
    @Test public void testComp_int_posint_1()
    {
        int x = NodeValue.compareAlways(nvInt9, nvPosInt9) ;
        assertTrue("Int 9 should be not equals to positive integer 9", Expr.CMP_EQUAL != x ) ;
    }
    
    @Test public void testComp_int_posint_2()
    {
        int x = NodeValue.compareAlways(nvInt10, nvPosInt9) ;
        assertTrue("Int 10 not greater than positive integer 9", Expr.CMP_GREATER == x ) ;
    }
    
    @Test public void test_xsd_string1()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", XSDDatatype.XSDstring)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_EQUAL == x ) ;
    }
    
    @Test public void test_xsd_string2()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", XSDDatatype.XSDstring)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }

    @Test public void test_xsd_string3()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "", XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", null)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }

    @Test public void test_xsd_string4()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "", XSDDatatype.XSDstring)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }

    @Test public void test_xsd_string5()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "", null)) ;
        int x = NodeValue.compare(nv1, nv2) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }
    @Test public void test_lang1()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", null)) ;
        
        int x = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang tags should sort after plain literal", Expr.CMP_GREATER == x ) ;
    }

    @Test public void test_lang2()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "EN", null)) ;
        
        int x = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang tags should sort by case", Expr.CMP_GREATER == x ) ;
    }

    @Test public void test_lang3()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("ABC", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "EN", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical if tags value-same", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang nodes should sort by case (syntactically)", Expr.CMP_LESS == y ) ;
    }

    @Test public void test_lang4()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("ABC", "en", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical if tags the same", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue("Lang nodes should sort by lexical form if lang tags the same", Expr.CMP_LESS == x ) ;
    }
    
    @Test public void test_lang5()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if one is plain", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }

    @Test public void test_lang6()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "", null)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if one is plain", Expr.CMP_GREATER == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }
    
    @Test public void test_lang7()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "",  XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if other is XSD string", Expr.CMP_LESS == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_LESS == x ) ;
    }
    
    @Test public void test_lang8()
    {
        NodeValue nv1 = NodeValue.makeNode(NodeFactory.createLiteral("xyz", "",  XSDDatatype.XSDstring)) ;
        NodeValue nv2 = NodeValue.makeNode(NodeFactory.createLiteral("abc", "en", null)) ;
        
        int x = NodeValue.compareAlways(nv1, nv2) ;
        assertTrue("Lang nodes should sort by lexical form if other is XSD string", Expr.CMP_GREATER == x ) ;
        int y = NodeUtils.compareRDFTerms(nv1.asNode() , nv2.asNode()) ;
        assertTrue(Expr.CMP_GREATER == x ) ;
    }
    
    @Test public void test_variable1() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createVariable("x");
        
        int res = NodeUtils.compareRDFTerms(x, y);
        assertTrue("Variable nodes should sort by variable names", Expr.CMP_EQUAL == res);
    }
    
    @Test public void test_variable2() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createVariable("y");
        
        int res = NodeUtils.compareRDFTerms(x, y);
        assertTrue("Variable nodes should sort by variable names", Expr.CMP_LESS == res);
        res = NodeUtils.compareRDFTerms(y, x);
        assertTrue("Variable nodes should sort by variable names", Expr.CMP_GREATER == res);
    }
    
    @Test public void test_variable3() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createAnon();
        
        int res = NodeUtils.compareRDFTerms(x, y);
        assertTrue("Variable nodes should be less than blank nodes", Expr.CMP_LESS == res);
        res = NodeUtils.compareRDFTerms(y, x);
        assertTrue("Variable nodes should be less than blank nodes", Expr.CMP_GREATER == res);
    }
    
    @Test public void test_variable4() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createURI("http://uri");
        
        int res = NodeUtils.compareRDFTerms(x, y);
        assertTrue("Variable nodes should be less than URI nodes", Expr.CMP_LESS == res);
        res = NodeUtils.compareRDFTerms(y, x);
        assertTrue("Variable nodes should be less than URI nodes", Expr.CMP_GREATER == res);
    }
    
    @Test public void test_variable5() {
        Node x = NodeFactory.createVariable("x");
        Node y = NodeFactory.createLiteral("test");
        
        int res = NodeUtils.compareRDFTerms(x, y);
        assertTrue("Variable nodes should be less than literal nodes", Expr.CMP_LESS == res);
        res = NodeUtils.compareRDFTerms(y, x);
        assertTrue("Variable nodes should be less than literal nodes", Expr.CMP_GREATER == res);
    }
}
