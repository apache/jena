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

package org.apache.jena.ttl_test.test.turtle;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TurtleTestSuiteManifest extends TestSuite {
    static public TestSuite suite() {
        return new TurtleTestSuiteManifest();
    }

    private TurtleTestSuiteManifest() {
        super("TurtleManifest");
        testGood("rdf-schema"     , "RaptorTurtle/rdf-schema.ttl",      "RaptorTurtle/rdf-schema.out");
        testGood("rdfq-results"   , "RaptorTurtle/rdfq-results.ttl",    "RaptorTurtle/rdfq-results.out", "http://www.w3.org/2001/sw/DataAccess/df1/tests/rdfq-results.ttl");
        testGood("rdfs-namespace" , "RaptorTurtle/rdfs-namespace.ttl",  "RaptorTurtle/rdfs-namespace.out");
        testGood("test-00"        , "RaptorTurtle/test-00.ttl",         "RaptorTurtle/test-00.out",      "http://www.w3.org/2001/sw/DataAccess/df1/tests/test-00.ttl");
        testGood("test-01"        , "RaptorTurtle/test-01.ttl",         "RaptorTurtle/test-01.out");
        testGood("test-02"        , "RaptorTurtle/test-02.ttl",         "RaptorTurtle/test-02.out");
        testGood("test-03"        , "RaptorTurtle/test-03.ttl",         "RaptorTurtle/test-03.out");
        testGood("test-04"        , "RaptorTurtle/test-04.ttl",         "RaptorTurtle/test-04.out");
        testGood("test-05"        , "RaptorTurtle/test-05.ttl",         "RaptorTurtle/test-05.out");
        testGood("test-06"        , "RaptorTurtle/test-06.ttl",         "RaptorTurtle/test-06.out");
        testGood("test-07"        , "RaptorTurtle/test-07.ttl",         "RaptorTurtle/test-07.out");
        testGood("test-08"        , "RaptorTurtle/test-08.ttl",         "RaptorTurtle/test-08.out");
        testGood("test-09"        , "RaptorTurtle/test-09.ttl",         "RaptorTurtle/test-09.out");
        testGood("test-10"        , "RaptorTurtle/test-10.ttl",         "RaptorTurtle/test-10.out");
        testGood("test-11"        , "RaptorTurtle/test-11.ttl",         "RaptorTurtle/test-11.out");
        testGood("test-12"        , "RaptorTurtle/test-12.ttl",         "RaptorTurtle/test-12.out");
        testGood("test-13"        , "RaptorTurtle/test-13.ttl",         "RaptorTurtle/test-13.out");
        testGood("test-14"        , "RaptorTurtle/test-14.ttl",         "RaptorTurtle/test-14.out");
        testGood("test-15"        , "RaptorTurtle/test-15.ttl",         "RaptorTurtle/test-15.out");
        testGood("test-16"        , "RaptorTurtle/test-16.ttl",         "RaptorTurtle/test-16.out");

        testBad("Bad-00" , "RaptorTurtle/bad-00.ttl");
        testBad("Bad-01" , "RaptorTurtle/bad-01.ttl");
        testBad("Bad-02" , "RaptorTurtle/bad-02.ttl");
        testBad("Bad-03" , "RaptorTurtle/bad-03.ttl");
        testBad("Bad-04" , "RaptorTurtle/bad-04.ttl");
        testBad("Bad-05" , "RaptorTurtle/bad-05.ttl");
        testBad("Bad-06" , "RaptorTurtle/bad-06.ttl");
        testBad("Bad-07" , "RaptorTurtle/bad-07.ttl");
        testBad("Bad-08" , "RaptorTurtle/bad-08.ttl");
        testBad("Bad-09" , "RaptorTurtle/bad-09.ttl");
        testBad("Bad-10" , "RaptorTurtle/bad-10.ttl");
        testBad("Bad-11" , "RaptorTurtle/bad-11.ttl");
        testBad("Bad-12" , "RaptorTurtle/bad-12.ttl");
        testBad("Bad-13" , "RaptorTurtle/bad-13.ttl");
    }

    private void testBad(String name, String inputFile) {
        TestCase test = new TestItemBadSyntax(name, "file:testing/Turtle/"+inputFile);
        this.addTest(test);
    }

    private void testGood(String name, String inputFile, String outputFile) {
        testGood(name, inputFile, outputFile, null);
    }

    private void testGood(String name, String inputFile, String outputFile, String baseURI) {
        TestCase test = new TestItemTurtleExec(name, "file:testing/Turtle/"+inputFile, "file:testing/Turtle/"+outputFile, baseURI);
        this.addTest(test);
    }
}
