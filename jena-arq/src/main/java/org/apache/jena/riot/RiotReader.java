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

import static org.apache.jena.riot.RDFLanguages.CSV;
import static org.apache.jena.riot.RDFLanguages.N3 ;
import static org.apache.jena.riot.RDFLanguages.NQUADS ;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES ;
import static org.apache.jena.riot.RDFLanguages.RDFJSON ;
import static org.apache.jena.riot.RDFLanguages.RDFXML ;
import static org.apache.jena.riot.RDFLanguages.TRIG ;
import static org.apache.jena.riot.RDFLanguages.TURTLE ;
import static org.apache.jena.riot.RDFLanguages.RDFNULL ;
import static org.apache.jena.riot.RDFLanguages.filenameToLang ;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.iterator.IteratorResourceClosing ;
import org.apache.jena.atlas.json.io.parser.TokenizerJSON ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.riot.lang.* ;
import org.apache.jena.riot.out.CharSpace;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Operations to access RIOT parsers and send the output to 
 *  a StreamRDF (triples or quads as appropriate).
 *  This class is probably not what you want to use.
 *  It is public to give maximum compatibility.
 *
 *  @see RDFDataMgr for reading from a location, including web access and content negotation.
 */
public class RiotReader
{
    /** Parse a file, sending output to a StreamRDF sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param dest  Where to send the triples from the parser.
     */
    public static void parse(String filename, StreamRDF dest)
    { parse(filename, null, null, dest) ; }

    /** Parse a file, sending output to a StreamRDF sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param dest      Where to send the triples from the parser.
     */  
    public static void parse(String filename, Lang lang, StreamRDF dest)
    {
        parse(filename, lang, null, dest) ;
    }

    /** Parse a file, sending output to a StreamRDF sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param baseIRI   Base IRI, or null for based on input filename
     * @param dest      Where to send the triples from the parser.
     */  
    public static void parse(String filename, Lang lang, String baseIRI, StreamRDF dest)
    {
        if ( lang == null )
            lang = filenameToLang(filename, NTRIPLES) ;
        
        InputStream in = IO.openFile(filename) ; 
        String base = SysRIOT.chooseBaseIRI(baseIRI, filename) ;
        parse(in, lang, base, dest) ;
        IO.close(in) ;
    }

    /** Parse an InputStream, using RDFParserOutput as the destination for the parser output.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param dest      Where to send the triples from the parser.
     */  
    public static void parse(InputStream in, Lang lang, StreamRDF dest)
    {
        parse(in, lang, null, dest) ;
    }

    /** Parse an InputStream, using RDFParserOutput as the destination for the parser output.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param dest      Where to send the triples from the parser.
     */  
    public static void parse(InputStream in, Lang lang, String baseIRI, StreamRDF dest)
    {
        LangRIOT parser = RiotReader.createParser(in, lang, baseIRI, dest) ;
        parser.parse() ;
    }

    // -------- Parsers
    
