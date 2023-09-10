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

package org.apache.jena.riot.lang.rdfxml.converted_legacy;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.rdfxml.RRX;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.slf4j.Logger;

/** Support for the test suites from jena-core based on files. */

class LibTestARP1 {

    private static String RDF_CORE = "http://www.w3.org/2000/10/rdf-tests/rdfcore/";
    private static String ARP_PREFIX = "http://jcarroll.hpl.hp.com/arp-tests/";

    // "/gw" and "/arp" areas
    static String DIR = "../jena-core/testing";

    static PrintStream output = System.out;

    /** Map URI to local file name */
    static String iriToFile(String uri) {

        if ( uri.startsWith(RDF_CORE) ) {
            return DIR+"/wg/"+uri.substring("http://www.w3.org/2000/10/rdf-tests/rdfcore/".length());
        }
        if ( uri.startsWith(ARP_PREFIX) ) {
            return DIR+"/arp/"+uri.substring("http://jcarroll.hpl.hp.com/arp-tests/".length());
        }
        throw new RuntimeException("Not recognized: "+uri);
    }

    // file type - false - NT, true - RDF
    // Ignore use file extension

    public static void withLogLevel(Logger log, String level, Runnable action) {
        String originalLevel = LogCtl.getLevel(log);
        LogCtl.setLevel(log, level);
        try {
            action.run();
        } finally {
            LogCtl.setLevel(SysRIOT.getLogger(), originalLevel);
        }
    }

    static void positiveTest(String testURI,
                             String inputURI,
                             String outputURI) {
        runTest(testURI, inputURI, outputURI);
    }

    static void runTest(String testURI,
                        String inputURI,
                        String outputURI) {
        String fnInput = iriToFile(inputURI);
        String fnOutput = iriToFile(outputURI);

        if ( ! FileOps.exists(fnInput) )
            System.out.println("Positive input missing: "+fnInput);
        if ( ! FileOps.exists(fnOutput) )
            System.out.println("Positive output Missing: "+fnOutput);

        withLogLevel(SysRIOT.getLogger(),
                     "ERROR", ()->runPositive1(inputURI, fnInput, fnOutput));
    }

    private static void runPositive1(String baseURI, String fnInput, String fnOutput) {
        String label = "RDF: "+fnInput;
        Graph actual;
        try {
            try ( InputStream input = IO.openFile(fnInput) ) {
                // Explicit lang
                actual = RDFParser.source(input).lang(RRX.RDFXML_ARP1).base(baseURI).toGraph();
            } catch (IOException ex) { throw IOX.exception(ex); }
        } catch (RiotException ex) {
            String x = IO.readWholeFileAsUTF8(fnInput);
            System.err.println("Bad: "+fnInput);
            System.err.print(x);
            throw ex;
        }

        Graph expected;
        try {
            expected = RDFParser.source(fnOutput).base(baseURI).toGraph();
        } catch (RiotException ex) {
            throw ex;
        }

        if ( ! actual.isIsomorphicWith(expected) ) {
            output.println("---- "+label+" : "+fnInput);
            output.println("==== Actual");
            RDFWriter.source(actual).lang(Lang.NT).output(output);
            output.println("==== "+label);
            RDFWriter.source(expected).lang(Lang.NT).output(output);
            output.println("----");
            output.println();
            fail("Actual graph not isomorphic to expected graph");
            return;
        }
    }

    static void negativeTest(String testURI,
                             String inputURI
                             ) {
        String fnInput = iriToFile(inputURI);

        if ( ! FileOps.exists(fnInput) )
            System.out.println("Negative input missing: "+fnInput);

        ErrorHandler errorHandler = ErrorHandlerFactory.errorHandlerStrict;

        try {
            // XXX ARP generate warnings rather than errors in some cases.
            // XXX Fix!
            withLogLevel(SysRIOT.getLogger(),
                         "FATAL", ()->{
                             InputStream input = IO.openFile(fnInput);
                             RDFParser.source(input)
                                     .errorHandler(errorHandler)
                                     .lang(RRX.RDFXML_ARP1)
                                     .base(inputURI)
                                     .toGraph();
                         });
            // Should have throw an exception
            Graph actual =  RDFParser.source(fnInput).base(inputURI).toGraph();
            String x = IO.readWholeFileAsUTF8(fnInput);

            String label = "RDF: "+fnInput;
            output.println("---- "+label+" : "+fnInput);
            output.println("==== Actual");
            output.print(x);
            RDFWriter.source(actual).lang(Lang.NT).output(output);
            output.println("----");

            fail("Expected a test to fail: "+fnInput);
        } catch(RiotException ex) {}
    }

    static void warningTest(String testURI,
                            String inputURI,
                            String outputURI) {
        runTest(testURI, inputURI, outputURI);
    }
}
