/******************************************************************
 * File:        UtilForTests.java
 * Created by:  Dave Reynolds
 * Created on:  14-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestUtil.java,v 1.11 2003-12-12 16:40:21 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import junit.framework.TestCase;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Collection of utilities to assist with unit testing.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.11 $ on $Date: 2003-12-12 16:40:21 $
 */
public class TestUtil {
    
    /**
     * Helper method to test an iterator against a list of objects - order independent
     * @param testCase The JUnit test case that is invoking this helper
     * @param it The iterator to test
     * @param vals The expected values of the iterator
     */
    public static void assertIteratorValues(TestCase testCase, Iterator it, Object[] vals) {
        assertIteratorValues( testCase, it, vals, 0 );
    }
    
    /**
     * Helper method to test an iterator against a list of objects - order independent, and
     * can optionally check the count of anonymous resources.  This allows us to test a 
     * iterator of resource values which includes both URI nodes and bNodes. 
     * @param testCase The JUnit test case that is invoking this helper
     * @param it The iterator to test
     * @param vals The expected values of the iterator
     * @param anonCount If non zero, count the number of anonymous resources returned by <code>it</code>,
     * and don't check these resources against the expected <code>vals</code>.
     */
    public static void assertIteratorValues(TestCase testCase, Iterator it, Object[] vals, int countAnon ) {
        Log logger = LogFactory.getLog( testCase.getClass() );
        
        boolean[] found = new boolean[vals.length];
        int anonFound = 0;
        
        for (int i = 0; i < vals.length; i++) found[i] = false;
        while (it.hasNext()) {
            Object n = it.next();
            // System.err.println( "Iterator " + n );
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
            TestCase.assertTrue( testCase.getName() + " found unexpected iterator value", gotit);
        }
        
        // check that no expected values were unfound
        for (int i = 0; i < vals.length; i++) {
            if (!found[i]) {
                logger.debug( testCase.getName() + " failed to find expected iterator value: " + vals[i]);
            }
            TestCase.assertTrue(testCase.getName() + " failed to find expected iterator value", found[i]);
        }
        
        // check we got the right no. of anons
        TestCase.assertEquals( testCase.getName() + " iterator test did not find the right number of anon. nodes", countAnon, anonFound );
    }
    

    /**
     * Replace all blocks of white space by a single space character, just
     * used for creating test cases.
     * 
     * @param src the original string
     * @return normalized version of src
     */
    public static String normalizeWhiteSpace(String src) {
        StringBuffer result = new StringBuffer(src.length());
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
    public static void assertIteratorLength(Iterator it, int expectedLength) {
        int length = 0;
        while (it.hasNext()) {
            it.next();
            length++;
        }
        TestCase.assertEquals(expectedLength, length);
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

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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

