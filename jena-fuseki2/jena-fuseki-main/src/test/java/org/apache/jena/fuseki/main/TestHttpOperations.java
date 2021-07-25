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


import static java.net.http.HttpRequest.BodyPublishers.ofString;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.http.HttpOp;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.util.Convert;
import org.junit.Assert;
import org.junit.Test;

/** Operation by HTTP - test dispatch - lower level than TestSPARQLProtocol */
public class TestHttpOperations extends AbstractFusekiTest {

    @Test
    public void query_by_get_1() {
        String qs = Convert.encWWWForm("ASK{}");
        String u = serviceQuery()+"?query=" + qs;
        try (TypedInputStream in = HttpOp.httpGet(u)) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void query_by_post_1() {
        //String url, String contentType, String content, String acceptType
        try (TypedInputStream in = HttpOp.httpPostStream(serviceQuery(),
                                                          WebContent.contentTypeSPARQLQuery, ofString("ASK{}"),
                                                          "*")) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void query_by_post_2() {
        String qs = Convert.encWWWForm("ASK{}");
        String u = serviceQuery()+"?query=" + qs;
        try (TypedInputStream in = HttpOp.httpPostStream(u)) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void query_by_form_1() {
        Params params = Params.create().add("query", "ASK{}");
        try (TypedInputStream in = HttpOp.httpPostForm(serviceQuery(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    @Test(expected=HttpException.class)
    public void query_by_form_2() {
        Params params = Params.create().add("foobar", "ASK{}");    // Wrong.
        try (TypedInputStream in = HttpOp.httpPostForm(serviceQuery(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void update_by_post_1() {
        HttpOp.httpPost(serviceUpdate(), WebContent.contentTypeSPARQLUpdate, ofString("INSERT DATA{}"));
    }

    // POST ?request= :: Not supported.
//    @Test
//    public void update_by_post_2() {
//        String us = Convert.encWWWForm("INSERT DATA {}");
//        String u = serviceUpdate+"?update=" + us;
//        try (TypedInputStream in = HttpOp.execHttpPostStream(u, null, null)) {
//            Assert.assertNotNull(in);
//        }
//    }

    @Test
    public void update_by_form_1() {
        Params params = Params.create().add("update", "INSERT DATA{}");
        try (TypedInputStream in = HttpOp.httpPostForm(serviceUpdate(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    @Test(expected=HttpException.class)
    public void update_by_form_2() {
        Params params = Params.create().add("query", "INSERT DATA{}");  // Wrong paramater
        try (TypedInputStream in = HttpOp.httpPostForm(serviceUpdate(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    // ---- Dataset direct, with content type.

    @Test
    public void ds_fetch_by_get_1() {
        String u = databaseURL();
        try (TypedInputStream in = HttpOp.httpGet(u)) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void ds_query_by_post_1() {
        String u = databaseURL();
        try (TypedInputStream in = HttpOp.httpPostStream(u, WebContent.contentTypeSPARQLQuery, ofString("ASK{}"), "*")) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void ds_update_by_post_1() {
        String u = databaseURL();
        HttpOp.httpPost(u, WebContent.contentTypeSPARQLUpdate, ofString("INSERT DATA{}"));
    }
}
