/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdflink.dataset;

import java.util.Objects;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.update.UpdateRequest;

/**
 * {@link StreamRDF} that writes to an {@link RDFLink}.
 */
/* package */ class StreamRDFToRDFLink implements StreamRDF {
    public static final int DFT_BUFFER_SIZE = 1000;

    private RDFLink link;
    private int bufferSize;
    private PrefixMapping prefixes;
    private QuadDataAcc quadAcc = new QuadDataAcc();

    /**
     * Constructs the StreamRDFToRDFLink using default {@value #DFT_BUFFER_SIZE} quad buffer size.
     *
     * @param link the link to talk to.
     */
    public StreamRDFToRDFLink(RDFLink link) {
        this(link, null);
    }

    public StreamRDFToRDFLink(RDFLink link, PrefixMapping prefixes) {
        this(link, prefixes, DFT_BUFFER_SIZE);
    }

    public StreamRDFToRDFLink(RDFLink connection, PrefixMapping prefixes, int bufferSize) {
        super();
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Buffer size must be at least 1");
        }

        this.link = Objects.requireNonNull(connection);
        this.prefixes = prefixes;
        this.bufferSize = bufferSize;
    }

    /**
     * See if we should flush the buffer.
     */
    private void isBufferFull() {
        if ( quadAcc.getQuads().size() >= bufferSize ) {
            flush();
        }
    }

    /**
     * Flushes the buffer to the connection.
     */
    private void flush() {
        UpdateRequest updateRequest = new UpdateRequest(new UpdateDataInsert(quadAcc));
        if (prefixes != null) {
            updateRequest.setPrefixMapping(prefixes);
        }
        quadAcc = new QuadDataAcc();
        link.update(updateRequest);
        quadAcc.close();
    }

    @Override
    public void start() {
        // does nothing.
    }

    @Override
    public void triple(Triple triple) {
        quadAcc.addTriple(triple);
        isBufferFull();
    }

    @Override
    public void quad(Quad quad) {
        quadAcc.addQuad(quad);
        isBufferFull();
    }

    @Override
    public void base(String base) {
        // do nothing
    }

    @Override
    public void version(String version) {}

    @Override
    public void prefix(String prefix, String iri) {
        if (prefixes != null) {
            prefixes.setNsPrefix(prefix, iri);
        }
    }

    @Override
    public void finish() {
        flush();
    }

    // ----- Utils; move to StreamRDFOps? -----

    static class StreamRDFTriplesToQuads
        extends StreamRDFWrapper {

        protected final Node graphName;

        public StreamRDFTriplesToQuads(StreamRDF other, Node graphName) {
            super(other);
            this.graphName = Objects.requireNonNull(graphName);
        }

        @Override
        public void triple(Triple triple) {
            Quad quad = Quad.create(graphName, triple);
            get().quad(quad);
        }
    }

    /** Send triples of the source graph as quads in the given target graph to the sink. */
    static void sendGraphTriplesToStream(Graph sourceGraph, Node targetGraphName, StreamRDF sink) {
        boolean isSinkDefaultGraph = targetGraphName == null || Quad.isDefaultGraph(targetGraphName);
        StreamRDF effectiveSink = isSinkDefaultGraph ? sink : new StreamRDFTriplesToQuads(sink, targetGraphName);
        StreamRDFOps.sendGraphTriplesToStream(sourceGraph, effectiveSink);
    }
}
