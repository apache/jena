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

package org.apache.jena.fuseki.main.prefixes;

import static org.apache.jena.http.HttpOp.httpDelete;
import static org.apache.jena.http.HttpOp.httpGetString;
import static org.apache.jena.http.HttpOp.httpPostForm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.PrefixesService;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.web.HttpSC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestPrefixesService {

    private FusekiServer server = null;
    private String serviceURL = null;
    String serviceUpdateURL = null;

    @BeforeEach
    public void before() {
        PrefixesService.init();
        String DATASET = "dataset";
        server = FusekiServer.create()
                .port(0)
                .parseConfigFile("src/test/java/org/apache/jena/fuseki/main/files/config-prefixes-test.ttl")
                .start();

        int port = server.getHttpPort();
        serviceURL = "http://localhost:"+port+"/"+DATASET+"/prefixes";
        serviceUpdateURL = "http://localhost:"+port+"/"+DATASET+"/updatePrefixes";
    }

    @AfterEach
    public void afterSuite() {
        if ( server != null )
            server.stop();
    }

// FETCH URI tests
// ---------------------------------------------------------------------------------------------

    @Test
    public void fetchURILegal() {
        // request legal, prefix does not exist implies return ""
        String url = String.format(serviceURL, server.getHttpPort());
        String x = exec(url, "?prefix=abc");
        assertEquals("", x, "Expected empty string");
    }

    @Test
    public void fetchURIBadArgument() {
        // Bad argument, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?junk");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchURIEmptyPrefix0() {
        // Empty prefix, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?prefix=");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchURIEmptyPrefix1() {
        // Empty prefix, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?prefix");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchURIIllegalChars0() {
        // illegal prefix, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?prefix=pr.");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

// UPDATE URI tests
// ---------------------------------------------------------------------------------------------

    @Test
    public void updateURINewLegal0() {
        // valid new prefix, valid new uri, implies return uri3
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(url, "prefix3", "http://www.localhost.org/uri3");
        String x = exec(url, "?prefix=prefix3");
        assertEquals("http://www.localhost.org/uri3", x, "Expected http://www.localhost.org/uri3 got " + x);
    }

    @Test
    public void updateURINewLegal1() {
        // valid new prefix, existing uri, implies return uri3
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(url, "prefix4", "http://www.localhost.org/uri3");
        String x = exec(url, "?prefix=prefix4");
        assertEquals("http://www.localhost.org/uri3", x, "Expected http://www.localhost.org/uri3 got " + x);
    }

    @Test
    public void updateURINewLegal2() {
        // existing prefix, valid new uri, implies return uri7
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(url, "prefix1", "http://www.localhost.org/uri7");
        String x = exec(url, "?prefix=prefix1");
        assertEquals("http://www.localhost.org/uri7", x, "Expected http://www.localhost.org/uri7 got " + x);
    }

    @Test
    public void updateURINewLegal3() {
        // existing prefix, existing uri, implies return uri2
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(url, "prefix2", "http://www.localhost.org/uri2");
        String x = exec(url, "?prefix=prefix2");
        assertEquals("http://www.localhost.org/uri2", x, "Expected http://www.localhost.org/uri2 got " + x);
    }

    @Test
    public void updateValidNewPrefixInvalidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            //valid new prefix, invalid uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix4", "-.-");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateValidNewPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            //valid new prefix, empty uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix4", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateValidNewPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // valid new prefix, null uri (bad argument), 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix4", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateExistingPrefixInvalidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // existing prefix, invalid uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix1", "http:abc");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateExistingPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // existing prefix, empty uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix1", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateExistingPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // existing prefix null uri (bad argument), 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix1", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixNewValidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, valid new uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix..-", "http://www.localhost.org/uri7");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixExistingURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, existing uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "-refix..1", "http://www.localhost.org/uri1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixInvalidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, invalid uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "-", "http://");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, empty uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "p/p", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, null uri (bad argument), 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "prefix..-", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixValidNewURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, valid new uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, "", "http://www.localhost.org/uri5");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixExistingURI() {
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        // empty prefix, existing uri, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(url, "", "http://www.localhost.org/uri1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixEInvalidURI() {
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        // empty prefix, invalid uri, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(url, "", "http:abcur..i1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixEmptyURI() {
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        // empty prefix, empty uri, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(url, "", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixNullURI() {
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        // empty prefix, null uri (bad argument), 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(url, "", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixValidNewURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), valid new uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, null, "http://www.localhost.org/uri6");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixExistingURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), existing uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, null, "http://www.localhost.org/uri1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixInValidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), invalid uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, null, "...");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), empty uri, 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, null, "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), null uri (bad argument), 400 bad request exception expected
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execPost(url, null, null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

// GET ALL tests
// ---------------------------------------------------------------------------------------------

    @Test
    public void getAllLegal() {
        // request legal, returns multiple prefix-uri pairs
        String url = String.format(serviceURL, server.getHttpPort());
        String postURL = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(postURL, "test", "http://www.localhost.org/uritest");
        execPost(postURL, "test2", "http://www.localhost.org/uritest2");
        execPost(postURL, "test3", "http://www.localhost.org/uritest3");
        String x = exec(url, "");

        // convert to set
        Set<String> set = new HashSet<>();
        set.add("{\"prefix\":\"test\",\"uri\":\"http://www.localhost.org/uritest\"}");
        set.add("{\"prefix\":\"test2\",\"uri\":\"http://www.localhost.org/uritest2\"}");
        set.add("{\"prefix\":\"test3\",\"uri\":\"http://www.localhost.org/uritest3\"}");

        Set<String> resultSet = new HashSet<>();
        JsonArray jsonArray = JsonParser.parseString(x).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            resultSet.add(element.getAsJsonObject().toString());
        }
        assertEquals(set, resultSet, "Expected prefix");
    }

    @Test
    public void getAllEmpty() {
        // request legal, dataset empty implies return []
        String url = String.format(serviceURL, server.getHttpPort());
        String x = exec(url, "");
        assertEquals("[]", x, "Expected prefix");
    }

// FETCH PREFIX tests
// ---------------------------------------------------------------------------------------------

    @Test
    public void fetchPrefixLegal() {
        // request legal, uri exists in the database with a single prefix assigned
        String url = String.format(serviceURL, server.getHttpPort());
        String postURL = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(postURL, "test", "http://www.localhost.org/uritest");
        String x = exec(url, "?uri=http://www.localhost.org/uritest");
        assertEquals("[{\"prefix\":\"test\",\"uri\":\"http://www.localhost.org/uritest\"}]", x, "Expected prefix");
    }

    @Test
    public void fetchPrefixLegalMultiple() {
        // request legal, uri exists in the database with multiple prefixes assigned
        String url = String.format(serviceURL, server.getHttpPort());
        String postURL = String.format(serviceUpdateURL, server.getHttpPort());
        execPost(postURL, "test", "http://www.localhost.org/uritest");
        execPost(postURL, "testDuplicate", "http://www.localhost.org/uritest");
        String x = exec(url, "?uri=http://www.localhost.org/uritest");

        // convert to set
        Set<String> set = new HashSet<>();
        set.add("{\"prefix\":\"test\",\"uri\":\"http://www.localhost.org/uritest\"}");
        set.add("{\"prefix\":\"testDuplicate\",\"uri\":\"http://www.localhost.org/uritest\"}");

        Set<String> resultSet = new HashSet<>();
        JsonArray jsonArray = JsonParser.parseString(x).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            resultSet.add(element.getAsJsonObject().toString());
        }
        assertEquals(set, resultSet, "Expected prefix");
    }

    @Test
    public void fetchPrefixLegalNull() {
        // request legal, uri does not exist implies return []
        String url = String.format(serviceURL, server.getHttpPort());
        String x = exec(url, "?uri=http://www.localhost.org/uritest");
        assertEquals("[]", x, "Expected prefix");
    }

    @Test
    public void fetchPrefixIllegal() {
        // request illegal, uri is not valid
        HttpException ex = assertThrows(HttpException.class, ()-> {
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?uri=----localhost.org/uritest");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchPrefixEmpty() {
        // request illegal, uri is empty
        HttpException ex = assertThrows(HttpException.class, ()-> {
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?uri=");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void tooManyParams() {
        // request illegal, provided too many arguments
        HttpException ex = assertThrows(HttpException.class, ()-> {
            String url = String.format(serviceURL, server.getHttpPort());
            String x = exec(url, "?prefix=abc?uri=cde");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    // REMOVE HTTP DELETE tests
// ---------------------------------------------------------------------------------------------
    @Test
    public void deleteURI0() {
        // request legal, prefix exists, implies return null
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execDelete(url, "?prefix=prefix1");
        String x = exec(url, "?prefix=prefix1");
        assertEquals("", x, "Expected null got " + x);
    }

    @Test
    public void deleteURI1() {
        // request legal, prefix doesn't exist implies return null
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execDelete(url, "?prefix=prefix16");
        String x = exec(url, "?prefix=prefix16");
        assertEquals("", x, "Expected null got " + x);
    }

    @Test
    public void deleteURI2() {
        // request illegal, prefix invalid, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String url = String.format(serviceUpdateURL, server.getHttpPort());
            execDelete(url, "?prefix=prefix16-");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void deleteURI3() {
        // request legal, prefix exists implies return null
        String url = String.format(serviceUpdateURL, server.getHttpPort());
        execDelete(url, "?prefix=prefix2");
        String x = exec(url, "?prefix=prefix2");
        assertEquals("", x, "Expected null got " + x);
    }
// ---------------------------------------------------------------------------------------------

    private static String exec(String url, String queryString) {
        String urlExec = queryString.startsWith("?")
                ? url + queryString
                : url + "?" + queryString;
        return httpGetString(urlExec);
    }

    private static void execPost(String url, String prefix, String uri) {
        Params params = Params.create()
                .add("prefix", prefix)
                .add("uri", uri);
        httpPostForm(url, params);
    }

    private static void execDelete(String url, String queryString) {
        String urlExec = queryString.startsWith("?")
                ? url + queryString
                : url+"?"+queryString;
        httpDelete(urlExec);
    }
}
