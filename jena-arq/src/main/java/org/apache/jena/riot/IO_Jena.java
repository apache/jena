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

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.WebReader2.RDFReaderRIOT_NT ;
import org.apache.jena.riot.WebReader2.RDFReaderRIOT_RDFJSON ;
import org.apache.jena.riot.WebReader2.RDFReaderRIOT_RDFXML ;
import org.apache.jena.riot.WebReader2.RDFReaderRIOT_TTL ;
import org.apache.jena.riot.stream.StreamManager ;
import org.apache.jena.riot.system.JenaWriterRdfJson ;
import org.openjena.riot.RiotNotFoundException ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class IO_Jena
{
    static Logger log = LoggerFactory.getLogger(IO_Jena.class) ;
    
    private static String riotBase = "http://jena.apache.org/riot/" ; 
    private static String streamManagerSymbolStr = riotBase+"streammanager" ; 
    public static Symbol streamManagerSymbol = Symbol.create(streamManagerSymbolStr) ; 

    public static void wireIntoJena()
    {
//        // Wire in generic 
//        String readerRDF = RDFReaderRIOT.class.getName() ;
//        RDFReaderFImpl.setBaseReaderClassName("RDF/XML",    readerRDF) ;           // And default
//        RDFReaderFImpl.setBaseReaderClassName("RDF/XML-ABBREV", readerRDF) ;
//
//        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES",  readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",   readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("N3",         readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("TURTLE",     readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("Turtle",     readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("TTL",        readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("RDF/JSON",   readerRDF) ;
        
      RDFReaderFImpl.setBaseReaderClassName("RDF/XML",    RDFReaderRIOT_RDFXML.class.getName()) ;           // And default
      RDFReaderFImpl.setBaseReaderClassName("RDF/XML-ABBREV", RDFReaderRIOT_RDFXML.class.getName()) ;

      RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES",  RDFReaderRIOT_NT.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",   RDFReaderRIOT_NT.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("N3",         RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("TURTLE",     RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("Turtle",     RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("TTL",        RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("RDF/JSON",   RDFReaderRIOT_RDFJSON.class.getName()) ;
      
      // Old style Jena writers.
      String writerRdfJson = JenaWriterRdfJson.class.getName() ;
      RDFWriterFImpl.setBaseWriterClassName("RDF/JSON", writerRdfJson) ;
    }
    
//    // Yukky hack to integrate into current jena-core where the structure of model.read assumes
//    // the language is determined before the reading process starts.
//    
//    static class RDFReaderRIOT_RDFXML extends RDFReaderRIOT   { public RDFReaderRIOT_RDFXML() { super("RDF/XML") ; } }
//    static class RDFReaderRIOT_TTL extends RDFReaderRIOT      { public RDFReaderRIOT_TTL() { super("TTL") ; } }
//    static class RDFReaderRIOT_NT extends RDFReaderRIOT       { public RDFReaderRIOT_NT() { super("N-TRIPLE") ; } }
//    static class RDFReaderRIOT_RDFJSON extends RDFReaderRIOT  { public RDFReaderRIOT_RDFJSON() { super("RDF/JSON") ; } }

    static String jenaNTriplesReader = "com.hp.hpl.jena.rdf.model.impl.NTripleReader" ; 
    static String jenaTurtleReader = "com.hp.hpl.jena.n3.turtle.TurtleReader" ; 
    static String jenaN3Reader = jenaTurtleReader ; 
    
    public static void resetJenaReaders()
    {
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", jenaNTriplesReader) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",  jenaNTriplesReader) ;
        
        RDFReaderFImpl.setBaseReaderClassName("N3",     jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TURTLE", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("Turtle", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TTL",    jenaTurtleReader) ;

        RDFReaderFImpl.setBaseReaderClassName("RDF/JSON", "") ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/JSON", "") ;

    }
    
    // --- IO
    
    /** Open a stream to the destination (URI or filename)
     * Performs content negotition, including looking at file extension. 
     */
    public static TypedInputStream open(String filenameOrURI)
    { return open(filenameOrURI, (Context)null) ; }
    
    /** Open a stream to the destination (URI or filename)
     * Performs content negotition, including looking at file extension. 
     */
    public static TypedInputStream open(String filenameOrURI, Context context)
    {
        StreamManager sMgr = StreamManager.get() ;
        if ( context != null )
        {
            try { sMgr = (StreamManager)context.get(streamManagerSymbol, context) ; }
            catch (ClassCastException ex) 
            { log.warn("Context symbol '"+streamManagerSymbol+"' is not a "+Utils.classShortName(StreamManager.class)) ; }
        }
        
        return open(filenameOrURI, sMgr) ;
    }
    
    public static TypedInputStream open(String filenameOrURI, StreamManager sMgr)
    {
        TypedInputStream in = sMgr.open(filenameOrURI) ;
            
        if ( in == null )
        {
            if ( log.isDebugEnabled() )
                //log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
                log.debug("Not Found: "+filenameOrURI) ;
            throw new RiotNotFoundException("Not found: "+filenameOrURI) ;
            //return null ;
        }
        if ( log.isDebugEnabled() )
            //log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
            log.debug("Found: "+filenameOrURI) ;
        return in ;
    }
    
    protected static ContentType determineCT(String target, String ctStr, Lang2 hintLang)
    {
        if ( ctStr != null )
            ctStr = WebContent.contentTypeCanonical(ctStr) ;
        
        boolean isTextPlain = WebContent.contentTypeTextPlain.equals(ctStr) ;
        ContentType ct = (ctStr==null) ? null : ContentType.parse(ctStr) ;
        
        // It's it's text plain, we ignore it because a lot of naive
        // server setups return text/plain for any file type.
        
        if ( ct == null || isTextPlain )
        {
            if ( hintLang == null )
                ct = RDFLanguages.guessContentType(target) ;
            else
            {
                ct = hintLang.getContentType() ;
//                if ( ct == null )
//                {
//                    // Is the hint a content type?
//                    Lang2 lang = RDFLanguages.contentTypeToLang(hintLang) ;
//                    if ( lang != null )
//                        ct = lang.getContentType() ;
//                }
            }
        }
        return ct ;
    }

    // --- Syntax framework
//  public static void addTripleSyntax(Lang2 language, ContentType contentType, ReaderRIOTFactory<Triple> factory, String ... fileExt )
//  { 
//      RDFLanguages.addTripleSyntax$(language, contentType, factory, fileExt) ;
//  } 
//  
//  public static void addQuadSyntax(Lang2 language, ContentType contentType, ReaderRIOTFactory<Quad> factory, String ... fileExt )
//  {
//      RDFLanguages.addQuadSyntax$(language, contentType, factory, fileExt) ;
//  }

    
}

