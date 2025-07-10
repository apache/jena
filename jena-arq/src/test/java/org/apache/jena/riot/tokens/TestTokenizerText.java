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

import static org.apache.jena.riot.system.ErrorHandlerFactory.errorHandlerExceptions;
import static org.apache.jena.riot.system.ErrorHandlerFactory.errorHandlerSimple;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.ErrorHandlerFactory.ErrorHandlerRecorder;
import org.apache.jena.sparql.ARQConstants;

public class TestTokenizerText {

    private static Tokenizer tokenizer(String string) {
        return tokenizer(string, false);
    }

    private static Tokenizer tokenizer(String string, boolean lineMode) {
        PeekReader r = PeekReader.readString(string);
        Tokenizer tokenizer = TokenizerText.create()
                .errorHandler(errorHandlerExceptions())
                .source(r)
                .lineMode(lineMode)
                .build();
        return tokenizer;
    }

    private static void tokenFirst(String string) {
        Tokenizer tokenizer = tokenizer(string);
        assertTrue(tokenizer.hasNext());
        assertNotNull(tokenizer.next());
        // Maybe more.
        // assertFalse(tokenizer.hasNext());
    }

    private static Token tokenFor(String string) {
        Tokenizer tokenizer = tokenizer(string);
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        assertFalse(tokenizer.hasNext());
        return token;
    }

    private static Token tokenizeAndTestExact(String input, TokenType tokenType, String tokenImage) {
        return tokenizeAndTestExact(input, tokenType, tokenImage, null);
    }

    private static Token tokenizeAndTestExact(String input, StringType stringType, String tokenImage) {
        Token token = tokenizeAndTestExact(input, TokenType.STRING, tokenImage, null);
        assertEquals(stringType, token.getStringType());
        return token;
    }

    private static Token tokenizeAndTestExact(String input, TokenType tokenType, String tokenImage1, String tokenImage2) {
        Tokenizer tokenizer = tokenizer(input);
        Token token = testNextToken(tokenizer, tokenType, tokenImage1, tokenImage2);
        assertFalse(tokenizer.hasNext(), ()->"Excess tokens");
        return token;
    }

    private static Token tokenizeAndTestExact(String input, TokenType tokenType, String tokenImage1,
                                              String tokenImage2, Token subToken1, Token subToken2) {
        Token token = tokenFor(input);
        assertEquals(tokenType, token.getType());
        assertEquals(tokenImage1, token.getImage());
        assertEquals(tokenImage2, token.getImage2());
        assertEquals(subToken1, token.getSubToken1());
        assertEquals(subToken2, token.getSubToken2());
        return token;
    }

    private static Tokenizer tokenizeAndTestFirst(String input, TokenType tokenType) {
        return tokenizeAndTestFirst(input, tokenType, null, null);
    }

    private static Tokenizer tokenizeAndTestFirst(String input, TokenType tokenType, String tokenImage) {
        return tokenizeAndTestFirst(input, tokenType, tokenImage, null);
    }

    private static Tokenizer tokenizeAndTestFirst(String input, TokenType tokenType, String tokenImage1, String tokenImage2) {
        Tokenizer tokenizer = tokenizer(input);
        testNextToken(tokenizer, tokenType, tokenImage1, tokenImage2);
        return tokenizer;
    }

    private static Token testNextToken(Tokenizer tokenizer, TokenType tokenType) {
        return testNextToken(tokenizer, tokenType, null, null);
    }

    private static Token testNextToken(Tokenizer tokenizer, TokenType tokenType, String tokenImage1) {
        return testNextToken(tokenizer, tokenType, tokenImage1, null);
    }

    private static Token testNextToken(Tokenizer tokenizer, TokenType tokenType, String tokenImage1, String tokenImage2) {
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        assertNotNull(token);
        assertEquals(tokenType, token.getType());
        if ( tokenImage1 != null )
            assertEquals(tokenImage1, token.getImage());
        if ( tokenImage2 != null )
            assertEquals(tokenImage1, token.getImage());
        assertEquals(tokenImage2, token.getImage2());
        return token;
    }

    private static Token tokenizeAndTest(String input, TokenType tokenType, String tokenImage1, String tokenImage2, Token subToken1, Token subToken2) {
        Token token = tokenFor(input);
        assertNotNull(token);
        assertEquals(tokenType, token.getType());
        assertEquals(tokenImage1, token.getImage());
        assertEquals(tokenImage2, token.getImage2());
        assertEquals(subToken1, token.getSubToken1());
        assertEquals(subToken2, token.getSubToken2());
        return token;
    }

    @Test
    public void tokenUnit_iri1() {
        tokenizeAndTestExact("<x>", TokenType.IRI, "x");
    }

    @Test
    public void tokenUnit_iri2() {
        tokenizeAndTestExact("   <>   ", TokenType.IRI, "");
    }

    @Test
    public void tokenUnit_iri3() {
        RiotParseException ex = assertThrows(RiotParseException.class, () -> tokenFirst("<abc\\>def>"));
        String x = ex.getMessage();
        assertTrue(x.contains("Illegal"));
    }

    @Test
    public void tokenUnit_iri4() {
        // \\\\ is a double \\ in the data. 0x41 is 'A'
        tokenizeAndTestFirst("<abc\\u0041def>   123", TokenType.IRI, "abcAdef");
    }

    @Test
    public void tokenUnit_iri5() {
        // \\\\ is a double \\ in the data. 0x41 is 'A'
        tokenizeAndTestFirst("<\\u0041def>   123", TokenType.IRI, "Adef");
    }

    @Test
    public void tokenUnit_iri6() {
        // \\\\ is a double \\ in the data. 0x41 is 'A'
        tokenizeAndTestFirst("<abc\\u0041>   123", TokenType.IRI, "abcA");
    }

