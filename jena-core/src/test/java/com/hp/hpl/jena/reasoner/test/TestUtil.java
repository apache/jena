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

package com.hp.hpl.jena.reasoner.test;

import java.util.Iterator ;

import org.junit.Assert ;
import junit.framework.TestCase ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;

/**
 * Collection of utilities to assist with unit testing.
 */
public class TestUtil {
    
    /**
     * Helper method to test an iterator against a list of objects - order independent
     * @param testCase The JUnit test case that is invoking this helper
     * @param it The iterator to test
     * @param vals The expected values of the iterator
     */
    public static void assertIteratorValues(TestCase testCase, Iterator<?> it, Object[] vals) {
        assertIteratorValues( testCase, it, vals, 0 );
    }
    
    /**
     * Helper method to test an iterator against a list of objects - order independent, and
     * can optionally check the count of anonymous resources.  This allows us to test a 
     * iterator of resource values which includes both URI nodes and bNodes. 
     * @param testCase The JUnit test case that is invoking this helper
     * @param it The iterator to test
     * @param vals The expected values of the iterator
     * @param countAnon If non zero, count the number of anonymous resources returned by <code>it</code>,
     * and don't check these resources against the expected <code>vals</code>.
     */
    public static void assertIteratorValues(TestCase testCase, Iterator<?> it, Object[] vals, int countAnon ) {
        Logger logger = LoggerFactory.getLogger( testCase.getClass() );
        
        boolean[] found = new boolean[vals.length];
        int anonFound = 0;
        
        for (int i = 0; i < vals.length; i++) found[i] = false;
        
        
        while (it.hasNext()) {
            Object n = it.next();
            boolean gotit = false;
            
            // do bNodes separately
            if (countAnon > 0 && isAnonValue( n )) {
                anonFound++;
                continue;
            }
            
            for (int i = 0; i < vals.length; i++) {
                if (n.equals(vals[i])) {
                    gotit = true;
                    found[i] = true;
                }
            }
            if (!gotit) {
                logger.debug( testCase.getName() + " found unexpected iterator value: " + n);
            }
            Assert.assertTrue( testCase.getName() + " found unexpected iterator value: " + n, gotit);
        }
        
        // check that no expected values were unfound
        for (int i = 0; i < vals.length; i++) {
            if (!found[i]) {
//                for (int j = 0; j < vals.length; j += 1) System.err.println( "#" + j + ": " + vals[j] );
                logger.debug( testCase.getName() + " failed to find expected iterator value: " + vals[i]);
            }
            Assert.assertTrue(testCase.getName() + " failed to find expected iterator value: " + vals[i], found[i]);
        }
        
        // check we got the right no. of anons
        Assert.assertEquals( testCase.getName() + " iterator test did not find the right number of anon. nodes", countAnon, anonFound );
    }
    

    /**
     * Replace all blocks of white space by a single space character, just
     * used for creating test cases.
     * 
     * @param src the original string
     * @return normalized version of src
     */
    public static String normalizeWhiteSpace(String src) {
        StringBuilder result = new StringBuilder(src.length());
        boolean inWhitespaceBlock = false;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!inWhitespaceBlock) {
                    result.append(" ");
                    inWhitespaceBlock = true;
                }
            } else {
                inWhitespaceBlock = false;
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * Check the length of an iterator.
     */
    public static void assertIteratorLength(Iterator<?> it, int expectedLength) {
        int length = 0;
        while (it.hasNext()) {
            it.next();
            length++;
        }
        Assert.assertEquals(expectedLength, length);
    }
    
    
    /**
     * For the purposes of counting, a value is anonymous if (a) it is an anonymous resource,
     * or (b) it is a statement with a bNode subject or (c) it is a statement with a bNode
     * object.  This is because we cannot check bNode identity against fixed expected data values.
     * @param n A value
     * @return True if n is anonymous
     */
    protected static boolean isAnonValue( Object n ) {
        return ((n instanceof Resource) && ((Resource) n).isAnon()) ||
               ((n instanceof Statement) && ((Statement) n).getSubject().isAnon()) ||
               ((n instanceof Statement) && isAnonValue( ((Statement) n).getObject() ));
    }
}
