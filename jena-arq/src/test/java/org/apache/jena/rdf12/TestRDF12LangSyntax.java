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

package org.apache.jena.rdf12;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.turtlejcc.TurtleJCC;
import org.apache.jena.riot.system.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.IsoMatcher;

/**
 * Testing of RDF 1.2 parsing.
 * Also - compare the output of Lang.TRURLE and TurtleJCC.TTL.JCC.
 */
public class TestRDF12LangSyntax {
    // -- The tests

    // Details of a test entry - it may contain information relevant to only some kinds of test.
    record OneTest(String label, Outcome testType,  Lang[] langs, String str) {};

    static String PREFIXES = """
            PREFIX  :     <http://example/>
            PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            """;

    private static Lang[] langsTurtle = { Lang.TURTLE, TurtleJCC.TTLJCC, Lang.TRIG };
    private static Lang[] langsTriG = { Lang.TRIG };
    // Feed to all places where N-triples is legal syntax
    private static Lang[] langsNTriples = { Lang.NT , Lang.NQ, Lang.TURTLE, TurtleJCC.TTLJCC};
    private static Lang[] langsNQuads = { Lang.NQ };

    // -- Shorthand functions
    private static OneTest testTurtle(String label, Outcome testType, String str) {
        String turtleText = PREFIXES + str;
        return testLangs(label, testType, langsTurtle, turtleText);
    }

    private static OneTest testTriG(String label, Outcome testType, String str) {
        String text = PREFIXES + str;
        return testLangs(label, testType, langsTriG, text);
    }


    private static OneTest testNTriples(String label, Outcome testType, String str) {
        return testLangs(label, testType, langsNTriples, str);
    }

    private static OneTest testNQuads(String label, Outcome testType, String str) {
        return testLangs(label, testType, langsNQuads, str);
    }

    // -- Shorthand functions

    private static OneTest testLangs(String label, Outcome testType, Lang[] langs, String str) {
        return new OneTest(label, testType, langs, str);
    }

    /**
     * All tests, all details.
     * <p>
     * These are then converted into parser tests and also output tests that get run by JUnit5.
     */
    private static Stream<OneTest> allTests() {
        return Stream.of(ntuplesTests(), turtleTests(), trigTests()).flatMap(x->x);
    }

