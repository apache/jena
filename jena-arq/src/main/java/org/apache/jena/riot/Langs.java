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

import java.io.InputStream ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.Locale ;
import java.util.Map ;

import org.openjena.atlas.lib.DS ;
import org.openjena.atlas.lib.MultiMap ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.ContentType ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.WebContent ;
import org.openjena.riot.lang.LangRIOT ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.FileUtils ;

public class Langs
{
    // Constants and wiring. 

    // Languages as constants.  
    // Lang is an "open enum" (you can add your own),
    // Registered with their MIME type or official name,
    static Lang2 langRDFXML     = Lang2.create("RDF/XML",   WebContent.contentTypeRDFXML) ;
    static Lang2 langTurtle     = Lang2.create("Turtle",    WebContent.contentTypeTurtle) ;
    static Lang2 langNTriples   = Lang2.create("N-Triples", WebContent.contentTypeNTriplesAlt) ; // Exception: not text/plain.
    static Lang2 langN3         = Lang2.create("N3",        WebContent.contentTypeN3) ;
    static Lang2 langRDFJSON    = Lang2.create("RDF/JSON",  WebContent.contentTypeRDFJSON) ;
    // JSON-LD
    
    static Lang2 langNQuads     = Lang2.create("NQuads",    WebContent.contentTypeNQuads) ;
    static Lang2 langTriG       = Lang2.create("TriG",      WebContent.contentTypeTriG) ;

    private static Map<Lang2, ReaderRIOTFactory<Triple>> langToTriples  = DS.map() ;
    private static Map<Lang2, ReaderRIOTFactory<Quad>> langToQuads      = DS.map() ;
    private static Map<String, Lang2> mapContentTypeToLang              = DS.map() ;
    private static MultiMap<Lang2, String> langToExt                    = MultiMap.createMapList() ;
    private static Map<String, Lang2> extToLang                         = DS.map() ;
    private static Map<String, Lang2> shortNameToLang                   = DS.map() ;
   
    // This code understands different types of things it can read:
    //   triples, quads, result sets, unknown
    // These form (disjoint) value spaces static ReaderFactory<Triple> pfTriples = new ReaderFactory<Triple>() {
    // This is needed because we need to determine the output type of the reading process.
    // The decision is driven by the Content-Type.

