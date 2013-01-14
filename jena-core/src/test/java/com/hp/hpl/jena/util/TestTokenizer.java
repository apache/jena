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

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.util.Tokenizer;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test for the trivial tokenizer utility.
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
