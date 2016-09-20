/* Created on 31 août 2016 */
package org.apache.jena.riot;

import org.apache.jena.riot.out.JsonLDWriter;
import org.apache.jena.sparql.util.Context;

import com.github.jsonldjava.core.JsonLdOptions;

/**
 * The set of parameters that can be used to control the writing of JSON-LD.
 */
public class JsonLDWriteContext extends Context {
    /**
     * Set the JSON-LD java API's options
     * 
     * If not set, a default value is used.
     * 
     * @param opts the options as defined by the JSON-LD java API
     */
    public void setOptions(JsonLdOptions opts) {     
        set(JsonLDWriter.JSONLD_OPTIONS, opts);
    }

    /**
     * Set the value of the JSON-LD "@context" node, used in "Compact" and "Flattened" JSON-LD outputs.
     * 
     * It is not required to set this parameter: if it is not set,
     * a value for the "@Context" node is computed, based on the content of the dataset and its prefix mappings.
     * 
     * If an URI is passed, the JSONLD-code java will attempt to dereference it.
     * 
     * @param jsonLdContext the value of the "@context" node. Note that it is a Json Value: if passing an URI, the String must be quoted.
     */
    public void setJsonLdContext(String jsonLdContext) {
        set(JsonLDWriter.JSONLD_CONTEXT, jsonLdContext);
    }

    //        /**
    //         * Allow to replace the content of the "@context" node with a given value.
    //         * 
    //         * This is useful, for instance, to set the "@context" to an URI, without requiring to dereference the URI in question:
    //         * the context used to compute the JSONLD output is the one normally used (as defined by a call to -
    //         * or the lack of call to - setJsonLdContext) 
    //         * 
    //         * (only useful for "Compact" and "Flattened" JSON-LD outputs).
    //         * 
    //         * @param jsonLdContext the value of the "@context" node.
    //         */
    //        public void setJsonLdContextSubstitution(JsonString jsonLdContext) {
    //            setJsonLdContextSubstitution(jsonLdContext.toString());       
    //        }

    /**
     * Allow to replace the content of the "@context" node with a given value.
     * 
     * This is useful, for instance, to set the "@context" to an URI, without requiring to dereference the URI in question:
     * the context used to compute the JSONLD output is the one normally used (as defined by a call to -
     * or the lack of call to - setJsonLdContext) 
     * 
     * (only useful for "Compact" and "Flattened" JSON-LD outputs).
     * 
     * @param jsonLdContext the value of the "@context" node. Note that it is a Json Value: if passing an URI, the String must be quoted.
     */
    public void setJsonLdContextSubstitution(String jsonLdContext) {
        set(JsonLDWriter.JSONLD_CONTEXT_SUBSTITUTION, jsonLdContext);       
    }

    //        public void setFrame(JsonObject frame) {
    //            setFrame(frame.toString());
    //        }

    /**
     * Set the frame used in a "Frame" output
     * @param frame the Json Object used as frame for the "frame" output
     */
    public void setFrame(String frame) {
        set(JsonLDWriter.JSONLD_FRAME, frame);
    }           
}
