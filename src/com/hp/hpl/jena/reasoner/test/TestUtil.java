/******************************************************************
 * File:        UtilForTests.java
 * Created by:  Dave Reynolds
 * Created on:  14-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestUtil.java,v 1.3 2003-04-15 21:27:15 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import junit.framework.TestCase;
import java.util.Iterator;

/**
 * Collection of utilities to assist with unit testing.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-04-15 21:27:15 $
 */
public class TestUtil {

    /** Helper function test an iterator against a list of objects - order independent */
    public static void assertIteratorValues(TestCase testCase, Iterator it, Object[] vals) {
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
            testCase.assertTrue(gotit);
        }
        for (int i = 0; i < vals.length; i++) {
            testCase.assertTrue(found[i]);
        }
    }
    
}

/*
    (c) Copyright Hewlett-Packard Company 2003
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

