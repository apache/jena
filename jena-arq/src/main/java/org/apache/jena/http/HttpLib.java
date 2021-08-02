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

package org.apache.jena.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.http.sys.HttpRequestModifier;
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;

/**
 * Operations related to SPARQL HTTP requests - Query, Update and Graph Store protocols.
 * This class is not considered "API".
 */
public class HttpLib {

    private HttpLib() {}

    public static BodyHandler<Void> noBody() { return BodyHandlers.discarding(); }

    public static BodyPublisher stringBody(String str) { return BodyPublishers.ofString(str); }

    private static BodyHandler<InputStream> bodyHandlerInputStream = buildDftBodyHandlerInputStream();

    private static BodyHandler<InputStream> buildDftBodyHandlerInputStream() {
        return responseInfo -> {
            return BodySubscribers.ofInputStream();
        };
    }

    /** Read the body of a response as a string in UTF-8. */
    private static Function<HttpResponse<InputStream>, String> bodyInputStreamToString = r-> {
        try {
            InputStream in = r.body();
            String msg = IO.readWholeFileAsUTF8(in);
            return msg;
        } catch (Throwable ex) { throw new HttpException(ex); }
    };

    /**
     * Calculate basic auth header value. Use with header "Authorization" (constant
     * {@link HttpNames#hAuthorization}). Best used over https.
     */
    public static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the InputStream from an HttpResponse, handling possible compression settings.
     * The application must consume or close the {@code InputStream} (see {@link #finish(InputStream)}).
     * Closing the InputStream may close the HTTP connection.
     * Assumes the status code has been handled e.g. {@link #handleHttpStatusCode} has been called.
     */
    public static InputStream getInputStream(HttpResponse<InputStream> httpResponse) {
        String encoding = httpResponse.headers().firstValue(HttpNames.hContentEncoding).orElse("");
        InputStream responseInput = httpResponse.body();
        // Only support "Content-Encoding: <compression>" and not
        // "Content-Encoding: chunked, <compression>"
        try {
            switch (encoding) {
                case "" :
                case "identity" : // Proper name for no compression.
                    return responseInput;
                case "gzip" :
                    return new GZIPInputStream(responseInput, 2*1024);
                case "inflate" :
                    return new InflaterInputStream(responseInput);
                case "br" : // RFC7932
                default :
                    throw new UnsupportedOperationException("Not supported: Content-Encoding: " + encoding);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Deal with status code and any error message sent as a body in the response.
     * <p>
     * It is this handling 4xx/5xx error messages in the body that forces the use of
     * {@code InputStream}, not generic {@code T}. We don't know until we see the
     * status code how we are going to process the response body.
     * <p>
     * Exits normally without processing the body if the response is 200.
     * <p>
     * Throws {@link HttpException} for 3xx (redirection should have happened by
     * now), 4xx and 5xx, having consumed the body input stream.
     */
    public static void handleHttpStatusCode(HttpResponse<InputStream> response) {
        int httpStatusCode = response.statusCode();
        // There is no status message in HTTP/2.
        if ( ! inRange(httpStatusCode, 100, 599) )
            throw new HttpException("Status code out of range: "+httpStatusCode);
        else if ( inRange(httpStatusCode, 100, 199) ) {
            // Informational
        }
        else if ( inRange(httpStatusCode, 200, 299) ) {
            // Success. Continue processing.
        }
        else if ( inRange(httpStatusCode, 300, 399) ) {
            // We had follow redirects on (default client) so it's http->https,
            // or the application passed on a HttpClient with redirects off.
            // Either way, we should not continue processing.
            try {
                finish(response);
            } catch (Exception ex) {
                throw new HttpException("Error discarding body of "+httpStatusCode , ex);
            }
            throw new HttpException(httpStatusCode, HttpSC.getMessage(httpStatusCode));
        }
        else if ( inRange(httpStatusCode, 400, 499) ) {
            throw exception(response, httpStatusCode);
        }
        else if ( inRange(httpStatusCode, 500, 599) ) {
            throw exception(response, httpStatusCode);
        }
    }

    /**
     * Handle the HTTP response (see {@link #handleHttpStatusCode(HttpResponse)}) and
     * return the InputStream if a 200.
     *
     * @param httpResponse
     * @return InputStream
     */
    public static InputStream handleResponseInputStream(HttpResponse<InputStream> httpResponse) {
        handleHttpStatusCode(httpResponse);
        return getInputStream(httpResponse);
    }

    /**
     * Handle the HTTP response (see {@link #handleHttpStatusCode(HttpResponse)}) and
     * return the TypedInputStream that includes the {@code Content-Type} if a 200.
     *
     * @param httpResponse
     * @return TypedInputStream
     */
    public static TypedInputStream handleResponseTypedInputStream(HttpResponse<InputStream> httpResponse) {
        InputStream input = handleResponseInputStream(httpResponse);
        String ct = HttpLib.responseHeader(httpResponse, HttpNames.hContentType);
        return new TypedInputStream(input, ct);
    }

    /**
     * Handle the HTTP response and consume the body if a 200.
     * Otherwise, throw an {@link HttpException}.
     * @param response
     */
    public static void handleResponseNoBody(HttpResponse<InputStream> response) {
        handleHttpStatusCode(response);
        finish(response);
    }

    /**
     * Handle the HTTP response and read the body to produce a string if a 200.
     * Otherwise, throw an {@link HttpException}.
     * @param response
     * @return String
     */
    public static String handleResponseRtnString(HttpResponse<InputStream> response) {
        InputStream input = handleResponseInputStream(response);
        try {
            return IO.readWholeFileAsUTF8(input);
        } catch (RuntimeIOException e) { throw new HttpException(e); }
    }

    static HttpException exception(HttpResponse<InputStream> response, int httpStatusCode) {

        URI uri = response.request().uri();

        //long length = HttpLib.getContentLength(response);
        // Not critical path code. Read body regardless.
        InputStream in = response.body();
        String msg;
        try {
            msg = IO.readWholeFileAsUTF8(in);
            if ( msg.isBlank())
                msg = null;
        } catch (RuntimeIOException e) {
            msg = null;
        }
        return new HttpException(httpStatusCode, HttpSC.getMessage(httpStatusCode), msg);
    }

    private static long getContentLength(HttpResponse<InputStream> response) {
        Optional<String> x = response.headers().firstValue(HttpNames.hContentLength);
        if ( x.isEmpty() )
            return -1;
        try {
            return Long.parseLong(x.get());
        } catch (NumberFormatException ex) { return -1; }
    }

    /** Test x:int in [min, max] */
    private static boolean inRange(int x, int min, int max) { return min <= x && x <= max; }

    /** Finish with {@code HttpResponse<InputStream>}.
     * This read and drops any remaining bytes in the response body.
     * {@code close} may close the underlying HTTP connection.
     *  See {@link BodySubscribers#ofInputStream()}.
     */
    private static void finish(HttpResponse<InputStream> response) {
        finish(response.body());
    }

    /** Read to end of {@link InputStream}.
     *  {@code close} may close the underlying HTTP connection.
     *  See {@link BodySubscribers#ofInputStream()}.
     */
    public static void finish(InputStream input) {
        consume(input);
    }

    // This is extracted from commons-io, IOUtils.skip.
    // Changes:
    // * No exception.
    // * Always consumes to the end of stream (or stream throws IOException)
    // * Larger buffer
    private static int SKIP_BUFFER_SIZE = 8*1024;
    private static byte[] SKIP_BYTE_BUFFER = null;

    private static void consume(final InputStream input) {
        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
        }
        int bytesRead = 0; // Informational
        try {
            for(;;) {
                // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
                final long n = input.read(SKIP_BYTE_BUFFER, 0, SKIP_BUFFER_SIZE);
                if (n < 0) { // EOF
                    break;
                }
                bytesRead += n;
            }
        } catch (IOException ex) { /*ignore*/ }
    }

    /** String to {@link URI}. Throws {@link HttpException} on bad syntax or if the URI isn't absolute. */
    public static URI toRequestURI(String uriStr) {
        try {
            URI uri = new URI(uriStr);
            if ( ! uri.isAbsolute() )
                throw new HttpException("Not an absolute URL: <"+uriStr+">");
            return uri;
        } catch (URISyntaxException ex) {
            int idx = ex.getIndex();
            String msg = (idx<0)
                ? String.format("Bad URL: %s", uriStr)
                : String.format("Bad URL: %s starting at character %d", uriStr, idx);
            throw new HttpException(msg, ex);
        }
    }

    // Terminology:
    // RFC 2616:   Request-Line   = Method SP Request-URI SP HTTP-Version CRLF

    // RFC 7320:   request-line   = method SP request-target SP HTTP-version CRLF
    // https://datatracker.ietf.org/doc/html/rfc7230#section-3.1.1

    // request-target:
    // https://datatracker.ietf.org/doc/html/rfc7230#section-5.3
    // When it is for the origin server ==> absolute-path [ "?" query ]

    // EndpointURI: URL for a service, no query string.

    /** Test whether a URI is a service endpoint. It must be absolute, with host and path, and without query string or fragment. */
    public static boolean isEndpoint(URI uri) {
        return uri.isAbsolute() &&
                uri.getHost() != null &&
                uri.getRawPath() != null &&
                uri.getRawQuery() == null &&
                uri.getRawFragment() == null;
    }

    /**
     * Return a string (assumed ot be a URI) without query string or fragment.
     */
    public static String endpoint(String uriStr) {
        int idx1 = uriStr.indexOf('?');
        int idx2 = uriStr.indexOf('#');

        if ( idx1 < 0 && idx2 < 0 )
            return uriStr;

        int idx = -1;
        if ( idx1 < 0 && idx2 > 0 )
            idx = idx2;
        else if ( idx1 > 0 && idx2 < 0 )
            idx = idx1;
        else
            idx = Math.min(idx1,  idx2);
        return uriStr.substring(0, idx);
    }

    /** RFC7320 "request-target", used in digest authentication. */
    public static String requestTarget(URI uri) {
        String path = uri.getRawPath();
        if ( path == null || path.isEmpty() )
            path = "/";
        String qs = uri.getQuery();
        if ( qs == null || qs.isEmpty() )
            return path;
        return path+"?"+qs;
    }

    /** URI, without query string and fragment. */
    public static URI endpointURI(URI uri) {
        if ( uri.getRawQuery() == null && uri.getRawFragment() == null )
            return uri;
        try {
            // Same URI except without query strinf an fragment.
            return new URI(uri.getScheme(), uri.getRawAuthority(), uri.getRawPath(), null, null);
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }

    /** Return a HttpRequest */
    public static HttpRequest newGetRequest(String url, Consumer<HttpRequest.Builder> modifier) {
        HttpRequest.Builder builder = HttpLib.requestBuilderFor(url).uri(toRequestURI(url)).GET();
        if ( modifier != null )
            modifier.accept(builder);
        return builder.build();
    }

    public static <X> X dft(X value, X dftValue) {
        return (value != null) ? value : dftValue;
    }

    public static <X> List<X> copyArray(List<X> array) {
        if ( array == null )
            return null;
        return new ArrayList<>(array);
    }

    /** Encode a string suitable for use in an URL query string */
    public static String urlEncodeQueryString(String str) {
        // java.net.URLEncoder is excessive - it encodes / and : which
        // is not necessary in a query string or fragment.
        return IRILib.encodeUriQueryFrag(str);
    }

    /** Query string is assumed to already be encoded. */
    public static String requestURL(String url, String queryString) {
        if ( queryString == null || queryString.isEmpty() )
            // Empty string. Don't add "?"
            return url;
        String sep =  url.contains("?") ? "&" : "?";
        String requestURL = url+sep+queryString;
        return requestURL;
    }

    public static HttpRequest.Builder requestBuilderFor(String serviceEndpoint) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        return AuthEnv.get().addAuth(requestBuilder, serviceEndpoint);
    }

    public static Builder requestBuilder(String url, Map<String, String> httpHeaders, long readTimeout, TimeUnit readTimeoutUnit) {
        HttpRequest.Builder builder = HttpLib.requestBuilderFor(url);
        headers(builder, httpHeaders);
        builder.uri(toRequestURI(url));
        if ( readTimeout >= 0 )
            builder.timeout(Duration.ofMillis(readTimeoutUnit.toMillis(readTimeout)));
        return builder;
    }

    /** Create a {@code HttpRequest.Builder} from an {@code HttpRequest}. */
    public static HttpRequest.Builder createBuilder(HttpRequest request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .expectContinue(request.expectContinue())
                .uri(request.uri());
        builder.method(request.method(), request.bodyPublisher().orElse(BodyPublishers.noBody()));
        request.timeout().ifPresent(builder::timeout);
        request.version().ifPresent(builder::version);
        request.headers().map().forEach((name, values)->values.forEach(value->builder.header(name, value)));
        return builder;
    }

    /** Set the headers from the Map if the map is not null. Returns the Builder. */
    static Builder headers(Builder builder, Map<String, String> httpHeaders) {
        if ( httpHeaders != null )
            httpHeaders.forEach(builder::header);
        return builder;
    }

    /** Set the "Accept" header if value is not null. Returns the builder. */
    public static Builder acceptHeader(Builder builder, String acceptHeader) {
        if ( acceptHeader != null )
            builder.header(HttpNames.hAccept, acceptHeader);
        return builder;
    }

    /** Set the "Content-Type" header if value is not null. Returns the builder. */
    public static Builder contentTypeHeader(Builder builder, String contentType) {
        if ( contentType != null )
            builder.header(HttpNames.hContentType, contentType);
        return builder;
    }

    // Disabled. Don't encourage using compression ("Content-Encoding: gzip") because it interacts with streaming.
    // Specifically, streaming (unknown Content-Length) needs chunking. Both chunking and compression
    // encodings use the same HTTP header. Yet they are handled by different layers.
    // The basic http code handles chunking.
    // The complete encoding header can get removed resulting in a compressed stream
    // without any indication being passed to the application.
//    /**
//     * Set the "Accept-Encoding" header. Returns the builder.
//     * See {@link #getInputStream(HttpResponse)}.
//     */
//    public
//    /*package*/ static Builder acceptEncodingCompressed(Builder builder) {
//        builder.header(HttpNames.hAcceptEncoding, WebContent.acceptEncodingCompressed);
//        return builder;
//    }

    /**
     * Execute a request, return a {@code HttpResponse<InputStream>} which
     * can be passed to {@link #handleResponseInputStream(HttpResponse)} which will
     * convert non-2xx status code to {@link HttpException HttpExceptions}.
     * <p>
     * This function applies the HTTP authentication challenge support
     * and will repeat the request if necessary with added authentication.
     * <p>
     * See {@link AuthEnv} for authentication registration.
     * <br/>
     * See {@link #executeJDK} to execute exactly once without challenge response handling.
     *
     * @see AuthEnv AuthEnv for authentic registration
     * @see #executeJDK executeJDK to execute exacly once.
     *
     * @param httpClient
     * @param httpRequest
     * @return HttpResponse
     */
    public static HttpResponse<InputStream> execute(HttpClient httpClient, HttpRequest httpRequest) {
        return execute(httpClient, httpRequest, BodyHandlers.ofInputStream());
    }

    /**
     * Execute a request, return a {@code HttpResponse<X>} which
     * can be passed to {@link #handleHttpStatusCode(HttpResponse)} which will
     * convert non-2xx status code to {@link HttpException HttpExceptions}.
     * <p>
     * This function applies the HTTP authentication challenge support
     * and will repeat the request if necessary with added authentication.
     * <p>
     * See {@link AuthEnv} for authentication registration.
     * <br/>
     * See {@link #executeJDK} to execute exactly once without challenge response handling.
     *
     * @see AuthEnv AuthEnv for authentic registration
     * @see #executeJDK executeJDK to execute exacly once.
     *
     * @param httpClient
     * @param httpRequest
     * @param bodyHandler
     * @return HttpResponse
     */
    public
    /*package*/ static <X> HttpResponse<X> execute(HttpClient httpClient, HttpRequest httpRequest, BodyHandler<X> bodyHandler) {
        // To run with no jena-supplied authentication handling.
        if ( false )
            return executeJDK(httpClient, httpRequest, bodyHandler);
        URI uri = httpRequest.uri();
        URI key = null;

        AuthEnv authEnv = AuthEnv.get();

        if ( uri.getUserInfo() != null ) {
            String[] up = uri.getUserInfo().split(":");
            if ( up.length == 2 ) {
                // Only if "user:password@host", not "user@host"
                key = HttpLib.endpointURI(uri);
                // The auth key will be with u:p making it specific.
                authEnv.registerUsernamePassword(key, up[0], up[1]);
            }
        }
        try {
            return AuthLib.authExecute(httpClient, httpRequest, bodyHandler);
        } finally {
            if ( key != null )
                authEnv.unregisterUsernamePassword(key);
        }
    }

    /**
     * Execute request and return a response without authentication challenge handling.
     * @param httpClient
     * @param httpRequest
     * @param bodyHandler
     * @return HttpResponse
     */
    public static <T> HttpResponse<T> executeJDK(HttpClient httpClient, HttpRequest httpRequest, BodyHandler<T> bodyHandler) {
        try {
            // This is the one place all HTTP requests go through.
            logRequest(httpRequest);
            HttpResponse<T> httpResponse = httpClient.send(httpRequest, bodyHandler);
            logResponse(httpResponse);
            return httpResponse;
        //} catch (HttpTimeoutException ex) {
        } catch (IOException | InterruptedException ex) {
            if ( ex.getMessage() != null ) {
                // This is silly.
                // Rather than an HTTP exception, bad authentication becomes IOException("too many authentication attempts");
                // or IOException("No credentials provided") if the authenticator decides to return null.
                if ( ex.getMessage().contains("too many authentication attempts") ||
                     ex.getMessage().contains("No credentials provided") ) {
                    throw new HttpException(401, HttpSC.getMessage(401));
                }
            }
            throw new HttpException(httpRequest.method()+" "+httpRequest.uri().toString(), ex);
        }
    }

    /*package*/ static CompletableFuture<HttpResponse<InputStream>> asyncExecute(HttpClient httpClient, HttpRequest httpRequest) {
        logAsyncRequest(httpRequest);
        return httpClient.sendAsync(httpRequest, BodyHandlers.ofInputStream());
    }

    /** Push data. POST, PUT, PATCH request with no response body data. */
    public static void httpPushData(HttpClient httpClient, Push style, String url, Consumer<HttpRequest.Builder> modifier, BodyPublisher body) {
        HttpResponse<InputStream> response = httpPushWithResponse(httpClient, style, url, modifier, body);
        handleResponseNoBody(response);
    }

    // Worker
    /*package*/ static HttpResponse<InputStream> httpPushWithResponse(HttpClient httpClient, Push style, String url,
                                                                      Consumer<HttpRequest.Builder> modifier, BodyPublisher body) {
        URI uri = toRequestURI(url);
        HttpRequest.Builder builder = requestBuilderFor(url);
        builder.uri(uri);
        builder.method(style.method(), body);
        if ( modifier != null )
            modifier.accept(builder);
        HttpResponse<InputStream> response = execute(httpClient, builder.build());
        return response;
    }


    /** Request */
    private static void logRequest(HttpRequest httpRequest) {
        // Uses the SystemLogger which defaults to JUL.
        // Add org.apache.jena.logging:log4j-jpl
        // (java11 : 11.0.9, if using log4j-jpl, logging prints the request as {0} but response OK)
//        httpRequest.uri();
//        httpRequest.method();
//        httpRequest.headers();
    }

    /** Async Request */
    private static void logAsyncRequest(HttpRequest httpRequest) {}

        /** Response (do not touch the body!)  */
    private static void logResponse(HttpResponse<?> httpResponse) {
//        httpResponse.uri();
//        httpResponse.statusCode();
//        httpResponse.headers();
//        httpResponse.previousResponse();
    }

    /**
     * Allow setting additional/optional query parameters on a per remote service (including for SERVICE).
     * <ul>
     * <li>ARQ.httpRequestModifer - the specific modifier</li>
     * <li>ARQ.httpRegistryRequestModifer - the registry, keyed by service URL.</li>
     * </ul>
     */
    /*package*/ public static void modifyByService(String serviceURI, Context context, Params params, Map<String, String> httpHeaders) {
        HttpRequestModifier modifier = context.get(ARQ.httpRequestModifer);
        if ( modifier != null ) {
            modifier.modify(params, httpHeaders);
            return;
        }
        RegistryRequestModifier modifierRegistry = context.get(ARQ.httpRegistryRequestModifer);
        if ( modifierRegistry == null )
            modifierRegistry = RegistryRequestModifier.get();
        if ( modifierRegistry != null ) {
            HttpRequestModifier mods = modifierRegistry.find(serviceURI);
            if ( mods != null )
                mods.modify(params, httpHeaders);
        }
    }

    /**
     * Return a modifier that will set the Accept header to the value.
     * An argument of "null" means "no action".
     */
    public static Consumer<HttpRequest.Builder> setHeaders(Map<String, String> headers) {
        if ( headers == null )
            return (x)->{};
        return x->headers.forEach(x::header);
    }

    /**
     * Return a modifier that will set the Accept header to the value.
     * An argument of "null" means "no action".
     */
    static Consumer<HttpRequest.Builder> setAcceptHeader(String acceptHeader) {
        if ( acceptHeader == null )
            return (x)->{};
        return header(HttpNames.hAccept, acceptHeader);
    }

    /**
     * Return a modifier that will set the Content-Type header to the value.
     * An argument of "null" means "no action".
     */
    static Consumer<HttpRequest.Builder> setContentTypeHeader(String contentType) {
        if ( contentType == null )
            return (x)->{};
        return header(HttpNames.hContentType, contentType);
    }

    /**
     * Return a modifier that will set the named header to the value.
     */
    static Consumer<HttpRequest.Builder> header(String headerName, String headerValue) {
        return x->x.header(headerName, headerValue);
    }

    /** Return the first header of the given name, or null if none */
    public static String responseHeader(HttpResponse<?> response, String headerName) {
        Objects.requireNonNull(response);
        Objects.requireNonNull(headerName);
        return response.headers().firstValue(headerName).orElse(null);
    }

    // HTTP response header inserted to aid tracking.
    public static String FusekiRequestIdHeader = "Fuseki-Request-Id";

    /**
     * Test whether a URL identifies a Fuseki server. This operation can not guarantee to
     * detect a Fuseki server - for example, it may be behind a reverse proxy that masks
     * the signature.
     */
    public static boolean isFuseki(String datasetURL) {
        HttpRequest.Builder builder =
                HttpRequest.newBuilder().uri(toRequestURI(datasetURL)).method(HttpNames.METHOD_HEAD, BodyPublishers.noBody());
        HttpRequest request = builder.build();
        HttpClient httpClient = HttpEnv.getDftHttpClient();
        HttpResponse<InputStream> response = execute(httpClient, request);
        handleResponseNoBody(response);

        Optional<String> value1 = response.headers().firstValue(FusekiRequestIdHeader);
        if ( value1.isPresent() )
            return true;
        Optional<String> value2 = response.headers().firstValue("Server");
        if ( value2.isEmpty() )
            return false;
        String headerValue = value2.get();
        boolean isFuseki = headerValue.startsWith("Apache Jena Fuseki");
        if ( !isFuseki )
            isFuseki = headerValue.toLowerCase().contains("fuseki");
        return isFuseki;
    }
}
