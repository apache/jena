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

package org.apache.jena.riot.tokens ;

import java.io.ByteArrayInputStream ;

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.RiotParseException ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.ARQConstants ;

public class TestTokenizer extends BaseTest {
    // WORKERS
    private static Tokenizer tokenizer(String string) {
        return tokenizer(string, false) ;
    }

    private static Tokenizer tokenizer(String string, boolean lineMode) {
        PeekReader r = PeekReader.readString(string) ;
        Tokenizer tokenizer = new TokenizerText(r, lineMode) ;
        return tokenizer ;
    }

    private static void tokenFirst(String string) {
        Tokenizer tokenizer = tokenizer(string) ;
        assertTrue(tokenizer.hasNext()) ;
        assertNotNull(tokenizer.next()) ;
        // Maybe more.
        // assertFalse(tokenizer.hasNext()) ;
    }

    private static Token tokenFor(String string) {
        Tokenizer tokenizer = tokenizer(string) ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertFalse(tokenizer.hasNext()) ;
        return token ;
    }

    private static Token tokenizeAndTestExact(String input, TokenType tokenType, String tokenImage) {
        return tokenizeAndTestExact(input, tokenType, tokenImage, null) ;
    }

    private static Token tokenizeAndTestExact(String input, TokenType tokenType, String tokenImage1, String tokenImage2) {
        Tokenizer tokenizer = tokenizer(input) ;
        Token token = testNextToken(tokenizer, tokenType, tokenImage1, tokenImage2) ;
        assertFalse("Excess tokens", tokenizer.hasNext()) ;
        return token ;
    }

    private static Token tokenizeAndTestExact(String input, TokenType tokenType, String tokenImage1,
                                              String tokenImage2, Token subToken1, Token subToken2) {
        Token token = tokenFor(input) ;
        assertEquals(tokenType, token.getType()) ;
        assertEquals(tokenImage1, token.getImage()) ;
        assertEquals(tokenImage2, token.getImage2()) ;
        assertEquals(subToken1, token.getSubToken1()) ;
        assertEquals(subToken2, token.getSubToken2()) ;
        return token ;
    }

    private static Tokenizer tokenizeAndTestFirst(String input, TokenType tokenType, String tokenImage) {
        return tokenizeAndTestFirst(input, tokenType, tokenImage, null) ;
    }

    private static Tokenizer tokenizeAndTestFirst(String input, TokenType tokenType, String tokenImage1,
                                                  String tokenImage2) {
        Tokenizer tokenizer = tokenizer(input) ;
        testNextToken(tokenizer, tokenType, tokenImage1, tokenImage2) ;
        return tokenizer ;
    }

    private static Token testNextToken(Tokenizer tokenizer, TokenType tokenType) {
        return testNextToken(tokenizer, tokenType, null, null) ;
    }

    private static Token testNextToken(Tokenizer tokenizer, TokenType tokenType, String tokenImage1) {
        return testNextToken(tokenizer, tokenType, tokenImage1, null) ;
    }

    private static Token testNextToken(Tokenizer tokenizer, TokenType tokenType, String tokenImage1, String tokenImage2) {
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(tokenType, token.getType()) ;
        assertEquals(tokenImage1, token.getImage()) ;
        assertEquals(tokenImage2, token.getImage2()) ;
        return token ;
    }

    private static Token tokenizeAndTest(String input, TokenType tokenType, String tokenImage1, String tokenImage2,
                                         Token subToken1, Token subToken2) {
        Token token = tokenFor(input) ;
        assertNotNull(token) ;
        assertEquals(tokenType, token.getType()) ;
        assertEquals(tokenImage1, token.getImage()) ;
        assertEquals(tokenImage2, token.getImage2()) ;
        assertEquals(subToken1, token.getSubToken1()) ;
        assertEquals(subToken2, token.getSubToken2()) ;
        return token ;
    }

    @Test
    public void tokenUnit_iri1() {
        tokenizeAndTestExact("<x>", TokenType.IRI, "x") ;
    }

    @Test
    public void tokenUnit_iri2() {
        tokenizeAndTestExact("   <>   ", TokenType.IRI, "") ;
    }

