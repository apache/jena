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

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.lang.*;
import org.apache.jena.riot.lang.extra.TurtleJCC;
import org.apache.jena.riot.protobuf.ProtobufRDF;
import org.apache.jena.riot.protobuf.RiotProtobufException;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.thrift.RiotThriftException;
import org.apache.jena.riot.thrift.ThriftRDF;
import org.apache.jena.sparql.util.Context;

/** The registry of languages and parsers.
 * To register a new parser:
 * <ul>
 * <li>Register the language with {@link RDFLanguages}</li>
 * <li>Register the parser factory with {@link RDFParserRegistry}</li>
 * </ul>
 */

public class RDFParserRegistry
{
    // System defaults for JSON-LD writing in init().

    /** map language to a parser factory */
    private static Map<Lang, ReaderRIOTFactory> langToParserFactory    = new HashMap<>();

    /** Known triples languages */
    private static Set<Lang> langTriples  = new HashSet<>();

    /** Known quads languages */
    private static Set<Lang> langQuads    = new HashSet<>();

    private static boolean initialized = false;
    static { init(); }
    public static void init() {
        if ( initialized )
            return;
        initialized = true;
        initStandard();
    }

    private static void initStandard() {
        // Make sure the constants are initialized.
        RDFLanguages.init();

        /** General parser factory for parsers implemented by "Lang" */
        ReaderRIOTFactory parserFactory          = ReaderRIOTLang.factory;

        ReaderRIOTFactory parserFactoryRDFXML    = ReaderRIOTRDFXML.factory;
        ReaderRIOTFactory parserFactoryProtobuf  = ReaderRDFProtobuf.factory;
        ReaderRIOTFactory parserFactoryThrift    = ReaderRDFThrift.factory;
        ReaderRIOTFactory parserFactoryTriX      = ReaderTriX.factory;
        ReaderRIOTFactory parserFactoryRDFNULL   = ReaderRDFNULL.factory;

        registerLangTriples(NTRIPLES,   parserFactory);
        registerLangTriples(N3,         parserFactory);
        registerLangTriples(TURTLE,     parserFactory);
        registerLangTriples(RDFJSON,    parserFactory);
        registerLangTriples(RDFXML,     parserFactoryRDFXML);
        registerLangTriples(RDFPROTO,   parserFactoryProtobuf);
        registerLangTriples(RDFTHRIFT,  parserFactoryTriX);
        registerLangTriples(TRIX,       parserFactoryTriX);
        registerLangTriples(RDFNULL,    parserFactoryRDFNULL);

        // Keep here, not in statics, due to class initialization ordering effects.
        // JSON-LD
        ReaderRIOTFactory parserFactoryJsonLD10  = new ReaderRIOTFactoryJSONLD10();
        ReaderRIOTFactory parserFactoryJsonLD11  = new ReaderRIOTFactoryJSONLD11();

        // ==== JSON-LD system default for parsing.
        ReaderRIOTFactory jsonldReadDefault = parserFactoryJsonLD11;

        // Register default JSON-LD here.
        registerLangTriples(JSONLD,     jsonldReadDefault);
        registerLangTriples(JSONLD10,   parserFactoryJsonLD10);
        registerLangTriples(JSONLD11,   parserFactoryJsonLD11);

        registerLangQuads(NQUADS,       parserFactory);
        registerLangQuads(TRIG,         parserFactory);
        registerLangQuads(RDFPROTO,     parserFactoryProtobuf);
        registerLangQuads(RDFTHRIFT,    parserFactoryThrift);
        registerLangQuads(TRIX,         parserFactoryTriX);
        registerLangQuads(RDFNULL,      parserFactoryRDFNULL);

        registerLangQuads(JSONLD,       jsonldReadDefault);
        registerLangQuads(JSONLD10,     parserFactoryJsonLD10);
        registerLangQuads(JSONLD11,     parserFactoryJsonLD11);

        // Javacc based Turtle parser, different language name.
        // Lang = TurtleJCC.TTLJCC.
        // File extension = ".ttljcc"
        TurtleJCC.register();
    }

