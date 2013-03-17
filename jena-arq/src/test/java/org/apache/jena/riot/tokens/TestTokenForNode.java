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

package org.apache.jena.riot.tokens;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TestTokenForNode extends BaseTest 
{
    static String base = "http://localhost/" ;
    static PrefixMap prefixMap = PrefixMapFactory.create() ;
    
    @BeforeClass static public void beforeClass()
    {
        prefixMap.add("ex", "http://example/") ;
    }
    
    @Test public void tokenForNode01()
    { test( "'x'", TokenType.STRING, "x", null, null, null) ; }

    @Test public void tokenForNode02()
    { test( "<x>", TokenType.IRI, "x", null, null, null) ; }

    @Test public void tokenForNode03()
    { test( "'en'@lang", TokenType.LITERAL_LANG, "en", "lang", new Token(TokenType.STRING, "en"), null) ; }

    @Test public void tokenForNode04()
    { 
        Token sub = new Token(-1,-1) ;
        sub.setType(TokenType.IRI) ;
        sub.setImage("dtype") ;
        test( "'lex'^^<dtype>", TokenType.LITERAL_DT, "lex", null, new Token(TokenType.STRING, "lex"), sub) ;
    }

    @Test public void tokenForNode05()
    { test( "<http://localhost/foo>", TokenType.IRI, "foo", null, null, null) ; }

    @Test public void tokenForNode06()
    { test( "<http://example/bar>", TokenType.PREFIXED_NAME, "ex", "bar", null, null) ; }
    
    @Test public void tokenForNode07()
    { test( com.hp.hpl.jena.graph.NodeFactory.createAnon(new AnonId("abc")), TokenType.BNODE, "abc", null, null, null ) ; }

    @Test public void tokenForNode08()
    { test( Node.ANY, TokenType.KEYWORD, "ANY", null, null, null) ; }
    
    // Short forms.
    
    @Test public void tokenForNode20()
    { test( "123", TokenType.INTEGER, "123", null, null, null) ; }

    @Test public void tokenForNode21()
    { test( "123.0", TokenType.DECIMAL, "123.0", null, null, null) ; }

    @Test public void tokenForNode22()
    { test( "12e0", TokenType.DOUBLE, "12e0", null, null, null) ; }

    private static void test(String nodeStr,
                             TokenType type, String image, String image2, Token subToken1, Token subToken2)
    {
        Node n = NodeFactoryExtra.parseNode(nodeStr) ;
        test(n, type, image, image2, subToken1, subToken2) ;
    }
    
    
    private static void test(Node node,
                             TokenType type, String image, String image2, Token subToken1, Token subToken2)
    {
        Token t = Token.tokenForNode(node, base, prefixMap) ;
        assertEquals(type, t.getType()) ;
        assertEquals(image, t.getImage()) ;
        assertEquals(image2, t.getImage2()) ;
        assertEquals(subToken1, t.getSubToken1()) ;
        assertEquals(subToken2, t.getSubToken2()) ;
    }
}
