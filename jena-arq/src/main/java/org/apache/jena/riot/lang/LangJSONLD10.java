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

package org.apache.jena.riot.lang ;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.Reader ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context ;

import com.fasterxml.jackson.core.JsonLocation ;
import com.fasterxml.jackson.core.JsonProcessingException ;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils ;
import org.apache.jena.sparql.util.Symbol;

/**
 * One can pass a jsonld context using the (jena) Context mechanism, defining a (jena) Context
 * (sorry for this clash of "contexts"), (cf. last argument in
 * {@link ReaderRIOT#read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context)})
 * with:
 * <pre>
 * Context jenaContext = new Context()
 * jenaCtx.set(JsonLdReader.JSONLD_CONTEXT, contextAsJsonString);
 * </pre>
 * where contextAsJsonString is a JSON string containing the value of the "@context".
 *
 * It is also possible to define the different options supported
 * by JSONLD-java using the {@link #JSONLD_OPTIONS} Symbol
 *
 * The {@link JsonLDReadContext} is a convenience class that extends Context and
 * provides methods to set the values of these different Symbols that are used in controlling the writing of JSON-LD.
 * Note: it is possible to override jsonld's "@context" value by providing one,
 * using a {@link org.apache.jena.sparql.util.Context}, and setting the {@link LangJSONLD10#JSONLD_CONTEXT} Symbol's value
 * to the data expected by JSON-LD java API (a {@link Map}).
 */
public class LangJSONLD10 implements ReaderRIOT
{
    private static final String SYMBOLS_NS = "http://jena.apache.org/riot/jsonld#" ;
    private static Symbol createSymbol(String localName) {
        return Symbol.create(SYMBOLS_NS + localName);
    }
    /**
     * Symbol to use to pass (in a Context object) the "@context" to be used when reading jsonld
     * (overriding the actual @context in the jsonld)
     * Expected value: the value of the "@context",
     * as expected by the JSONLD-java API (a Map) */
    public static final Symbol JSONLD_CONTEXT = createSymbol("JSONLD_CONTEXT");
    /** value: the option object expected by JsonLdProcessor (instance of JsonLdOptions) */
    public static final Symbol JSONLD_OPTIONS = createSymbol("JSONLD_OPTIONS");
    private /*final*/ ErrorHandler errorHandler = ErrorHandlerFactory.getDefaultErrorHandler() ;
    private /*final*/ ParserProfile profile;
    
