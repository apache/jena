/*
 * (c) Copyright 2010 Epimorphics Ltd.
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

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.util.FileManager ;

/** Retrieve data from the web */
public class WebReader
{
    // Other ops:
    // read from StringWriter
    // set the base.
    
    // Reuse FileManager and LocationMapper.
    public static void readGraph(Graph graph, String uri)
    {
        Lang lang = Lang.guess(uri) ;
        readGraph(graph, uri, lang) ;
    }
    
    public static void readGraph(Graph graph, String uri, Lang lang)
    {
        // file:
        
        //FileManager.get().open(uri) ;
        // Open URI -> TypedInputStream
        InputStream inputStream = null ;
        // Find ContentType.
        String contentType = null ;
        // Final decision.
        
        // Or Lang?
        lang = chooseLang(contentType, lang) ;
        
        // Choose reader - include lang in decision.
        
        SinkTriplesFactory factory = readerTriplesMap.get(contentType.toLowerCase()) ;
        Process<Triple> processor = factory.create(contentType) ;
        processor.parse(inputStream) ;
    }

    
    static private Lang chooseLang(String contentType, Lang lang)
    {
        return null ; //contentType ;
    }
    
    public static void readDataset(DatasetGraph graph, String uri)
    {
        
    }
    
    public static void readDataset(DatasetGraph graph, String uri, Lang lang)
    {
        
    }
    
    // -----------------------
    
    static public void addReader(String contentType, SinkTriplesFactory implFactory) {}
    
    
    // -----------------------

    interface Process<T> { void parse(InputStream inputStream) ; }
    // Name?
    interface SinkTriplesFactory { Process<Triple> create(String contentType) ; } 

    // No need for this - use content type to get lang.
    
    /** Content type to sink factory for triples */ 
    static Map<String, SinkTriplesFactory> readerTriplesMap = new HashMap<String, SinkTriplesFactory>() ;
    static {
        readerTriplesMap.put(contentTypeN3.toLowerCase(), null) ;
        readerTriplesMap.put(contentTypeN3Alt1.toLowerCase(), null) ;
        readerTriplesMap.put(contentTypeN3Alt2.toLowerCase(), null) ;

        readerTriplesMap.put(contentTypeTurtle1.toLowerCase(), null) ;
        readerTriplesMap.put(contentTypeTurtle2.toLowerCase(), null) ;
        readerTriplesMap.put(contentTypeTurtle3.toLowerCase(), null) ;

        readerTriplesMap.put(contentTypeRDFXML.toLowerCase(), null) ;
    }

    interface SinkQuadsFactory {} 

    /** Content type to sink factory for quads */ 
    static Map<String, SinkQuadsFactory> readerQuadsMap = new HashMap<String, SinkQuadsFactory>() ;

    static {
        readerQuadsMap.put(contentTypeNTriples.toLowerCase(), null) ;
        readerQuadsMap.put(contentTypeNTriplesAlt.toLowerCase(), null) ;

        readerQuadsMap.put(contentTypeTriG.toLowerCase(), null) ;
        readerQuadsMap.put(contentTypeTriGAlt.toLowerCase(), null) ;

        readerQuadsMap.put(contentTypeNQuads.toLowerCase(), null) ;
        readerQuadsMap.put(contentTypeNQuadsAlt.toLowerCase(), null) ;
    }

    //public static content
    
    // Lang.guess 
    
    // open(Name) -> types stream.
    //   files by ext
    //   c.f FileManager
    
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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