    // Bad IRIs
    @Test
    public void tokenUnit_iri10() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc def>"));
    }

    @Test
    public void tokenUnit_iri11() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc<def>"));
    }

    @Test
    public void tokenUnit_iri12() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc{def>"));
    }

    @Test
    public void tokenUnit_iri13() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc}def>"));
    }

    @Test
    public void tokenUnit_iri14() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc|def>"));
    }

    @Test
    public void tokenUnit_iri15() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc^def>"));
    }

    @Test
    public void tokenUnit_iri16() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc`def>"));
    }

    @Test
    public void tokenUnit_iri17() {
        // Java escape - real tab
        assertThrows(RiotException.class, () ->
            tokenFirst("<abc\tdef>")
        );
    }

    @Test
    public void tokenUnit_iri18() {
        // Java escape - codepoint 7
        assertThrows(RiotException.class, ()->
            tokenFirst("<abc\u0007def>")
        );
    }

    @Test
    public void tokenUnit_iri19() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc\\>"));
    }

    @Test
    public void tokenUnit_iri20() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc\\def>"));
    }

    @Test
    public void tokenUnit_iri21() {
        // \\\\ is a double \\ in the data
        // RDF 1.1 - \\ is not legal in a IRIREF
        assertThrows(RiotException.class, () -> tokenFirst("<abc\\\\def>"));
    }

    @Test
    public void tokenUnit_iri22() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc\\u00ZZdef>"));
    }

    @Test
    public void tokenUnit_iri23() {
		assertThrows(RiotException.class, ()->
					 tokenFirst("<abc\\uZZ20def>"));
    }

    @Test
    public void tokenUnit_str1() {
        tokenizeAndTestExact("   'abc'   ", StringType.STRING1, "abc");
    }

    @Test
    public void tokenUnit_str2() {
        tokenizeAndTestExact("   ''   ", StringType.STRING1, "");
    }

    @Test
    public void tokenUnit_str3() {
        tokenizeAndTestExact("'\\u0020'", StringType.STRING1, " ");
    }

    @Test
    public void tokenUnit_str4() {
        tokenizeAndTestExact("'a\\'\\\"\\n\\t\\r\\f'", StringType.STRING1, "a'\"\n\t\r\f");
    }

    @Test
    public void tokenUnit_str5() {
		assertThrows(RiotParseException.class, ()->
        // This is a raw newline. \n is a Java string escape.
        tokenizeAndTestExact("'\n'", StringType.STRING1, "\n")
					 );
    }

    @Test
    public void tokenUnit_str6() {
        tokenizeAndTestExact("   \"abc\"   ", StringType.STRING2, "abc");
    }

    @Test
    public void tokenUnit_str7() {
        tokenizeAndTestExact("\"\"", StringType.STRING2, "");
    }

    @Test
    public void tokenUnit_str8() {
		assertThrows(RiotParseException.class, ()->{
					 Tokenizer tokenizer = tokenizer("\"");
					 assertTrue(tokenizer.hasNext());
		});
    }

    @Test
    public void tokenUnit_str9() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("'abc"));
    }

    @Test
    public void tokenUnit_str10() {
        tokenizeAndTestExact("'\\'abc'", StringType.STRING1, "'abc");
    }

    @Test
    public void tokenUnit_str11() {
        tokenizeAndTestExact("'\\U00000020'", StringType.STRING1, " ");
    }

    // Raw newline and carriage return not allowed in single quoted strings

    @Test
    public void tokenUnit_str12() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("'abc\rdef'"));
    }

    @Test
    public void tokenUnit_str13() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("'abc\ndef'"));
    }

    @Test
    public void tokenUnit_str14() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("\"abc\rdef\""));
    }

    @Test
    public void tokenUnit_str15() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("\"abc\ndef\""));
    }

    @Test
    public void tokenUnit_str16() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("'\r'"));
    }

    @Test
    public void tokenUnit_str17() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("\"\n\""));
    }

    @Test
    public void tokenUnit_str_long1() {
        tokenizeAndTestExact("'''aaa'''", StringType.LONG_STRING1, "aaa");
    }

    @Test
    public void tokenUnit_str_long2() {
        tokenizeAndTestExact("\"\"\"aaa\"\"\"", StringType.LONG_STRING2, "aaa");
    }

    @Test
    public void tokenUnit_str_long3() {
        tokenizeAndTestExact("''''1234'''", StringType.LONG_STRING1, "'1234");
    }

    @Test
    public void tokenUnit_str_long4() {
        tokenizeAndTestExact("'''''1234'''", StringType.LONG_STRING1, "''1234");
    }

    @Test
    public void tokenUnit_str_long5() {
        tokenizeAndTestExact("'''\\'''1234'''", StringType.LONG_STRING1, "'''1234");
    }

    @Test
    public void tokenUnit_str_long6() {
        tokenizeAndTestExact("\"\"\"\"1234\"\"\"", StringType.LONG_STRING2, "\"1234");
    }

    @Test
    public void tokenUnit_str_long7() {
        tokenizeAndTestExact("\"\"\"\"\"1234\"\"\"", StringType.LONG_STRING2, "\"\"1234");
    }

    @Test
    public void tokenUnit_str_long8() {
        tokenizeAndTestExact("''''''", StringType.LONG_STRING1, "");
    }

    @Test
    public void tokenUnit_str_long9() {
        tokenizeAndTestExact("\"\"\"'''''''''''''''''\"\"\"", StringType.LONG_STRING2, "'''''''''''''''''");
    }

    @Test
    public void tokenUnit_str_long10() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("\"\"\"abcdef"));
    }

    @Test
    public void tokenUnit_str_long11() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("'''"));
    }

    @Test
    public void tokenUnit_str_long20() {
        tokenizeAndTestExact("\"\"\"abc\ndef\"\"\"", StringType.LONG_STRING2, "abc\ndef");
    }

    @Test
    public void tokenUnit_str_long21() {
        tokenizeAndTestExact("\"\"\"abc\rdef\"\"\"", StringType.LONG_STRING2, "abc\rdef");
    }

    @Test
    public void tokenUnit_str_long22() {
        tokenizeAndTestExact("'''abc\ndef'''", StringType.LONG_STRING1, "abc\ndef");
    }

    @Test
    public void tokenUnit_str_long23() {
        tokenizeAndTestExact("'''abc\rdef'''", StringType.LONG_STRING1, "abc\rdef");
    }


    @Test
    public void tokenUnit_str_long12() {
        tokenizeAndTestExact("'''x'''@en", TokenType.LITERAL_LANG, "x", "en");
    }

    @Test
    public void tokenUnit_bNode1() {
        tokenizeAndTestExact("_:abc", TokenType.BNODE, "abc");
    }

    @Test
    public void tokenUnit_bNode2() {
        tokenizeAndTestExact("_:123 ", TokenType.BNODE, "123");
    }

    @Test
    public void tokenUnit_bNode3() {
		assertThrows(RiotParseException.class, ()->{
            Tokenizer tokenizer = tokenizer("_:");
            assertTrue(tokenizer.hasNext());
            Token token = tokenizer.next();
            assertNotNull(token);
		});
    }

    @Test
    public void tokenUnit_bNode4() {
        tokenizeAndTestExact("_:1-2-Z ", TokenType.BNODE, "1-2-Z");
    }

    @Test
    public void tokenUnit_bNode5() {
        Tokenizer tokenizer = tokenizeAndTestFirst("_:x.    ", TokenType.BNODE, "x");
        testNextToken(tokenizer, TokenType.DOT);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void tokenUnit_bNode6() {
        Tokenizer tokenizer = tokenizeAndTestFirst("_:x:a.    ", TokenType.BNODE, "x");
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "", "a");
        testNextToken(tokenizer, TokenType.DOT);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void tokenUnit_syntax1() {
        tokenizeAndTestExact(".", TokenType.DOT, null, null);
    }

    @Test
    public void tokenUnit_syntax2() {
        Tokenizer tokenizer = tokenizer(".;,");
        testNextToken(tokenizer, TokenType.DOT);
        testNextToken(tokenizer, TokenType.SEMICOLON);
        testNextToken(tokenizer, TokenType.COMMA);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void tokenUnit_pname1() {
        tokenizeAndTestExact("a:b.c", TokenType.PREFIXED_NAME, "a", "b.c");
    }

    @Test
    public void tokenUnit_pname2() {
        Tokenizer tokenizer = tokenizeAndTestFirst("a:b.", TokenType.PREFIXED_NAME, "a", "b");
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        assertEquals(TokenType.DOT, token.getType());
    }

    @Test
    public void tokenUnit_pname3() {
        tokenizeAndTestExact("a:b123", TokenType.PREFIXED_NAME, "a", "b123");
    }

    @Test
    public void tokenUnit_pname4() {
        tokenizeAndTestExact("a:", TokenType.PREFIXED_NAME, "a", "");
    }

    @Test
    public void tokenUnit_pname5() {
        tokenizeAndTestExact(":", TokenType.PREFIXED_NAME, "", "");
    }

    @Test
    public void tokenUnit_pname6() {
        tokenizeAndTestExact(":a", TokenType.PREFIXED_NAME, "", "a");
    }

    @Test
    public void tokenUnit_pname7() {
        tokenizeAndTestExact(":123", TokenType.PREFIXED_NAME, "", "123");
    }

    @Test
    public void tokenUnit_pname8() {
        tokenizeAndTestExact("a123:456", TokenType.PREFIXED_NAME, "a123", "456");
    }

    @Test
    public void tokenUnit_pname9() {
        Tokenizer tokenizer = tokenizeAndTestFirst("a123:-456", TokenType.PREFIXED_NAME, "a123", "");
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        assertEquals(TokenType.INTEGER, token.getType());
        assertEquals("-456", token.getImage());
    }

    @Test
    public void tokenUnit_pname10() {
        tokenizeAndTestExact("a:a.b", TokenType.PREFIXED_NAME, "a", "a.b");
    }

    @Test
    public void tokenUnit_pname11() {
        tokenizeAndTestExact("a:0.b", TokenType.PREFIXED_NAME, "a", "0.b");
    }

    @Test
    public void tokenUnit_pname12() {
        tokenizeAndTestFirst("a:0. b", TokenType.PREFIXED_NAME, "a", "0");
    }

    @Test
    public void tokenUnit_pname13() {
        // x00e9 é
        // x0065 e and x0301 ́
        tokenizeAndTestExact("a:xyzé", TokenType.PREFIXED_NAME, "a", "xyz\u00e9");
    }

    @Test
    public void tokenUnit_pname14() {
        // x0065 e and x0301 ́
        tokenizeAndTestExact("a:xyze\u0301", TokenType.PREFIXED_NAME, "a", "xyze\u0301");
    }

    @Test
    public void tokenUnit_pname15() {
        // x0065 e and x0301 ́
        tokenizeAndTestExact("a:xe\u0301y", TokenType.PREFIXED_NAME, "a", "xe\u0301y");
    }

    @Test
    public void tokenUnit_pname16() {
        tokenizeAndTestExact("a:b\\#c", TokenType.PREFIXED_NAME, "a", "b#c");
    }

    @Test
    public void tokenUnit_pname17() {
        tokenizeAndTestExact("a:b\\/c", TokenType.PREFIXED_NAME, "a", "b/c");
    }

    @Test
    public void tokenUnit_pname18() {
        tokenizeAndTestExact("a:b:c", TokenType.PREFIXED_NAME, "a", "b:c");
    }

    @Test
    public void tokenUnit_pname19() {
        tokenizeAndTestExact("a:b%AAc", TokenType.PREFIXED_NAME, "a", "b%AAc");
    }

    @Test
    public void tokenUnit_pname20() {
        Tokenizer tokenizer = tokenizeAndTestFirst("123:", TokenType.INTEGER, "123");
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "", "");
    }

    // Generic: parse first token from ...
    // tokenTest(str, TokenType, TokenImage);

    @Test
    public void tokenUnit_num01() {
        tokenizeAndTestExact("123", TokenType.INTEGER, "123");
    }

    @Test
    public void tokenUnit_num02() {
        // This is a change in Turtle (and SPARQL 1.1)
        tokenizeAndTestFirst("123.", TokenType.INTEGER, "123");
    }

    @Test
    public void tokenUnit_num03() {
        tokenizeAndTestExact("+123.456", TokenType.DECIMAL, "+123.456");
    }

    @Test
    public void tokenUnit_num04() {
        tokenizeAndTestExact("-1", TokenType.INTEGER, "-1");
    }

    @Test
    public void tokenUnit_num05() {
        tokenizeAndTestExact("-1e0", TokenType.DOUBLE, "-1e0");
    }

    @Test
    public void tokenUnit_num06() {
        tokenizeAndTestExact("1e+1", TokenType.DOUBLE, "1e+1");
    }

    @Test
    public void tokenUnit_num07() {
        tokenizeAndTestExact("1.3e+1", TokenType.DOUBLE, "1.3e+1");
    }

    @Test
    public void tokenUnit_num08() {
        tokenizeAndTestFirst("1.3.4", TokenType.DECIMAL, "1.3");
    }

    @Test
    public void tokenUnit_num09() {
        tokenizeAndTestFirst("1.3e67.7", TokenType.DOUBLE, "1.3e67");
    }

    @Test
    public void tokenUnit_num10() {
        tokenizeAndTestExact(".1", TokenType.DECIMAL, ".1");
    }

    @Test
    public void tokenUnit_num11() {
        tokenizeAndTestExact(".1e0", TokenType.DOUBLE, ".1e0");
    }

    @Test
    public void tokenUnit_num12() {
        tokenizeAndTestExact("+.1", TokenType.DECIMAL, "+.1");
    }

    @Test
    public void tokenUnit_num13() {
        tokenizeAndTestExact("-.1", TokenType.DECIMAL, "-.1");
    }

    @Test
    public void tokenUnit_num14() {
        tokenizeAndTestExact("+.1e0", TokenType.DOUBLE, "+.1e0");
    }

    @Test
    public void tokenUnit_num15() {
        tokenizeAndTestExact("-.1e0", TokenType.DOUBLE, "-.1e0");
    }

    @Test
    public void tokenUnit_num20() {
        Tokenizer tokenizer = tokenizer("+.123.");
        testNextToken(tokenizer, TokenType.DECIMAL, "+.123");
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    @Test
    public void tokenUnit_num21() {
        Tokenizer tokenizer = tokenizer("++.123.");
        testNextToken(tokenizer, TokenType.PLUS);
        testNextToken(tokenizer, TokenType.DECIMAL, "+.123");
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    @Test
    public void tokenUnit_num22() {
        Tokenizer tokenizer = tokenizer("123.");
        testNextToken(tokenizer, TokenType.INTEGER, "123");
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    @Test
    public void tokenUnit_dot1() {
        Tokenizer tokenizer = tokenizer("+.");
        testNextToken(tokenizer, TokenType.PLUS);
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    @Test
    public void tokenUnit_dot2() {
        Tokenizer tokenizer = tokenizer("++.");
        testNextToken(tokenizer, TokenType.PLUS);
        testNextToken(tokenizer, TokenType.PLUS);
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    @Test
    public void tokenUnit_dot3() {
        Tokenizer tokenizer = tokenizer("++..");
        testNextToken(tokenizer, TokenType.PLUS);
        testNextToken(tokenizer, TokenType.PLUS);
        testNextToken(tokenizer, TokenType.DOT);
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    @Test
    public void tokenUnit_dot4() {
        Tokenizer tokenizer = tokenizer("..");
        testNextToken(tokenizer, TokenType.DOT);
        testNextToken(tokenizer, TokenType.DOT);
        assertTrue(tokenizer.eof());
    }

    // This is not a hex number.
    @Test
    public void tokenUnit_num99() {
        Tokenizer tokenizer = tokenizeAndTestFirst("000A     .", TokenType.INTEGER, "000");
        testNextToken(tokenizer, TokenType.KEYWORD, "A");
    }

    @Test
    public void tokenUnit_var1() {
        tokenizeAndTestFirst("?x ?y", TokenType.VAR, "x");
    }

    @Test
    public void tokenUnit_var2() {
        tokenizeAndTestFirst("? x", TokenType.VAR, "");
    }

    @Test
    public void tokenUnit_var3() {
        tokenizeAndTestExact("??x", TokenType.VAR, "?x");
    }

    @Test
    public void tokenUnit_var4() {
        tokenizeAndTestExact("?.1", TokenType.VAR, ".1");
    }

    @Test
    public void tokenUnit_var5() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarMarker, TokenType.VAR, ARQConstants.allocVarMarker);
    }

    @Test
    public void tokenUnit_var6() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarMarker + "0", TokenType.VAR, ARQConstants.allocVarMarker + "0");
    }

    @Test
    public void tokenUnit_var7() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarScopeHiding + "0", TokenType.VAR, ARQConstants.allocVarScopeHiding + "0");
    }

    @Test
    public void tokenUnit_var8() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarAnonMarker + "0", TokenType.VAR, ARQConstants.allocVarAnonMarker + "0");
    }

    @Test
    public void tokenUnit_var9() {
        tokenizeAndTestExact("?" + ARQConstants.allocVarTripleTerm + "9", TokenType.VAR, ARQConstants.allocVarTripleTerm + "9");
    }

    @Test
    public void tokenUnit_hex1() {
        tokenizeAndTestExact("0xABC", TokenType.HEX, "0xABC");
    }

    @Test
    public void tokenUnit_hex2() {
        tokenizeAndTestFirst("0xABCXYZ", TokenType.HEX, "0xABC");
    }

    @Test
    public void tokenUnit_hex3() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("0xXYZ"));
    }

    @Test
    public void tokenUnit_hex4() {
        tokenizeAndTestExact("0Xabc", TokenType.HEX, "0Xabc");
    }

    private static void tokenizeAndTestLiteralDT(String input, StringType lexType, String image, TokenType dt,
                                                 String dtImage1, String dtImage2) {
        Token lexToken = new Token(TokenType.STRING, image);
        lexToken.setStringType(lexType);
        Token dtToken = new Token(dt, dtImage1, dtImage2);
        tokenizeAndTest(input, TokenType.LITERAL_DT, image, null, lexToken, dtToken);

        Token expectedToken = new Token(TokenType.LITERAL_DT);
        expectedToken.setImage(image);
        expectedToken.setImage2(null);
        expectedToken.setSubToken1(lexToken);
        expectedToken.setSubToken2(dtToken);

        Token token = tokenFor(input);
        assertEquals(expectedToken, token);

        Token token2 = tokenizeAndTestExact(input, TokenType.LITERAL_DT, image).getSubToken2();
        assertEquals(dt, token2.getType());
        assertEquals(dtImage1, token2.getImage());
        assertEquals(dtImage2, token2.getImage2());
    }

    @Test
    public void tokenLiteralDT_0() {
        tokenizeAndTestLiteralDT("\"123\"^^<x> ", StringType.STRING2, "123", TokenType.IRI, "x", null);
    }

    // literal test function.

    @Test
    public void tokenLiteralDT_1() {
        tokenizeAndTestLiteralDT("'123'^^x:y ", StringType.STRING1, "123", TokenType.PREFIXED_NAME, "x", "y");
    }

    @Test
    public void tokenLiteralDT_2() {
        tokenizeAndTestLiteralDT("'123'^^:y", StringType.STRING1, "123", TokenType.PREFIXED_NAME, "", "y");
    }

    @Test
    public void tokenLiteralDT_3() {
        tokenizeAndTestLiteralDT("'''123'''^^<xyz>", StringType.LONG_STRING1, "123", TokenType.IRI, "xyz", null);
    }

    @Test
    public void tokenLiteralDT_4() {
        tokenizeAndTestLiteralDT("'123'  ^^<xyz>", StringType.STRING1, "123", TokenType.IRI, "xyz", null);
    }

    @Test
    public void tokenLiteralDT_bad_1() {
        // Can't split ^^
		assertThrows(RiotParseException.class, ()->{
				Tokenizer tokenizer = tokenizer("'123'^ ^<x> ");
				assertTrue(tokenizer.hasNext());
				Token token = tokenizer.next();
				assertNotNull(token);
			});
    }

    @Test
    public void tokenLiteralLang_0() {
        tokenizeAndTestExact("'a'@en", TokenType.LITERAL_LANG, "a", "en");
    }

    @Test
    public void tokenLiteralLang_1() {
        tokenizeAndTestExact("'a'@en-UK", TokenType.LITERAL_LANG, "a", "en-UK");
    }

    @Test
    public void tokenLiteralLang_2() {
        Tokenizer tokenizer = tokenizeAndTestFirst("'' @lang ", TokenType.LITERAL_LANG, "", "lang");
        //testNextToken(tokenizer, TokenType.LITERAL_LANG, "lang");
    }

    @Test
    public void tokenLiteralLang_3() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("''@ lang "));
    }

    @Test
    public void tokenLiteralLang_4() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("''@lang- "));
    }

    @Test
    public void tokenLiteralLang_5() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("'abc'@- "));
    }

    @Test
    public void tokenLiteralLang_6() {
        tokenizeAndTestExact("'XYZ'@a-b-c ", TokenType.LITERAL_LANG, "XYZ", "a-b-c");
    }

    @Test
    public void tokenLiteralLang_7() {
        tokenizeAndTestExact("'X'@a-b9z-c99 ", TokenType.LITERAL_LANG, "X", "a-b9z-c99");
    }

    @Test
    public void tokenLiteralLang_8() {
        tokenizeAndTestExact("'X'  @a", TokenType.LITERAL_LANG, "X", "a");
    }

    @Test
    public void tokenLiteralLang_bad_1() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("''@9-b"));
    }

    @Test
    public void tokenLiteralLang_bad_2() {
		assertThrows(RiotParseException.class, ()->
					 tokenFirst("''@  tag"));
    }

    @Test
    public void tokenLiteralLangDir_1() {
        tokenizeAndTestExact("'X'  @a--ltr", TokenType.LITERAL_LANG, "X", "a--ltr");
    }

    @Test
    public void tokenLiteralLangDir_2() {
        // Not checked at this point.
        tokenizeAndTestExact("'X'  @a--AB", TokenType.LITERAL_LANG, "X", "a--AB");
    }

    @Test
    public void tokenLiteralLangDir_3() {
        tokenizeAndTestExact("'X'  @en-GB--ltr", TokenType.LITERAL_LANG, "X", "en-GB--ltr");
    }

    @Test
    public void directive_1() {
        tokenizeAndTestExact("@prefix", TokenType.DIRECTIVE, "prefix");
    }

    @Test
    public void directive_2() {
        tokenizeAndTestExact("@base", TokenType.DIRECTIVE, "base");
    }

    @Test
    public void directive_3() {
        tokenizeAndTestExact("@whatever", TokenType.DIRECTIVE, "whatever");
    }

    @Test
    public void tokenComment_01() {
        tokenizeAndTestExact("_:123 # Comment", TokenType.BNODE, "123");
    }

    @Test
    public void tokenComment_02() {
        tokenizeAndTestExact("\"foo # Non-Comment\"", TokenType.STRING, "foo # Non-Comment");
    }

    @Test
    public void tokenComment_03() {
        Tokenizer tokenizer = tokenizeAndTestFirst("'foo' # Comment\n'bar'", TokenType.STRING, "foo");
        testNextToken(tokenizer, TokenType.STRING, "bar");
    }

    @Test
    public void tokenWord_01() {
        tokenizeAndTestExact("abc", TokenType.KEYWORD, "abc");
    }

    // Multiple terms

    @Test
    public void token_multiple() {
        Tokenizer tokenizer = tokenizer("<x><y>");
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.IRI, token.getType());
        assertEquals("x", token.getImage());

        assertTrue(tokenizer.hasNext());
        Token token2 = tokenizer.next();
        assertNotNull(token2);
        assertEquals(TokenType.IRI, token2.getType());
        assertEquals("y", token2.getImage());

        assertFalse(tokenizer.hasNext());
    }

    // These tests converts some java characters to UTF-8 and read back as
    // ASCII.

    private static Tokenizer tokenizerASCII (String string) {
        ByteArrayInputStream in = bytes(string);
        Tokenizer tokenizer = TokenizerText.create()
                    .asciiOnly(true)
                    .errorHandler(errorHandlerExceptions())
                    .source(in)
                    .build();
        return tokenizer;
    }

    private static ByteArrayInputStream bytes(String string) {
        byte b[] = StrUtils.asUTF8bytes(string);
        return new ByteArrayInputStream(b);
    }

    @Test
    public void tokenizer_charset_str_1() {
        Tokenizer tokenizer = tokenizerASCII("'abc'");
        Token t = tokenizer.next();
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void tokenizer_charset_str_2() {
        assertThrows(RiotParseException.class, ()->{
            Tokenizer tokenizer = tokenizerASCII("'abcdé'");
            // ASCII only -> bad.
            Token t = tokenizer.next();
        });
    }

    @Test
    public void tokenizer_charset_str_3() {
        assertThrows(RiotParseException.class, ()->{
            Tokenizer tokenizer = tokenizerASCII("'α'");
            // ASCII only -> bad.
            Token t = tokenizer.next();
        });
    }

    public void tokenizer_charset_uri_1() {
        assertThrows(RiotParseException.class, ()->{
            Tokenizer tokenizer = tokenizerASCII("<http://example/abcdé>");
            // ASCII only -> bad.
            Token t = tokenizer.next();
        });
    }

    public void token_replacementChar_uri_1() {
        Tokenizer tokenizer = tokenizer("<a\uFFFDz>");
        testNextToken(tokenizer, TokenType.IRI);
    }

    public void token_replacementChar_uri_2() {
        Tokenizer tokenizer = tokenizer("<a\\uFFFDz>");
        testNextToken(tokenizer, TokenType.IRI);
    }

    @Test
    public void token_replacementChar_bnode_1() {
		assertThrows(RiotParseException.class, ()->{
        Tokenizer tokenizer = tokenizer("ns\uFFFD:xyz");
        testNextToken(tokenizer, TokenType.PREFIXED_NAME);
        //assertFalse(tokenizer.hasNext());
		});
    }

    @Test
    public void token_replacementChar_bnode_2() {
        assertThrows(RiotParseException.class, () -> {
            Tokenizer tokenizer = tokenizer("ns:\uFFFDbc");
            testNextToken(tokenizer, TokenType.PREFIXED_NAME);
            // assertFalse(tokenizer.hasNext());
        });
    }

    private static final int CountWaringsOnReplacementChar = 0;

    // Test for warnings
    @Test
    public void tokenStr_replacementChar_str_1() {
        testExpectWarning("'\uFFFD'", TokenType.STRING, CountWaringsOnReplacementChar);
    }

    @Test
    public void tokenStr_replacementChar_str_2() {
        // As unicode escape.
        testExpectWarning("'\\uFFFD'", TokenType.STRING, 0);
    }

    @Test
    public void tokenStr_replacementChar_str_3() {
        testExpectWarning("'''\uFFFD'''", TokenType.STRING, CountWaringsOnReplacementChar);
    }

    @Test
    public void tokenStr_replacementChar_str_4() {
        // As unicode escape.
        testExpectWarning("'''\\uFFFD'''", TokenType.STRING, 0);
    }

    @Test
    public void tokenStr_replacementChar_str_5() {
        testExpectWarning("'abc\uFFFDdef'", TokenType.STRING, CountWaringsOnReplacementChar);
    }

    @Test
    public void tokenStr_replacementChar_str_6() {
        // Illegal encoding.
        // 0xDF is ß (lower case) in ISO-8859-1.
        // Here it is an illegal encoding (high set, next byte should have the high bit set but does not).
        // In Unicode it is 0xC39F.

        // This is the quoted string "ß" in ISO-8859-1.
        byte[] bytes = {(byte)0x22, (byte)0xDF, (byte)0x22};
        Reader r = IO.asUTF8(new ByteArrayInputStream(bytes));
        PeekReader pr = PeekReader.make(r);
        Token t = testExpectWarning(pr, TokenType.STRING, CountWaringsOnReplacementChar);
        int char0 = t.getImage().codePointAt(0);
        assertEquals(0xFFFD, char0, ()->"Expected Unicode REPLACEMENT CHARACTER");
    }

    @Test
    public void tokenStr_replacementChar_IRI_1() {
        testExpectWarning("<http://example/\uFFFD>", TokenType.IRI, 0);
    }

    @Test
    public void tokenStr_replacementChar_IRI_2() {
        // As unicode escape. Still bad in a URI.
        testExpectWarning("<http://example/\\uFFFD>", TokenType.IRI, 0);
    }

    @Test
    public void tokenStr_replacementChar_prefixedName_1() {
        testExpectWarning("ex:abc\uFFFD", TokenType.PREFIXED_NAME, 1);
    }

    @Test
    public void tokenStr_replacementChar_prefixedName_2() {
        // Unicode escape
		assertThrows(RiotException.class, ()->
		    testExpectWarning("ex:abc\\uFFFD", TokenType.PREFIXED_NAME, 0)
		        );
    }

    @Test
    public void tokenStr_replacementChar_blankNode_1() {
        testExpectWarning("_:b\uFFFD", TokenType.BNODE, 1);
        // and no escaped characters for blank node labels.
    }

    @Test
    public void tokenIRI_tab() {
        assertThrows(RiotException.class, () -> {
            // Raw tab in a IRI string. Illegal - this is an error.
            Tokenizer tokenizer = tokenizer("<http://example/invalid/iri/with_\t_tab>");
            testNextToken(tokenizer, TokenType.IRI);
        });
    }

    private static Token testExpectWarning(String input, TokenType expectedTokenType, int warningCount) {
        PeekReader r = PeekReader.readString(input);
        return testExpectWarning(r, expectedTokenType, warningCount);
    }

    private static Token testExpectWarning(PeekReader r, TokenType expectedTokenType, int warningCount) {
        ErrorHandlerRecorder errHandler = new ErrorHandlerRecorder(errorHandlerSimple());
        Tokenizer tokenizer = TokenizerText.create().source(r).errorHandler(errHandler).build();
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        if ( expectedTokenType != null )
            assertEquals(expectedTokenType, token.getType());
        assertFalse(tokenizer.hasNext(), ()->"Expected one token");
        assertEquals(warningCount, errHandler.getWarningCount(), ()->"Warnings: ");
        assertEquals(0, errHandler.getErrorCount(), ()->"Errors: ");
        assertEquals(0, errHandler.getFatalCount(), ()->"Fatal: ");
        return token;
    }

    @Test
    public void tokenizer_BOM_1() {
        // BOM
        ByteArrayInputStream in = bytes("\uFEFF'abc'");
        Tokenizer tokenizer = TokenizerText.create().source(in).build();
        assertTrue(tokenizer.hasNext());
        Token token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("abc", token.getImage());
        assertFalse(tokenizer.hasNext());
    }

    // First symbol from the stream.
    private static void testSymbol(String string, TokenType expected) {
        Tokenizer tokenizer = tokenizeAndTestFirst(string, expected, null);
        assertFalse(tokenizer.hasNext());
    }

    // -- Symbols
    // CNTRL
    // @Test public void tokenizer_symbol_01() { testSymbol("*", TokenType.STAR)
    //; }
    @Test
    public void tokenizer_symbol_02() {
        testSymbol("+", TokenType.PLUS);
    }

    @Test
    public void tokenizer_symbol_03() {
        testSymbol("-", TokenType.MINUS);
    }

    // @Test public void tokenizer_symbol_04() { testSymbol("<", TokenType.LT);
    // }
    @Test
    public void tokenizer_symbol_05() {
        testSymbol(">", TokenType.GT);
    }

    @Test
    public void tokenizer_symbol_06() {
        testSymbol("=", TokenType.EQUALS);
    }

//    @Test
//    public void tokenizer_symbol_07() {
//        testSymbol(">=", TokenType.LE);
//    }
//
//    @Test
//    public void tokenizer_symbol_08() {
//        testSymbol("<=", TokenType.GE);
//    }
//
//    @Test
//    public void tokenizer_symbol_09() {
//        testSymbol("&&", TokenType.LOGICAL_AND);
//    }
//
//    @Test
//    public void tokenizer_symbol_10() {
//        testSymbol("||", TokenType.LOGICAL_OR);
//    }

    @Test
    public void tokenizer_symbol_11() {
        testSymbol(" & ", TokenType.AMPERSAND);
    }

    @Test
    public void tokenizer_symbol_12() {
        testSymbol(" | ", TokenType.VBAR);
    }

    @Test
    public void tokenUnit_symbol_11() {
        Tokenizer tokenizer = tokenizeAndTestFirst("+A", TokenType.PLUS, null);

    }

    @Test
    public void tokenUnit_symbol_12() {
        Tokenizer tokenizer = tokenizeAndTestFirst("+-", TokenType.PLUS, null);
        testNextToken(tokenizer, TokenType.MINUS);
    }

    @Test
    public void tokenUnit_symbol_13() {
        testSymbol(".", TokenType.DOT);
    }

    @Test
    public void tokenUnit_symbol_14() {
        Tokenizer tokenizer = tokenizeAndTestFirst(".a", TokenType.DOT, null);
        testNextToken(tokenizer, TokenType.KEYWORD, "a");
    }

    @Test
    public void tokenUnit_symbol_15() {
        Tokenizer tokenizer = tokenizer("| |");
        testNextToken(tokenizer, TokenType.VBAR);
        testNextToken(tokenizer, TokenType.VBAR);
    }

    @Test
    public void tokenUnit_symbol_16() {
        Tokenizer tokenizer = tokenizer("|&/");
        testNextToken(tokenizer, TokenType.VBAR);
        testNextToken(tokenizer, TokenType.AMPERSAND);
        testNextToken(tokenizer, TokenType.SLASH);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void tokenUnit_symbol_17() {
        testSymbol("*", TokenType.STAR);
    }

    @Test
    public void tokenUnit_symbol_18() {
        testSymbol("\\", TokenType.RSLASH);
    }

    @Test
    public void tokenUnit_symbol_19() {
        testSymbol(";", TokenType.SEMICOLON);
    }

    @Test
    public void token_newlines_1() {
        Tokenizer tokenizer = tokenizer("\n", true);
        testNextToken(tokenizer, TokenType.NL);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_newlines_2() {
        Tokenizer tokenizer = tokenizer("abc\ndef", true);
        testNextToken(tokenizer, TokenType.KEYWORD, "abc");
        testNextToken(tokenizer, TokenType.NL);
        testNextToken(tokenizer, TokenType.KEYWORD, "def");
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_newlines_3() {
        Tokenizer tokenizer = tokenizer("abc\n\ndef", true);
        testNextToken(tokenizer, TokenType.KEYWORD, "abc");
        testNextToken(tokenizer, TokenType.NL);
        testNextToken(tokenizer, TokenType.KEYWORD, "def");
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_newlines_4() {
        Tokenizer tokenizer = tokenizer("abc\n\rdef", true);
        testNextToken(tokenizer, TokenType.KEYWORD, "abc");
        testNextToken(tokenizer, TokenType.NL);
        testNextToken(tokenizer, TokenType.KEYWORD, "def");
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_newlines_5() {
        Tokenizer tokenizer = tokenizer("abc\n\n", true);
        testNextToken(tokenizer, TokenType.KEYWORD, "abc");
        testNextToken(tokenizer, TokenType.NL);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_newlines_6() {
        Tokenizer tokenizer = tokenizer("\n \n", true);
        testNextToken(tokenizer, TokenType.NL);
        testNextToken(tokenizer, TokenType.NL);
        assertFalse(tokenizer.hasNext());
    }

    // U+D800-U+DBFF is a high surrogate (first part of a pair)
    // U+DC00-U+DFFF is a low surrogate (second part of a pair)
    // so D800-DC00 is legal.

    @Test public void turtle_surrogate_pair_esc_esc_01() {
        // escaped high, escaped low
        surrogate("'\\ud800\\udc00'");
    }

    @Test public void turtle_surrogate_pair_esc_esc_02() {
        // escaped high, escaped low
        surrogate("'''\\ud800\\udc00'''");
    }

    @Test public void turtle_surrogate_pair_esc_esc_03() {
        // escaped high, escaped low
        surrogate("<\\ud800\\udc00>");
    }

    @Test public void turtle_surrogate_pair_esc_raw_01() {
        // escaped high, raw low
        surrogate("'\\ud800\udc00'");
    }

    @Test public void turtle_surrogate_pair_esc_raw_02() {
        // escaped high, raw low
        surrogate("'''\\ud800\udc00'''");
    }
    @Test public void turtle_surrogate_pair_esc_raw_03() {
        // escaped high, raw low
        surrogate("<\\ud800\udc00>");
    }
    @Test public void turtle_surrogate_pair_esc_raw_04() {
        // escaped high, raw low
        surrogate("_:b\ud800\udc00");
    }

    // Compilation failure - illegal escape character
//    @Test public void turtle_surrogate_pair_raw_esc_01() {
//        // raw high, escaped low
//        surrogate("'\ud800\\udc00'");
//    }

    @Test public void turtle_surrogate_pair_raw_raw_01() {
        // raw high, raw low
        surrogate("'\ud800\udc00'");
    }

    @Test public void turtle_surrogate_pair_raw_raw_02() {
        // raw high, raw low
        surrogate("'''\ud800\udc00'''");
    }

    @Test public void turtle_surrogate_pair_raw_raw_03() {
        // raw high, raw low
        surrogate("<\ud800\udc00>");
    }

    // Blank nodes label allow unicode but not unicode escapes.
    @Test public void turtle_surrogate_pair_raw_raw_04() {
        // raw high, raw low
        surrogate("_:b\ud800\udc00");
    }

    @Test public void turtle_surrogate_pair_raw_raw_05() {
        // escaped high, escaped low
        surrogate("ns:\ud800\udc00");
    }

    @Test public void turtle_surrogate_pair_raw_raw_06() {
        // escaped high, escaped low
        surrogate("\ud800\udc00:local");
    }

    @Test public void turtle_surrogate_pair_esc_esc_internal_01() {
        // escaped high, escaped low
        surrogate("'a\\ud800\\udc00x'");
    }

    @Test public void turtle_surrogate_pair_esc_esc_internal_02() {
        // escaped high, escaped low
        surrogate("'''a\\ud800\\udc00x'''");
    }

    @Test public void turtle_surrogate_pair_esc_esc_internal_03() {
        // escaped high, escaped low
        surrogate("<a\\ud800\\udc00x>");
    }

    @Test public void turtle_surrogate_pair_esc_raw_internal_01() {
        // escaped high, raw low
        surrogate("'z\\ud800\udc00z'");
    }

    @Test public void turtle_surrogate_pair_esc_raw_internal_02() {
        // escaped high, raw low
        surrogate("'''z\\ud800\udc00z'''");
    }

    @Test public void turtle_surrogate_pair_esc_raw_internal_03() {
        // escaped high, raw low
        surrogate("<z\\ud800\udc00z>");
    }

    // Compilation failure - illegal escape character
//    @Test public void turtle_surrogate_pair_raw_esc() {
//        // raw high, escaped low
//        surrogate("'a\ud800\\udc00'z");
//    }

    @Test public void turtle_surrogate_pair_raw_raw_internal_01() {
        // raw high, raw low
        surrogate("'a\ud800\udc00z'");
    }

    @Test public void turtle_surrogate_pair_raw_raw_internal_02() {
        // raw high, raw low
        surrogate("'''a\ud800\udc00z'''");
    }

    @Test public void turtle_surrogate_pair_raw_raw_internal_03() {
        // raw high, raw low
        surrogate("<a\ud800\udc00z>");
    }

    @Test public void turtle_surrogate_pair_raw_raw_internal_04() {
        // raw high, raw low
        surrogate("_:ba\ud800\udc00z");
    }

    @Test public void turtle_surrogate_pair_raw_raw__internal05() {
        // escaped high, escaped low
        surrogate("ns:x\ud800\udc00y");
    }

    @Test public void turtle_surrogate_pair_raw_raw__internal06() {
        // escaped high, escaped low
        surrogate("x\ud800\udc00y:local");
    }

    @Test
    public void turtle_bad_surrogate_01() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'\\ud800'"));
    }

    @Test
    public void turtle_bad_surrogate_01a() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'''\\ud800''''"));
    }

    @Test
    public void turtle_bad_surrogate_02() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'a\\ud800z'"));
    }

    @Test
    public void turtle_bad_surrogate_02a() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'''a\\ud800z'''"));
    }

    @Test
    public void turtle_bad_surrogate_02b() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("<a\\ud800z>"));
    }

    @Test
    public void turtle_bad_surrogate_03() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'\\udfff'"));
    }

    @Test
    public void turtle_bad_surrogate_04() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'a\\udfffz'"));
    }

    @Test
    public void turtle_bad_surrogate_05() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'\\U0000d800'"));
    }

    @Test
    public void turtle_bad_surrogate_06() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'a\\U0000d800z'"));
    }

    @Test
    public void turtle_bad_surrogate_07() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'\\U0000dfff'"));
    }

    @Test
    public void turtle_bad_surrogate_08() {
		assertThrows(RiotParseException.class, ()->
					 surrogate("'a\\U0000dfffz'"));
    }

    @Test
    public void turtle_bad_surrogate_09() {
        // Wrong way round: low-hig);
		assertThrows(RiotParseException.class, ()->
					 surrogate("'\\uc800\\ud800'")
					 );
    }

    @Test
    public void turtle_bad_surrogate_10() {
		assertThrows(RiotParseException.class, ()->
        // Wrong way round: low-high
        surrogate("'a\\uc800\\ud800z'")
					 );
    }

    // Compilation failure - illegal escape character
//    @Test
//     public void turtle_bad_surrogate_11() {
//      // raw low - escaped high
// 		assertThrows(RiotParseException.class, ()->
// 					 surrogate("'\ud800\\ud800'"));
//     }
//
//     @Test
//     public void turtle_bad_surrogate_12() {
//      // raw low - escaped high
// 		assertThrows(RiotParseException.class, ()->
// 					 surrogate("'a\ud800\\ud800z'"));
//     }

    @Test
    public void turtle_bad_surrogate_13() {
        // escaped low - raw high
		assertThrows(RiotParseException.class, ()->
        surrogate("'\\uc800\ud800'")
					 );
    }

    @Test
    public void turtle_bad_surrogate_14() {
        // escaped low - raw high
		assertThrows(RiotParseException.class, ()->
        surrogate("'a\\uc800\ud800z'")
					 );
    }

    private void surrogate(String string) {
        Tokenizer tokenizer = tokenizer(string);
        assertTrue(tokenizer.hasNext());
        tokenizer.next();
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_reified_1() {
        Tokenizer tokenizer = tokenizer("<<");
        testNextToken(tokenizer, TokenType.LT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_reified_2() {
        Tokenizer tokenizer = tokenizer("<<");
        testNextToken(tokenizer, TokenType.LT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_reified_3() {
        Tokenizer tokenizer = tokenizer("<<>>");
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_reified_4() {
        Tokenizer tokenizer = tokenizer("<< >>");
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }

    // LT not active.
//    @Test
//    public void token_rdf_star_reified_5() {
//        Tokenizer tokenizer = tokenizer("< <> >");
//        testNextToken(tokenizer, TokenType.LT);
//        testNextToken(tokenizer, TokenType.IRI);
//        testNextToken(tokenizer, TokenType.GT);
//        assertFalse(tokenizer.hasNext());
//    }

    @Test
    public void token_rdf_star_reified_6() {
        Tokenizer tokenizer = tokenizer("<< <> >>");
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.IRI);
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_reified_7() {
        Tokenizer tokenizer = tokenizer("<< <>>>");
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.IRI);
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_tripleTerm_1() {
        Tokenizer tokenizer = tokenizer("<<(");
        testNextToken(tokenizer, TokenType.L_TRIPLE);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_tripleTerm_2() {
        Tokenizer tokenizer = tokenizer(")>>");
        testNextToken(tokenizer, TokenType.R_TRIPLE);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_tripleTerm_3() {
        Tokenizer tokenizer = tokenizer("<<( )>>");
        testNextToken(tokenizer, TokenType.L_TRIPLE);
        testNextToken(tokenizer, TokenType.R_TRIPLE);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_tripleTerm_4() {
        Tokenizer tokenizer = tokenizer("<<()>>");
        testNextToken(tokenizer, TokenType.L_TRIPLE);
        testNextToken(tokenizer, TokenType.R_TRIPLE);
        assertFalse(tokenizer.hasNext());
    }

    // Not triple term syntax
    @Test
    public void token_rdf_star_tripleTerm_5() {
        Tokenizer tokenizer = tokenizer("<< () >>");
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.LPAREN);
        testNextToken(tokenizer, TokenType.RPAREN);
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_tripleTerm_6() {
        Tokenizer tokenizer = tokenizer(")>> .");
        testNextToken(tokenizer, TokenType.R_TRIPLE);
        testNextToken(tokenizer, TokenType.DOT);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_tripleTerm_7() {
        Tokenizer tokenizer = tokenizer("().");
        testNextToken(tokenizer, TokenType.LPAREN);
        testNextToken(tokenizer, TokenType.RPAREN);
        testNextToken(tokenizer, TokenType.DOT);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_occurence1() {
        Tokenizer tokenizer = tokenizer("<<:s x:p 123>> :q ");
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "", "s");
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "x", "p");
        testNextToken(tokenizer, TokenType.INTEGER, "123", null);
        testNextToken(tokenizer, TokenType.GT2);
        testNextToken(tokenizer, TokenType.PREFIXED_NAME, "", "q");
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_inner_IRI() {
        Tokenizer tokenizer = tokenizer("<<<>>>");         // which is <<  <>  >>
        testNextToken(tokenizer, TokenType.LT2);
        Token t = testNextToken(tokenizer, TokenType.IRI);
        assertEquals("", t.getImage());
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }

    @Test
    public void token_rdf_star_inner_triple_term() {
        Tokenizer tokenizer = tokenizer("<<<<()>>>>");     // which is << <<( )>>  >>
        testNextToken(tokenizer, TokenType.LT2);
        testNextToken(tokenizer, TokenType.L_TRIPLE);
        testNextToken(tokenizer, TokenType.R_TRIPLE);
        testNextToken(tokenizer, TokenType.GT2);
        assertFalse(tokenizer.hasNext());
    }
}
