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

import static org.apache.jena.http.HttpLib.*;
import static org.apache.jena.http.Push.PATCH;
import static org.apache.jena.http.Push.POST;
import static org.apache.jena.http.Push.PUT;
import static org.apache.jena.riot.web.HttpNames.METHOD_HEAD;
import static org.apache.jena.riot.web.HttpNames.METHOD_OPTIONS;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.web.HttpSC;

/**
 * This is a collection of convenience operations for HTTP requests, mostly in
 * support of RDF handling and common, basic use cases for HTTP. It is not
 * comprehensive. For more complicated requirements of HTTP, then the
 * application can use {@link java.net.http.HttpClient} directly.
 * <p>
 * Authentication can be handled by supplying a {@link java.net.http.HttpClient} which
 * has been built with an {@link Authenticator} or for challenge response (basic and digest)
 * see {@link AuthEnv}.
 * <p>
 * Operations throw {@link HttpException} when the response is not 2xx, except for
 * "httpGetString" and "httpPostRtnString" which return null for a 404 response.
 * </p>
 * <p>
 * Also supported:
 * </p>
 * <ul>
 * <li>GET and return a string</li>
 * <li>GET and return a JSON structure</li>
 * </ul>
 * @see HttpRDF
 * @see GSP
 */
public class HttpOp {

    private HttpOp() {}

    // -- GET, POST returning a string (shorthand helpers, esp for tests).

    /** Perform an HTTP and return the body as a string, Return null for a "404 Not Found". */
    public static String httpGetString(String url) {
        return httpGetString(HttpEnv.getDftHttpClient(), url, null);
    }

