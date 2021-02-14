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

import java.io.*;
import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.IteratorResourceClosing;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIs;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.riot.lang.RiotParsers;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.riot.writer.NQuadsWriter;
import org.apache.jena.riot.writer.NTriplesWriter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * General purpose reader framework for RDF (triples and quads) syntaxes.
 * </p>
 * <ul>
 * <li>HTTP Content negotiation</li>
 * <li>File type hint by the extension</li>
 * <li>Application language hint</li>
 * </ul>
 * <p>
 * It also provides a way to lookup names in different locations and to remap URIs to
 * other URIs.
 * </p>
 * <p>
 * Extensible - a new syntax can be added to the framework.
 * </p>
 * <p>
 * Operations fall into the following categories:
 * </p>
 * <ul>
 * <li>{@code read} -- Read data from a location into a Model, Dataset, etc. The
 * methods in this class treat all types of Model in the same way. For behavior
 * specific to a subtype of Model, use the methods of that specific class.</li>
 * <li>{@code loadXXX} -- Read data and return an in-memory object holding the
 * data.</li>
 * <li>{@code parse} -- Read data and send to an {@link StreamRDF}</li>
 * <li>{@code open} -- Open a typed input stream to the location, using any
 * alternative locations</li>
 * <li>{@code write} -- Write Model/Dataset etc</li>
 * <li>{@code create} -- Create a reader or writer explicitly</li>
 * </ul>
 * <p>
 * {@code RDFDataMgr} provides single functions for many of the common application
 * patterns. It is built on top of {@link RDFParser} for reading and
 * {@link RDFWriter} for output. Each of these classes has an associated builder that
 * provides complete control over the parsing process. For example, to translate
 * language tags to lower case on input:
 *
 * <pre>
 *     RDFParser.create()
 *         .source("myData.ttl")
 *         .langTagLowerCase()
 *         .parse(graph);
 * </pre>
 *
 * or to have Turtle written with {@code BASE} and {@code PREFIX} rather than
 * {@code @base} and {@code @prefix} (both are legal Turtle):
 * <pre>
 *     RDFWriter.create()
 *         .set(RIOT.symTurtleDirectiveStyle, "rdf11")
 *         .source(model)
 *         .output(System.out);
 * </pre>
 */

public class RDFDataMgr
{
    static { JenaSystem.init(); }

    static Logger log = LoggerFactory.getLogger(RDFDataMgr.class);

    /**
     * Read triples into a Model from the given location. The syntax is determined
     * from input source URI (content negotiation or file extension).
     *
     * @param model Destination for the RDF read.
     * @param uri URI to read from (includes file: and a plain file name).
     * @throws RiotNotFoundException if the location is not found - the model is
     *     unchanged.
     */
    public static void read(Model model, String uri) {
        Objects.requireNonNull(model, "Model is null");
        read(model.getGraph(), uri, defaultLang(uri));
    }

    /**
     * Read triples into a Model from the given location. The syntax is determined
     * from input source URI (content negotiation or file extension).
     *
     * @param graph Destination for the RDF read.
     * @param uri URI to read from (includes file: and a plain file name).
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
     */
    public static void read(Graph graph, String uri) {
        read(graph, uri, defaultBase(uri), defaultLang(uri));
    }

    /**
     * Read triples into a Model from the given location, with a hint of the language
     * (MIME type)
     *
     * @param model Destination for the RDF read.
     * @param uri URI to read from (includes file: and a plain file name).
     * @param hintLang Hint for the syntax.
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, Lang hintLang) {
        read(model.getGraph(), uri, hintLang);
    }

    /**
     * Read triples into a Model from the given location, with a hint of the language
     * (MIME type or short name)
     *
     * @param graph Destination for the RDF read.
     * @param uri URI to read from (includes file: and a plain file name).
     * @param hintLang Hint for the syntax.
     * @throws RiotNotFoundException if the location is not found - the graph is unchanged.
     */
    public static void read(Graph graph, String uri, Lang hintLang) {
        read(graph, uri, defaultBase(uri), hintLang);
    }

