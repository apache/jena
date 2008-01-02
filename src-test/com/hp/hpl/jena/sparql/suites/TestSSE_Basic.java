/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.util.NodeFactory;

public class TestSSE_Basic extends TestCase
{
    // Tests not requiring URI resolution or prefix name handling.
    
    static Node int1 = Node.createLiteral("1", null, XSDDatatype.XSDinteger) ;
    static Node int2 = Node.createLiteral("2", null, XSDDatatype.XSDinteger) ;
    static Node int3 = Node.createLiteral("3", null, XSDDatatype.XSDinteger) ;
    static Node strLangEN = Node.createLiteral("xyz", "en", null) ;

    static Node typeLit1 = NodeFactory.createLiteralNode("123", null, "http://example/type") ;
    
    
    static Item int1i = Item.createNode(int1) ;
    static Item int2i = Item.createNode(int2) ;
    static Item int3i = Item.createNode(int3) ;
    
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestSSE_Basic.class) ;
        ts.setName("SSE Basic") ;
        return ts ;
    }

    // ---- Parsing : not check for the correct outcome
    public void testParseTerm_01() { parse("'xyz'") ; }
    public void testParseTerm_02() { parse("'xyz'@en") ; }
    public void testParseTerm_03() { parseBad("'xyz' @en") ; }

    public void testParseSymbol_01() { parse("a") ; }      
    public void testParseSymbol_02() { parseBad("'a") ; }
    // TODO Parser needs fixing
    //public void testParseSymbol_03() { parse("@a") ; }
    public void testParseSymbol_04() { parse("a@") ; }
    
    
    public void testParseList_01() { parse("()") ; }
    public void testParseList_02() { parse("(a)") ; }
    public void testParseList_03() { parse(" (a)") ; }
    public void testParseList_04() { parse("( a)") ; }
    public void testParseList_05() { parse("(a )") ; }
    public void testParseList_06() { parse("(a) ") ; }
    public void testParseList_07() { parse("('a') ") ; }
    public void testParseList_08() { parse("(<a>) ") ; }
    
    public void testParse_10() { parseBad("'foo' @en") ; }

    // ---- Terms 
    public void testLit_01() { testNode("'foo'") ; } 
    public void testLit_02() { testNode("\"foo\"") ; } 
    public void testLit_03() { testNode("''") ; }
    public void testLit_04() { testNode("\"\"") ; }
    public void testLit_05() { testNode("'foo'@en") ; } 
    public void testLit_06() { parseBad("'foo' @en") ; } 
    public void testLit_07() { parseBad("'") ; }
    public void testLit_08() { parseBad("'\"") ; }
    public void testLit_09() { parseBad("'''") ; } 
    public void testLit_10() { parseBad("''@") ; }
    public void testLit_11() { testNode("'''abc\\ndef'''") ; }
    
    public void testLit_12()
    { 
        Node n = Node.createLiteral("A\tB") ;
        testNode("'''A\\tB'''", n) ;
    }
    
    public void testNum_1() { testNode("1") ; }
    public void testNum_2() { testNode("1.1") ; }
    public void testNum_3() { testNode("1.0e6") ; }

    public void testNum_4() { parseBadNode("1 ") ; }
    
    public void testNum_5() { parseBadNode("1 1") ; }
 
    public void testURI_1() { testNode("<http://example/base>") ; }
    public void testURI_2() { parseBadNode("http://example/baseNoDelimiters") ; }
    public void testURI_3() { parseBadNode("<http://example/ space>") ; }
    
    public void testVar_01() { testVar("?x") ; }
    public void testVar_02() { testVar("?") ; }
    public void testVar_03() { testVar("?0") ; }
    // See ARQConstants.anonVarMarker
    public void testVar_04() { testVar("??x") ; }
    public void testVar_05() { testVar("??") ; }
    public void testVar_06() { testVar("??0") ; }
    
    // See ARQConstants.allocVarMarker
    public void testVar_07() { testVar("?"+ARQConstants.allocVarMarker+"0") ; }
    public void testVar_08() { testVar("?"+ARQConstants.allocVarMarker) ; }

    // Default allocations
    public void testVar_09()
    { 
        Node v = SSE.parseNode("?") ;
        assertTrue( v instanceof Var ) ;
        String vn = ((Var)v).getVarName() ;
        assertFalse(vn.equals("")) ;
    }
    
    public void testVar_10()
    { 
        Node v = SSE.parseNode("?"+ARQConstants.anonVarMarker) ;
        assertTrue( v instanceof Var ) ;
        String vn = ((Var)v).getVarName() ;
        assertFalse(vn.equals(ARQConstants.anonVarMarker)) ;
    }
    
    public void testVar_11()
    { 
        Node v = SSE.parseNode("?"+ARQConstants.allocVarMarker) ;
        assertTrue( v instanceof Var ) ;
        String vn = ((Var)v).getVarName() ;
        assertFalse(vn.equals(ARQConstants.allocVarMarker)) ;
    }
    
    public void testWS_1() { parseBadNode("?x ") ; }
    public void testWS_2() { parseBadNode(" ?x") ; }
    
    // ---- Nodes
    
    public void testNode_1()    { testNode("3", int3) ; }
    public void testNode_2()    { testNode("<http://example/node1>", Node.createURI("http://example/node1")) ; } 
    public void testTypedLit_1() { testNode("\"123\"^^<http://example/type>", typeLit1) ; }
    public void testTypedLit_2() { testNode("'123'^^<http://example/type>", typeLit1) ; }
    public void testTypedLit_3() { testNode("'3'^^<"+XSDDatatype.XSDinteger.getURI()+">", int3) ; }

    // --- Symbols
    
    public void testSymbol_1()    { testSymbol("word") ; }
    public void testSymbol_2()    { testSymbol("+") ; }
    // XXX Parser broken
