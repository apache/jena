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

    @Test public void tokenForNode11()
    { test( Node.ANY, TokenType.KEYWORD, "ANY", null, null) ; }

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
        // Use SSE to be clear we expect it to be a different node parser to calling Tokenizer.
        Node n = SSE.parseNode(nodeStr) ;
        Token t = Token.tokenForNode(n) ;
        assertEquals(type, t.getType()) ;
        assertEquals(image, t.getImage()) ;
        assertEquals(image2, t.getImage2()) ;
        assertEquals(subToken, t.getSubToken()) ;
    }

}
