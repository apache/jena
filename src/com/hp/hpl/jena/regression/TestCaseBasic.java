/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 * $Id: TestCaseBasic.java,v 1.1.1.1 2002-12-19 19:20:48 bwm Exp $
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.regression.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestCase;

/** <p>This is a wrapper class, which implements a set of basic regression tests
 * as a set of JUnit tests.</p>
 *
 * <p>This is the first step of reworking the old Regression tests as a JUnit
 * test suite.  This is a simple wrapper class for the Regression test package.
 * The idea is that if time permits (fat chance) the old regression tests
 * will be reworked properly into this framework.</p>
 *
 * <p>This class is inteded to be subclassed with setup and teardown methods
 * to create models required for the tests.</p>
 *
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1.1.1 $ $Date: 2002-12-19 19:20:48 $
 */
public class TestCaseBasic extends TestCase {

    protected Model m1 = null;
    protected Model m2 = null;
    protected Model m3 = null;
    protected Model m4 = null;

    public TestCaseBasic(String name) {
        super(name);
    }

    public void test0() {
        // empty the test models in case they are persistent
        empty(m1);
        empty(m2);
        empty(m3);
        empty(m4);
    }

    protected void empty(Model m) {
        try {
            StmtIterator iter = m.listStatements();
            while (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
            assertTrue(m.size() == 0);
        } catch (Exception e) {
            System.err.println(e);
            assertTrue(false);
        }
    }

    public void test1() {
        try {
            Regression test = new Regression();
            test.test1(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test2() {
        try {
            Regression test = new Regression();
            test.test2(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test3() {
        try {
            Regression test = new Regression();
            test.test3(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test4() {
        try {
            Regression test = new Regression();
            test.test4(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test5() {
        try {
            Regression test = new Regression();
            test.test5(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test6() {
        try {
            Regression test = new Regression();
            test.test6(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test7() {
        try {
            Regression test = new Regression();
            test.test7(m1, m2);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test8() {
        try {
            Regression test = new Regression();
            test.test8(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test9() {
        try {
            Regression test = new Regression();
            test.test9(m2);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test10() {
        try {
            Regression test = new Regression();
            test.test10(m3);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test11() {
        try {
            Regression test = new Regression();
            test.test11(m1, m2);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test12() {
        try {
            Regression test = new Regression();
            test.test12(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test13() {
        try {
            Regression test = new Regression();
            test.test13(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test14() {
        try {
            Regression test = new Regression();
            test.test14(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test15() {
        try {
            Regression test = new Regression();
            test.test15(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test16() {
        try {
            Regression test = new Regression();
            test.test16(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test17() {
        try {
            Regression test = new Regression();
            test.test17(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test18() {
        try {
            Regression test = new Regression();
            test.test18(m4);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void test19() {
        try {
            Regression test = new Regression();
            test.test19(m2, m3);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }
    /*    
        public void test20() {
            try {
                Regression test = new Regression();
                test.test20(m4);
                assertTrue (! test.getErrors());            
            } catch (Exception e) {
                System.out.println(e);
                assertTrue(false);
            }
        }
    */
    public void test97() {
        try {
            Regression test = new Regression();
            test.test97(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void testModelEquals() {
        try {
            testModelEquals test = new testModelEquals();
            test.test(m1, m2);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }
    public void testMatch() {
        try {
            testMatch test = new testMatch(0xfab, m1, m2);
            test.test();
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }
    /*
    public void testWriterAndReader() {
        try {
            testWriterAndReader test = new testWriterAndReader();
            test.test(m1,m2,m3,m4);
            assertTrue(! test.getErrors());            
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }
    */
    public void testNTripleReader() {
        try {
            testNTripleReader test = new testNTripleReader();
            test.test(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }
    /* 
     public void testWriterInterface() {
         try {
             testWriterInterface test = new testWriterInterface();
             test.test(m1);
             assertTrue(! test.getErrors());            
         } catch (Exception e) {
             System.out.println(e);
             assertTrue(false);
         }
     }
     */
    public void testReaderInterface() {
        try {
            testReaderInterface test = new testReaderInterface();
            test.test(m1);
            assertTrue(!test.getErrors());
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }

    public void soaktest() { // a very crude soak test
        try {
            int errCount = 0;

            for (int i = 1; i <= 100; i++) {
                Regression test = new Regression();
                test0();
                test.test1(m1);
                test.test2(m1);
                test.test3(m1);
                test.test4(m1);
                test.test5(m1);
                test.test6(m1);
                test.test7(m1, m2);
                test.test8(m1);
                test.test9(m2);
                test.test10(m3);
                test.test11(m1, m2);
                test.test12(m1);
                test.test13(m1);
                test.test14(m1);
                test.test15(m1);
                test.test16(m1);
                test.test17(m1);
                test.test18(m4);
                test.test19(m2, m3);
                test.test97(m1);
                if (test.getErrors())
                    errCount++;
                if ((i % 10) == 0) {
                    System.out.println(
                        "error count = " + errCount + " rounds = " + i);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(false);
        }
    }
}