    @Test
    // (expected=RiotParseException.class) We test the message.
    public void tokenUnit_iri3() {
        try {
            // That's one \
            tokenFirst("<abc\\>def>") ;
        } catch (RiotParseException ex) {
            String x = ex.getMessage() ;
            assertTrue(x.contains("illegal escape sequence value: >")) ;
        }
    }

    @Test
    public void tokenUnit_iri4() {
        // \\\\ is a double \\ in the data.
        tokenizeAndTestFirst("   <abc\\\\def>   123", TokenType.IRI, "abc\\def") ;
    }

    @Test
    public void tokenUnit_iri5() {
        // \\\\ is a double \\ in the data. 0x41 is 'A'
        tokenizeAndTestFirst("<abc\\u0041def>   123", TokenType.IRI, "abcAdef") ;
    }

    @Test
    public void tokenUnit_str1() {
        tokenizeAndTestExact("   'abc'   ", TokenType.STRING1, "abc") ;
    }

    @Test
    public void tokenUnit_str2() {
        tokenizeAndTestExact("   ''   ", TokenType.STRING1, "") ;
    }

    @Test
    public void tokenUnit_str3() {
        tokenizeAndTestExact("'\\u0020'", TokenType.STRING1, " ") ;
    }

    @Test
    public void tokenUnit_str4() {
        tokenizeAndTestExact("'a\\'\\\"\\n\\t\\r\\f'", TokenType.STRING1, "a'\"\n\t\r\f") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_str5() {
        // This is a raw newline. \n is a Java string escape.
        tokenizeAndTestExact("'\n'", TokenType.STRING1, "\n") ;
    }

    @Test
    public void tokenUnit_str6() {
        tokenizeAndTestExact("   \"abc\"   ", TokenType.STRING2, "abc") ;
    }

    @Test
    public void tokenUnit_str7() {
        tokenizeAndTestExact("\"\"", TokenType.STRING2, "") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_str8() {
        Tokenizer tokenizer = tokenizer("\"") ;
        assertTrue(tokenizer.hasNext()) ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_str9() {
        tokenFirst("'abc") ;
    }

    @Test
    public void tokenUnit_str10() {
        tokenizeAndTestExact("'\\'abc'", TokenType.STRING1, "'abc") ;
    }

    @Test
    public void tokenUnit_str11() {
        tokenizeAndTestExact("'\\U00000020'", TokenType.STRING1, " ") ;
    }

    @Test
    public void tokenUnit_str_long1() {
        tokenizeAndTestExact("'''aaa'''", TokenType.LONG_STRING1, "aaa") ;
    }

    @Test
    public void tokenUnit_str_long2() {
        tokenizeAndTestExact("\"\"\"aaa\"\"\"", TokenType.LONG_STRING2, "aaa") ;
    }

    @Test
    public void tokenUnit_str_long3() {
        tokenizeAndTestExact("''''1234'''", TokenType.LONG_STRING1, "'1234") ;
    }

    @Test
    public void tokenUnit_str_long4() {
        tokenizeAndTestExact("'''''1234'''", TokenType.LONG_STRING1, "''1234") ;
    }

    @Test
    public void tokenUnit_str_long5() {
        tokenizeAndTestExact("'''\\'''1234'''", TokenType.LONG_STRING1, "'''1234") ;
    }

    @Test
    public void tokenUnit_str_long6() {
        tokenizeAndTestExact("\"\"\"\"1234\"\"\"", TokenType.LONG_STRING2, "\"1234") ;
    }

    @Test
    public void tokenUnit_str_long7() {
        tokenizeAndTestExact("\"\"\"\"\"1234\"\"\"", TokenType.LONG_STRING2, "\"\"1234") ;
    }

    @Test
    public void tokenUnit_str_long8() {
        tokenizeAndTestExact("''''''", TokenType.LONG_STRING1, "") ;
    }

    @Test
    public void tokenUnit_str_long9() {
        tokenizeAndTestExact("\"\"\"'''''''''''''''''\"\"\"", TokenType.LONG_STRING2, "'''''''''''''''''") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_str_long10() {
        tokenFirst("\"\"\"abcdef") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_str_long11() {
        tokenFirst("'''") ;
    }

    @Test
    public void tokenUnit_str_long12() {
        tokenizeAndTestExact("'''x'''@en", TokenType.LITERAL_LANG, "x", "en") ;
    }

    @Test
    public void tokenUnit_bNode1() {
        tokenizeAndTestExact("_:abc", TokenType.BNODE, "abc") ;
    }

    @Test
    public void tokenUnit_bNode2() {
        tokenizeAndTestExact("_:123 ", TokenType.BNODE, "123") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_bNode3() {
        Tokenizer tokenizer = tokenizer("_:") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test
    public void tokenUnit_bNode4() {
        tokenizeAndTestExact("_:1-2-Z ", TokenType.BNODE, "1-2-Z") ;
    }

    @Test
    public void tokenUnit_bNode5() {
        Tokenizer tokenizer = tokenizeAndTestFirst("_:x.    ", TokenType.BNODE, "x") ;
        testNextToken(tokenizer, TokenType.DOT) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_bNode6() {
        Tokenizer tokenizer = tokenizeAndTestFirst("_:x:a.    ", TokenType.BNODE, "x") ;
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "", "a") ;
        testNextToken(tokenizer, TokenType.DOT) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    // @Test
    // public void tokenUnit_cntrl1()
    // {
    // tokenizeAndTestExact("*S", TokenType.CNTRL, "S") ;
    // }
    //
    // @Test
    // public void tokenUnit_cntr2()
    // {
    // tokenizeAndTestExact("*SXYZ", TokenType.CNTRL, "SXYZ") ;
    // }
    //
    // @Test
    // public void tokenUnit_cntrl3()
    // {
    // Tokenizer tokenizer = tokenizer("*S<x>") ;
    // assertTrue(tokenizer.hasNext()) ;
    // Token token = tokenizer.next() ;
    // assertNotNull(token) ;
    // assertEquals(TokenType.CNTRL, token.getType()) ;
    // assertEquals('S', token.getCntrlCode()) ;
    // assertNull(token.getImage()) ;
    // assertNull(token.getImage2()) ;
    //
    // assertTrue(tokenizer.hasNext()) ;
    // Token token2 = tokenizer.next() ;
    // assertNotNull(token2) ;
    // assertEquals(TokenType.IRI, token2.getType()) ;
    // assertEquals("x", token2.getImage()) ;
    // assertNull(token2.getImage2()) ;
    // assertFalse(tokenizer.hasNext()) ;
    // }

    @Test
    public void tokenUnit_syntax1() {
        tokenizeAndTestExact(".", TokenType.DOT, null, null) ;
    }

    @Test
    public void tokenUnit_syntax2() {
        Tokenizer tokenizer = tokenizer(".;,") ;
        testNextToken(tokenizer, TokenType.DOT) ;
        testNextToken(tokenizer, TokenType.SEMICOLON) ;
        testNextToken(tokenizer, TokenType.COMMA) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_pname1() {
        tokenizeAndTestExact("a:b.c", TokenType.PREFIXED_NAME, "a", "b.c") ;
    }

    @Test
    public void tokenUnit_pname2() {
        Tokenizer tokenizer = tokenizeAndTestFirst("a:b.", TokenType.PREFIXED_NAME, "a", "b") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOT, token.getType()) ;
    }

    @Test
    public void tokenUnit_pname3() {
        tokenizeAndTestExact("a:b123", TokenType.PREFIXED_NAME, "a", "b123") ;
    }

    @Test
    public void tokenUnit_pname4() {
        tokenizeAndTestExact("a:", TokenType.PREFIXED_NAME, "a", "") ;
    }

    @Test
    public void tokenUnit_pname5() {
        tokenizeAndTestExact(":", TokenType.PREFIXED_NAME, "", "") ;
    }

    @Test
    public void tokenUnit_pname6() {
        tokenizeAndTestExact(":a", TokenType.PREFIXED_NAME, "", "a") ;
    }

    @Test
    public void tokenUnit_pname7() {
        tokenizeAndTestExact(":123", TokenType.PREFIXED_NAME, "", "123") ;
    }

    @Test
    public void tokenUnit_pname8() {
        tokenizeAndTestExact("a123:456", TokenType.PREFIXED_NAME, "a123", "456") ;
    }

    @Test
    public void tokenUnit_pname9() {
        Tokenizer tokenizer = tokenizeAndTestFirst("a123:-456", TokenType.PREFIXED_NAME, "a123", "") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.INTEGER, token.getType()) ;
        assertEquals("-456", token.getImage()) ;
    }

    @Test
    public void tokenUnit_pname10() {
        tokenizeAndTestExact("a:a.b", TokenType.PREFIXED_NAME, "a", "a.b") ;
    }

    @Test
    public void tokenUnit_pname11() {
        tokenizeAndTestExact("a:0.b", TokenType.PREFIXED_NAME, "a", "0.b") ;
    }

    @Test
    public void tokenUnit_pname12() {
        tokenizeAndTestFirst("a:0. b", TokenType.PREFIXED_NAME, "a", "0") ;
    }

    @Test
    public void tokenUnit_pname13() {
        // x00e9 é
        // x0065 e and x0301 ́
        tokenizeAndTestExact("a:xyzé", TokenType.PREFIXED_NAME, "a", "xyz\u00e9") ;
    }

    @Test
    public void tokenUnit_pname14() {
        // x0065 e and x0301 ́
        tokenizeAndTestExact("a:xyze\u0301", TokenType.PREFIXED_NAME, "a", "xyze\u0301") ;
    }

    @Test
    public void tokenUnit_pname15() {
        // x0065 e and x0301 ́
        tokenizeAndTestExact("a:xe\u0301y", TokenType.PREFIXED_NAME, "a", "xe\u0301y") ;
    }

    @Test
    public void tokenUnit_pname16() {
        tokenizeAndTestExact("a:b\\#c", TokenType.PREFIXED_NAME, "a", "b#c") ;
    }

    @Test
    public void tokenUnit_pname17() {
        tokenizeAndTestExact("a:b\\/c", TokenType.PREFIXED_NAME, "a", "b/c") ;
    }

    @Test
    public void tokenUnit_pname18() {
        tokenizeAndTestExact("a:b:c", TokenType.PREFIXED_NAME, "a", "b:c") ;
    }

    @Test
    public void tokenUnit_pname19() {
        tokenizeAndTestExact("a:b%AAc", TokenType.PREFIXED_NAME, "a", "b%AAc") ;
    }

    @Test
    public void tokenUnit_25() {
        Tokenizer tokenizer = tokenizeAndTestFirst("123:", TokenType.INTEGER, "123") ;
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "", "") ;
    }

    // Generic: parse first token from ...
    // tokenTest(str, TokenType, TokenImage) ;

    @Test
    public void tokenUnit_num1() {
        tokenizeAndTestExact("123", TokenType.INTEGER, "123") ;
    }

    @Test
    public void tokenUnit_num2() {
        // This is a chnage in Turtle (and SPARQL 1.1)
        tokenizeAndTestFirst("123.", TokenType.INTEGER, "123") ;
    }

    @Test
    public void tokenUnit_num3() {
        tokenizeAndTestExact("+123.456", TokenType.DECIMAL, "+123.456") ;
    }

    @Test
    public void tokenUnit_num4() {
        tokenizeAndTestExact("-1", TokenType.INTEGER, "-1") ;
    }

    @Test
    public void tokenUnit_num5() {
        tokenizeAndTestExact("-1e0", TokenType.DOUBLE, "-1e0") ;
    }

    @Test
    public void tokenUnit_num6() {
        tokenizeAndTestExact("1e+1", TokenType.DOUBLE, "1e+1") ;
    }

    @Test
    public void tokenUnit_num7() {
        tokenizeAndTestExact("1.3e+1", TokenType.DOUBLE, "1.3e+1") ;
    }

    @Test
    public void tokenUnit_num8() {
        tokenizeAndTestFirst("1.3.4", TokenType.DECIMAL, "1.3") ;
    }

    @Test
    public void tokenUnit_num9() {
        tokenizeAndTestFirst("1.3e67.7", TokenType.DOUBLE, "1.3e67") ;
    }

    @Test
    public void tokenUnit_num10() {
        tokenizeAndTestExact(".1", TokenType.DECIMAL, ".1") ;
    }

    @Test
    public void tokenUnit_num11() {
        tokenizeAndTestExact(".1e0", TokenType.DOUBLE, ".1e0") ;
    }

    @Test
    public void tokenUnit_num12() {
        // This is not a hex number.

        Tokenizer tokenizer = tokenizeAndTestFirst("000A     .", TokenType.INTEGER, "000") ;
        testNextToken(tokenizer, TokenType.KEYWORD, "A") ;
    }

    @Test
    public void tokenUnit_var1() {
        tokenizeAndTestFirst("?x ?y", TokenType.VAR, "x") ;
    }

    @Test
    public void tokenUnit_var2() {
        tokenizeAndTestFirst("? x", TokenType.VAR, "") ;
    }

    @Test
    public void tokenUnit_var3() {
        tokenizeAndTestExact("??x", TokenType.VAR, "?x") ;
    }

    @Test
    public void tokenUnit_var4() {
        tokenizeAndTestExact("?.1", TokenType.VAR, ".1") ;
    }

    @Test
    public void tokenUnit_var5() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarMarker, TokenType.VAR, ARQConstants.allocVarMarker) ;
    }

    @Test
    public void tokenUnit_var6() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarMarker + "0", TokenType.VAR, ARQConstants.allocVarMarker + "0") ;
    }

    @Test
    public void tokenUnit_hex1() {
        tokenizeAndTestExact("0xABC", TokenType.HEX, "0xABC") ;
    }

    @Test
    public void tokenUnit_hex2() {
        tokenizeAndTestFirst("0xABCXYZ", TokenType.HEX, "0xABC") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenUnit_hex3() {
        tokenFirst("0xXYZ") ;
    }

    @Test
    public void tokenUnit_hex4() {
        tokenizeAndTestExact("0Xabc", TokenType.HEX, "0Xabc") ;
    }

    private static void tokenizeAndTestLiteralDT(String input, TokenType lexType, String image, TokenType dt,
                                                 String dtImage1, String dtImage2) {
        Token lexToken = new Token(lexType, image) ;
        Token dtToken = new Token(dt, dtImage1, dtImage2) ;
        tokenizeAndTest(input, TokenType.LITERAL_DT, image, null, lexToken, dtToken) ;

        Token expectedToken = new Token(TokenType.LITERAL_DT) ;
        expectedToken.setImage(image) ;
        expectedToken.setImage2(null) ;
        expectedToken.setSubToken1(lexToken) ;
        expectedToken.setSubToken2(dtToken) ;

        Token token = tokenFor(input) ;
        assertEquals(expectedToken, token) ;

        Token token2 = tokenizeAndTestExact(input, TokenType.LITERAL_DT, image).getSubToken2() ;
        assertEquals(dt, token2.getType()) ;
        assertEquals(dtImage1, token2.getImage()) ;
        assertEquals(dtImage2, token2.getImage2()) ;
    }

    @Test
    public void tokenLiteralDT_0() {
        tokenizeAndTestLiteralDT("\"123\"^^<x> ", TokenType.STRING2, "123", TokenType.IRI, "x", null) ;
    }

    // literal test function.

    @Test
    public void tokenLiteralDT_1() {
        tokenizeAndTestLiteralDT("'123'^^x:y ", TokenType.STRING1, "123", TokenType.PREFIXED_NAME, "x", "y") ;
    }

    @Test
    public void tokenLiteralDT_2() {
        tokenizeAndTestLiteralDT("'123'^^:y", TokenType.STRING1, "123", TokenType.PREFIXED_NAME, "", "y") ;
    }

    @Test
    public void tokenLiteralDT_3() {
        tokenizeAndTestLiteralDT("'''123'''^^<xyz>", TokenType.LONG_STRING1, "123", TokenType.IRI, "xyz", null) ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralDT_bad_1() {
        Tokenizer tokenizer = tokenizer("'123'^^ <x> ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralDT_bad_2() {
        Tokenizer tokenizer = tokenizer("'123' ^^<x> ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ; // 123
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("123", token.getImage()) ;

        assertTrue(tokenizer.hasNext()) ;
        Token token2 = tokenizer.next() ;
        assertNotNull(token2) ; // ^^
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralDT_bad_3() {
        Tokenizer tokenizer = tokenizer("'123'^ ^<x> ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralDT_bad_4() {
        Tokenizer tokenizer = tokenizer("'123'^^ x:y") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test
    public void tokenLiteralLang_0() {
        tokenizeAndTestExact("'a'@en", TokenType.LITERAL_LANG, "a", "en") ;
    }

    @Test
    public void tokenLiteralLang_1() {
        tokenizeAndTestExact("'a'@en-UK", TokenType.LITERAL_LANG, "a", "en-UK") ;
    }

    @Test
    public void tokenLiteralLang_2() {
        Tokenizer tokenizer = tokenizeAndTestFirst("'' @lang ", TokenType.STRING1, "") ;
        testNextToken(tokenizer, TokenType.DIRECTIVE, "lang") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralLang_3() {
        tokenFirst("''@ lang ") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralLang_4() {
        tokenFirst("''@lang- ") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralLang_5() {
        tokenFirst("'abc'@- ") ;
    }

    @Test
    public void tokenLiteralLang_6() {
        tokenizeAndTestExact("'XYZ'@a-b-c ", TokenType.LITERAL_LANG, "XYZ", "a-b-c") ;
    }

    @Test
    public void tokenLiteralLang_7() {
        tokenizeAndTestExact("'X'@a-b9z-c99 ", TokenType.LITERAL_LANG, "X", "a-b9z-c99") ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenLiteralLang_8() {
        tokenFirst("''@9-b") ;
    }

    @Test
    public void directive_1() {
        tokenizeAndTestExact("@prefix", TokenType.DIRECTIVE, "prefix") ;
    }

    @Test
    public void directive_2() {
        tokenizeAndTestExact("@base", TokenType.DIRECTIVE, "base") ;
    }

    @Test
    public void tokenComment_01() {
        tokenizeAndTestExact("_:123 # Comment", TokenType.BNODE, "123") ;
    }

    @Test
    public void tokenComment_02() {
        tokenizeAndTestExact("\"foo # Non-Comment\"", TokenType.STRING2, "foo # Non-Comment") ;
    }

    @Test
    public void tokenComment_03() {
        Tokenizer tokenizer = tokenizeAndTestFirst("'foo' # Comment\n'bar'", TokenType.STRING1, "foo") ;
        testNextToken(tokenizer, TokenType.STRING1, "bar") ;
    }

    @Test
    public void tokenWord_01() {
        tokenizeAndTestExact("abc", TokenType.KEYWORD, "abc") ;
    }

    // Multiple terms

    @Test
    public void token_multiple() {
        Tokenizer tokenizer = tokenizer("<x><y>") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.IRI, token.getType()) ;
        assertEquals("x", token.getImage()) ;

        assertTrue(tokenizer.hasNext()) ;
        Token token2 = tokenizer.next() ;
        assertNotNull(token2) ;
        assertEquals(TokenType.IRI, token2.getType()) ;
        assertEquals("y", token2.getImage()) ;

        assertFalse(tokenizer.hasNext()) ;
    }

    // These tests converts some java characters to UTF-8 and read back as
    // ASCII.

    private static ByteArrayInputStream bytes(String string) {
        byte b[] = StrUtils.asUTF8bytes(string) ;
        return new ByteArrayInputStream(b) ;
    }

    @Test
    public void tokenizer_charset_1() {
        ByteArrayInputStream in = bytes("'abc'") ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(in) ;
        Token t = tokenizer.next() ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenizer_charset_2() {
        ByteArrayInputStream in = bytes("'abcdé'") ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(in) ;
        Token t = tokenizer.next() ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test(expected = RiotParseException.class)
    public void tokenizer_charset_3() {
        ByteArrayInputStream in = bytes("<http://example/abcdé>") ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(in) ;
        Token t = tokenizer.next() ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenizer_BOM_1() {
        // BOM
        ByteArrayInputStream in = bytes("\uFEFF'abc'") ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in) ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("abc", token.getImage()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    // First symbol from the stream.
    private static void testSymbol(String string, TokenType expected) {
        tokenizeAndTestFirst(string, expected, null) ;
    }

    // -- Symbols
    // CNTRL
    // @Test public void tokenizer_symbol_01() { testSymbol("*", TokenType.STAR)
    // ; }
    @Test
    public void tokenizer_symbol_02() {
        testSymbol("+", TokenType.PLUS) ;
    }

    @Test
    public void tokenizer_symbol_03() {
        testSymbol("-", TokenType.MINUS) ;
    }

    // @Test public void tokenizer_symbol_04() { testSymbol("<", TokenType.LT) ;
    // }
    @Test
    public void tokenizer_symbol_05() {
        testSymbol(">", TokenType.GT) ;
    }

    @Test
    public void tokenizer_symbol_06() {
        testSymbol("=", TokenType.EQUALS) ;
    }

    // @Test public void tokenizer_symbol_07() { testSymbol(">=", TokenType.LE)
    // ; }
    // @Test public void tokenizer_symbol_08() { testSymbol("<=", TokenType.GE)
    // ; }
    // @Test public void tokenizer_symbol_09() { testSymbol("&&",
    // TokenType.LOGICAL_AND) ; }
    // @Test public void tokenizer_symbol_10() { testSymbol("||",
    // TokenType.LOGICAL_OR) ; }
    // @Test public void tokenizer_symbol_11() { testSymbol("&  &",
    // TokenType.AMPHERSAND) ; }
    // @Test public void tokenizer_symbol_12() { testSymbol("| |",
    // TokenType.VBAR) ; }

    @Test
    public void tokenUnit_symbol_11() {
        testSymbol("+A", TokenType.PLUS) ;
    }

    @Test
    public void tokenUnit_symbol_12() {
        Tokenizer tokenizer = tokenizeAndTestFirst("+-", TokenType.PLUS, null) ;
        testNextToken(tokenizer, TokenType.MINUS) ;
    }

    @Test
    public void tokenUnit_symbol_13() {
        testSymbol(".", TokenType.DOT) ;
    }

    @Test
    public void tokenUnit_symbol_14() {
        Tokenizer tokenizer = tokenizeAndTestFirst(".a", TokenType.DOT, null) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "a") ;
    }

    @Test
    public void token_newlines_1() {
        Tokenizer tokenizer = tokenizer("\n", true) ;
        testNextToken(tokenizer, TokenType.NL) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void token_newlines_2() {
        Tokenizer tokenizer = tokenizer("abc\ndef", true) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "abc") ;
        testNextToken(tokenizer, TokenType.NL) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "def") ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void token_newlines_3() {
        Tokenizer tokenizer = tokenizer("abc\n\ndef", true) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "abc") ;
        testNextToken(tokenizer, TokenType.NL) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "def") ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void token_newlines_4() {
        Tokenizer tokenizer = tokenizer("abc\n\rdef", true) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "abc") ;
        testNextToken(tokenizer, TokenType.NL) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "def") ;
        assertFalse(tokenizer.hasNext()) ;
    }

    public void token_newlines_5() {
        Tokenizer tokenizer = tokenizer("abc\n\n", true) ;
        testNextToken(tokenizer, TokenType.KEYWORD, "abc") ;
        testNextToken(tokenizer, TokenType.NL) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    public void token_newlines_6() {
        Tokenizer tokenizer = tokenizer("\n \n", true) ;
        testNextToken(tokenizer, TokenType.NL) ;
        testNextToken(tokenizer, TokenType.NL) ;
        assertFalse(tokenizer.hasNext()) ;
    }
}
