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

package org.apache.jena.rdfxml.xmloutput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.*;
import org.apache.jena.irix.IRIException;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler;
import org.apache.jena.rdfxml.arp1.RDFXMLReader;
import org.apache.jena.rdfxml.xmloutput.impl.BaseXMLWriter;
import org.apache.jena.rdfxml.xmloutput.impl.SimpleLogger;
import org.apache.jena.shared.InvalidPropertyURIException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.RDF;

public abstract class BaseTestXMLFeatures extends BaseTestXMLOutput {
    private String base1 = "http://example/foobar";

    private String base2 = "http://example/barfoo";

    protected static String file1 = "testing/abbreviated/namespaces.rdf";

    public BaseTestXMLFeatures() {
        super();
    }

    @Override
    public String toString() {
        return BaseTestXMLFeatures.class.getSimpleName() + " " + getLang();
    }

    /**
     * Writing a model with the base URI set to null should not throw a null pointer
     * exception.
     */
    @Test
    public void testNullBaseWithAbbrev() {
        ModelFactory.createDefaultModel().write(new StringWriter(), getLang(), null);
    }

    /**
     * This test checks that using a FileWriter works. It used not to work for some
     * encodings. The encoding used is the platform default encoding. Because this
     * may be MacRoman, we have to suppress warning messages.
     *
     * @throws IOException
     */
    @Test
    public void testBug696057() throws IOException {
        File f = File.createTempFile("jena", ".rdf");
        String fileName = f.getAbsolutePath();
        Model m = ModelTestLib.createMemModel();
        m.read(new FileInputStream("testing/wg/rdfms-syntax-incomplete/test001.rdf"), "");
        RDFDefaultErrorHandler.silent = true;
        Model m1 = null;
        SimpleLogger old = null;
        try {
            old = BaseXMLWriter.setLogger(new SimpleLogger() {
                @Override
                public void warn(String s) {}

                @Override
                public void warn(String s, Exception e) {}
            });
            m.write(new FileWriter(fileName), getLang());
            m1 = ModelTestLib.createMemModel();
            m1.read(new FileInputStream(fileName), "");
        } finally {
            RDFDefaultErrorHandler.silent = false;
            BaseXMLWriter.setLogger(old);
        }
        assertTrue(m.isIsomorphicWith(m1), "Use of FileWriter");
        f.delete();
    }

    @Test
    public void testXMLBase() throws IOException {
        checkA(file1, // any will do
               "xml:base=['\"]" + base2 + "['\"]", new Change() {
                   @Override
                   public void modify(RDFWriterI writer) {
                       String oldvalue = (String)writer.setProperty("xmlbase", base1);
                       assertTrue(oldvalue == null, "xmlbase valued non-null");

                       oldvalue = (String)writer.setProperty("xmlbase", base2);
                       assertEquals(base1, oldvalue, "xmlbase valued incorrect.");
                   }

               });
    }

    @Test
    public void testPropertyURI() throws IOException {
        doBadPropTest(getLang());
    }

    void doBadPropTest(String lg) throws IOException {
        Model m = ModelTestLib.createMemModel();
        m.add(m.createResource(), m.createProperty("http://example/", "foo#"), "foo");
        File file = File.createTempFile("rdf", ".xml");
        // file.deleteOnExit();

        FileOutputStream fwriter = new FileOutputStream(file);
        try {
            m.write(fwriter, lg);
            fwriter.close();
            fail("Writer did not detect bad property URI");
        } catch (InvalidPropertyURIException je) {
            // as required, so nowt to do.
        }
        file.delete();
    }

    @Test
    public void testUseNamespace() throws IOException {
        checkA(file1, "xmlns:eg=['\"]http://example.org/#['\"]", Change.setPrefix("eg", "http://example.org/#"));
    }

    @Test
    public void testSingleQuote() throws IOException {
        checkY(file1, "'", "\"", Change.setProperty("attributeQuoteChar", "'"));
    }

    @Test
    public void testDoubleQuote() throws IOException {
        checkY(file1, "\"", "'", Change.setProperty("attributeQuoteChar", "\""));
    }

