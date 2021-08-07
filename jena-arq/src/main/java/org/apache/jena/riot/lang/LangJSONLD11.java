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

package org.apache.jena.riot.lang;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.nquad.NQuadsWriter;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;

/**
 * JSON-LD 1.1 {@link ReaderRIOT}.
 * Placeholder implementation!
 */
public class LangJSONLD11 implements ReaderRIOT {
    public LangJSONLD11() {}

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        Sys.checkForTitanium();
        try {
            Document document = JsonDocument.of(in);
            read(document, output, context);
        } catch (Exception ex) {
            throw new RiotException(ex);
        }
    }

    @Override
    public void read(Reader in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        Sys.checkForTitanium();
        try {
            Document document = JsonDocument.of(in);
            read(document, output, context);
        } catch (Exception ex) {
            throw new RiotException(ex);
        }
    }

    // Simple - read with Titanium, output N-Quads to a string, parser string.
    // Minimal linkage!
    private void read(Document document, StreamRDF output, Context context) throws Exception {
        // JSON-LD to RDF
        RdfDataset dataset = JsonLd.toRdf(document).get();

        // Write N-Quads to string.
        StringWriter writer = new StringWriter();
        RdfWriter w = new NQuadsWriter(writer);
        w.write(dataset);

        // Do something with the string.
        RDFParser.fromString(writer.toString()).forceLang(Lang.NQUADS).parse(output);
    }

    public static class Sys {

        private static boolean titaniumPresent = false;
        private static boolean jakartaJsonPresent = false;

        public static void init() {
            checkForTitaniumOnClassPath();
            ReaderRIOTFactory factory;

            if ( ! titaniumPresent ) {
                factory = (language, profile) -> {
                    throw new UnsupportedOperationException("Need both titanium-json-ld (1.1.0 or later) and org.glassfish:jakarta on the classpath");
                };
            } else {
                factory = (language, profile) -> new LangJSONLD11();
            }
            RDFParserRegistry.registerLangTriples(Lang.JSONLD11, factory);
        }

        public static void checkForTitanium() {
            if ( ! titaniumPresent )
                throw new RiotException("Artifact com.apicatalog:titanium-json-ld not on classpath");
            if ( ! jakartaJsonPresent )
                throw new RiotException("Artifact org.glassfish:jakarta not on classpath");
        }

        static void checkForTitaniumOnClassPath() {
            try {
                // This class is in Titanium 1.1.0 (JakartaEE9 - jakarta.json) and not in
                // 0.8.6 (which uses JakartaEE8 - javax.json) where it is "com.apicatalog.jsonld.lang.Version"
                Class.forName("com.apicatalog.jsonld.JsonLdVersion", false, Thread.currentThread().getContextClassLoader());
                titaniumPresent = true;
            } catch (ClassNotFoundException ex) {
                return;
            }
            try {
                Class.forName("jakarta.json.Json", false, Thread.currentThread().getContextClassLoader());
                jakartaJsonPresent = true;
            } catch (ClassNotFoundException ex) {
                return;
            }
        }

    }
}