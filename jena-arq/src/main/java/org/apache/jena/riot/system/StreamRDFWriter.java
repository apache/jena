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

package org.apache.jena.riot.system;

import java.io.OutputStream ;
import java.util.Collection ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.thrift.BinRDF ;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks ;
import org.apache.jena.riot.writer.WriterStreamRDFFlat ;
import org.apache.jena.riot.writer.WriterStreamRDFPlain ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Write RDF in a streaming fashion.
 *  {@linkplain RDFDataMgr} operations do not provide this guaranttee.
 *  See {@linkplain  RDFWriterRegistry} for general purpose writers.
 *  
 * @see RDFDataMgr 
 * @see RDFWriterRegistry 
 */
public class StreamRDFWriter {

    private static StreamRDFWriterFactory streamWriterFactoryBlocks = new StreamRDFWriterFactory() {
        @Override
        public StreamRDF create(OutputStream output, RDFFormat format) {
            return new WriterStreamRDFBlocks(output) ;
        }
    } ;
    
    private static StreamRDFWriterFactory streamWriterFactoryFlat = new StreamRDFWriterFactory() {
        @Override
        public StreamRDF create(OutputStream output, RDFFormat format) {
            return new WriterStreamRDFFlat(output) ;
        }
    } ;
    
    private static StreamRDFWriterFactory streamWriterFactoryTriplesQuads = new StreamRDFWriterFactory() {
        @Override
        public StreamRDF create(OutputStream output, RDFFormat format) {
            AWriter w = IO.wrapUTF8(output) ;
            return new WriterStreamRDFPlain(w, CharSpace.UTF8) ;     // N-Quads and N-Triples.
        }
    } ;
    
    private static StreamRDFWriterFactory streamWriterFactoryTriplesQuadsAscii = new StreamRDFWriterFactory() {
        @Override
        public StreamRDF create(OutputStream output, RDFFormat format) {
            AWriter w = IO.wrapUTF8(output) ;
            return new WriterStreamRDFPlain(w, CharSpace.ASCII) ;     // N-Quads and N-Triples.
        }
    } ;
    
    private static StreamRDFWriterFactory streamWriterFactoryThrift = new StreamRDFWriterFactory() {
        @Override
        public StreamRDF create(OutputStream output, RDFFormat format) {
            boolean withValues = RDFFormat.RDF_THRIFT_VALUES.equals(format) ; 
            return BinRDF.streamToOutputStream(output, withValues) ;
        }
    } ;
    
    private static WriterRegistry<StreamRDFWriterFactory> registry = new WriterRegistry<>() ;

    /** Register the default serialization for the language (replace any existing registration).
     * @param lang      Languages
     * @param format    The serialization forma to use when the language is used for writing.
     */
    public static void register(Lang lang, RDFFormat format) {
        registry.register(lang, format) ;
    }
    
    /** Register the serialization for datasets and it's associated factory
     * @param serialization         RDFFormat for the output format.
     * @param streamWriterFactory    Source of writer engines
     */
    public static void register(RDFFormat serialization, StreamRDFWriterFactory streamWriterFactory) {
        registry.register(serialization, streamWriterFactory) ;
    }
    
    /** Return the format registered as the default for the language */ 
    public static RDFFormat defaultSerialization(Lang lang) {
        return registry.defaultSerialization(lang) ;
    }
    
    static {
        register(Lang.TURTLE,       RDFFormat.TURTLE_BLOCKS) ;
        register(Lang.TRIG,         RDFFormat.TRIG_BLOCKS) ;
        register(Lang.NTRIPLES,     RDFFormat.NTRIPLES) ;
        register(Lang.NQUADS,       RDFFormat.NQUADS) ;
        register(Lang.RDFTHRIFT,       RDFFormat.RDF_THRIFT) ;
        
        register(RDFFormat.TURTLE_BLOCKS,   streamWriterFactoryBlocks) ;
        register(RDFFormat.TURTLE_FLAT,     streamWriterFactoryFlat) ;
        register(RDFFormat.TRIG_BLOCKS,     streamWriterFactoryBlocks) ;
        register(RDFFormat.TRIG_FLAT,       streamWriterFactoryFlat) ;
        
        register(RDFFormat.NTRIPLES,        streamWriterFactoryTriplesQuads) ;
        register(RDFFormat.NTRIPLES_UTF8,   streamWriterFactoryTriplesQuads) ;
        register(RDFFormat.NTRIPLES_ASCII,  streamWriterFactoryTriplesQuadsAscii) ;

        register(RDFFormat.NQUADS,          streamWriterFactoryTriplesQuads) ;
        register(RDFFormat.NQUADS_UTF8,     streamWriterFactoryTriplesQuads) ;
        register(RDFFormat.NQUADS_ASCII,    streamWriterFactoryTriplesQuadsAscii) ;

        register(RDFFormat.RDF_THRIFT,          streamWriterFactoryThrift) ;
        register(RDFFormat.RDF_THRIFT_VALUES,   streamWriterFactoryThrift) ;
    }

    /** Get a StreamRDF destination that will output in syntax <tt>Lang</tt>
     *  and is guaranteed to do so in a scaling, streaming fashion.    
     * @param output OutputStream
     * @param lang   The syntax 
     * @return       StreamRDF
     * @see StreamOps#graphToStream
     * @see StreamOps#datasetToStream
     */
    public static StreamRDF getWriterStream(OutputStream output, Lang lang) {
        RDFFormat fmt = registry.choose(lang) ;
        return getWriterStream(output, fmt) ;
    }

