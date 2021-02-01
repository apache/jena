/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.ResourceFactory.Interface;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

/**
 * Tests for parsing RDF in various formats using various URI schemes from
 * https://www.iana.org/assignments according to
 * https://tools.ietf.org/html/rfc3986#section-3.1 and registered according to
 * https://tools.ietf.org/html/bcp35
 *
 * URI schemes can be classified as:
 *
 * permanent https://tools.ietf.org/html/bcp35#section-3 provisional
 * https://tools.ietf.org/html/bcp35#section-4 historical
 * https://tools.ietf.org/html/bcp35#section-5 private (not listed)
 * https://tools.ietf.org/html/bcp35#section-6
 *
 * Note that all of the above are syntactically equal, so there should not be
 * any difference in Jena's parsing.
 *
 * Tests named *Base* assumes the format-specific base URI, e.g.
 * <code>@base</code> in Turtle or <code>xml:base</code> in RDF/XML. Test named
 * *Rel* load RDF files that only have relative URI references and apply a base
 * as parameter to RDFDataMGr.
 *
 */
public class TestParseURISchemeBases {

    /**
     * Run with -Ddebug=true to log all parsed models to System.out
     */
    private static boolean DEBUG = Boolean.getBoolean("debug");
    private static Path DIR = Paths.get("testing/RIOT/URISchemes/");

    // private
    // https://www.w3.org/TR/app-uri/
    private static final String APP_BASE = "app://2dee5b0a-6100-470a-a67f-1399518cb470/";

    // Permanent https://tools.ietf.org/html/rfc7595#section-8
    private static final String EXAMPLE_BASE = "example://2dee5b0a-6100-470a-a67f-1399518cb470/";
    // permanent file with a real hostname
    // Must match src/main/resources/file*
    private static final String FILE_BASE = "file:///";
    // permanent http with a phoney (but syntactically valid) UUID hostname
    private static final String HTTP_BASE = "http://2dee5b0a-6100-470a-a67f-1399518cb470/";
    // private scheme according to https://tools.ietf.org/html/bcp35#section-3.8
    private static final String JENA_BASE = "org.apache.jena.test://foo/";
    // Provisional https://www.iana.org/assignments/uri-schemes/prov/ssh
    private static final String SSH_BASE = "ssh://example.com/";

    // private made-up scheme that contains "-"
    private static final String X_MADEUP_BASE = "x-madeup://2dee5b0a-6100-470a-a67f-1399518cb470/";

    Model m = ModelFactory.createDefaultModel();
    Interface resourceFactory = ResourceFactory.getInstance();

    // Helpers
    private void dump(String f) throws IOException {
        String filename = DIR.resolve(f).toString();
        String content = IO.readWholeFileAsUTF8(filename);
        System.out.println("File = "+f);
        System.out.println(content);
    }

    /**
     * Parse all files as input stream to avoid confusion with local file:/// URIs
     */
    private InputStream load(String f) throws IOException {
        Path file = DIR.resolve(f);
        assertTrue("Can't find " + file, Files.isRegularFile(file));
        // Read in memory to avoid keeping the file open in case test fails on Windows
        byte[] content = Files.readAllBytes(file);
        return new ByteArrayInputStream(content);
    }

    /**
     * The expected statement, depending on the provided base.
     */
    private Statement expectedStatement(String base) {
        Resource s = resourceFactory.createResource(base + "nested/foo.txt");
        Property p = RDFS.seeAlso;
        // Find the URI up to and excluding the path start
        String baseSchemeHost = base.replaceAll("(.*://[^/]*)/", "$1");

        RDFNode o = resourceFactory.createResource(baseSchemeHost + "/bar.txt");
        return resourceFactory.createStatement(s, p, o);
    }