    /** Perform an HTTP and return the body as a string, Return null for a "404 Not Found". */
    public static String httpGetString(String url, String acceptHeader) {
        return httpGetString(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    /** Perform an HTTP and return the body as a string. Return null for a "404 Not Found". */
    public static String httpGetString(HttpClient httpClient, String url) {
        return httpGetString(httpClient, url, null);
    }

    /** Perform an HTTP and return the body as a string. Return null for a "404 Not Found". */
    public static String httpGetString(HttpClient httpClient, String url, String acceptHeader) {
        HttpRequest request = newGetRequest(url, setAcceptHeader(acceptHeader));
        HttpResponse<InputStream> response = execute(httpClient, request);
        try {
            return handleResponseRtnString(response);
        } catch (HttpException ex) {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                return null;
            throw ex;
        }
    }

    /**
     * POST (without a body) - like httpGetString but uses POST - expects a response.
     * Return null for a "404 Not Found".
     */
    public static String httpPostRtnString(String url) {
        return httpPostRtnString(HttpEnv.getDftHttpClient(), url);
    }

    /**
     * POST (without a body) - like httpGetString but uses POST - expects a response.
     * Return null for a "404 Not Found".
     */
    public static String httpPostRtnString(HttpClient httpClient, String url) {
        HttpRequest requestData = HttpLib.requestBuilderFor(url)
            .POST(BodyPublishers.noBody())
            .uri(toRequestURI(url))
            .build();
        HttpResponse<InputStream> response = execute(httpClient, requestData);
        try {
            return handleResponseRtnString(response);
        } catch (HttpException ex) {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                return null;
            throw ex;
        }
    }

    // ---- POST HTML form

    /** POST params as a HTML form. */
    public static void httpPostForm(String url, Params params) {
        try ( TypedInputStream in = execPostForm(HttpEnv.getDftHttpClient(), url, params, null) ) {}
    }

    /** POST params as a HTML form. */
    public static TypedInputStream httpPostForm(String url, Params params, String acceptString) {
        return execPostForm(HttpEnv.getDftHttpClient(), url, params, acceptString);
    }

    private static TypedInputStream execPostForm(HttpClient httpClient, String url, Params params, String acceptString) {
        Objects.requireNonNull(url);
        acceptString = HttpLib.dft(acceptString, "*/*");
        URI uri = toRequestURI(url);
        String formData = params.httpString();
        HttpRequest request = HttpLib.requestBuilderFor(url)
            .uri(uri)
            .POST(BodyPublishers.ofString(formData))
            .header(HttpNames.hContentType, WebContent.contentTypeHTMLForm)
            .header(HttpNames.hAccept, acceptString)
            .build();
        HttpResponse<InputStream> response = execute(httpClient, request);
        return handleResponseTypedInputStream(response);
    }

    // ---- JSON

    public static JsonValue httpPostRtnJSON(String url) {
        try ( TypedInputStream in = httpPostStream(url, WebContent.contentTypeJSON) ) {
            return JSON.parseAny(in.getInputStream());
        }
    }

    public static JsonValue httpGetJson(String url) {
        try ( TypedInputStream in = httpGet(url, WebContent.contentTypeJSON) ) {
            return JSON.parseAny(in.getInputStream());
        }
    }

    // ---- GET

    /** Perform an HTTP GET to a URL, with "Accept" header "*{@literal /}*". The application MUST close the InputStream. */
    public static TypedInputStream httpGet(String url) {
        return httpGet(HttpEnv.getDftHttpClient(), url);
    }

    /** Perform an HTTP GET to a URL. The application MUST close the InputStream. */
    public static TypedInputStream httpGet(String url, String acceptHeader) {
        return httpGet(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    /** Perform an HTTP GET to a URL. The application MUST close the InputStream. */
    public static TypedInputStream httpGet(HttpClient httpClient, String url) {
        return httpGet(httpClient, url, null);
    }

    /** Perform an HTTP GET to a URL. The application MUST close the InputStream. */
    public static TypedInputStream httpGet(HttpClient httpClient, String url, String acceptHeader) {
        return execGet(httpClient, url, acceptHeader);
    }

    /** MUST read the whole InputStream or close it. */
    private static TypedInputStream execGet(HttpClient httpClient, String url, String acceptHeader) {
        if ( acceptHeader == null )
            acceptHeader = "*/*";
        HttpRequest request = newGetRequest(url, setAcceptHeader(acceptHeader));
        return execGet(httpClient, request);
    }

    /** MUST read the whole InputStream or close it. */
    private static TypedInputStream execGet(HttpClient httpClient, HttpRequest request) {
        HttpResponse<InputStream> response = execute(httpClient, request);
        return handleResponseTypedInputStream(response);
    }

    // ---- POST

    /** POST */
    public static void httpPost(String url) {
        httpPost(HttpEnv.getDftHttpClient(), url, null, BodyPublishers.noBody());
    }

    /** POST
     * @see BodyPublishers
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPost(String url, String contentType, BodyPublisher body) {
        httpPost(HttpEnv.getDftHttpClient(), url, contentType, body);
    }

    /** POST to a URL with content=type and string. */
    public static void httpPost(String url, String contentType, String body) {
        httpPost(HttpEnv.getDftHttpClient(), url, contentType, BodyPublishers.ofString(body));
    }

    /** POST
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPost(HttpClient httpClient, String url, String contentType, BodyPublisher body) {
        execHttpPost(httpClient, url, contentType, body);
    }

    private static void execHttpPost(HttpClient httpClient, String url, String contentType, BodyPublisher body) {
        execPushData(httpClient, POST, url, contentType, body);
    }

    // ---- POST stream response.

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(String url) {
        return httpPostStream(HttpEnv.getDftHttpClient(), url);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(String url, String acceptHeader) {
        return execPostStream(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(HttpClient httpClient, String url) {
        return httpPostStream(httpClient, url, null);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(HttpClient httpClient, String url, String acceptHeader) {
        return execPostStream(httpClient, url, acceptHeader);
    }

    /** POST(URL) -> InputStream+Content-Type. The application MUST close the InputStream. */
    private static TypedInputStream execPostStream(HttpClient httpClient, String url, String acceptHeader) {
        return execPostStream(httpClient, url, null, null, acceptHeader);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(String url, String contentType, BodyPublisher bodyContent) {
        return httpPostStream(HttpEnv.getDftHttpClient(), url, contentType, bodyContent);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(String url, String contentType, String bodyContent) {
        return httpPostStream(HttpEnv.getDftHttpClient(), url, contentType, BodyPublishers.ofString(bodyContent));
    }

    // ---- POST content, stream response

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(String url, String contentType, BodyPublisher bodyContent, String acceptHeader) {
        return httpPostStream(HttpEnv.getDftHttpClient(), url, contentType, bodyContent, acceptHeader);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(HttpClient httpClient, String url, String contentType, BodyPublisher bodyContent) {
        return httpPostStream(httpClient, url, contentType, bodyContent);
    }

    /** POST - the application MUST close the InputStream.*/
    public static TypedInputStream httpPostStream(HttpClient httpClient, String url, String contentType, BodyPublisher bodyContent, String acceptHeader) {
        return execPostStream(httpClient, url, contentType, bodyContent, acceptHeader);
    }

    /** POST(URL), with a body -> InputStream+Content-Type. The application MUST close the InputStream. */
    private static TypedInputStream execPostStream(HttpClient httpClient, String url, String contentType, BodyPublisher bodyPublisher, String acceptHeader) {
        acceptHeader = HttpLib.dft(acceptHeader, "*/");
        if ( bodyPublisher == null )
            bodyPublisher = BodyPublishers.noBody();
        HttpRequest.Builder builder = HttpLib.requestBuilderFor(url).uri(toRequestURI(url));
        HttpLib.contentTypeHeader(builder, contentType);
        HttpLib.acceptHeader(builder, acceptHeader);
        HttpRequest request = builder.POST(bodyPublisher).build();
        HttpResponse<InputStream> response = HttpLib.execute(httpClient, request);
        return HttpLib.handleResponseTypedInputStream(response);
    }

    // ---- PUT

    /** PUT
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPut(String url, String contentType, BodyPublisher body) {
        httpPut(HttpEnv.getDftHttpClient(), url, contentType, body);
    }

    /** PUT
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPut(HttpClient httpClient, String url, String contentType, BodyPublisher body) {
        execPushData(httpClient, PUT, url, contentType, body);
    }

    // ---- PATCH

    /** PATCH
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPatch(String url, String contentType, BodyPublisher body) {
        httpPatch(HttpEnv.getDftHttpClient(), url, contentType, body);
    }

    /** PATCH
     * @see BodyPublishers
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPatch(HttpClient httpClient, String url, String contentType, BodyPublisher body) {
        execPushData(httpClient, PATCH, url, contentType, body);
    }

    /** Push data. POST, PUT, PATCH request with no response body data. */
    private static void execPushData(HttpClient httpClient, Push style, String url, String contentType, BodyPublisher body) {
        HttpLib.httpPushData(httpClient, style, url, setContentTypeHeader(contentType), body);
    }

    // ---- DELETE

    /** DELETE */
    public static void httpDelete(String url) {
        httpDelete(HttpEnv.getDftHttpClient(), url);
    }

    /** DELETE */
    public static void httpDelete(HttpClient httpClient, String url) {
        URI uri = toRequestURI(url);
        HttpRequest requestData = HttpLib.requestBuilderFor(url)
            .DELETE()
            .uri(uri)
            .build();
        HttpResponse<InputStream> response = execute(httpClient, requestData);
        handleResponseNoBody(response);
    }


    // ---- OPTIONS

    /** OPTIONS. Returns the HTTP response "Allow" field string. */
    public static String httpOptions(String url) {
        return httpOptions(HttpEnv.getDftHttpClient(), url);
    }

    /** OPTIONS. Returns the HTTP response "Allow" field string. */
    public static String httpOptions(HttpClient httpClient, String url) {
        // Need to access the response headers
        HttpRequest.Builder builder =
                HttpLib.requestBuilderFor(url).uri(toRequestURI(url)).method(METHOD_OPTIONS, BodyPublishers.noBody());
        HttpRequest request = builder.build();
        HttpResponse<InputStream> response = execute(httpClient, request);
        String allowValue = HttpLib.responseHeader(response, HttpNames.hAllow);
        handleResponseNoBody(response);
        return allowValue;
    }

    // ---- HEAD

    /**
     * HEAD request, return the Content-Type (if any).
     * Return null for success (200) but no content type.
     * Throw {@link HttpException} for any response that is not 2xx.
     */
    public static String httpHead(String url) {
        return httpHead(HttpEnv.getDftHttpClient(), url);
    }

    /**
     * HEAD request, return the Content-Type (if any).
     * Return null for success (200) but no content type.
     * Throw {@link HttpException} for any response that is not 2xx.
     */
    public static String httpHead(HttpClient httpClient, String url) {
        return httpHead(httpClient, url, null);
    }

    /**
     * HEAD request, return the Content-Type (if any).
     * Return null for success (200) but no content type.
     * Throw {@link HttpException} for any response that is not 2xx.
     */
    public static String httpHead(String url, String acceptHeader) {
        return httpHead(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    /**
     * HEAD request, return the Content-Type (if any).
     * Return null for success (200) but no content type.
     * Throw {@link HttpException} for any response that is not 2xx.
     */
    public static String httpHead(HttpClient httpClient, String url, String acceptHeader) {
        HttpRequest.Builder builder =
                HttpLib.requestBuilderFor(url).uri(toRequestURI(url)).method(METHOD_HEAD, BodyPublishers.noBody());
        HttpLib.acceptHeader(builder, acceptHeader);
        HttpRequest request = builder.build();
        HttpResponse<InputStream> response = execute(httpClient, request);
        HttpLib.handleResponseNoBody(response);
        return response.headers().firstValue(HttpNames.hContentType).orElse(null);
    }
}