    @Test
    public void testUseDefaultNamespace() throws IOException {
        checkA(file1, "xmlns=['\"]http://example.org/#['\"]", Change.setPrefix("", "http://example.org/#"));
    }

    @Test
    public void testUseUnusedNamespace() throws IOException {
        checkA(file1, "xmlns:unused=['\"]http://unused.org/#['\"]", Change.setPrefix("unused", "http://unused.org/#"));
    }

    @Test
    public void testRDFNamespace() throws IOException {
        checkY(file1, "xmlns:r=['\"]" + RDF.getURI() + "['\"]", "rdf:", new Change() {
            @Override
            public void modify(Model m) {
                m.removeNsPrefix("rdf");
                m.setNsPrefix("r", RDF.getURI());
            }
        });
    }

    @Test
    public void testTab() throws IOException {
        checkA(file1, "          ", Change.setProperty("tab", "5"));
    }

    @Test
    public void testNoTab() throws IOException {
        checkA(file1, "  ", Change.setProperty("tab", "0"));
    }

    @Test
    public void testNoLiteral() throws IOException {
        checkY("testing/wg/rdfms-xml-literal-namespaces/test001.rdf", "#XMLLiteral", "[\"']Literal[\"']",
               Change.setProperty("blockrules", "parseTypeLiteralPropertyElt"));
    }

    @Test
    public void testRDFDefaultNamespace() throws IOException {
        checkA(file1, "xmlns=['\"]" + RDF.getURI() + "['\"].*" + "xmlns:(j\\.cook.up|j\\.fixup)=['\"]" + RDF.getURI() + "['\"]",
               Change.setPrefix("", RDF.getURI()));
    }

    @Test
    public void testBadPrefixNamespace() {
        // Trying to set the prefix should generate a warning.
    }

    // JENA-24
    @Test
    public void testDisallowedXMLNamespace() throws IOException {
        // xml, if present, must be bound to correct namespaces

        // fine, though ill-advised
        checkA(file1, null, Change.setPrefix("xml", "http://www.w3.org/XML/1998/namespace"));

        // bad, but not fatal now -- we probably ought to raise an warning
        checkA(file1, null, Change.setPrefix("notxml", "http://www.w3.org/XML/1998/namespace"));

        // bad, will warn
        checkX(file1, null, null, null, true, Change.setPrefix("xml", "http://example.org/#"));
    }

    // JENA-24
    @Test
    public void testDisallowedXMLNSNamespace() throws IOException {
        // xmlns, if present, must be bound to correct namespace

        // fine, though ill-advised
        checkA(file1, null, Change.setPrefix("xmlns", "http://www.w3.org/2000/xmlns/"));

        // bad, but not fatal now -- we probably ought to raise an warning
        checkA(file1, null, Change.setPrefix("notxmlns", "http://www.w3.org/2000/xmlns/"));

        // bad, will warn
        checkX(file1, null, null, null, true, Change.setPrefix("xmlns", "http://example.org/#"));
    }

    @Test
    public void testDuplicateNamespace() throws IOException {
        checkY(file1, "xmlns:eg[12]=['\"]http://example.org/#['\"]",
               "xmlns:eg[12]=['\"]http://example.org/#['\"].*xmlns:eg[12]=['\"]http://example.org/#['\"]", new Change() {
                   @Override
                   public void modify(Model m) {
                       m.setNsPrefix("eg1", "http://example.org/#");
                       m.setNsPrefix("eg2", "http://example.org/#");
                   }
               });
    }

    @Test
    public void testEntityDeclaration() throws IOException {
        checkY(file1, "<!DOCTYPE rdf:RDF \\[[^]]*<!ENTITY spoo *'goo:boo'>", "SPONGLE",
               Change.setProperty("showDoctypeDeclaration", true).andSetPrefix("spoo", "goo:boo"));
    }

    @Test
    public void testEntityUse() throws IOException {
        checkY(file1, "rdf:resource=\"&ex0;spoo\"", "SPONGLE", Change.setProperty("showDoctypeDeclaration", true));
    }

