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

import java.util.*;

import org.apache.jena.riot.lang.*;
import org.apache.jena.riot.lang.extra.TurtleJCC;
import org.apache.jena.riot.lang.rdfxml.RRX;
import org.apache.jena.riot.lang.rdfxml.rrx.LangRDFXML_SAX;

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

        registerLangTriples(NTRIPLES,   RiotParsers.factoryNT);
        registerLangTriples(N3,         RiotParsers.factoryTTL);
        registerLangTriples(TURTLE,     RiotParsers.factoryTTL);

        registerLangTriples(RDFJSON,    RiotParsers.factoryRDFJSON);

        registerLangTriples(RDFXML,     LangRDFXML_SAX.factory);
        registerLangTriples(RDFPROTO,   RiotParsers.factoryRDFProtobuf);
        registerLangTriples(RDFTHRIFT,  RiotParsers.factoryRDFThrift);

        registerLangTriples(TRIX,       ReaderTriX.factory);
        registerLangTriples(RDFNULL,    ReaderRDFNULL.factory);

        // Register default JSON-LD here.
        registerLangTriples(JSONLD,     RiotParsers.factoryJSONLD);
        registerLangTriples(JSONLD11,   RiotParsers.factoryJSONLD);

        registerLangQuads(NQUADS,       RiotParsers.factoryNQ);
        registerLangQuads(TRIG,         RiotParsers.factoryTRIG);
        registerLangQuads(RDFPROTO,     RiotParsers.factoryRDFProtobuf);
        registerLangQuads(RDFTHRIFT,    RiotParsers.factoryRDFThrift);
        registerLangQuads(TRIX,         ReaderTriX.factory);
        registerLangQuads(RDFNULL,      ReaderRDFNULL.factory);

        registerLangQuads(JSONLD,       RiotParsers.factoryJSONLD);
        registerLangQuads(JSONLD11,     RiotParsers.factoryJSONLD);

        // Javacc based Turtle parser, different language name.
        // Lang = TurtleJCC.TTLJCC.
        // File extension = ".ttljcc"
        TurtleJCC.register();
        // Languagfes to access specific RDF parsers
        RRX.register();
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

    /** Return registered triple languages. */
    public static Collection<Lang> registeredLangTriples() {
        return Set.copyOf(langTriples);
    }

    /** Return registered quad languages. */
    public static Collection<Lang> registeredLangQuads() {
        return Set.copyOf(langQuads);
    }
}

