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

package org.apache.jena.riot.lang;

import static org.apache.jena.riot.RDFLanguages.N3;
import static org.apache.jena.riot.RDFLanguages.NQUADS;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES;
import static org.apache.jena.riot.RDFLanguages.RDFJSON;
import static org.apache.jena.riot.RDFLanguages.TRIG;
import static org.apache.jena.riot.RDFLanguages.TURTLE;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad;

/** Use RDFDataMgr operations.
 *
 * <b>This class is internal to RIOT.</b>
 */
public class RiotParsers {
    private RiotParsers() {}

    /** InputStream input */
    public static LangRIOT createParser(InputStream input, Lang lang, StreamRDF dest, ParserProfile profile) {
        if ( RDFLanguages.sameLang(RDFJSON, lang) ) {
            Tokenizer tokenizer = new TokenizerJSON(PeekReader.makeUTF8(input));
            return createParserRdfJson(tokenizer, dest, profile);
        }
        Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(profile.getErrorHandler()).build();
        if ( RDFLanguages.sameLang(TURTLE, lang) || RDFLanguages.sameLang(N3,  lang) )
            return createParserTurtle(tokenizer, dest, profile);
        if ( RDFLanguages.sameLang(NTRIPLES, lang) )
            return createParserNTriples(tokenizer, dest, profile);
        if ( RDFLanguages.sameLang(NQUADS, lang) )
            return createParserNQuads(tokenizer, dest, profile);
        if ( RDFLanguages.sameLang(TRIG, lang) )
            return createParserTriG(tokenizer, dest, profile);
        return null;
    }

    /** Reader input */
    public static LangRIOT createParser(Reader input, Lang lang, StreamRDF dest, ParserProfile profile) {
        if ( RDFLanguages.sameLang(RDFJSON, lang) ) {
            Tokenizer tokenizer = new TokenizerJSON(PeekReader.make(input));
            return createParserRdfJson(tokenizer, dest, profile);
        }

        Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(profile.getErrorHandler()).build();
        if ( RDFLanguages.sameLang(TURTLE, lang) || RDFLanguages.sameLang(N3,  lang) )
            return createParserTurtle(tokenizer, dest, profile);
        if ( RDFLanguages.sameLang(NTRIPLES, lang) )
            return createParserNTriples(tokenizer, dest, profile);
        if ( RDFLanguages.sameLang(NQUADS, lang) )
            return createParserNQuads(tokenizer, dest, profile);
        if ( RDFLanguages.sameLang(TRIG, lang) )
            return createParserTriG(tokenizer, dest, profile);
        return null;
    }

    // Package access for tests.

    /*package*/ static LangNTriples createParserNTriples(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangNTriples parser = new LangNTriples(tokenizer, profile, dest);
        return parser;
    }

    /*package*/ static LangNQuads createParserNQuads(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangNQuads parser = new LangNQuads(tokenizer, profile, dest);
        return parser;
    }

    /*package*/ static LangTurtle createParserTurtle(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangTurtle parser = new LangTurtle(tokenizer, profile, dest);
        return parser;
    }

    /*package*/ static LangTriG createParserTriG(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangTriG parser = new LangTriG(tokenizer, profile, dest);
        return parser;
    }

    /*package*/ static LangRDFJSON createParserRdfJson(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangRDFJSON parser = new LangRDFJSON(tokenizer, profile, dest);
        return parser;
    }

    /**
     * Create an iterator for parsing N-Triples.
     * @deprecated StreamRDF argument ignored. Use {@link #createIteratorNTriples(InputStream)}
     */
    @Deprecated
    public static Iterator<Triple> createIteratorNTriples(InputStream input, StreamRDF dest) {
        return createIteratorNTriples(input, RiotLib.dftProfile());
    }

    /** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input) {
        return createIteratorNTriples(input, RiotLib.dftProfile());
    }

    /** Create an iterator for parsing N-Triples.
     * @deprecated StreamRDF argument ignored. Use {@link #createIteratorNTriples(InputStream)}
     */
    @Deprecated
    public static Iterator<Triple> createIteratorNTriples(InputStream input, StreamRDF dest, ParserProfile profile) {
        return createIteratorNTriples(input, profile);
    }

    /** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input, ParserProfile profile) {
        // LangNTriples supports iterator use.
        Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(profile.getErrorHandler()).build();
        return createParserNTriples(tokenizer, null, profile);
    }

    /**
     * Create an iterator for parsing N-Quads.
     * @deprecated StreamRDF argument ignored. Use {@link #createIteratorNQuads(InputStream)}
     */
    @Deprecated
    public static Iterator<Quad> createIteratorNQuads(InputStream input, StreamRDF dest) {
        return createIteratorNQuads(input, RiotLib.dftProfile());
    }

    /** Create an iterator for parsing N-Quads. */
    public static Iterator<Quad> createIteratorNQuads(InputStream input) {
        return createIteratorNQuads(input, RiotLib.dftProfile());
    }

    /**
     * Create an iterator for parsing N-Quads.
     * @deprecated StreamRDF argument ignored. Use {@link #createIteratorNQuads(InputStream)}
     */
    @Deprecated
    public static Iterator<Quad> createIteratorNQuads(InputStream input, StreamRDF dest, ParserProfile profile) {
        return createIteratorNQuads(input, profile);
    }

    /**
     * Create an iterator for parsing N-Quads.
     */
    public static Iterator<Quad> createIteratorNQuads(InputStream input, ParserProfile profile) {
        // LangNQuads supports iterator use.
        Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(profile.getErrorHandler()).build();
        return createParserNQuads(tokenizer, null,  profile);
    }

}

