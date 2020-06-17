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

import static org.apache.jena.riot.RDFLanguages.NQUADS;
import static org.apache.jena.riot.RDFLanguages.NTRIPLES;
import static org.apache.jena.riot.RDFLanguages.RDFJSON;
import static org.apache.jena.riot.RDFLanguages.sameLang;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.process.normalize.StreamCanonicalLangTag;
import org.apache.jena.riot.process.normalize.StreamCanonicalLiterals;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * An {@link RDFParser} is a process that will generate triples; {@link RDFParserBuilder}
 * provides the means to setup the parser.
 * <p>
 * An {@link RDFParser} has a predefined source; the target for output is given when the
 * "parse" method is called. It can be used multiple times in which case the same source
 * is reread. The destination can vary. The application is responsible for concurrency of
 * the destination of the parse operation.
 * 
 * The process is
 * 
 * <pre>
 *    StreamRDF destination = ...
 *    RDFParser parser = RDFParser.create().source("filename.ttl").build();
 *    parser.parse(destination);
 * </pre>
 * or using abbreviated forms:
 * <pre>
 * RDFParser.source("filename.ttl").parse(destination);
 * </pre>
 * The {@code destination} {@link StreamRDF} and can be given as a
 * {@link Graph} or {@link DatasetGraph} as well.
 * 
 * @see ReaderRIOT The interface to the syntax parsing process for each RDF syntax. 
 */

public class RDFParser {
    /*package*/ enum LangTagForm { NONE, LOWER_CASE, CANONICAL }  

    private final String            uri;
    private final Path              path;
    private final String            content;
    private final InputStream       inputStream;
    private final Reader            javaReader;
    private final StreamManager     streamManager;

    private final HttpClient        httpClient;
    private final Lang              hintLang;
    private final Lang              forceLang;
    private final String            baseUri;
    private final boolean           strict;
    private final boolean           resolveURIs;
    private final boolean           canonicalLexicalValues;
    private final LangTagForm       langTagForm;
    private final Optional<Boolean> checking;
    private final IRIResolver       resolver;
    private final FactoryRDF        factory;
    private final ErrorHandler      errorHandler;
    private final Context           context;

    // Some cases the parser is reusable (read a file), some are not (input streams).
    private boolean                 canUseThisParser = true;

    // ---- Builder creation
    
    /** Create an {@link RDFParserBuilder}.
     * <p>
     * Often used in a pattern such as:
     * <pre>
     *    RDFParser.create()
     *        .source("data.ttl")
     *        .parse(graph);
     * </pre>
     * 
     */
    public static RDFParserBuilder create() {
        return RDFParserBuilder.create();
    }

    /**
     * Create an {@link RDFParserBuilder} and set the source to the
     * {@link Path}.
     * <p>
     * This is a shortcut for {@code RDFParser.create().source(path)}.
     * 
     * @param path
     * @return this
     */
    public static RDFParserBuilder source(Path path) {
        return RDFParserBuilder.create().source(path);
    }

    /**
     * Create an {@link RDFParserBuilder} and set the source to the URI, which
     * can be a filename.
     * <p>
     * This is a shortcut for {@code RDFParser.create().source(uriOrFile)}.
     * 
     * @param uriOrFile
     * @return this
     */
    
    public static RDFParserBuilder source(String uriOrFile) {
        return RDFParserBuilder.create().source(uriOrFile);
    }

    /**
     * Create an {@link RDFParserBuilder} and set content to parse to be the
     * given string. The syntax must be set with {@code .lang(...)}.
     * <p>
     * Shortcut for {@code RDFParser.create.fromString(string)}.
     * @param string
     * @return this
     */
    public static RDFParserBuilder fromString(String string) {
        return RDFParserBuilder.create().fromString(string);
    }

    /** 
     * Create an {@link RDFParserBuilder} and set the source to {@link InputStream}.
     *  The {@link InputStream} will be closed when the 
     *  parser is called and the parser can not be reused. 
     *  The syntax must be set with {@code .lang(...)}.
     *  <p>
     *  This is a shortcut for {@code RDFParser.create().source(input)}.
     *  @param input
     *  @return this 
     */
    public static RDFParserBuilder source(InputStream input) {
        return RDFParserBuilder.create().source(input);
    }
    
