/*
    (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: testMatch.java,v 1.7 2004-12-06 13:50:22 andy_seaborne Exp $
*/
package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;

import java.util.Random;
/**
 *
 * @author  jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.7 $' Date='$Date: 2004-12-06 13:50:22 $' 
 */

public class testMatch extends java.lang.Object {
    static int QUANTITY = 1;
    static int DIMENSION = 6;
    protected static void doTest(GetModel gm1) {
        new testMatch(0xfab
        //(int)System.currentTimeMillis()
        , gm1).test();
        System.out.println("End testMatch");
    }

    private String test;
    private Random random;
    private int n = 0;
    private Model m1, m2;
    private GetModel gm1, gm2;

    /** Creates new testMatch */
    testMatch(int seed, GetModel gm) {
        test = "testMatch seed=" + seed;
        random = new Random(seed);
        this.gm1 = gm;
        this.gm2 = gm;        
    }
    
    void test() {
        test2HyperCube(DIMENSION, QUANTITY);
        test4DiHyperCube(DIMENSION, QUANTITY, true);
        test4DiHyperCube(DIMENSION, QUANTITY, false);
        test4ToggleHyperCube(DIMENSION, QUANTITY, true);
        test4ToggleHyperCube(DIMENSION, QUANTITY, false);
        if (QUANTITY > 5) {
            test2DiHyperCube(DIMENSION, QUANTITY, true);
            test2DiHyperCube(DIMENSION, QUANTITY, false);
            test4HyperCube(DIMENSION, QUANTITY, true);
            test4HyperCube(DIMENSION, QUANTITY, false);
        }
    }
    
    private void test2DiHyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;
            for (int i = 0; i < cnt; i++) {
                n++;
                m1 = gm1.get();
                n++;
                m2 = gm2.get();
                int a1, b1;
                do {
                    a1 = random.nextInt(sz);
                    b1 = random.nextInt(sz);
                } while (type != DiHyperCube.equal(a1, b1));
                n++;
                new DiHyperCube(dim, m1).dupe(a1).dupe(a1).dupe(a1);
                n++;
                new DiHyperCube(dim, m2).dupe(b1).dupe(b1).dupe(b1);
                n++;
                if (m1.isIsomorphicWith(m2) != type)
                    error();
            }

        } catch (Exception e) {
            error(e);
        }
    }
    
    private void test4DiHyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;
            for (int i = 0; i < cnt; i++) {
            	n++;
            	m1 = gm1.get();
            	n++;
            	m2 = gm2.get();
                int a1, b1, a2, b2;
                do {
                    a1 = random.nextInt(sz);
                    b1 = random.nextInt(sz);
                    a2 = random.nextInt(sz);
                    b2 = random.nextInt(sz);
                } while (type != DiHyperCube.equal(a1, a2, b1, b2));
                n++;
                new DiHyperCube(dim, m1).dupe(a1).dupe(a1).dupe(a1).dupe(
                    a2).dupe(
                    a2).dupe(
                    a2);
                n++;
                new DiHyperCube(dim, m2).dupe(b1).dupe(b1).dupe(b1).dupe(
                    b2).dupe(
                    b2).dupe(
                    b2);
                n++;
                if (m1.isIsomorphicWith(m2) != type) {
                    System.out.println(
                        "(" + a1 + "," + a2 + "),(" + b1 + "," + b2 + ")");
                    error();
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void test2HyperCube(int dim, int cnt) {
        try {
            int sz = 1 << dim;

            for (int i = 0; i < cnt; i++) {
            	n++;
            	m1 = gm1.get();
            	n++;
            	m2 = gm2.get();
                int a1, b1;
                a1 = random.nextInt(sz);
                b1 = random.nextInt(sz);
                n++;
                new HyperCube(dim, m1).dupe(a1).dupe(a1).dupe(a1);
                n++;
                new HyperCube(dim, m2).dupe(b1).dupe(b1).dupe(b1);
                n++;
                if (!m1.isIsomorphicWith(m2))
                    error();
            }
        } catch (Exception e) {
            error(e);
        }
    }
    
    private void test4HyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;

            for (int i = 0; i < cnt; i++) {
            	n++;
            	m1 = gm1.get();
            	n++;
            	m2 = gm2.get();
                int a1, b1, a2, b2;
                do {
                    a1 = random.nextInt(sz);
                    b1 = random.nextInt(sz);
                    a2 = random.nextInt(sz);
                    b2 = random.nextInt(sz);
                } while (type != HyperCube.equal(a1, a2, b1, b2));
                n++;
                new HyperCube(dim, m1).dupe(a1).dupe(a1).dupe(a1).dupe(
                    a2).dupe(
                    a2).dupe(
                    a2);
                n++;
                new HyperCube(dim, m2).dupe(b1).dupe(b1).dupe(b1).dupe(
                    b2).dupe(
                    b2).dupe(
                    b2);
                n++;
                if (m1.isIsomorphicWith(m2) != type) {
                    System.out.println(
                        "(" + a1 + "," + a2 + "),(" + b1 + "," + b2 + ")");
                    error();
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }
    
    private void test4ToggleHyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;

            for (int i = 0; i < cnt; i++) {
            	n++;
            	m1 = gm1.get();
            	n++;
            	m2 = gm2.get();
                int a1, b1, a2, b2;
                do {
                    a1 = random.nextInt(sz);
                    b1 = random.nextInt(sz);
                    a2 = random.nextInt(sz);
                    b2 = random.nextInt(sz);
                } while (type != HyperCube.equal(a1, a2, b1, b2));
                n++;
                new HyperCube(dim, m1).toggle(a1, a2);
                n++;
                new HyperCube(dim, m2).toggle(b1, b2);
                n++;
                if (m1.isIsomorphicWith(m2) != type) {
                    System.out.println(
                        "(" + a1 + "," + a2 + "),(" + b1 + "," + b2 + ")");
                    error();
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }
    
    private boolean inError = false;
    
    private void error() {
        System.out.println(test + ": failed test " + Integer.toString(n));
        inError = true;
    }
    
    public void error(Exception e) {
        System.out.println(
            "Test Failed: " + test + " " + n + " " + e.toString());
        inError = true;
        e.printStackTrace();
    }
    
    public boolean getErrors() {
        return inError;
    }
}

/*
 *  (c)  Copyright 2001,2002 Hewlett-Packard Development Company, LP
 *   All rights reserved.
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
 *
 * $Id: testMatch.java,v 1.7 2004-12-06 13:50:22 andy_seaborne Exp $
 *
 * testMatch.java
 *
 * Created on June 29, 2001, 9:36 PM
 */
