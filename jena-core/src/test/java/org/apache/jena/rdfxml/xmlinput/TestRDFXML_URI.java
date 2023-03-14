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

package org.apache.jena.rdfxml.xmlinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.JUnit4TestAdapter;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFErrorHandler;
import org.apache.jena.shared.JenaException;
import org.junit.Test;

/** tests of bad URis in RDF/XML. */
public class TestRDFXML_URI {
    static public junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestRDFXML_URI.class) ;
    }

    private static String DIR = "testing/arp1/uri/";

    // Bad URIs in RDF/XML - various positions.
    @Test public void bad_uri_1() { test("bad-uri-1.rdf"); }
    @Test public void bad_uri_2() { test("bad-uri-2.rdf"); }
    @Test public void bad_uri_3() { test("bad-uri-3.rdf"); }
    @Test public void bad_uri_4() { test("bad-uri-4.rdf"); }

    // Bad Base URIs in RDF/XML - various ways to set the base
    @Test
    public void bad_base_0() {
        // Passed into the parser by calling code.
        test("bad-base-0.rdf", "http://example/bad base");
    }

    @Test public void bad_base_1() { test("bad-base-1.rdf"); }
    @Test public void bad_base_2() { test("bad-uri-2.rdf"); }
    @Test public void bad_base_3() { test("bad-uri-3.rdf"); }
    @Test public void bad_base_4() { test("bad-uri-4.rdf"); }

    private static void test(String file) {
        String base = IRIs.resolve(file);
        test(file, base);
    }

    private static void test(String file, String base) {
        ErrorHandlerNoPrint eh = new ErrorHandlerNoPrint();
        try {
            file = DIR+file;
            Model m1 = ModelFactory.createDefaultModel();
            RDFXMLReader xr = new RDFXMLReader(true);
            xr.setErrorHandler(eh);
            InputStream in = new FileInputStream(file);
            xr.read(m1, in, base);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        catch (JenaException ex) {
            assertEquals(1, eh.errors);
            Throwable cause = ex.getCause();
            assertTrue(cause instanceof ParseException);
        }
    }


    static class ErrorHandlerNoPrint implements RDFErrorHandler {
        int warnings = 0;
        int errors = 0;
        int fatalErrors = 0;

        @Override
        public void warning(Exception e) { warnings++; }

        @Override
        public void error(Exception e) { errors++; }

        @Override
        public void fatalError(Exception e) { fatalErrors++; }
    }

}
