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
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.util.FileUtils ;

import com.fasterxml.jackson.core.* ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.github.jsonldjava.core.* ;

public class JsonLDReader implements ReaderRIOT
{
    private ErrorHandler errorHandler = ErrorHandlerFactory.getDefaultErrorHandler() ;
    private ParserProfile parserProfile = null ; 
    
    @Override public ErrorHandler getErrorHandler() { return errorHandler ; }
    @Override public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler ; }
    
    @Override public ParserProfile getParserProfile()                   { return parserProfile ; }
    @Override public void setParserProfile(ParserProfile parserProfile) { this.parserProfile = parserProfile ; }
    
    // pre jsonld-java issue #144 code.
    // Remove at any point.
//    @Override
//    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
//        try {
//            Object jsonObject = JsonUtils.fromReader(reader) ;
//            read$(jsonObject, baseURI, ct, output, context) ;
//        }
//        catch (IOException e) {
//            IO.exception(e) ;
//        }
//    }
//
//    @Override
//    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
//        try {
//            Object jsonObject = JsonUtils.fromInputStream(in) ;
//            read$(jsonObject, baseURI, ct, output, context) ;
//        }
//        catch (IOException e) {
//            IO.exception(e) ;
//        }
//    }
    
    // This addresses jsonld-java issue #144 so that we get triples/quads out
    // then there is a parse error. Even if it is fixed in jsonld-java, it would
    // mean that no triples would be produced - all the JSON parsing is done
    // before JSON-LD processing. Here we process the first JSON object, whch
    // causes triples to be generated then decide whether to throw a parse
    // error.  This is more in the style of other syntaxes and stream parsing.
    
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            readProcess(reader, 
                       (jsonObject)->read$(jsonObject, baseURI, ct, output, context)) ;
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
        Reader r = FileUtils.asBufferedUTF8(in) ;
        read(r, baseURI, ct, output, context) ;
    }

    // From JsonUtils.fromReader in the jsonld-java codebase.
    // Note that jsonld-java always uses a reader.
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final JsonFactory JSON_FACTORY = new JsonFactory(JSON_MAPPER);
    
    /** Read a JSON object from the reader, acall the prcoessing function, then
     * check for trailing content. For Jena, this means tripes/quads are generated
     * from a valid JSON object, then there is a  parse error for trailing junk.   
     * @param reader
     * @param function
     * @throws IOException
     */
    private static void readProcess(Reader reader, Consumer<Object> function) throws IOException {
        final JsonParser jp = JSON_FACTORY.createParser(reader);
        Object rval ;
        final JsonToken initialToken = jp.nextToken();

        if (initialToken == JsonToken.START_ARRAY) {
            rval = jp.readValueAs(List.class);
        } else if (initialToken == JsonToken.START_OBJECT) {
            rval = jp.readValueAs(Map.class);
        } else if (initialToken == JsonToken.VALUE_STRING) {
            rval = jp.readValueAs(String.class);
        } else if (initialToken == JsonToken.VALUE_FALSE || initialToken == JsonToken.VALUE_TRUE) {
            rval = jp.readValueAs(Boolean.class);
        } else if (initialToken == JsonToken.VALUE_NUMBER_FLOAT
                || initialToken == JsonToken.VALUE_NUMBER_INT) {
            rval = jp.readValueAs(Number.class);
        } else if (initialToken == JsonToken.VALUE_NULL) {
            rval = null;
        } else {
            throw new JsonParseException("document doesn't start with a valid json element : "
                    + initialToken, jp.getCurrentLocation());
        }
        
        function.accept(rval);
        
        JsonToken t ;
        try { t = jp.nextToken(); }
        catch (JsonParseException ex) {
            throw new JsonParseException("Document contains more content after json-ld element - (possible mismatched {}?)",
                                         jp.getCurrentLocation());
        }
        if ( t != null )
            throw new JsonParseException("Document contains possible json content after the json-ld element - (possible mismatched {}?)",
                                             jp.getCurrentLocation());
    }
    
    private void read$(Object jsonObject, String baseURI, ContentType ct, final StreamRDF output, Context context) {
        if ( parserProfile == null )
            parserProfile = RiotLib.profile(RDFLanguages.JSONLD, baseURI, errorHandler) ;
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
                                Triple triple = parserProfile.createTriple(s, p, o, -1, -1) ;
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
                                Quad quad = parserProfile.createQuad(g, s, p, o, -1, -1) ;
                                output.quad(quad) ;
                            }
                        }
                    }
                    return null ;
                }
            } ;
            JsonLdOptions options = new JsonLdOptions(baseURI);
            options.useNamespaces = true;
            JsonLdProcessor.toRDF(jsonObject, callback, options) ;
        }
        catch (JsonLdError e) {
            errorHandler.error(e.getMessage(), -1, -1); 
            throw new RiotException(e) ;
        }
        output.finish() ;
    }

    private LabelToNode  labels     = SyntaxLabels.createLabelToNode() ;

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
            return labels.get(null, lex) ;  //??
        else if ( type.equals(LITERAL) ) {
            String lang = (String)map.get("language") ;
            String datatype = (String)map.get("datatype") ;
            if ( Objects.equals(xsdString, datatype) )
                // In RDF 1.1, simple literals and xsd:string are the same.
                // During migration, we prefer simple literals to xsd:strings. 
                datatype = null ;
            if ( lang == null && datatype == null )
                return parserProfile.createStringLiteral(lex,-1, -1) ;
            if ( lang != null )
                return parserProfile.createLangLiteral(lex, lang, -1, -1) ;
            RDFDatatype dt = NodeFactory.getType(datatype) ;
            return parserProfile.createTypedLiteral(lex, dt, -1, -1) ;
        } else
            throw new InternalErrorException("Node is not a IRI, bNode or a literal: " + type) ;
    }

    private Node createURI(String str) {
        if ( str.startsWith("_:") )
            return labels.get(null, str) ;
        else
            return parserProfile.createURI(str, -1, -1) ;
    }

    private Node createLiteral(String lex, String datatype, String lang) {
        if ( lang == null && datatype == null )
            return NodeFactory.createLiteral(lex) ;
        if ( lang != null )
            return NodeFactory.createLiteral(lex, lang) ;
        RDFDatatype dt = NodeFactory.getType(datatype) ;
        return NodeFactory.createLiteral(lex, dt) ;
    }
}