    /* package */ RDFParser(String uri, Path path, String content, InputStream inputStream, Reader javaReader, StreamManager streamManager, 
                            HttpClient httpClient, Lang hintLang, Lang forceLang, String baseUri, boolean strict, Optional<Boolean> checking, 
                            boolean canonicalLexicalValues, LangTagForm langTagForm,  
                            boolean resolveURIs, IRIResolver resolver, FactoryRDF factory,
                            ErrorHandler errorHandler, Context context) {
        int x = countNonNull(uri, path, content, inputStream, javaReader);
        if ( x >= 2 )
            throw new IllegalArgumentException("Only one source allowed: one of uri, path, content, inputStream and javaReader must be set");
        if ( x < 1 )
            throw new IllegalArgumentException("No source specified allowed: one of uri, path, content, inputStream and javaReader must be set");
        Objects.requireNonNull(factory);
        Objects.requireNonNull(errorHandler);
        Objects.requireNonNull(checking);
        
        this.uri = uri;
        this.path = path;
        this.content = content;
        this.inputStream = inputStream;
        this.javaReader = javaReader;
        this.streamManager = streamManager;
        this.httpClient = httpClient;
        this.hintLang = hintLang;
        this.forceLang = forceLang;
        this.baseUri = baseUri;
        this.strict = strict;
        this.resolveURIs = resolveURIs;
        this.canonicalLexicalValues = canonicalLexicalValues;
        this.langTagForm = langTagForm;
        this.checking = checking;
        this.resolver = resolver;
        this.factory = factory;
        this.errorHandler = errorHandler;
        this.context = context;
    }

