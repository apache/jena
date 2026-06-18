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

package org.apache.jena.core_ttl.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.core_ttl.TurtleTestReader;
import org.apache.jena.core_ttl.parser.TurtleParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;

public class TurtleSuite {

    static { JenaTestLib.setup(); }

    @Test
    public void test_rdf_schema() {
        testGood("rdf-schema", "RaptorTurtle/rdf-schema.ttl", "RaptorTurtle/rdf-schema.out");
    }

    @Test
    public void test_rdfq_results() {
        testGood("rdfq-results", "RaptorTurtle/rdfq-results.ttl", "RaptorTurtle/rdfq-results.out",
                 "http://www.w3.org/2001/sw/DataAccess/df1/tests/rdfq-results.ttl");
    }

    @Test
    public void test_rdfs_namespace() {
        testGood("rdfs-namespace", "RaptorTurtle/rdfs-namespace.ttl", "RaptorTurtle/rdfs-namespace.out");
    }

    @Test
    public void test_00() {
        testGood("test-00", "RaptorTurtle/test-00.ttl", "RaptorTurtle/test-00.out",
                 "http://www.w3.org/2001/sw/DataAccess/df1/tests/test-00.ttl");
    }

    @Test
    public void test_01() {
        testGood("test-01", "RaptorTurtle/test-01.ttl", "RaptorTurtle/test-01.out");
    }

    @Test
    public void test_02() {
        testGood("test-02", "RaptorTurtle/test-02.ttl", "RaptorTurtle/test-02.out");
    }

    @Test
    public void test_03() {
        testGood("test-03", "RaptorTurtle/test-03.ttl", "RaptorTurtle/test-03.out");
    }

    @Test
    public void test_04() {
        testGood("test-04", "RaptorTurtle/test-04.ttl", "RaptorTurtle/test-04.out");
    }

    @Test
    public void test_05() {
        testGood("test-05", "RaptorTurtle/test-05.ttl", "RaptorTurtle/test-05.out");
    }

    @Test
    public void test_06() {
        testGood("test-06", "RaptorTurtle/test-06.ttl", "RaptorTurtle/test-06.out");
    }

    @Test
    public void test_07() {
        testGood("test-07", "RaptorTurtle/test-07.ttl", "RaptorTurtle/test-07.out");
    }

    @Test
    public void test_08() {
        testGood("test-08", "RaptorTurtle/test-08.ttl", "RaptorTurtle/test-08.out");
    }

    @Test
    public void test_09() {
        testGood("test-09", "RaptorTurtle/test-09.ttl", "RaptorTurtle/test-09.out");
    }

    @Test
    public void test_10() {
        testGood("test-10", "RaptorTurtle/test-10.ttl", "RaptorTurtle/test-10.out");
    }

    @Test
    public void test_11() {
        testGood("test-11", "RaptorTurtle/test-11.ttl", "RaptorTurtle/test-11.out");
    }

    @Test
    public void test_12() {
        testGood("test-12", "RaptorTurtle/test-12.ttl", "RaptorTurtle/test-12.out");
    }

    @Test
    public void test_13() {
        testGood("test-13", "RaptorTurtle/test-13.ttl", "RaptorTurtle/test-13.out");
    }

    @Test
    public void test_14() {
        testGood("test-14", "RaptorTurtle/test-14.ttl", "RaptorTurtle/test-14.out");
    }

    @Test
    public void test_15() {
        testGood("test-15", "RaptorTurtle/test-15.ttl", "RaptorTurtle/test-15.out");
    }

    @Test
    public void test_16() {
        testGood("test-16", "RaptorTurtle/test-16.ttl", "RaptorTurtle/test-16.out");
    }

    @Test
    public void test_bad_00() {
        testBad("Bad-00", "RaptorTurtle/bad-00.ttl");
    }

    @Test
    public void test_bad_01() {
        testBad("Bad-01", "RaptorTurtle/bad-01.ttl");
    }

    @Test
    public void test_bad_02() {
        testBad("Bad-02", "RaptorTurtle/bad-02.ttl");
    }

    @Test
    public void test_bad_03() {
        testBad("Bad-03", "RaptorTurtle/bad-03.ttl");
    }

    @Test
    public void test_bad_04() {
        testBad("Bad-04", "RaptorTurtle/bad-04.ttl");
    }

    @Test
    public void test_bad_05() {
        testBad("Bad-05", "RaptorTurtle/bad-05.ttl");
    }

    @Test
    public void test_bad_06() {
        testBad("Bad-06", "RaptorTurtle/bad-06.ttl");
    }

    @Test
    public void test_bad_07() {
        testBad("Bad-07", "RaptorTurtle/bad-07.ttl");
    }

    @Test
    public void test_bad_08() {
        testBad("Bad-08", "RaptorTurtle/bad-08.ttl");
    }

    @Test
    public void test_bad_09() {
        testBad("Bad-09", "RaptorTurtle/bad-09.ttl");
    }

    @Test
    public void test_bad_10() {
        testBad("Bad-10", "RaptorTurtle/bad-10.ttl");
    }

    @Test
    public void test_bad_11() {
        testBad("Bad-11", "RaptorTurtle/bad-11.ttl");
    }

    @Test
    public void test_bad_12() {
        testBad("Bad-12", "RaptorTurtle/bad-12.ttl");
    }

    @Test
    public void test_bad_13() {
        testBad("Bad-13", "RaptorTurtle/bad-13.ttl");
    }

    private static void testBad(String name, String inputFile) {
        String fn = "file:testing/Turtle/"+inputFile;
        Model model = ModelFactory.createDefaultModel();
        RDFReaderI t = new TurtleTestReader();
        assertThrows(TurtleParseException.class, ()->t.read(model, fn));
    }

    private void testGood(String name, String inputFile, String outputFile) {
        testGood(name, inputFile, outputFile, null);
    }

    private void testGood(String name, String inputFile, String outputFile, String baseURI) {
        inputFile = "file:testing/Turtle/"+inputFile;
        outputFile = "file:testing/Turtle/"+outputFile;

        Model model = ModelFactory.createDefaultModel();
        RDFReaderI t = new TurtleTestReader();
        if ( baseURI != null )
            t.read(model, FileManager.getInternal().open(inputFile), baseURI);
        else
            t.read(model, inputFile);
        String syntax = FileUtils.guessLang(outputFile, FileUtils.langNTriple);
        Model results = ModelFactory.createDefaultModel();
        results.read(outputFile, null, syntax);
        boolean b = model.isIsomorphicWith(results);
        assertTrue(b, "Models not isomorphic");
    }
}