    /** Create a parser 
     * @deprecated Use {@linkplain RDFDataMgr#createReader(Lang)}
     */
    @Deprecated
    public static LangRIOT createParser(InputStream input, Lang lang, String baseIRI, StreamRDF dest)
    {
        if ( lang == RDFXML )
        {
            if ( baseIRI != null )
                baseIRI = IRIResolver.resolveString(baseIRI) ;
            return LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        } else if ( lang == CSV){
        	return new LangCSV (input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(),  dest);
        }
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.makeUTF8(input)) :
                TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParser(tokenizer, lang, baseIRI, dest) ;
    }

    /** Create a parser 
     * @deprecated Use {@linkplain RDFDataMgr#createReader(Lang)}
     */
    @Deprecated
    public static LangRIOT createParser(Reader input, Lang lang, String baseIRI, StreamRDF dest)
    {
        if ( lang == RDFXML )
        {
            if ( baseIRI != null )
                baseIRI = IRIResolver.resolveString(baseIRI) ;
            return LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        } else if ( lang == CSV){
        	return new LangCSV (input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(),  dest);
        }
        Tokenizer tokenizer = ( lang == RDFJSON ) ?
            new TokenizerJSON(PeekReader.make(input)) :
                TokenizerFactory.makeTokenizer(input) ;
        return createParser(tokenizer, lang, baseIRI, dest) ;
    }

    /** Create a parser 
     * @deprecated Use {@linkplain RDFDataMgr#createReader(Lang)}
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
        
        if ( RDFLanguages.sameLang(RDFNULL, lang) )
            return new LangNull() ;
        
        return null ;
    }

    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param sink  Where to send the triples from the parser.
     * @see         RiotReader#parse(String,StreamRDF)
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
        StreamRDF dest = StreamRDFLib.sinkTriples(sink) ;
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
        StreamRDF dest = StreamRDFLib.sinkTriples(sink) ;
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
        StreamRDF dest = StreamRDFLib.sinkQuads(sink) ;
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
        StreamRDF dest = StreamRDFLib.sinkQuads(sink) ;
        parse(in, lang, baseIRI, dest) ;
    }

    /**
     * Create an iterator over the parsed triples
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the triples
     */
    public static Iterator<Triple> createIteratorTriples(final InputStream input, final Lang lang, final String baseIRI)
    {
        // Special case N-Triples, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NTRIPLES, lang) )
        {
            return new IteratorResourceClosing<>(createParserNTriples(input, null), input);
        }
        else
        {
            // Otherwise, we have to spin up a thread to deal with it
            final PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
            final PipedTriplesStream out = new PipedTriplesStream(it);
            
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    parse(input, lang, baseIRI, out);
                }
            });
            t.start();
            
            return it;
        }
    }
   
    /**
     * Creates an iterator over the parsed quads
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the quads
     */
    public static Iterator<Quad> createIteratorQuads(final InputStream input, final Lang lang, final String baseIRI)
    {
        // Special case N-Quads, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NQUADS, lang) )
        {
            return new IteratorResourceClosing<>(createParserNQuads(input, null), input);
        }
        else
        {
            // Otherwise, we have to spin up a thread to deal with it
            final PipedRDFIterator<Quad> it = new PipedRDFIterator<>();
            final PipedQuadsStream out = new PipedQuadsStream(it);
            
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    parse(input, lang, baseIRI, out);
                }
            });
            t.start();
            
            return it;
        }
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
    
    /** Create a parser for Turtle */
    public static LangTurtle createParserTurtle(Tokenizer tokenizer, String baseIRI, StreamRDF dest)
    {
        LangTurtle parser = new LangTurtle(tokenizer, RiotLib.profile(RDFLanguages.TURTLE, baseIRI), dest) ;
        return parser ;
    }

    /** Create a parser for RDF/XML
     * @deprecated use an RDFDataMgr operation with argument Lang.RDFXML
     */
    @Deprecated
    public static LangRDFXML createParserRDFXML(InputStream input, String baseIRI, StreamRDF dest)
    {
        if ( baseIRI == null )
            baseIRI = SysRIOT.chooseBaseIRI() ;
        LangRDFXML parser = LangRDFXML.create(input, baseIRI, baseIRI, ErrorHandlerFactory.getDefaultErrorHandler(), dest) ;
        return parser ;
    }

    /** Create parsers for RDF/JSON */
    public static LangRDFJSON createParserRdfJson(Tokenizer tokenizer, StreamRDF dest)
    {
    	LangRDFJSON parser = new LangRDFJSON(tokenizer, RiotLib.profile(RDFLanguages.RDFJSON, null), dest) ;
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
    
    /** Create a parser for TriG */
    public static LangTriG createParserTriG(Tokenizer tokenizer, String baseIRI, StreamRDF dest)
    {
        LangTriG parser = new LangTriG(tokenizer, RiotLib.profile(RDFLanguages.TRIG, baseIRI), dest) ;
        return parser ;
    }

    /** Create a parser for N-Triples
     * @deprecated use an RDFDataMgr operation with argument Lang.NTRIPLES
     */
    @Deprecated
    public static LangNTriples createParserNTriples(InputStream input, StreamRDF dest)
    {
        return createParserNTriples(input, CharSpace.UTF8, dest) ;
    }
    
    /** Create a parser for N-Triples
     * @deprecated use an RDFDataMgr operation with argument Lang.NTRIPLES
     */
    @Deprecated
    public static LangNTriples createParserNTriples(InputStream input, CharSpace charSpace, StreamRDF dest)
    {
        Tokenizer tokenizer = charSpace == CharSpace.ASCII ? TokenizerFactory.makeTokenizerASCII(input) : TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserNTriples(tokenizer, dest) ;
    }
    
    /** Create a parser for N-Triples */
    public static LangNTriples createParserNTriples(Tokenizer tokenizer, StreamRDF dest)
    {
        LangNTriples parser = new LangNTriples(tokenizer, RiotLib.profile(RDFLanguages.NTRIPLES, null), dest) ;
        return parser ;
    }
    
    /** Create a parser for NQuads
     * @deprecated use an RDFDataMgr operation with argument Lang.NQUADS)
     */
    @Deprecated
    public static LangNQuads createParserNQuads(InputStream input, StreamRDF dest)
    {
        return createParserNQuads(input, CharSpace.UTF8, dest) ;
    }
    
    /** Create a parser for NQuads
     * @deprecated use an RDFDataMgr operation with argument Lang.NQUADS)
     */
    @Deprecated
    public static LangNQuads createParserNQuads(InputStream input, CharSpace charSpace, StreamRDF dest)
    {
        Tokenizer tokenizer = charSpace == CharSpace.ASCII ? TokenizerFactory.makeTokenizerASCII(input) : TokenizerFactory.makeTokenizerUTF8(input) ;
        return createParserNQuads(tokenizer, dest) ;
    }
    
    /** Create a parser for NQuads */
    public static LangNQuads createParserNQuads(Tokenizer tokenizer, StreamRDF dest)
    {
        LangNQuads parser = new LangNQuads(tokenizer, RiotLib.profile(RDFLanguages.NQUADS, null), dest) ;
        return parser ;
    }
}