    /**
     * Read triples into a Model from the given location, with a hint of language.
     * Throws parse errors depending on the
     * language and reader; the model may be partially updated.
     *
     * @param model Destination for the RDF read.
     * @param uri URI to read from (includes file: and a plain file name).
     * @param base Base URI (defaults to uri).
     * @param hintLang Hint for the syntax
     * @throws RiotNotFoundException if the location is not found - the model is unchanged.
     */
    public static void read(Model model, String uri, String base, Lang hintLang) {
        read(model.getGraph(), uri, base, hintLang);
    }

    /**
     * Read triples into a Model from the given location, with a hint of language.
     * Throws parse errors depending on the language and reader; the model may be partially updated.
     *
     * @param graph Destination for the RDF read.
     * @param uri URI to read from (includes file: and a plain file name).
     * @param base Base URI (defaults to uri).
     * @param hintLang Hint for the syntax
     * @throws RiotNotFoundException if the location is not found - the graph is
     *     unchanged.
     */
    public static void read(Graph graph, String uri, String base, Lang hintLang) {
        Objects.requireNonNull(graph, "Graph is null");
        StreamRDF dest = StreamRDFLib.graph(graph);
        parseFromURI(dest, uri, base, hintLang);
    }

    /**
     * Read triples into a Model with bytes from an InputStream.
     * A base URI and a syntax can be provided.
     * The base URI defaults to "no base" in which case the data should have no relative URIs.
     * The lang gives the syntax of the stream.
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Model model, InputStream in, Lang lang) {
        read(model.getGraph(), in, lang);
    }

    /**
     * Read triples into a Model with bytes from an InputStream.
     * A base URI and a syntax can be provided.
     * The base URI defaults to "no base" in which case the data should have no relative URIs.
     * The lang gives the syntax of the stream.
     *
     * @param graph     Destination for the RDF read.
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Graph graph, InputStream in, Lang lang) {
        read(graph, in, defaultBase(), lang);
    }

    /**
     * Read triples into a Model with bytes from an InputStream.
     * A base URI and a syntax can be provided.
     * The base URI defaults to "no base" in which case the data should have no relative URIs.
     * The lang gives the syntax of the stream.
     *
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Model model, InputStream in, String base, Lang lang) {
        read(model.getGraph(), in, base, lang);
    }

    /**
     * Read triples into a Model with bytes from an InputStream.
     * A base URI and a syntax can be provided.
     * The base URI defaults to "no base" in which case the data should have no relative URIs.
     * The lang gives the syntax of the stream.
     *
     * @param graph     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Graph graph, InputStream in, String base, Lang lang) {
        Objects.requireNonNull(graph, "Graph is null");
        Objects.requireNonNull(in, "InputStream is null");
        StreamRDF dest = StreamRDFLib.graph(graph);
        parseFromInputStream(dest, in, base, lang);
    }

    /**
     * Read triples into a model with chars from an Reader.
     * Use of java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream}, not a {@link java.io.Reader}
     * to ensure correct character set handling.
     *
     * @param model     Destination for the RDF read.
     * @param in        Reader
     * @param base      Base URI
     * @param lang      Language syntax
     * @deprecated      Use an InputStream or StringReader.
     */
    @Deprecated
    public static void read(Model model, Reader in, String base, Lang lang) {
        Objects.requireNonNull(in, "Reader is null");
        read(model.getGraph(), in, base,  lang);
    }

    /** Read triples into a model with chars from an Reader.
     * Use of java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream}, not a {@link java.io.Reader}
     * to ensure correct character set handling.
     * @param graph     Destination for the RDF read.
     * @param in        Reader
     * @param base      Base URI
     * @param lang      Language syntax
     * @deprecated      Use an InputStream or StringReader.
     */
    @Deprecated
    public static void read(Graph graph, Reader in, String base, Lang lang) {
        StreamRDF dest = StreamRDFLib.graph(graph);
        parseFromReader(dest, in, base, lang);
    }

