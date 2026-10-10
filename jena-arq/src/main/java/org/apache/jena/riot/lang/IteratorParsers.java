/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.riot.lang;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

/**
 * Parsers that support the iterator pattern for receiving the results of parsing.
 */
public class IteratorParsers {

    /**
     * Creates an iterator over parsing of triples.
     * This function creates a thread unless the Lang is N-Triples.
     * @deprecated Use {@link #createIteratorTriples(InputStream, Lang, String, Context)}
     */
    @Deprecated(forRemoval = true)
    public static Iterator<Triple> createIteratorTriples(InputStream input, Lang lang, String baseURI) {
		return createIteratorTriples(input, lang, baseURI, RIOT.getContext());
	}

	/**
     * Creates an iterator over parsing of quads.
     * This function creates a thread unless the Lang is N-Triples.
     */
    public static Iterator<Triple> createIteratorTriples(InputStream input, Lang lang, String baseURI, Context context) {
        Objects.requireNonNull(lang);
        if ( Lang.NTRIPLES.equals(lang) )
            return createIteratorNTriples(input, context);
        if ( ! RDFLanguages.isTriples(lang) )
            throw new RiotException("Not a triples syntax: "+lang.getName());
        // For all other languages, we need to do the parsing asynchronously
        return AsyncParser.asyncParseTriples(input, lang, baseURI);
    }

    /**
     * Create an iterator over parsing of triples.
     * This function creates a thread unless the Lang is N-Quads.
     * @deprecated Use {@link #createIteratorQuads(InputStream, Lang, String, Context)}
     */
    @Deprecated(forRemoval = true)
    public static Iterator<Quad> createIteratorQuads(InputStream input, Lang lang, String baseURI) {
		return createIteratorQuads( input, lang, baseURI, RIOT.getContext());
	}

	/**
     * Create an iterator over parsing of triples.
     * This function creates a thread unless the Lang is N-Quads.
     */
    public static Iterator<Quad> createIteratorQuads(InputStream input, Lang lang, String baseURI, Context context) {
        Objects.requireNonNull(lang);
        if ( Lang.NQUADS.equals(lang) )
            return createIteratorNQuads(input, context);
        // For all other languages, we need to do the parsing asynchronously
        return AsyncParser.asyncParseQuads(input, lang, baseURI);
    }

    /**
     * Create an iterator for parsing N-Triples.
     * @deprecated Use {@link #createIteratorNTriples(InputStream, Context)}
     */
    @Deprecated(forRemoval = true)
    public static Iterator<Triple> createIteratorNTriples(InputStream input) {
		return createIteratorNTriples(input, RIOT.getContext());
	}

	/** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input, Context context) {
        return createIteratorNTriples(input, RiotLib.dftProfile(), context);
    }

    /**
     * Create an iterator for parsing N-Triples.
     * @deprecated Use {@link #createIteratorNTriples(InputStream, ParserProfile, Context)}
     */
    @Deprecated(forRemoval = true)
    public static Iterator<Triple> createIteratorNTriples(InputStream input, ParserProfile profile) {
		return createIteratorNTriples(input, profile, RIOT.getContext());
	}

	/** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input, ParserProfile profile, Context context) {
        // LangNTriples supports iterator use.
        Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(profile.getErrorHandler()).build();
        return createParserNTriples(tokenizer, null, profile, context);
    }

    /*package*/ static LangNTriples createParserNTriples(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile, Context context) {
        LangNTriples parser = new LangNTriples(tokenizer, profile, dest, context);
        return parser;
    }

    /**
     * Create an iterator for parsing N-Quads.
     * @deprecated Use {@link #createIteratorNQuads(InputStream, Context)}
     */
    @Deprecated(forRemoval = true)
    public static Iterator<Quad> createIteratorNQuads(InputStream input) {
		return createIteratorNQuads(input, RIOT.getContext());
	}

	/** Create an iterator for parsing N-Quads. */
    public static Iterator<Quad> createIteratorNQuads(InputStream input, Context context) {
        return createIteratorNQuads(input, RiotLib.dftProfile(), context);
    }

    /**
     * Create an iterator for parsing N-Quads.
     * @deprecated Use {@link #createIteratorNQuads(InputStream, ParserProfile, Context)}
     */
    @Deprecated(forRemoval = true)
    public static Iterator<Quad> createIteratorNQuads(InputStream input, ParserProfile profile) {
		return createIteratorNQuads(input, profile, RIOT.getContext());
	}

	/**
     * Create an iterator for parsing N-Quads.
     */
    public static Iterator<Quad> createIteratorNQuads(InputStream input, ParserProfile profile, Context context) {
        // LangNQuads supports iterator use.
        Tokenizer tokenizer = TokenizerText.create().source(input).errorHandler(profile.getErrorHandler()).build();
        return createParserNQuads(tokenizer, null,  profile, context);
    }

    /*package*/ static LangNQuads createParserNQuads(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile, Context context) {
        LangNQuads parser = new LangNQuads(tokenizer, profile, dest, context);
        return parser;
    }
}
