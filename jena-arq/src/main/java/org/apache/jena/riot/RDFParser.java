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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
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
 * or using an abbreviated form:
 * <pre>
 * RDFParser.create().source("filename.ttl").parse(destination);
 * </pre>
 * The {@code destination} {@link StreamRDF} and can be given as a
 * {@link Graph} or {@link DatasetGraph} as well. 
 */

public class RDFParser {
    private final String       uri;
    private final Path         path;
    private final InputStream  inputStream;
    private final Reader       javaReader;
    private final HttpClient   httpClient;
    private final Lang         hintLang;
    private final Lang         forceLang;
    private final String       baseUri;
    private final boolean      strict;
    private final boolean      resolveURIs;
    private final IRIResolver  resolver;
    private final FactoryRDF   factory;
    private final ErrorHandler errorHandler;
    private final Context      context;

    private boolean            canUse = true;

    public static RDFParserBuilder create() {
        return RDFParserBuilder.create();
    }

    /* package */ RDFParser(String uri, Path path, InputStream inputStream, Reader javaReader, HttpClient httpClient, Lang hintLang,
                            Lang forceLang, String baseUri, boolean strict, boolean resolveURIs, IRIResolver resolver, FactoryRDF factory,
                            ErrorHandler errorHandler, Context context) {
        int x = countNonNull(uri, path, inputStream, javaReader);
        if ( x >= 2 )
            throw new IllegalArgumentException("Only one source allowed: At most one of uri, path, inputStream and javaReader can be set");
        Objects.requireNonNull(factory);
        Objects.requireNonNull(errorHandler);
        
        this.uri = uri;
        this.path = path;
        this.inputStream = inputStream;
        this.javaReader = javaReader;
        this.httpClient = httpClient;
        this.hintLang = hintLang;
        this.forceLang = forceLang;
        this.baseUri = baseUri;
        this.strict = strict;
        this.resolveURIs = resolveURIs;
        this.resolver = resolver;
        this.factory = factory;
        this.errorHandler = errorHandler;
        this.context = context;
    }

    private int countNonNull(Object... objs) {
        int x = 0;
        for ( Object obj : objs )
            if ( obj != null )
                x++;
        return x;
    }

    public void parse(StreamRDF destination) {
        if ( !canUse )
            throw new RiotException("Parser has been used once and can not be used again");
        // Consuming mode.
        canUse = (inputStream == null && javaReader == null);
        // XXX FactoryRDF is stateful in the LabelToNode mapping.
        // NB FactoryRDFCaching does not reset it's cache.
        // factory.reset() ;

        if ( inputStream != null || javaReader != null ) {
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
                // Conneg and hint
                ct = WebContent.determineCT(input.getContentType(), hintLang, baseUri);
                if ( ct == null )
                    throw new RiotException("Failed to determine the content type: (URI=" + baseUri + " : stream=" + input.getContentType()+")");
                reader = createReader(ct);
                if ( reader == null )
                    throw new RiotException("No parser registered for content type: " + ct.getContentType());
            }
            read(reader, input, null, ct, destination);
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
            throw new RiotException("Failed to determine the RDF syntax");
    
        ReaderRIOT readerRiot = createReader(ct);
        if ( readerRiot == null )
            throw new RiotException("No parser registered for content type: " + ct.getContentType());
        read(readerRiot, inputStream, javaReader, ct, destination);
    }
    
    /** Call the reader, from either an InputStream or a Reader */
    private void read(ReaderRIOT readerRiot, InputStream inputStream, Reader javaReader, ContentType ct, StreamRDF destination) {
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
        if ( urlStr.startsWith("http://") || urlStr.startsWith("https://") ) {
            // For complete compatibility, we have to let null pass through.
            // Pair with RDFParserBuilder.buildHttpClient
            //   Objects.requireNonNull(httpClient);
            // Remap.
            urlStr = StreamManager.get(context).mapURI(urlStr);
            in = HttpOp.execHttpGet(urlStr, null, httpClient, null);
        } else { 
            // StreamManager and Locators, based on urlStr.
            StreamManager sMgr = StreamManager.get(context);
            in = sMgr.open(urlStr);
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
        MakerRDF maker = makeMaker(lang);
        ReaderRIOT reader = r.create(lang, (MakerRDFStd)maker);
        return reader ;
    }

    private MakerRDF makeMaker(Lang lang) {
        boolean resolve = resolveURIs;
        boolean checking = true;
        
        // Per language tweaks.
        if ( sameLang(NTRIPLES, lang) || sameLang(NQUADS, lang) ) {
            checking = SysRIOT.isStrictMode();
            resolve = false;
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

        MakerRDFStd parserFactory = new MakerRDFStd(factory, errorHandler, resolver, prefixMap, context, checking, strict);
        return parserFactory;
    }
}
