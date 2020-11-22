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
import java.io.Writer ;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.CharSpace ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.writer.WriterStreamRDFPlain ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;

/** Various Common StreamRDF setups */
public class StreamRDFLib
{
    /** Send everything to nowhere ... efficiently */
    public static StreamRDF sinkNull()                       { return new StreamRDFBase() ; }

    /**
     * Create a {@link StreamRDF} that outputs to an {@link OutputStream}. It is important
     * to call {@link StreamRDF#start} and {@link StreamRDF#finish} because the output is
     * buffered.
     */
    public static StreamRDF writer(OutputStream out)         { return new WriterStreamRDFPlain(IO.wrapUTF8(out)) ; }

    /** Create a {@link StreamRDF} that outputs to an {@link AWriter}. */
    public static StreamRDF writer(AWriter out)              { return new WriterStreamRDFPlain(out) ; }

    /**
     * Create a {@link StreamRDF} that outputs to an {@link Writer}. It is important to
     * call {@link StreamRDF#start} and {@link StreamRDF#finish} because the output is
     * buffered.
     */
    public static StreamRDF writer(Writer out)               { return new WriterStreamRDFPlain(IO.wrap(out)) ; }

    /**
     * Create a {@link StreamRDF} that outputs to an {@link OutputStream} with a specific
     * {@link CharSpace} (ASCII or UTF-8).
     * <p>
     * It is important to call {@link StreamRDF#start}
     * and {@link StreamRDF#finish} because the output is buffered.
     */
    public static StreamRDF writer(OutputStream out, CharSpace charSpace) {
        switch (charSpace) {
            case ASCII :
                return new WriterStreamRDFPlain(IO.wrapASCII(out), charSpace) ;
            case UTF8 :
            default :
                return writer(out) ;
        }
    }

    /**
     * Create a {@link StreamRDF} that outputs to an {@link OutputStream} with a specific
     * {@link CharSpace} (ASCII or UTF-8).
     * <p>
     * It is important to call {@link StreamRDF#start}
     * and {@link StreamRDF#finish} because the output is buffered.
     */
    public static StreamRDF writer(AWriter out, CharSpace charSpace) {
        return new WriterStreamRDFPlain(out, charSpace) ;
    }

    /**
     * Create a {@link StreamRDF} that outputs to an {@link Writer} with a specific
     * {@link CharSpace} (ASCII or UTF-8) writing out-of-range codepoints (if ASCII)
     * as "\ uXXXX".
     * <p>
     * It is important to call {@link StreamRDF#start}
     * and {@link StreamRDF#finish} because the output is buffered.
     */
    public static StreamRDF writer(Writer out, CharSpace charSpace) {
        return new WriterStreamRDFPlain(IO.wrap(out), charSpace) ;
    }

    public static StreamRDF graph(Graph graph)               { return new ParserOutputGraph(graph) ; }

    public static StreamRDF dataset(DatasetGraph dataset)    { return new ParserOutputDataset(dataset) ; }

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

    public static StreamRDFCounting count()
    { return new StreamRDFCountingBase(sinkNull()) ; }

    public static StreamRDFCounting count(StreamRDF other)
    { return new StreamRDFCountingBase(other) ; }

    private static class ParserOutputSinkTriplesToQuads extends StreamRDFWrapper
    {
        private final Node gn ;
        ParserOutputSinkTriplesToQuads(Node gn, StreamRDF base)
        { super(base) ; this.gn = gn ; }

        @Override public void triple(Triple triple)
        { other.quad(new Quad(gn, triple)) ; }
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

    private static class ParserOutputGraph extends StreamRDFBase {
        protected final Graph graph;
        protected boolean     warningIssued = false;
        public ParserOutputGraph(Graph graph) {
            this.graph = graph;
        }

        @Override
        public void triple(Triple triple) {
            graph.add(triple);
        }

        @Override
        public void quad(Quad quad) {
            if ( quad.isTriple() || quad.isDefaultGraph() )
                graph.add(quad.asTriple());
            else {
                if ( !warningIssued ) {
                    // SysRIOT.getLogger().warn("Only triples or default graph data expected : named graph data ignored") ;
                    // Not ideal - assumes the global default.
                    ErrorHandlerFactory.getDefaultErrorHandler()
                        .warning("Only triples or default graph data expected : named graph data ignored", -1, -1);
                }
                warningIssued = true;
            }
            // throw new IllegalStateException("Quad passed to graph parsing") ;
        }

        @Override
        public void base(String base) {}

        @Override
        public void prefix(String prefix, String uri) {
            try { // Some graphs applies XML rules to prefixes.
                graph.getPrefixMapping().setNsPrefix(prefix, uri);
            } catch (JenaException ex) {}
        }
    }

    private static class ParserOutputDataset extends StreamRDFBase {
        protected final DatasetGraph dsg;
        protected final PrefixMap    prefixMap;

        public ParserOutputDataset(DatasetGraph dsg) {
            this.dsg = dsg;
            this.prefixMap = dsg.prefixes();
        }

        @Override
        public void triple(Triple triple) {
            dsg.add(Quad.defaultGraphNodeGenerated, triple.getSubject(), triple.getPredicate(), triple.getObject());
        }

        @Override
        public void quad(Quad quad) {
            if ( quad.isTriple() )
                dsg.add(Quad.defaultGraphNodeGenerated, quad.getSubject(), quad.getPredicate(), quad.getObject());
            else
                dsg.add(quad);
        }

        @Override
        public void base(String base) {}

        @Override
        public void prefix(String prefix, String uri) {
            try {
                // Some datasets may be tied to PrefixMappings and may apply XML
                // rules to prefixes.
                prefixMap.add(prefix, uri);
            } catch (JenaException ex) {}
        }
    }
}