    /**
     * Read triples into a model with chars from a StringReader.
     *
     * @param model     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Model model, StringReader in, String base, Lang lang) {
        Graph g = model.getGraph();
        StreamRDF dest = StreamRDFLib.graph(g);
        parseFromReader(dest, in, base, lang);
    }

    /**
     * Read triples into a model with chars from a StringReader.
     *
     * @param graph     Destination for the RDF read.
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Graph graph, StringReader in, String base, Lang lang) {
        Objects.requireNonNull(graph, "Graph is null");
        Objects.requireNonNull(in, "StringReader is null");
        StreamRDF dest = StreamRDFLib.graph(graph);
        parseFromReader(dest, in, base, lang);
    }

    private static Model createModel()                  { return ModelFactory.createDefaultModel(); }
    private static Graph createGraph()                  { return GraphFactory.createDefaultGraph(); }
    private static Dataset createDataset()              { return DatasetFactory.createTxnMem(); }
    private static DatasetGraph createDatasetGraph()    { return DatasetGraphFactory.createTxnMem(); }

    /**
     * Create a memory Model and read in some data
     *
     * @see #read(Model,String)
     */
    public static Model loadModel(String uri) {
        Model m = createModel();
        read(m, uri);
        return m;
    }

    /**
     * Create a memory Model and read in some data
     *
     * @see #read(Model,String,Lang)
     */
    public static Model loadModel(String uri, Lang lang) {
		Model m = createModel();
        read(m, uri,lang);
        return m;
	}

    /**
     * Create a memory Graph and read in some data
     * @see #read(Graph,String)
     */
    public static Graph loadGraph(String uri) {
        Graph g = createGraph();
        read(g, uri);
        return g;
    }

	/**
	 * Create a memory Graph and read in some data
     * @see #read(Graph,String,Lang)
     */
    public static Graph loadGraph(String uri, Lang lang) {
        Graph g = createGraph();
        read(g, uri, lang);
        return g;
    }

	/**
	 * Create a memory Dataset and read in some data
     * @see #read(Dataset,String,Lang)
     */
    public static Dataset loadDataset(String uri) {
        Dataset ds = createDataset();
        read(ds, uri);
        return ds;
    }

	/**
	 * Create a memory Dataset and read in some data
     * @see #read(Dataset,String,Lang)
     */
    public static Dataset loadDataset(String uri, Lang lang) {
        Dataset ds = createDataset();
        read(ds, uri, lang);
        return ds;
	}

	/**
	 * Create a memory DatasetGraph and read in some data
     * @see #read(DatasetGraph,String)
     */
    public static DatasetGraph loadDatasetGraph(String uri)	{
		DatasetGraph ds = createDatasetGraph();
        read(ds, uri);
        return ds;
	}

	/**
	 * Create a memory DatasetGraph and read in some data
     * @see #read(DatasetGraph,String,Lang)
     */
    public static DatasetGraph loadDatasetGraph(String uri, Lang lang) {
		DatasetGraph ds = createDatasetGraph();
        read(ds, uri, lang);
        return ds;
	}

    /**
     * Read quads or triples into a Dataset from the given location, with a hint of the language.
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     */
    public static void read(Dataset dataset, String uri) {
        read(dataset.asDatasetGraph(), uri);
    }

    /**
     * Read quads or triples into a Dataset from the given location, with a hint of the language.
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     */
    public static void read(DatasetGraph dataset, String uri) {
        read(dataset, uri, defaultLang(uri));
    }

    /**
     * Read quads or triples into a Dataset from the given location, with a hint of the language.
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(Dataset dataset, String uri, Lang hintLang) {
        read(dataset.asDatasetGraph(), uri, hintLang);
    }

    /**
     * Read quads or triples into a Dataset from the given location, with a hint of the language.
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Language syntax
     */
    public static void read(DatasetGraph dataset, String uri, Lang hintLang) {
        read(dataset, uri, defaultBase(uri), hintLang);
    }

    /**
     * Read quads or triples into a Dataset from the given location, with a hint of the language.
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Language syntax
     */
    public static void read(Dataset dataset, String uri, String base, Lang hintLang) {
        read(dataset.asDatasetGraph(), uri, base, hintLang);
    }

    /**
     * Read quads or triples into a Dataset from the given location, with a hint of the language.
     * @param dataset   Destination
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Language syntax
     */
    public static void read(DatasetGraph dataset, String uri, String base, Lang hintLang) {
        StreamRDF sink = StreamRDFLib.dataset(dataset);
        parseFromURI(sink, uri, base, hintLang);
    }

