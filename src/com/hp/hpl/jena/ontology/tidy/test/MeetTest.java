/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.test;

import com.hp.hpl.jena.ontology.tidy.impl.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
/**
 * @author Jeremy J. Carroll
 *
 */
public class MeetTest extends TestCase implements Constants {


    static private LookupTable look = (LookupTable) LookupTable.get();
    
   
    static private final int SZ = CategorySet.unsorted.size();
    final private int p;
	/**
     * @param j
     */
    public MeetTest(int j) {
        super(CategorySet.catString(j));
        p = j;
    }


    static public Test suite() {
		TestSuite s = new TestSuite("OWL Syntax Meet Tests");
	       for (int j = 1; j < SZ; j++) {
	            if (Grammar.isPseudoCategory(j))
	                continue;
	            s.addTest(new MeetTest(j));
	       }
		return s;
   }
    protected void runTest() {

        for (int i = 1; i < SZ; i++) {
            int ci[] = CategorySet.getSet(i);
            int cp[] = CategorySet.getSet(p);
            int interp[] = LookupTable.intersection(ci,cp);
            Arrays.sort(interp);

            int ix;
            for (ix =0; ix<interp.length &&
                  Grammar.isPseudoCategory(interp[ix]); ix++);
            
            int rslt = look.meet(i,p);
            if (rslt == Failure) {
                assertTrue("Failure but non-empty", ix==interp.length);
            } else {
                int interp1[] = CategorySet.getSet(rslt);
                assertTrue("Empty result, but not failure",interp1.length>0);
                //assertFalse("Pseudocats in meet",Grammar.isPseudoCategory(interp1[0]));
                assertEquals("Lengths not equal",interp.length,interp1.length);
                for (int j=0;j<interp1.length;j++)
                    assertEquals("member not equal",interp[j],interp1[j]);
            }
        }
       
    }    
    




    
}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 