    private static Stream<OneTest> turtleTests() {
        return Stream.of(
                 // Triple terms
                 testTurtle("tt1", Outcome.GOOD, ":s :o <<( :x :y :z )>>. "),
                 testTurtle("tt2", Outcome.GOOD, ":s :o <<( :x :y <<( :x1 :y1 :z1 )>> )>>. "),

                 // Reified triple declarations.
                 testTurtle("ReifierTripleDecl_1", Outcome.GOOD, "<< :s :p :o >> ."),
                 testTurtle("ReifierTripleDecl_2", Outcome.GOOD, "<< <<:s :p :o >> :p1 :o1 >> ."),
                 testTurtle("ReifierTripleDecl_3", Outcome.GOOD, "<< :s1 :p1 << :s :p :o >> >> ."),
                 testTurtle("ReifierTripleDecl_4", Outcome.GOOD, "<< :s :p :o ~:r >> ."),
                 testTurtle("ReifierTripleDecl_5", Outcome.GOOD, "<< :s :p :o ~_:b >> ."),
                 testTurtle("ReifierTripleDecl_6", Outcome.GOOD, "<< :s :p :o ~ >> ."),
                 testTurtle("ReifierTripleDecl_7", Outcome.GOOD, "<< :s :p :o ~[] >> ."),

                 testTurtle("ReifierTripleDecl_bad_1", Outcome.BAD, "<< :s :p :o ~ :r1 :r2 >> ."),
                 testTurtle("ReifierTripleDecl_bad_1", Outcome.BAD, "<< :s :p :o ~ :r1 ~:r2 >> ."),

                 // Reified triple
                 testTurtle("ReifiedTriple_1", Outcome.GOOD, "<<:s :p :o >> :q :z ."),
                 testTurtle("ReifiedTriple_2", Outcome.GOOD, ":a :b << :s :p :o >> ."),
                 testTurtle("ReifiedTriple_3", Outcome.GOOD, "<< << :s :p :o >> :p1 :o1 >> :q :z ."),
                 testTurtle("ReifiedTriple_4", Outcome.GOOD, "<< :s0 :p0 <<:s :p :o >>  >> :q :z ."),

                 // Reified triple with reifier
                 testTurtle("ReifiedTriple_5", Outcome.GOOD, "<< :s :p :o ~_:b >> :q :z ."),
                 testTurtle("ReifiedTriple_6", Outcome.GOOD, "<< :s :p :o ~ >> :q :z ."),
                 testTurtle("ReifiedTriple_7", Outcome.GOOD, "<< <<:s :p :o ~ :r1 >> :p1 :o1 ~:r2>> ."),
                 testTurtle("ReifiedTriple_8", Outcome.BAD, "<< :s :p :o ~ :r1 :r2 >> :q :z ."),

                 // Annotations
                 testTurtle("ann1", Outcome.GOOD, ":x :y :z ~:r ."),
                 testTurtle("ann2", Outcome.GOOD, ":s :p :o {| :q :z |} ."),
                 testTurtle("ann3", Outcome.GOOD, ":s :p :o ~:r1 {| :q :z |} ."),
                 testTurtle("ann4", Outcome.GOOD, ":x :y :z ~:r1 ~:r2 ."),
                 testTurtle("ann5", Outcome.GOOD, ":s :p :o {| :q :z |} {| :q1 :z1 |} ."),
                 testTurtle("ann6", Outcome.GOOD, ":s :p :o ~:r1 ~:r2 {| :q :z |} ."),
                 testTurtle("ann7", Outcome.GOOD, ":s :p :o ~:r1 ~:r2 {| :q :z |} {| :q1 :z1 |} ."),
                 testTurtle("ann8", Outcome.GOOD, ":s :p :o ~:r1 ~:r2 {| :q :z |} {| :q1 :z1 |} ~:r3 ~:r4  ."),

                 // With spec fix.
                 testTurtle("Turtle Bad Reification 1", Outcome.BAD, "<< :s :p ( 1 2 3 ) >> . "),
                 testTurtle("Turtle Bad Reification 2", Outcome.BAD, "<< ( 1 2 3 ) :p :o >> . "),
                 testTurtle("Turtle Bad Reification 3", Outcome.BAD, "<< :s :p [ :y :z ] >> . "),

                 // Old syntax, not legal.
                 testTurtle("OldReifierSyntax_1", Outcome.BAD, ":s :p :o | :r ."),
                 testTurtle("OldReifierSyntax_2", Outcome.BAD, "<< :s :p :o | :r >> ."),

                 // Test direction
                 testTurtle("Turtle Text dir 1", Outcome.GOOD, "<http://example/s> <http://example/p> \"abc\"@en--ltr ."),
                 testTurtle("Turtle Text dir bad 1", Outcome.BAD, "<http://example/s> <http://example/p> \"abc\"@en-en--LTR ."),
                 testTurtle("Turtle Text dir bad 2", Outcome.BAD, "<http://example/s> <http://example/p> \"abc\"@en-en--ABC ."),
                 testTurtle("Turtle Text dir bad 3", Outcome.BAD, "\"abc\"@en--ltr <http://example/q> <http://example/o> .")
                 );
    }

    private static Stream<OneTest> trigTests() {
        return Stream.of(
                 testTriG("TriG graph 1" , Outcome.GOOD, "GRAPH <http://example/g> {  << :s :p :o >> .}"),
                 testTriG("TriG graph 2" , Outcome.GOOD, "<http://example/g> {  << :s :p :o >> .}"),
                 testTriG("TriG graph bad 1" , Outcome.BAD, "GRAPH <<( :x :y :z )>>. {  :s :p :o  .}"),
                 testTriG("TriG graph bad 2" , Outcome.BAD, "<<( :x :y :z )>>. {  :s :p :o  .}")
                );
    }

