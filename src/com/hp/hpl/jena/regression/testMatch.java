/*
 *  (c)  Copyright Hewlett-Packard Company 2001,2002
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
 * $Id: testMatch.java,v 1.3 2003-06-17 14:39:39 chris-dollin Exp $
 *
 * testMatch.java
 *
 * Created on June 29, 2001, 9:36 PM
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.ModelMem;

import java.util.Random;
/**
 *
 * @author  jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.3 $' Date='$Date: 2003-06-17 14:39:39 $' 
 */

public class testMatch extends java.lang.Object {
    static int QUANTITY = 1;
    static int DIMENSION = 6;
    static boolean QUIET = true;
    protected static void doTest(Model m1, Model m2) {
        new testMatch(0xfab
        //(int)System.currentTimeMillis()
        , m1, m2).test();
        System.out.println("End testMatch");
    }

    private String test;
    private Random random;
    private int n = 0;
    private Model m1, m2;

    /** Creates new testMatch */
    testMatch(int seed, Model m1, Model m2) {
        test = "testMatch seed=" + seed;
        if (!QUIET)
            System.out.println("Beginning " + test);
        random = new Random(seed);
        this.m1 = m1;
        this.m2 = m2;
    }
    static private void empty(Model m) {
        StmtIterator iter = m.listStatements();
        while (iter.hasNext()) {
            iter.nextStatement();
            iter.remove();
        }
    }
    void test() {
        //  test4DiHyperCube(5,1,true);
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
            int tenth = cnt / 10;
            if (tenth == 0)
                tenth = 1;
            if (!QUIET) {
                System.out.print("2di " + dim + "x" + cnt + ":");
                System.out.flush();
            }
            long startTime = System.currentTimeMillis();
            long equalsTime = 0;

            for (int i = 0; i < cnt; i++) {
                n++;
                empty(m1);
                n++;
                empty(m2);
                int a1, b1;
                do {
                    a1 = random.nextInt(sz);
                    b1 = random.nextInt(sz);
                } while (type != DiHyperCube.equal(a1, b1));
                n++;
                new DiHyperCube(dim, m1).dupe(a1).dupe(a1).dupe(a1);
                n++;
                new DiHyperCube(dim, m2).dupe(b1).dupe(b1).dupe(b1);
                if ((!QUIET) && (i + 1) % tenth == 0) {
                    System.out.print(type ? "+" : "-");
                    System.out.flush();
                }
                n++;
                long beforeEquals = System.currentTimeMillis();
                if (m1.isIsomorphicWith(m2) != type)
                    error();
                equalsTime += System.currentTimeMillis() - beforeEquals;
            }
            long totalTime = System.currentTimeMillis() - startTime;
            int percent = (int) (equalsTime * 100 / totalTime);
            int millis = (int) (equalsTime / cnt);

            if (!QUIET)
                System.out.println("time: " + millis + "ms (" + percent + "%)");
        } catch (Exception e) {
            error(e);
        }
    }
    private void test4DiHyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;
            int tenth = cnt / 10;
            if (tenth == 0)
                tenth = 1;
            if (!QUIET)
                System.out.print("4di " + dim + "x" + cnt + ":");
            System.out.flush();
            long startTime = System.currentTimeMillis();
            long equalsTime = 0;

            for (int i = 0; i < cnt; i++) {
                n++;
                empty(m1);
                n++;
                empty(m2);
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
                if ((!QUIET) && (i + 1) % tenth == 0) {
                    System.out.print(type ? "+" : "-");
                    System.out.flush();
                }
                n++;
                long beforeEquals = System.currentTimeMillis();
                // if ( n == 45 )
                //     System.out.println("Here");
                if (m1.isIsomorphicWith(m2) != type) {
                    System.out.println(
                        "(" + a1 + "," + a2 + "),(" + b1 + "," + b2 + ")");
                    error();
                }
                equalsTime += System.currentTimeMillis() - beforeEquals;
            }
            long totalTime = System.currentTimeMillis() - startTime;
            int percent = (int) (equalsTime * 100 / totalTime);
            int millis = (int) (equalsTime / cnt);

            if (!QUIET)
                System.out.println("time: " + millis + "ms (" + percent + "%)");
        } catch (Exception e) {
            error(e);
        }
    }

    private void test2HyperCube(int dim, int cnt) {
        try {
            int sz = 1 << dim;
            int tenth = cnt / 10;
            if (tenth == 0)
                tenth = 1;
            if (!QUIET)
                System.out.print("2:  " + dim + "x" + cnt + ":");
            System.out.flush();
            long startTime = System.currentTimeMillis();
            long equalsTime = 0;

            for (int i = 0; i < cnt; i++) {
                n++;
                empty(m1);
                n++;
                empty(m2);
                int a1, b1;
                a1 = random.nextInt(sz);
                b1 = random.nextInt(sz);
                n++;
                new HyperCube(dim, m1).dupe(a1).dupe(a1).dupe(a1);
                n++;
                new HyperCube(dim, m2).dupe(b1).dupe(b1).dupe(b1);
                if ((!QUIET) && (i + 1) % tenth == 0) {
                    System.out.print("+");
                    System.out.flush();
                }
                n++;
                long beforeEquals = System.currentTimeMillis();
                if (!m1.isIsomorphicWith(m2))
                    error();
                equalsTime += System.currentTimeMillis() - beforeEquals;
            }
            long totalTime = System.currentTimeMillis() - startTime;
            int percent = (int) (equalsTime * 100 / totalTime);
            int millis = (int) (equalsTime / cnt);

            if (!QUIET)
                System.out.println("time: " + millis + "ms (" + percent + "%)");
        } catch (Exception e) {
            error(e);
        }
    }
    private void test4HyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;
            int tenth = cnt / 10;
            if (tenth == 0)
                tenth = 1;
            if (!QUIET)
                System.out.print("4:  " + dim + "x" + cnt + ":");
            System.out.flush();
            long startTime = System.currentTimeMillis();
            long equalsTime = 0;

            for (int i = 0; i < cnt; i++) {
                n++;
                empty(m1);
                n++;
                empty(m2);
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
                if ((!QUIET) && (i + 1) % tenth == 0) {
                    System.out.print(type ? "+" : "-");
                    System.out.flush();
                }
                n++;
                long beforeEquals = System.currentTimeMillis();
                // if ( n == 45 )
                //     System.out.println("Here");
                if (m1.isIsomorphicWith(m2) != type) {
                    System.out.println(
                        "(" + a1 + "," + a2 + "),(" + b1 + "," + b2 + ")");
                    error();
                }
                equalsTime += System.currentTimeMillis() - beforeEquals;
            }
            long totalTime = System.currentTimeMillis() - startTime;
            int percent = (int) (equalsTime * 100 / totalTime);
            int millis = (int) (equalsTime / cnt);

            if (!QUIET)
                System.out.println("time: " + millis + "ms (" + percent + "%)");
        } catch (Exception e) {
            error(e);
        }
    }
    private void test4ToggleHyperCube(int dim, int cnt, boolean type) {
        try {
            int sz = 1 << dim;
            int tenth = cnt / 10;
            if (tenth == 0)
                tenth = 1;
            if (!QUIET)
                System.out.print("4T: " + dim + "x" + cnt + ":");
            System.out.flush();
            long startTime = System.currentTimeMillis();
            long equalsTime = 0;

            for (int i = 0; i < cnt; i++) {
                n++;
                empty(m1);
                n++;
                empty(m2);
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
                if ((!QUIET) && (i + 1) % tenth == 0) {
                    System.out.print(type ? "+" : "-");
                    System.out.flush();
                }
                n++;
                long beforeEquals = System.currentTimeMillis();
                // if ( n == 45 )
                //     System.out.println("Here");
                if (m1.isIsomorphicWith(m2) != type) {
                    System.out.println(
                        "(" + a1 + "," + a2 + "),(" + b1 + "," + b2 + ")");
                    error();
                }
                equalsTime += System.currentTimeMillis() - beforeEquals;
            }
            long totalTime = System.currentTimeMillis() - startTime;
            int percent = (int) (equalsTime * 100 / totalTime);
            int millis = (int) (equalsTime / cnt);
            if (!QUIET)
                System.out.println("time: " + millis + "ms (" + percent + "%)");
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
    }
    public boolean getErrors() {
        return inError;
    }
    // RUN THIS TEST ONLY
    static public void main(String args[]) {
        DIMENSION = 8;
        QUANTITY = 10;
        QUIET = false;
        doTest(new ModelMem(), new ModelMem());

    }
}