    /** Get a StreamRDF destination that will output in syntax <tt>RDFFormat</tt>
     *  and is guaranteed to do so in a scaling, streaming fashion.    
     * @param output OutputStream
     * @param format  The syntax (as an {@linkplain RDFFormat}) 
     * @return       StreamRDF
     * @see StreamOps#graphToStream
     * @see StreamOps#datasetToStream
     */
    public static StreamRDF getWriterStream(OutputStream output, RDFFormat format) {
        StreamRDFWriterFactory x = registry.get(format) ;
        if ( x == null )
            throw new RiotException("Failed to find a writer factory for "+format) ;
        StreamRDF stream = x.create(output, format) ;
        if ( ! RDFLanguages.isQuads(format.getLang()) )
            // Only pass through triples.
            stream = new StreamTriplesOnly(stream) ;
        return stream ;
    }
    
    public static boolean registered(Lang lang) {
        RDFFormat fmt = registry.defaultSerialization(lang) ;
        return registry.contains(fmt) ;
    }
    
    public static boolean registered(RDFFormat format) {
        return registry.contains(format) ;
    }
    
    public static Collection<RDFFormat> registered() {
       return Collections.unmodifiableSet(registry.formatRegistry.keySet()) ;
    }
    
    /** Write a Graph in streaming fashion
     * 
     * @param output OutputStream
     * @param graph  Graph to write
     * @param lang   Syntax
     */
    public static void write(OutputStream output, Graph graph, Lang lang) {
        RDFFormat fmt = registry.choose(lang) ;
        write(output, graph, fmt) ;
    }

    /** Write a Graph in streaming fashion
     * 
     * @param output OutputStream
     * @param graph  Graph to write
     * @param lang   Syntax
     */
    public static void write(OutputStream output, Graph graph, RDFFormat lang) {
        StreamRDF stream = getWriterStream(output, lang) ;
        StreamOps.graphToStream(graph, stream) ;
    }

    /** Write a DatasetGraph in streaming fashion
     *  
     * @param output        OutputStream
     * @param datasetGraph  DatasetGraph to write
     * @param lang          Syntax
     */
    public static void write(OutputStream output, DatasetGraph datasetGraph, Lang lang) {
        RDFFormat fmt = registry.choose(lang) ;
        write(output, datasetGraph, fmt) ;
    }

    /** Write a DatasetGraph in streaming fashion
     *  
     * @param output        OutputStream
     * @param datasetGraph  DatasetGraph to write
     * @param format          Syntax
     */
    public static void write(OutputStream output, DatasetGraph datasetGraph, RDFFormat format) {
        StreamRDF stream = getWriterStream(output, format) ;
        StreamOps.datasetToStream(datasetGraph, stream) ;
    }
    
    private static class StreamTriplesOnly extends StreamRDFWrapper {

        public StreamTriplesOnly(StreamRDF sink) {
            super(sink) ;
        }
        
        @Override public void quad(Quad quad) {
            if ( quad.isTriple() || quad.isDefaultGraph() || quad.isUnionGraph() ) {
                triple(quad.asTriple()) ;
            }
        }
        
        @Override public void triple(Triple triple) 
        { sink.triple(triple) ; }
    }
    
    /** Writer registry */
    public static class WriterRegistry<T> {
        // But RDFWriterregistry is two registries with shared Map<Lang, RDFFormat>
        // Coudl refator but the benefit is not so great.
        
        private Map<RDFFormat, T>     formatRegistry  = new HashMap<>() ;
        private Map<Lang, RDFFormat>  langToFormat    = new HashMap<>() ;

        /** Register the default serialization for the language (replace any existing registration).
         * @param lang      Languages
         * @param format    The serialization forma to use when the language is used for writing.
         */
        public void register(Lang lang, RDFFormat format)
        {
            //register(format) ;
            langToFormat.put(lang, format) ;
        }
        
        /** Register the serialization for datasets and it's associated factory
         * @param serialization         RDFFormat for the output format.
         * @param streamWriterFactory    Source of writer engines
         */
        public void register(RDFFormat serialization, T streamWriterFactory) {
            formatRegistry.put(serialization, streamWriterFactory) ;
        }
        
        /** Return the T for a given RDFFormat.
         * @param serialization     RDFFormat for the output format.
         * @return T                Registered thing or null. 
         */
        public T get(RDFFormat serialization) {
            return formatRegistry.get(serialization) ;
        }

        /** Return true if the format is registered 
         * 
         * @param serialization
         * @return boolean
         */
        public boolean contains(RDFFormat serialization) {
            return formatRegistry.containsKey(serialization) ;
        }

        /** Return the format registered as the default for the language */ 
        public RDFFormat defaultSerialization(Lang lang) {
            return langToFormat.get(lang) ;
        }
        
        /**
         * @param lang
         * @return The RDFFormat for the lang
         * @throws  RiotException if there is no registered format
         */
        public RDFFormat choose(Lang lang) {
            RDFFormat fmt = defaultSerialization(lang) ;
            if ( fmt == null )
                throw new RiotException("No serialization for language "+lang) ;
            return fmt ;
        }
    }
}

