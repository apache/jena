/**
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

import java.io.OutputStream;

import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.JsonLD10Writer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

import com.github.jsonldjava.core.JsonLdOptions;

/**
 * Set of parameters that can be used to control the writing of JSON-LD.
 *
 * This class provides setters to define a "Context" suitable to be passed as
 * last argument to  {@link org.apache.jena.riot.WriterDatasetRIOT#write(OutputStream, DatasetGraph, PrefixMap, String, Context)}
 * when the WriterDatasetRIOT has been created with one of the JSON-LD RDFFormat variants (that is, when it is
 * an instance of {@link org.apache.jena.riot.writer.JsonLD10Writer})
 *
 * Parameters that are actually useful depend on the JSON-LD output variant.
 *
 * None of them is required (default values being used), except for the "frame" one,
 * when outputting using JSON-LD "frame" output variant.
 *
 */
public class JsonLDWriteContext extends Context {
    /**
     * Set the JSON-LD java API's options
     *
     * If not set, a default value is used. (Note that this default
     * is not the same as the one used by JSON-LD java API).
     *
     * @param opts the options as defined by the JSON-LD java API
     */
    public void setOptions(JsonLdOptions opts) {
        set(JsonLD10Writer.JSONLD_OPTIONS, opts);
    }

    /**
     * Set the value of the JSON-LD "@context" node, used in "Compact" and "Flattened" JSON-LD outputs.
     *
     * Only useful for "Compact" and "Flattened" JSON-LD outputs, and not required: if not set,
     * a value for the "@Context" node is computed, based on the content of the dataset and its prefix mappings.
     *
     * @param jsonLdContext the value of the "@context" node (a JSON value).
     * @see #setJsonLDContextSubstitution(String) for a way to overcome this problem.
     *
     * @see #setJsonLDContext(Object)
     */
    public void setJsonLDContext(String jsonLdContext) {
        set(JsonLD10Writer.JSONLD_CONTEXT, jsonLdContext);
    }

    /**
     * Set the value of the JSON-LD "@context" node, used in "Compact" and "Flattened" JSON-LD outputs.
     *
     * Only useful for "Compact" and "Flattened" JSON-LD outputs, and not required: if not set,
     * a value for the "@Context" node is computed, based on the content of the dataset and its prefix mappings.
     *
     * @param jsonLdContext the context as expected by JSON-LD java API.
     * @see #setJsonLDContextSubstitution(String) for a way to overcome this problem.
     *
     * @see #setJsonLDContext(String)
     */
    public void setJsonLDContext(Object jsonLdContext) {
        set(JsonLD10Writer.JSONLD_CONTEXT, jsonLdContext);
    }

    /**
     * Allow to replace the content of the "@context" node with a given value.
     *
     * This is useful, for instance, to allow to set the @content in the output to an URI, such as "@context": "https://schema.org/"
     * Note that the actual content at this URI is NOT used when computing the output.
     * The context used to compute the JSONLD output is the one normally used (as defined by a call to -
     * or the lack of call to - setJsonLdContext)
     *
     * Only useful for "Compact" and "Flattened" JSON-LD outputs, and not required
     *
     * @param jsonLdContext the value of the "@context" node. Note the string is supposed to be a JSON Value: if passing an URI, the String must be quoted.
     */
    public void setJsonLDContextSubstitution(String jsonLdContext) {
        set(JsonLD10Writer.JSONLD_CONTEXT_SUBSTITUTION, jsonLdContext);
    }

    /**
     * Set the frame used in a "Frame" output
     * @param frame the Json Object used as frame for the "frame" output
     */
    public void setFrame(String frame) {
        set(JsonLD10Writer.JSONLD_FRAME, frame);
    }

    /**
     * Set the frame used in a "Frame" output
     * @param frame the frame Object expected by the JSON-LD java API
     */
    public void setFrame(Object frame) {
        set(JsonLD10Writer.JSONLD_FRAME, frame);
    }

}