    @Test
    public void testDuplicatePrefix() throws IOException {
        checkY(file1, "xmlns:eg=['\"]http://example.org/file[12]#['\"]", null, new Change() {
            @Override
            public void modify(Model m) {
                m.setNsPrefix("eg", "http://example.org/file1#");
                m.setNsPrefix("eg", "http://example.org/file2#");
            }
        });
    }

    void setNsPrefixSysProp(String prefix, String uri) {
        System.setProperty(RDFWriterI.NSPREFIXPROPBASE + uri, prefix);
    }

    @Test
    public void testUseNamespaceSysProp() throws IOException {
        checkA(file1, "xmlns:eg=['\"]http://example.org/#['\"]", new Change() {
            @Override
            public void modify(RDFWriterI writer) {
                setNsPrefixSysProp("eg", "http://example.org/#");
            }
        });
    }

    @Test
    public void testDefaultNamespaceSysProp() throws IOException {
        checkA(file1, "xmlns=['\"]http://example.org/#['\"]", new Change() {
            @Override
            public void modify(RDFWriterI writer) {
                setNsPrefixSysProp("", "http://example.org/#");
            }
        });
    }

    @Test
    public void testDuplicateNamespaceSysProp() throws IOException {
        checkY(file1, "xmlns:eg[12]=['\"]http://example.org/#['\"]",
               "xmlns:eg[12]=['\"]http://example.org/#['\"].*xmlns:eg[12]=['\"]http://example.org/#['\"]", new Change() {

                   @Override
                   public void modify(RDFWriterI writer) {
                       setNsPrefixSysProp("eg1", "http://example.org/#");
                       setNsPrefixSysProp("eg2", "http://example.org/#");
                   }
               });
    }

    @Test
    public void testDuplicatePrefixSysProp() throws IOException {
        checkY(file1, "xmlns:eg=['\"]http://example.org/file[12]#['\"]", null, new Change() {
            @Override
            public void modify(RDFWriterI writer) {
                setNsPrefixSysProp("eg", "http://example.org/file1#");
                setNsPrefixSysProp("eg", "http://example.org/file2#");
            }
        });
    }

    @Test
    public void testDuplicatePrefixSysPropAndExplicit() throws IOException {
        checkY(file1, "xmlns:eg=['\"]http://example.org/file[12]#['\"]", null, new Change() {
            @Override
            public void modify(Model m) {
                m.setNsPrefix("eg", "http://example.org/file1#");
                setNsPrefixSysProp("eg", "http://example.org/file2#");
            }
        });
    }

    @Test
    public void testUTF8DeclAbsent() throws IOException {
        checkC(file1, "utf-8", null, "<\\?xml", Change.none());
    }

    @Test
    public void testUTF16DeclAbsent() throws IOException {
        checkX(file1, "utf-16", null, "<\\?xml", false, Change.none());
    }

    @Test
    public void testUTF8DeclPresent() throws IOException {
        checkC(file1, "utf-8", "<\\?xml", null, Change.setProperty("showXmlDeclaration", true));
    }

    @Test
    public void testUTF16DeclPresent() throws IOException {
        checkC(file1, "utf-16", "<\\?xml", null, Change.setProperty("showXmlDeclaration", true));
    }

    @Test
    public void testISO8859_1_DeclAbsent() throws IOException {
        checkC(file1, "iso-8859-1", null, "<\\?xml", Change.setProperty("showXmlDeclaration", false));
    }

    @Test
    public void testISO8859_1_DeclPresent() throws IOException {
        checkC(file1, "iso-8859-1", "<\\?xml[^?]*ISO-8859-1", null, Change.none());
    }

    @Test
    public void testStringDeclAbsent() throws IOException {
        checkY(file1, null, "<\\?xml", Change.none());
    }

    @Test
    public void testStringDeclPresent() throws IOException {
        checkY(file1, "<\\?xml", "encoding", Change.setProperty("showXmlDeclaration", true));
    }

