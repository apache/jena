/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */


package org.apache.jena.reasoner.rulesys.test;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.FBRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.test.ReasonerTester;

/**
 *  Test an FB hybrid using the emerging LP engine on the basic RDFS tests.
 */
public class TestLPRDFS {

    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs-fb-lp-expt.rules";

    /** The parsed rules */
    protected static List<Rule> ruleSet;

    @Test
    public void test1()  throws IOException {
        doTest("test1");
    }

    @Test
    public void test2()  throws IOException {
        doTest("test2");
    }

    @Test
    public void test3()  throws IOException {
        doTest("test3");
    }

    @Test
    public void test4()  throws IOException {
        doTest("test4");
    }

    @Test
    public void test5()  throws IOException {
        doTest("test5");
    }

    @Test
    public void test6()  throws IOException {
        doTest("test6");
    }

    @Test
    public void test7()  throws IOException {
        doTest("test7");
    }

    @Test
    public void test8()  throws IOException {
        doTest("test8");
    }

    @Test
    public void test9()  throws IOException {
        doTest("test9");
    }

    @Test
    public void test10()  throws IOException {
        doTest("test10");
    }

    @Test
    public void test11()  throws IOException {
        doTest("test11");
    }

    @Test
    public void test12()  throws IOException {
        doTest("test12");
    }

    @Test
    public void test13()  throws IOException {
        doTest("test13");
    }

    @Test
    public void test14()  throws IOException {
        doTest("test14");
    }

    @Test
    public void test15()  throws IOException {
        doTest("test15");
    }

    @Test
    public void test16()  throws IOException {
        doTest("test16");
    }

    @Test
    public void test18()  throws IOException {
        doTest("test18");
    }

    @Test
    public void test20()  throws IOException {
        doTest("test20");
    }

    /**
     * Run a named test.
     */
    public void doTest(String name) throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        tester.runTest(ReasonerTester.BASE_URI + "rdfs/" + name, makeReasoner(), this);
    }

    /**
     * Return the reasoner to test
     */
    public static Reasoner makeReasoner() {
        FBRuleReasoner reasoner = new FBRuleReasoner(loadRules());
        // Don't have TGC enable yet.
        return reasoner;
    }

    /**
     * Return the RDFS rule set, loading it in if necessary
     */
    public static List<Rule> loadRules() {
        if (ruleSet == null) ruleSet = FBRuleReasoner.loadRules( RULE_FILE );
        return ruleSet;
    }
}
