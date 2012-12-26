/*
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

import static org.apache.jena.riot.RDFLanguages.* ;

import java.io.InputStream ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.iterator.IteratorResourceClosing ;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.riot.lang.* ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.openjena.riot.RiotQuadParsePuller ;
import org.openjena.riot.RiotTripleParsePuller ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Operations to access RIOT parsers and send the output to 
 *  a ParserOutput (triples or quads as appropriate).
 *  Operations to send to a sink (special case of a ParserOutput).
 *  @see RDFDataMgr for reading from a location, including web access and content negotation.   
 */
public class RiotReader
{
    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param sink  Where to send the triples from the parser.
     * @see         RiotReader#parse(String,RDFParserOutput)
     */  
    public static void parseTriples(String filename, Sink<Triple> sink)
    { parseTriples(filename, null, null, sink) ; }
    
    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param baseIRI   Base IRI, or null for based on input filename
     * @param sink      Where to send the triples from the parser.
     * @see             RiotReader#parse
     */  
    public static void parseTriples(String filename, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        RDFParserOutput dest = RDFParserOutputLib.sinkTriples(sink) ;
        parse(filename, lang, baseIRI, dest) ;
    }

    /** Parse an InputStream, sending triples to a sink.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param sink      Where to send the triples from the parser.
     * @see             RiotReader#parse
     */  
    public static void parseTriples(InputStream in, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        RDFParserOutput dest = RDFParserOutputLib.sinkTriples(sink) ;
        parse(in, lang, baseIRI, dest) ;
    }
    
    // -------- Quads
    
    /** Parse a file, sending quads to a sink.
     * @param filename
     * @param sink  Where to send the quads from the parser.
     * @see         RiotReader#parse
     */
    public static void parseQuads(String filename, Sink<Quad> sink)
    { parseQuads(filename, null, null, sink) ; }
    
    /** Parse a file, sending quads to a sink.
     * @param filename 
     * @param lang      Language, or null for "guess from filename" (e.g. extension)
     * @param baseIRI   Base IRI, or null for base on input filename
     * @param sink      Where to send the quads from the parser.
     * @see             RiotReader#parse
     */
    public static void parseQuads(String filename, Lang lang, String baseIRI, Sink<Quad> sink)
    {
        RDFParserOutput dest = RDFParserOutputLib.sinkQuads(sink) ;
        parse(filename, lang, baseIRI, dest) ;
    }

    /** Parse an InputStream, sending quads to a sink.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param sink      Where to send the quads from the parser.
     * @see             RiotReader#parse
     */
    public static void parseQuads(InputStream in, Lang lang, String baseIRI, Sink<Quad> sink)
    {
        RDFParserOutput dest = RDFParserOutputLib.sinkQuads(sink) ;
        parse(in, lang, baseIRI, dest) ;
    }

    /** Parse a file, sending output to a RDFParserOutput sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param dest  Where to send the triples from the parser.
     */
    public static void parse(String filename, RDFParserOutput dest)
    { parse(filename, null, null, dest) ; }
    
    /** Parse a file, sending output to a RDFParserOutput sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param baseIRI   Base IRI, or null for based on input filename
     * @param dest      Where to send the triples from the parser.
     */  
    public static void parse(String filename, Lang lang, String baseIRI, RDFParserOutput dest)
    {
        if ( lang == null )
            lang = filenameToLang(filename, NTriples) ;
        
        InputStream in = IO.openFile(filename) ; 
        String base = chooseBaseIRI(baseIRI, filename) ;
        parse(in, lang, base, dest) ;
        IO.close(in) ;
    }

    /** Parse an InputStream, using RDFParserOutput as the destination for the parser output.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param dest      Where to send the triples from the parser.
     */  
    public static void parse(InputStream in, Lang lang, String baseIRI, RDFParserOutput dest)
    {
        LangRIOT parser = RiotReader.createParser(in, lang, baseIRI, dest) ;
        parser.parse() ;
    }
    
    // -------- Parsers
    