    /**
     * Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, InputStream in, Lang lang) {
        read(dataset.asDatasetGraph(), in, lang);
    }

    /**
     * Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param lang      Language syntax
     */
    public static void read(DatasetGraph dataset, InputStream in, Lang lang) {
        read(dataset, in, defaultBase(), lang);
    }

    /**
     * Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, InputStream in, String base, Lang lang) {
        read(dataset.asDatasetGraph(), in, base, lang);
    }

    /**
     * Read quads or triples into a dataset with bytes from an input stream.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(DatasetGraph dataset, InputStream in, String base, Lang lang) {
        Objects.requireNonNull(in, "InputStream is null");
        StreamRDF dest = StreamRDFLib.dataset(dataset);
        parseFromInputStream(dest, in, base, lang);
    }

    /**
     * Read quads into a dataset with chars from an Reader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     * @deprecated use an InputStream or a StringReader.
     */
    @Deprecated
    public static void read(Dataset dataset, Reader in, String base, Lang lang) {
        Objects.requireNonNull(in, "Java Reader is null");
		read(dataset.asDatasetGraph(), in, base, lang);
    }

    /**
     * Read quads into a dataset with chars from an Reader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     * @deprecated use an InputStream or a StringReader.
     */
    @Deprecated
    public static void read(DatasetGraph dataset, Reader in, String base, Lang lang) {
        StreamRDF dest = StreamRDFLib.dataset(dataset);
        parseFromReader(dest, in, base, lang);
    }

    /**
     * Read quads into a dataset with chars from a StringReader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(Dataset dataset, StringReader in, String base, Lang lang) {
        read(dataset.asDatasetGraph(), in, base, lang);
    }

    /**
     * Read quads into a dataset with chars from a StringReader.
     * Use java.io.Readers is not encouraged - use with a StringReader is the primary use case.
     * For files, open a {@link java.io.FileInputStream} to ensure correct character set handling.
     * @param dataset   Destination
     * @param in        InputStream
     * @param base      Base URI
     * @param lang      Language syntax
     */
    public static void read(DatasetGraph dataset, StringReader in, String base, Lang lang) {
        StreamRDF dest = StreamRDFLib.dataset(dataset);
        parseFromReader(dest, in, base, lang);
    }