    @Test
    public void appBaseRDF() throws Exception {
        RDFDataMgr.read(m, load("app-base.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(APP_BASE)));
    }

    @Test
    public void appBaseTTL() throws Exception {
        RDFDataMgr.read(m, load("app-base.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(APP_BASE)));
    }

    @Test
    public void appRDF() throws Exception {
        RDFDataMgr.read(m, load("app.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(APP_BASE)));
    }

    @Test
    public void appRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), APP_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(APP_BASE)));
    }

    @Test
    public void appRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), APP_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(APP_BASE)));
    }

    @Test
    public void appTTL() throws Exception {
        RDFDataMgr.read(m, load("app.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(APP_BASE)));
    }

    @Test
    public void exampleRDF() throws Exception {
        RDFDataMgr.read(m, load("example.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(EXAMPLE_BASE)));
    }

    @Test
    public void exampleRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), EXAMPLE_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(EXAMPLE_BASE)));
    }

    @Test
    public void exampleRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), EXAMPLE_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(EXAMPLE_BASE)));
    }

    @Test
    public void exampleTTL() throws Exception {
        RDFDataMgr.read(m, load("example.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(EXAMPLE_BASE)));
    }

    @Test
    public void fileRDF() throws Exception {
        RDFDataMgr.read(m, load("file.rdf"), "file:///", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(FILE_BASE)));
    }

    @Test
    public void fileRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), FILE_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(FILE_BASE)));
    }

    @Test
    public void fileRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), FILE_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(FILE_BASE)));
    }

    @Test
    public void fileTTL() throws Exception {
        RDFDataMgr.read(m, load("file.ttl"), "file:///", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(FILE_BASE)));
    }

    @Test
    public void fileBaseRDF() throws Exception {
        RDFDataMgr.read(m, load("file-base.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(FILE_BASE)));
    }

    @Test
    public void fileBaseTTL() throws Exception {
        RDFDataMgr.read(m, load("file-base.ttl"), "file:///", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(FILE_BASE)));
    }

    @Test
    public void httpRDF() throws Exception {
        RDFDataMgr.read(m, load("http.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(HTTP_BASE)));
    }

    @Test
    public void httpBaseRDF() throws Exception {
        RDFDataMgr.read(m, load("http-base.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(HTTP_BASE)));
    }

    @Test
    public void httpBaseTTL() throws Exception {
        RDFDataMgr.read(m, load("http-base.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(HTTP_BASE)));
    }

    @Test
    public void httpRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), HTTP_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(HTTP_BASE)));
    }

    @Test
    public void httpRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), HTTP_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(HTTP_BASE)));
    }

    @Test
    public void httpTTL() throws Exception {
        RDFDataMgr.read(m, load("http.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(HTTP_BASE)));
    }

    @Test
    public void jenaRDF() throws Exception {
        RDFDataMgr.read(m, load("jena.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(JENA_BASE)));
    }

    @Test
    public void jenaRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), JENA_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(JENA_BASE)));
    }

    @Test
    public void jenaRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), JENA_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(JENA_BASE)));
    }

    @Test
    public void jenaTTL() throws Exception {
        RDFDataMgr.read(m, load("jena.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(JENA_BASE)));
    }

    @Test
    public void sshBaseRDF() throws Exception {
        RDFDataMgr.read(m, load("ssh-base.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(SSH_BASE)));
    }

    @Test
    public void sshBaseTTL() throws Exception {
        RDFDataMgr.read(m, load("ssh-base.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(SSH_BASE)));
    }

    @Test
    public void sshRDF() throws Exception {
        RDFDataMgr.read(m, load("ssh.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(SSH_BASE)));
    }

    @Test
    public void sshRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), SSH_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(SSH_BASE)));
    }

    @Test
    public void sshRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), SSH_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(SSH_BASE)));
    }

    @Test
    public void sshTTL() throws Exception {
        RDFDataMgr.read(m, load("ssh.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(SSH_BASE)));
    }

    @Test
    public void xMadeupRDF() throws Exception {
        RDFDataMgr.read(m, load("x-madeup.rdf"), Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(X_MADEUP_BASE)));
    }

    @Test
    public void xMadeupRelRDF() throws Exception {
        RDFDataMgr.read(m, load("rel.rdf"), X_MADEUP_BASE + "nested/", Lang.RDFXML);
        assertTrue("Can't find statement", m.contains(expectedStatement(X_MADEUP_BASE)));
    }

    @Test
    public void xMadeupRelTTL() throws Exception {
        RDFDataMgr.read(m, load("rel.ttl"), X_MADEUP_BASE + "nested/", Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(X_MADEUP_BASE)));
    }

    @Test
    public void xMadeupTTL() throws Exception {
        RDFDataMgr.read(m, load("x-madeup.ttl"), Lang.TURTLE);
        assertTrue("Can't find statement", m.contains(expectedStatement(X_MADEUP_BASE)));
    }
}
