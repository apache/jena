/******************************************************************
 * File:        UtilForTests.java
 * Created by:  Dave Reynolds
 * Created on:  14-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestUtil.java,v 1.8 2003-12-08 09:29:35 ian_dickinson Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import junit.framework.TestCase;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * Collection of utilities to assist with unit testing.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2003-12-08 09:29:35 $
 */
public class TestUtil {
    
    /** Helper function test an iterator against a list of objects - order independent */
    public static void assertIteratorValues(TestCase testCase, Iterator it, Object[] vals) {
        Logger logger = Logger.getLogger( testCase.getClass() );
        
        boolean[] found = new boolean[vals.length];
        for (int i = 0; i < vals.length; i++) found[i] = false;
        while (it.hasNext()) {
            Object n = it.next();
            boolean gotit = false;
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
        for (int i = 0; i < vals.length; i++) {
            if (!found[i]) {
                logger.debug( testCase.getName() + " failed to find expected iterator value: " + vals[i]);
            }
            TestCase.assertTrue(testCase.getName() + " failed to find expected iterator value", found[i]);
        }
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

