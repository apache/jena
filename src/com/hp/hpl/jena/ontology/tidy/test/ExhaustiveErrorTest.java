/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.test;

import com.hp.hpl.jena.ontology.tidy.impl.*;

import com.hp.hpl.jena.graph.*;
import java.util.*;

import junit.framework.*;

/**
 * @author Jeremy J. Carroll
 *30843957 
 *C4 335556
C5 6567175
C6 2072041
C10 3600
C11 252
C13 712
C14 7000
C15 1804
C21 1835754
C22 2728980
C31 15213623
C32 5612769
C34 14286029
C43 128343
C45 15588
C46 123771
C52 635043
C61 95552
C62 70066
C64 97248
C67 35464
299596/30809197  errors checked. (850.103s) {notype ,ontologyID }
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
    
    static SimpleChecker empty = new SimpleChecker();
    
    static public Test suite() {
        TestSuite s = new TestSuite("OWL Syntax Error Checks - by predicate");
        for (int j = 1; j < SZ; j++) {
            if (Grammar.isPseudoCategory(j))
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
        
        for (int i=0;i<startp.length;i++) {
            Assert.assertNotNull(TestExamples.examples[startp[i]]);
            for (int j=0;j<TestExamples.examples[startp[i]].length;j++) {
                Assert.assertEquals(startp[i],empty.getCategory(TestExamples.examples[startp[i]][j]));
            }
        }
        if (MonotonicErrorAnalyzer.isBlank(p))
            return;
        if (MonotonicErrorAnalyzer.isLiteral(p))
            return;
        for (int i = 1; i < SZ; i++) {
            if (Grammar.isPseudoCategory(i))
                continue;
            if (MonotonicErrorAnalyzer.isLiteral(i))
                continue;
            
            int starts[] = start(i);
            for (int k = 1; k < SZ; k++) {
                if (Grammar.isPseudoCategory(k))
                    continue;
                int key = look.qrefine(i, p, k); 
                int starto[] = start(k);
                if (key == Failure) {
                    allCases(i,p,k,starts, startp, starto,-1);
                } else {
                    
                    int s0 = look.subject(i, key);
                    int p0 = look.prop(p, key);
                    int o0 = look.object(k, key);
                    int sp[] = Q.intersection(starts,startp);
                    int so[] = Q.intersection(starts,starto);
                    int po[] = Q.intersection(startp,starto);
                    if (sp.length>0 && look.meet(s0,p0)==Failure) {
                        allCases(i,p,k,sp, sp, starto,2);
                    }
                    if (so.length>0 && look.meet(s0,o0)==Failure) {
                        allCases(i,p,k,so, startp, so,1);
                    }
                    if (po.length>0 && look.meet(o0,p0)==Failure) {
                        allCases(i,p,k,starts, po, po,0);
                    }
                }
            }
        }
        long time = System.currentTimeMillis() - startTime;
        System.out.println(predTestCount +"/" + allTestCount+ "  errors checked. (" + (time/1000) +"." + (time%1000) + "s) " + this.getName());
        
        for (int xx=0;xx<allCodes.length;xx++)
            if (allCodes[xx]!=0)
                System.out.println("C"+xx+" "+allCodes[xx]);
    }    
    
    static int allCodes[] = new int[200];
    
    private void allCases(int s, int p, int o, int starts[], int startp[], int starto[],int force) {
        MonotonicErrorAnalyzer m = new MonotonicErrorAnalyzer();
        for (int i = 0; i < starts.length; i++)
            for (int j = 0; j < startp.length; j++)
                for (int k = 0; k < starto.length; k++) {
                    Iterator it = TestExamples.examples(i,j,k,starts[i],startp[j],starto[k]);
                    while (it.hasNext()) {
                        //   if ( predTestCount % 10000 == 0)
                        //   System.out.println(predTestCount +"/" + allTestCount+ "  errors checked. "+ this.getName());
                        Triple t = (Triple)it.next();
                        switch (force) {
                        case -1:
                            break;
                        case 0:
                            if (!t.getObject().equals(t.getPredicate()))
                                continue;
                            break;
                        case 1:
                            if ((!t.getSubject().equals(t.getObject()))
                                || t.getSubject().equals(t.getPredicate()))
                                    continue;
                            break;
                        case 2:
                            if ((!t.getSubject().equals(t.getPredicate()))
                                    || t.getObject().equals(t.getPredicate()))
                                continue;
                            break;
                        }
                        predTestCount++;
                        
                        allTestCount++;
                        MonotonicProblem rs[] =
                            MonotonicErrorAnalyzer.flattenProblem(
                            t,m.getErrorCode(s, p, o, t));
                        assertTrue("No problems found",rs.length>0);
                        for (int xx=0;xx<rs.length;xx++)
                            allCodes[rs[xx].getTypeCode()]++;
                        
                    }
                }
    }
    
    
    /**
     * An iterator over triples that could
     * express this example, with all possible variations
     * of the meet condition.
     * 
     * @param s
     * @param p
     * @param o
     * @return
     */
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
        } else
            if (MonotonicErrorAnalyzer.maybeBuiltinID(i)) {
                s[ix++] = i;
            }
            
        int rslt[] = new int[ix];
        System.arraycopy(s, 0, rslt, 0, ix);
        Arrays.sort(rslt);
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

