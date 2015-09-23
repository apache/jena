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

import static org.apache.jena.riot.RDFLanguages.NTRIPLES ;
import static org.apache.jena.riot.RDFLanguages.filenameToLang ;

import java.io.InputStream ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sparql.core.Quad ;

/** Operations to access RIOT parsers and send the output to 
 *  a StreamRDF (triples or quads as appropriate).
 *  <p>
 *  <b>This class is probably not what you want to use.</b>
 *  <p>
 *
 *  @see RDFDataMgr for reading from a location, including web access and content negotation.
 *  @deprecated
 */
@Deprecated
public class RiotReader
{
    /** Parse a file, sending output to a StreamRDF sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param dest  Where to send the triples from the parser.
     * @deprecated Use {@link RDFDataMgr#parse(StreamRDF, String)}
     */
    @Deprecated
    public static void parse(String filename, StreamRDF dest)
    { parse(filename, null, null, dest) ; }

    /** Parse a file, sending output to a StreamRDF sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param dest      Where to send the triples from the parser.
     * @deprecated Use {@link RDFDataMgr#parse(StreamRDF, String, Lang)}
     */
    @Deprecated
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
     * @deprecated Use {@link RDFDataMgr#parse(StreamRDF, String, String, Lang)}
     */
    @Deprecated
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
     * @deprecated     use {@link RDFDataMgr#parse(StreamRDF, InputStream, Lang)}
     */
    @Deprecated
    public static void parse(InputStream in, Lang lang, StreamRDF dest)
    {
        parse(in, lang, null, dest) ;
    }

    /** Parse an InputStream, using RDFParserOutput as the destination for the parser output.
     * @param in        Source for bytes to parse.
     * @param lang      Language.
     * @param baseIRI   Base IRI. 
     * @param dest      Where to send the triples from the parser.
     * @deprecated     use {@link RDFDataMgr#parse(StreamRDF, InputStream, String, Lang)}
     */
    @Deprecated
    public static void parse(InputStream in, Lang lang, String baseIRI, StreamRDF dest)
    {
        RDFDataMgr.parse(dest, in, baseIRI, lang);
    }

    // -------- Parsers
    
    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param sink  Where to send the triples from the parser.
     * @deprecated Use an {@link StreamRDF} and {@link RDFDataMgr#parse(StreamRDF, String)}
     */
    @Deprecated
    public static void parseTriples(String filename, Sink<Triple> sink)
    { parseTriples(filename, null, null, sink) ; }
    
    /** Parse a file, sending triples to a sink.
     * Must be in a triples syntax.
     * @param filename 
     * @param lang      Language, or null for "guess from URL" (e.g. file extension)
     * @param baseIRI   Base IRI, or null for based on input filename
     * @param sink      Where to send the triples from the parser.
     * @deprecated Use an {@link StreamRDF} and {@link RDFDataMgr#parse(StreamRDF, String, String, Lang)}
     */
    @Deprecated
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
     * @deprecated Use an {@link StreamRDF} and {@link RDFDataMgr#parse(StreamRDF, InputStream, Lang)}
     */
    @Deprecated
    public static void parseTriples(InputStream in, Lang lang, String baseIRI, Sink<Triple> sink)
    {
        StreamRDF dest = StreamRDFLib.sinkTriples(sink) ;
        parse(in, lang, baseIRI, dest) ;
    }
    
    // -------- Quads
    
    /** Parse a file, sending quads to a sink.
     * @param filename
     * @param sink  Where to send the quads from the parser.
     * @deprecated Use an {@link StreamRDF} and {@link RDFDataMgr#parse(StreamRDF, String)}  
     */
    @Deprecated
    public static void parseQuads(String filename, Sink<Quad> sink)
    { parseQuads(filename, null, null, sink) ; }
    
    /** Parse a file, sending quads to a sink.
     * @param filename 
     * @param lang      Language, or null for "guess from filename" (e.g. extension)
     * @param baseIRI   Base IRI, or null for base on input filename
     * @param sink      Where to send the quads from the parser.
     * @deprecated Use an {@link StreamRDF} and {@link RDFDataMgr#parse(StreamRDF, String, String, Lang)}
     */
    @Deprecated
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
     * @deprecated Use an {@link StreamRDF} and {@link RDFDataMgr#parse(StreamRDF, InputStream, String, Lang)}
     */
    @Deprecated
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
     * @deprecated Use {@link RDFDataMgr#createIteratorTriples(InputStream,Lang,String)} instead
     */
    @Deprecated
    public static Iterator<Triple> createIteratorTriples(final InputStream input, final Lang lang, final String baseIRI)
    {
        return RDFDataMgr.createIteratorTriples(input, lang, baseIRI);
    }
   
    /**
     * Creates an iterator over the parsed quads
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the quads
     * @deprecated Use {@link RDFDataMgr#createIteratorQuads(InputStream,Lang,String)} instead
     */
    @Deprecated
    public static Iterator<Quad> createIteratorQuads(final InputStream input, final Lang lang, final String baseIRI) 
    {
        return RDFDataMgr.createIteratorQuads(input, lang, baseIRI);
    }
}
