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

package org.apache.jena.riot.system;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.LangJSONLD11;
import org.apache.jena.riot.writer.JsonLD11Writer;

public class SysJSONLD11 {

    private static boolean titaniumPresent = false;
    private static boolean jakartaJsonPresent = false;

    public static ReaderRIOTFactory jsonld11ReaderFactory;

    private static void chooseFactories() {
        if ( titaniumPresent ) {
            jsonld11ReaderFactory = (language, profile) -> new LangJSONLD11();
            jsonld11WriterDatasetFactory = syntaxForm -> new JsonLD11Writer(syntaxForm);
            jsonld11WriterGraphFactory = syntaxForm -> RiotLib.adapter(new JsonLD11Writer(syntaxForm));
        } else {
            jsonld11ReaderFactory =  (language, profile) -> {
                throw new UnsupportedOperationException("Need both titanium-json-ld (1.1.0 or later) and org.glassfish:jakarta on the classpath");
            };
            jsonld11WriterDatasetFactory = syntaxForm -> {
                throw new UnsupportedOperationException("Need both titanium-json-ld (1.1.0 or later) and org.glassfish:jakarta on the classpath");
            };
            jsonld11WriterGraphFactory = syntaxForm -> {
                throw new UnsupportedOperationException("Need both titanium-json-ld (1.1.0 or later) and org.glassfish:jakarta on the classpath");
            };
        }
    }
    public static WriterDatasetRIOTFactory jsonld11WriterDatasetFactory;
    public static WriterGraphRIOTFactory jsonld11WriterGraphFactory;

    public static void init() {
        checkForTitaniumOnClassPath();
        chooseFactories();

        // --- Reader
        RDFParserRegistry.registerLangTriples(Lang.JSONLD11, jsonld11ReaderFactory);
        RDFParserRegistry.registerLangQuads(Lang.JSONLD11, jsonld11ReaderFactory);

        //  Writer.
        RDFWriterRegistry.register(RDFFormat.JSONLD11_PLAIN, jsonld11WriterGraphFactory);
        RDFWriterRegistry.register(RDFFormat.JSONLD11_PLAIN, jsonld11WriterDatasetFactory);
        RDFWriterRegistry.register(RDFFormat.JSONLD11_FLAT, jsonld11WriterGraphFactory);
        RDFWriterRegistry.register(RDFFormat.JSONLD11_FLAT, jsonld11WriterDatasetFactory);
    }

    public static void becomeDefaultJsonLd() {
        if ( titaniumPresent ) {
            RDFParserRegistry.registerLangTriples(Lang.JSONLD, jsonld11ReaderFactory);
            RDFParserRegistry.registerLangQuads(Lang.JSONLD, jsonld11ReaderFactory);
            // TODO Writer.
            return;
        }
        Log.warn(LangJSONLD11.class, "Titanium not configured - can't take over JSON-LD");
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