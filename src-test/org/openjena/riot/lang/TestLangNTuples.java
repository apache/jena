/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.ErrorHandlerTestLib.* ;

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
    
    protected abstract long parseCount(String... strings) ;
    
    protected abstract void parseCheck(String... strings) ;
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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