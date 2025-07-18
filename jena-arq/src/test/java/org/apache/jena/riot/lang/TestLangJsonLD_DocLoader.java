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

import static org.apache.jena.riot.Lang.JSONLD11;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.context.cache.Cache;
import com.apicatalog.jsonld.context.cache.LruCache;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.util.Context;

public class TestLangJsonLD_DocLoader {

    // Triggers the document loader.
    private static final String CONTENT = "{ \"@context\": \"http://unused/\" }";

    @Test
    public void testGetJsonLdOptions() {
        TestDocumentLoader loader = new TestDocumentLoader();
        Context context = setupContext(loader, false);

        StreamRDF sink = StreamRDFLib.sinkNull();
        RDFParser parser = RDFParserBuilder.create()
                .context(context)
                .lang(JSONLD11)
                .fromString(CONTENT)
                .build();
        parser.parse(sink);

        int count1 = loader.COUNTER;

        // By observation, it's actually called twice
        assertTrue(count1 >= 1 , ()->"Custom DocumentLoader wasn't called to handle loading");

        parser.parse(sink);
        int count2 = loader.COUNTER;
        assertTrue(count2 > count1 , ()->"Custom DocumentLoader wasn't called during second parser run");
    }

    @Test
    public void testGetJsonLdOptionsWithCache() {
        TestDocumentLoader loader = new TestDocumentLoader();
        Context context = setupContext(loader, true);

        StreamRDF sink = StreamRDFLib.sinkNull();
        RDFParser parser = RDFParserBuilder.create()
                .context(context)
                .lang(JSONLD11)
                .fromString(CONTENT)
                .build();

        parser.parse(sink);
        int count1 = loader.COUNTER;

        assertEquals(1, count1, ()->"Custom DocumentLoader wasn't called to handle loading");

        parser.parse(sink);
        int count2 = loader.COUNTER;

        assertTrue(count2 == count1 , ()->"Custom DocumentCache wasn't used during second parser run");
    }

    private final Context setupContext(TestDocumentLoader docLoader, boolean withCache) {
        JsonLdOptions opts = new JsonLdOptions();

        opts.setDocumentLoader(docLoader);

        if ( withCache ) {
            Cache<String, Document> documentCache = new LruCache<>(256);
            opts.setDocumentCache(documentCache);
        }

        Context context = new Context();
        context.set(LangJSONLD11.JSONLD_OPTIONS, opts);
        return context;
    }

    private final static class TestDocumentLoader implements DocumentLoader {

        public int COUNTER = 0;

        @Override
        public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
            DocumentLoader loader = HttpLoader.defaultInstance();
            JsonObject obj =
                    Json.createObjectBuilder()
                    .add("@context", JsonObject.EMPTY_JSON_OBJECT)
                    .build();

            COUNTER++;
            // Return an minimally empty JSON-LD document.
            return JsonDocument.of(obj);
        }
    }
}
