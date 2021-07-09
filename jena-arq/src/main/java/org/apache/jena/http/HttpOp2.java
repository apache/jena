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

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.Params;

/**
 * This is a collection of convenience operations for HTTP requests,
 * mostly in support of RDF handling and common, basic use cases for HTTP.
 * It is not comprehensive
 * <p>
 * Authentication is handled by supplying a {@link java.net.http.HttpClient}
 * which has been built with an {@link Authenticator}.
 *
 * @see HttpRDF
 * @see GSP
 */
public class HttpOp2 {

    private HttpOp2() {}

    public static String httpGetString(String url) {
        return httpGetString(HttpEnv.getDftHttpClient(), url, null);
    }

    public static String httpGetString(String url, String acceptHeader) {
        return httpGetString(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    public static String httpGetString(HttpClient httpClient, String url, String acceptHeader) {
        HttpRequest request = newGetRequest(httpClient, url, setAcceptHeader(acceptHeader));
        HttpResponse<InputStream> response = execute(httpClient, request);
        return handleResponseRtnString(response);
    }

    /** POST (without a body) - like httpGetString but uses POST - expects a response */
    public static String httpPostRtnString(String url) {
        return httpPostRtnString(HttpEnv.getDftHttpClient(), url);
    }

    /** POST (without a body) - like httpGetString but uses POST - expects a response */
    public static String httpPostRtnString(HttpClient httpClient, String url) {
        HttpRequest requestData = HttpRequest.newBuilder()
            .POST(BodyPublishers.noBody())
            .uri(toRequestURI(url))
            .build();
        HttpResponse<InputStream> response = execute(httpClient, requestData);
        return handleResponseRtnString(response);
    }

    /** Perform an HTTP GET to a URL, with "Accept" header "*{@literal /}*". The application MUST close the InputStream. */
    public static InputStream httpGet(String url) {
        return httpGet(HttpEnv.getDftHttpClient(), url);
    }

    /** Perform an HTTP GET to a URL. The application MUST close the InputStream. */
    public static InputStream httpGet(String url, String acceptHeader) {
        return httpGet(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    /** Perform an HTTP GET to a URL. The application MUST close the InputStream. */
    public static InputStream httpGet(HttpClient httpClient, String url) {
        return httpGet(httpClient, url, null);
    }

    /** Perform an HTTP GET to a URL. The application MUST close the InputStream. */
    public static InputStream httpGet(HttpClient httpClient, String url, String acceptHeader) {
        return execGet(httpClient, url, acceptHeader);
    }

    /** MUST read the whole InputStream or close it. */
    private static InputStream execGet(HttpClient httpClient, String url, String acceptHeader) {
        if ( acceptHeader == null )
            acceptHeader = "*/*";
        HttpRequest request = newGetRequest(httpClient, url, setAcceptHeader(acceptHeader));
        return execGet(httpClient, request);
    }

    /** MUST read the whole InputStream or close it. */
    private static InputStream execGet(HttpClient httpClient, HttpRequest request) {
        HttpResponse<InputStream> response = execute(httpClient, request);
        return handleResponseInputStream(response);
    }

    /** POST
     * @see BodyPublishers
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPost(String url, String contentType, BodyPublisher body) {
        httpPost(HttpEnv.getDftHttpClient(), url, contentType, body);
    }

    /** POST
     * @see BodyPublishers#ofFile
     * @see BodyPublishers#ofString
     */
    public static void httpPost(HttpClient httpClient, String url, String contentType, BodyPublisher body) {
        httpPushData(httpClient, POST, url, contentType, body);
    }

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
        httpPushData(httpClient, PUT, url, contentType, body);
    }

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
        httpPushData(httpClient, PATCH, url, contentType, body);
    }

    /** Push data. POST, PUT, PATCH request with no response body data. */
    private static void httpPushData(HttpClient httpClient, Push style, String url, String contentType, BodyPublisher body) {
        HttpLib.httpPushData(httpClient, style, url, setContentTypeHeader(contentType), body);
    }

    // POST form - probably not needed in this convenience class.
    // Retain for reference.
    /*package*/ static HttpResponse<InputStream> httpPostForm(String url, Params params, String acceptString) {
        return httpPostForm(HttpEnv.getDftHttpClient(), url, params, acceptString);
    }

    private static HttpResponse<InputStream> httpPostForm(HttpClient httpClient, String url, Params params, String acceptString) {
        Objects.requireNonNull(url);
        acceptString = HttpLib.dft(acceptString, "*/*");
        URI uri = toRequestURI(url);
        String formData = params.httpString();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .POST(BodyPublishers.ofString(formData))
            .header(HttpNames.hContentType, WebContent.contentTypeHTMLForm)
            .header(HttpNames.hAccept, acceptString)
            .build();
        HttpResponse<InputStream> response = execute(httpClient, request);
        handleHttpStatusCode(response);
        return response;
    }

    /** DELETE */
    public static void httpDelete(String url) {
        httpDelete(HttpEnv.getDftHttpClient(), url);
    }

    /** DELETE */
    public static void httpDelete(HttpClient httpClient, String url) {
        URI uri = toRequestURI(url);
        HttpRequest requestData = HttpRequest.newBuilder()
            .DELETE()
            .uri(uri)
            .build();
        HttpResponse<InputStream> response = execute(httpClient, requestData);
        handleResponseNoBody(response);
    }


    /** OPTIONS. Returns the HTTP response "Allow" field string. */
    public static String httpOptions(String url) {
        return httpOptions(HttpEnv.getDftHttpClient(), url);
    }

    /** OPTIONS. Returns the HTTP response "Allow" field string. */
    public static String httpOptions(HttpClient httpClient, String url) {
        // Need to access the response headers
        HttpRequest.Builder builder =
            HttpRequest.newBuilder().uri(toRequestURI(url)).method(HttpNames.METHOD_OPTIONS, BodyPublishers.noBody());
        HttpRequest request = builder.build();
        HttpResponse<InputStream> response = execute(httpClient, request);
        String allowValue = response.headers().firstValue(HttpNames.hAllow).orElse(null);
        handleResponseNoBody(response);
        return allowValue;
    }

//    /**
//     * General HTTP request, String only version. Processes the HTTP status response.
//     */
//    public static String httpRequest(HttpClient httpClient, String method, String url) {
//        HttpRequest.Builder builder =
//            HttpRequest.newBuilder().uri(toRequestURI(url)).method(method, BodyPublishers.noBody());
//        HttpRequest request = builder.build();
//        HttpResponse<InputStream> response = execute(httpClient, request);
//        return handleResponseRtnString(response);
//    }

    /**
     * Content-Type, without charset.
     * <p>
     * RDF formats are either UTF-8 or XML , where the charset is determined by the
     * processing instruction at the start of the content. Parsing is on byte
     * streams.
     */
    private static <T> String determineContentType(HttpResponse<T> response) {
        String ctStr = response.headers().firstValue(HttpNames.hContentType).orElse(null);
        if ( ctStr != null ) {
            int i = ctStr.indexOf(';');
            if ( i >= 0 )
                ctStr = ctStr.substring(0, i);
        }
        return ctStr;
    }
}
