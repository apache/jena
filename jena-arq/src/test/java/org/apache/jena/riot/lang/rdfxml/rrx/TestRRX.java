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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.rdfxml.RRX;
import org.apache.jena.riot.lang.rdfxml.rrx.RunTestRDFXML.ErrorHandlerCollector;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Additional tests for RRX:
 * <ul>
 * <li>errors and warnings not in the W3C manifest files</li>
 * <li>additional reports</li>
 * <li>extensions toRDF/XML</li>
 * </ul>
 */

@RunWith(Parameterized.class)
public class TestRRX {

    private static String DIR = "testing/RIOT/rrx-files/";

    @Parameters(name = "{index}: {0} {1}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>();
        x.add(new Object[] {"SAX", RRX.RDFXML_SAX});
        x.add(new Object[] {"StAXsr", RRX.RDFXML_StAX_sr});
        x.add(new Object[] {"StAXev", RRX.RDFXML_StAX_ev});
        return x;
    }

    private String label;
    private Lang lang;

    public TestRRX(String label, Lang lang) {
        this.label = label;
        this.lang = lang;
    }

    // Test2 for more than one object in RDF/XML striping.
    @Test public void error_multiple_objects_lex_node() {
        errorTest("multiple_objects_lex_node.rdf");
    }

    @Test public void error_multiple_objects_node_lex() {
        errorTest("multiple_objects_node_lex.rdf");
    }

    @Test public void error_multiple_objects_node_node() {
        errorTest("multiple_objects_node_node.rdf");
    }

    // Check that the "one object" parse state does not impact deeper structures.
    @Test public void nested_object() {
        goodTest("nested_object.rdf");
    }

    // rdf:parserType=
    @Test public void error_parseType_unknown() {
        // This is only a warning in ARP.
        errorTest("parseType-unknown.rdf", false);
    }

    @Test public void warn_parseType_extension_1() {
        // Now valid. parseType="literal" -> parseType="Literal"
        // because ARP behaved that way.
        // Warning issued.
        warningTest("warn01.rdf", 1);
    }

    // CIM
    @Test public void cim_statements01() {
        // parseType="Statements"
        // because ARP behaved that way.
        //errorTest("error02.rdf");
        // Warning issued.
        warningTest("cim_statements01.rdf", 2);
    }

    // misc
    @Test public void noBase01() {
        // Call with no base; no base needed.
        noBase("file-no-base.rdf");
    }

    @Test(expected=RiotException.class)
    public void noBase02() {
        // Call with no base; a base is needed => exception.
        noBase("file-external-base.rdf");
    }

    private void noBase(String filename) {
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        ErrorHandlerCollector errorHandler = new ErrorHandlerCollector();
        ParserProfile parserProfile = RiotLib.createParserProfile(RiotLib.factoryRDF(), errorHandler, true);
        ReaderRIOT reader = factory.create(lang, parserProfile);
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        try ( InputStream in = IO.openFile(fn) ) {
            reader.read(in, null, WebContent.ctRDFXML, dest, RIOT.getContext().copy());
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
    }

    private void goodTest(String filename) {
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        RunTestRDFXML.runTestPlain(filename, factory, label, fn);
        RunTestRDFXML.runTestCompareARP(fn, factory, label, fn);
    }

    private void warningTest(String filename, int warnings) {
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        RunTestRDFXML.runTestExpectWarning(filename, factory, label, warnings, fn);
        RunTestRDFXML.runTestCompareARP(fn, factory, label, fn);
    }

    private void errorTest(String filename) {
        errorTest(filename, true);
    }

    private void errorTest(String filename, boolean compare) {
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        RunTestRDFXML.runTestExpectFailure(filename, factory, label, fn);
        if ( compare )
            RunTestRDFXML.runTestCompareARP(fn, factory, label, fn);
    }
}
