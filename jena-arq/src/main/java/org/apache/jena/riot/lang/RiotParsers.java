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

import static org.apache.jena.riot.RDFLanguages.CSV ;
import static org.apache.jena.riot.RDFLanguages.N3 ;
import static org.apache.jena.riot.RDFLanguages.NQUADS ;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES ;
import static org.apache.jena.riot.RDFLanguages.RDFJSON ;
import static org.apache.jena.riot.RDFLanguages.RDFXML ;
import static org.apache.jena.riot.RDFLanguages.TRIG ;
import static org.apache.jena.riot.RDFLanguages.TURTLE ;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.atlas.lib.CharSpace ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.jena.sparql.core.Quad ;

/** Use RDFDataMgr operations.
 * 
 * <b>This class is internal to RIOT.</b>
 */
public class RiotParsers {
    // package statics -- for the tests to create exactly what they test.
    private RiotParsers() {}
    
    /** Create a parser 
     * Use {@link RDFDataMgr#createReader(Lang)}
     */
    public static LangRIOT createParser(InputStream input, Lang lang, String baseIRI, StreamRDF dest) {
        if ( lang == RDFXML )
            return createParserRDFXML(input, baseIRI, dest) ;
        if ( lang == CSV )
            return new LangCSV (input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(),  dest);
            
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.makeUTF8(input)) :
                TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParser(tokenizer, lang, baseIRI, dest) ;
    }

    /** Create a parser 
     */
    public static LangRIOT createParser(Reader input, Lang lang, String baseIRI, StreamRDF dest) {
        if ( lang == RDFXML )
            return createParserRDFXML(input, baseIRI, dest) ;
        if ( lang == CSV)
            return new LangCSV (input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(),  dest);
        
        @SuppressWarnings("deprecation")
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.make(input)) :
                TokenizerFactory.makeTokenizer(input) ;
        return createParser(tokenizer, lang, baseIRI, dest) ;
    }

    /** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input, StreamRDF dest) {
        // LangNTriples supports iterator use.
        return createParserNTriples(input, dest) ;
    }

    /** Create an iterator for parsing N-Quads. */
    public static Iterator<Quad> createIteratorNQuads(InputStream input, StreamRDF dest) {
        // LangNQuads supports iterator use.
        return createParserNQuads(input, dest) ;
    }

    private static LangRDFXML createParserRDFXML(InputStream input, String baseIRI, StreamRDF dest) {
        baseIRI = baseURI_RDFXML(baseIRI) ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        return parser ;
    }

    private static LangRDFXML createParserRDFXML(Reader input, String baseIRI, StreamRDF dest) {
        baseIRI = baseURI_RDFXML(baseIRI) ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        return parser ;
    }

//    /*package*/ static LangTurtle createParserTurtle(InputStream input, String baseIRI, StreamRDF dest) {
//        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
//        return createParserTurtle(tokenizer, baseIRI, dest) ;
//    }

//    /*package*/ static LangRDFJSON createParserRdfJson(InputStream input, StreamRDF dest) {
//        TokenizerJSON tokenizer = new TokenizerJSON(PeekReader.makeUTF8(input)) ;
//        return createParserRdfJson(tokenizer, dest) ;
//    }
//
//    /*package*/ static LangTriG createParserTriG(InputStream input, String baseIRI, StreamRDF dest) {
//        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
//        return createParserTriG(tokenizer, baseIRI, dest) ;
//    }
//
    private static LangNTriples createParserNTriples(InputStream input, StreamRDF dest) {
        return createParserNTriples(input, CharSpace.UTF8, dest);
    }

    private static  LangNTriples createParserNTriples(InputStream input, CharSpace charSpace, StreamRDF dest) {
        Tokenizer tokenizer = charSpace == CharSpace.ASCII
            ? TokenizerFactory.makeTokenizerASCII(input) : TokenizerFactory.makeTokenizerUTF8(input);
        return createParserNTriples(tokenizer, dest) ;
    }

    private static LangNQuads createParserNQuads(InputStream input, StreamRDF dest) {
        return createParserNQuads(input, CharSpace.UTF8, dest) ;
    }

    private static LangNQuads createParserNQuads(InputStream input, CharSpace charSpace, StreamRDF dest) {
        Tokenizer tokenizer = charSpace == CharSpace.ASCII ? TokenizerFactory.makeTokenizerASCII(input) : TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserNQuads(tokenizer, dest) ;
    }

    private static LangRIOT createParser(Tokenizer tokenizer, Lang lang, String baseIRI, StreamRDF dest) {
        if ( RDFLanguages.sameLang(RDFXML, lang) )
            throw new RiotException("Not possible - can't parse RDF/XML from a RIOT token stream") ;
        if ( RDFLanguages.sameLang(TURTLE, lang) || RDFLanguages.sameLang(N3,  lang) ) 
                return createParserTurtle(tokenizer, baseIRI, dest) ;
        if ( RDFLanguages.sameLang(NTRIPLES, lang) )
                return createParserNTriples(tokenizer, dest) ;
        if ( RDFLanguages.sameLang(RDFJSON, lang) )
            // But it must be a JSON tokenizer ...
            return createParserRdfJson(tokenizer, dest) ;
        
        if ( RDFLanguages.sameLang(NQUADS, lang) )
            return createParserNQuads(tokenizer, dest) ;
        if ( RDFLanguages.sameLang(TRIG, lang) )
            return createParserTriG(tokenizer, baseIRI, dest) ;
        return null ;
    }

    /*package*/ static LangNTriples createParserNTriples(Tokenizer tokenizer, StreamRDF dest) {
        /* XXX */ ParserProfile profile = RiotLib.profile(RDFLanguages.NTRIPLES, null) ;
        LangNTriples parser = new LangNTriples(tokenizer, profile, dest) ;
        return parser ;
    }

    /*package*/ static LangNQuads createParserNQuads(Tokenizer tokenizer, StreamRDF dest) {
        /* XXX */ ParserProfile profile = RiotLib.profile(RDFLanguages.NQUADS, null) ;
        LangNQuads parser = new LangNQuads(tokenizer, profile, dest) ;
        return parser ;
    }

    /*package*/ static LangTurtle createParserTurtle(Tokenizer tokenizer, String baseIRI, StreamRDF dest) {
        /* XXX */ ParserProfile profile = RiotLib.profile(RDFLanguages.TURTLE, baseIRI) ;
        LangTurtle parser = new LangTurtle(tokenizer, profile, dest) ;
        return parser ;
    }

    /*package*/ static LangTriG createParserTriG(Tokenizer tokenizer, String baseIRI, StreamRDF dest) {
        /* XXX */ ParserProfile profile = RiotLib.profile(RDFLanguages.TRIG, baseIRI);
        LangTriG parser = new LangTriG(tokenizer, profile, dest) ;
        return parser ;
    }

    /*package*/ static LangRDFJSON createParserRdfJson(Tokenizer tokenizer, StreamRDF dest) {
        /* XXX */ ParserProfile profile = RiotLib.profile(RDFLanguages.RDFJSON, null);
        LangRDFJSON parser = new LangRDFJSON(tokenizer, profile, dest) ;
    	return parser;
    }

    /** Sort out the base URI for RDF/XML parsing. */
    private static String baseURI_RDFXML(String baseIRI) {
        // LangRIOT derived from LangEngine do this in ParserProfile 
        if ( baseIRI == null )
            return SysRIOT.chooseBaseIRI() ;
        else
            // This normalizes the URI.
            return SysRIOT.chooseBaseIRI(baseIRI) ;
    }
}

