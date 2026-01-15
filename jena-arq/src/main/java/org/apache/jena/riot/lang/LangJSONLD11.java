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

package org.apache.jena.riot.lang;

import java.io.InputStream;
import java.io.Reader;
import java.util.Set;
import java.util.function.BiConsumer;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.lang.Keywords;
import jakarta.json.*;
import jakarta.json.stream.JsonLocation;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.irix.IRIs;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.jsonld.TitaniumJsonLdOptions;
import org.apache.jena.riot.system.jsonld.TitaniumToJena;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * JSON-LD 1.1 {@link ReaderRIOT}.
 */
public class LangJSONLD11 implements ReaderRIOT {
    private final ErrorHandler errorHandler;
    private final ParserProfile profile;

    private static final String SYMBOLS_NS = "http://jena.apache.org/riot/jsonld#";
    /**
     * value: the option object expected by JsonLdProcessor (instance of JsonLdOptions)
     */
    public static final Symbol JSONLD_OPTIONS = SystemARQ.allocSymbol(SYMBOLS_NS, "options");

    public LangJSONLD11(Lang language, ParserProfile profile, ErrorHandler errorHandler) {
        this.profile = profile;
        this.errorHandler = errorHandler;
    }

    @Override
    public void read(InputStream input, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Document document = JsonDocument.of(input);
            read(document, baseURI, output, context);
        } catch (JsonLdError ex) {
            handleJsonLdError(ex);
        } catch (Exception ex) {
            errorHandler.error(ex.getMessage(), -1, -1);
            throw new RiotException(ex);
        }
    }

    private void handleJsonLdError(JsonLdError ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof jakarta.json.stream.JsonParsingException exp) {
            JsonLocation loc = exp.getLocation();
            errorHandler.error(ex.getMessage(), loc.getLineNumber(), loc.getColumnNumber());
        } else if ( cause instanceof JsonLdError ex2) {
            // Avoid seemingly circular causes.
            if ( ex != ex2 )
                errorHandler.error(ex2.getMessage(), -1, -1);
        }
        throw new RiotException(ex);
    }

    @Override
    public void read(Reader in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Document document = JsonDocument.of(in);
            read(document, baseURI, output, context);
        } catch (JsonLdError ex) {
            handleJsonLdError(ex);
        } catch (Exception ex) {
            errorHandler.error(ex.getMessage(), -1, -1);
            throw new RiotException(ex);
        }
    }

    private void read(Document document, String baseURI, StreamRDF output, Context context) throws JsonLdError {
        // JSON-LD to RDF
        JsonLdOptions opts = TitaniumJsonLdOptions.get(baseURI, context);
        extractPrefixes(document, output::prefix);
        TitaniumToJena.convert(document, opts, output, profile);
    }

    /**
     * JSON-LD does not define prefixes.
     * <p>
     * The use of "prefix:localname" happens for any definition of "prefix" in the
     * {@literal @context} even if intended for a URI e.g. a property.
     * </p>
     * <p>
     * We could extract any {"key" : "value"} from the context but we add a
     * pragmatic filter to to see if the URI value ends in "#", "/" or ":" (for
     * "urn:" and "did:" cases).
     * </p>
     * <p>
     * In addition, {@literal @vocab} becomes prefix "".
     * </p>
     * <p>
     * The code assumes the structure is well-formed - it passed JSON-LD parsing by Titanium.
     * </p>
     */
    private static void extractPrefixes(Document document, BiConsumer<String, String> action) {
        try {

            JsonStructure jsonStructure = document.getJsonContent().orElse(null);
            if ( jsonStructure == null )
                return;
            extractPrefixesValue(jsonStructure, action);
        } catch (Throwable ex) {
            Log.warn(LangJSONLD11.class, "Unexpected problem while extracting prefixes: " + ex.getMessage(), ex);
        }
    }

    /**
     * <a href="https://www.w3.org/TR/json-ld11/#graph-objects">JSON-LD 1.1 section 9.4 Graph Objects</a>.
     * Assume the structure is well-fromed - it passed JSON-LD parsing by Titanium.
     */
    private static void extractPrefixesValue(JsonValue jsonValue, BiConsumer<String, String> action) {
        switch (jsonValue.getValueType()) {
            case ARRAY -> extractPrefixesArray(jsonValue.asJsonArray(), action);
            case OBJECT -> extractPrefixesObject(jsonValue.asJsonObject(), action);
            default->{}
        }
    }

    private static void extractPrefixesArray(JsonArray jsonArray, BiConsumer<String, String> action) {
        jsonArray.forEach(jv -> extractPrefixesValue(jv, action));
    }

    /**
    * <a href="https://www.w3.org/TR/json-ld11/#graph-objects">JSON-LD 1.1 section 9.4 Graph Objects</a>.
    */
    private static void extractPrefixesObject(JsonObject jsonObject, BiConsumer<String, String> action) {
        JsonValue contextValue = jsonObject.get(Keywords.CONTEXT);
        if ( contextValue == null )
            return;
        // If the graph object contains the @context key, its value MUST be null, an IRI reference, a context definition, or an array composed of any of these.
        switch (contextValue.getValueType()) {
            // Assuming the contextValue is valid (Titanium parsed it).
            case ARRAY -> extractPrefixesContextArray(contextValue.asJsonArray(), action);
            case OBJECT -> extractPrefixesContextDefinition(contextValue.asJsonObject(), action);
            // URI or null.
            default -> {}
        }
    }

    // @context [ ]
    private static void extractPrefixesContextArray(JsonArray jsonArray, BiConsumer<String, String> action) {
        jsonArray.forEach(cxtArrayEntry -> {
            switch (cxtArrayEntry.getValueType()) {
                case OBJECT -> extractPrefixesContextDefinition(cxtArrayEntry.asJsonObject(), action);
                // URI or null.
                default -> {}
            }
        });
    }

    /**
     * Extract prefixes from a context definition.
     * <p>
     * "A context definition defines a local context in a node object."
     */
    private static void extractPrefixesContextDefinition(JsonObject jCxt, BiConsumer<String, String> action) {
        // Assume the local context is valid.
        Set<String> keys = jCxt.keySet();
        keys.stream().forEach(k -> {
            // "@vocab" : "uri"
            // "shortName" : "uri"
            // "shortName" : { "@type":"@id" , "@id": "uri" } -- presumed to be a single property aliases, not a prefix.
            JsonValue jValue = jCxt.get(k);
            if (JsonValue.ValueType.STRING != jValue.getValueType())
                return;
            String prefix = k;
            if (Keywords.VOCAB.equals(k))
                prefix = "";
            else if (k.startsWith("@"))
                // Keyword, not @vocab.
                return;
            // Pragmatic filter: URI ends in "#" or "/" or ":"
            String uri = JsonString.class.cast(jValue).getString();
            if (uri.endsWith("#") || uri.endsWith("/") || uri.endsWith(":")) {
                if ( IRIs.check(uri) )
                    action.accept(prefix, uri);
                return;
            }
        });
    }
}