    /** Create a parser */  
    public static LangRIOT createParser(InputStream input, Lang lang, String baseIRI, RDFParserOutput dest)
    {
        if ( lang == RDFXML )
        {
            if ( baseIRI != null )
                baseIRI = IRIResolver.resolveString(baseIRI) ;
            return LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        }
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.makeUTF8(input)) :
                TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParser(tokenizer, lang, baseIRI, dest) ;
    }
    /** Create a parser */  
    public static LangRIOT createParser(Tokenizer tokenizer, Lang lang, String baseIRI, RDFParserOutput dest)
    {
        if ( RDFLanguages.sameLang(RDFXML, lang) )
            throw new RiotException("Not possible - can't parse RDF/XML from a RIOT token stream") ;
        if ( RDFLanguages.sameLang(Turtle, lang) || RDFLanguages.sameLang(N3,  lang) ) 
                return createParserTurtle(tokenizer, baseIRI, dest) ;
        if ( RDFLanguages.sameLang(NTriples, lang) )
                return createParserNTriples(tokenizer, dest) ;
        if ( RDFLanguages.sameLang(RDFJSON, lang) )
            // But it must be a JSON tokenizer ...
            return createParserRdfJson(tokenizer, dest) ;
        
        if ( RDFLanguages.sameLang(NQuads, lang) )
            return createParserNQuads(tokenizer, dest) ;
        if ( RDFLanguages.sameLang(TriG, lang) )
            return createParserTriG(tokenizer, baseIRI, dest) ;
        
        return null ;
    }


    // TODO create a Tokenizer version of this method
    public static Iterator<Triple> createIteratorTriples(InputStream input, Lang lang, String baseIRI)
    {
        // Special case N-Triples, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NTriples, lang) )
        {
            return new IteratorResourceClosing<Triple>(createParserNTriples(input, null), input);
        }
        else
        {
            // Otherwise, we have to spin up a thread to deal with it
            RiotTripleParsePuller parsePuller = new RiotTripleParsePuller(input, lang, baseIRI);
            parsePuller.parse();
            return parsePuller;
        }
    }
    
    // TODO create a Tokenizer version of this method
    public static Iterator<Quad> createIteratorQuads(InputStream input, Lang lang, String baseIRI)
    {
        // Special case N-Quads, because the RIOT reader has a pull interface
        if (  RDFLanguages.sameLang(RDFLanguages.NTriples, lang) )
        {
            return new IteratorResourceClosing<Quad>(createParserNQuads(input, null), input);
        }
        else
        {
            // Otherwise, we have to spin up a thread to deal with it
            RiotQuadParsePuller parsePuller = new RiotQuadParsePuller(input, lang, baseIRI);
            parsePuller.parse();
            return parsePuller;
        }
    }
    
    /** Create a parser for Turtle, with default behaviour */
    public static LangTurtle createParserTurtle(InputStream input, String baseIRI, RDFParserOutput dest)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTurtle(tokenizer, baseIRI, dest) ;
    }
    
    /** Create a parser for Turtle, with default behaviour */
    public static LangTurtle createParserTurtle(Tokenizer tokenizer, String baseIRI, RDFParserOutput dest)
    {
        LangTurtle parser = new LangTurtle(tokenizer, RiotLib.profile(RDFLanguages.Turtle, baseIRI), dest) ;
        return parser ;
    }

    /** Create a parser for RDF/XML */
    public static LangRDFXML createParserRDFXML(InputStream input, String baseIRI, RDFParserOutput dest)
    {
        if ( baseIRI == null )
            baseIRI = chooseBaseIRI() ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        return parser ;
    }

    /** Create parsers for RDF/JSON */
    public static LangRDFJSON createParserRdfJson(Tokenizer tokenizer, RDFParserOutput dest)
    {
    	LangRDFJSON parser = new LangRDFJSON(tokenizer, RiotLib.profile(RDFLanguages.RDFJSON, null), dest) ;
    	return parser;
    }

    public static LangRDFJSON createParserRdfJson(InputStream input, RDFParserOutput dest)
    {
    	TokenizerJSON tokenizer = new TokenizerJSON(PeekReader.makeUTF8(input)) ;
    	return createParserRdfJson(tokenizer, dest) ;
    }
    
    /** Create a parser for TriG, with default behaviour */
    public static LangTriG createParserTriG(InputStream input, String baseIRI, RDFParserOutput dest)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTriG(tokenizer, baseIRI, dest) ;
    }
    
    /** Create a parser for TriG, with default behaviour */
    public static LangTriG createParserTriG(Tokenizer tokenizer, String baseIRI, RDFParserOutput dest)
    {
        if ( baseIRI == null )
            baseIRI = chooseBaseIRI() ;
        LangTriG parser = new LangTriG(tokenizer, RiotLib.profile(RDFLanguages.TriG, baseIRI), dest) ;
        return parser ;
    }

    /** Create a parser for N-Triples, with default behaviour */
    public static LangNTriples createParserNTriples(InputStream input, RDFParserOutput dest)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(input) ;
        return createParserNTriples(tokenizer, dest) ;
    }
    
    /** Create a parser for N-Triples, with default behaviour */
    public static LangNTriples createParserNTriples(Tokenizer tokenizer, RDFParserOutput dest)
    {
        LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(RDFLanguages.NTriples, null), dest) ;
        return parser ;
    }
    
    /** Create a parser for NQuads, with default behaviour */
    public static LangNQuads createParserNQuads(InputStream input, RDFParserOutput dest)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(input) ;
        return createParserNQuads(tokenizer, dest) ;
    }
    
    /** Create a parser for NQuads, with default behaviour */
    public static LangNQuads createParserNQuads(Tokenizer tokenizer, RDFParserOutput dest)
    {
        LangNQuads parser = new LangNQuads(tokenizer, RiotLib.profile(RDFLanguages.NQuads, null), dest) ;
        return parser ;
    }
    
    public static String chooseBaseIRI()
    {
        return IRIResolver.chooseBaseURI().toString() ;
    }
    
    public static String chooseBaseIRI(String baseIRI, String filename)
    {
        if ( baseIRI != null )
            return baseIRI ;
        if ( filename == null || filename.equals("-") )
            return "http://localhost/stdin/" ;
        String x = IRILib.filenameToIRI(filename) ;
        return x ;
    }

    private static String nameForFile(String filename)
    {
        if ( filename == null || filename.equals("-") )
            return "stdin" ;
        return filename ;
    }
        
}