//    public void testSymbol_3()    { testSymbol("^^") ; }
//    public void testSymbol_4()    { testSymbol("^^<foo>") ; }
//    public void testSymbol_5()    { testSymbol("@") ; }
//    public void testSymbol_6()    { testSymbol("@en") ; }
    
    // --- nil
    
    public void testNil_1()    { testItem("nil", Item.nil) ; }
    public void testNil_2()    { testNotItem("null", Item.nil) ; }
    public void testNil_3()    { testNotItem("()", Item.nil) ; }
    public void testNil_4()
    { 
        Item x = Item.createList() ;
        x.getList().add(Item.nil) ;
        testItem("(nil)", x) ;
    }

    // ---- Lists
    
    public void testList_1()
    { 
        Item item = parse("()") ;
        assertTrue(item.isList()) ;
        assertEquals(item.getList().size(), 0 ) ;
    }

    public void testList_2()    { testList("(1)", int1i) ; }
    public void testList_3()    { testList("(1 2)", int1i, int2i) ; }
    public void testList_4()    { testList("(1 a)", int1i, Item.createSymbol("a")) ; }
    
    public void testList_5()
    { 
        Item list = Item.createList() ;
        list.getList().add(int1i) ;
        testList("((1) a)", list, Item.createSymbol("a")) ;
    }
    
    public void testList_6()
    { testList("(+ 1)", Item.createSymbol("+"), int1i) ; }

    public void testList_7()
    { testList("[+ 1]", Item.createSymbol("+"), int1i) ; }
    
    public void testMisc_01()    { testEquals("()") ; }
    public void testMisc_02()    { testEquals("(a)") ; }
    public void testMisc_10()    { testNotEquals("(a)", "a") ; }
    public void testMisc_11()    { testNotEquals("(a)", "()") ; }
    public void testMisc_12()    { testNotEquals("(a)", "(<a>)") ; }
    
    
    
    
    
    
     
    // ---- Workers ----
    
    private void testEquals(String x)
    {
        Item item1 = parse(x) ;
        Item item2 = parse(x) ;
        assertTrue(item1.equals(item2)) ;
        assertTrue(item2.equals(item1)) ;
    }
    
    private void testNotEquals(String x1, String x2)
    {
        Item item1 = parse(x1) ;
        Item item2 = parse(x2) ;
        assertFalse(item1.equals(item2)) ;
        assertFalse(item2.equals(item1)) ;
    }
    
    private Item parse(String str)
    {
        Item item = SSE.parse(str) ;
        return item ;
    }
    
    private void testSymbol(String str)
    {
        Item item = parse(str) ;
        assertTrue(item.isSymbol()) ;
        assertEquals(item.getSymbol(), str) ;
    }
    
    private void testList(String str, Item item1)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        
        Item i = item.getList().get(0) ;
        
        assertEquals(1, item.getList().size()) ;
        assertEquals(item.getList().get(0), item1) ;
    }

    private void testList(String str, Item item1, Item item2)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        assertEquals(2, item.getList().size()) ;
        assertEquals(item.getList().get(0), item1) ;
        assertEquals(item.getList().get(1), item2) ;
    }

    private void testList(String str, Item item1, Item item2, Item item3)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        assertEquals(3, item.getList().size()) ;
        assertEquals(item.getList().get(0), item1) ;
        assertEquals(item.getList().get(1), item2) ;
        assertEquals(item.getList().get(2), item3) ;
    }
    
    private void testItem(String str, Item result)
    {
        Item item = parse(str) ;
        assertEquals(result, item) ;
    }
    
    private void testNotItem(String str, Item result)
    {
        Item item = parse(str) ;
        assertFalse(result.equals(item)) ;
    }

    private void testNode(String str)
    {
        Node node = SSE.parseNode(str) ;
    }
    
    private void testVar(String str)
    {
        Node node = SSE.parseNode(str) ;
        assertTrue( node instanceof Var ) ;
    }
    
    private void testNode(String str, Node result)
    {
        Node node = SSE.parseNode(str, null) ;
        assertEquals(result, node) ;
    }

    
    private void parseBad(String str)
    {
        try {
            Item item = SSE.parse(str) ;
            //System.out.println(str+" => "+item) ;
            fail("Did not get a parse failure") ;
        } 
        catch (SSEParseException ex) {}
        catch (ARQException ex) {}
    }
    
    private void parseBadNode(String str)
    {
        try {
            Node node = SSE.parseNode(str) ;
            //System.out.println(str+" => "+item) ;
            fail("Did not get a parse failure") ;
        } 
        catch (SSEParseException ex) {}
        catch (ARQException ex) {}
    }

}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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