    private static Stream<OneTest> ntuplesTests() {
        return Stream.of(
                 testNTriples("NT TripleTerm 1", Outcome.GOOD, "<http://example/s> <http://example/p> <<( <http://example/s> <http://example/p> <http://example/s> )>> ."),
                 testNTriples("NT TripleTerm 2", Outcome.BAD, "<<( <http://example/s> <http://example/p> <http://example/s> )>> <http://example/q> <http://example/o> ."),

                 testNTriples("NT Text dir 1", Outcome.GOOD, "<http://example/s> <http://example/p> \"abc\"@en--ltr ."),
                 testNTriples("NT Text dir bad 1", Outcome.BAD, "<http://example/s> <http://example/p> \"abc\"@en-en--LTR ."),
                 testNTriples("NT Text dir bad 2", Outcome.BAD, "<http://example/s> <http://example/p> \"abc\"@en-en--ABC ."),
                 testNTriples("NT Text dir bad 3", Outcome.BAD, "\"abc\"@en--ltr <http://example/q> <http://example/o> ."),

                 testNQuads("NQ 1", Outcome.GOOD,
                         "<http://example/s> <http://example/p> <<( <http://example/s> <http://example/p> <http://example/o> )>> <http://example/g> ."),
                 testNQuads("NQ 1", Outcome.BAD,
                         "<http://example/s> <http://example/p> <http://example/s>  <<( <http://example/g1> <http://example/g2> <http://example/g3> )>> .")
                );
    }

    // Test framework : data syntax parsers

    @ParameterizedTest(name = "{0}")
    @MethodSource("allParserTests")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestParsers {}

    // JUnit source for @TestParsers (syntax tests)
    static Stream<Arguments> allParserTests() {
        return allTests().flatMap(item->{
            return Arrays.stream(item.langs).map(lang->parserTestArgs(item, lang));
        });
    }

    // Arguments for one syntax test
    static Arguments parserTestArgs(OneTest item, Lang lang) {
        String label = String.format("%s (%s)", item.label, lang.getLabel());
        return Arguments.of(label, item.testType, item.str, lang);
    }

    @TestParsers
    public void parseLang12(String label, Outcome testType, String str, Lang lang) {
        testLangParse(testType, label, str, lang, false);
    }

    // Syntax test for one lang
    private static void testLangParse(Outcome testType, String label, String turtleText, Lang lang, boolean verbose) {
        try {
            if ( !verbose ) {
                if ( RDFLanguages.isQuads(lang) ) {
                    DatasetGraph dsg = RDFParser.fromString(turtleText, lang).errorHandler(errorHandlerSilent).toDatasetGraph();
                } else {
                    Graph graph = RDFParser.fromString(turtleText, lang).errorHandler(errorHandlerSilent).toGraph();
                }
            } else {
                printString(label, lang, turtleText);
                StreamRDF dest = StreamRDFWriter.getWriterStream(System.out, RDFFormat.TURTLE_FLAT);
                RDFParser.fromString(turtleText, TurtleJCC.TTLJCC).parse(dest);
            }
            if ( testType == Outcome.BAD ) {
                if ( !verbose )
                    printString(label, lang, turtleText);
                fail("Expected failure", null);
            }
            return;
        } catch (RiotException ex) {
            if ( testType == Outcome.BAD )
                // Expected.
                return;
            printString(label, lang, turtleText);
            String exMsg = parserErrorMessage(ex);
            System.out.println("**** "+exMsg);
            throw ex;
        } catch (JenaException ex) {
            if ( testType == Outcome.BAD )
                // Expected.
                return;
            printString(label, lang, turtleText);
            String exMsg = parserErrorMessage(ex);
            System.out.println("**** "+exMsg);
            throw ex;

        } catch (Throwable ex) {
            if ( verbose )
                printString(label, lang, turtleText);
            ex.printStackTrace();
            throw ex;
        }
    }

