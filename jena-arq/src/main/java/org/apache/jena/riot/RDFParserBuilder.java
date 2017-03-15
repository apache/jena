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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * An {@link RDFParser} is a process that will generate triples; 
 * {@link RDFParserBuilder} provides the means to setup the parser.
 * <p>
 * An {@link RDFParser} has a predefined source; the target for output is given when the "parse" method is called. 
 * It can be used multiple times in which case the same source is reread. The destination can vary.
 * The application is responsible for concurrency of the destination of the parse operation.
 * 
 * The process is
 * <pre>
 *     StreamRDF destination = ...
 *     RDFParser parser = RDFParser.create()
 *          .source("filename.ttl")
 *          .build();
 *     parser.parse(destination); 
 * </pre>
 * or using a short cut: 
 * <pre>
 *     RDFParser parser = RDFParser.create()
 *          .source("filename.ttl")
 *          .parse(destination); 
 * </pre> 
 */
public class RDFParserBuilder {
    // Source
    private String uri = null;
    private Path path = null;
    private InputStream inputStream;
    // StringReader - charset problems with any other kind.
    private Reader javaReader = null;

    // HTTP
    private Map<String, String> httpHeaders = new HashMap<>(); 
    private HttpClient httpClient = null;

    // Syntax
    private Lang hintLang = null;
    private Lang forceLang = null;
    
    private String baseUri = null;
    
    // ---- Unused but left in case required in the future.
    private boolean strict = SysRIOT.isStrictMode();
    private boolean resolveURIs = true;
    private IRIResolver resolver = null;
    // ----
    
    // Construction for the StreamRDF 
    private FactoryRDF factory = null;
    private LabelToNode labelToNode = null;
    
    // Bad news.
    private ErrorHandler errorHandler = null;
    
    // Parsing process
    private Context context = null;
    
    public static RDFParserBuilder create() { return new RDFParserBuilder() ; }
    private RDFParserBuilder() {}
    
    /** 
     *  Set the source to {@link Path}. 
     *  This clears any other source setting.
     *  @param path
     *  @return this
     */
    public RDFParserBuilder source(Path path) {
        clearSource();
        this.path = path;
        return this;
    }

    /** 
     *  Set the source to a URI; this includes OS file names.
     *  File URL shoudl be of the form {@code file:///...}. 
     *  This clears any other source setting.
     *  @param uri
     *  @return this
     */
    public RDFParserBuilder source(String uri) {
        clearSource();
        this.uri = uri;
        return this;
    }

    /** 
     *  Set the source to {@link InputStream}. 
     *  This clears any other source setting.
     *  The {@link InputStream} will be closed when the 
     *  parser is called and the parser can not be reused.  
     *  @param input
     *  @return this
     */
    public RDFParserBuilder source(InputStream input) {
        clearSource();
        this.inputStream = input;
        return this;
    }

    /** 
     *  Set the source to {@link StringReader}. 
     *  This clears any other source setting.
     *  The {@link StringReader} will be closed when the 
     *  parser is called and the parser can not be reused.  
     *  @param reader
     *  @return this
     */
    public RDFParserBuilder source(StringReader reader) {
        clearSource();
        this.javaReader = reader;
        return this;
    }

    /** 
     *  Set the source to {@link StringReader}. 
     *  This clears any other source setting.
     *  The {@link StringReader} will be closed when the 
     *  parser is called and the parser can not be reused.  
     *  @param reader
     *  @return this
     *  @deprecated   Use an InputStream or a StringReader. 
     */
    @Deprecated
    public RDFParserBuilder source(Reader reader) {
        clearSource();
        this.javaReader = reader;
        return this;
    }

    private void clearSource() {
        this.uri = null;
        this.inputStream = null;
        this.path = null;
        this.javaReader = null;
    }

    /**
     * Set the hint {@link Lang}. This is the RDF syntax used when there is no way to
     * deduce the syntax (e.g. read from a InputStream, no recognized file extension, no
     * recognized HTTP Content-Type provided).
     * 
     * @param lang
     * @return this
     */
    public RDFParserBuilder lang(Lang lang) { this.hintLang = lang ; return this; }