    /**
     * Read RDF data.
     * @param sink     Destination for the RDF read.
     * @param uri      URI to read from (includes file: and a plain file name).
     * @deprecated     Use {@code RDFParser.source(uri).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, String uri) {
        parse(sink, uri, defaultLang(uri));
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param lang      Hint for the syntax
     * @deprecated      Use {@code RDFParser.source(uri).lang(hintLang).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, String uri, Lang lang) {
        parse(sink, uri, defaultBase(uri), lang);
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @deprecated      Use {@code RDFParser.source(uri).base(base).lang(hintLang).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, String uri, String base, Lang hintLang) {
        if ( uri == null )
            throw new IllegalArgumentException("URI to read from is null");
        if ( base == null )
            base = IRIs.toBase(uri);
        if ( hintLang == null )
            hintLang = RDFLanguages.pathnameToLang(uri);
        parseFromURI(sink, uri, base, hintLang);
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read.
     * @param lang      Syntax for the stream.
     * @deprecated     To be removed.  Use {@code RDFParser.source(in).lang(lang).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, InputStream in, Lang lang) {
        parseFromInputStream(sink, in, defaultBase(), lang);
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read.
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @deprecated     To be removed.  Use {@code RDFParser.source(in).lang(lang).base(base).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, InputStream in, String base, Lang hintLang) {
        parseFromInputStream(sink, in, base, hintLang);
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        StringReader
     * @param lang      Syntax for the stream.
     * @deprecated     To be removed. Use {@code RDFParser.create().source(in).lang(hintLang)...}
     */
    @Deprecated
    public static void parse(StreamRDF sink, StringReader in, Lang lang) {
        parse(sink, in, defaultBase(), lang);
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Reader
     * @param base      Base URI (defaults to uri).
     * @param hintLang  Hint for the syntax
     * @deprecated     To be removed. Use {@code RDFParser.create().source(in).base(base).lang(hintLang).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, StringReader in, String base, Lang hintLang) {
        parseFromReader(sink, in, base, hintLang);
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read. This must include the content type.
     * @deprecated     To be removed. Use an {@code InputStream} and {@code RDFParser.source(in).lang(hintLang).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, TypedInputStream in) {
        parse(sink, in, defaultBase());
    }

    /**
     * Read RDF data.
     * @param sink      Destination for the RDF read.
     * @param in        Bytes to read.
     * @param base      Base URI
     * @deprecated      To be removed. Use an {@code InputStream} and {@code RDFParser.source(in).base(base).lang(lang).parse(sink)}
     */
    @Deprecated
    public static void parse(StreamRDF sink, TypedInputStream in, String base) {
        Objects.requireNonNull(in, "TypedInputStream is null");
        Lang hintLang = RDFLanguages.contentTypeToLang(in.getMediaType());
        processFromTypedInputStream(sink, in, base, hintLang);
    }

    /**
     * Open a stream to the destination (URI or filename)
     * Performs content negotiation, including looking at file extension.
     * @param filenameOrURI
     * @return TypedInputStream
     */
    public static TypedInputStream open(String filenameOrURI)
    { return open(filenameOrURI, StreamManager.get()); }

    /**
     * Open a stream to the destination (URI or filename)
     * Performs content negotiation, including looking at file extension.
     * @param filenameOrURI
     * @param streamManager
     * @return TypedInputStream
     */
    public static TypedInputStream open(String filenameOrURI, StreamManager streamManager) {
        TypedInputStream in = streamManager.open(filenameOrURI);
        if ( in == null )
            throw new RiotNotFoundException("Not found: "+filenameOrURI);
        return in;
    }

    // ----
    // The ways to parse from 4 kinds of source: URI, InputStream, Reader and TypedInputStream

    private static void parseFromInputStream(StreamRDF destination, InputStream in, String baseUri, Lang lang) {
        RDFParser.create()
            .source(in)
            .base(baseUri)
            .lang(lang)
            //.context(context)
            .parse(destination);
    }

    @SuppressWarnings("deprecation")
    private static void parseFromReader(StreamRDF destination, Reader in, String baseUri, Lang lang) {
        RDFParser.create()
            .source(in)
            .base(baseUri)
            .lang(lang)
            //.context(context)
            .parse(destination);
    }

    private static void parseFromURI(StreamRDF destination, String uri, String baseUri, Lang lang) {
        RDFParser.create()
            .source(uri)
            .base(baseUri)
            .lang(lang)
            //.context(context)
            .parse(destination);
    }

    // ---- Support for RDFDataMgr.parse from a TypedInputStream only.
    private static void processFromTypedInputStream(StreamRDF sink, TypedInputStream in, String baseUri, Lang hintLang) {
        // If the input stream comes with a content type, use that in preference to the hint (compatibility).
        // Except for text/plain.
        // Do here, which duplicates RDFParser, because "TypedInputStream" gets lost at RDFParser
        if ( in.getContentType() != null ) {
            // Special case of text/plain.
            ContentType ct = WebContent.determineCT(in.getContentType(), hintLang, null);
            Lang lang2 = RDFLanguages.contentTypeToLang(ct);
            hintLang = lang2;
        }
        RDFParser.create()
            .source(in)
            .base(baseUri)
            .lang(hintLang)
            // We made the decision above.
            .forceLang(hintLang)
            //.context(context)
            .parse(sink);
    }

    // Operations to remove "null"s in the code.

    /** Default base - no known URI. e.g. input streams */
    private static String defaultBase() {
        return null;
    }

    /** Default base - URI present */
    private static String defaultBase(String uri) {
        return uri;
    }

    /** Default lang - usually left as unknown so that extended content negotiation happens */
    private static Lang defaultLang(String uri) {
        return null;
    }

    /** Map {@link Lang} to {@link RDFFormat}, or throw an exception. */
    private static RDFFormat langToFormatOrException(Lang lang) {
        RDFFormat serialization = RDFWriterRegistry.defaultSerialization(lang);
        if ( serialization == null )
            throw new RiotException("No output format for "+lang);
        return serialization;
    }

    /** Determine the Lang, given the URI target, any content type header string and a hint */
    public static Lang determineLang(String target, String ctStr, Lang hintLang) {
        ContentType ct = WebContent.determineCT(ctStr, hintLang, target);
        if ( ct == null )
            return hintLang;
        Lang lang = RDFLanguages.contentTypeToLang(ct);
        if (lang == null )
            return hintLang;
        return lang;
    }

    // -------- WRITERS

    /** Write the model to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param model     Graph to write
     * @param lang      Language for the serialization.
     */
    public static void write(OutputStream out, Model model, Lang lang) {
        write(out, model.getGraph(), lang);
    }

    /** Write the model to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param model         Model to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, Model model, RDFFormat serialization) {
        write(out, model.getGraph(), serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param model         Model to write
     * @param lang          Serialization format
     */
    public static void write(StringWriter out, Model model, Lang lang) {
        write(out, model.getGraph(), lang);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param model         Model to write
     * @param lang          Serialization format
     * @deprecated          Use of writers is deprecated - use an {@link java.io.OutputStream}
     */
    @Deprecated
    public static void write(Writer out, Model model, Lang lang) {
        write(out, model.getGraph(), lang);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param model         Model to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, Model model, RDFFormat serialization) {
        write(out, model.getGraph(), serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param model         Model to write
     * @param serialization Serialization format
     * @deprecated          Use of writers is deprecated - use an {@link java.io.OutputStream}
     */
    @Deprecated
    public static void write(Writer out, Model model, RDFFormat serialization) {
        write(out, model.getGraph(), serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param graph     Graph to write
     * @param lang      Language for the serialization.
     */
    public static void write(OutputStream out, Graph graph, Lang lang) {
        RDFFormat serialization = langToFormatOrException(lang);
        write(out, graph, serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, Graph graph, RDFFormat serialization) {
        write$(out, graph, serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param graph         Graph to write
     * @param lang          Serialization format
     */
    public static void write(StringWriter out, Graph graph, Lang lang) {
        // Only known reasonable use of a Writer
        write$(out, graph, langToFormatOrException(lang));
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param graph         Graph to write
     * @param lang          Serialization format
     * @deprecated          Use of writers is deprecated - use an {@link java.io.OutputStream}
     */
    @Deprecated
    public static void write(Writer out, Graph graph, Lang lang) {
        write$(out, graph, langToFormatOrException(lang));
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, Graph graph, RDFFormat serialization) {
        // Only known reasonable use of a Writer
        write$(out, graph, serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param graph         Graph to write
     * @param serialization Serialization format
     * @deprecated          Use of writers is deprecated - use an {@link java.io.OutputStream}
     */
    @Deprecated
    public static void write(Writer out, Graph graph, RDFFormat serialization) {
        write$(out, graph, serialization);
    }

    /** Write the Dataset to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param dataset   Dataset to write
     * @param lang      Language for the serialization.
     */
    public static void write(OutputStream out, Dataset dataset, Lang lang) {
        write(out, dataset.asDatasetGraph(), lang);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param dataset       Dataset to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, Dataset dataset, RDFFormat serialization) {
        write(out, dataset.asDatasetGraph(), serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param dataset       Dataset to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, Dataset dataset, RDFFormat serialization) {
        write$(out, dataset.asDatasetGraph(), serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param dataset       Dataset to write
     * @param lang      Language for the serialization.
     */
    public static void write(StringWriter out, Dataset dataset, Lang lang) {
        RDFFormat serialization = langToFormatOrException(lang);
        write$(out, dataset.asDatasetGraph(), serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param dataset       Dataset to write
     * @param serialization Serialization format
     * @deprecated          Use of writers is deprecated - use an {@link java.io.OutputStream}
     */
    @Deprecated
    public static void write(Writer out, Dataset dataset, RDFFormat serialization) {
        write$(out, dataset.asDatasetGraph(), serialization);
    }

    /** Write the DatasetGraph to the output stream in the default serialization for the language.
     * @param out       OutputStream
     * @param dataset   DatasetGraph to write
     * @param lang      Language for the serialization.
     */
    public static void write(OutputStream out, DatasetGraph dataset, Lang lang) {
        RDFFormat serialization = langToFormatOrException(lang);
        write(out, dataset, serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           OutputStream
     * @param dataset       DatasetGraph to write
     * @param serialization Serialization format
     */
    public static void write(OutputStream out, DatasetGraph dataset, RDFFormat serialization) {
        write$(out, dataset, serialization);
    }

    /** Write the DatasetGraph to the output stream in the default serialization for the language.
     * @param out       StringWriter
     * @param dataset   DatasetGraph to write
     * @param lang      Language for the serialization.
     */
    public static void write(StringWriter out, DatasetGraph dataset, Lang lang) {
        RDFFormat serialization = langToFormatOrException(lang);
        write(out, dataset, serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           StringWriter
     * @param dataset       DatasetGraph to write
     * @param serialization Serialization format
     */
    public static void write(StringWriter out, DatasetGraph dataset, RDFFormat serialization) {
        write$(out, dataset, serialization);
    }

    /** Write the graph to the output stream in the default serialization for the language.
     * @param out           Writer
     * @param dataset       DatasetGraph to write
     * @param serialization Serialization format
     * @deprecated          Use of writers is deprecated - use an {@link java.io.OutputStream}
     */
    @Deprecated
    public static void write(Writer out, DatasetGraph dataset, RDFFormat serialization) {
        write$(out, dataset, serialization);
    }

    /** Write an iterator of triples (in N-Triples)
     * @param out
     * @param iterator
     */
    public static void writeTriples(OutputStream out, Iterator<Triple> iterator) {
        NTriplesWriter.write(out, iterator);
    }

    /** Write an iterator of quads (in N-Quads)
     * @param out
     * @param iterator
     */
    public static void writeQuads(OutputStream out, Iterator<Quad> iterator) {
        NQuadsWriter.write(out, iterator);
    }

    private static WriterDatasetRIOT createDatasetWriter$(RDFFormat serialization) {
        WriterDatasetRIOTFactory wf = RDFWriterRegistry.getWriterDatasetFactory(serialization);
        if ( wf == null )
            throw new RiotException("No dataset writer for " + serialization);
        return wf.create(serialization);
    }

    private static void write$(OutputStream out, Graph graph, RDFFormat serialization) {
        RDFWriter.create(graph).format(serialization).output(out);
    }

    @SuppressWarnings("deprecation")
    private static void write$(Writer out, Graph graph, RDFFormat serialization) {
        RDFWriter.create(graph).format(serialization).build().output(out);
    }

    private static void write$(OutputStream out, DatasetGraph dataset, RDFFormat serialization) {
        RDFWriter.create(dataset).format(serialization).output(out);
    }

    @SuppressWarnings("deprecation")
    private static void write$(Writer out, DatasetGraph dataset, RDFFormat serialization) {
        RDFWriter.create(dataset).format(serialization).build().output(out);
    }

    /**
     * Create an iterator over parsing of triples
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the triples
     */
    public static Iterator<Triple> createIteratorTriples(InputStream input, Lang lang, String baseIRI) {
        // Special case N-Triples, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NTRIPLES, lang) )
            return new IteratorResourceClosing<>(RiotParsers.createIteratorNTriples(input, null), input);
        // Otherwise, we have to spin up a thread to deal with it
        PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
        PipedTriplesStream out = new PipedTriplesStream(it);
        Thread t = new Thread(()->parseFromInputStream(out, input, baseIRI, lang));
        t.start();
        return it;
    }

    /**
     * Creates an iterator over parsing of quads
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the quads
     */
    public static Iterator<Quad> createIteratorQuads(InputStream input, Lang lang, String baseIRI) {
        // Special case N-Quads, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NQUADS, lang) ) {
            return new IteratorResourceClosing<>(
                RiotParsers.createIteratorNQuads(input, null, RiotLib.dftProfile()),
                input);
        }
        // Otherwise, we have to spin up a thread to deal with it
        final PipedRDFIterator<Quad> it = new PipedRDFIterator<>();
        final PipedQuadsStream out = new PipedQuadsStream(it);

        Thread t = new Thread(()->parseFromInputStream(out, input, baseIRI, lang));
        t.start();
        return it;
    }
}
