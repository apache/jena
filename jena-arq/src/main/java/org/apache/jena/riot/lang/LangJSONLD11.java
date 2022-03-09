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
import java.util.Set;
import java.util.function.BiConsumer;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.rdf.RdfDataset;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.JenaTitanium;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;

/**
 * JSON-LD 1.1 {@link ReaderRIOT}.
 */
public class LangJSONLD11 implements ReaderRIOT {
    private final ErrorHandler errorHandler;
    private final ParserProfile profile;

    public LangJSONLD11(Lang language, ParserProfile profile, ErrorHandler errorHandler) {
        this.profile = profile;
        this.errorHandler = errorHandler;
    }

    @Override
    public void read(InputStream input, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Document document = JsonDocument.of(input);
            read(document, output, context);
        } catch (JsonLdError ex) {
            handleJsonLdError(ex);
        } catch (Exception ex) {
            errorHandler.error(ex.getMessage(), -1, -1);
            throw new RiotException(ex);
        }
    }

    private void handleJsonLdError(JsonLdError ex) {
        if ( ex.getCause() instanceof jakarta.json.stream.JsonParsingException) {
            jakarta.json.stream.JsonParsingException exp = (jakarta.json.stream.JsonParsingException)(ex.getCause());
            JsonLocation loc = exp.getLocation();
            errorHandler.error(ex.getMessage(), loc.getLineNumber(), loc.getColumnNumber());
        } else {
            errorHandler.error(ex.getMessage(), -1, -1);
        }
        throw new RiotException(ex);
    }

    @Override
    public void read(Reader in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Document document = JsonDocument.of(in);
            read(document, output, context);
        } catch (JsonLdError ex) {
            ex.printStackTrace();
            handleJsonLdError(ex);
        } catch (Exception ex) {
            errorHandler.error(ex.getMessage(), -1, -1);
            throw new RiotException(ex);
        }
    }

    private void read(Document document, StreamRDF output, Context context) throws JsonLdError {
        // JSON-LD to RDF
        RdfDataset dataset = JsonLd.toRdf(document).get();
        extractPrefixes(document, output::prefix);
        JenaTitanium.convert(dataset, output);

//        try {
//
//        } catch (RuntimeException ex) {
//    }
//    catch (IOException e) {
//        errorHandler.error(e.getMessage(), -1, -1);
//        IO.exception(e) ;
//    }
    }

    /**
     * JSON-LD does not define prefixes.
     * <p>
     * The use of "prefix:localname" happens for any definition of "prefix" in the
     * {@literal @context} even if intended for a URI e.g a property.
     * </p>
     * <p>
     * We could extract any {"key" : "value"} from the context but we add a pragmatic
     * filter to to see if the URI value ends in "#", "/" or ":" (for "urn:" and "did:"
     * cases).
     * </p>
     * <p>
     * In addition, {@literal @vocab} becomes prefix "".
     * </p>
     */
    private static void extractPrefixes(Document document, BiConsumer<String, String> action) {
        try {
            JsonStructure js = document.getJsonContent().get();
            switch(js.getValueType()) {
                case ARRAY :
                    extractPrefixes(js, action);
                    break;
                case OBJECT :
                    JsonValue jv = js.asJsonObject().get(Keywords.CONTEXT);
                    extractPrefixes(jv, action);
                    break;
                default :
                    break;
            }
        } catch(Throwable ex) {
            Log.warn(LangJSONLD11.class, "Unexpected problem while extracting prefixes: "+ex.getMessage(), ex);
        }
    }

    private static void extractPrefixes(JsonValue jsonValue, BiConsumer<String, String> action) {
        if ( jsonValue == null )
            return ;
        // JSON-LD 1.1 section 9.4
        switch(jsonValue.getValueType()) {
            case ARRAY :
                jsonValue.asJsonArray().forEach(jv -> extractPrefixes(jv, action));
                break;
            case OBJECT :
                extractPrefixesCxtDefn(jsonValue.asJsonObject(), action);
                break;
            case NULL: break;       // We are only interested in prefixes
            case STRING: break;     // We are only interested in prefixes
            default :
                break;
        }
    }

    private static void extractPrefixesCxtDefn(JsonObject jCxt, BiConsumer<String, String> action) {
        Set<String> keys = jCxt.keySet();
        keys.stream().forEach(k->{
            // "@vocab" : "uri"
            // "shortName" : "uri"
            // "shortName" : { "@type":"@id" , "@id": "uri" } -- presumed to be a single property aliases, not a prefix.
            JsonValue jvx = jCxt.get(k);
            if ( JsonValue.ValueType.STRING != jvx.getValueType() )
                return;
            String prefix = k;
            if ( Keywords.VOCAB.equals(k) )
                prefix = "";
            else if ( k.startsWith("@") )
                // Keyword, not @vocab.
                return;
            // Pragmatic filter: URI ends in "#" or "/" or ":"
            String uri = JsonString.class.cast(jvx).getString();
            if ( uri.endsWith("#") || uri.endsWith("/") || uri.endsWith(":") ) {
                action.accept(prefix, uri);
                return;
            }
        });
    }
}
