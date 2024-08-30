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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.rdfxml.RRX;
import org.apache.jena.riot.lang.rdfxml.rrx.RunTestRDFXML.ErrorHandlerCollector;
import org.apache.jena.riot.system.*;
import org.junit.AfterClass;
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

    private static Set<String> processedFiles = new HashSet<>();
    private void trackFilename(String filename) {
        processedFiles.add(filename);
    }

    /** Check all files in the were touched */

    @AfterClass public static void checkFiles() {
        // This can break when running single tests.
        Set<String> fsFiles = localTestFiles();
        if ( fsFiles.size() !=  processedFiles.size()) {
            System.out.flush();
            System.err.flush();
            Set<String> missed = SetUtils.difference(fsFiles, processedFiles);
            System.err.println("Missed files: ");
            missed.forEach(x->System.err.printf("  %s\n",x));
            System.out.flush();
            System.err.flush();
            //Assert.fail();
        }
    }

    // Test for more than one object in RDF/XML striping.
    @Test public void error_multiple_objects_lex_node() {
        checkForErrorCompare("multiple_objects_lex_node.rdf");
    }

    @Test public void error_multiple_objects_node_lex() {
        checkForErrorCompare("multiple_objects_node_lex.rdf");
    }

    @Test public void error_multiple_objects_node_node() {
        checkForErrorCompare("multiple_objects_node_node.rdf");
    }

    // Check that the "one object" parse state does not impact deeper structures.
    @Test public void nested_object() {
        goodTest("nested_object.rdf");
    }

    // rdf:parserType=
    @Test public void error_parseType_unknown() {
        // This is only a warning in ARP.
        checkForError("parseType-unknown.rdf", false);
    }

    @Test public void warn_parseType_extension_1() {
        // Now valid. parseType="literal" -> parseType="Literal"
        // because ARP behaved that way.
        // Warning issued.
        warningTest("parseType-warn.rdf", 1);
    }

    // misc
    @Test public void base_not_needed() {
        // Call with no base; no base needed.
        noBase("base-none.rdf");
    }

    @Test(expected=RiotException.class)
    public void bare_needed() {
        // Call with no base; a base is needed => exception.
        noBase("base-external-needed.rdf");
    }

    @Test(expected=RiotException.class)
    public void base_inner_1() {
        // Call with no base; xml:base is relative in the data.
        noBase("base-inner.rdf");
    }

    public void base_inner_2() {
        // Called external base
        goodTest("base-inner.rdf");
    }

    // CIM
    @Test public void cim_statements01() {
        // parseType="Statements"
        // This is an extension to support CIM XML data.
        // ARP behaved this way.
        // Warning issued.
        warningTest("cim_statements01.rdf", 2);
    }

    @Test public void element_node_rdf_resource_bad() {
        checkForErrorCompare("bad-rdf-resource-node.rdf");
    }

    @Test public void element_node_rdf_id_bad() {
        checkForErrorCompare("bad-rdf-id-node.rdf");
    }

    @Test public void bad_unqualified_property() {
        checkForError("bad-unqualified-property.rdf", false);
    }

    @Test public void bad_unqualified_attribute1() {
        checkForError("bad-unqualified-attribute1.rdf", false);
    }

    @Test public void bad_unqualified_attribute2() {
        checkForError("bad-unqualified-attribute2.rdf", false);
    }

    @Test public void bad_unqualified_attribute3() {
        checkForError("bad-unqualified-attribute3.rdf", false);
    }

    @Test public void bad_unqualified_attribute4() {
        warningTest("bad-unqualified-attribute4.rdf", 1);
    }

    @Test public void bad_unqualified_attribute5() {
        warningTest("bad-unqualified-attribute5.rdf", 1);
    }

    @Test public void bad_unqualified_class() {
        checkForError("bad-unqualified-class.rdf", false);
    }

    @Test public void bad_property_object_1() {
        checkForError("bad-object-type-1.rdf", false);
    }

    @Test public void bad_property_object_2() {
        checkForError("bad-object-type-2.rdf", false);
    }

    @Test public void bad_property_object_3() {
        checkForError("bad-object-type-3.rdf", false);
    }

    /** Parse with no base set by the parser */
    private void noBase(String filename) {
        trackFilename(filename);

        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        ErrorHandlerCollector errorHandler = new ErrorHandlerCollector();
        ParserProfile parserProfile = RiotLib.createParserProfile(RiotLib.factoryRDF(), errorHandler, true);
        ReaderRIOT reader = factory.create(lang, parserProfile);
        StreamRDF dest = false
                ? StreamRDFWriter.getWriterStream(System.out, RDFFormat.TURTLE_FLAT)
                : StreamRDFWriter.getWriterStream(System.out, RDFFormat.RDFNULL);
        try ( InputStream in = IO.openFile(fn) ) {
            reader.read(in, null/* No base*/, WebContent.ctRDFXML, dest, RIOT.getContext().copy());
        } catch (RiotException ex) {
            throw ex;
        } catch (IOException ex) {
            throw IOX.exception(ex);
        }
    }

    private void goodTest(String filename) {
        trackFilename(filename);
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        RunTestRDFXML.runTestPlain(filename, factory, label, fn);
        RunTestRDFXML.runTestCompareARP(fn, factory, label, fn);
    }

    private void warningTest(String filename, int warnings) {
        trackFilename(filename);
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        RunTestRDFXML.runTestExpectWarning(filename, factory, label, warnings, fn);
        RunTestRDFXML.runTestCompareARP(fn, factory, label, fn);
    }

    /**
     * Run test, expecting an error.
     * Compare to running ARP.
     */
    private void checkForErrorCompare(String filename) {
        checkForError(filename, true);
    }

    /**
     * Run test, expecting an error.
     * Dop not compare to running ARP.
     */
    private void checkForErroNoCompare(String filename) {
        checkForError(filename, false);
    }


    /**
     * Run test, expecting an error. If the second argument is true, also Compare to
     * make sure it is the same as ARP.
     */
    private void checkForError(String filename, boolean compare) {
        trackFilename(filename);
        ReaderRIOTFactory factory = RDFParserRegistry.getFactory(lang);
        String fn = DIR+filename;
        RunTestRDFXML.runTestExpectFailure(filename, factory, label, fn);
        if ( compare )
            RunTestRDFXML.runTestCompareARP(fn, factory, label, fn);
    }

    static Set<String> localTestFiles() {
        Path LOCAL_DIR = Path.of(DIR);
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
        return found;
    }
}