    // ---- Output tests for Turtle.
    // Run both Turtle parsers and compare the graphs produced.
    // Does not cover the case of both throwing an exception.

    // JUnit source for @TestOutput (Turtle: eval and compare both parsers)
    static Stream<Arguments> allOutputTests() {
        //Only Turtle
        return turtleTests().filter(item-> item.testType == Outcome.GOOD).map(item->outputTestArgs(item));
    }

    // Arguments for one output test
    static Arguments outputTestArgs(OneTest item) {
        return Arguments.of(item.label, item.str);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allOutputTests")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestOutput {}

    @TestOutput
    public void outputTurtle12(String label, String turtleText) {
        testTurtleOutput(label, turtleText);
    }

    // Output test : run Lang.TURLE and TurtleJCC.TTLJCC and compare.
    private void testTurtleOutput(String label, String turtleText) {
        Graph graph1 = parse(turtleText, Lang.TURTLE);
        if ( graph1 == null ) {
            fail("Parsing with Lang.TURTLE produced an error");
            return;
        }
        Graph graph2 = parse(turtleText,  TurtleJCC.TTLJCC);
        if ( graph2 == null ) {
            fail("Parsing with TurtleJCC.TTLJCC produced an error");
            return;
        }
        if ( IsoMatcher.isomorphic(graph1, graph2) ) {
//            printString(turtleText);
//            //System.out.println("** Same");
//            if ( false )
//                parsePrintOutput(turtleText, Lang.TURTLE);
//            System.out.println();
            return;
        }
        System.out.println("** Different graphs");
        printString(turtleText);
        parsePrintOutput(turtleText, TurtleJCC.TTLJCC);
        parsePrintOutput(turtleText, Lang.TURTLE);
        System.out.println();
        fail("Different graphs");
    }

    // Parse. Null if bad.
    private static Graph parse(String parseStr, Lang lang) {
        try {
            Graph graph = GraphMemFactory.createDefaultGraph();
            StreamRDF sink = StreamRDFLib.graph(graph);
            RDFParser.fromString(parseStr, lang).errorHandler(errorHandlerSilent).parse(graph);
            return graph;
        } catch (Exception ex) {
//            // Failed.
//            printString(parseStr);
            parsePrintOutput(parseStr, lang);
            return null;
        }
    }

    // General machinery

    private static ErrorHandler errorHandlerSilent = ErrorHandlerFactory.errorHandlerNoLogging;

    private static String parserErrorMessage(JenaException ex) {
        String exMsg = ex.getMessage();
        int idx = exMsg.indexOf("\n");
        if ( idx >= 0 )
            exMsg = exMsg.substring(0, idx);
        return exMsg;
    }

    // Parse and output
    private static void parsePrintOutput(String parseStr, Lang lang) {
        System.out.println("== "+lang);
        try {
            StreamRDF dest = StreamRDFWriter.getWriterStream(System.out, RDFFormat.TURTLE_FLAT);
            RDFParser.fromString(parseStr, lang).errorHandler(errorHandlerSilent).parse(dest);
        } catch (Exception ex) {
            System.out.flush();
            ex.printStackTrace(System.err);
        }
    }

    private static void printString(String label, Lang lang, String str) {
//        if ( label != null && lang != null )
//            System.out.printf("====  %s (%s)\n", label, lang.getLabel());
//        else if ( label != null )
            System.out.printf("====  %s\n", label);
        printString(str);
    }

    private static void printString(String str) {
        System.out.printf("====          1         2\n");
        System.out.printf("====  12345789_123456789_123456789\n");
        String[] x = str.split("\n");
        for ( int i = 0 ; i < x.length ; i++ ) {
            System.out.printf("%2d :: %s\n", i+1, x[i]);
        }
    }
}