    public LangJSONLD10(Lang lang, ParserProfile profile, ErrorHandler errorHandler) {
        this.profile = profile;
        this.errorHandler = errorHandler;
    }
    
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Object jsonObject = JsonUtils.fromReader(reader) ;
            readWithJsonLDCtxOptions(jsonObject, baseURI, output, context) ;
        }
        catch (JsonProcessingException ex) {    
            // includes JsonParseException
            // The Jackson JSON parser, or addition JSON-level check, throws up something.
            JsonLocation loc = ex.getLocation() ;
            errorHandler.error(ex.getOriginalMessage(), loc.getLineNr(), loc.getColumnNr()); 
            throw new RiotException(ex.getOriginalMessage()) ;
        }
        catch (IOException e) {
            errorHandler.error(e.getMessage(), -1, -1); 
            IO.exception(e) ;
        }
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Object jsonObject = JsonUtils.fromInputStream(in) ;
            readWithJsonLDCtxOptions(jsonObject, baseURI, output, context) ;
        }
        catch (JsonProcessingException ex) {    
            // includes JsonParseException
            // The Jackson JSON parser, or addition JSON-level check, throws up something.
            JsonLocation loc = ex.getLocation() ;
            errorHandler.error(ex.getOriginalMessage(), loc.getLineNr(), loc.getColumnNr()); 
            throw new RiotException(ex.getOriginalMessage()) ;
        }
        catch (IOException e) {
            errorHandler.error(e.getMessage(), -1, -1); 
            IO.exception(e) ;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void readWithJsonLDCtxOptions(Object jsonObject, String baseURI, final StreamRDF output, Context context)  throws JsonParseException, IOException {
        JsonLdOptions options = getJsonLdOptions(baseURI, context) ;
        Object jsonldCtx = getJsonLdContext(context);
        if (jsonldCtx != null) {
            if (jsonObject instanceof Map) {
                ((Map) jsonObject).put("@context", jsonldCtx);
            } else {
                errorHandler.warning("Unexpected: not a Map; unable to set JsonLD's @context",-1,-1);
            }
        }
        read$(jsonObject, options, output);
    }

    private void read$(Object jsonObject, JsonLdOptions options, final StreamRDF output) {
        output.start() ;
        try {       	
            JsonLdTripleCallback callback = new JsonLdTripleCallback() {
                @Override
                public Object call(RDFDataset dataset) {
                	
                	// Copy across namespaces
                	for (Entry<String, String> namespace : dataset.getNamespaces().entrySet()) {
                		output.prefix(namespace.getKey(), namespace.getValue());
                	}
                	
                	// Copy triples and quads
                    for ( String gn : dataset.keySet() ) {
                        Object x = dataset.get(gn) ;
                        if ( "@default".equals(gn) ) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> triples = (List<Map<String, Object>>)x ;
                            for ( Map<String, Object> t : triples ) {
                                Node s = createNode(t, "subject") ;
                                Node p = createNode(t, "predicate") ;
                                Node o = createNode(t, "object") ;
                                Triple triple = profile.createTriple(s, p, o, -1, -1) ;
                                output.triple(triple) ;
                            }
                        } else {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> quads = (List<Map<String, Object>>)x ;
                            Node g = createURI(gn) ; 
                            for ( Map<String, Object> q : quads ) {
                                Node s = createNode(q, "subject") ;
                                Node p = createNode(q, "predicate") ;
                                Node o = createNode(q, "object") ;
                                Quad quad = profile.createQuad(g, s, p, o, -1, -1) ;
                                output.quad(quad) ;
                            }
                        }
                    }
                    return null ;
                }
            } ;
            JsonLdProcessor.toRDF(jsonObject, callback, options) ;
        }
        catch (JsonLdError e) {
            errorHandler.error(e.getMessage(), -1, -1); 
            throw new RiotException(e) ;
        }
        output.finish() ;
    }

    /** Get the (jsonld) options from the jena context if exists or create default */
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

    static private JsonLdOptions defaultJsonLdOptions(String baseURI) {
        JsonLdOptions opts = new JsonLdOptions(baseURI);
        opts.useNamespaces = true ; // this is NOT jsonld-java's default
        return opts;
    }

    /** Get the (jsonld) context from the jena context if exists */
    static private Object getJsonLdContext(Context jenaContext) throws JsonParseException, IOException {
        Object ctx = null;
        boolean isCtxDefined = false; // to allow jenaContext to set ctx to null. Useful?

        if (jenaContext != null) {
            if (jenaContext.isDefined(JSONLD_CONTEXT)) {
                Object o = jenaContext.get(JSONLD_CONTEXT);
                if (o != null) {
                    if (o instanceof String) { // supposed to be a json string
                        String jsonString = (String) o;
                        o = JsonUtils.fromString(jsonString);
                    }
                    ctx = o;
                }
            }
        }
        return ctx;
    }

    public static String LITERAL    = "literal" ;
    public static String BLANK_NODE = "blank node" ;
    public static String IRI        = "IRI" ;

    private Node createNode(Map<String, Object> tripleMap, String key) {
        @SuppressWarnings("unchecked")
        Map<String, Object> x = (Map<String, Object>)(tripleMap.get(key)) ;
        return createNode(x) ;
    }

    private static final String xsdString = XSDDatatype.XSDstring.getURI() ;
    
    private Node createNode(Map<String, Object> map) {
        String type = (String)map.get("type") ;
        String lex = (String)map.get("value") ;
        if ( type.equals(IRI) )
            return createURI(lex) ;
        else if ( type.equals(BLANK_NODE) )
            return createBlankNode(lex);
        else if ( type.equals(LITERAL) ) {
            String lang = (String)map.get("language") ;
            String datatype = (String)map.get("datatype") ;
            if ( Objects.equals(xsdString, datatype) )
                // In RDF 1.1, simple literals and xsd:string are the same.
                // During migration, we prefer simple literals to xsd:strings. 
                datatype = null ;
            if ( lang == null && datatype == null )
                return profile.createStringLiteral(lex,-1, -1) ;
            if ( lang != null )
                return profile.createLangLiteral(lex, lang, -1, -1) ;
            RDFDatatype dt = NodeFactory.getType(datatype) ;
            return profile.createTypedLiteral(lex, dt, -1, -1) ;
        } else
            throw new InternalErrorException("Node is not a IRI, bNode or a literal: " + type) ;
    }

    private Node createBlankNode(String str) {
        if ( str.startsWith("_:") )
            str = str.substring(2);
        return profile.createBlankNode(null, str, -1,-1);
    }

    private Node createURI(String str) {
        if ( str.startsWith("_:") )
            return createBlankNode(str);
        else
            return profile.createURI(str, -1, -1) ;
    }
}
