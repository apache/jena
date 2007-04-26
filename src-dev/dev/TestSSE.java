/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;

public class TestSSE extends TestCase
{
    // TODO TestItem (esp hashCode and .equals)
    
    static Node int1 = Node.createLiteral("1", null, XSDDatatype.XSDinteger) ;
    static Node int2 = Node.createLiteral("2", null, XSDDatatype.XSDinteger) ;
    static Node int3 = Node.createLiteral("3", null, XSDDatatype.XSDinteger) ;
    static Node strLangEN = Node.createLiteral("xyz", "en", null) ;
    
    static Item int1i = Item.createNode(int1) ;
    static Item int3i = Item.createNode(int2) ;
    static Item int2i = Item.createNode(int3) ;

    
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestSSE.class) ;
        ts.setName("TestSSE") ;
        return ts ;
    }

    // ---- Parsing : not check for the correct outcome
    public void testParseTerm_01() { parse("'xyz'") ; }
    public void testParseTerm_02() { parse("'xyz'@en") ; }
    public void testParseTerm_03() { parseBad("'xyz' @en") ; }

    public void testParseWord_01() { parse("a") ; }      
    public void testParseWord_02() { parseBad("'a") ; }
    
    public void testParseList_01() { parse("()") ; }
    public void testParseList_02() { parse("(a)") ; }
    public void testParseList_03() { parse(" (a)") ; }
    public void testParseList_04() { parse("( a)") ; }
    public void testParseList_05() { parse("(a )") ; }
    public void testParseList_06() { parse("(a) ") ; }
    
    public void testParse_10() { parseBad("'foo' @en") ; }

    // ---- Terms 
    // TODO Parse single item : testing for WS around it.
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
    
    public void testNum_1() { testNode("1") ; }
    public void testNum_2() { testNode("1.1") ; }
    public void testNum_3() { testNode("1.0e6") ; }
    
    public void testNum_5() { parseBadNode("1 1") ; }
 
    public void testURI_1() { testNode("<http://example/base>") ; }
    public void testURI_2() { parseBadNode("http://example/baseNoDelimiters") ; }
    public void testURI_3() { parseBadNode("<http://example/ space>") ; }
    
    public void testVar_1() { testNode("?x") ; }
    
    public void testWS_1() { parseBadNode("?x ") ; }
    public void testWS_2() { parseBadNode(" ?x") ; }
    
    // ---- Nodes
    
    public void testNode_1()    { testNode("3", int3) ; }
    
    // --- Words
    
    public void testWord_1()    { testWord("word") ; }
    public void testWord_2()    { testWord("+") ; }
    
    // ---- Lists
    
    public void testList_1()
    { 
        Item item = parse("()") ;
        assertTrue(item.isList()) ;
        assertEquals(item.getList().size(), 0 ) ;
    }

    public void testList_2()
    { 
        testList("(1)", int1i) ;
    }

    
    public void XX_testList_1()
    {
        // ( 1  2 )
        // ( 1  'xyz'@en )
        // ( 1  2 )
        // ( 1  2 )
    }
    
    // ---- Workers ----
    
    private Item parse(String str)
    {
        Item item = SSE.parse(str) ;
        return item ;
    }
    
    private void testWord(String str)
    {
        Item item = parse(str) ;
        assertTrue(item.isWord()) ;
        assertEquals(item.getWord(), str) ;
    }
    
    private void testList(String str, Item item1)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        
        Item i = item.getList().get(0) ;
        
        i.equals(item1) ;
        
        assertEquals(item.getList().get(0), item1) ;
    }

    private void testList(String str, Item item1, Item item2)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        assertEquals(item.getList().get(0), item1) ;
        assertEquals(item.getList().get(1), item2) ;
    }

    private void testList(String str, Item item1, Item item2, Item item3)
    {
        Item item = parse(str) ;
        assertTrue(item.isList()) ;
        assertEquals(item.getList().get(0), item1) ;
        assertEquals(item.getList().get(1), item2) ;
        assertEquals(item.getList().get(2), item3) ;
    }

    private void testNode(String str)
    {
        Node node = SSE.parseNode(str) ;
    }
    
    private void testNode(String str, Node result)
    {
        Node node = SSE.parseNode(str) ;
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