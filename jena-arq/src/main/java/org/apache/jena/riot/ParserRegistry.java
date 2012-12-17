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

import static org.apache.jena.riot.RDFLanguages.NQuads ;
import static org.apache.jena.riot.RDFLanguages.NTriples ;
import static org.apache.jena.riot.RDFLanguages.RDFJSON ;
import static org.apache.jena.riot.RDFLanguages.RDFXML ;
import static org.apache.jena.riot.RDFLanguages.TriG ;
import static org.apache.jena.riot.RDFLanguages.Turtle ;

import java.io.InputStream ;
import java.util.Map ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.lang.RDFParserOutput ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;

import com.hp.hpl.jena.sparql.util.Context ;
//import org.apache.jena.atlas.lib.Sink ;

/** The registry of languages and parsers.
 * To register a new parser:
 * 
 * 
 */

public class ParserRegistry
{
    /** Map Jena I/O names to language */
    private static Map<String, Lang2> mapJenaNameToLang                 = DS.map() ;

    /** map language to a triples parser */ 
    private static Map<Lang2, ReaderRIOTFactory> langToTriples  = DS.map() ;
    /** map language to a quads parser */ 
    private static Map<Lang2, ReaderRIOTFactory> langToQuads      = DS.map() ;

    /** Triples : Generic parser factory. */
    private static ReaderRIOTFactory pfTriples = new ReaderRIOTFactoryTriple() ;

    /** Quads : Generic parser factory. */
    private static ReaderRIOTFactory pfQuads = new ReaderRIOTFactoryQuads() ;

    private static boolean initialized = false ;
    public static synchronized void init ()
    {
        if ( initialized ) return ;
        initialized = true ;
        initStandard() ;
    }
    
    private static void initStandard()
    {
        // Make sure the constants are initialized.
        RDFLanguages.init() ;
        
        // Reader name and variations to lang - must include Jena old-name.
        registerShortNameForLang("RDF/XML",         RDFXML) ;
        registerShortNameForLang("RDFXML",          RDFXML) ;
        registerShortNameForLang("RDF/XML-ABBREV",  RDFXML) ;
        
        registerShortNameForLang("N-TRIPLE",        NTriples) ;
        registerShortNameForLang("N-TRIPLES",       NTriples) ;
        registerShortNameForLang("NTRIPLE",         NTriples) ;
        registerShortNameForLang("NTRIPLES",        NTriples) ;
        registerShortNameForLang("NT",              NTriples) ;
            
        registerShortNameForLang("TURTLE",          Turtle) ;
        registerShortNameForLang("TTL",             Turtle) ;

        registerShortNameForLang("RDF/JSON",        RDFJSON) ;
        registerShortNameForLang("RDFJSON",         RDFJSON) ;
        
        registerShortNameForLang("N-QUADS",         NQuads) ;
        registerShortNameForLang("NQUADS",          NQuads) ;
        registerShortNameForLang("N-QUAD",          NQuads) ;
        registerShortNameForLang("NQUAD",           NQuads) ;
        registerShortNameForLang("NQ",              NQuads) ;
            
        registerShortNameForLang("TRIG",            TriG) ;
        
        registerLangTriples(RDFXML,     pfTriples) ;
        registerLangTriples(NTriples,   pfTriples) ;
        registerLangTriples(Turtle,     pfTriples) ;
        registerLangTriples(RDFJSON,    pfTriples) ;
        
        registerLangQuads(NQuads,       pfQuads) ;
        registerLangQuads(TriG,         pfQuads) ;
    }

    /** Register a Jena IO name */
    private static void registerShortNameForLang(String name, Lang2 lang)
    {
        mapJenaNameToLang.put(RDFLanguages.canonicalKey(name), lang) ;
    }

    /** Register a language that parses to produces triples.
     * To create a {@link Lang2} object use {@link LangBuilder}.
     */
    public static void registerLangTriples(Lang2 lang, ReaderRIOTFactory factory)
    {
        RDFLanguages.register(lang) ;
        langToTriples.put(lang, factory) ;
    }
        
    /** Register a language that parses to produces quads */
    public static void registerLangQuads(Lang2 lang, ReaderRIOTFactory factory)
    {
        RDFLanguages.register(lang) ;
        langToQuads.put(lang, factory) ;
    }
    
    /** Remove registration */
    public static void removeRegistration(Lang2 lang)
    {
        RDFLanguages.unregister(lang) ;
        langToTriples.remove(lang) ;
        langToQuads.remove(lang) ;
    }
    
    /** Return the triples parser factory for the language, or null if not registered */
    public static ReaderRIOTFactory getFactoryTriples(Lang2 language)
    {
        return langToTriples.get(language) ;
    }
    
    /** Return the quads parser factory for the language, or null if not registered */
    public static ReaderRIOTFactory getFactoryQuads(Lang2 language)
    {
        return langToQuads.get(language) ;
    }
    
    /** return true if the language is registered with the triples parser factories */
    public static boolean isTriples(Lang2 lang) { return langToTriples.containsKey(lang) ; }
    
    /** return true if the language is registered with the quads parser factories */
    public static boolean isQuads(Lang2 lang)   { return langToQuads.containsKey(lang) ; }

    // Parser factories
    
    private static class ReaderRIOTFactoryTriple implements ReaderRIOTFactory
    {
        @Override
        public ReaderRIOT create(final Lang2 language)
        {
            return new ReaderRIOT() {
                // Needs sorting out
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, RDFParserOutput output, Context context)
                {
                    Lang lang = RDFLanguages.convert(language) ;
                    LangRIOT parser = RiotReader.createParserTriples(in, lang, baseURI, output) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;

    private static class ReaderRIOTFactoryQuads implements ReaderRIOTFactory {
        @Override
        public ReaderRIOT create(final Lang2 language)
        {
            return new ReaderRIOT() {
                // Needs sorting out
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, RDFParserOutput output, Context context)
                {
                    Lang lang = RDFLanguages.convert(language) ;
                    LangRIOT parser = RiotReader.createParserQuads(in, lang, baseURI, output) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;


}

