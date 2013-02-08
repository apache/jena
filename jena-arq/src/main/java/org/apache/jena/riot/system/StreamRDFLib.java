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

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.riot.lang.RDFParserOutputCounting ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Various Common StreamRDF setups */
public class StreamRDFLib
{
    /** Send everything to nowhere ... efficiently */
    public static StreamRDF sinkNull()                       { return new StreamRDFBase() ; }

    public static StreamRDF graph(Graph graph)               { return new ParserOutputGraph(graph) ; }
    
    public static StreamRDF dataset(DatasetGraph dataset)    { return new ParserOutputDataset(dataset) ; }
    
    /** Set triples to a StreamRDF - does not all .start/.finish */ 
    public static void triplesToStream(StreamRDF dest, Iterator<Triple> iter)
    {
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            dest.triple(t) ;
        }
    }

    /** Set quads to a StreamRDF - does not all .start/.finish */ 
    public static void quadsToStream(StreamRDF dest, Iterator<Quad> iter)
    {
        for ( ; iter.hasNext() ; )
        {
            Quad q = iter.next() ;
            dest.quad(q) ;
        }
    }

    
    /** 
     * Output to a sink; prefix and base handled only within the parser.
     * Unfortunately, Java needs different names for the triples and 
     * quads versions because of type erasure.  
     */
    public static StreamRDF sinkTriples(Sink<Triple> sink)   { return new ParserOutputSinkTriples(sink) ; }

    /** 
     * Output to a sink; prefix and base handled only within the parser.
     * Unfortunately, Java needs different names for the triples and 
     * quads versions because of type erasure.  
     */
    public static StreamRDF sinkQuads(Sink<Quad> sink)       { return new ParserOutputSinkQuads(sink) ; }
    
    /** Convert any triples seen to a quads, adding a graph node of {@link Quad#tripleInQuad} */
    public static StreamRDF extendTriplesToQuads(StreamRDF base)
    { return extendTriplesToQuads(Quad.tripleInQuad, base) ; }
    
    /** Convert any triples seen to a quads, adding the specified graph node */
    public static StreamRDF extendTriplesToQuads(Node graphNode, StreamRDF base)
    { return new ParserOutputSinkTriplesToQuads(graphNode, base) ; }
    
    public static RDFParserOutputCounting count()
    { return new ParserOutputCountingBase(sinkNull()) ; }

    public static RDFParserOutputCounting count(StreamRDF other)
    { return new ParserOutputCountingBase(other) ; }

    private static class ParserOutputSinkTriplesToQuads extends StreamRDFWrapper
    {
        private final Node gn ;
        ParserOutputSinkTriplesToQuads(Node gn, StreamRDF base)
        { super(base) ; this.gn = gn ; }
        
        @Override public void triple(Triple triple)
        { sink.quad(new Quad(gn, triple)) ; }
    }

    private static class ParserOutputSinkTriples extends StreamRDFBase
    {
        private final Sink<Triple> sink ;

        public ParserOutputSinkTriples(Sink<Triple> sink)
        { this.sink = sink ; }

        @Override
        public void triple(Triple triple)
        { sink.send(triple) ; }

        @Override
        public void finish()
        { sink.flush() ; }
    }
    
    private static class ParserOutputSinkQuads extends StreamRDFBase
    {
        private final Sink<Quad> sink ;

        public ParserOutputSinkQuads(Sink<Quad> sink)
        { this.sink = sink ; }

        @Override
        public void quad(Quad quad)
        { sink.send(quad) ; }

        @Override
        public void finish()
        { sink.flush() ; }
    }
    
    private static class ParserOutputGraph extends StreamRDFBase
    {
        protected final Graph graph ;
        protected boolean warningIssued = false ;
        public ParserOutputGraph(Graph graph) { this.graph = graph ; }
        
        @Override public void triple(Triple triple)     { graph.add(triple) ; }
        @Override public void quad(Quad quad)
        {
            if ( quad.isTriple() || quad.isDefaultGraph() )
                graph.add(quad.asTriple()) ;
            else
            {
                if ( ! warningIssued )
                {
                    //SysRIOT.getLogger().warn("Only triples or default graph data expected : named graph data ignored") ;
                    // Not ideal - assumes the global default.
                    ErrorHandlerFactory.getDefaultErrorHandler().warning("Only triples or default graph data expected : named graph data ignored", -1, -1) ;
                }
                warningIssued = true ;
            }
            //throw new IllegalStateException("Quad passed to graph parsing") ;
        }
        
        @Override public void base(String base)
        { }

        @Override public void prefix(String prefix, String uri)
        {
            graph.getPrefixMapping().setNsPrefix(prefix, uri) ;
        }
    }

    private static class ParserOutputDataset extends StreamRDFBase
    {
        protected final DatasetGraph dsg ;
        public ParserOutputDataset(DatasetGraph dsg) { this.dsg = dsg ; }
        
        @Override public void triple(Triple triple) 
        {
            dsg.add(Quad.defaultGraphNodeGenerated, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
            //throw new IllegalStateException("Triple passed to dataset parsing") ;
        }
        
        @Override public void quad(Quad quad) 
        { 
            if ( quad.isTriple() )
                dsg.add(Quad.defaultGraphNodeGenerated, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            else
                dsg.add(quad) ;
        }
        
        @Override public void base(String base)
        { }

        @Override public void prefix(String prefix, String uri)
        {
            //dsg.getPrefixMapping().setNsPrefix(prefix, uri) ;
        }
    }

    private  static class ParserOutputCountingBase extends StreamRDFWrapper implements StreamRDF, RDFParserOutputCounting
    {
        private long countTriples = 0 ;
        private long countQuads = 0 ;
        private long countTuples = 0 ;
        private long countBase = 0 ;
        private long countPrefixes = 0 ;
        
        public ParserOutputCountingBase (StreamRDF other)     { super(other) ; }

        @Override
        public void triple(Triple triple)
        {
            countTriples++ ;
            super.triple(triple) ;
        }

        @Override
        public void quad(Quad quad)
        {
            countQuads++ ;
            super.quad(quad) ;
        }

        @Override
        public void tuple(Tuple<Node> tuple)
        {
            countTuples++ ;
            super.tuple(tuple) ;
        }
        
        @Override
        public long count()
        {
            return countTriples + countQuads + countTuples ;
        }

        @Override
        public long countTriples()
        {
            return countTriples ;
        }

        @Override
        public long countQuads()
        {
            return countQuads ;
        }

        @Override
        public long countTuples()
        {
            return countTuples ;
        }
    }
}