    // Triples : Generic parser factory.
    static ReaderRIOTFactory<Triple> pfTriples = new ReaderRIOTFactory<Triple>() {
        @Override
        public ReaderRIOT<Triple> create(final Lang2 language)
        {
            return new ReaderRIOT<Triple>() {
                // Needs sorting out
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, Sink<Triple> sink, Context context)
                {
                    Lang lang = convert(language) ;
                    LangRIOT parser = RiotReader.createParserTriples(in, lang, baseURI, sink) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;

    // Quads : Generic parser factory.
    static ReaderRIOTFactory<Quad> pfQuads = new ReaderRIOTFactory<Quad>() {
        @Override
        public ReaderRIOT<Quad> create(final Lang2 language)
        {
            return new ReaderRIOT<Quad>() {
                // Needs sorting out
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, Sink<Quad> sink, Context context)
                {
                    Lang lang = convert(language) ;
                    LangRIOT parser = RiotReader.createParserQuads(in, lang, baseURI, sink) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;

    static { initStandard(); }
    
    private static void addLangTriplesFactory$(Lang2 lang, ReaderRIOTFactory<Triple> factory)
    {
        langToTriples.put(lang, factory) ;
    }
        

    private static void addLangQuadFactory$(Lang2 lang, ReaderRIOTFactory<Quad> factory)
    {
        langToQuads.put(lang, factory) ;
    }
    
    /*package*/ static ReaderRIOTFactory<Triple> getFactoryTriples(Lang2 language)
    {
        return langToTriples.get(language) ;
    }
    
    /*package*/ static ReaderRIOTFactory<Quad> getFactoryQuads(Lang2 language)
    {
        return langToQuads.get(language) ;
    }

    private static void registerShortNameForLang(String name, Lang2 lang)
    {
        shortNameToLang.put(lowerCase(name), lang) ;
    }

    /** Turn a short name for a language into a Lang object. 
     */

    private static Lang2 shortNameToLang(String name)
    {
        return shortNameToLang.get(lowerCase(name)) ;
    }
    
    /** Turn a for a language into a Lang object.
     *  The name can be a short form, or a content type.
     */
    public static Lang2 nameToLang(String langName)
    { 
        if ( langName == null )
            return null ;
        Lang2 lang = shortNameToLang(langName) ;
        if ( lang != null )
            return lang ;
        lang = contentTypeToLang(langName) ;
        return lang ;
    }
    
    private static String lowerCase(String x) { return x.toLowerCase(Locale.US) ; }
    
    static void addTripleSyntax$(Lang2 language, String contentType, ReaderRIOTFactory<Triple> factory, String ... fileExt)
    {
        addTripleSyntax$(language, ContentType.parse(contentType), factory, fileExt) ;
    }
    
    static void addTripleSyntax$(Lang2 language, ContentType contentType, ReaderRIOTFactory<Triple> factory, String ... fileExt)
    { 
        if ( fileExt != null )
            extension(language, fileExt) ;
        addLangTriplesFactory$(language, factory) ;
        addContentTypeLang(contentType, language) ;
    } 
    
    static void addQuadSyntax$(Lang2 language, String contentType, ReaderRIOTFactory<Quad> factory, String ... fileExt)
    {
        addQuadSyntax$(language, ContentType.parse(contentType), factory, fileExt) ;
    }

    static void addQuadSyntax$(Lang2 language, ContentType contentType, ReaderRIOTFactory<Quad> factory, String ... fileExt)
    {
        extension(language, fileExt) ;
        addLangQuadFactory$(language, factory) ;
        addContentTypeLang(contentType, language) ;
    }
    
    private static void extension(Lang2 lang, String ... exts)
    {
        langToExt.putAll(lang, exts) ;
        for ( String ext : exts )
            extToLang.put(ext, lang) ;
    }

    // Initialize standard setup.
    // In Webreader2?
    private static void initStandard()
    {
        // RDF/XML
        addTripleSyntax$(langRDFXML, WebContent.contentTypeRDFXML, pfTriples,       "rdf", "owl", "xml") ;
        
        // Turtle
        addTripleSyntax$(langTurtle, WebContent.contentTypeTurtle, pfTriples,       "ttl") ;
        addContentTypeLang$(WebContent.contentTypeTurtleAlt1, langTurtle) ;
        addContentTypeLang$(WebContent.contentTypeTurtleAlt2, langTurtle) ;

        // N-triples
        addTripleSyntax$(langNTriples, WebContent.contentTypeNTriples, pfTriples,   "nt") ;
        addContentTypeLang$(WebContent.contentTypeNTriplesAlt, langNTriples) ;
        
        // N3 (redirect to Turtle)
        addTripleSyntax$(langN3, WebContent.contentTypeN3, pfTriples,               "n3") ;

        // RDF/JSON (this is not JSON-LD)
        addTripleSyntax$(langRDFJSON, WebContent.contentTypeRDFJSON, pfTriples,     "rj", "json") ;
        
        // TriG
        addQuadSyntax$(langTriG, WebContent.contentTypeTriG, pfQuads,               "trig") ;
        addContentTypeLang$(WebContent.contentTypeTriGAlt, langTriG) ;

        // N-Quads
        addQuadSyntax$(langNQuads, WebContent.contentTypeNQuads, pfQuads,           "nq") ;
        addContentTypeLang$(WebContent.contentTypeNQuadsAlt, langNQuads) ;
        
        // Reader name and variations to lang - must include Jena old-name.
        registerShortNameForLang("RDF/XML",         langRDFXML) ;
        registerShortNameForLang("RDFXML",          langRDFXML) ;
        registerShortNameForLang("RDF/XML-ABBREV",  langRDFXML) ;
        
        registerShortNameForLang("N-TRIPLE",        langNTriples) ;
        registerShortNameForLang("N-TRIPLES",       langNTriples) ;
        registerShortNameForLang("NTRIPLE",         langNTriples) ;
        registerShortNameForLang("NTRIPLES",        langNTriples) ;
        registerShortNameForLang("NT",              langNTriples) ;
            
        registerShortNameForLang("TURTLE",          langTurtle) ;
        registerShortNameForLang("TTL",             langTurtle) ;

        registerShortNameForLang("RDF/JSON",        langRDFJSON) ;
        registerShortNameForLang("RDFJSON",         langRDFJSON) ;
        
        registerShortNameForLang("N-QUADS",         langNQuads) ;
        registerShortNameForLang("NQUADS",          langNQuads) ;
        registerShortNameForLang("N-QUAD",          langNQuads) ;
        registerShortNameForLang("NQUAD",           langNQuads) ;
        registerShortNameForLang("NQ",              langNQuads) ;
            
        registerShortNameForLang("TRIG",            langTriG) ;
    }
    
    
    private static void addContentTypeLang$(String contentType, Lang2 lang)
    { addContentTypeLang(ContentType.parse(contentType), lang) ; }
    
    private static void addContentTypeLang(ContentType contentType, Lang2 lang)
    { mapContentTypeToLang.put(contentType.getContentType(), lang) ; }
    
    static Lang2 contentTypeToLang(String contentType)
    { return mapContentTypeToLang.get(contentType) ; }
    
    static Lang2 contentTypeToLang(ContentType contentType)
    { return contentTypeToLang(contentType.getContentType()) ; }

    // ** Lang upgrade.
    
    public static boolean isTriples(Lang2 lang) { return langToTriples.containsKey(lang) ; }
    public static boolean isQuads(Lang2 lang)   { return langToQuads.containsKey(lang) ; }

    
    /** Guess the language, based on filename, or URL, extenstion.
     * Returns null if there isn't a guess available
     */
    public static ContentType guessContentType(String filenameOrIRI)
    {
        Lang2 lang = guess(filenameOrIRI) ;
        if ( lang == null ) return null ;
        return lang.getContentType() ;
    }

    public static Lang2 guess(String resourceIRI)
    {
        if ( resourceIRI == null )
            return null ;
        String ext = FileUtils.getFilenameExt(resourceIRI).toLowerCase() ;
        if ( ext != null && ext.equals("gz") )
        {
            resourceIRI = resourceIRI.substring(0, resourceIRI.length()-".gz".length()) ;
            ext = FileUtils.getFilenameExt(resourceIRI).toLowerCase() ;
        }

        return extToLang.get(ext) ;
    }
    
    //** Merge with Lang.guess
    public static Lang2 guess(String resourceIRI, Lang2 dftLang)
    {
        Lang2 lang = guess(resourceIRI) ;
        if ( lang == null ) return dftLang ;
        return lang ;
    }
    
    /** Attempt to guess the content type string from a language name */   
    public static String shortNameToContentTypeStr(String jenaShortName)
    {
        return shortNameToContentTypeStr(jenaShortName, null) ;
    }

    /** Attempt to guess the content type string from a language name */   
    public static String shortNameToContentTypeStr(String jenaShortName, String dft)
    {
        ContentType ct = shortNameToContentType(jenaShortName) ;
        if ( ct == null )
            return dft ;
        return ct.getContentType() ;
    }
    
    /** Attempt to guess the content type string from a language name */   
    public static ContentType shortNameToContentType(String jenaShortName)
    {
        Lang2 lang = shortNameToLang(jenaShortName) ;
        if ( lang == null )
            return null ;
        return lang.getContentType() ;
    }
    
    public static String extensionFor(Lang2 lang)
    {
        Collection<String> x = extensionsFor(lang) ;
        Iterator<String> iter = x.iterator() ;
        if ( ! iter.hasNext() )
            return null ;
        return x.iterator().next() ;
    }

    public static Collection<String> extensionsFor(Lang2 lang)
    {
        return langToExt.get(lang) ;
    }

    
//    /** Attempt to guess the content type string from a language name */   
//    public static Lang2 jenaNameToLang(String jenaShortName)
//    {
//        return shortNameToLang.get(jenaShortName) ;
//    }
//
    static Lang convert(Lang2 language)
    {
        if ( language == null ) throw new RiotException("Null language") ;
        if ( language.equals(langRDFXML))     return Lang.RDFXML ;
        if ( language.equals(langTurtle))     return Lang.TURTLE ;
        if ( language.equals(langNTriples))   return Lang.NTRIPLES ;
        if ( language.equals(langNQuads))     return Lang.NQUADS ;
        if ( language.equals(langTriG))       return Lang.TRIG ;
        if ( language.equals(langN3))         return Lang.N3 ;
        if ( language.equals(langRDFJSON))    return Lang.RDFJSON ;
        throw new RiotException("Can't convert: "+language) ;
    }

}
