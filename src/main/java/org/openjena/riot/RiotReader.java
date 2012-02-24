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

package org.openjena.riot;

import static org.openjena.riot.Lang.NQUADS ;
import static org.openjena.riot.Lang.NTRIPLES ;
import static org.openjena.riot.Lang.RDFJSON ;
import static org.openjena.riot.Lang.RDFXML ;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.PeekReader ;
import org.openjena.atlas.json.io.parser.TokenizerJSON ;
import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.lang.* ;
import org.openjena.riot.system.IRIResolver ;
import org.openjena.riot.system.RiotLib ;
import org.openjena.riot.system.SinkExtendTriplesToQuads ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Operations to access RIOT parsers and send the output to 
 * a Sink (triples or quads as appropriate)
 */
public class RiotReader
{
    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param sink  Where to send the triples from the parser.
     */  
    public static void parseTriples(String filename, Sink<Triple> sink)
    { parseTriples(filename, null, null, sink) ; }
    
    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param baseIRI   Base IRI, or null for based on input filename
     * @param sink      Where to send the triples from the parser.
     */  
    public static void parseTriples(String filename, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        checkTriplesLanguage(filename, lang) ;

        InputStream in = IO.openFile(filename) ; 
        String base = chooseBaseIRI(baseIRI, filename) ;

        if ( lang == null )
            lang = Lang.guess(filename, NTRIPLES) ;     // ** N-Triples
        
        if ( lang == RDFXML )
        {
            // Fudge to make the bulk loader process RDF/XML files.
            LangRDFXML.create(in, base, filename, ErrorHandlerFactory.errorHandlerStd, sink).parse() ;
            IO.close(in) ;
            return ;
        }
        
        parseTriples(in, lang, base, sink) ;
        IO.close(in) ;
    }

    /** Parse an InputStream, sending triples to a sink.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param sink      Where to send the triples from the parser.
     */

    public static void parseTriples(InputStream in, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        LangRIOT parser = RiotReader.createParserTriples(in, lang, baseIRI, sink) ;
        parser.parse() ;
    }
    
    // -------- Quads
    
    /** Parse a file, sending quads to a sink.
     * @param filename
     * @param sink  Where to send the quads from the parser.
     */
    public static void parseQuads(String filename, Sink<Quad> sink)
    { parseQuads(filename, null, null, sink) ; }
    
    /** Parse a file, sending quads to a sink.
     * @param filename 
     * @param lang      Language, or null for "guess from filename" (e.g. extension)
     * @param baseIRI   Base IRI, or null for base on input filename
     * @param sink      Where to send the quads from the parser.
     */
    public static void parseQuads(String filename, Lang lang, String baseIRI, Sink<Quad> sink)
    {
        InputStream in = IO.openFile(filename) ; 
        String base = chooseBaseIRI(baseIRI, filename) ;
        if ( lang == null )
            lang = Lang.guess(filename, NQUADS) ;     // ** N-Quads
        parseQuads(in, lang, base, sink) ;
        IO.close(in) ;
    }

    /** Parse an InputStream, sending quads to a sink.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param sink      Where to send the quads from the parser.
     */
    public static void parseQuads(InputStream in, Lang lang, String baseIRI, Sink<Quad> sink)
    {
        LangRIOT parser = RiotReader.createParserQuads(in, lang, baseIRI, sink) ;
        parser.parse() ;
    }

    // -------- Parsers
    
