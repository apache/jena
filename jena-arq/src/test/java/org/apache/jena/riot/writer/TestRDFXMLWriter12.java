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

package org.apache.jena.riot.writer;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sys.JenaSystem;

/** RDFXML writer tests using Turtle to setup the test case. */
public class TestRDFXMLWriter12 {

    static { JenaSystem.init(); }

    enum Setup {
        PLAIN(RDFFormat.RDFXML_PLAIN),
        PRETTY(RDFFormat.RDFXML_PRETTY) ;

        private RDFFormat fmt;
        Setup(RDFFormat fmt) { this.fmt = fmt; }
    }

    // No ITS
    static String PREFIXES = """
PREFIX :        <http://example/>
PREFIX ex:      <http://example.org/>
PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>
            """;

    // Text direction.

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void textDirNoITS(Setup setup) {
        runTest(":s :p 'abc'@en--ltr .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void textDirWithITS(Setup setup) {
        // With its:
        runTest("PREFIX its: <http://www.w3.org/2005/11/its> :s :p 'abc'@en--ltr .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void textDirOtherITS(Setup setup) {
        // Someother use of "its:"
        runTest("PREFIX its: <http://example/ITS/> :s :p 'abc'@en--ltr .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void textDirTripleTermNoITS(Setup setup) {
        runTest(":s :p <<( :x :y 'abc'@en--ltr)>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void textDirTripleTermWithITS(Setup setup) {
        runTest("PREFIX its: <http://www.w3.org/2005/11/its> :s :p <<( :x :y 'abc'@en--ltr)>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void textDirTripleTermOtherITS(Setup setup) {
        runTest("PREFIX its: <http://example/ITS/> :s :p <<( :x :y 'abc'@en--ltr)>> .", setup.fmt);
    }

    // ---- Triple Term Basics

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjIRI(Setup setup) {
        runTest(":s :p <<( :x :y :z )>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjNumber(Setup setup) {
        runTest(":s :p <<( :x :y 123 )>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjBNode(Setup setup) {
        runTest(":s :p <<( :x :y _:x )>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjString(Setup setup) {
        runTest(":s :p <<( :x :y 'ABC' )>>", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjLangString(Setup setup) {
        runTest(":s :p <<( :x :y 'ABC'@en )>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjDirLangString(Setup setup) {
        runTest(":s :p <<( :x :y 'ABC'@en-ltr )>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicObjTripleterm(Setup setup) {
        runTest(":s :p <<( _:x :y <<( :a :b :c )>> )>> .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttBasicSubjBNode(Setup setup) {
        runTest(":s :p <<( _:x :y :z )>>", setup.fmt);
    }

    // BNode connectivity tests (not triple terms)

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodePlain01(Setup setup) {
        runTest("_:b :pp :bb .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodePlain02(Setup setup) {
        runTest(":ss :pp _:b .", setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodePlain03(Setup setup) {
        runTest("_:b :pp _:b .", setup.fmt);
    }

    // ---- Triple terms and blank node sharing.

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape11(Setup setup) {
        String bnodeShape11 = """
                _:a :pp _:a .
                :xx :qq <<( :s :pp _:b )>> .
                """;
        runTest(bnodeShape11, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape12(Setup setup) {
        // Share in triple term
        String bnodeShape12 = """
                _:a :pp :bb .
                :xx :qq <<( _:b :pp _:b )>> .
                """;
        runTest(bnodeShape12, setup.fmt);
    }

    // ----
    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape20(Setup setup) {
        // Share subjects
        String bnodeShape20 = """
                _:b :pp :bb .
                :xx :qq <<( _:b :pp "ABC" )>> .
                """;

        runTest(bnodeShape20, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape21(Setup setup) {
        String bnodeShape21 = """
                _:b :pp :bb .
                :xx :qq <<( :s :pp _:b )>> .
                """;
        runTest(bnodeShape21, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape22(Setup setup) {
        String bnodeShape22 = """
                :ss :pp _:b .
                :xx :qq <<( _:b :pp "ABC" )>> .
                """;
        runTest(bnodeShape22, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape23(Setup setup) {
        // Shared objects
        String bnodeShape23 = """
                :ss :pp _:b .
                :xx :qq <<( :s :pp _:b )>> .
                """;

        runTest(bnodeShape23, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape24(Setup setup) {
        String bnodeShape24 = """
                _:b :pp _:b .
                :xx :qq <<( _:b :pp _:b )>> .
                """;
        runTest(bnodeShape24, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void bnodeShape25(Setup setup) {
        // Shared, common subject
        String bnodeShape25 = """
                :xx :dd _:b .
                :xx :qq <<( _:b :pp _:b )>> .
                """;
        runTest(bnodeShape25, setup.fmt);
    }

    //---- Namespaces

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttNamespace1(Setup setup) {
        // Different namespace inside, but in the prefix map.
        String ttNamespace1 = """
                :xx :dd _:b .
                :xx :qq <<( _:b ex:pp _:b )>> .
                """;
        runTest(ttNamespace1, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttNamespace2(Setup setup) {
        // Unmentioned namespace both outside and inside
        String ttNamespace2 = """
                :xx <http://ex/p> _:b .
                :xx :qq <<( _:b <http://ex/pp> _:b )>> .
                """;
        runTest(ttNamespace2, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttNamespace3(Setup setup) {
        // Unmentioned namespace inside only
        String ttNamespace3 = """
                :xx :qq <<( _:b <http://ex/pp> _:b )>> .
                """;
        runTest(ttNamespace3, setup.fmt);
    }

    @ParameterizedTest
    @EnumSource(Setup.class)
    public void ttNamespace4(Setup setup) {
        // Unmentioned namespaces inside only
        String ttNamespace4 = """
                :xx :qq <<( :ss <http://ns1/p1> _:b )>> .
                :xx :qq <<( :ss <http://ns2/p2> _:b )>> .
                """;
        runTest(ttNamespace4, setup.fmt);
    }

    private static void runTest(String str, RDFFormat format) {
        Graph graph1;

        String parseString = PREFIXES+str;
        try {
            graph1 = RDFParser.fromString(parseString, Lang.TTL).toGraph();
        } catch (RiotException ex) {
            printString(str);
            fail("Can't parse input");
            return;
        }

        String outString;
        try {
            outString = RDFWriter.source(graph1).format(format).asString();
        } catch (Throwable ex) {
            ex.printStackTrace();
            RDFWriter.source(graph1).format(RDFFormat.TTL).output(System.out);
            System.out.println();
            fail("Can't write -- "+ex.getMessage());
            return;
        }

        // And parse the output
        Graph graph2;
        try {
            graph2 = RDFParser.fromString(outString, format.getLang()).toGraph();
        } catch (RiotException ex) {
            ex.printStackTrace();
            fail("Can't parse writtern graph -- "+ex.getMessage());
            return;
        }

        boolean same = IsoMatcher.isomorphic(graph1, graph2);
        //System.out.printf("[%s] Same = %s\n", format, same);
        if ( !same ) {
            System.out.println("==== Expected");
            //RDFWriter.source(graph1).format(format).output(System.out);
            RDFWriter.source(graph1).format(RDFFormat.TURTLE).output(System.out);
            System.out.println("==== Actual");
            RDFWriter.source(graph2).format(RDFFormat.TURTLE).output(System.out);
            System.out.println("== RDF/XML");
            System.out.print(outString);
            fail("Graph not isomorphic");
            return;
        }
    }

    /** Print a multiline string, with line numbers. */
    private static void printString(String str) {
        System.out.printf("====          1         2         3         4\n");
        System.out.printf("====  12345789_123456789_123456789_123456789_\n");
        String[] x = str.split("\n");
        for ( int i = 0 ; i < x.length ; i++ ) {
            System.out.printf("%2d -- %s\n", i+1, x[i]);
        }
        System.out.println("====");
    }
}
