/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.test;

import com.hp.hpl.jena.ontology.tidy.impl.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Jeremy J. Carroll
 *
 */
public class ExhaustiveErrorTest extends TestCase implements Constants {


    static private long startTime = 0;
    static private LookupTable look = (LookupTable) LookupTable.get();
    
    private int predTestCount = 0;
    static private int allTestCount=0;

    static private final int SZ = CategorySet.unsorted.size();
    final private int p;
	/**
     * @param j
     */
    public ExhaustiveErrorTest(int j) {
        super(CategorySet.catString(j));
        p = j;
    }


    static public Test suite() {
		TestSuite s = new TestSuite("OWL Syntax Error Checks - by predicate");
	       for (int j = 1; j < SZ; j++) {
	            if (Grammar.isPseudoCategory(j))
	                continue;
	            if (MonotonicErrorAnalyzer.isBlank(j))
	                continue;
	            if (MonotonicErrorAnalyzer.isLiteral(j))
	                continue;
	            s.addTest(new ExhaustiveErrorTest(j));
	       }
		return s;
   }
    protected void runTest() {

        if (startTime==0) {
            startTime = System.currentTimeMillis();
        }
        int startp[] = start(p);
        for (int i = 1; i < SZ; i++) {
            if (Grammar.isPseudoCategory(i))
                continue;
            if (MonotonicErrorAnalyzer.isLiteral(i))
                continue;

            int starts[] = start(i);
            for (int k = 1; k < SZ; k++) {
                if (Grammar.isPseudoCategory(k))
                    continue;
                if (look.qrefine(i, p, k) == Failure) {
                    allCases(i,p,k,starts, startp, start(k));
                }
            }
        }
        long time = System.currentTimeMillis() - startTime;
        System.out.println(predTestCount +"/" + allTestCount+ "  errors checked. (" + (time/1000) +"." + (time%1000) + "s) " + this.getName());
        
    }    

    private void allCases(int s, int p, int o, int starts[], int startp[], int starto[]) {
        MonotonicErrorAnalyzer m = new MonotonicErrorAnalyzer();
        for (int i = 0; i < starts.length; i++)
            for (int j = 0; j < startp.length; j++)
                for (int k = 0; k < starto.length; k++) {
                    predTestCount++;
                    allTestCount++;
                    m.getErrorCode(s, p, o, starts[i], startp[j],
                            starto[k]);
                }
    }

    
    /**
     * @param s
     * @return
     */
    private int[] start(int i) {

        int ix = 0;
        int s[] = new int[4];
            ix = 0;
            if (MonotonicErrorAnalyzer.isClassOnly(i)) {
                s[ix++] = Grammar.classOnly;
            }
            if (MonotonicErrorAnalyzer.isPropertyOnly(i)) {
                s[ix++] = Grammar.propertyOnly;
            }
            if (MonotonicErrorAnalyzer.isUserID(i)) {
                s[ix++] = Grammar.userID;
            }
            if (MonotonicErrorAnalyzer.isBlank(i)) {
                s[ix++] = Grammar.blank;
            }
            if (ix == 0) {
                s[ix++] = i;
                switch (i) {
                case Grammar.badID:
                case Grammar.dlInteger:
                case Grammar.liteInteger:
                case Grammar.literal:
                case Grammar.userTypedLiteral:
                    break;
                default:
                }
            }
            if (MonotonicErrorAnalyzer.maybeBuiltinID(i)) {
                s[ix++] = i;
            }

            int rslt[] = new int[ix];
            System.arraycopy(s, 0, rslt, 0, ix);
            return rslt;

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
 
