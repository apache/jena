/******************************************************************
 * File:        TestTokenizer.java
 * Created by:  Dave Reynolds
 * Created on:  24-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestTokenizer.java,v 1.4 2005-02-21 12:19:23 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.util.test;

import com.hp.hpl.jena.util.Tokenizer;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test for the trivial tokenizer utility.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2005-02-21 12:19:23 $
 */
public class TestTokenizer extends TestCase {
         
    /**
     * Boilerplate for junit
     */ 
    public TestTokenizer( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestTokenizer.class ); 
    }
    
    /**
     * Test tokenizer on a basic example or two.
     */
    public void testTokenizer() {
        Tokenizer tokenizer = new Tokenizer("a(foo,bar)  'i am a literal' so there", "()[], \t\n\r'", "'", true);
        assertEquals(tokenizer.nextToken(), "a");
        assertEquals(tokenizer.nextToken(), "(");
        assertEquals(tokenizer.nextToken(), "foo");
        assertEquals(tokenizer.nextToken(), ",");
        assertEquals(tokenizer.nextToken(), "bar");
        assertEquals(tokenizer.nextToken(), ")");
        assertEquals(tokenizer.nextToken(), " ");
        assertEquals(tokenizer.nextToken(), " ");
        assertEquals(tokenizer.nextToken(), "'");
        assertEquals(tokenizer.nextToken(), "i am a literal");
        assertEquals(tokenizer.nextToken(), "'");
        assertEquals(tokenizer.nextToken(), " ");
        assertEquals(tokenizer.nextToken(), "so");
        assertEquals(tokenizer.nextToken(), " ");
        assertEquals(tokenizer.nextToken(), "there");
        assertTrue( ! tokenizer.hasMoreTokens());
          
        tokenizer = new Tokenizer("a(foo,bar)  'i am a literal' so there", "()[], \t\n\r'", "'", false);
        assertEquals(tokenizer.nextToken(), "a");
        assertEquals(tokenizer.nextToken(), "foo");
        assertEquals(tokenizer.nextToken(), "bar");
        assertEquals(tokenizer.nextToken(), "i am a literal");
        assertEquals(tokenizer.nextToken(), "so");
        assertEquals(tokenizer.nextToken(), "there");
        assertTrue( ! tokenizer.hasMoreTokens());
          
    }
    

}


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/