    /** Create a parser for a triples language */  
    public static LangRIOT createParserTriples(InputStream input, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        if ( lang == RDFXML )
        {
            baseIRI = IRIResolver.resolveString(baseIRI) ;
            return LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.errorHandlerStd, sink) ;
        }
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.makeUTF8(input)) :
                TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTriples(tokenizer, lang, baseIRI ,sink) ;
    }
    
    /** Create a parser for a triples language */  
    public static LangRIOT createParserTriples(Tokenizer tokenizer, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        switch (lang)
        {
            case N3 :
            case TURTLE :
                return createParserTurtle(tokenizer, baseIRI, sink) ;
            case NTRIPLES :
                return createParserNTriples(tokenizer, sink) ;
            case RDFJSON :
                // But it must be a JSON tokenizer ...
            	return createParserRdfJson(tokenizer, sink) ;
            case RDFXML :
                throw new RiotException("Not possible - can't parse RDF/XML from a RIOT token stream") ;
            case NQUADS :
            case TRIG :
                throw new RiotException("Not a triples language: "+lang) ;
        }
        return null ;
    }
    
    /** Create a parser for a quads (or triples) language */  
    public static LangRIOT createParserQuads(InputStream input, Lang lang, String baseIRI, Sink<Quad> sink)
    {
        if ( lang.isTriples() )
        {
            SinkExtendTriplesToQuads converter = new SinkExtendTriplesToQuads(sink) ;
            return createParserTriples(input, lang, baseIRI, converter) ;
        }
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserQuads(tokenizer, lang, baseIRI, sink) ;
    }
    
    /** Create a parser for a quads language */  
    public static LangRIOT createParserQuads(Tokenizer tokenizer, Lang lang, String baseIRI, Sink<Quad> sink)
    {
        switch (lang)
        {
            case NTRIPLES : // Or move N-Triples just go through N-Quads. 
            case N3 :
            case TURTLE :
            case RDFXML :
            case RDFJSON :
                // Add a triples to quads wrapper.
                SinkExtendTriplesToQuads converter = new SinkExtendTriplesToQuads(sink) ;
                return createParserTriples(tokenizer, lang, baseIRI, converter) ;
            case NQUADS :
                return createParserNQuads(tokenizer, sink) ;
            case TRIG :
                return createParserTriG(tokenizer, baseIRI, sink) ;
        }
        return null ;
    }
    
    /** Create a parser for Turtle, with default behaviour */
    public static LangTurtle createParserTurtle(InputStream input, String baseIRI, Sink<Triple> sink)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTurtle(tokenizer, baseIRI, sink) ;
    }
    
    /** Create a parser for Turtle, with default behaviour */
    public static LangTurtle createParserTurtle(Tokenizer tokenizer, String baseIRI, Sink<Triple> sink)
    {
        LangTurtle parser = new LangTurtle(tokenizer, RiotLib.profile(Lang.TURTLE, baseIRI), sink) ;
        return parser ;
    }

    /** Create a parser for RDF/XML */
    public static LangRDFXML createParserRDFXML(InputStream input, String baseIRI, Sink<Triple> sink)
    {
        if ( baseIRI == null )
            baseIRI = chooseBaseIRI() ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.errorHandlerStd, sink) ;
        return parser ;
    }

    /** Create parsers for RDF/JSON */
    public static LangRDFJSON createParserRdfJson(Tokenizer tokenizer, Sink<Triple> sink)
    {
    	LangRDFJSON parser = new LangRDFJSON(tokenizer, RiotLib.profile(Lang.RDFJSON, null), sink) ;
    	return parser;
    }

    public static LangRDFJSON createParserRdfJson(InputStream input, Sink<Triple> sink)
    {
    	TokenizerJSON tokenizer = new TokenizerJSON(PeekReader.makeUTF8(input)) ;
    	return createParserRdfJson(tokenizer, sink) ;
    }
    
    /** Create a parser for TriG, with default behaviour */
    public static LangTriG createParserTriG(InputStream input, String baseIRI, Sink<Quad> sink)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserTriG(tokenizer, baseIRI, sink) ;
    }
    
    /** Create a parser for TriG, with default behaviour */
    public static LangTriG createParserTriG(Tokenizer tokenizer, String baseIRI, Sink<Quad> sink)
    {
        if ( baseIRI == null )
            baseIRI = chooseBaseIRI() ;
        LangTriG parser = new LangTriG(tokenizer, RiotLib.profile(Lang.TRIG, baseIRI), sink) ;
        return parser ;
    }

    /** Create a parser for N-Triples, with default behaviour */
    public static LangNTriples createParserNTriples(InputStream input, Sink<Triple> sink)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(input) ;
        return createParserNTriples(tokenizer, sink) ;
    }
    
    /** Create a parser for N-Triples, with default behaviour */
    public static LangNTriples createParserNTriples(Tokenizer tokenizer, Sink<Triple> sink)
    {
        LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(Lang.NTRIPLES, null), sink) ;
        return parser ;
    }
    
    /** Create a parser for NQuads, with default behaviour */
    public static LangNQuads createParserNQuads(InputStream input, Sink<Quad> sink)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerASCII(input) ;
        return createParserNQuads(tokenizer, sink) ;
    }
    
    /** Create a parser for NQuads, with default behaviour */
    public static LangNQuads createParserNQuads(Tokenizer tokenizer, Sink<Quad> sink)
    {
        LangNQuads parser = new LangNQuads(tokenizer, RiotLib.profile(Lang.NQUADS, null), sink) ;
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

    private static void checkTriplesLanguage(String filename, Lang lang)
    {
        if ( lang != null )
        {
            if ( ! lang.isTriples() )
                throw new RiotException("Can only parse triples languages to a triples sink: "+lang.getName()) ;
            return ;
        }
    
        lang = Lang.guess(filename) ;
        if ( lang != null && ! lang.isTriples() )
            throw new RiotException("Can only parse triples languages to a triples sink: "+lang.getName()) ; 
    }
    
        
}
