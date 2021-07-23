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


import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.engine.http.Params;
import org.apache.jena.sparql.util.Convert;
import org.junit.Assert;
import org.junit.Test;

/** Operation by HTTP - test dispatch - lower level than TestSPARQLProtocol */
public class TestHttpOperations extends AbstractFusekiTest {

    @Test
    public void query_by_get_1() {
        String qs = Convert.encWWWForm("ASK{}");
        String u = serviceQuery()+"?query=" + qs;
        try (TypedInputStream in = HttpOp.execHttpGet(u)) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void query_by_post_1() {
        try (TypedInputStream in = HttpOp.execHttpPostStream(serviceQuery(), WebContent.contentTypeSPARQLQuery, "ASK{}", "*")) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void query_by_post_2() {
        String qs = Convert.encWWWForm("ASK{}");
        String u = serviceQuery()+"?query=" + qs;
        try (TypedInputStream in = HttpOp.execHttpPostStream(u, null, null)) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void query_by_form_1() {
        Params params = new Params().addParam("query", "ASK{}");
        try (TypedInputStream in = HttpOp.execHttpPostFormStream(serviceQuery(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    @Test(expected=HttpException.class)
    public void query_by_form_2() {
        Params params = new Params().addParam("foobar", "ASK{}");    // Wrong.
        try (TypedInputStream in = HttpOp.execHttpPostFormStream(serviceQuery(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void update_by_post_1() {
        HttpOp.execHttpPost(serviceUpdate(), WebContent.contentTypeSPARQLUpdate, "INSERT DATA{}");
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
        Params params = new Params().addParam("update", "INSERT DATA{}");
        try (TypedInputStream in = HttpOp.execHttpPostFormStream(serviceUpdate(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    @Test(expected=HttpException.class)
    public void update_by_form_2() {
        Params params = new Params().addParam("query", "INSERT DATA{}");  // Wrong paramater
        try (TypedInputStream in = HttpOp.execHttpPostFormStream(serviceUpdate(), params, "*") ) {
            Assert.assertNotNull(in);
        }
    }

    // ---- Dataset direct, with content type.

    @Test
    public void ds_fetch_by_get_1() {
        String u = databaseURL();
        try (TypedInputStream in = HttpOp.execHttpGet(u)) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void ds_query_by_post_1() {
        String u = databaseURL();
        try (TypedInputStream in = HttpOp.execHttpPostStream(u, WebContent.contentTypeSPARQLQuery, "ASK{}", "*")) {
            Assert.assertNotNull(in);
        }
    }

    @Test
    public void ds_update_by_post_1() {
        String u = databaseURL();
        HttpOp.execHttpPost(u, WebContent.contentTypeSPARQLUpdate, "INSERT DATA{}");
    }
}
