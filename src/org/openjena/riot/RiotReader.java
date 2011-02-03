/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import static org.openjena.riot.Lang.NQUADS ;
import static org.openjena.riot.Lang.NTRIPLES ;
import static org.openjena.riot.Lang.RDFXML ;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.lang.LangNQuads ;
import org.openjena.riot.lang.LangNTriples ;
import org.openjena.riot.lang.LangRDFXML ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.LangTriG ;
import org.openjena.riot.lang.LangTurtle ;
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

        String printName = nameForFile(filename) ;  
        InputStream in = IO.openFile(filename) ; 
        // Logging:
        //--    loadLogger.info("Load: "+printName+" -- "+Utils.nowAsString()) ;
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
        String printName = nameForFile(filename) ;  
        InputStream in = IO.openFile(filename) ; 
        // Logging:
        //--    loadLogger.info("Load: "+printName+" -- "+Utils.nowAsString()) ;
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
            return LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.errorHandlerStd, sink) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(input) ;
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
        return createParserQuads(tokenizer, lang, baseIRI ,sink) ;
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
        LangTurtle parser = new LangTurtle(baseIRI, tokenizer, RiotLib.profile(Lang.TURTLE, baseIRI), sink) ;
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
        LangTriG parser = new LangTriG(baseIRI, tokenizer, RiotLib.profile(Lang.TRIG, baseIRI), sink) ;
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
    
    private static String chooseBaseIRI()
    {
        return IRIResolver.chooseBaseURI().toString() ;
    }
    
    private static String chooseBaseIRI(String baseIRI, String filename)
    {
        if ( baseIRI != null )
            return baseIRI ;
        if ( filename == null || filename.equals("-") )
            return "http://localhost/stdin/" ;
        return IRIResolver.get().resolveToString(filename) ;
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */