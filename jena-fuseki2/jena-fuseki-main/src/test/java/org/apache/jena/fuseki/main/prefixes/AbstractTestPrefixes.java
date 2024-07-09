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

import static org.apache.jena.http.HttpOp.httpGetString;
import static org.apache.jena.http.HttpOp.httpPostForm;
import static org.apache.jena.riot.WebContent.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.web.HttpSC;
import org.junit.jupiter.api.Test;

/** The tests to run. */
public abstract class AbstractTestPrefixes {

    protected abstract String testReadURL();
    protected abstract String testWriteURL();

// FETCH URI tests
// ---------------------------------------------------------------------------------------------

    // UPDATE URI tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void fetchURILegal() {
        // request legal, prefix does not exist implies return ""
        testGetByPrefix(testReadURL(), "?prefix=abc",
                        "{}", "");
    }

    @Test
    public void fetchURIBadArgument() {
        // Bad argument, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String x = execGet(testReadURL(), "?junk");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchURIEmptyPrefix0() {
        // Empty prefix, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String x = execGet(testReadURL(), "?prefix=");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchURIEmptyPrefix1() {
        // Empty prefix, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String x = execGet(testReadURL(), "?prefix", contentTypeJSON);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchURIIllegalChars0() {
        // illegal prefix, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            String x = execGet(testReadURL(), "?prefix=pr.", contentTypeJSON);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    // UPDATE URI tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void updateURINewLegal0() {
        // valid new prefix, valid new uri, implies return uri3
        execPost(testWriteURL(), "prefix3", "http://www.localhost.org/uri3");

        testGetByPrefix(testReadURL(), "?prefix=prefix3",
                        "{\"prefix\":\"prefix3\",\"uri\":\"http://www.localhost.org/uri3\"}",
                        "http://www.localhost.org/uri3");
    }

    @Test
    public void updateURINewLegal1() {
        // valid new prefix, existing uri, implies return uri3
        execPost(testWriteURL(), "prefix4", "http://www.localhost.org/uri3");

        testGetByPrefix(testReadURL(), "?prefix=prefix4",
                        "{\"prefix\":\"prefix4\",\"uri\":\"http://www.localhost.org/uri3\"}",
                        "http://www.localhost.org/uri3");
    }

    @Test
    public void updateURINewLegal2() {
        // existing prefix, valid new uri, implies return uri7

        execPost(testWriteURL(), "prefix1", "http://www.localhost.org/uri6");
        execPost(testWriteURL(), "prefix1", "http://www.localhost.org/uri7");

        testGetByPrefix(testReadURL(), "?prefix=prefix1",
                        "{\"prefix\":\"prefix1\",\"uri\":\"http://www.localhost.org/uri7\"}",
                        "http://www.localhost.org/uri7");
    }

    @Test
    public void updateURINewLegal3() {
        // existing prefix, existing uri, implies return uri2
        execPost(testWriteURL(), "prefix2", "http://www.localhost.org/uri2");
        execPost(testWriteURL(), "prefix2", "http://www.localhost.org/uri2");

        testGetByPrefix(testReadURL(), "?prefix=prefix2",
                        "{\"prefix\":\"prefix2\",\"uri\":\"http://www.localhost.org/uri2\"}",
                        "http://www.localhost.org/uri2");
    }

    @Test
    public void updateValidNewPrefixInvalidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            //valid new prefix, invalid uri, 400 bad request exception expected
            execPost(testWriteURL(), "prefix4", "-.-");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateValidNewPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            //valid new prefix, empty uri, 400 bad request exception expected
            execPost(testWriteURL(), "prefix4", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateValidNewPrefixNoURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // valid new prefix, null uri (bad argument), 400 bad request exception expected
            execPost(testWriteURL(), "?prefix=prefix4", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateExistingPrefixInvalidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // existing prefix, invalid uri, 400 bad request exception expected
            execPost(testWriteURL(), "prefix1", "http:abc");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateExistingPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // existing prefix, empty uri, 400 bad request exception expected

            execPost(testWriteURL(), "prefix1", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateExistingPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // existing prefix null uri (bad argument), 400 bad request exception expected

            execPost(testWriteURL(), "prefix1", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixNewValidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, valid new uri, 400 bad request exception expected

            execPost(testWriteURL(), "prefix..-", "http://www.localhost.org/uri7");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixExistingURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, existing uri, 400 bad request exception expected

            execPost(testWriteURL(), "-refix..1", "http://www.localhost.org/uri1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixInvalidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, invalid uri, 400 bad request exception expected

            execPost(testWriteURL(), "-", "http://");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, empty uri, 400 bad request exception expected

            execPost(testWriteURL(), "p/p", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateInvalidPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, null uri (bad argument), 400 bad request exception expected

            execPost(testWriteURL(), "prefix..-", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixValidNewURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // invalid prefix, valid new uri, 400 bad request exception expected

            execPost(testWriteURL(), "", "http://www.localhost.org/uri5");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixExistingURI() {

        // empty prefix, existing uri, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(testWriteURL(), "", "http://www.localhost.org/uri1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixEInvalidURI() {

        // empty prefix, invalid uri, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(testWriteURL(), "", "http:abcur..i1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixEmptyURI() {

        // empty prefix, empty uri, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(testWriteURL(), "", "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateEmptyPrefixNullURI() {

        // empty prefix, null uri (bad argument), 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execPost(testWriteURL(), "", null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixValidNewURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), valid new uri, 400 bad request exception expected

            execPost(testWriteURL(), null, "http://www.localhost.org/uri6");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixExistingURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), existing uri, 400 bad request exception expected

            execPost(testWriteURL(), null, "http://www.localhost.org/uri1");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixInValidURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), invalid uri, 400 bad request exception expected

            execPost(testWriteURL(), null, "...");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixEmptyURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), empty uri, 400 bad request exception expected

            execPost(testWriteURL(), null, "");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void updateNullPrefixNullURI() {
        HttpException ex = assertThrows(HttpException.class, ()->{
            // null prefix (bad argument), null uri (bad argument), 400 bad request exception expected

            execPost(testWriteURL(), null, null);
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    // DELETE URI tests
    // ---------------------------------------------------------------------------------------------
    @Test
    public void deleteURI0() {
        // request legal, prefix exists, implies return null

        execDelete(testWriteURL(), "prefix1");
        String x = execGet(testReadURL(), "?prefix=prefix1", contentTypeTextPlain);
        assertEquals("", x, "Expected empty string got " + x);
    }

    @Test
    public void deleteURI1() {
        // request legal, prefix doesn't exist implies return null
        execDelete(testWriteURL(), "prefix16");
        testGetByPrefix(testReadURL(), "?prefix=prefix16", "{}", "");
    }

    @Test
    public void deleteURI2() {
        // request illegal, prefix invalid, 400 bad request exception expected
        HttpException ex = assertThrows(HttpException.class, ()->{
            execDelete(testWriteURL(), "prefix16-");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void deleteURI3() {
        // request legal, prefix exists implies return null
        execDelete(testWriteURL(), "prefix2");
        testGetByPrefix(testReadURL(), "?prefix=prefix2", "{}", "");
    }

    // GET All tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void getAllLegal() {
        // request legal, returns multiple prefix-uri pairs

        execPost(testWriteURL(), "test", "http://www.localhost.org/uritest");
        execPost(testWriteURL(), "test2", "http://www.localhost.org/uritest2");
        execPost(testWriteURL(), "test3", "http://www.localhost.org/uritest3");
        String x = execGet(testReadURL(), "");

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
        String x = execGet(testReadURL(), "");
        assertEquals("[]", x, "Expected prefix");
    }

    // FETCH PREFIX tests
    // ---------------------------------------------------------------------------------------------

    @Test
    public void fetchPrefixLegal() {
        // request legal, uri exists in the database with a single prefix assigned
        execPost(testWriteURL(), "test", "http://www.localhost.org/uritest");
        String x = execGet(testReadURL(), "?uri=http://www.localhost.org/uritest");
        assertEquals("[{\"prefix\":\"test\",\"uri\":\"http://www.localhost.org/uritest\"}]", x, "Expected prefix");
    }

    @Test
    public void fetchPrefixLegalMultiple() {
        // request legal, uri exists in the database with multiple prefixes assigned

        execPost(testWriteURL(), "test", "http://www.localhost.org/uritest");
        execPost(testWriteURL(), "testDuplicate", "http://www.localhost.org/uritest");
        String x = execGet(testReadURL(), "?uri=http://www.localhost.org/uritest");

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

        String x = execGet(testReadURL(), "?uri=http://www.localhost.org/uritest");
        assertEquals("[]", x, "Expected prefix");
    }

    @Test
    public void fetchPrefixIllegal() {
        // request illegal, uri is not valid
        HttpException ex = assertThrows(HttpException.class, ()-> {

            String x = execGet(testReadURL(), "?uri=----localhost.org/uritest");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void fetchPrefixEmpty() {
        // request illegal, uri is empty
        HttpException ex = assertThrows(HttpException.class, ()-> {

                    String x = execGet(testReadURL(), "?uri=");
                });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    @Test
    public void tooManyParams() {
        // request illegal, provided too many arguments
        HttpException ex = assertThrows(HttpException.class, ()-> {

            String x = execGet(testReadURL(), "?prefix=abc&uri=http://www.localhost.org/uritest");
        });
        assertEquals(HttpSC.BAD_REQUEST_400, ex.getStatusCode());
    }

    // ---------------------------------------------------------------------------------------------

    private static String execGet(String url, String queryString) {
        return execGet(url, queryString, contentTypeJSON);
    }
    private static String execGet(String url, String queryString, String acceptHeader) {
        String urlExec = queryString.startsWith("?")
                ? url + queryString
                : url + "?" + queryString;
        return acceptHeader != null
                ? httpGetString(urlExec, acceptHeader)
                : httpGetString(urlExec);
    }
    private static void execPost(String url, String prefix, String uri) {
        Params params = Params.create().add("prefix", prefix);
        if ( uri != null )
                params.add("uri", uri);
        httpPostForm(url, params);
    }
    private static void execDelete(String url, String prefix) {
        String urlExec = url+"?prefix="+prefix;
        HttpOp.httpDelete(urlExec);
    }
    private static void assertEqualsJson(String expectedStr, String actualStr, String msg) {
        JsonElement expected = JsonParser.parseString(expectedStr);
        JsonElement actual = JsonParser.parseString(expectedStr);
        assertEquals(actual, expected, msg);
    }

    // FETCH URI tests
    // ---------------------------------------------------------------------------------------------

        // UPDATE URI tests
        // ---------------------------------------------------------------------------------------------

        // test the results of a GET by prefix with both plain text and json.
        private void testGetByPrefix(String testReadURL, String prefixString, String expectedJSON, String expectedText) {
            testGetByPrefixJSON(testReadURL, prefixString, expectedJSON);
            testGetByPrefixText(testReadURL, prefixString, expectedText);
        }
    private void testGetByPrefixJSON(String testReadURL, String prefixString, String expectedJSON) {
        String x1 = execGet(testReadURL(), prefixString, contentTypeJSON);
        assertEqualsJson(expectedJSON, x1, "Expected '" + expectedJSON + "' got '" + x1 + "'");
    }
    private void testGetByPrefixText(String testReadURL, String prefixString, String expectedText) {
        String x2 = execGet(testReadURL(), prefixString, contentTypeTextPlain);
        assertEquals(expectedText, x2, "Expected '" + expectedText + "' got " + x2 + "'");
    }
}
