/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.loader;

import java.io.StringReader;

import lib.Tuple;

import org.junit.Test;
import test.BaseTest;


import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;
import com.hp.hpl.jena.tdb.base.loader.NodeTupleReader;

public class TestNTriples extends BaseTest
{
    @Test public void iri_1()
    {
        Node x = readNode("<x>") ;
        assertEquals(Node.createURI("x"), x) ;
    }
    
    @Test public void iri_2()
    {
        Node x = readNode("<http://example/x>") ;
        assertEquals(Node.createURI("http://example/x"), x) ;
    }

    
    @Test public void blank_1()
    {
        Node x = readNode("_:a") ;
        assertTrue(x.isBlank()) ;
    }

    @Test public void blank_2()
    {
        Node x1 = readNode("_:a") ;
        Node x2 = readNode("_:b") ;
        assertNotEquals(x1, x2) ;
    }

    @Test public void blank_3()
    {
        Node x1 = readNode("_:a") ;     // Label preservation
        Node x2 = readNode("_:a") ;
        assertEquals(x1, x2) ;
    }

    @Test public void literal_1()
    {
        Node x = readNode("\"\"") ;
        assertEquals(Node.createLiteral(""), x) ;
    }        
    
    @Test public void literal_2()
    {
        Node x = readNode("\"abc\"") ;
        assertEquals(Node.createLiteral("abc"), x) ;
    }        
    
    @Test public void literal_3()
    {
        Node x = readNode("\"abc\"@en") ;
        assertEquals(Node.createLiteral("abc", "en", null), x) ;
    }        
    

    @Test public void literal_4()
    {
        Node x = readNode("\"abc\"@en-uk") ;
        assertEquals(Node.createLiteral("abc", "en-uk", null), x) ;
    }        

    @Test public void literal_5()
    {
        Node x = readNode("\"\"@en") ;
        assertEquals(Node.createLiteral("", "en", null), x) ;
    }        
    
    
    @Test public void literal_6()
    {
        Node x = readNode("\"abc\"^^<http://example/foo>\"") ;
        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName("http://example/foo");
        assertEquals(Node.createLiteral("abc", null, dt), x) ;
    }        
    
    @Test public void triple_1()
    {
        Triple x = readTriple("<s> <p> <o> .") ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createURI("o") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void triple_2()
    {
        Triple x = readTriple("<s> <p> \"abc\" .") ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createLiteral("abc") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void triple_3()
    {
        Triple x = readTriple("<s> <p> <o> . #comment") ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createURI("o") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void triple_4()
    {
        Triple x = readTriple("  <s><p><o>.") ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createURI("o") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void triple_5()
    {
        Triple x = readTriple("#comment") ;
        assertNull(x) ;
    }
    
    @Test public void triple_6()
    {
        Triple x = readTriple("  #comment") ;
        assertNull(x) ;
    }

    
    @Test public void esc_1()
    {
        Triple x = readTriple("<s> <p> \"X\\u0020Y\" .") ;
        assertNotNull(x) ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createLiteral("X Y") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void esc_2()
    {
        Triple x = readTriple("<s> <p> \"X\\U00000020Y\" .") ;
        assertNotNull(x) ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createLiteral("X Y") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void esc_3()
    {
        Triple x = readTriple("<s> <p> \"a\\tb\" .") ;
        assertNotNull(x) ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createLiteral("a\tb") ;
        assertEquals(new Triple(s,p,o), x) ;
    }

    @Test public void esc_4()
    {
        // Out of range for UTF-16
        int ch = 0x00010011 ;
        String str = new String(Character.toChars(ch)) ;
        
        Triple x = readTriple("<s> <p> \"X\\U00010011Y\" .") ;
        assertNotNull(x) ;
        Node s = Node.createURI("s") ;
        Node p = Node.createURI("p") ;
        Node o = Node.createLiteral("X"+str+"Y") ;
        assertEquals(new Triple(s,p,o), x) ;
    }
    
    @Test public void comment_1()
    {
        Graph g = parse("#Comment <x> <p> <z> .") ;
        assertEquals(0, g.size()) ;
    }
    
    @Test public void comment_2()
    {
        Graph g = parse("#Comment\n<x> <p> <z> .") ;
        assertEquals(1, g.size()) ;
    }
    
    @Test public void comment_3()
    {
        Graph g = parse("#Comment\r<x> <p> <z> .") ;
        assertEquals(1, g.size()) ;
    }

    @Test public void comment_4()
    {
        Graph g = parse("#Comment\r\n<x> <p> <z> .") ;
        assertEquals(1, g.size()) ;
    }
    
    @Test public void comment_5()
    {
        Graph g = parse("<x> <p> <z> .\n#Comment \n\n<x1> <p1> <z1> .") ;
        assertEquals(2, g.size()) ;
    }

    @Test public void comment_6()
    {
        Graph g = parse("<x> <p> <z> .\n<x1> <p1> <z1> .\r\n#Comment") ;
        assertEquals(2, g.size()) ;
    }

    @Test public void comment_7()
    {
        Graph g = parse("<x> <p> <z> .\r#Comment") ;
        assertEquals(1, g.size()) ;
    }
    // XXX more error cases
    
    private Node readNode(String form)
    {
        NodeTupleReader b = make(form) ;
        Node x = b.readNode() ;
        return x ;
    }
    
    private Triple readTriple(String form)
    {
        NodeTupleReader b = make(form) ;
        Tuple<Node> t = b.readTuple() ;
        if ( t == null )
            return null ;
        return new Triple(t.get(0), t.get(1), t.get(2) ) ;
    }
    
    private Graph parse(String contents)
    {
        Graph graph = GraphUtils.makePlainGraph() ;
        StringReader r = new StringReader(contents) ;
        NodeTupleReader.read(graph, r, null) ;
        return graph ;
    }
    
    private NodeTupleReader make(String contents)
    {
        return new NodeTupleReader(contents) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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