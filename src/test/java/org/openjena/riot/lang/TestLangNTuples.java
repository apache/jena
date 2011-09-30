/**
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

package org.openjena.riot.lang;

import java.io.ByteArrayInputStream ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.ErrorHandlerTestLib.ExError ;
import org.openjena.riot.ErrorHandlerTestLib.ExFatal ;
import org.openjena.riot.ErrorHandlerTestLib.ExWarning ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

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
    
    @Test(expected=RiotException.class) 
    public void tuple_charset_2()
    {
        parseCheck("<http://example/é> <http://example/p> \"é\" .") ; 
    }
    
    static protected Tokenizer tokenizer(String string)
    {
        //Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        // ASCII
        byte b[] = StrUtils.asUTF8bytes(string) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(in) ;
        return tokenizer ;
    }
    
    
    protected abstract long parseCount(String... strings) ;
    
    protected abstract void parseCheck(String... strings) ;
}
