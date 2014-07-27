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

import org.apache.jena.riot.writer.* ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Direct call to create writers for specific formats.
 *  The normal way to write is {@linkplain RDFDataMgr} 
 */
@Deprecated
public class RiotWriter
{
    public static void writeNQuads(OutputStream out, Iterator<Quad> it) {
        NQuadsWriter.write(out, it) ;
    }

    public static void writeTriples(OutputStream out, Iterator<Triple> it) {
        NTriplesWriter.write(out, it) ;
    }

    // ---- Create writers

    /** Create a Turtle writer
     @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} 
     */
    @Deprecated
    public static WriterGraphRIOT createTurtle()            { return RDFDataMgr.createGraphWriter(RDFFormat.TURTLE) ; }

    /** Create a streaming Turtle writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.TURTLE_BLOCKS</tt> 
     */
    @Deprecated
    public static WriterGraphRIOT createTurtleStreaming()   { return RDFDataMgr.createGraphWriter(RDFFormat.TURTLE_BLOCKS) ; }

    /** Create a streaming Turtle writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.TURTLE_FLAT</tt> 
     */
    @Deprecated
    public static WriterGraphRIOT createTurtleFlat()        { return RDFDataMgr.createGraphWriter(RDFFormat.TURTLE_FLAT) ; }

    /** Create am N-Triples writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} 
     */
    @Deprecated
    public static WriterGraphRIOT createNTriples()          { return RDFDataMgr.createGraphWriter(RDFFormat.NTRIPLES) ; } 

    /** Create an N-Triples writer, restricted to ASCII characters in the output. 
     * Other chars escaped in \ u sequences.
     * @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.NTRIPLES_ASCII</tt> 
     */
    @Deprecated
    public static WriterGraphRIOT createNTriplesASCII()     { return RDFDataMgr.createGraphWriter(RDFFormat.NTRIPLES_ASCII) ; }

    /** Create an RDF/XML writer which pretty-prints
    *   @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.RDFXML_ABBREV</tt>
    */
    @Deprecated
    public static WriterGraphRIOT createRDFXMLAbbrev()      { return RDFDataMgr.createGraphWriter(RDFFormat.RDFXML_ABBREV) ; }

    /** Create an RDF/XML writer which pretty-prints
    *   @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.RDFXML_PLAIN</tt>
    */
    @Deprecated
    public static WriterGraphRIOT createRDFXMLPlain()       { return RDFDataMgr.createGraphWriter(RDFFormat.RDFXML_PLAIN) ; }

    /** Create an RDF/JSON writer
     *  @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(Lang)} and <tt>Lang.RDFJSON</tt>
     */
    @Deprecated
    public static WriterGraphRIOT createRDFJSON()           { return RDFDataMgr.createGraphWriter(RDFFormat.RDFJSON) ; }
    /** Create a TriG writer
     *  @deprecated Use {@linkplain RDFDataMgr#createDatasetWriter(Lang)} and <tt>Lang.TRIG</tt>
     */
    @Deprecated
    public static WriterDatasetRIOT createTrig()            { return RDFDataMgr.createDatasetWriter(RDFFormat.TRIG) ; }

    /** Create a streaming TriG writer
     *  @deprecated Use {@linkplain RDFDataMgr#createDatasetWriter(RDFFormat)} and <tt>RDFFormat.TRIG_BLOCKS</tt> 
     */
    @Deprecated
    public static WriterDatasetRIOT createTrigStreaming()   { return RDFDataMgr.createDatasetWriter(RDFFormat.TRIG_BLOCKS) ; }

    /** Create a streaming TriG writer with one quad per line.
     *  @deprecated Use {@linkplain RDFDataMgr#createDatasetWriter(RDFFormat)} and <tt>RDFFormat.TRIG_FLAT</tt> 
     */
    @Deprecated
    public static WriterDatasetRIOT createTrigFlat()        { return RDFDataMgr.createDatasetWriter(RDFFormat.TRIG_FLAT) ; }

    /** Create an N-Quads writer
     * @deprecated Use {@linkplain RDFDataMgr#createDatasetWriter(Lang)} and <tt>Lang.NQUADS</tt>     
     */
    @Deprecated
    public static WriterDatasetRIOT createNQuads()          { return RDFDataMgr.createDatasetWriter(RDFFormat.NQUADS) ; }
    
    /** Create an N-Quads writer, restricted to ASCII characters in the output. 
     * Other chars escaped in \ u sequences.
     * @deprecated Use {@linkplain RDFDataMgr#createGraphWriter(RDFFormat)} and <tt>RDFFormat.NQUADS_ASCII</tt> 
     */
    @Deprecated
    public static WriterDatasetRIOT createNQuadsASCII()     { return RDFDataMgr.createDatasetWriter(RDFFormat.NQUADS_ASCII) ; }

    /** Create a sink writer
     * @deprecated Use {@linkplain RDFDataMgr#createDatasetWriter(RDFFormat)} and <tt>RDFFormat.RDFNULL</tt>
     */
    @Deprecated
    public static WriterDatasetRIOT createRDFNULL()         { return RDFDataMgr.createDatasetWriter(RDFFormat.RDFNULL) ; }           
}