    /**
     * Force the choice RDF syntax to be {@code lang}, and ignore any indications such as file extension
     * or HTTP Content-Type.
     * @see Lang
     * @param lang
     * @return this
     */
    public RDFParserBuilder forceLang(Lang lang) { this.forceLang = lang ; return this; }
    
    /**
     * Set the HTTP "Accept" header.
     * The default if not set is {@link WebContent#defaultRDFAcceptHeader}.
     * @param acceptHeader
     * @return this
     */
    public RDFParserBuilder httpAccept(String acceptHeader) { 
        httpHeader(HttpNames.hAccept, acceptHeader);
        return this; 
    }

    /**
     * Set an HTTP header. Any previous setting is lost.
     * <p> 
     * Consider setting up an {@link HttpClient} if more complicated
     * setting to an HTTP request is required.
     */
    public RDFParserBuilder httpHeader(String header, String value) {
        httpHeaders.put(header, value);
        return this;
    }
    
    /** Set the HttpClient to use.
     *  This will override any HTTP header settings set for this builder.
     */
    public RDFParserBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /** Set the base URI for parsing.  The default is to have no base URI. */ 
    public RDFParserBuilder base(String base) { this.baseUri = base ; return this; }

    /** Choose whether to resolve URIs.<br/>
     *  This does not affect all langages: N-Triples and N-Quads never resolve URIs.<br/>
     *  Relative URIs are bad data.<br/>
     *  Only set this to false for debugging and development purposes. 
     */ 
    public RDFParserBuilder resolveURIs(boolean flag) { this.resolveURIs = flag ; return this; }

    /**
     * Set the {@link ErrorHandler} to use.
     * This replaces any previous setting.
     * The default is use slf4j logger "RIOT".   
     * @param handler
     * @return this
     */
    public RDFParserBuilder errorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }
    
    /**
     * Set the {@link FactoryRDF} to use. {@link FactoryRDF} control how parser output is
     * turned into {@code Node} and how {@code Triple}s and {@code Quad}s are built. This
     * replaces any previous setting. 
     * <br/>
     * The default is use {@link RiotLib#factoryRDF()} which is provides {@code Node}
     * reuse. 
     * <br/>
     * The {@code FactoryRDF} also determines how blank node labels in RDF syntax are
     * mapped to {@link BlankNodeId}. Use
     * <pre>
     *    new Factory(myLabelToNode) 
     * </pre>
     * to create an {@code FactoryRDF} and set the {@code LabelToNode} step.
     * @see #labelToNode
     * @param factory
     * @return this
     */
    public RDFParserBuilder factory(FactoryRDF factory) {
        this.factory = factory;
        return this;
    }
    
    /**
     * Use the given {@link LabelToNode}, the policy for converting blank node labels in
     * RDF syntax to Jena's {@code Node} objects (usually a blank node).
     * <br/>
     * Only applies when the {@link FactoryRDF} is not set in the
     * {@code RDFParserBuilder}, otherwise the {@link FactoryRDF} controls the
     * label-to-node process.
     * <br/>
     * {@link SyntaxLabels#createLabelToNode} is the default policy.
     * <br>
     * {@link LabelToNode#createUseLabelAsGiven()} uses the label in the RDF syntax directly. 
     * This does not produce safe RDF and should only be used for development and debugging.   
     * @see #factory
     * @param labelToNode
     * @return this
     */
    public RDFParserBuilder labelToNode(LabelToNode labelToNode) {
        this.labelToNode = labelToNode;
        return this;
    }
    
    // There are no strict/unstrict differences. 
