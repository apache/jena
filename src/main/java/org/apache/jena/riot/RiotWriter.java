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
import org.apache.jena.riot.writer.* ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Direct call to create writers for specific formats.
 *  The normal way to write is {@linkplain RDFWriterMgr} 
 */
public class RiotWriter
{
    // Compatibility stuff
    /** @deprecated Use RDFWriterMgr.write(OutputStream, DatasetGraph, Lang.NQUADS) */
    @Deprecated
    public static void writeNQuads(OutputStream out, DatasetGraph dsg)
    {
        RDFWriterMgr.write(out, dsg, Lang.NQUADS) ;
    }
    
    public static void writeNQuads(OutputStream out, Iterator<Quad> it)
    {
        NQuadsWriter.write(out, it) ;
    }

    /** @deprecated Use RDFWriterMgr.write(OutputStream, Graph, Lang.NTRIPLES) */
    @Deprecated
    public static void writeTriples(OutputStream out, Graph graph)
    {
        RDFWriterMgr.write(out, graph, Lang.NTRIPLES) ;
    }
    
    public static void writeTriples(OutputStream out, Iterator<Triple> it)
    {
        NTriplesWriter.write(out, it) ;
    }

    /** @deprecated Use RDFWriterMgr.write(OutputStream, Graph, Lang.RDFJSON) */
    @Deprecated
    public static void writeRDFJSON(OutputStream out, Graph graph)
    {
        RDFWriterMgr.write(out, graph, Lang.RDFJSON) ;
    }

    // Remove after release ... no one should be using this code unless
    // they used the off-trunk development code. 
    
//    /** Write as Turtle
//     * @param out   OutputStream
//     * @param model Model to write 
//     */
//    public static void writeTurtle(OutputStream out, Model model)
//    { writeTurtle(out, model.getGraph()) ; }
//    
//    /** Write as Turtle
//     * @param out   OutputStream
//     * @param graph Graph to write 
//     */
//    public static void writeTurtle(OutputStream out, Graph graph)
//    { createTurtle().write(out, graph) ; }
//
//    /** Write as Turtle, using a streaming writer
//     * @param out   OutputStream
//     * @param model Model to write 
//     */
//    public static void writeTurtleStreaming(OutputStream out,  Model model)
//    { writeTurtleStreaming(out, model.getGraph()) ; }
//    
//    /** Write as Turtle, using a streaming writer
//     * @param out   OutputStream
//     * @param graph Graph to write 
//     */
//    public static void writeTurtleStreaming(OutputStream out, Graph graph)
//    { createTurtleStreaming().write(out, graph) ; }
//
//    /** Write a model as NTriples
//     * @param out       OutputStream
//     * @param model     Model to write 
//     */
//    public static void writeNTriples(OutputStream out, Model model)
//    { writeNTriples(out, model.getGraph()) ; }
//    
//    /** Write a graph as NTriples
//     * @param out       OutputStream
//     * @param graph     Graph to write 
//     */
//    public static void writeNTriples(OutputStream out, Graph graph)
//    { createNTriples().write(out, graph) ; }
//
//    /** Write a model as RDF/XML
//     * @param out       OutputStream
//     * @param model     Model to write 
//     */
//    public static void writeRDFXML(OutputStream out, Model model)
//    { writeRDFXML(out, model.getGraph()) ; }
//    
//    /** Write a graph as RDF/XML
//     * @param out       OutputStream
//     * @param graph     Graph to write 
//     */
//    public static void writeRDFXML(OutputStream out, Graph graph)
//    { createRDFXMLAbbrev().write(out, graph) ; }
//    
//    /** Write a model as RDF/XML
//     * @param out       OutputStream
//     * @param model     Model to write 
//     */
//    public static void writeRDFXMLStreaming(OutputStream out, Model model)
//    { writeRDFXMLStreaming(out, model.getGraph()) ; }
//    
//    /** Write a graph as RDF/XML
//     * @param out       OutputStream
//     * @param graph     Graph to write 
//     */
//    public static void writeRDFXMLStreaming(OutputStream out, Graph graph)
//    { createRDFXMLPlain().write(out, graph) ; }
//    
//    /** Write a model as RDF/JSON (this is not JSON-LD)
//     * @param out       OutputStream
//     * @param model     Model to write 
//     */
//    public static void writeRDFJSON(OutputStream out, Model model)
//    { writeRDFJSON(out, model.getGraph()) ; }
//    
//    /** Write a graph as RDF/JSON (this is not JSON-LD)
//     * @param out       OutputStream
//     * @param graph     Graph to write 
//     */
//    public static void writeRDFJSON(OutputStream out, Graph graph)
//    { createRDFJSON().write(out, graph) ; }
//    
//    /** Write a dataset as TriG
//     * @param out       OutputStream
//     * @param dataset   Dataset to write 
//     */
//    public static void writeTrig(OutputStream out, Dataset dataset)
//    { writeTrig(out, dataset.asDatasetGraph()) ; }
//
//    /** Write a dataset as TriG
//     * @param out       OutputStream
//     * @param dataset   Dataset to write 
//     */
//    public static void writeTrig(OutputStream out, DatasetGraph dataset)
//    { createTrig().write(out, dataset) ; }
//    
//    /** Write a dataset as TriG, using a streaming writer
//     * @param out       OutputStream
//     * @param dataset   Dataset to write 
//     */
//    public static void writeTrigStreaming(OutputStream out, Dataset dataset)
//    { writeTrigStreaming(out, dataset.asDatasetGraph()) ; }
//
//    /** Write a dataset as TriG, using a streaming writer
//     * @param out       OutputStream
//     * @param dataset   Dataset to write 
//     */
//    public static void writeTrigStreaming(OutputStream out, DatasetGraph dataset)
//    { createTrigStreaming().write(out, dataset) ; }
//    
//    /** Write a dataset as NQuads
//     * @param out       OutputStream
//     * @param dataset   Dataset to write 
//     */
//    public static void writeNQuads(OutputStream out, Dataset dataset)
//    { writeNQuads(out, dataset.asDatasetGraph()) ; }
//    
//    /** Write a dataset as NQuads
//     * @param out       OutputStream
//     * @param dataset   Dataset to write 
//     */
//    public static void writeNQuads(OutputStream out, DatasetGraph dataset)
//    { createNQuads().write(out, dataset) ; }
//    
    // ---- Create writers