    /**
     * Register a language and it's parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    private static void registerLang(Lang lang, ReaderRIOTFactory factory) {
        RDFLanguages.register(lang);
        langToParserFactory.put(lang, factory);
    }

    /**
     * Register a language and its parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    public static void registerLangTriples(Lang lang, ReaderRIOTFactory factory) {
        langTriples.add(lang);
        registerLang(lang, factory);
    }

    /**
     * Register a language and its parser factory.
     * To create a {@link Lang} object use {@link LangBuilder}.
     */
    public static void registerLangQuads(Lang lang, ReaderRIOTFactory factory) {
        langQuads.add(lang);
        registerLang(lang, factory);
    }

    /** Remove registration */
    public static void removeRegistration(Lang lang) {
        RDFLanguages.unregister(lang);
        langToParserFactory.remove(lang);
    }

    /**
     * Return the parser factory for the language, or null if not registered. Use
     * {@code RDFParser.create() ... .build()}
     */
    public static ReaderRIOTFactory getFactory(Lang language) {
        return langToParserFactory.get(language);
    }

    /** return true if the language has a registered parser. */
    public static boolean isRegistered(Lang lang) { return langToParserFactory.containsKey(lang); }

    /** return true if the language is registered with the triples parser factories */
    public static boolean isTriples(Lang lang) { return langTriples.contains(lang); }

    /** return true if the language is registered with the quads parser factories */
    public static boolean isQuads(Lang lang)   { return langQuads.contains(lang); }

    // Parsers and factories.

    private static class ReaderRIOTLang implements ReaderRIOT
    {
        static ReaderRIOTFactory factory =
            (Lang lang, ParserProfile parserProfile) -> new ReaderRIOTLang(lang, parserProfile);

        private final Lang lang;
        private ParserProfile parserProfile = null;

        ReaderRIOTLang(Lang lang, ParserProfile parserProfile) {
            this.lang = lang;
            this.parserProfile = parserProfile;
        }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            LangRIOT parser = RiotParsers.createParser(in, lang, output, parserProfile);
            parser.parse();
        }

        @Override
        public void read(Reader in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            LangRIOT parser = RiotParsers.createParser(in, lang, output, parserProfile);
            parser.parse();
        }
    }

    private static class ReaderRIOTFactoryJSONLD10 implements ReaderRIOTFactory {
        @Override
        public ReaderRIOT create(Lang language, ParserProfile profile) {
            if ( !Lang.JSONLD.equals(language) && !Lang.JSONLD10.equals(language) )
                throw new InternalErrorException("Attempt to parse " + language + " as JSON-LD 1.0");
            // jsonld-java is JSON-LD 1.0
            return new LangJSONLD10(language, profile, profile.getErrorHandler());
        }
    }

    private static class ReaderRIOTFactoryJSONLD11 implements ReaderRIOTFactory {
        @Override
        public ReaderRIOT create(Lang language, ParserProfile profile) {
            if ( !Lang.JSONLD.equals(language) && !Lang.JSONLD11.equals(language) )
                throw new InternalErrorException("Attempt to parse " + language + " as JSON-LD 1.1");
            // Titanium json-ld for JSON-LD 1.1
            return new LangJSONLD11(language, profile, profile.getErrorHandler());
        }
    }

    private static class ReaderRDFProtobuf implements ReaderRIOT {
        static ReaderRIOTFactory factory = (Lang language, ParserProfile profile) -> new ReaderRDFProtobuf(profile);

        private final ParserProfile profile;

        public ReaderRDFProtobuf(ParserProfile profile) {
            this.profile = profile;
        }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            try {
                ProtobufRDF.inputStreamToStreamRDF(in, output);
            } catch (RiotProtobufException ex) {
                if ( profile != null && profile.getErrorHandler() != null )
                    profile.getErrorHandler().error(ex.getMessage(), -1, -1);
                else
                    ErrorHandlerFactory.errorHandlerStd.error(ex.getMessage(), -1, -1);
                throw ex;
            }
        }

        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            throw new RiotException("RDF Protobuf : Reading binary data from a java.io.reader is not supported. Please use an InputStream");
        }
    }

    private static class ReaderRDFThrift implements ReaderRIOT {
        static ReaderRIOTFactory factory = (Lang language, ParserProfile profile) -> new ReaderRDFThrift(profile);
        private final ParserProfile profile;
        public ReaderRDFThrift(ParserProfile profile) { this.profile = profile; }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            try {
                ThriftRDF.inputStreamToStream(in, output);
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
            throw new RiotException("RDF Thrift : Reading binary data from a java.io.reader is not supported. Please use an InputStream");
        }
    }
}

