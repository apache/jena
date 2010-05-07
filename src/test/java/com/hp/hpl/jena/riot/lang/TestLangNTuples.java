/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.riot.ParseException ;


abstract public class TestLangNTuples extends BaseTest
{
    // Test streaming interface.
    
    @Test public void nt0()
    {
        long count = parseToSink("") ;
        assertEquals(0, count) ;
    }
    
    @Test public void nt1()
    {
        long count = parseToSink("<x> <y> <z>.") ;
        assertEquals(1, count) ;
    }
    
    @Test public void nt2()
    {
        long count = parseToSink("<x> <y> \"z\".") ;
        assertEquals(1, count) ;
    }
    
    @Test public void nt3()
    {
        long count = parseToSink("<x> <y> <z>. <x> <y> <z>.") ;
        assertEquals(2, count) ;
    }

    @Test public void nt4()
    {
        long count = parseToSink("<x> <y> \"123\"^^<int>.") ;
        assertEquals(1, count) ;
    }

    @Test public void nt5()
    {
        long count = parseToSink("<x> <y> \"123\"@lang.") ;
        assertEquals(1,count) ;
    }
    
    // Test iterator interface.

    // Test parse errors interface.
    @Test(expected=ParseException.class)
    public void nt_bad_01()
    {
        parseToSink("<x> <y> <z>") ;          // No DOT
    }
    
    @Test(expected=ParseException.class)
    public void nt_bad_02()
    {
        parseToSink("<x> _:a <z> .") ;        // Bad predicate
    }

    @Test(expected=ParseException.class)
    public void nt_bad_03()
    {
        parseToSink("<x> \"p\" <z> .") ;      // Bad predicate 
    }

    @Test(expected=ParseException.class)
    public void nt_bad_4()
    {
        parseToSink("\"x\" <p> <z> .") ;      // Bad subject
    }

    @Test(expected=ParseException.class)
    public void nt_bad_5()
    {
        parseToSink("<x> <p> ?var .") ;        // No variables 
    }
    
    @Test(expected=ParseException.class)
    public void nt_bad_6()
    {
        parseToSink("<x> <p> 123 .") ;        // No abbreviations. 
    }
    
    @Test(expected=ParseException.class)
    public void nt_bad_7()
    {
        parseToSink("<x> <p> x:y .") ;        // No prefixed names 
    }

    protected abstract long parseToSink(String string) ;
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