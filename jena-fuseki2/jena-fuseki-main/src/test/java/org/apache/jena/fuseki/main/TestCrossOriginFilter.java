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
package org.apache.jena.fuseki.main;

import static org.apache.jena.fuseki.servlets.CrossOriginFilter.*;
import static org.apache.jena.http.HttpLib.handleResponseNoBody;
import static org.apache.jena.riot.web.HttpNames.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.web.HttpNames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration tests for CORS handling.
 * Mock unit testing: {@code TestCrossOriginFilterMock} (in jena-fuseki-core)
 */
public class TestCrossOriginFilter {
    static { FusekiLogging.setLogging(); }

    /** System property that allows "Host" to be set. */
    private static String jdkAllowRestrictedHeaders = "jdk.httpclient.allowRestrictedHeaders";

//    private static FusekiServer server = null;
//    private static String URL = null;
    private static Optional<String> systemValue = null;

    @BeforeClass
    public static void beforeClass() {
        // Allow pretending to be another host
        systemValue = Optional.ofNullable(System.setProperty(jdkAllowRestrictedHeaders, "host"));
    }

    @AfterClass
    public static void afterClass() {
        if ( systemValue != null ) {
            if ( systemValue.isPresent() )
                System.setProperty(jdkAllowRestrictedHeaders, systemValue.get());
            else
                System.clearProperty(jdkAllowRestrictedHeaders);
        }
    }

    private static FusekiServer server(String ...args) {
        return FusekiServer.construct(args);
    }

    private static void executeWithServer(FusekiServer server, String datasetName, Consumer<String> action) {
        server.start();
        String URL = server.datasetURL(datasetName);
        try {
            action.accept(URL);
        } finally { server.stop(); }
    }

    @Test
    public void test_corsWithOrigin() {
        FusekiServer server = FusekiServer.construct("-v", "--port=0", "--mem", "/ds");
        executeWithServer(server, "/ds", URL->{
            // Default responses.
            String originString = "https://test.example.org/";
            HttpResponse<InputStream> response = httpOptions(URL,
                                                             // "Host", "test.example.com"
                                                             "Access-Control-Request-Method", "GET",
                                                             "Origin", originString);
            //print(response);

            String allowCreds = getHeader(response, ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER);
            assertNotNull(allowCreds);
            assertEqualsIgnoreCase(allowCreds, "true");

            Set<String> allowHeaders = getHeaderSet(response, ACCESS_CONTROL_ALLOW_HEADERS_HEADER);
            Set<String> expectedHeaders = Set.of("X-Requested-With", "Content-Type", "Accept", "Origin", "Last-Modified", "Authorization");
            assertSetEquals(allowHeaders, expectedHeaders);

            Set<String> allowMethods = getHeaderSet(response, ACCESS_CONTROL_ALLOW_METHODS_HEADER);
            Set<String> expectedMethods = Set.of(METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_HEAD, METHOD_PATCH, METHOD_OPTIONS);
            assertSetEquals(allowMethods, expectedMethods);

            String allowOriginHeader = getHeader(response, ACCESS_CONTROL_ALLOW_ORIGIN_HEADER);
            assertEqualsIgnoreCase(allowOriginHeader, originString);

            String allowMaxAge = getHeader(response, ACCESS_CONTROL_MAX_AGE_HEADER);
            assertNotNull(allowMaxAge);
            handleResponseNoBody(response);
        });
    }

    @Test
    public void test_corsLocalhost() {
        FusekiServer server = FusekiServer.construct("-v", "--port=0", "--mem", "/ds");
        executeWithServer(server, "/ds", URL->{
            HttpResponse<InputStream> response = httpOptions(URL,
                                                            //"Host", "test.example.com"
                                                            "Access-Control-Request-Method", "GET"
                                                            //,"Origin", "localhost"
                    );
            //print(response);

            // Nothing expected.
            String h1 = getHeader(response, ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER);
            assertNull(h1);

            String h2 = getHeader(response, ACCESS_CONTROL_ALLOW_HEADERS_HEADER);
            assertNull(h2);

            String h3 = getHeader(response, ACCESS_CONTROL_ALLOW_METHODS_HEADER);
            assertNull(h3);

            String h4 = getHeader(response, ACCESS_CONTROL_ALLOW_ORIGIN_HEADER);
            assertNull(h4);

            String h5 = getHeader(response, ACCESS_CONTROL_MAX_AGE_HEADER);
            assertNull(h5);
            handleResponseNoBody(response);
        });
    }

    private static String getHeader(HttpResponse<?> response, String header) {
        Map<String, List<String>> map = response.headers().map();
        List<String> x = map.get(Lib.lowercase(header));
        if ( x == null )
            return null;
        assertEquals(1, x.size());
        return x.get(0);
    }

