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

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.system.* ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

/** @deprecated Use RDFDataMgr operations.
 * This class will become internal to RIOT.
 */
@Deprecated
public class RiotParsers {
    private RiotParsers() {}
    
    /** Create a parser 
     * @deprecated Use {@link RDFDataMgr#createReader(Lang)}
     */
    @Deprecated
    public static LangRIOT createParser(InputStream input, Lang lang, String baseIRI, StreamRDF dest)
    {
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
     * @deprecated Use {@link RDFDataMgr#createReader(Lang)}
     */
    @Deprecated
    public static LangRIOT createParser(Reader input, Lang lang, String baseIRI, StreamRDF dest)
    {
        if ( lang == RDFXML )
            return createParserRDFXML(input, baseIRI, dest) ;
        if ( lang == CSV)
            return new LangCSV (input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(),  dest);
        
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.make(input)) :
                TokenizerFactory.makeTokenizer(input) ;
        return createParser(tokenizer, lang, baseIRI, dest) ;
    }

    /** Create a parser for Turtle
     * @deprecated use an RDFDataMgr operation with argument Lang.Turtle
     */
    @Deprecated
    public static LangTurtle createParserTurtle(InputStream input, String baseIRI, StreamRDF dest)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTurtle(tokenizer, baseIRI, dest) ;
    }

    /** Create a parser for Turtle
     * @deprecated use an RDFDataMgr operation with argument Lang.Turtle
     */
    @Deprecated
    public static LangTurtle createParserTurtle(Tokenizer tokenizer, String baseIRI, StreamRDF dest)
    {
        ParserProfile profile = RiotLib.profile(RDFLanguages.TURTLE, baseIRI) ;
        LangTurtle parser = new LangTurtle(tokenizer, profile, dest) ;
        return parser ;
    }

    /** Create a parser for RDF/XML
     * @deprecated use an RDFDataMgr operation with argument Lang.RDFXML
     */
    @Deprecated
    public static LangRDFXML createParserRDFXML(InputStream input, String baseIRI, StreamRDF dest)
    {
        baseIRI = baseURI_RDFXML(baseIRI) ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        return parser ;
    }

    /** Create a parser for RDF/XML
     * @deprecated use an RDFDataMgr operation with argument Lang.RDFXML
     */
    @Deprecated
    public static LangRDFXML createParserRDFXML(Reader input, String baseIRI, StreamRDF dest)
    {
        baseIRI = baseURI_RDFXML(baseIRI) ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        return parser ;
    }
    
    /** Sort out the base URi fo RDF/XML parsing. */
    private static String baseURI_RDFXML(String baseIRI) {
        // LangRIOT derived from LangEngine do this in ParserProfile 
        if ( baseIRI == null )
            return SysRIOT.chooseBaseIRI() ;
        else
            // This normalizes the URI.
            return SysRIOT.chooseBaseIRI(baseIRI) ;
    }

    /** Create parsers for RDF/JSON
     * @deprecated use an RDFDataMgr operation with argument Lang.RDFJSON
     */
    @Deprecated
    public static LangRDFJSON createParserRdfJson(Tokenizer tokenizer, StreamRDF dest)
    {
        ParserProfile profile =  RiotLib.profile(RDFLanguages.RDFJSON, null) ;
    	LangRDFJSON parser = new LangRDFJSON(tokenizer, profile, dest) ;
    	return parser;
    }

    /**
     * @deprecated use RDFDataMgr and Lang.RDFJSON
     */
    @Deprecated
    public static LangRDFJSON createParserRdfJson(InputStream input, StreamRDF dest)
    {
        TokenizerJSON tokenizer = new TokenizerJSON(PeekReader.makeUTF8(input)) ;
        return createParserRdfJson(tokenizer, dest) ;
    }

    /** Create a parser for TriG
     * @deprecated use an RDFDataMgr operation with argument Lang.TRIG
     */
    @Deprecated
    public static LangTriG createParserTriG(InputStream input, String baseIRI, StreamRDF dest)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTriG(tokenizer, baseIRI, dest) ;
    }

    /** Create a parser for TriG.
     * @deprecated use an RDFDataMgr operation with argument Lang.TRIG
     */
    @Deprecated
    public static LangTriG createParserTriG(Tokenizer tokenizer, String baseIRI, StreamRDF dest)
    {
        ParserProfile profile = RiotLib.profile(RDFLanguages.TRIG, baseIRI) ;
        LangTriG parser = new LangTriG(tokenizer, profile, dest) ;
        return parser ;
    }

    /** Create a parser for N-Triples
     *  @deprecated Use an RDFDataMgr operation with argument Lang.NTRIPLES
     */
    @Deprecated
    public static LangNTriples createParserNTriples(InputStream input, StreamRDF dest)
    {
        return createParserNTriples(input, CharSpace.UTF8, dest) ;
    }

    /** Create a parser for N-Triples
     *  @deprecated Use an RDFDataMgr operation with argument Lang.NTRIPLES
     */
    @Deprecated
    public static LangNTriples createParserNTriples(InputStream input, CharSpace charSpace, StreamRDF dest)
    {
        Tokenizer tokenizer = charSpace == CharSpace.ASCII ? TokenizerFactory.makeTokenizerASCII(input) : TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserNTriples(tokenizer, dest) ;
    }

    /** Create a parser for N-Triples
     *  @deprecated Use an RDFDataMgr operation with argument Lang.NTRIPLES
     */
    @Deprecated
    public static LangNTriples createParserNTriples(Tokenizer tokenizer, StreamRDF dest)
    {
        ParserProfile profile = RiotLib.profile(RDFLanguages.NTRIPLES, null) ;
        LangNTriples parser = new LangNTriples(tokenizer, profile, dest) ;
        return parser ;
    }

    /** Create a parser for NQuads
     *  @deprecated Use an RDFDataMgr operation with argument Lang.NQUADS.
     */
    @Deprecated
    public static LangNQuads createParserNQuads(InputStream input, StreamRDF dest)
    {
        return createParserNQuads(input, CharSpace.UTF8, dest) ;
    }

    /** Create a parser for NQuads
     *  @deprecated Use an RDFDataMgr operation with argument Lang.NQUADS.
     */
    @Deprecated
    public static LangNQuads createParserNQuads(InputStream input, CharSpace charSpace, StreamRDF dest)
    {
        Tokenizer tokenizer = charSpace == CharSpace.ASCII ? TokenizerFactory.makeTokenizerASCII(input) : TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserNQuads(tokenizer, dest) ;
    }

    /** Create a parser for NQuads
     *  @deprecated Use an RDFDataMgr operation with argument Lang.NQUADS.
     */
    @Deprecated
    public static LangNQuads createParserNQuads(Tokenizer tokenizer, StreamRDF dest)
    {
        ParserProfile profile = RiotLib.profile(RDFLanguages.NQUADS, null) ;
        LangNQuads parser = new LangNQuads(tokenizer, profile, dest) ;
        return parser ;
    }

    /** Create a parser 
     * @deprecated Use {@link RDFDataMgr#createReader(Lang)}
     */
    @Deprecated  
    public static LangRIOT createParser(Tokenizer tokenizer, Lang lang, String baseIRI, StreamRDF dest)
    {
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

}

