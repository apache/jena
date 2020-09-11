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

package org.apache.jena.riot;

import com.github.jsonldjava.core.JsonLdOptions;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.lang.JsonLDReader;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;

import java.io.InputStream;

/**
 * Set of parameters that can be used to control the reading of JSON-LD.
 *
 * This class provides setters to define a "Context" suitable to be passed as 
 * last argument to  {@link ReaderRIOT#read(InputStream, String, ContentType, StreamRDF, Context)}
 * when the ReaderRIOT has been created with one of the JSON-LD RDFFormat variants (that is, when it is
 * an instance of {@link JsonLDReader})
 *
 * Parameters that are actually useful are ''documentLoader'' and ''produceGeneralizedRdf''.
 *
 */
public class JsonLDReadContext extends Context {
    /**
     * Set the value of the JSON-LD "@context" node, used when reading a jsonld (overriding the actual @context in the jsonld). "Compact" and "Flattened" JSON-LD outputs.
     *
     * @param jsonLdContext the value of the "@context" node (a JSON value). Note that the use of an URI to pass an external context is not supported (as of JSONLD-java API 0.8.3)
     *
     * @see #setJsonLDContext(Object)
     */
    public void setJsonLDContext(String jsonLdContext) {
        set(JsonLDReader.JSONLD_CONTEXT, jsonLdContext);
    }

    /**
     * Set the value of the JSON-LD "@context" node, used when reading a jsonld (overriding the actual @context in the jsonld). "Compact" and "Flattened" JSON-LD outputs.
     *
     *
     * @param jsonLdContext the context as expected by JSON-LD java API. As of JSON-LD java 0.8.3, a Map
     * defining the properties and the prefixes is OK. Note that the use an URI to pass an external context is not supported (JSONLD-java RDF 0.8.3)
     *
     * @see #setJsonLDContext(String)
     */
    public void setJsonLDContext(Object jsonLdContext) {
        set(JsonLDReader.JSONLD_CONTEXT, jsonLdContext);
    }
    /**
     * Set the JSON-LD java API's options
     *
     * If not set, a default value is used.
     *
     * @param opts the options as defined by the JSON-LD java API
     */
    public void setOptions(JsonLdOptions opts) {
        set(JsonLDReader.JSONLD_OPTIONS, opts);
    }
}
