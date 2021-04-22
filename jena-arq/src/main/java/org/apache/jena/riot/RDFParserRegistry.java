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

import static org.apache.jena.riot.Lang.*;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.lang.* ;
import org.apache.jena.riot.lang.extra.TurtleJCC;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.thrift.BinRDF ;
import org.apache.jena.riot.thrift.RiotThriftException;
import org.apache.jena.sparql.util.Context ;

/** The registry of languages and parsers.
 * To register a new parser:
 * <ul>
 * <li>Register the language with {@link RDFLanguages}</li>
 * <li>Register the parser factory with {@link RDFParserRegistry}</li>
 * </ul>
 */

public class RDFParserRegistry
{
    /** map language to a parser factory */
    private static Map<Lang, ReaderRIOTFactory> langToParserFactory    = new HashMap<>() ;

    /** Known triples languages */
    private static Set<Lang> langTriples  = new HashSet<>() ;

    /** Known quads languages */
    private static Set<Lang> langQuads    = new HashSet<>() ;


    private static boolean initialized = false ;
    static { init() ; }
    public static void init()
    {
        if ( initialized ) return ;
        initialized = true ;
        initStandard() ;
    }

    private static void initStandard()
    {
        // Make sure the constants are initialized.
        RDFLanguages.init() ;

        /** General parser factory for parsers implemented by "Lang" */
        ReaderRIOTFactory parserFactory          = ReaderRIOTLang.factory;
        // Others
        ReaderRIOTFactory parserFactoryRDFXML    = ReaderRIOTRDFXML.factory ;
        ReaderRIOTFactory parserFactoryJsonLD    = new ReaderRIOTFactoryJSONLD() ;
        ReaderRIOTFactory parserFactoryThrift    = ReaderRDFThrift.factory;
        ReaderRIOTFactory parserFactoryTriX      = ReaderTriX.factory;
        ReaderRIOTFactory parserFactoryRDFNULL   = ReaderRDFNULL.factory;

        registerLangTriples(NTRIPLES,   parserFactory) ;
        registerLangTriples(N3,         parserFactory) ;
        registerLangTriples(TURTLE,     parserFactory) ;
        registerLangTriples(RDFJSON,    parserFactory) ;
        registerLangTriples(RDFXML,     ReaderRIOTRDFXML.factory) ;
        registerLangTriples(JSONLD,     parserFactoryJsonLD) ;
        registerLangTriples(RDFTHRIFT,  ReaderRDFThrift.factory) ;
        registerLangTriples(TRIX,       ReaderTriX.factory) ;
        registerLangTriples(RDFNULL,    ReaderRDFNULL.factory) ;

        registerLangQuads(JSONLD,       parserFactoryJsonLD) ;
        registerLangQuads(NQUADS,       parserFactory) ;
        registerLangQuads(TRIG,         parserFactory) ;
        registerLangQuads(RDFTHRIFT,    parserFactoryThrift) ;
        registerLangQuads(TRIX,         parserFactoryTriX) ;
        registerLangQuads(RDFNULL,      parserFactoryRDFNULL) ;

        // Javacc based Turtle parser, different language name.
        TurtleJCC.register();
    }

    /** Register a language and it's parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    private static void registerLang(Lang lang, ReaderRIOTFactory factory)
    {
        RDFLanguages.register(lang) ;
        langToParserFactory.put(lang, factory) ;
    }

    /** Register a language and its parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    public static void registerLangTriples(Lang lang, ReaderRIOTFactory factory)
    {
        langTriples.add(lang) ;
        registerLang(lang, factory) ;
    }

    /** Register a language and its parser factory.
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

    /**
     * Return the parser factory for the language, or null if not registered.
     * Use {@code RDFParser.create() ... .build()}
     */
    public static ReaderRIOTFactory getFactory(Lang language)
    {
        return langToParserFactory.get(language) ;
    }

    /** return true if the language has a registered parser. */
    public static boolean isRegistered(Lang lang) { return langToParserFactory.containsKey(lang) ; }

    /** return true if the language is registered with the triples parser factories */
    public static boolean isTriples(Lang lang) { return langTriples.contains(lang) ; }

    /** return true if the language is registered with the quads parser factories */
    public static boolean isQuads(Lang lang)   { return langQuads.contains(lang) ; }

    // Parsers and factories.

    private static class ReaderRIOTLang implements ReaderRIOT
    {
        static ReaderRIOTFactory factory =
            (Lang lang, ParserProfile parserProfile) -> new ReaderRIOTLang(lang, parserProfile);

        private final Lang lang ;
        private ParserProfile parserProfile = null ;

        ReaderRIOTLang(Lang lang, ParserProfile parserProfile) {
            this.lang = lang ;
            this.parserProfile = parserProfile;
        }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            // Unnecessary - RDFParser did it and set it in the ParserProfile
//            if ( baseURI != null ) {
//                IRIResolver newResolver = IRIResolver.create(baseURI) ;
//                parserProfile.setIRIResolver(newResolver);
//            }
            LangRIOT parser = RiotParsers.createParser(in, lang, output, parserProfile);
            parser.parse() ;
        }

        @Override
        public void read(Reader in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            // Unnecessary - RDFParser did it and set it in the ParserProfile
//          if ( baseURI != null ) {
//              IRIResolver newResolver = IRIResolver.create(baseURI) ;
//              parserProfile.setIRIResolver(newResolver);
//          }
            LangRIOT parser = RiotParsers.createParser(in, lang, output, parserProfile);
            parser.parse() ;
        }
    }

    private static class ReaderRIOTFactoryJSONLD implements ReaderRIOTFactory {
        @Override
        public ReaderRIOT create(Lang language, ParserProfile profile) {
            if ( !Lang.JSONLD.equals(language) )
                throw new InternalErrorException("Attempt to parse " + language + " as JSON-LD") ;
            return new JsonLDReader(language, profile, profile.getErrorHandler());
        }
    }

    private static class ReaderRDFThrift implements ReaderRIOT {
        static ReaderRIOTFactory factory = (Lang language, ParserProfile profile) -> new ReaderRDFThrift(profile);
        private final ParserProfile profile;
        public ReaderRDFThrift(ParserProfile profile) { this.profile = profile; }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            try {
                BinRDF.inputStreamToStream(in, output);
            } catch (RiotThriftException ex) {
                if ( profile != null && profile.getErrorHandler() != null )
                    profile.getErrorHandler().error(ex.getMessage(), -1, -1);
                else
                    ErrorHandlerFactory.errorHandlerStd.error(ex.getMessage(), -1 , -1);
                throw ex;
            }
        }

        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            throw new RiotException("RDF Thrift : Reading binary data from a java.io.reader is not supported. Please use an InputStream") ;
        }
    }
}