    /** Create a Turtle writer */
    public static WriterGraphRIOT createTurtle()            { return new TurtleWriter() ; }

    /** Create a streaming Turtle writer */
    public static WriterGraphRIOT createTurtleStreaming()   { return new TurtleWriterBlocks() ; }

    /** Create a streaming Turtle outputing one triple per line using Turtle abbreviations */
    public static WriterGraphRIOT createTurtleFlat()        { return new TurtleWriterFlat() ; }

    /** Create an N-Triples writer */
    public static WriterGraphRIOT createNTriples()          { return new NTriplesWriter() ; }

    /** Create an RDF/XML writer which pretty-prints */
    public static WriterGraphRIOT createRDFXMLAbbrev()      { return new RDFXMLAbbrevWriter() ; }

    /** Create an RDF/XML writer which does not pretty-print */
    public static WriterGraphRIOT createRDFXMLPlain()       { return new RDFXMLPlainWriter() ; }

    /** Create an RDF/JSON writer */
    public static WriterGraphRIOT createRDFJSON()           { return new RDFJSONWriter() ; }

    /** Create a TriG writer */
    public static WriterDatasetRIOT createTrig()            { return new TriGWriter() ; }

    /** Create a TriG writer that streams */
    public static WriterDatasetRIOT createTrigStreaming()   { return new TriGWriterBlocks() ; }

    /** Create a TriG writer that writes one quad per line in Trig, using abbreviated forms */ 
    public static WriterDatasetRIOT createTrigFlat()        { return new TriGWriterFlat() ; }

    /** Create an NQuads writer */
    public static WriterDatasetRIOT createNQuads()          { return new NQuadsWriter() ; }

    public static WriterDatasetRIOT createRDFNULL()         { return NullWriter.factory.create(RDFFormat.RDFNULL) ; }           
}

