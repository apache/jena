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
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;

public class TestSSE extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestSSE.class) ;
        ts.setName("TestMisc") ;
        return ts ;
    }
    
    public void testList_1()
    { 
        Item item = parse("()") ;
        assertTrue(item.isList()) ;
        assertEquals(item.getList().size(), 0 ) ;
    }

    public void testNode_1()
    {
        Item item = parse("123") ;
        assertTrue(item.isNode()) ;
        assertEquals(item.getNode(), Node.createLiteral("123", null, XSDDatatype.XSDinteger)) ;
    }
    
    public void testWord_1()
    {
        Item item = parse("word") ;
        assertTrue(item.isWord()) ;
        assertEquals(item.getWord(), "word") ;
    }
    
    public void testWord_2()
    {
        Item item = parse("+") ;
        assertTrue(item.isWord()) ;
        assertEquals(item.getWord(), "+") ;
    }

    
    private Item parse(String str)
    {
        Item item = SSE.parseString(str) ;
        return item ;
    }
    
    private void testBad(String str)
    {
        try {
            Item item = SSE.parseString(str) ;
            //System.out.println(str+" => "+item) ;
            fail("Did not get a parse failure") ;
        } catch (SSEParseException ex)
        {}
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