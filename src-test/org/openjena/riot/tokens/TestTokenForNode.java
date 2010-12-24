/*
 * (c) Copyright 2009 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.tokens;

import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.system.PrefixMap ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

public class TestTokenForNode extends BaseTest 
{
    static String base = "http://localhost/" ;
    static PrefixMap prefixMap = new PrefixMap() ;
    
    @BeforeClass static public void beforeClass()
    {
        prefixMap.add("ex", "http://example/") ;
    }
    
    @Test public void tokenForNode01()
    { test( "'x'", TokenType.STRING, "x", null, null) ; }

    @Test public void tokenForNode02()
    { test( "<x>", TokenType.IRI, "x", null, null) ; }

    @Test public void tokenForNode03()
    { test( "'en'@lang", TokenType.LITERAL_LANG, "en", "lang", null) ; }

    @Test public void tokenForNode04()
    { 
        Token sub = new Token(-1,-1) ;
        sub.setType(TokenType.IRI) ;
        sub.setImage("dtype") ;
        test( "'lex'^^<dtype>", TokenType.LITERAL_DT, "lex", null, sub) ;
    }

    @Test public void tokenForNode05()
    { test( "<http://localhost/foo>", TokenType.IRI, "foo", null, null) ; }

    @Test public void tokenForNode06()
    { test( "<http://example/bar>", TokenType.PREFIXED_NAME, "ex", "bar", null) ; }
    
    @Test public void tokenForNode07()
    { test( "123", TokenType.INTEGER, "123", null, null) ; }

    @Test public void tokenForNode08()
    { test( "123.0", TokenType.DECIMAL, "123.0", null, null) ; }

    @Test public void tokenForNode09()
    { test( "12e0", TokenType.DOUBLE, "12e0", null, null) ; }

    @Test public void tokenForNode10()
    { test( Node.createAnon(new AnonId("abc")), TokenType.BNODE, "abc", null, null) ; }

    private static void test(String nodeStr,
                             TokenType type, String image, String image2, Token subToken)
    {
        Node n = NodeFactory.parseNode(nodeStr) ;
        test(n, type, image, image2, subToken) ;
    }
    
    
    private static void test(Node node,
                             TokenType type, String image, String image2, Token subToken)
    {
        Token t = Token.tokenForNode(node, base, prefixMap) ;
        assertEquals(type, t.getType()) ;
        assertEquals(image, t.getImage()) ;
        assertEquals(image2, t.getImage2()) ;
        assertEquals(subToken, t.getSubToken()) ;
    }
    
    private static void test(String nodeStr, PrefixMap pmap,
                             TokenType type, String image, String image2, Token subToken)
    {
        Node n = SSE.parseNode(nodeStr) ;
        Token t = Token.tokenForNode(n) ;
        assertEquals(type, t.getType()) ;
        assertEquals(image, t.getImage()) ;
        assertEquals(image2, t.getImage2()) ;
        assertEquals(subToken, t.getSubToken()) ;
    }

}

/*
 * (c) Copyright 2009 Talis Systems Ltd
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