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

package org.apache.jena.riot.writer ;

import static org.apache.jena.graph.Triple.ANY;
import static org.apache.jena.rdf.model.impl.Util.isLangString;
import static org.apache.jena.rdf.model.impl.Util.isSimpleString;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonGenerationException ;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException ;
import com.github.jsonldjava.core.*;
import com.github.jsonldjava.utils.JsonUtils ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF ;

/**
 * Writer that prints out JSON-LD.
 *
 * By default, the output is "compact" (in JSON-LD terminology), and the JSON is "pretty" (using line breaks).
 * One can choose another form using one of the dedicated RDFFormats (JSONLD_EXPAND_PRETTY, etc.).
 *
 * For formats using a context (that is, which have an "@context" node), (compact and expand),
 * this automatically generates a default one.
 *
 * One can pass a jsonld context using the (jena) Context mechanism, defining a (jena) Context
 * (sorry for this clash of "contexts"), (cf. last argument in
 * {@link WriterDatasetRIOT#write(OutputStream out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context)})
 * with:
 * <pre>
 * Context jenaContext = new Context()
 * jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT, contextAsJsonString);
 * </pre>
 * where contextAsJsonString is a JSON string containing the value of the "@context".
 *
 * It is possible to change the content of the "@context" node in the output using the {@link #JSONLD_CONTEXT_SUBSTITUTION} Symbol.
 *
 * For a frame output, one must pass a frame in the jenaContext using the {@link #JSONLD_FRAME} Symbol.
 *
 * It is also possible to define the different options supported
 * by JSONLD-java using the {@link #JSONLD_OPTIONS} Symbol
 *
 * The {@link org.apache.jena.riot.JsonLDWriteContext} is a convenience class that extends Context and
 * provides methods to set the values of these different Symbols that are used in controlling the writing of JSON-LD.
 *
 * Note that this class also provides a static method to convert jena RDF data to the corresponding object in JsonLD API:
 * {@link #toJsonLDJavaAPI(org.apache.jena.riot.RDFFormat.JSONLDVariant, DatasetGraph, PrefixMap, String, Context)}
 */
public class JsonLDWriter extends WriterDatasetRIOTBase
{
    private static final String SYMBOLS_NS = "http://jena.apache.org/riot/jsonld#" ;
    private static Symbol createSymbol(String localName) {
        return Symbol.create(SYMBOLS_NS + localName);
    }
    /**
     * Expected value: the value of the "@context"
     * (a JSON String, or the object expected by the JSONLD-java API) */
    public static final Symbol JSONLD_CONTEXT = createSymbol("JSONLD_CONTEXT");
    /**
     * Expected value: the value of the "@context" to be put in final output (a JSON String)
     * This is NOT the context used to produce the output (given by JSONLD_CONTEXT,
     * or computed from the input RDF. It is something that will replace the @context content.
     * This is useful<ol><li>for the cases you want to have a URI as value of @context,
     * without having JSON-LD java to download it and</li><li>as a trick to
     * change the URIs in your result.</li></ol>
     *
     * Only for compact and flatten formats.
     *
     * Note that it is supposed to be a JSON String: to set the value of @context to a URI,
     * the String must be quoted.*/
    public static final Symbol JSONLD_CONTEXT_SUBSTITUTION = createSymbol("JSONLD_CONTEXT_SUBSTITUTION");
    /** value: a JSON String, or the frame object expected by JsonLdProcessor.frame */
    public static final Symbol JSONLD_FRAME = createSymbol("JSONLD_FRAME");
    /** value: the option object expected by JsonLdProcessor (instance of JsonLdOptions) */
    public static final Symbol JSONLD_OPTIONS = createSymbol("JSONLD_OPTIONS");
    /**
     * if creating a (jsonld) context from dataset, should we include all the prefixes defined in graph's prefix mappings
     * value: a Boolean (default: true) */
    public static final Symbol JSONLD_ADD_ALL_PREFIXES_TO_CONTEXT = createSymbol("JSONLD_ADD_ALL_PREFIXES_TO_CONTEXT");

    private final RDFFormat format ;

    public JsonLDWriter(RDFFormat syntaxForm) {
        format = syntaxForm ;
    }

    @Override
    public Lang getLang() {
        return format.getLang() ;
    }