//    /**
//     * Set "strict" mode.
//     * @param strictMode
//     * @return this
//     */
//    public RDFParserBuilder strict(boolean strictMode) {
//        this.strict = strictMode;
//        return this;
//    }
    
    public RDFParserBuilder context(Context context) {
        if ( context != null )
            context = context.copy();
        this.context = context;
        return this;
    }
    
    // ---- Terminals
    // "parse" are short cuts for {@code build().parse(...)}.
    
    /** 
     * Parse the source, sending the results to a {@link StreamRDF}.
     * Short form for {@code build().parse(stream)}.
     * @param stream
     */
    public void parse(StreamRDF stream) {
        build().parse(stream);
    }

    /**
     * Parse the source, sending the results to a {@link Graph}. The source must be for
     * triples; any quads are discarded. 
     * Short form for {@code build().parse(stream)}
     * where {@code stream} sends tripes and prfixes to the {@code Graph}.
     * 
     * @param graph
     */
    public void parse(Graph graph) {
        parse(StreamRDFLib.graph(graph));
    }

    /**
     * Parse the source, sending the results to a {@link DatasetGraph}.
     * Short form for {@code build().parse(stream)}
     * where {@code stream} sends tripes and prefixes to the {@code DatasetGraph}.
     * 
     * @param dataset
     */
    public void parse(DatasetGraph dataset) {
        parse(StreamRDFLib.dataset(dataset));
    }

    /** Build an {@link RDFParser}. The parser takes it's configuration from this builder and can not then be changed.
     * The source must be set.
     * When a parser is used, it is takes the source and sends output to an {@link StreamRDF}.
     * <p>  
     * Shortcuts:
     * <ul>
     * <li>{@link #parse(DatasetGraph)} - parse the source and output to a {@code DatasetGraph}
     * <li>{@link #parse(Graph)} - parse the source and output to a {@code Graph}
     * <li>{@link #parse(StreamRDF)} - parse the source and output to a {@code StreamRDF}
     * </ul>
     * 
     * @return RDFParser
     */
    public RDFParser build() {
        // Build what we can now - something have to be built in the parser.
        
        if ( uri == null && path == null && inputStream == null && javaReader == null )
            throw new RiotException("No source specified");
        
        // Setup the HTTP client.
        HttpClient client = buildHttpClient();
        FactoryRDF factory$ = buildFactoryRDF();
        ErrorHandler errorHandler$ = errorHandler;
        if ( errorHandler$ == null )
            errorHandler$ = ErrorHandlerFactory.getDefaultErrorHandler();

        if ( path != null && baseUri == null )
            baseUri = IRILib.filenameToIRI(path.toString());
        if ( path == null && baseUri == null && uri != null )
            baseUri = uri;
        
        // Can't build the maker here as it is Lang/conneg dependent.
        return new RDFParser(uri, path, inputStream, javaReader, client,
                             hintLang, forceLang,
                             baseUri, strict, resolveURIs,
                             resolver, factory$, errorHandler$, context);
    }

    private FactoryRDF buildFactoryRDF() {
        FactoryRDF factory$ = factory;
        if ( factory$ == null ) { 
            if ( labelToNode != null )
                factory$ = RiotLib.factoryRDF(labelToNode);
            else
                factory$ = RiotLib.factoryRDF();
        }
        return factory$;
    }

    private HttpClient buildHttpClient() {
        if ( httpClient != null )
            return httpClient;
        if ( httpHeaders.isEmpty() )
            // System default.
            // For complete compatibility, we have to let null pass through.
            return null; // HttpOp.getDefaultHttpClient();
        List<Header> hdrs = new ArrayList<>();
        httpHeaders.forEach((k,v)->{
            Header header = new BasicHeader(k, v);
            hdrs.add(header);
        });
        HttpClient hc = CachingHttpClientBuilder.create().setDefaultHeaders(hdrs).build();
        return hc;
    }

    /**
     * Duplicate this builder with current settings.
     * Changes to setting to this builder do not affect the clone. 
     */
    @Override
    public RDFParserBuilder clone() { 
        RDFParserBuilder builder = new RDFParserBuilder();
        builder.uri =               this.uri;
        builder.path =              this.path;
        builder.inputStream =       this.inputStream;
        builder.javaReader =        this.javaReader;
        builder.httpHeaders =       new HashMap<>(this.httpHeaders);
        builder.httpClient =        this.httpClient;
        builder.hintLang =          this.hintLang;
        builder.forceLang =         this.forceLang;
        builder.baseUri =           this.baseUri;
        builder.strict =            this.strict;
        builder.resolveURIs =       this.resolveURIs;
        builder.resolver =          this.resolver;
        builder.factory =           this.factory;
        builder.labelToNode =       this.labelToNode;
        builder.errorHandler =      this.errorHandler;
        builder.context =           this.context;
        return builder;
    }
}
