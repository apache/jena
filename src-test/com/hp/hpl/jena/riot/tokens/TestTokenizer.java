/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.tokens ;

import com.hp.hpl.jena.riot.ParseException;
import com.hp.hpl.jena.riot.tokens.Token;
import com.hp.hpl.jena.riot.tokens.TokenType;
import com.hp.hpl.jena.riot.tokens.Tokenizer;
import com.hp.hpl.jena.riot.tokens.TokenizerText;

import atlas.io.PeekReader;
import org.junit.Test ;
import atlas.test.BaseTest ;


public class TestTokenizer extends BaseTest
{
    private static Tokenizer tokenizer(String string)
    {
        PeekReader r = PeekReader.readString(string) ;
        Tokenizer tokenizer = new TokenizerText(r) ;
        return tokenizer ;
    }

    @Test
    public void tokenUnit_iri1()
    {
        Tokenizer tokenizer = tokenizer("<x>") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.IRI, token.getType()) ;
        assertEquals("x", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_iri2()
    {
        Tokenizer tokenizer = tokenizer("   <>   ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.IRI, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_iri3()
    {
        try {
            Tokenizer tokenizer = tokenizer("   <abc\\>   123") ;
        } catch (ParseException ex)
        {
            String x = ex.getMessage() ;
            assertTrue("illegal escape sequence value: >".equalsIgnoreCase(x)) ;
        }
    }
    
    
    @Test
    public void tokenUnit_str1()
    {
        Tokenizer tokenizer = tokenizer("   'abc'   ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("abc", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str2()
    {
        Tokenizer tokenizer = tokenizer("''") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str3()
    {
        Tokenizer tokenizer = tokenizer("'\\u0020'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals(" ", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str4()
    {
        Tokenizer tokenizer = tokenizer("'a\\'\\\"\\n\\t\\r'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("a'\"\n\t\r", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test(expected = ParseException.class)
    public void tokenUnit_str5()
    {
        Tokenizer tokenizer = tokenizer("'\n'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test
    public void tokenUnit_str6()
    {
        Tokenizer tokenizer = tokenizer("   \"abc\"   ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING2, token.getType()) ;
        assertEquals("abc", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str7()
    {
        Tokenizer tokenizer = tokenizer("\"\"") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING2, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test(expected = ParseException.class)
    public void tokenUnit_str8()
    {
        Tokenizer tokenizer = tokenizer("\"") ;
        assertTrue(tokenizer.hasNext()) ;
    }

    @Test(expected = ParseException.class)
    public void tokenUnit_str9()
    {
        Tokenizer tokenizer = tokenizer("'abc") ;
        assertTrue(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str10()
    {
        Tokenizer tokenizer = tokenizer("'\\'abc'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("'abc", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str11()
    {
        Tokenizer tokenizer = tokenizer("'\\U00000020'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals(" ", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    

    @Test
    public void tokenUnit_str_long1()
    {
        Tokenizer tokenizer = tokenizer("'''aaa'''") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING1, token.getType()) ;
        assertEquals("aaa", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str_long2()
    {
        Tokenizer tokenizer = tokenizer("\"\"\"aaa\"\"\"") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING2, token.getType()) ;
        assertEquals("aaa", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str_long3()
    {
        Tokenizer tokenizer = tokenizer("''''1234'''") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING1, token.getType()) ;
        assertEquals("'1234", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str_long4()
    {
        Tokenizer tokenizer = tokenizer("'''''1234'''") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING1, token.getType()) ;
        assertEquals("''1234", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str_long5()
    {
        Tokenizer tokenizer = tokenizer("'''\\'''1234'''") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING1, token.getType()) ;
        assertEquals("'''1234", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str_long6()
    {
        Tokenizer tokenizer = tokenizer("\"\"\"\"1234\"\"\"") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING2, token.getType()) ;
        assertEquals("\"1234", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str_long7()
    {
        Tokenizer tokenizer = tokenizer("\"\"\"\"\"1234\"\"\"") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING2, token.getType()) ;
        assertEquals("\"\"1234", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_str_long8()
    {
        Tokenizer tokenizer = tokenizer("''''''") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING1, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_str_long9()
    {
        Tokenizer tokenizer = tokenizer("\"\"\"'''''''''''''''''\"\"\"") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LONG_STRING2, token.getType()) ;
        assertEquals("'''''''''''''''''", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test(expected = ParseException.class)
    public void tokenUnit_str_long10()
    {
        Tokenizer tokenizer = tokenizer("\"\"\"abcdef") ;
        assertTrue(tokenizer.hasNext()) ;
    }
    
    @Test(expected = ParseException.class)
    public void tokenUnit_str_long11()
    {
        Tokenizer tokenizer = tokenizer("'''") ;
        assertTrue(tokenizer.hasNext()) ;
    }

    public void tokenUnit_str_long12()
    {
        Tokenizer tokenizer = tokenizer("'''x'''@en") ;
        assertTrue(tokenizer.hasNext()) ;
    }

    public void tokenUnit_str_long13()
    {
        Tokenizer tokenizer = tokenizer("'''123'''^^<xyz>") ;
        assertTrue(tokenizer.hasNext()) ;
    }

    
    @Test
    public void tokenUnit_bNode1()
    {
        Tokenizer tokenizer = tokenizer("_:abc") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.BNODE, token.getType()) ;
        assertEquals("abc", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_bNode2()
    {
        Tokenizer tokenizer = tokenizer("_:123 ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.BNODE, token.getType()) ;
        assertEquals("123", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test(expected = ParseException.class)
    public void tokenUnit_bNode3()
    {
        Tokenizer tokenizer = tokenizer("_:") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test
    public void tokenUnit_bNode4()
    {
        Tokenizer tokenizer = tokenizer("_:1-2-Z ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.BNODE, token.getType()) ;
        assertEquals("1-2-Z", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_bNode5()
    {
        Tokenizer tokenizer = tokenizer("*S") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.CNTRL, token.getType()) ;
        assertEquals('S', token.getCntrlCode()) ;
        assertNull(token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_cntrl1()
    {
        Tokenizer tokenizer = tokenizer("*SXYZ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.CNTRL, token.getType()) ;
        assertEquals('S', token.getCntrlCode()) ;
        assertNull(token.getImage()) ;
        assertNull(token.getImage2()) ;
    }

    @Test
    public void tokenUnit_cntrl2()
    {
        Tokenizer tokenizer = tokenizer("*S<x>") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.CNTRL, token.getType()) ;
        assertEquals('S', token.getCntrlCode()) ;
        assertNull(token.getImage()) ;
        assertNull(token.getImage2()) ;

        assertTrue(tokenizer.hasNext()) ;
        Token token2 = tokenizer.next() ;
        assertNotNull(token2) ;
        assertEquals(TokenType.IRI, token2.getType()) ;
        assertEquals("x", token2.getImage()) ;
        assertNull(token2.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenUnit_syntax1()
    {
        Tokenizer tokenizer = tokenizer(".") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.DOT, token.getType()) ;
        assertNull(token.getImage()) ;
        assertNull(token.getImage2()) ;
    }

    @Test
    public void tokenUnit_syntax2()
    {
        Tokenizer tokenizer = tokenizer(".;,") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.DOT, token.getType()) ;
        assertNull(token.getImage()) ;
        assertNull(token.getImage2()) ;

        assertTrue(tokenizer.hasNext()) ;
        Token token2 = tokenizer.next() ;
        assertNotNull(token2) ;
        assertEquals(TokenType.SEMICOLON, token2.getType()) ;
        assertEquals(";", token2.getImage()) ;
        assertNull(token2.getImage2()) ;

        assertTrue(tokenizer.hasNext()) ;
        Token token3 = tokenizer.next() ;
        assertNotNull(token3) ;
        assertEquals(TokenType.COMMA, token3.getType()) ;
        assertEquals(",", token3.getImage()) ;
        assertNull(token3.getImage2()) ;
    }

    @Test
    public void tokenUnit_pname1()
    {
        Tokenizer tokenizer = tokenizer("a:b.c") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "a", "b.c") ;
    }
    
    @Test
    public void tokenUnit_pname2()
    {
        Tokenizer tokenizer = tokenizer("a:b.") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "a", "b") ;
    }

    @Test
    public void tokenUnit_pname3()
    {
        Tokenizer tokenizer = tokenizer("a:b123") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "a", "b123") ;
    }

    @Test
    public void tokenUnit_pname4()
    {
        Tokenizer tokenizer = tokenizer("a:") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "a", "") ;
    }

    @Test
    public void tokenUnit_pname5()
    {
        Tokenizer tokenizer = tokenizer(":") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "", "") ;
    }

    @Test
    public void tokenUnit_pname6()
    {
        Tokenizer tokenizer = tokenizer(":a") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "", "a") ;
    }
    
    @Test
    public void tokenUnit_pname7()
    {
        Tokenizer tokenizer = tokenizer(":123") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "", "123") ;
    }

    @Test
    public void tokenUnit_pname8()
    {
        Tokenizer tokenizer = tokenizer("a123:456") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "a123", "456") ;
    }

    @Test
    public void tokenUnit_pname9()
    {
        Tokenizer tokenizer = tokenizer("a123:-456") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        pnameToken(token, "a123", "") ;
        
        token = tokenizer.next() ;
        assertEquals(TokenType.INTEGER, token.getType()) ;
        assertEquals("-456", token.getImage()) ;
        
    }


private void pnameToken(Token token, String string1, String string2)
    {
        assertEquals(TokenType.PREFIXED_NAME, token.getType()) ;
        assertEquals(string1, token.getImage()) ;
        assertEquals(string2, token.getImage2()) ;
    }

//    @Test(expected = ParseException.class)
//    public void tokenUnit_25()
//    {
//        Tokenizer tokenizer = tokenizer("123:") ;
//        assertTrue(tokenizer.hasNext()) ;
//        Token token = tokenizer.next() ;
//        pnameToken(token, "123", "") ;
//    }

    @Test public void tokenUnit_num1()
    {
        Tokenizer tokenizer = tokenizer("123") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.INTEGER, token.getType()) ;
        assertEquals("123", token.getImage()) ;
    }
    
    @Test public void tokenUnit_num2()
    {
        Tokenizer tokenizer = tokenizer("123.") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DECIMAL, token.getType()) ;
        assertEquals("123.", token.getImage()) ;
    }

    @Test public void tokenUnit_num3()
    {
        Tokenizer tokenizer = tokenizer("+123.456") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DECIMAL, token.getType()) ;
        assertEquals("+123.456", token.getImage()) ;
    }
    
    @Test public void tokenUnit_num4()
    {
        Tokenizer tokenizer = tokenizer("-1") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.INTEGER, token.getType()) ;
        assertEquals("-1", token.getImage()) ;
    }
    
    @Test public void tokenUnit_num5()
    {
        Tokenizer tokenizer = tokenizer("-1e0") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOUBLE, token.getType()) ;
        assertEquals("-1e0", token.getImage()) ;
    }
    
    @Test public void tokenUnit_num6()
    {
        Tokenizer tokenizer = tokenizer("1e+1") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOUBLE, token.getType()) ;
        assertEquals("1e+1", token.getImage()) ;
    }
    
    @Test public void tokenUnit_num7()
    {
        Tokenizer tokenizer = tokenizer("1.3e+1") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOUBLE, token.getType()) ;
        assertEquals("1.3e+1", token.getImage()) ;
    }
    
    // Bad numbers.
    
    @Test
    public void tokenUnit_num8()
    {
        Tokenizer tokenizer = tokenizer("1.3.4") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DECIMAL, token.getType()) ;
        assertEquals("1.3", token.getImage()) ;
    }

    @Test
    public void tokenUnit_num9()
    {
        Tokenizer tokenizer = tokenizer("1.3e67.7") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOUBLE, token.getType()) ;
        assertEquals("1.3e67", token.getImage()) ;
    }

    @Test(expected = ParseException.class)
    public void tokenUnit_num10()
    {
        Tokenizer tokenizer = tokenizer("+") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.KEYWORD, token.getType()) ;
    }
    
    @Test(expected = ParseException.class)
    public void tokenUnit_num11()
    {
        Tokenizer tokenizer = tokenizer("+-") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.KEYWORD, token.getType()) ;
    }
    
    @Test
    public void tokenUnit_num12()
    {
        // Not a number.
        Tokenizer tokenizer = tokenizer(".") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOT, token.getType()) ;
    }

    @Test
    public void tokenUnit_num13()
    {
        // Not a number.
        Tokenizer tokenizer = tokenizer(".a") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOT, token.getType()) ;
    }
    
    @Test
    public void tokenUnit_num14()
    {
        Tokenizer tokenizer = tokenizer(".1") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DECIMAL, token.getType()) ;
        assertEquals(".1", token.getImage()) ;
    }

    @Test
    public void tokenUnit_num15()
    {
        Tokenizer tokenizer = tokenizer(".1e0") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.DOUBLE, token.getType()) ;
        assertEquals(".1e0", token.getImage()) ;
    }

    @Test
    public void tokenUnit_num16()
    {
        // This is not a hex number.
        Tokenizer tokenizer = tokenizer("000A     .") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.INTEGER, token.getType()) ;
        assertEquals("000", token.getImage()) ;
        Token token2 = tokenizer.next() ;
        assertEquals(TokenType.KEYWORD, token2.getType()) ;
        assertEquals("A", token2.getImage()) ;
    }

    @Test
    public void tokenUnit_var1()
    {
        Tokenizer tokenizer = tokenizer("?x ?y") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.VAR, token.getType()) ;
        assertEquals("x", token.getImage()) ;
    }
    
    @Test
    public void tokenUnit_var2()
    {
        Tokenizer tokenizer = tokenizer("? x") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.VAR, token.getType()) ;
        assertEquals("", token.getImage()) ;
    }

    @Test
    public void tokenUnit_hex1()
    {
        Tokenizer tokenizer = tokenizer("0xABC") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.HEX, token.getType()) ;
        assertEquals("0xABC", token.getImage()) ;
    }
        
    @Test
    public void tokenUnit_hex2()
    {
        Tokenizer tokenizer = tokenizer("0xABCXYZ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.HEX, token.getType()) ;
        assertEquals("0xABC", token.getImage()) ;
    }
    
    @Test(expected = ParseException.class)
    public void tokenUnit_hex3()
    {
        Tokenizer tokenizer = tokenizer("0xXYZ") ;
        assertTrue(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenUnit_hex4()
    {
        Tokenizer tokenizer = tokenizer("0Xabc") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertEquals(TokenType.HEX, token.getType()) ;
        assertEquals("0Xabc", token.getImage()) ;
    }
    
    @Test
    public void tokenLiteralDT_0()
    {
        Tokenizer tokenizer = tokenizer("'123'^^<x> ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LITERAL_DT, token.getType()) ;
        assertEquals("123", token.getImage()) ;
        
        Token token2 = token.getSubToken() ; 
        assertEquals(TokenType.IRI, token2.getType()) ;
        assertEquals("x", token2.getImage()) ;
        
        assertFalse(tokenizer.hasNext()) ;
    }
    
    @Test
    public void tokenLiteralDT_1()
    {
        Tokenizer tokenizer = tokenizer("'123'^^x:y") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LITERAL_DT, token.getType()) ;
        assertEquals("123", token.getImage()) ;
        
        Token token2 = token.getSubToken() ; 
        assertEquals(TokenType.PREFIXED_NAME, token2.getType()) ;
        assertEquals("x", token2.getImage()) ;
        assertEquals("y", token2.getImage2()) ;
        
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test(expected = ParseException.class)
    public void tokenLiteralDT_2()
    {
        Tokenizer tokenizer = tokenizer("'123'^^ <x> ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test(expected = ParseException.class)
    public void tokenLiteralDT_3()
    {
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

    @Test(expected = ParseException.class)
    public void tokenLiteralDT_4()
    {
        Tokenizer tokenizer = tokenizer("'123'^ ^<x> ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test(expected = ParseException.class)
    public void tokenLiteralDT_5()
    {
        Tokenizer tokenizer = tokenizer("'123'^^ x:y") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }


    @Test
    public void tokenLiteralLang_0()
    {
        Tokenizer tokenizer = tokenizer("'a'@en") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LITERAL_LANG, token.getType()) ;
        assertEquals("a", token.getImage()) ;
        assertEquals("en", token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenLiteralLang_1()
    {
        Tokenizer tokenizer = tokenizer("'a'@en-UK ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LITERAL_LANG, token.getType()) ;
        assertEquals("a", token.getImage()) ;
        assertEquals("en-UK", token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test public void tokenLiteralLang_2()
    {
        Tokenizer tokenizer = tokenizer("'' @lang ") ;
        
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertTrue(tokenizer.hasNext()) ;
        
        Token token2 = tokenizer.next() ;
        assertEquals(TokenType.DIRECTIVE, token2.getType()) ;
        assertEquals("lang", token2.getImage()) ;
        assertNotNull(token2) ;
    }

    @Test(expected = ParseException.class)
    public void tokenLiteralLang_3()
    {
        Tokenizer tokenizer = tokenizer("''@ lang ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test(expected = ParseException.class)
    public void tokenLiteralLang_4()
    {
        Tokenizer tokenizer = tokenizer("''@lang- ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test(expected = ParseException.class)
    public void tokenLiteralLang_5()
    {
        Tokenizer tokenizer = tokenizer("''@- ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test
    public void tokenLiteralLang_6()
    {
        Tokenizer tokenizer = tokenizer("''@a-b-c ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LITERAL_LANG, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertEquals("a-b-c", token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;

    }

    @Test
    public void tokenLiteralLang_7()
    {
        Tokenizer tokenizer = tokenizer("''@a-b9z-c99 ") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.LITERAL_LANG, token.getType()) ;
        assertEquals("", token.getImage()) ;
        assertEquals("a-b9z-c99", token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;

    }

    @Test(expected = ParseException.class)
    public void tokenLiteralLang_8()
    {
        Tokenizer tokenizer = tokenizer("''@9-b") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
    }

    @Test
    public void tokenComment_01()
    {
        Tokenizer tokenizer = tokenizer("_:123 # Comment") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.BNODE, token.getType()) ;
        assertEquals("123", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenComment_02()
    {
        Tokenizer tokenizer = tokenizer("'foo # Non-Comment'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("foo # Non-Comment", token.getImage()) ;
        assertNull(token.getImage2()) ;
        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenComment_03()
    {
        Tokenizer tokenizer = tokenizer("'foo' # Comment\n'bar'") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.STRING1, token.getType()) ;
        assertEquals("foo", token.getImage()) ;
        assertNull(token.getImage2()) ;

        assertTrue(tokenizer.hasNext()) ;
        Token token2 = tokenizer.next() ;
        assertNotNull(token2) ;
        assertEquals(TokenType.STRING1, token2.getType()) ;
        assertEquals("bar", token2.getImage()) ;
        assertNull(token2.getImage2()) ;

        assertFalse(tokenizer.hasNext()) ;
    }

    @Test
    public void tokenWord_01()
    {
        Tokenizer tokenizer = tokenizer("abc") ;
        assertTrue(tokenizer.hasNext()) ;
        Token token = tokenizer.next() ;
        assertNotNull(token) ;
        assertEquals(TokenType.KEYWORD, token.getType()) ;
    }
    
    // Multiple terms

    @Test
    public void token_50()
    {
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

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */