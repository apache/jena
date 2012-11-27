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

package org.openjena.riot;

import static org.openjena.riot.WebContent.contentTypeN3 ;
import static org.openjena.riot.WebContent.contentTypeN3Alt1 ;
import static org.openjena.riot.WebContent.contentTypeN3Alt2 ;
import static org.openjena.riot.WebContent.contentTypeNQuads ;
import static org.openjena.riot.WebContent.contentTypeNQuadsAlt1 ;
import static org.openjena.riot.WebContent.contentTypeNQuadsAlt2 ;
import static org.openjena.riot.WebContent.contentTypeNTriples ;
import static org.openjena.riot.WebContent.contentTypeNTriplesAlt ;
import static org.openjena.riot.WebContent.contentTypeRDFXML ;
import static org.openjena.riot.WebContent.contentTypeTriG ;
import static org.openjena.riot.WebContent.contentTypeTriGAlt1 ;
import static org.openjena.riot.WebContent.contentTypeTriGAlt2 ;
import static org.openjena.riot.WebContent.contentTypeTurtle ;
import static org.openjena.riot.WebContent.contentTypeTurtleAlt1 ;
import static org.openjena.riot.WebContent.contentTypeTurtleAlt2 ;

import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.web.TypedInputStream ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Retrieve data from the web */
public class WebReader
{
    /* where files are "on the web" */
    
    // TODO base URIs.

    // Reuse FileManager and LocationMapper.
    public static void readGraph(Graph graph, String uri)
    {
        Lang lang = Lang.guess(uri) ;
        readGraph(graph, uri, lang) ;
    }
    
    public static void readGraph(Graph graph, String uri, Lang lang)
    {
        TypedInputStream typedInput = open(uri, lang) ;
        String contentType = typedInput.getMediaType() ;
        lang = chooseLang(contentType, lang) ;

        if ( lang == null )
            throw new RiotException("Can't determine the syntax of <"+uri+"> (media type="+typedInput.getMediaType()+")") ;
        
        Sink<Triple> sink = RiotLoader.graphSink(graph) ;
        try {
            RiotLoader.readTriples(typedInput, lang, uri, sink) ;
        } finally { sink.close() ; }
    }

    
    static private Lang chooseLang(String contentType, Lang lang)
    {
        contentType = contentType.toLowerCase() ;
        return contentTypeToLang.get(contentType) ;
    }
    
    public static void readDataset(DatasetGraph dataset, String uri)
    {
        Lang lang = Lang.guess(uri) ;
        readDataset(dataset, uri, lang) ;
    }
    
    public static void readDataset(DatasetGraph dataset, String uri, Lang lang)
    {
        TypedInputStream typedInput = open(uri, lang) ;
        String contentType = typedInput.getMediaType() ;
        lang = chooseLang(contentType, lang) ;

        if ( lang == null )
            throw new RiotException("Can't determine the syntax of <"+uri+"> (media type="+typedInput.getMediaType()+")") ;
        
        Sink<Quad> sink = RiotLoader.datasetSink(dataset) ;
        try {
            RiotLoader.readQuads(typedInput, lang, uri, sink) ;
        } finally { sink.close() ; }
    }
    
    private static TypedInputStream open(String uri, Lang lang)
    {
        // Partial/
        try {
        InputStream in = new FileInputStream(uri) ; 
        // **** A FileManager that deals in TypedStreams properly (copy/rewrite)
        return new TypedInputStream(in, lang.getContentType()/*Content-Type*/, null/*charset*/) ;
    } catch (IOException ex) { IO.exception(ex) ; return null ; }
    }
    
    // -----------------------
    // Extensibility.
    interface Process<T> { void parse(InputStream inputStream) ; }
    // Name?
    interface SinkTriplesFactory { Process<Triple> create(String contentType) ; } 
    
    static private Map<String, Lang> contentTypeToLang = new HashMap<String, Lang>() ;

//    static public void addReader(String contentType, SinkTriplesFactory implFactory) {}
//    static public void removeReader(String contentType) {}

    // -----------------------

    // No need for this - use content type to get lang.
    
    /** Media type name to language */ 
    static {
        contentTypeToLang.put(contentTypeN3.toLowerCase(), Lang.N3) ;
        contentTypeToLang.put(contentTypeN3Alt1.toLowerCase(), Lang.N3) ;
        contentTypeToLang.put(contentTypeN3Alt2.toLowerCase(), Lang.N3) ;

        contentTypeToLang.put(contentTypeTurtle.toLowerCase(), Lang.TURTLE) ;
        contentTypeToLang.put(contentTypeTurtleAlt1.toLowerCase(), Lang.TURTLE) ;
        contentTypeToLang.put(contentTypeTurtleAlt2.toLowerCase(), Lang.TURTLE) ;

        contentTypeToLang.put(contentTypeNTriples.toLowerCase(), Lang.NTRIPLES) ;
        contentTypeToLang.put(contentTypeNTriplesAlt.toLowerCase(), Lang.NTRIPLES) ;
        
        contentTypeToLang.put(contentTypeRDFXML.toLowerCase(), Lang.RDFXML) ;
        
        contentTypeToLang.put(contentTypeTriG.toLowerCase(), Lang.TRIG) ;
        contentTypeToLang.put(contentTypeTriGAlt1.toLowerCase(), Lang.TRIG) ;
        contentTypeToLang.put(contentTypeTriGAlt2.toLowerCase(), Lang.TRIG) ;

        contentTypeToLang.put(contentTypeNQuads.toLowerCase(), Lang.NQUADS) ;
        contentTypeToLang.put(contentTypeNQuadsAlt1.toLowerCase(), Lang.NQUADS) ;
        contentTypeToLang.put(contentTypeNQuadsAlt2.toLowerCase(), Lang.NQUADS) ;
    }
}
