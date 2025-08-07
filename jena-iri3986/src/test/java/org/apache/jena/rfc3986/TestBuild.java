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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

/** Building IRIs from components. */
public class TestBuild {

    @Test public void build_01() {
        testBuild("http://host/abc/def?qs=ghi#jkl", "http", "host", "/abc/def", "qs=ghi", "jkl");
    }

    @Test public void build_02() {
        testBuild("http://host/abc/def", "http", "host", "/abc/def", null, null);
    }

    @Test public void build_03() {
        IRI3986 iri =
            RFC3986.newBuilder()
                .scheme("http")
                .host("host")
                .path("/abc")
                .build();
        assertEquals("http://host/abc", iri.toString());
    }

    @Test public void build_04() {
        IRI3986 iri =
            RFC3986.newBuilder()
                .scheme("http")
                .authority("AUTH")  // Replaced.
                .host("host")
                .port(8080)
                .path("/abc")
                .build();
        assertEquals("http://host:8080/abc", iri.toString());
    }

    @Test public void build_05() {
        IRI3986 iri =
            RFC3986.newBuilder()
                .scheme("http")
                .host("host")
                .port(8080)
                .authority("AUTH")
                .path("/abc")
                .build();
        assertEquals("http://AUTH/abc", iri.toString());
    }

    @Test public void build_06() {
        IRI3986 iri =
            RFC3986.newBuilder()
                .scheme("http")
                .authority("AUTH")
                .path("/abc")
                .query("a=b")
                .fragment("frag")
                .build();
        assertEquals("http://AUTH/abc?a=b#frag", iri.toString());
    }



    private void testBuild(String expected, String scheme, String authority, String path, String query, String fragment) {
        // All parts
        IRI3986 iri =
            RFC3986.newBuilder()
                .scheme(scheme)
                .authority(authority)
                .path(path)
                .query(query)
                .fragment(fragment)
                .build();
        assertEquals(expected, iri.toString());
    }

}
