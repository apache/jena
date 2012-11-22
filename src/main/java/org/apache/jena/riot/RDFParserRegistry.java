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
import java.util.Map ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;

import org.apache.jena.riot.Lang2 ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.ReaderRIOTFactory ;
import org.openjena.atlas.lib.DS ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.ContentType ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;

import static org.apache.jena.riot.RDFLanguages.* ;

/** The registry of languages and parsers.
 * To register a new parser:
 * 
 * 
 */

public class RDFParserRegistry
{
    /** Map Jena I/O names to language */
    private static Map<String, Lang2> mapJenaNameToLang                 = DS.map() ;

    /** map language to a triples parser */ 
    private static Map<Lang2, ReaderRIOTFactory<Triple>> langToTriples  = DS.map() ;
    /** map language to a quads parser */ 
    private static Map<Lang2, ReaderRIOTFactory<Quad>> langToQuads      = DS.map() ;

    static { init() ; }
    
    /** Register a Jena IO name */
    private static void registerShortNameForLang(String name, Lang2 lang)
    {
        mapJenaNameToLang.put(RDFLanguages.canonicalKey(name), lang) ;
    }

    public static void init()
    {
        // Make sure the constants are initialized.
        RDFLanguages.init() ;
        
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
        
        registerLangTriples(langRDFXML, pfTriples) ;
        registerLangTriples(langNTriples, pfTriples) ;
        registerLangTriples(langTurtle, pfTriples) ;
        registerLangTriples(langRDFJSON, pfTriples) ;
        
        registerLangQuads(langNQuads, pfQuads) ;
        registerLangQuads(langTriG,   pfQuads) ;
    }

    // ****
    // getParserFor
    // triples vs quads
    
    /** Register a language that parses to produces triples.
     * To create a {@link Lang2} object use {@link LangBuilder}.
     */
    public static void registerLangTriples(Lang2 lang, ReaderRIOTFactory<Triple> factory)
    {
        RDFLanguages.register(lang) ;
        langToTriples.put(lang, factory) ;
    }
        
    /** Register a language that parses to produces quads */
    public static void registerLangQuads(Lang2 lang, ReaderRIOTFactory<Quad> factory)
    {
        RDFLanguages.register(lang) ;
        langToQuads.put(lang, factory) ;
    }
    
    public static void removeRegistration(Lang2 lang)
    {
        RDFLanguages.unregister(lang) ;
        langToTriples.remove(lang) ;
        langToQuads.remove(lang) ;
    }
    
    /** Return the triples parser factory for the language, or null if not registered */
    public static ReaderRIOTFactory<Triple> getFactoryTriples(Lang2 language)
    {
        return langToTriples.get(language) ;
    }
    
    /** Return the quads parser factory for the language, or null if not registered */
    public static ReaderRIOTFactory<Quad> getFactoryQuads(Lang2 language)
    {
        return langToQuads.get(language) ;
    }
    
    /** return true if the language is registered with the triples parser factories */
    public static boolean isTriples(Lang2 lang) { return langToTriples.containsKey(lang) ; }
    
    /** return true if the language is registered with the quads parser factories */
    public static boolean isQuads(Lang2 lang)   { return langToQuads.containsKey(lang) ; }

    // Triples : Generic parser factory.
    private static ReaderRIOTFactory<Triple> pfTriples = new ReaderRIOTFactory<Triple>() {
        @Override
        public ReaderRIOT<Triple> create(final Lang2 language)
        {
            return new ReaderRIOT<Triple>() {
                // Needs sorting out
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, Sink<Triple> sink, Context context)
                {
                    Lang lang = RDFLanguages.convert(language) ;
                    LangRIOT parser = RiotReader.createParserTriples(in, lang, baseURI, sink) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;

    // Quads : Generic parser factory.
    private static ReaderRIOTFactory<Quad> pfQuads = new ReaderRIOTFactory<Quad>() {
        @Override
        public ReaderRIOT<Quad> create(final Lang2 language)
        {
            return new ReaderRIOT<Quad>() {
                // Needs sorting out
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, Sink<Quad> sink, Context context)
                {
                    Lang lang = RDFLanguages.convert(language) ;
                    LangRIOT parser = RiotReader.createParserQuads(in, lang, baseURI, sink) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;
    
    // Kludge for now.
    private static Lang convert(Lang2 language)
    {
        return null ;
    }

}

