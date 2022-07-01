/**
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

import static org.junit.Assert.*;

import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.web.TypedInputStream;
import static org.apache.jena.fuseki.test.HttpTest.*;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.test.conn.EnvTest;
import org.apache.jena.web.HttpSC;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link HttpOp} testing, and including {@link HttpOp} used directly for SPARQL operations.
 * Includes error cases and unusual usage that the higher level APIs may not use but are correct.
 */
public class TestHttpOp {

    // replacement.
    private static EnvTest env;
    private static DatasetGraph dsg = DatasetGraphFactory.createTxnMem();

    @BeforeClass public static void beforeClass() {
        env = EnvTest.create("/ds");
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    // Includes the trailing "/" so it is correct in itself.
    private static String urlRoot() { return env.serverBaseURL(); }

    /** URL : server and path */
    private static String url(String path) { return env.serverPath(path); }


    /** URL : server, dataset name and path */
    private static String datasetPath(String path) {
        if ( ! path.startsWith("/") )
            path = "/"+path;
        return url(env.dsName())+path;
    }

    /** URL: the string holder. */
    private String PLAIN() { return env.stringHolderPath(); }

    @Before public void before() {
        env.clear();
    }

    // Standard Fuseki names.
    static String pingURL()         { return url("/$/ping"); }
    static String gspServiceURL()   { return datasetPath("/data"); }

    static String defaultGraphURL() { return gspServiceURL()+"?default"; }
    static String namedGraphURL()   { return gspServiceURL()+"?graph=http://example/g"; }
    static String sparqlURL()       { return datasetPath("sparql"); }    // No dataset
    static String queryURL()        { return datasetPath("query"); }
    static String updateURL()       { return datasetPath("update"); }

    static String simpleQuery() { return sparqlURL()+"?query="+IRILib.encodeUriComponent("ASK{}"); }

    // Basic operations

    @Test public void httpGet_01() {
        assertNotNull(HttpOp.httpGetString(pingURL()));
    }

    @Test public void httpGet_02() {
        expect404(() -> HttpOp.httpGet(urlRoot() + "does-not-exist"));
    }

    @Test public void httpGet_03() {
        assertNotNull(HttpOp.httpGetString(pingURL()));
    }

    @Test public void httpGet_04() {
        String x = HttpOp.httpGetString(urlRoot()+"does-not-exist");
        assertNull(x);
    }

    @Test public void httpGet_05() {
        assertNotNull(HttpOp.httpGetString(simpleQuery()));
    }

    // GET, POST, PUT, DELETE

    @Test public void httpREST_get_1() {
        assertNotNull(HttpOp.httpGetString(PLAIN()));
    }

    @Test public void httpREST_put_1() {
        HttpOp.httpPut(PLAIN(), WebContent.contentTypeTextPlain, HttpLib.stringBody("Hello"));
        assertEquals("Hello", HttpOp.httpGetString(PLAIN()));
    }

    @Test public void httpREST_post_1() {
        HttpOp.httpPut(PLAIN(), WebContent.contentTypeTextPlain, HttpLib.stringBody("Hello"));
        HttpOp.httpPost(PLAIN(), WebContent.contentTypeTextPlain, HttpLib.stringBody(" "));
        HttpOp.httpPost(PLAIN(), WebContent.contentTypeTextPlain, HttpLib.stringBody("World"));
        assertEquals("Hello World", HttpOp.httpGetString(PLAIN()));
    }

    @Test public void httpREST_delete_1() {
        HttpOp.httpPut(PLAIN(), WebContent.contentTypeTextPlain, HttpLib.stringBody("Hello"));
        HttpOp.httpDelete(PLAIN());
        assertEquals("", HttpOp.httpGetString(PLAIN()));
    }

    // SPARQL Query like.

    @Test public void queryGet_01() {
        assertNotNull(HttpOp.httpGetString(simpleQuery()));
    }

    @Test public void queryGet_02() {
        // No query.
        execWithHttpException(HttpSC.BAD_REQUEST_400, () -> HttpOp.httpGetString(sparqlURL() + "?query="));
    }

    // Conneg always produces an answer, whether in the accept or not.
    //@Test
    public void httpPost_01() {
        execWithHttpException(HttpSC.UNSUPPORTED_MEDIA_TYPE_415,
                () -> HttpOp.httpPost(sparqlURL(), "text/plain", BodyPublishers.ofString("ASK{}")));
    }

    //@Test
    public void httpPost_03() {
        execWithHttpException(HttpSC.UNSUPPORTED_MEDIA_TYPE_415,
                () -> HttpOp.httpPost(sparqlURL(), WebContent.contentTypeOctets, BodyPublishers.ofString("ASK{}")));
    }

    @Test public void httpPostForm_01() {
        Params params = Params.create().add("query", "ASK{}");
        try ( TypedInputStream in = HttpOp.httpPostForm(sparqlURL(), params, WebContent.contentTypeResultsJSON) ) {
            assertEquals(WebContent.contentTypeResultsJSON, in.getContentType());
            IO.readWholeFile(in);
        }
    }

    @Test public void httpPostForm_02() {
        Params params = Params.create().add("query", "ASK{}");
        // Query to Update
        execWithHttpException(HttpSC.BAD_REQUEST_400,
                () -> HttpOp.httpPostForm(updateURL(), params, WebContent.contentTypeResultsJSON));
    }

    @Test public void httpPostForm_03() {
        Params params = Params.create().add("update", "CLEAR ALL");
        // Update to Query
        execWithHttpException(HttpSC.BAD_REQUEST_400,
            ()->HttpOp.httpPostForm(queryURL(), params, "*/*"));
    }

    @Test public void httpPostForm_04() {
        Params params = Params.create().add("update", "CLEAR ALL");
        // Update request to update endpoint.
        HttpOp.httpPostForm(updateURL(), params, "*/*");
    }

    @Test public void httpPostStream_01() {
        try(TypedInputStream in = HttpOp.httpPostStream(pingURL())) {}
    }

    private static BodyPublisher graphString() { return BodyPublishers.ofString("PREFIX : <http://example/> :s :p :o ."); }

    private static BodyPublisher datasetString() {return BodyPublishers.ofString("PREFIX : <http://example/> :s :p :o . :g { :sg :pg :og }"); }

    // The HTTP actions that go with GSP.

    @Test public void http_gsp_01() {
        String x = HttpOp.httpGetString(defaultGraphURL(), "application/rdf+xml");
        assertTrue(x.contains("</"));
        assertTrue(x.contains(":RDF"));
    }

    @Test public void http_gsp_02() {
        String x = HttpOp.httpGetString(defaultGraphURL(), "application/n-triples");
        assertTrue(x.isEmpty());
    }

    @Test public void http_gsp_03() {
        HttpOp.httpPut(defaultGraphURL(), WebContent.contentTypeTurtle, graphString());
        String s1 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());
    }

