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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * An {@link RDFWriter} is a process that will output triples or quads in an RDF syntax.
 * {@link RDFWriterBuilder} provides the means to setup the writer.
 * <p>
 * The process is
 *
 * <pre>
 *    DatasetGraph dsg = ...
 *    RDFWriter writer = RDFWriter.create().source(dsg).lang(Lang.TTL).build();
 *    OutputStream out =
 *    writer.output(out);
 * </pre>
 * or using abbreviated forms:
 * <pre>
 *    RDFWriter.create().source(dsg).lang(Lang.TTL).output(out);
 * </pre>
 * @see WriterDatasetRIOT
 * @see WriterGraphRIOT
 */

public class RDFWriter {
    private static int BUF_SIZE = 128*1024;
    private final DatasetGraph dataset;
    private final Graph graph;
    private final RDFFormat format;
    private final String baseURI;
    private final Context context;

    /** Create an {@link RDFWriterBuilder}.
     * <p>
     * Often used in a pattern such as:
     * <pre>
     *    RDFWriter.create()
     *        .lang(Lang.TTL)
     *        .source(graph)
     *        .output(System.out);
     * </pre>
     */
    public static RDFWriterBuilder create() { return RDFWriterBuilder.create(); }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param graph     A {@link Graph}.
     * @return RDFWriterBuilder
     */
    public static RDFWriterBuilder source(Graph graph) {
        return create().source(graph);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param model     A {@link Model}.
     * @return RDFWriterBuilder
     */
    public static RDFWriterBuilder source(Model model) {
        return create().source(model);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param dataset     A {@link DatasetGraph}.
     * @return RDFWriterBuilder
     */
    public static RDFWriterBuilder source(DatasetGraph dataset) {
        return create().source(dataset);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param dataset     A {@link Dataset}.
     * @return RDFWriterBuilder
     */
    public static RDFWriterBuilder source(Dataset dataset) {
        return create().source(dataset);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param graph     A {@link Graph}.
     * @return RDFWriterBuilder
     * @deprecated Use {@link #source(Graph)}
     */
    @Deprecated
    public static RDFWriterBuilder create(Graph graph) {
        return create().source(graph);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param model     A {@link Model}.
     * @return RDFWriterBuilder
     * @deprecated Use {@link #source(Model)}
     */
    @Deprecated
    public static RDFWriterBuilder create(Model model) {
        return create().source(model);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param dataset     A {@link DatasetGraph}.
     * @return RDFWriterBuilder
     * @deprecated Use {@link #source(DatasetGraph)}
     */
    @Deprecated
    public static RDFWriterBuilder create(DatasetGraph dataset) {
        return create().source(dataset);
    }

    /** Create an {@link RDFWriterBuilder} and set the source of writing to the graph argument.
     * @param dataset     A {@link Dataset}.
     * @return RDFWriterBuilder
     * @deprecated Use {@link #source(Dataset)}
     */
    @Deprecated
    public static RDFWriterBuilder create(Dataset dataset) {
        return create().source(dataset);
    }

    /*package*/ RDFWriter(DatasetGraph dataset, Graph graph, RDFFormat format, Lang lang, String baseURI, Context context) {
        this.dataset = dataset;
        this.graph = graph;
        // format may still be null - output to a file later.
        this.format = chooseFormat(format, lang);
        this.baseURI = baseURI;
        this.context = context;
    }

    private static RDFFormat chooseFormat(RDFFormat format, Lang lang) {
        if ( format != null )
            return format;
        if ( lang == null )
            //throw new RiotException("No syntax for output") ;
            return null;
        format = RDFWriterRegistry.defaultSerialization(lang);
        return format;
    }

    /** Write and return as a string.
     * <p>
     * The {@code Lang} or {@code RDFFormat} must have been set.
     */
    public String asString() {
        if ( format == null )
            throw new RiotException("Output as a string needs the Lang/Format specificied");
        try ( StringWriter sw = new StringWriter() ) {
            output(sw, format);
            return sw.toString();
        } catch (IOException ex) { IO.exception(ex); return null; }
    }

    /** Write the source to the {@code OutputStream}.
     * <p>
     * The {@code Lang} or {@code RDFFormat} must have been set.
     * @param output
     */
    public void output(OutputStream output) {
        output(output, format);
    }

    /** Write the source to the Java {@code Writer}.
     * <p>
     * The {@code Lang} or {@code RDFFormat} must have been set.
     * @param javaWriter
     * @deprecated Using Java Writers risks corruption because of mismatch of character set. Only UTF-8 is safe.
     */
    @Deprecated
    public void output(Writer javaWriter) {
        output(javaWriter, format);
    }

    /** Write the source to a Java {@link StringWriter}.
     * <p>
     * The {@code Lang} or {@code RDFFormat} must have been set.
     * @param javaWriter
     */
    public void output(StringWriter javaWriter) {
        output(javaWriter, format);
    }

    /** Write the source to the file.
     * <p>
     * If neither {@code Lang} nor {@code RDFFormat} are set, an attempt to
     * guess an RDF Syntax is made from the file extension.
     * <p>Output to "-" goes to stdout.
     * @param filename
     */
    public void output(String filename) {
        Objects.requireNonNull(filename, "Null filename");
        RDFFormat fmt = format;
        if ( fmt == null ) {
            ContentType ct = RDFLanguages.guessContentType(filename);
            if ( ct == null )
                throw new RiotException("Lang and RDFformat unset and can't determine syntax from '"+filename+"'");
            Lang lang = RDFLanguages.contentTypeToLang(ct);
            if ( lang == null )
                throw new RiotException("No syntax registered for '"+ct.getContentTypeStr()+"'");
            fmt = RDFWriterRegistry.defaultSerialization(lang);
        }
        if ( filename.equals("-") ) {
            output(System.out, fmt);
            return;
        }
        Path p = Path.of(filename);
        try ( OutputStream out1 = Files.newOutputStream(p);
              OutputStream out = new BufferedOutputStream(out1, BUF_SIZE)){
            output(out, fmt);
        } catch (IOException ex) { IO.exception(ex); }
    }

    private void output(OutputStream output, RDFFormat format) {
        if ( format == null )
            throw new RiotException("No syntax (Lang or RDFFormat) for output") ;
        if ( graph != null ) {
            write$(output, graph, format);
            return;
        }
        if ( dataset != null ) {
            write$(output, dataset, format);
            return;
        }
        throw new RiotException("No graph or dataset to write") ;
    }

    private void output(Writer javaWriter, RDFFormat format) {
        if ( format == null )
            throw new RiotException("No syntax (Lang or RDFFormat) for output") ;
        if ( graph != null ) {
            write$(javaWriter, graph, format);
            return;
        }
        if ( dataset != null ) {
            write$(javaWriter, dataset, format);
            return;
        }
        throw new RiotException("No graph or dataset to write") ;
    }

    // Allowing an externally set PrefixMap was (probably) a mistake.
    private static WriterGraphRIOT createGraphWriter$(RDFFormat serialization) {
        WriterGraphRIOTFactory wf = RDFWriterRegistry.getWriterGraphFactory(serialization);
        if ( wf == null )
            throw new RiotException("No graph writer for " + serialization);
        return wf.create(serialization);
    }

    private static WriterDatasetRIOT createDatasetWriter$(RDFFormat serialization) {
        WriterDatasetRIOTFactory wf = RDFWriterRegistry.getWriterDatasetFactory(serialization);
        if ( wf == null )
            throw new RiotException("No dataset writer for " + serialization);
        return wf.create(serialization);
    }

    private void write$(OutputStream out, Graph graph, RDFFormat serialization) {
        WriterGraphRIOT w = createGraphWriter$(serialization);
        w.write(out, graph, prefixMap(graph), baseURI, context);
    }

    private void write$(OutputStream out, DatasetGraph dataset, RDFFormat serialization) {
        WriterDatasetRIOT w = createDatasetWriter$(serialization);
        w.write(out, dataset, prefixMap(dataset), baseURI, context);
    }

    private void write$(Writer out, Graph graph, RDFFormat serialization) {
        WriterGraphRIOT w = createGraphWriter$(serialization);
        w.write(out, graph, prefixMap(graph), baseURI,context);
    }

    private void write$(Writer out, DatasetGraph dataset, RDFFormat serialization) {
        WriterDatasetRIOT w = createDatasetWriter$(serialization);
        w.write(out, dataset, prefixMap(dataset), baseURI, context);
    }

    private  PrefixMap prefixMap(DatasetGraph dataset) {
        return PrefixMapFactory.createForOutput(dataset.prefixes());
    }

    private static PrefixMap prefixMap(Graph graph) {
        return PrefixMapFactory.createForOutput(graph.getPrefixMapping());
    }
}