    @Override
    public void write(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        serialize(out, dataset, prefixMap, baseURI, context) ;
    }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        Writer w = new OutputStreamWriter(out, Chars.charsetUTF8) ;
        write(w, dataset, prefixMap, baseURI, context) ;
        IO.flush(w) ;
    }

    private RDFFormat.JSONLDVariant getVariant() {
        return (RDFFormat.JSONLDVariant) format.getVariant();
    }

    static private JsonLdOptions getJsonLdOptions(String baseURI, Context jenaContext) {
        JsonLdOptions opts = null;
        if (jenaContext != null) {
            opts = (JsonLdOptions) jenaContext.get(JSONLD_OPTIONS);
        }
        if (opts == null) {
            opts = defaultJsonLdOptions(baseURI);
        }
        return opts;
    }

    // jena is not using same default as JSONLD-java
    // maybe we should have, but it's too late now:
    // changing it now would imply some unexpected changes in current users' outputs
    static private JsonLdOptions defaultJsonLdOptions(String baseURI) {
        JsonLdOptions opts = new JsonLdOptions(baseURI);
        opts.useNamespaces = true ; // this is NOT jsonld-java's default
        // opts.setUseRdfType(true); // false -> use "@type"
        opts.setUseNativeTypes(true); // this is NOT jsonld-java's default
        opts.setCompactArrays(true); // this is jsonld-java's default
        return opts;
    }

    private void serialize(Writer writer, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context jenaContext) {
        try {
            Object obj = toJsonLDJavaAPI(getVariant(), dataset, prefixMap, baseURI, jenaContext);
            if (getVariant().isPretty()) {
                JsonUtils.writePrettyPrint(writer, obj) ;
            } else {
                JsonUtils.write(writer, obj) ;
            }
            writer.write("\n") ;

        } catch (JsonLdError | JsonMappingException | JsonGenerationException e) {
            throw new RiotException(e) ;
        } catch (IOException e) {
            IO.exception(e) ;
        }
    }

    /**
     * the JsonLD-java API object corresponding to a dataset and a JsonLD format.
     */
    static public Object toJsonLDJavaAPI(RDFFormat.JSONLDVariant variant, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context jenaContext) throws JsonLdError, JsonParseException, IOException {
        JsonLdOptions opts = getJsonLdOptions(baseURI, jenaContext) ;

        // we can benefit from the fact we know that there are no duplicates in the jsonld RDFDataset that we create
        // see https://github.com/jsonld-java/jsonld-java/pull/173

        // with this, we cannot call the json-ld fromRDF method that assumes no duplicates in RDFDataset
        // Object obj = JsonLdProcessor.fromRDF(dataset, opts, new JenaRDF2JSONLD()) ;
        final RDFDataset jsonldDataset = (new JenaRDF2JSONLD()).parse(dataset);
        Object obj = (new JsonLdApi(opts)).fromRDF(jsonldDataset, true); // true because we know that we don't have any duplicate in jsonldDataset

        if (variant.isExpand()) {
            // nothing more to do

        } else if (variant.isFrame()) {
            Object frame = null;
            if (jenaContext != null) {
                frame = jenaContext.get(JSONLD_FRAME);
            }
            if (frame == null) {
                throw new IllegalArgumentException("No frame object found in jena Context");
            }

            if (frame instanceof String) {
                frame = JsonUtils.fromString((String) frame);
            }
            obj = JsonLdProcessor.frame(obj, frame, opts);

        } else { // compact or flatten
            // we need a (jsonld) context. Get it from jenaContext, or create one:
            Object ctx = getJsonldContext(dataset, prefixMap, jenaContext);

            if (variant.isCompact()) {
                obj = JsonLdProcessor.compact(obj, ctx, opts);

            } else if (variant.isFlatten()) {
                obj = JsonLdProcessor.flatten(obj, ctx, opts);

            } else {
                throw new IllegalArgumentException("Unexpected " + RDFFormat.JSONLDVariant.class.getName() + ": " + variant);
            }

            // replace @context in output?
            if (jenaContext != null) {
                Object ctxReplacement = jenaContext.get(JSONLD_CONTEXT_SUBSTITUTION);
                if (ctxReplacement != null) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        if (map.containsKey("@context")) {
                            map.put("@context", JsonUtils.fromString(ctxReplacement.toString()));
                        }
                    }
                }
            }
        }
        return obj;
    }

    //
    // getting / creating a (jsonld) context
    //

    /** Get the (jsonld) context from the jena context, or create one */
    private static Object getJsonldContext(DatasetGraph dataset, PrefixMap prefixMap, Context jenaContext) throws JsonParseException, IOException {
        Object ctx = null;
        boolean isCtxDefined = false; // to allow jenaContext to set ctx to null. Useful?

        if (jenaContext != null) {
            if (jenaContext.isDefined(JSONLD_CONTEXT)) {
                isCtxDefined = true;
                Object o = jenaContext.get(JSONLD_CONTEXT);
                if (o != null) {
                    if (o instanceof String) { // supposed to be a json string
                        String jsonString = (String) o;
                        ctx = JsonUtils.fromString(jsonString);
                    } else {
                        ctx = o;
                    }
                }
            }
        }

        if (!isCtxDefined) {
            // if no ctx passed via jenaContext, create one in order to have localnames as keys for properties
            ctx = createJsonldContext(dataset.getDefaultGraph(), prefixMap, addAllPrefixesToContextFlag(jenaContext)) ;

            // I don't think this should be done: the JsonLdProcessor begins
            // by looking whether the argument passed is a map with key "@context" and if so, takes corresponding value
            // Then, better not to do this: we create a map for nothing, and worse,
            // if the context object has been created by a user and passed through the (jena) context
            // in case the user got the same idea, we would end up with 2 levels of maps and it would not work
            //        Map<String, Object> localCtx = new HashMap<>() ;
            //        localCtx.put("@context", ctx) ;
        }
        return ctx;
    }

    static Object createJsonldContext(Graph g) {
        return createJsonldContext(g, PrefixMapFactory.create(g.getPrefixMapping()), true);
    }

    private static Object createJsonldContext(Graph g, PrefixMap prefixMap, boolean addAllPrefixesToContext) {
        final Map<String, Object> ctx = new LinkedHashMap<>() ;

        // Add properties (in order to get: "localname": ....)
        addProperties(ctx, g);

        // Add prefixes
        addPrefixes(ctx, g, prefixMap, addAllPrefixesToContext);

        return ctx ;
    }

    /** Add properties to jsonld context. */
    static void addProperties(Map<String, Object> ctx, Graph g) {
        Consumer<Triple> x = (Triple item) -> {
            Node p = item.getPredicate() ;
            Node o = item.getObject() ;

            if ( p.equals(RDF.type.asNode()) )
                return ;
            // JENA-1744 : split as a "Curie" (at the last / or #, regardless of the characters in a local name).
            // Curie : https://www.w3.org/TR/curie/
            String uriStr = SplitIRI.localname(p.getURI());

            if ( ctx.containsKey(uriStr) ) {
            } else if ( o.isBlank() || o.isURI() ) {
                // add property as a property (the object is an IRI)
                Map<String, Object> x2 = new LinkedHashMap<>() ;
                x2.put("@id", p.getURI()) ;
                x2.put("@type", "@id") ;
                ctx.put(uriStr, x2) ;
            } else if ( o.isLiteral() ) {
                String literalDatatypeURI = o.getLiteralDatatypeURI() ;
                if ( literalDatatypeURI != null ) {
                    // add property as a typed attribute (the object is a
                    // typed literal)
                    Map<String, Object> x2 = new LinkedHashMap<>() ;
                    x2.put("@id", p.getURI()) ;
                    if (! isLangString(o) && ! isSimpleString(o) )
                        // RDF 1.1 : Skip if rdf:langString or xsd:string.
                        x2.put("@type", literalDatatypeURI) ;
                    ctx.put(uriStr, x2) ;
                } else {
                    // add property as an untyped attribute (the object is
                    // an untyped literal)
                    ctx.put(uriStr, p.getURI()) ;
                }
            }
        } ;
        g.find(ANY).forEach(x);
    }

    /**
     * Add the prefixes to jsonld context.
     *
     * @param ctx
     * @param g
     * @param prefixMap
     * @param addAllPrefixesToContext true to add all prefixes in prefixMap to the jsonld context,
     * false to only add those which are actually used in g (false is useful for instance
     * when downloading schema.org: we get a very long list of prefixes.
     */
    // if adding all the prefixes in PrefixMap to ctx
    // one pb is, many of the prefixes may be actually unused in the graph.
    // This happens for instance when downloading schema.org: a very long list of prefixes
    // hence the addAllPrefixesToContext param
    private static void addPrefixes(Map<String, Object> ctx, Graph g, PrefixMap prefixMap, boolean addAllPrefixesToContext) {
        if (prefixMap != null) {
            Map<String, String> mapping = prefixMap.getMapping();
            if (addAllPrefixesToContext) {
                for ( Entry<String, String> e : mapping.entrySet() ) {
                    addOnePrefix(ctx, e.getKey(), e.getValue());
                }
            } else {
                // only add those that are actually used
                Consumer<Triple> x = new Consumer<Triple>() {
                    @Override
                    public void accept(Triple item) {
                        Node node = item.getSubject();
                        if (node.isURI()) addPrefix2Ctx(node.getURI());
                        node = item.getPredicate() ;
                        addPrefix2Ctx(node.getURI());
                        node = item.getObject() ;
                        if (node.isURI()) addPrefix2Ctx(node.getURI());
                    }

                    private void addPrefix2Ctx(String resUri) {
                        Pair<String, String> pair = prefixMap.abbrev(resUri);
                        if (pair != null) {
                            String prefix = pair.getLeft();
                            addOnePrefix(ctx, prefix, mapping.get(prefix).toString());
                        }
                    }
                } ;
                g.find(ANY).forEachRemaining(x);
            }
        }
    }

    /** Add one prefix to jsonld context */
    static void addOnePrefix(Map<String, Object> ctx, String prefix, String value) {
        if (!prefix.isEmpty()) { // Prefix "" is not allowed in JSON-LD
            ctx.put(prefix, value);
        } else {
            ctx.put("@vocab", value);
        }
    }

    private static boolean addAllPrefixesToContextFlag(Context jenaContext) {
        if (jenaContext != null) {
            Object o = jenaContext.get(JSONLD_ADD_ALL_PREFIXES_TO_CONTEXT);
            if (o != null) {
                if (o instanceof Boolean) {
                    return ((Boolean) o).booleanValue();
                } else {
                    throw new IllegalArgumentException("Value attached to JSONLD_ADD_ALL_PREFIXES_TO_CONTEXT shoud be a Boolean");
                }
            }
        }
        // default
        return true;
    }
}
