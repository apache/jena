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

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.github.jsonldjava.core.JSONLD ;
import com.github.jsonldjava.core.JSONLDProcessingError ;
import com.github.jsonldjava.core.JSONLDTripleCallback ;
import com.github.jsonldjava.core.RDFDataset ;
import com.github.jsonldjava.utils.JSONUtils ;
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

    @Override public ErrorHandler getErrorHandler() { return errorHandler ; }
    @Override public void setErrorHandler(ErrorHandler errorHandler) { this.errorHandler = errorHandler ; }
    
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Object jsonObject = JSONUtils.fromReader(reader) ;
            read$(jsonObject, baseURI, ct, output, context) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        try {
            Object jsonObject = JSONUtils.fromInputStream(in) ;
            read$(jsonObject, baseURI, ct, output, context) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }

    private void read$(Object jsonObject, String baseURI, ContentType ct, final StreamRDF output, Context context) {
        try {
            JSONLDTripleCallback callback = new JSONLDTripleCallback() {
                @Override
                // public Object call(Map<String, Object> dataset) {
                public Object call(RDFDataset dataset) {
                    for ( String gn : dataset.keySet() ) {
                        Object x = dataset.get(gn) ;
                        if ( "@default".equals(gn) ) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> triples = (List<Map<String, Object>>)x ;
                            for ( Map<String, Object> t : triples ) {
                                Node s = createNode(t, "subject") ;
                                Node p = createNode(t, "predicate") ;
                                Node o = createNode(t, "object") ;
                                Triple triple = Triple.create(s, p, o) ;
                                output.triple(triple) ;
                            }
                        } else {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> quads = (List<Map<String, Object>>)x ;
                            Node g = NodeFactory.createURI(gn) ; // Bnodes?
                            for ( Map<String, Object> q : quads ) {
                                Node s = createNode(q, "subject") ;
                                Node p = createNode(q, "predicate") ;
                                Node o = createNode(q, "object") ;
                                output.quad(Quad.create(g, s, p, o)) ;
                            }
                        }
                    }
                    return null ;
                }
            } ;
            JSONLD.toRDF(jsonObject, callback) ;
        }
        catch (JSONLDProcessingError e) {
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
            return NodeFactory.createURI(lex) ;
        else if ( type.equals(BLANK_NODE) )
            return labels.get(null, lex) ;
        else if ( type.equals(LITERAL) ) {
            String lang = (String)map.get("language") ;
            String datatype = (String)map.get("datatype") ;
            if ( Lib.equal(xsdString, datatype) )
                // In RDF 1.1, simple literals and xsd:string are the same.
                // During migration, we prefer simple literals to xsd:strings. 
                datatype = null ;
            if ( lang == null && datatype == null )
                return NodeFactory.createLiteral(lex) ;
            if ( lang != null )
                return NodeFactory.createLiteral(lex, lang, null) ;
            RDFDatatype dt = NodeFactory.getType(datatype) ;
            return NodeFactory.createLiteral(lex, dt) ;
        } else
            throw new InternalErrorException("Node is not a IRI, bNode or a literal: " + type) ;
    }

    private Node createURI(String str) {
        if ( str.startsWith("_:") )
            return labels.get(null, str) ;
        else
            return NodeFactory.createURI(str) ;
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