    private static Set<String> getHeaderSet(HttpResponse<?> response, String header) {
        Map<String, List<String>> map = response.headers().map();
        List<String> x = map.get(Lib.lowercase(header));
        if ( x == null )
            return null;
        assertEquals(1, x.size());
        String s = x.get(0);
        String[] elts = s.split(" *, *");
        return Set.of(elts);
    }

    private static void assertEqualsIgnoreCase(String allowCreds, String string) {
        assertEquals("Not equals (ignoring case)",
                     Lib.lowercase(allowCreds), Lib.lowercase(string));
    }

    // Assumes no repeated but different case.
    // Case insensitive
    private static void assertSetEquals(Set<String> set, Set<String> expected) {
        if ( set.size() != expected.size() ) {
            fail("Different size: "+set+ " -- "+expected);
        }
        for ( String val : expected ) {
            assertSetContains(set, val);
        }
    }

    // Case insensitive
    private static void assertSetContains(Set<String> set, String value) {
        String x = Lib.lowercase(value);
        for ( String s : set ) {
            if ( s.equalsIgnoreCase(value))
                return;
        }
        fail("Not found: "+value+" in set "+set);
    }

    @Test public void test_CORS_default_success() {
        // given
        String defaultHeaders = "X-Requested-With, Content-Type, Accept, Origin, Last-Modified, Authorization";
        String[] headersToPass = {"Access-Control-Request-Method", "POST",
                                  "Origin", "localhost:12345",
                                  "Access-Control-Request-Headers", defaultHeaders};
        String expectedAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin,Last-Modified,Authorization";
        int port = WebLib.choosePort();
        FusekiServer server = server("--port="+port, "--mem", "/ds");
        assertEquals(port, server.getHttpPort());

        executeWithServer(server, "/ds", URL->{
            // when
            HttpResponse<InputStream> response = httpOptions(URL, headersToPass);
            // then
            assertNotNull(response);
            assertEquals(response.statusCode(), 200);
            String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
            assertNotNull("Expecting valid headers", actualAllowedHeaders);
            assertEquals(expectedAllowedHeaders, actualAllowedHeaders);
            handleResponseNoBody(response);
        });
    }

    @Test public void test_CORS_default_fail() {
        // given
        String unrecognisedHeader = "Content-Type, unknown-header";
        String[] headersToPass = {"Access-Control-Request-Method", "POST",
                                  "Access-Control-Request-Headers", unrecognisedHeader};
        FusekiServer server = server("--port=0", "--mem", "/ds");
        executeWithServer(server, "/ds", URL->{
            // when
            HttpResponse<InputStream> response = httpOptions(URL, headersToPass);
            // then
            assertNotNull(response);
            assertEquals(response.statusCode(), 200);
            String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
            assertNull("No headers expected given invalid request", actualAllowedHeaders);
            handleResponseNoBody(response);
        });
    }

    @Test public void test_CORS_config_success() {
        // given
        String nonDefaultAllowedHeader = "Content-Type, Custom-Header";
        String[] headersToPass = {"Access-Control-Request-Method", "POST",
                                  "Origin", "http://localhost:5173",
                                  "Access-Control-Request-Headers", nonDefaultAllowedHeader};
        String expectedAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin,Last-Modified,Authorization,Custom-Header";
        FusekiServer server = server("--port=0", "--mem", "--CORS=testing/Config/cors.properties","/ds");
        executeWithServer(server, "/ds", URL->{
            // when
            HttpResponse<InputStream> response = httpOptions(URL, headersToPass);
            // then
            assertNotNull(response);
            assertEquals(response.statusCode(), 200);
            String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
            assertNotNull("Expecting valid headers", actualAllowedHeaders);
            assertEquals(expectedAllowedHeaders, actualAllowedHeaders);
            handleResponseNoBody(response);
        });
    }

    @Test public void test_CORS_noConfig() {
        // given
        String defaultHeader = "Content-Type";
        String[] headersToPass = {"Access-Control-Request-Method", "POST",
                                  "Access-Control-Request-Headers", defaultHeader};
        FusekiServer server = server("--port=0", "--mem", "--noCORS", "/ds");
        executeWithServer(server, "/ds", URL->{
            // when
            HttpResponse<InputStream> response = httpOptions(URL, headersToPass);
            // then
            assertNotNull(response);
            assertEquals(response.statusCode(), 200);
            String actualAllowedHeaders = HttpLib.responseHeader(response, HttpNames.hAccessControlAllowHeaders);
            assertNull("No headers expected given invalid request", actualAllowedHeaders);
            handleResponseNoBody(response);
        });
    }

    private static HttpResponse<InputStream> httpOptions(String URL, String...headers) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .method(METHOD_OPTIONS, BodyPublishers.noBody())
                .headers(headers)
                .build();
        try {
            return httpClient.send(request, BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void print(HttpResponse<?> response) {
        response.headers().map().forEach((h,v) -> System.out.printf("%s : %s\n", h, v));
    }
}
