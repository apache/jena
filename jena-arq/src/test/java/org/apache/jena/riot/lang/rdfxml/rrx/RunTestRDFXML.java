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

package org.apache.jena.riot.lang.rdfxml.rrx;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.rdfxml.ReaderRDFXML_ARP1;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.graph.GraphFactory;

/** Ways to run tests on parsers. */
public class RunTestRDFXML {

    static ReaderRIOTFactory arpFactory = ReaderRDFXML_ARP1.factory;

    /**
     * The RDF/XML tests from rdf-tests CG
     * These are also run from {@link org.apache.jena.riot.lang.rdfxml.manifest_rdf11.Scripts_RRX_RDFXML}.
     * Here, the exact warnings and errors are checked.
     */
    static List<String> w3cTestFiles() {
        Path DIR = Path.of("testing/RIOT/rdf11-xml");
        return allTestFiles(DIR);
    }

    static List<String> allTestFiles(Path DIR) {
        if ( !Files.exists(DIR) )
            return List.of();
        try {
            return Files.walk(DIR)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(fn->fn.endsWith(".rdf"))
                .collect(Collectors.toList());
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
    }

    /**
     * Manifest-like in that the test files in a specific order.
     * The local files cover all the features of RDF/XML parsing
     * but not in great depth.
     * These tests more easily highlight problems and the grouping helps.
     */
    static List<String> localTestFiles() {
        Path LOCAL_DIR = Path.of("testing/RIOT/rrx/");
        Set<String> found;
        try {
            found = Files
                    .list(LOCAL_DIR)
                    // Directory relative name.
                    .map(path->path.getFileName().toString())
                    .filter(fn->fn.endsWith(".rdf"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw IOX.exception(e);
        }

        // For a better order ...
        List<String> testfiles = List.of(
                 "xml.rdf", "xml10.rdf", "xml11.rdf",

                 "basic01.rdf",
                 "basic02.rdf",
                 "basic03.rdf",
                 "basic04.rdf",
                 "basic05.rdf",
                 "basic06.rdf",
                 "basic07.rdf",
                 "basic08.rdf",

                 "blankNodeIds01.rdf",
                 "blankNodeIds02.rdf",
                 "blankNodeIds03.rdf",

                 "attribute-property01.rdf",
                 "attribute-property02.rdf",
                 "attribute-property03.rdf",
                 "attribute-property04.rdf",

                 "datatype01.rdf",

                 // Illegal lexical form => warning
                 "datatype02.rdf",

                 "type01.rdf",
                 "type02.rdf",
                 "type03.rdf",

                 // Zero bytes
                 "empty1.rdf",
                 // Empty rdf:RDF
                 "empty2.rdf",
                 // Empty rdf:RDF/
                 "empty3.rdf",

                 "base01.rdf",
                 "base02.rdf",
                 "base03.rdf",
                 "base04.rdf",
                 "base05.rdf",
                 "base06.rdf",

                 "lang01.rdf",
                 "lang02.rdf",
                 "lang03.rdf",
                 "lang04.rdf",

                 "objects01.rdf",
                 "objects02.rdf",

                 "striped01.rdf",
                 "striped02.rdf",
                 "striped03.rdf",
                 "striped04.rdf",
                 "striped05.rdf",

                 "containers01.rdf",
                 "containers02.rdf",
                 "containers03.rdf",
                 "containers04.rdf",

                 "xmlliteral01.rdf",
                 "xmlliteral02.rdf",
                 "xmlliteral03.rdf",
                 "xmlliteral04.rdf",
                 "xmlliteral05.rdf",
                 "xmlliteral06.rdf",
                 "xmlliteral07.rdf",

                 "collections01.rdf",
                 "collections02.rdf",
                 "collections03.rdf",
                 "collections04.rdf",
                 "collections05.rdf",
                 "collections06.rdf",

                 "reification01.rdf",
                 "reification02.rdf",
                 "reification03.rdf",
                 "reification04.rdf",
                 "reification05.rdf",

                 "entities01.rdf",
                 "entities02.rdf",

                 "comments01.rdf",
                 "comments02.rdf"
                 );

        for ( String fn : testfiles ) {

            if ( ! found.contains(fn) )
                output.printf("Not found in file area: %s\n", fn);
        }

        for ( String fn : found ) {
            if ( ! testfiles.contains(fn) )
                output.printf("Not listed as a test: %s\n", fn);
        }

        if ( found.size() != testfiles.size() )
            throw new RuntimeException(format("Found: %d :: Listed: %d", found.size(), testfiles.size()));

        // Convert to absolute, in order
        List<String> testfilesAbs = testfiles.stream()
                // If not enabling entity support in StAX
                //.filter(fn-> ( xmlParserType != XMLParser.StAX || !fn.startsWith("entities") ) )
                .map(fn->LOCAL_DIR.resolve(fn).toString())
                .collect(Collectors.toList());

        return testfilesAbs;
    }

    static List<Object[]> makeTestSetup(List<String> testfiles, String label) {
        List<Object[]> x = new ArrayList<>();
        for ( String fn : testfiles ) {
            //System.out.println(fn);
            x.add(new Object[] {label, fn});
        }
        return x;
    }

    static void runTest(String label, ReaderRIOTFactory factory, String implLabel, String filename) {
        try {
            runTestCompareARP(label, factory, implLabel, filename);
        } catch(Throwable ex) {
            throw new RuntimeException(filename, ex) {
                @Override
                public Throwable fillInStackTrace() { return this; }
            };
        }
    }

    static class ErrorHandlerCollector implements ErrorHandler {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> fatals = new ArrayList<>();

        @Override
        public void warning(String message, long line, long col) {
            warnings.add(message);
        }

        @Override
        public void error(String message, long line, long col) {
            errors.add(message);
            throw new RiotException(message);
        }

        @Override
        public void fatal(String message, long line, long col) {
            fatals.add(message);
            throw new RiotException(message);
        }

        public boolean anySet() {
            return ! ( warnings.isEmpty() && errors.isEmpty() && fatals.isEmpty() );
        }

        public void reset() {
            warnings.clear();
            errors.clear();
            fatals.clear();
        }

        public String summary() {
            if ( fatals.isEmpty() )
                return format("E:%d W:%d", errors.size(), warnings.size(), fatals.size());
            return format("E:%d W:%d F:%d", errors.size(), warnings.size(), fatals.size());
        }

        public void print(PrintStream out) {
            warnings.forEach(s -> out.println("W: " + s));
            errors.forEach(s -> out.println("E: " + s));
            fatals.forEach(s -> out.println("F: " + s));
        }
    }

    // If true, print if there were any warnings/errors. If false, print only if different counts.
    static boolean PrintAll = false;
    static PrintStream output = System.out;

    /**
     * Run a test comparing the outcome to ARP. Check the output graphs or expect a parse exception from both implementations.
     */
    public static void runTestCompareARP(String label, ReaderRIOTFactory factory, String implLabel, String filename) {
        runTestCompare(label, factory, implLabel, arpFactory, "ARP", filename);
    }

    /**
     * Run a test using two implementations, a "reference" and a "test subject".
     * Compare the output graphs or expect a parse exception from both implementations.
     * The error handles are also check for the same number of warnings and errors.
     */
    public static void runTestCompare(String testLabel,
                                      ReaderRIOTFactory testSubjectFactory, String subjectLabel,
                                      ReaderRIOTFactory referenceFactory, String referenceLabel,
                                      String filename) {
        String testFullLabel = format("-- Test : %-4s : %s", testLabel, filename);

        Graph expectedGraph;
        // -- "Reference" implementation
        ErrorHandlerCollector errorHandlerReference = new ErrorHandlerCollector();
        try {
            expectedGraph = parseFile(referenceFactory, errorHandlerReference, filename);
        } catch (RiotException ex) {
            // Exception expected. Run as "failure test"
            runTestExpectFailure(testLabel, testSubjectFactory, subjectLabel, filename,
                                 errorHandlerReference);
            return;
        }

        runTestExpectGraph(testLabel, testSubjectFactory, subjectLabel, expectedGraph, filename, errorHandlerReference);
    }


    /**
     * Run a test, expecting a graph as the result.
     */
    static void runTestExpectGraph(String testLabel,
                                   ReaderRIOTFactory testSubjectFactory, String subjectLabel,
                                   Graph expectedGraph, String filename) {
        runTestExpectGraph(testLabel, testSubjectFactory, subjectLabel, expectedGraph, filename, null);
    }

    /** Run a test expecting a RiotException. */
    static void runTestExpectFailure(String testLabel,
                                     ReaderRIOTFactory testSubjectFactory, String subjectLabel,
                                     String filename) {
        ErrorHandlerCollector actualErrorHandler = new ErrorHandlerCollector();
        assertThrows(RiotException.class, ()->{
            parseFile(testSubjectFactory, actualErrorHandler, filename);
            output.printf("## Expected RiotExpection : %-4s : %s : %s", subjectLabel, testLabel, filename);
        });
        checkErrorHandler(testLabel, actualErrorHandler, -1, 1, -1);
    }

    /**
     * Run a test, expecting a graph as the result.
     * Compare with the expected error handler if that argument is not null.
     */
    private static void runTestExpectGraph(String testLabel,
                                           ReaderRIOTFactory testSubjectFactory, String subjectLabel,
                                           Graph expectedGraph, String filename,
                                           ErrorHandlerCollector expectedErrorHandler) {
        ErrorHandlerCollector errorHandlerTest = new ErrorHandlerCollector();
        try {
            Graph actualGraph = parseFile(testSubjectFactory, errorHandlerTest, filename);
            // "same" graph output?
            if ( ! expectedGraph.isIsomorphicWith(actualGraph) ) {
                output.println("---- "+testLabel+" : "+filename);
                output.println("==== Expected");
                RDFWriter.source(expectedGraph).lang(Lang.NT).output(output);
                output.println("==== "+subjectLabel);
                RDFWriter.source(actualGraph).lang(Lang.NT).output(output);
                output.println("----");
                output.println();
                fail("Graph1 not isomorphic to graph2");
            }
            if ( expectedErrorHandler != null )
                checkErrorHandler(testLabel, expectedErrorHandler, errorHandlerTest);
            return;
        } catch(RiotException ex) {
            output.println("## "+testLabel);
            ex.printStackTrace();
            fail("Unexpected parse error: "+ex.getMessage());
        }
    }

    /**
     * Run a test, expecting a graph as the result.
     * Compare with the expected error handler if that argument is not null.
     */
    private static void runTestExpectSuccess(String testLabel,
                                             ReaderRIOTFactory testSubjectFactory, String subjectLabel,
                                             String filename,
                                             ErrorHandlerCollector expectedErrorHandler) {
        ErrorHandlerCollector errorHandlerTest = new ErrorHandlerCollector();
        try {
            Graph actualGraph = parseFile(testSubjectFactory, errorHandlerTest, filename);
            if ( expectedErrorHandler != null )
                checkErrorHandler(testLabel, expectedErrorHandler, errorHandlerTest);
            return;
        } catch(RiotException ex) {
            output.println("## "+testLabel);
            ex.printStackTrace();
            fail("Unexpected parse error: "+ex.getMessage());
        }
    }


    /** Run a test expecting a RiotException. Check the error handler. */
    private static void runTestExpectFailure(String testLabel,
                                     ReaderRIOTFactory testSubjectFactory, String subjectLabel,
                                     String filename, ErrorHandlerCollector expectedErrorHandler) {
        ErrorHandlerCollector actualErrorHandler = new ErrorHandlerCollector();
        assertThrows(RiotException.class, ()->{
            parseFile(testSubjectFactory, actualErrorHandler, filename);
            output.printf("## Expected RiotExpection : %-4s : %s : %s", subjectLabel, testLabel, filename);
        });

        if ( expectedErrorHandler != null )
            checkErrorHandler(testLabel, expectedErrorHandler, actualErrorHandler);
    }

    private static void checkErrorHandler(String testLabel, ErrorHandlerCollector errorHandlerReference, ErrorHandlerCollector errorHandlerActual) {
        // Check errors and warnings.
        boolean sameCounts =
                errorHandlerReference.warnings.size() == errorHandlerActual.warnings.size() &&
                errorHandlerReference.errors.size() == errorHandlerActual.errors.size( ) &&
                errorHandlerReference.fatals.size() == errorHandlerActual.fatals.size( );
        if ( ! sameCounts ) {
            // Error handlers different counts.
            if ( testLabel == null )
                testLabel = "Test";
            output.println("== "+testLabel);
            if ( errorHandlerReference.warnings.size() != errorHandlerActual.warnings.size( ) )
                output.println("** Warnings different");
            if ( errorHandlerReference.errors.size() != errorHandlerActual.errors.size( ) )
                output.println("** Errors different");
            if ( errorHandlerReference.fatals.size() != errorHandlerActual.fatals.size( ) )
                output.println("** Fatals different");
            output.printf("Expected -- %s\n", errorHandlerReference.summary());
            errorHandlerReference.print(output);

            output.printf("Actual -- %s\n", errorHandlerActual.summary());
            errorHandlerActual.print(output);
        }

        if ( PrintAll ) {
            output.println("Expected error handler");
            errorHandlerReference.print(output);
            output.println("Actual error handler");
            errorHandlerActual.print(output);
            output.println();
        }
    }

    /** Counts check of an error handler */
    private static void checkErrorHandler(String testLabel, ErrorHandlerCollector errorHandler, int countWarnings, int countErrors, int countFatals) {
        if ( countFatals >= 0 )
            assertEquals("Fatal message counts different", countWarnings, errorHandler.fatals.size());
        if ( countErrors >= 0 )
            assertEquals("Error message counts different", countErrors, errorHandler.errors.size());
        if ( countWarnings >= 0 )
            assertEquals("Warning message counts different", countWarnings, errorHandler.warnings.size());
    }

    /** Parse one file using a reader of the give factory */
    private static Graph parseFile(ReaderRIOTFactory factory, ErrorHandler errorHandler, String filename) {
        //ParserProfile parserProfile = RiotLib.dftProfile();
        ParserProfile parserProfile = RiotLib.createParserProfile(RiotLib.factoryRDF(), errorHandler, true);
        ReaderRIOT reader = factory.create(Lang.RDFXML, parserProfile);
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        try ( InputStream in = IO.openFile(filename) ) {
            reader.read(in, "http://external/base", WebContent.ctRDFXML, dest, RIOT.getContext().copy());
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
        return graph;
    }
}