    /**
     * Introduced to cope with bug 832682: double spacing on windows platforms. Make
     * sure the xmlns prefixes are introduced by the correct line separator. (Java
     * doesn't appear to understand that the notion of "line separator" should be
     * portable ... come back C, all is forgiven. Well, not *all* ...)
     */
    @Test
    public void testLineSeparator() {
        String newline = System.getProperty("line.separator");
        String newline_XMLNS = newline + "    xmlns";
        Model m = ModelTestLib.modelWithStatements("http://eh/spoo thingies something");
        m.setNsPrefix("eh", "http://eh/");
        StringWriter sos = new StringWriter();
        m.write(sos, getLang());
        assertTrue(sos.toString().contains(newline_XMLNS));
    }

    static final int BadPropURI = 1;

    static final int NoError = 0;

    static final int ExtraTriples = 2;

    static final int BadURI = 3;

    public void checkPropURI(String s, String p, Object val, int behaviour) throws IOException {
        // create triple and graph.
        // BaseXMLWriter.dbg = true;
        // SystemOutAndErr.block();
        // TestLogger tl = new TestLogger(BaseXMLWriter.class);
        blockLogger();
        Node blank = NodeFactory.createBlankNode();
        Node prop = NodeFactory.createURI(s);
        Graph g = GraphMemFactory.createDefaultGraphSameValue();
        g.add(Triple.create(blank, prop, blank));
        // create Model
        Model m = ModelFactory.createModelForGraph(g);
        // serialize

        @SuppressWarnings("deprecation")
        RDFWriterI rw = m.getWriter(getLang());
        if (p != null)
            rw.setProperty(p, val);
        try (StringWriter w = new StringWriter()) {
            rw.write(m, w, "http://example.org/");
            String f = w.toString();

            switch (behaviour) {
                case BadPropURI:
                    fail("Bad property URI <" + s + "> was not detected.");
                    return;
                case BadURI:
                    fail("Bad URI <" + s + "> was not detected.");
                    return;
            }
            // read back in
            Model m2 = ModelTestLib.createMemModel();
            @SuppressWarnings("removal")
            RDFReaderI rdr = new RDFXMLReader();
            rdr.setProperty("error-mode", "lax");
            try (StringReader sr = new StringReader(f)) {
                rdr.read(m2, sr, "http://example.org/");
            }

            // check
            switch (behaviour) {
                case ExtraTriples:
                    assertTrue(m2.size() == 3, "Expecting Brickley behaviour.");
                    break;
                case NoError:
                    assertTrue(m.isIsomorphicWith(m2), "Comparing Model written out and read in.");
                    break;
            }
        } catch (IRIException ex) {
            if (behaviour == BadURI)
                return;
            throw ex;
        } catch (InvalidPropertyURIException je) {
            if (behaviour == BadPropURI)
                return;
            throw je;
        } catch (JenaException e) {
            throw e;
        } finally {
            // BaseXMLWriter.dbg = false;
            // tl.end();
            unblockLogger();
            // SystemOutAndErr.unblock();
        }
    }

    @Test
    public void testBadURIAsProperty1() throws IOException {
        try {
            // RDFDefaultErrorHandler.logger.setLevel( Level.OFF );
            checkPropURI("_:aa", null, null, BadURI);
        } finally { // RDFDefaultErrorHandler.logger.setLevel( Level.WARN );
        }
    }

    @Test
    public void testBadURIAsProperty2() throws IOException {
        try {
            // RDFDefaultErrorHandler.logger.setLevel( Level.OFF );
            checkPropURI("_:aa", "allowBadURIs", "true", NoError);
        } finally {// RDFDefaultErrorHandler.logger.setLevel( Level.WARN );
        }
    }

    @Test
    public void testLiAsProperty1() throws IOException {
        checkPropURI(RDF.getURI() + "li", null, null, BadPropURI);
    }

    /* @Test public void testLiAsProperty2() throws IOException {
     * checkPropURI(RDF.getURI()+"li", "brickley", "true", ExtraTriples); } */
    @Test
    public void testDescriptionAsProperty() throws IOException {
        checkPropURI(RDF.getURI() + "Description", null, null, BadPropURI);
    }

    @Test
    public void testBadProperty1() throws IOException {
        checkPropURI("http://x/a.b/", null, null, BadPropURI);
    }
}