    /** Count the nulls */
    private int countNonNull(Object... objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj != null )
                x++;
        return x;
    }

    /** One or more non-null */
    private boolean isNonNull(Object... objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj != null )
                return true;
        return false;
    }

    /** All null */
    private boolean allNull(Object... objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj != null )
                return false;
        return true;
    }
    
    /**
     * Parse the source, sending the results to a {@link Graph}. The source must be for
     * triples; any quads are discarded. 
     */
    public void parse(Graph graph) {
        parse(StreamRDFLib.graph(graph));
    }
    
    /**
     * Parse the source, sending the results to a {@link Model}.
     * The source must be for triples; any quads are discarded.
     * This method is equivalent to {@code parse(model.getGraph())}. 
     */
    public void parse(Model model) {
        parse(model.getGraph());
    }
    
    /**
     * Parse the source, sending the results to a {@link DatasetGraph}.
     */
    public void parse(DatasetGraph dataset) {
        parse(StreamRDFLib.dataset(dataset));
    }
    
    /**
     * Parse the source, sending the results to a {@link Dataset}.
     * This method is equivalent to {@code parse(dataset.asDatasetGraph())}. 
     */
    public void parse(Dataset dataset) {
        parse(dataset.asDatasetGraph());
    }
    
    /** 
     * Parse the source, sending the results to a {@link StreamRDF}.
     */
    public void parse(StreamRDF destination) {
        if ( !canUseThisParser )
            throw new RiotException("Parser has been used once and can not be used again");
        // Consuming mode.
        canUseThisParser = (inputStream == null && javaReader == null);
        // FactoryRDF is stateful in the LabelToNode mapping.
        // NB FactoryRDFCaching does not need to reset its cache.
        factory.reset() ;
        
        if ( canonicalLexicalValues )
            destination = new StreamCanonicalLiterals(destination);
        switch(langTagForm) {
            case NONE :
                break;
            case CANONICAL :
                destination = StreamCanonicalLangTag.toCanonical(destination);
                break;
            case LOWER_CASE :
                destination = StreamCanonicalLangTag.toLC(destination);
                break;
            default : throw new InternalErrorException("langTagForm = "+langTagForm);
        }

        if ( isNonNull(content, inputStream, javaReader) ) {
            parseNotUri(destination);
            return;
        }
        Objects.requireNonNull(baseUri);
        parseURI(destination);
    }

    /** Parse when there is a URI to guide the choice of syntax */
    private void parseURI(StreamRDF destination) {
        // Source by uri or path.
        try (TypedInputStream input = openTypedInputStream(uri, path)) {
            ReaderRIOT reader;
            ContentType ct;
            if ( forceLang != null ) {
                @SuppressWarnings("deprecation")
                ReaderRIOTFactory r = RDFParserRegistry.getFactory(forceLang);
                if ( r == null )
                    throw new RiotException("No parser registered for language: " + forceLang);
                ct = forceLang.getContentType();
                reader = createReader(r, forceLang);
            } else {
                // No forced language.
                // Conneg and hint, ignoring text/plain.
                ct = WebContent.determineCT(input.getContentType(), hintLang, baseUri);
                if ( ct == null )
                    throw new RiotException("Failed to determine the content type: (URI=" + baseUri + " : stream=" + input.getContentType()+")");
                reader = createReader(ct);
                if ( reader == null )
                    throw new RiotException("No parser registered for content type: " + ct.getContentTypeStr());
            }
            read(reader, input, null, baseUri, context, ct, destination);
        }
    }

    /** Parse when there is no URI to guide the choice of syntax */
    private void parseNotUri(StreamRDF destination) {
        // parse from bytes or chars, no indication of the syntax from the source.
        Lang lang = hintLang;
        if ( forceLang != null )
            lang = forceLang;
        ContentType ct = WebContent.determineCT(null, lang, baseUri);
        if ( ct == null )
            throw new RiotException("Failed to determine the RDF syntax (.lang or .base required)");
    
        ReaderRIOT readerRiot = createReader(ct);
        if ( readerRiot == null )
            throw new RiotException("No parser registered for content type: " + ct.getContentTypeStr());
        Reader jr = javaReader;
        if ( content != null )
            jr = new StringReader(content);
        
        read(readerRiot, inputStream, jr, baseUri, context, ct, destination);
    }
    
    /** Call the reader, from either an InputStream or a Reader */
    private static void read(ReaderRIOT readerRiot, InputStream inputStream, Reader javaReader,
                             String baseUri, Context context,
                             ContentType ct, StreamRDF destination) {
        if ( inputStream != null && javaReader != null )
            throw new InternalErrorException("Both inputStream and javaReader are non-null"); 
        if ( inputStream != null ) {
            readerRiot.read(inputStream, baseUri, ct, destination, context);
            return;
        }
        if ( javaReader != null ) {
            readerRiot.read(javaReader, baseUri, ct, destination, context);
            return;
        }
        throw new InternalErrorException("Both inputStream and javaReader are null");
    }

    @SuppressWarnings("resource")
    private TypedInputStream openTypedInputStream(String urlStr, Path path) {
        // If path, use that.
        if ( path != null ) {
            try {
                InputStream in = Files.newInputStream(path);
                ContentType ct = RDFLanguages.guessContentType(urlStr) ;
                return new TypedInputStream(in, ct);
            }
            catch (NoSuchFileException | FileNotFoundException ex)
            { throw new RiotNotFoundException() ;}
            catch (IOException ex) { IO.exception(ex); }
        }
        
        TypedInputStream in;
        urlStr = streamManager.mapURI(urlStr);
        if ( urlStr.startsWith("http://") || urlStr.startsWith("https://") ) {
            // Need more control than LocatorURL provides. We could use it for the
            // httpClient == null case.
            //  
            // HttpOp.execHttpGet(,acceptHeader,) overrides the HttpClient default setting.
            // 
            // If there is an explicitly set HttpClient use that as given, and do not override
            // the accept header (i.e. pass null to arg accpetHeader in execHttpGet).
            // Else, use httpOp as setup and set the accept header.
            String acceptHeader = 
                ( httpClient == null ) ? WebContent.defaultRDFAcceptHeader : null; 
            in = HttpOp.execHttpGet(urlStr, acceptHeader, httpClient, null);
        } else { 
            in = streamManager.open(urlStr);
        }
        if ( in == null )
            throw new RiotNotFoundException("Not found: "+urlStr);
        return in ;
        
    }

    private ReaderRIOT createReader(ContentType ct) {
        Lang lang = RDFLanguages.contentTypeToLang(ct);
        if ( lang == null )
            return null;

        @SuppressWarnings("deprecation")
        ReaderRIOTFactory r = RDFParserRegistry.getFactory(lang);
        if ( r == null )
            return null;
        
        ReaderRIOT reader = createReader(r, lang);
        return reader ;
    }

    private ReaderRIOT createReader(ReaderRIOTFactory r, Lang lang) {
        ParserProfile profile = makeParserProfile(lang);
        ReaderRIOT reader = r.create(lang, profile);
        return reader ;
    }

    private ParserProfile makeParserProfile(Lang lang) {
        boolean resolve = resolveURIs;
        boolean checking$ = strict;
        
        // Per language tweaks.
        if ( sameLang(NTRIPLES, lang) || sameLang(NQUADS, lang) ) {
            if ( ! strict )
                checking$ = checking.orElseGet(()->false);
            resolve = false;
        } else {
            if ( ! strict )
                checking$ = checking.orElseGet(()->true);
        }
        if ( sameLang(RDFJSON, lang) )
            resolve = false;

        IRIResolver resolver = this.resolver;
        if ( resolver == null ) {
            resolver = resolve ? 
                IRIResolver.create(baseUri) :
                IRIResolver.createNoResolve() ;
        }
        PrefixMap prefixMap = PrefixMapFactory.createForInput();
        ParserProfileStd parserFactory = new ParserProfileStd(factory, errorHandler, resolver, prefixMap, context, checking$, strict);
        return parserFactory;
    }
}
