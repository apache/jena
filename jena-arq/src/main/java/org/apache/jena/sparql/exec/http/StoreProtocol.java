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

package org.apache.jena.sparql.exec.http;

import java.io.FileNotFoundException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.http.Push;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.NotFoundException;

/**
 * State and settings management for {@link GSP Graph Store Protocol}
 * and {@link DSP Dataset Store Protocol} clients.
 */
public abstract class StoreProtocol<X extends StoreProtocol<X>> {
    protected String              serviceEndpoint = null;
    // Need to keep this separately from contentType because it affects the choice of writer.
    protected RDFFormat           rdfFormat       = null;
    protected HttpClient          httpClient      = null;
    protected Map<String, String> httpHeaders     = new HashMap<>();

    protected StoreProtocol() {}

    protected abstract X thisBuilder();

    /**
     * Set the URL of the query endpoint.
     */
    public X endpoint(String serviceURL) {
        this.serviceEndpoint = Objects.requireNonNull(serviceURL);
        return thisBuilder();
    }

    protected String endpoint() {
        return serviceEndpoint;
    }

    public X httpClient(HttpClient httpClient) {
        Objects.requireNonNull(httpClient, "HttpClient");
        this.httpClient = httpClient;
        return thisBuilder();
    }

    /**
     * Set an HTTP header that is added to the request.
     * See {@link #accept}, {@link #acceptHeader} and {@link #contentType(RDFFormat)}.
     * for specific handling of {@code Accept:} and {@code Content-Type}.
     */
    public X httpHeader(String headerName, String headerValue) {
        Objects.requireNonNull(headerName);
        Objects.requireNonNull(headerValue);
        if ( httpHeaders == null )
            httpHeaders = new HashMap<>();
        httpHeaders.put(headerName, headerValue);
        return thisBuilder();
    }

    // Protected - no public getters.
    protected String httpHeader(String header) {
        Objects.requireNonNull(header);
        if ( httpHeaders == null )
            return null;
        return httpHeaders.get(header);
    }

    /** Set the accept header on GET requests. Optional; if not set, a system default is used. */
    public X acceptHeader(String acceptHeader) {
        httpHeader(HttpNames.hAccept, acceptHeader);
        return thisBuilder();
    }

    // No getters.
    protected String acceptHeader() {
        return httpHeader(HttpNames.hAccept);
    }

    /** Set the accept header on GET requests. Optional; if not set, a system default is used. */
    public X accept(Lang lang) {
        String acceptHeader = (lang != null ) ? lang.getContentType().getContentTypeStr() : null;
        httpHeader(HttpNames.hAccept, acceptHeader);
        return thisBuilder();
    }

    /**
     * Set the Content-type for a POST, PUT request of a file
     * or serialization of a graph of dataset is necessary.
     * Optional; if not set, the file extension is used or the
     * system default RDF syntax encoding.
     */
    public X contentTypeHeader(String contentType) {
        httpHeader(HttpNames.hContentType, contentType);
        return thisBuilder();
    }

    // No getters.
    protected String contentType() {
        return httpHeader(HttpNames.hContentType);
    }

    /**
     * Set the Content-type for a POST, PUT request of a file
     * or serialization of a graph of dataset is necessary.
     * Optional; if not set, the file extension is used or the
     * system default RDF syntax encoding.
     */
    public X contentType(RDFFormat rdfFormat) {
        this.rdfFormat = rdfFormat;
        String contentType = rdfFormat.getLang().getContentType().getContentTypeStr();
        httpHeader(HttpNames.hContentType, contentType);
        return thisBuilder();
    }

    protected RDFFormat rdfFormat() { return rdfFormat; }

    /**
     * Copy the state (endpoint, HttpClient, HTTP headers, RDFFormat) of one StoreProtocol into this one.
     * Any old setup on this object is lost.
     */
    public X copySetup(StoreProtocol<?> other) {
        clearSetup();
        other.httpHeaders(this::httpHeader);
        if ( other.endpoint() != null )
            this.endpoint(other.endpoint());
        if (other.httpClient() != null )
            this.httpClient(other.httpClient());
        if ( other.rdfFormat() != null )
            this.rdfFormat(other.rdfFormat());
        return thisBuilder();
    }

    private void clearSetup() {
        serviceEndpoint = null;
        rdfFormat       = null;
        httpClient      = null;
        httpHeaders     = new HashMap<>();
    }

    /**
     * Choose the HttpClient to use.
     * The requestURL includes the query string (for graph GSP operations).
     * If explicit set with {@link #httpClient(HttpClient)}, use that;
     * other use the system registry and default {@code HttpClient} settings
     * in {@link HttpEnv}.
     */
    protected HttpClient requestHttpClient(String serviceURL, String requestURL) {
        if ( httpClient != null )
            return httpClient;
        return HttpEnv.getHttpClient(serviceURL, httpClient);
    }

    // Setup problems.
    protected static RuntimeException exception(String msg) {
        return new HttpException(msg);
    }

    final protected String service() { return serviceEndpoint; }
    final protected HttpClient httpClient() { return httpClient; }
    final protected void httpHeaders(BiConsumer<String, String> action) { httpHeaders.forEach(action); }

