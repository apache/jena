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

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;

import java.util.Random;
/**
 *
 * @author  jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:39 $' 
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
