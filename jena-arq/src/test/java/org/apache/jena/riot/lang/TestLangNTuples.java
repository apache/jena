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

package org.apache.jena.riot.lang;

import java.io.ByteArrayInputStream ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx;
import org.apache.jena.riot.ErrorHandlerTestLib.ExError;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.out.CharSpace;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.junit.Test ;

/** Test of syntax by a tuples parser (does not include node validitiy checking) */ 

abstract public class TestLangNTuples extends BaseTest
{
    // Test streaming interface.
    
    @Test public void tuple_0()
    {
        long count = parseCount("") ;
        assertEquals(0, count) ;
    }
    
    @Test public void tuple_1()
    {
        long count = parseCount("<x> <y> <z>.") ;
        assertEquals(1, count) ;
    }
    
    @Test public void tuple_2()
    {
        long count = parseCount("<x> <y> \"z\".") ;
        assertEquals(1, count) ;
    }
    
    @Test public void tuple_3()
    {
        long count = parseCount("<x> <y> <z>. <x> <y> <z>.") ;
        assertEquals(2, count) ;
    }

    @Test public void tuple_4()
    {
        long count = parseCount("<x> <y> \"123\"^^<int>.") ;
        assertEquals(1, count) ;
    }

    @Test public void tuple_5()
    {
        long count = parseCount("<x> <y> \"123\"@lang.") ;
        assertEquals(1,count) ;
    }
    
    // Test iterator interface.

    // Test parse errors interface.
    @Test(expected=ExFatal.class)
    public void tuple_bad_01()
    {
        parseCount("<x> <y> <z>") ;          // No DOT
    }
    
    @Test(expected=ExFatal.class)
    public void tuple_bad_02()
    {
        parseCount("<x> _:a <z> .") ;        // Bad predicate
    }

    @Test(expected=ExFatal.class)
    public void tuple_bad_03()
    {
        parseCount("<x> \"p\" <z> .") ;      // Bad predicate 
    }

    @Test(expected=ExFatal.class)
    public void tuple_bad_4()
    {
        parseCount("\"x\" <p> <z> .") ;      // Bad subject
    }

    @Test(expected=ExFatal.class)
    public void tuple_bad_5()
    {
        parseCount("<x> <p> ?var .") ;        // No variables 
    }
    
    @Test(expected=ExFatal.class)
    public void tuple_bad_6()
    {
        parseCount("<x> <p> 123 .") ;        // No abbreviations. 
    }
    
    @Test(expected=ExFatal.class)
    public void tuple_bad_7()
    {
        parseCount("<x> <p> x:y .") ;        // No prefixed names 
    }
    
    // Bad terms - but accepted by default.
    @Test 
    public void tuple_bad_10()       { parseCount("<x> <p> <bad uri> .") ; } 

    // Bad terms - but accepted by default.
    @Test 
    public void tuple_bad_11()       { parseCount("<x> <p> \"9000\"^^<http://www.w3.org/2001/XMLSchema#byte> .") ; } 

    // Bad terms - but accepted by default.
    @Test (expected=ExError.class)
    public void tuple_bad_21()       { parseCheck("<x> <p> <z> .") ; } 

    // Bad terms - with checking.
    @Test (expected=ExWarning.class)
    public void tuple_bad_22()       { parseCheck("<http://example/x> <http://example/p> <http://example/bad uri> .") ; } 

    @Test  (expected=ExWarning.class)
    public void tuple_bad_23()       { parseCheck("<http://example/x> <http://example/p> \"9000\"^^<http://www.w3.org/2001/XMLSchema#byte> .") ; } 
    
    // ASCII vs UTF-8
    @Test
    public void tuple_charset_1()
    {
        // E9 is e-acute
        parseCheck("<http://example/x\\u00E9> <http://example/p> <http://example/s> .") ; 
    }
    
    @Test
    public void tuple_charset_2()
    {
        parseCheck("<http://example/é> <http://example/p> \"é\" .") ; 
    }
    
    static protected Tokenizer tokenizer(CharSpace charSpace, String string)
    {
        byte b[] = StrUtils.asUTF8bytes(string) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        Tokenizer tokenizer = charSpace == CharSpace.ASCII ? TokenizerFactory.makeTokenizerASCII(in) : TokenizerFactory.makeTokenizerUTF8(in) ;
        return tokenizer ;
    }
    
    static protected Tokenizer tokenizer(String string)
    {
        // UTF-8
        byte b[] = StrUtils.asUTF8bytes(string) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in) ;
        return tokenizer ;
    }
    
    protected void parse(StreamRDF sink, String... strings ) 
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(string) ;
        LangRIOT parser = createParser(tokenizer(string), sink) ;
        parser.getProfile().setHandler(new ErrorHandlerEx()) ;
        parser.parse() ;
    }
    
    protected abstract LangRIOT createParser(Tokenizer tokenizer, StreamRDF sink) ;

    final protected void parseCheck(String... strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(string) ;
        StreamRDFCounting sink = StreamRDFLib.count() ;
        LangRIOT x = RiotReader.createParserNQuads(tokenizer, sink) ;
        x.setProfile(RiotLib.profile(null, false, true, new ErrorHandlerEx())) ;
        x.parse() ;
    }

    
    protected abstract long parseCount(String... strings) ;
}