    @Test public void http_gsp_04() {
        HttpOp.httpPut(defaultGraphURL(), WebContent.contentTypeTurtle, graphString());
        String s1 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());
        HttpOp.httpDelete(defaultGraphURL());
        String s2 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertTrue(s2.isEmpty());
    }

    @Test public void http_gsp_05() {
        HttpOp.httpDelete(defaultGraphURL());

        HttpOp.httpPost(defaultGraphURL(), WebContent.contentTypeTurtle, graphString());
        String s1 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());
        HttpOp.httpDelete(defaultGraphURL());
        String s2 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertTrue(s2.isEmpty());
    }

    @Test public void http_gsp_06() {
        HttpOp.httpPost(namedGraphURL(), WebContent.contentTypeTurtle, graphString());
        String s1 = HttpOp.httpGetString(namedGraphURL(), WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());

        String s2 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertTrue(s2.isEmpty());

        HttpOp.httpDelete(namedGraphURL());
        String s3 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertTrue(s3.isEmpty());

        expect404(()->HttpOp.httpDelete(namedGraphURL()));
    }

    @Test public void http_gsp_10() {
        HttpOp.httpDelete(defaultGraphURL());
    }

    // Extended GSP - no ?default, no ?graph acts on the datasets as a whole.
    @Test public void http_gsp_12() {
        execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, () -> HttpOp.httpDelete(gspServiceURL()));
    }

    @Test public void http_gsp_20() {
        String s1 = HttpOp.httpGetString(gspServiceURL(), WebContent.contentTypeNQuads);
        assertNotNull("Got 404 (via null)", s1);
        assertTrue(s1.isEmpty());

        HttpOp.httpPost(gspServiceURL(), WebContent.contentTypeTriG, datasetString());
        String s2 = HttpOp.httpGetString(gspServiceURL(), WebContent.contentTypeNQuads);
        assertFalse(s2.isEmpty());

        String s4 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        assertFalse(s4.isEmpty());
    }
}

