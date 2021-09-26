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

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;

import org.apache.jena.riot.RDFFormat;

/**
 * JVM wide settings.
 */
public class HttpEnv {

    // These preserve prefixes.
    public static final RDFFormat defaultTriplesFormat = RDFFormat.TURTLE_BLOCKS;
    public static final RDFFormat defaultQuadsFormat = RDFFormat.TRIG_BLOCKS;

    /**
     * Maximum length of URL for GET requests for SPARQL queries
     * (<a href="https://www.w3.org/TR/sparql11-protocol/">SPARQL 1.1 Protocol</a>).
     * Above this limit, the code switches to using the HTTP POST form.
     */
    public static /* final */ int urlLimit = 2 * 1024;

    public static HttpClient getDftHttpClient() { return httpClient; }
    public static void setDftHttpClient(HttpClient dftHttpClient) { httpClient = dftHttpClient; }

    /** Return the {@link HttpClient} based on URL and a possible pre-selected {@link HttpClient}. */
    public static HttpClient getHttpClient(String url, HttpClient specificHttpClient) {
        if ( specificHttpClient != null )
             return specificHttpClient;
        HttpClient requestHttpClient = RegistryHttpClient.get().find(url);
        if ( requestHttpClient == null )
            requestHttpClient = getDftHttpClient();
        return requestHttpClient;
    }

    private static HttpClient httpClient = buildDftHttpClient();

    private static HttpClient buildDftHttpClient() {
        return httpClientBuilder().build();
    }

    public static HttpClient.Builder httpClientBuilder() {
        return HttpClient.newBuilder()
                // By default, the client has polling and connection-caching.
                // Version HTTP/2 is the default, negotiating up from HTTP 1.1.
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(Redirect.NORMAL)
                //.sslContext
                //.sslParameters
                //.proxy
                //.authenticator
                ;
    }
}
