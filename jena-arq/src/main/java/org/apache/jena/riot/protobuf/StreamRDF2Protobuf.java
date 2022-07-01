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

package org.apache.jena.riot.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.protobuf.wire.PB_RDF.*;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

/** Encode StreamRDF in RDF_StreamRow and send to a handler.
 *
 * @see Protobuf2StreamRDF for the reverse process.
 */
public class StreamRDF2Protobuf implements StreamRDF, AutoCloseable
{
    // No REPEAT support.

   // OutputStream + delimited.
    private PrefixMap pmap = PrefixMapFactory.create();
    private final boolean encodeValues;
    private final Consumer<RDF_StreamRow> rowHandler;
    private final Runnable andFinally;

    public static StreamRDF createDelimited(OutputStream outputStream, boolean withValues) {
        Consumer<RDF_StreamRow> output = sr->PBufRDF.writeDelimitedTo(sr, outputStream);
        return new StreamRDF2Protobuf(output, withValues, ()->IO.flush(outputStream));
    }

    public static void writeBlk(OutputStream outputStream, Consumer<StreamRDF> stream, boolean withValues) {
        RDF_Stream.Builder builder = RDF_Stream.newBuilder();
        Consumer<RDF_StreamRow> output = sr->builder.addRow(sr);
        StreamRDF2Protobuf processor = new StreamRDF2Protobuf(output, withValues, ()->IO.flush(outputStream));
        stream.accept(processor);
        RDF_Stream pbStream = builder.build();
        try {
            pbStream.writeTo(outputStream);
        } catch (IOException ex) { IO.exception(ex); }
    }


    private StreamRDF2Protobuf(Consumer<RDF_StreamRow> rowHandler, boolean encodeValues, Runnable atEnd) {
        this.pmap = PrefixMapFactory.create();
        this.encodeValues = encodeValues;
        this.rowHandler = rowHandler;
        this.andFinally = atEnd;
    }

    private RDF_StreamRow.Builder streamRowBuilder = RDF_StreamRow.newBuilder();
    private RDF_Triple.Builder tripleBuilder = RDF_Triple.newBuilder();
    private RDF_Quad.Builder quadBuilder = RDF_Quad.newBuilder();
    private RDF_PrefixDecl.Builder prefixBuilder = RDF_PrefixDecl.newBuilder();
    private RDF_IRI.Builder baseBuilder = RDF_IRI.newBuilder();
    private RDF_Term.Builder termBuilder = RDF_Term.newBuilder();

    @Override
    public void start() {}

    @Override
    public void finish() {
        andFinally.run();
    }

    @Override
    public void close() {
        finish();
    }


    @Override
    public void base(String base) {
        streamRowBuilder.clear();
        baseBuilder.clear();
        baseBuilder.setIri(base);
        streamRowBuilder.setBase(baseBuilder.build());
        rowHandler.accept(streamRowBuilder.build());
    }

    @Override
    public void prefix(String prefix, String iri) {
        streamRowBuilder.clear();
        prefixBuilder.clear();
        prefixBuilder.setPrefix(prefix);
        prefixBuilder.setUri(iri);
        streamRowBuilder.setPrefixDecl(prefixBuilder.build());
        rowHandler.accept(streamRowBuilder.build());
    }

    @Override
    public void triple(Triple triple) {
        streamRowBuilder.clear();
        RDF_Triple triplePB = PBufRDF.rdfTriple(triple, tripleBuilder, termBuilder);
        streamRowBuilder.setTriple(triplePB);
        rowHandler.accept(streamRowBuilder.build());
    }

    @Override
    public void quad(Quad quad) {
        streamRowBuilder.clear();
        RDF_Quad quadPB = PBufRDF.rdfQuad(quad, quadBuilder, termBuilder);
        streamRowBuilder.setQuad(quadPB);
        rowHandler.accept(streamRowBuilder.build());
    }
}
