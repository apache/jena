/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestSSE_Resolve extends TestCase
{
    static PrefixMapping pmap = new PrefixMappingImpl() ;
    static { 
        pmap.setNsPrefix("xsd", XSD.getURI()) ;
        pmap.setNsPrefix("ex", "http://example/") ;
    }
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestSSE_Resolve.class) ;
        ts.setName("SSE Resolve") ;
        return ts ;
    }

    // URI resolving.
    
    public void testTypedLit_r1()
    { 
        Node node = Node.createLiteral("3", null, XSDDatatype.XSDinteger) ; 
        testItem("'3'^^xsd:integer", Item.createNode(node)) ;
    }

    // ---- Assume ParseHandlerresolver from here on
    
    public void testBase_01()
    { 
        Item r = Item.createNode(Node.createURI("http://example/x")) ; 
        testItem("(base <http://example/> <x>)", r) ;
    }
    
    public void testBase_02()
    { 
        Item r = Item.createNode(Node.createURI("http://example/x")) ; 
        testItem("(base <http://HOST/> (base <http://example/xyz> <x>))", r) ;
    }

    public void testBase_03()
    { 
        Item r = SSE.parse("(1 <http://example/xyz>)", null) ;
        testItem("(base <http://example/> (1 <xyz>))", r) ;
    }
    
    public void testBase_04()
    { 
        Item r = SSE.parse("(1 <http://example/xyz>)", null) ;
        testItem("(1 (base <http://example/> <xyz>))", r) ;
    }
    
    public void testBase_05()
    { 
        Item r = SSE.parse("(<http://example/xyz> <http://EXAMPLE/other#foo>)", null) ;
        testItem("((base <http://example/> <xyz>) (base <http://EXAMPLE/other> <#foo>))", r) ;
    }
    
    public void testBase_06()
    { 
        Item r = SSE.parse("(<http://example/xyz> <http://EXAMPLE/other#foo>)", null) ;
        testItem("(base <http://example/> (<xyz> (base <http://EXAMPLE/other> <#foo>)))", r) ;
    }

    public void testBase_07()
    { 
        Item r = SSE.parse("(<http://example/xyz> <http://EXAMPLE/other#foo>)", null) ;
        testItem("(base <http://EXAMPLE/other#> ((base <http://example/> <xyz>) <#foo>))", r) ;
    }
    
    // ----
    
    public void testPrefix_01()
    { 
        Item r = Item.createNode(Node.createURI("http://example/abc")) ;
        testItem("(prefix ((ex: <http://example/>)) ex:abc)", r);
    }

    public void testPrefix_02()
    { 
        Item r = Item.createNode(Node.createURI("http://EXAMPLE/abc")) ;
        testItem("(prefix ((ex: <http://example/>)) (prefix ((ex: <http://EXAMPLE/>)) ex:abc))", r);
    }
    
    public void testPrefix_03()
    { 
        Item r = SSE.parse("(<http://example/abc>)" , null) ;
        testItem("(prefix ((ex: <http://example/>)) (ex:abc))", r);
    }
    
    public void testPrefix_04()
    { 
        Item r = SSE.parse("(<http://EXAMPLE/abc>)" , null) ;
        testItem("(prefix ((ex: <http://example/>)) ( (prefix ((ex: <http://EXAMPLE/>)) ex:abc) ))", r);
    }
    
    public void testPrefix_05()
    { 
        Item r = SSE.parse("(<http://example/abc>)" , null) ;
        testItem("(prefix ((ex: <http://example/>)) ( (prefix ((x: <http://EXAMPLE/>)) ex:abc) ))", r);
    }
    
    // ----

    public void testBasePrefix_01()
    { 
        Item r = SSE.parse("<http://example/abc>" , null) ;
        testItem("(base <http://example/> (prefix ((x: <>)) x:abc) )", r);
    }
    
    private void testItem(String str, Item result)
    {
        Item item = SSE.parse(str) ;
        assertEquals(result, item) ;
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