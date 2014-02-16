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

import java.io.OutputStream ;
import java.util.Iterator ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.writer.* ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Direct call to create writers for specific formats.
 *  The normal way to write is {@linkplain RDFDataMgr} 
 */
public class RiotWriter
{
    // Compatibility stuff
    /** @deprecated Use RDFDataMgr.write(OutputStream, DatasetGraph, Lang.NQUADS) */
    @Deprecated
    public static void writeNQuads(OutputStream out, DatasetGraph dsg)
    {
        RDFDataMgr.write(out, dsg, Lang.NQUADS) ;
    }
    
    public static void writeNQuads(OutputStream out, Iterator<Quad> it)
    {
        NQuadsWriter.write(out, it) ;
    }

    /** @deprecated Use {@linkplain RDFDataMgr#write(OutputStream, Graph, Lang)} with <tt>Lang.NTRIPLES</tt>. */
    @Deprecated
    public static void writeTriples(OutputStream out, Graph graph)
    {
        RDFDataMgr.write(out, graph, Lang.NTRIPLES) ;
    }
    
    public static void writeTriples(OutputStream out, Iterator<Triple> it)
    {
        NTriplesWriter.write(out, it) ;
    }

    /** @deprecated Use {@linkplain RDFDataMgr#write(OutputStream, Graph, Lang)} with <tt>Lang.RDFJSON</tt>. */
    @Deprecated
    public static void writeRDFJSON(OutputStream out, Graph graph)
    {
        RDFDataMgr.write(out, graph, Lang.RDFJSON) ;
    }

    // ---- Create writers

    /** Create a Turtle writer
     @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} 
     */
    @Deprecated
    public static WriterGraphRIOT createTurtle()            { return new TurtleWriter() ; }

    /** Create a streaming Turtle writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.TURTLE_BLOCKS</tt> 
     */
    @Deprecated
    public static WriterGraphRIOT createTurtleStreaming()   { return new TurtleWriterBlocks() ; }

    /** Create a streaming Turtle writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.TURTLE_FLAT</tt> 
     */
    @Deprecated
    public static WriterGraphRIOT createTurtleFlat()        { return new TurtleWriterFlat() ; }

    /** Create am N-Triples writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} 
     */
    @Deprecated
    public static WriterGraphRIOT createNTriples()          { return new NTriplesWriter() ; }

    /** Create an N-Triples writer, restricted to ASCII characters in the output. 
     * Other chars escaped in \ u sequences.
     * @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.NTRIPLES_ASCII</tt> 
     */
    @Deprecated
    public static WriterGraphRIOT createNTriplesASCII()     { return new NTriplesWriter(CharSpace.ASCII) ; }

    /** Create an RDF/XML writer which pretty-prints
    *   @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.RDFXML_ABBREV</tt>
    */
    @Deprecated
    public static WriterGraphRIOT createRDFXMLAbbrev()      { return new RDFXMLAbbrevWriter() ; }

    /** Create an RDF/XML writer which pretty-prints
    *   @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.RDFXML_PLAIN</tt>
    */
    @Deprecated
    public static WriterGraphRIOT createRDFXMLPlain()       { return new RDFXMLPlainWriter() ; }

    /** Create an RDF/JSON writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} and <tt>Lang.RDFJSON</tt>
     */
    @Deprecated
    public static WriterGraphRIOT createRDFJSON()           { return new RDFJSONWriter() ; }

    /** Create a TriG writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} and <tt>Lang.TRIG</tt>
     */
    @Deprecated
    public static WriterDatasetRIOT createTrig()            { return new TriGWriter() ; }

    /** Create a streaming TriG writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.TRIG_BLOCKS</tt> 
     */
    @Deprecated
    public static WriterDatasetRIOT createTrigStreaming()   { return new TriGWriterBlocks() ; }

    /** Create a streaming TriG writer with one quad per line.
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.TRIG_FLAT</tt> 
     */
    @Deprecated
    public static WriterDatasetRIOT createTrigFlat()        { return new TriGWriterFlat() ; }

    /** Create an N-Quads writer
     * @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} and <tt>Lang.NQUADS</tt>     
     */
    @Deprecated
    public static WriterDatasetRIOT createNQuads()          { return new NQuadsWriter() ; }
    
    /** Create an N-Quads writer, restricted to ASCII characters in the output. 
     * Other chars escaped in \ u sequences.
     * @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.NQUADS_ASCII</tt> 
     */
    @Deprecated
    public static WriterDatasetRIOT createNQuadsASCII()     { return new NQuadsWriter(CharSpace.ASCII) ; }

    /** Create a sink writer */
    @Deprecated
    public static WriterDatasetRIOT createRDFNULL()         { return NullWriter.factory.create(RDFFormat.RDFNULL) ; }           
}

