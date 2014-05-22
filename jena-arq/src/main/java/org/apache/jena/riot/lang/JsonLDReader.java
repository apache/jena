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

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.* ;

import com.github.jsonldjava.core.* ;
import com.github.jsonldjava.utils.JsonUtils ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;

public class JsonLDReader implements ReaderRIOT
{
    private ErrorHandler errorHandler = ErrorHandlerFactory.getDefaultErrorHandler() ;
    private ParserProfile parserProfile = null ; 
    
    @Override public ErrorHandler getErrorHandler() { return errorHandler ; }
    @Override public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler ; }
    
    @Override public ParserProfile getParserProfile()                   { return parserProfile ; }
    @Override public void setParserProfile(ParserProfile parserProfile) { this.parserProfile = parserProfile ; }
    
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        if ( parserProfile == null )
            parserProfile = RiotLib.profile(RDFLanguages.JSONLD, baseURI, errorHandler) ;
        try {
            Object jsonObject = JsonUtils.fromReader(reader) ;
            read$(jsonObject, baseURI, ct, output, context) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        if ( parserProfile == null )
            parserProfile = RiotLib.profile(RDFLanguages.JSONLD, baseURI, errorHandler) ;
        try {
            Object jsonObject = JsonUtils.fromInputStream(in) ;
            read$(jsonObject, baseURI, ct, output, context) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }
    
    private void read$(Object jsonObject, String baseURI, ContentType ct, final StreamRDF output, Context context) {
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
            if ( Lib.equal(xsdString, datatype) )
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
            return NodeFactory.createLiteral(lex, lang, null) ;
        RDFDatatype dt = NodeFactory.getType(datatype) ;
        return NodeFactory.createLiteral(lex, dt) ;
    }

}