    protected void ensureAcceptHeader(String dftAcceptheader) {
        String requestAccept = header(acceptHeader(), WebContent.defaultRDFAcceptHeader);
        acceptHeader(requestAccept);
    }

//    /**
//     * POST the contents of a file using the filename extension to determine the
//     * Content-Type to use if not already set.
//     * <p>
//     * This operation does not parse the file.
//     */
//    public void POST(String file) {
//        String fileExtContentType = contentTypeFromFilename(file);
//        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
//        uploadQuads(hc, serviceEndpoint, file, fileExtContentType, httpHeaders, Push.POST);
//    }
//
//    /** POST a dataset */
//    public void POST(DatasetGraph dataset) {
//        RDFFormat requestFmt = rdfFormat(HttpEnv.defaultQuadsFormat);
//        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
//        HttpRDF.httpPostDataset(hc, serviceEndpoint, dataset, requestFmt, httpHeaders);
//    }
//
//    /**
//     * PUT the contents of a file using the filename extension to determine the
//     * Content-Type to use if not already set.
//     * <p>
//     * This operation does not parse the file.
//     */
//    public void PUT(String file) {
//        String fileExtContentType = contentTypeFromFilename(file);
//        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
//        uploadQuads(hc, serviceEndpoint, file, fileExtContentType, httpHeaders, Push.PUT);
//    }
//
//    /** PUT a dataset */
//    public void PUT(DatasetGraph dataset) {
//        RDFFormat requestFmt = rdfFormat(HttpEnv.defaultQuadsFormat);
//        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
//        HttpRDF.httpPutDataset(hc, serviceEndpoint, dataset, requestFmt, httpHeaders);
//    }
//
////    /** Send a file of triples to a URL. */
////    private static void uploadTriples(HttpClient httpClient, String gspUrl, String file, String fileExtContentType,
////                                      Map<String, String> headers, Push mode) {
////        Lang lang = RDFLanguages.contentTypeToLang(fileExtContentType);
////        if ( lang == null )
////            throw new ARQException("Not a recognized as an RDF format: "+fileExtContentType);
////        if ( RDFLanguages.isQuads(lang) && ! RDFLanguages.isTriples(lang) )
////            throw new ARQException("Can't load quads into a graph");
////        if ( ! RDFLanguages.isTriples(lang) )
////            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")");
////        pushFile(httpClient, gspUrl, file, fileExtContentType, headers, mode);
////    }
//
//    /**
//     * Send a file of quads to a URL. The Content-Type is inferred from the file
//     * extension.
//     */
//    private static void uploadQuads(HttpClient httpClient, String endpoint, String file, String fileExtContentType, Map<String, String> headers, Push mode) {
//        Lang lang = RDFLanguages.contentTypeToLang(fileExtContentType);
//        if ( !RDFLanguages.isQuads(lang) && !RDFLanguages.isTriples(lang) )
//            throw new ARQException("Not an RDF format: " + file + " (lang=" + lang + ")");
//        pushFile(httpClient, endpoint, file, fileExtContentType, headers, mode);
//    }

    /** Header string or default value. */
    private static String header(String choice, String dftString) {
        return choice != null ? choice : dftString;
    }

    /** Choose the format to write in.
     * <ol>
     * <li> {@code rdfFormat}
     * <li> {@code contentType} setting, choosing streaming
     * <li> {@code contentType} setting, choosing pretty
     * <li> HttpEnv.dftTriplesFormat / HttpEnv.dftQuadsFormat /
     * </ol>
     */
    protected RDFFormat rdfFormat(RDFFormat dftFormat) {
        if ( rdfFormat != null )
            return rdfFormat;

        if ( contentType() == null )
            return dftFormat;

        Lang lang = RDFLanguages.contentTypeToLang(contentType());
        RDFFormat streamFormat = StreamRDFWriter.defaultSerialization(null);
        if ( streamFormat != null )
            return streamFormat;
        return RDFWriterRegistry.defaultSerialization(lang);
    }

    /** Choose the Content-Type header for sending a file unless overridden. */
    protected String contentTypeFromFilename(String filename) {
        String ctx = contentType();
        if ( ctx != null )
            return ctx;
        ContentType ct = RDFLanguages.guessContentType(filename);
        return ct == null ? null : ct.getContentTypeStr();
    }

    /** Send a file. fileContentType takes precedence over this.contentType.*/
    protected static void pushFile(HttpClient httpClient, String endpoint, String file, String fileContentType,
                                   Map<String, String> httpHeaders, Push style) {
        try {
            Path path = Path.of(file);
            if ( fileContentType != null )
            //if ( ! httpHeaders.containsKey(HttpNames.hContentType) )
                httpHeaders.put(HttpNames.hContentType, fileContentType);
            BodyPublisher body = BodyPublishers.ofFile(path);
            HttpLib.httpPushData(httpClient, style, endpoint, HttpLib.setHeaders(httpHeaders), body);
        } catch (FileNotFoundException ex) {
            throw new NotFoundException(file);
        }
    }

}
