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

import java.io.OutputStream;

import org.apache.jena.graph.Graph ;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;

public class RDFWriterBuilder {
    private DatasetGraph dataset = null;
    private Graph        graph   = null;
    private Lang         lang    = null;
    private RDFFormat    format  = null;
    private String       baseURI = null;
    private ContextAccumulator contextAcc = ContextAccumulator.newBuilder(()->RIOT.getContext());

    /** A new {@code RDFWriterBuilder}.
     * <p>
     * See also {@link RDFWriter#create()}
     */
    private RDFWriterBuilder () {}

    public static RDFWriterBuilder create() { return new RDFWriterBuilder() ; }

    /** Set the source of writing to the graph argument.
     * <p>
     * Any previous source setting is cleared.
     * @param graph A {@link Graph}.
     * @return this
     */
    public RDFWriterBuilder source(Graph graph) {
        this.dataset = null;
        this.graph = graph;
        return this;
    }

    /** Set the source of writing to the graph argument.
     * <p>
     * Any previous source setting is cleared.
     * <p>
     * Equivalent to {@code source(model.getGraph()(s)}
     *
     * @param model A {@link Model}.
     * @return this
     */
    public RDFWriterBuilder source(Model model) {
        return source(model.getGraph());
    }

    /** Set the source of writing to the {@code DatasetGraph} argument.
     * <p>
     * Any previous source setting is cleared.
     * @param dataset A {@link DatasetGraph}.
     * @return this
     */
    public RDFWriterBuilder source(DatasetGraph dataset) {
        this.graph = null;
        this.dataset = dataset;
        return this;
    }

    /** Set the source of writing to the {@code DatasetGraph} argument.
     * <p>
     * Any previous source setting is cleared.
     * <p>
     * Equivalent to {@code source(dataset.asDatasetGraph())}
     *
     * @param dataset A {@link DatasetGraph}.
     * @return this
     */
    public RDFWriterBuilder source(Dataset dataset) {
        return source(dataset.asDatasetGraph());
    }

//    // Not implemented
//    public RDFWriterBuilder labels(NodeToLabel nodeToLabel) { return this; }
//
//    // Not implemented
//    public RDFWriterBuilder formatter(NodeFormatter nodeFormatter) { return this; }

    /**
     * Set the context for the writer when built.
     *
     * @param context
     * @return this
     * @see Context
     */
    public RDFWriterBuilder context(Context context) {
        contextAcc.context(context);
        return this;
    }

    /**
     * Add a setting to the context for the writer when built.
     * @param symbol
     * @param value
     * @return this
     * @see Context
     */
    public RDFWriterBuilder set(Symbol symbol, boolean value) {
        contextAcc.set(symbol, value);
        return this;
    }

    /**
     * Added a setting to the context for the writer when built.
     * A value of "null" removes a previous setting.
     * @param symbol
     * @param value
     * @return this
     * @see Context
     */
    public RDFWriterBuilder set(Symbol symbol, Object value) {
        contextAcc.set(symbol, value);
        return this;
    }

    /**
     * Set the output language to a {@link Lang}; this will set the format.
     * <p>
     * If {@code Lang} and {@code RDFFormat} are not set, an attempt is made to guess it from file name or URI on output.
     * <p>
     * If output is to an {@code OutputStream}, {@code Lang} or {@code RDFFormat} must be set.
     * <p>
     * Any previous setting of {@code Lang} or {@code RDFFormat} is cleared.
     *
     * @param lang
     * @return this
     */
    public RDFWriterBuilder lang(Lang lang) {
        this.format = null;
        this.lang = lang;
        return this;
    }

    /**
     * Set the output format to a {@link RDFFormat}.
     * <p>
     * If {@code Lang} and {@code RDFFormat} are not set, an attempt is made to guess it from file name or URI on output.
     * <p>
     * If output is to an {@code OutputStream}, {@code Lang} or {@code RDFFormat} must be set.
     * <p>
     * Any previous setting of {@code Lang} or {@code RDFFormat} is cleared.
     * @param format
     * @return this
     */
    public RDFWriterBuilder format(RDFFormat format) {
        this.lang = null;
        this.format = format;
        return this;
    }

    public RDFWriterBuilder base(String baseURI) {
        this.baseURI = baseURI;
        return this;
    }

    @Override
    public RDFWriterBuilder clone() {
        RDFWriterBuilder clone = new RDFWriterBuilder();
        clone.dataset       = this.dataset;
        clone.graph         = this.graph;
        clone.contextAcc    = this.contextAcc.clone();
        clone.lang          = this.lang;
        clone.format        = this.format;
        clone.baseURI       = this.baseURI;
        return clone;
    }

    public RDFWriter build() {
        Context context = contextAcc.context();
        if ( dataset == null && graph == null )
            throw new RiotException("No source to be written");
        return new RDFWriter(dataset, graph, format, lang, baseURI, context);
    }


    /**
     * Write output to a file.
     * This is a short form for {@code build().output(OutputStream)}.
     *
     * @param outputStream
     */
    public void output(OutputStream outputStream) {
        build().output(outputStream);
    }

    /**
     * Write output to a file.
     * This is a short form for {@code build().output(String)}.
     *
     * @param filename
     */
    public void output(String filename) {
        build().output(filename);
    }

    /**
     * Write and return the output as a string.
     * This is a short form for {@code build().asString()}.
     */
    public String asString() {
        return build().asString();
    }

    /**
     * Write and return the output as a string.
     * This is a short form for {@code build().toString()}.
     */
    @Override
    public String toString() {
        return build().toString();
    }
}
