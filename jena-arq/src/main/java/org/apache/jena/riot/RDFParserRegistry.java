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

import static org.apache.jena.riot.RDFLanguages.NQUADS ;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES ;
import static org.apache.jena.riot.RDFLanguages.RDFJSON ;
import static org.apache.jena.riot.RDFLanguages.RDFXML ;
import static org.apache.jena.riot.RDFLanguages.TRIG ;
import static org.apache.jena.riot.RDFLanguages.N3 ;
import static org.apache.jena.riot.RDFLanguages.TURTLE ;

import java.io.InputStream ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.sparql.util.Context ;
//import org.apache.jena.atlas.lib.Sink ;

/** The registry of languages and parsers.
 * To register a new parser:
 * <ul>
 * <li>Register the language with {@link RDFLanguages}</li>
 * <li>Register the parser factory with {@link RDFParserRegistry}</li>
 * </ul>
 */

public class RDFParserRegistry
{
    /** Map Jena I/O names to language */
    private static Map<String, Lang> mapJenaNameToLang                 = DS.map() ;

    /** map language to a parser factory */ 
    private static Map<Lang, ReaderRIOTFactory> langToParserFactory  = DS.map() ;
    
    /** Known triples languages */
    private static Set<Lang> langTriples  = DS.set() ;

    /** Known quads languages */
    private static Set<Lang> langQuads    = DS.set() ;

    /** Generic parser factory. */
    private static ReaderRIOTFactory parserFactory = new ReaderRIOTFactoryImpl() ;
    
    private static boolean initialized = false ;
    static { init() ; }
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
        
        registerShortNameForLang("N-TRIPLE",        NTRIPLES) ;
        registerShortNameForLang("N-TRIPLES",       NTRIPLES) ;
        registerShortNameForLang("NTRIPLE",         NTRIPLES) ;
        registerShortNameForLang("NTRIPLES",        NTRIPLES) ;
        registerShortNameForLang("NT",              NTRIPLES) ;
            
        registerShortNameForLang("TURTLE",          TURTLE) ;
        registerShortNameForLang("TTL",             TURTLE) ;

        registerShortNameForLang("RDF/JSON",        RDFJSON) ;
        registerShortNameForLang("RDFJSON",         RDFJSON) ;
        
        registerShortNameForLang("N-QUADS",         NQUADS) ;
        registerShortNameForLang("NQUADS",          NQUADS) ;
        registerShortNameForLang("N-QUAD",          NQUADS) ;
        registerShortNameForLang("NQUAD",           NQUADS) ;
        registerShortNameForLang("NQ",              NQUADS) ;
            
        registerShortNameForLang("TRIG",            TRIG) ;
        
        registerLangTriples(RDFXML,     parserFactory) ;
        registerLangTriples(NTRIPLES,   parserFactory) ;
        registerLangTriples(N3,         parserFactory) ;
        registerLangTriples(TURTLE,     parserFactory) ;
        registerLangTriples(RDFJSON,    parserFactory) ;
        
        registerLangQuads(NQUADS,       parserFactory) ;
        registerLangQuads(TRIG,         parserFactory) ;
    }

    /** Register a Jena IO name */
    private static void registerShortNameForLang(String name, Lang lang)
    {
        mapJenaNameToLang.put(RDFLanguages.canonicalKey(name), lang) ;
    }

    /** Register a language and it's parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    private static void registerLang(Lang lang, ReaderRIOTFactory factory)
    {
        RDFLanguages.register(lang) ;
        langToParserFactory.put(lang, factory) ;
    }
    
    /** Register a language and it's parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    public static void registerLangTriples(Lang lang, ReaderRIOTFactory factory)
    {
        langTriples.add(lang) ;
        registerLang(lang, factory) ;
    }
    
    /** Register a language and it's parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    public static void registerLangQuads(Lang lang, ReaderRIOTFactory factory)
    {
        langQuads.add(lang) ;
        registerLang(lang, factory) ;
    }

    /** Remove registration */
    public static void removeRegistration(Lang lang)
    {
        RDFLanguages.unregister(lang) ;
        langToParserFactory.remove(lang) ;
    }
    
    /** Return the parser factory for the language, or null if not registered */
    public static ReaderRIOTFactory getFactory(Lang language)
    {
        return langToParserFactory.get(language) ;
    }

    /** return true if the language is registered with the triples parser factories */
    public static boolean isTriples(Lang lang) { return langTriples.contains(lang) ; }
    
    /** return true if the language is registered with the quads parser factories */
    public static boolean isQuads(Lang lang)   { return langQuads.contains(lang) ; }

    // Parser factories
    
    private static class ReaderRIOTFactoryImpl implements ReaderRIOTFactory
    {
        @Override
        public ReaderRIOT create(final Lang lang)
        {
            return new ReaderRIOT() {
                @Override
                public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context)
                {
                    LangRIOT parser = RiotReader.createParser(in, lang, baseURI, output) ;
                    parser.parse() ;
                }
            } ;
        }
    } ;
}

