/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import static org.openjena.riot.WebContent.contentTypeN3 ;
import static org.openjena.riot.WebContent.contentTypeN3Alt1 ;
import static org.openjena.riot.WebContent.contentTypeN3Alt2 ;
import static org.openjena.riot.WebContent.contentTypeNQuads ;
import static org.openjena.riot.WebContent.contentTypeNQuadsAlt ;
import static org.openjena.riot.WebContent.contentTypeNTriples ;
import static org.openjena.riot.WebContent.contentTypeNTriplesAlt ;
import static org.openjena.riot.WebContent.contentTypeRDFXML ;
import static org.openjena.riot.WebContent.contentTypeTriG ;
import static org.openjena.riot.WebContent.contentTypeTriGAlt ;
import static org.openjena.riot.WebContent.contentTypeTurtle1 ;
import static org.openjena.riot.WebContent.contentTypeTurtle2 ;
import static org.openjena.riot.WebContent.contentTypeTurtle3 ;

import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.TypedInputStream ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Retrieve data from the web */
public class WebReader
{
    /* where file are "on the web" */

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
        RiotLoader.readTriples(typedInput, lang, uri, sink) ;
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
        RiotLoader.readQuads(typedInput, lang, uri, sink) ;
    }
    
    private static TypedInputStream open(String uri, Lang lang)
    {
        // **** A FileManager that deals in TypedStreams properly (copy/rewrite)
        return new TypedInputStream(null, null/*Content-Type*/, null/*charset*/) ;
    }
    
    // -----------------------
    // Extensibility.
    interface Process<T> { void parse(InputStream inputStream) ; }
    // Name?
    interface SinkTriplesFactory { Process<Triple> create(String contentType) ; } 
    static public void addReader(String contentType, SinkTriplesFactory implFactory) {}

    // -----------------------

    // No need for this - use content type to get lang.
    
    /** Media type name to language */ 
    static Map<String, Lang> contentTypeToLang = new HashMap<String, Lang>() ;
    static {
        contentTypeToLang.put(contentTypeN3.toLowerCase(), Lang.N3) ;
        contentTypeToLang.put(contentTypeN3Alt1.toLowerCase(), Lang.N3) ;
        contentTypeToLang.put(contentTypeN3Alt2.toLowerCase(), Lang.N3) ;

        contentTypeToLang.put(contentTypeTurtle1.toLowerCase(), Lang.TURTLE) ;
        contentTypeToLang.put(contentTypeTurtle2.toLowerCase(), Lang.TURTLE) ;
        contentTypeToLang.put(contentTypeTurtle3.toLowerCase(), Lang.TURTLE) ;

        contentTypeToLang.put(contentTypeNTriples.toLowerCase(), Lang.NTRIPLES) ;
        contentTypeToLang.put(contentTypeNTriplesAlt.toLowerCase(), Lang.NTRIPLES) ;
        
        contentTypeToLang.put(contentTypeRDFXML.toLowerCase(), Lang.RDFXML) ;
        
        contentTypeToLang.put(contentTypeTriG.toLowerCase(), Lang.TRIG) ;
        contentTypeToLang.put(contentTypeTriGAlt.toLowerCase(), Lang.TRIG) ;

        contentTypeToLang.put(contentTypeNQuads.toLowerCase(), Lang.NQUADS) ;
        contentTypeToLang.put(contentTypeNQuadsAlt.toLowerCase(), Lang.NQUADS) ;
    }
}

/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */