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

package org.apache.jena.riot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.web.ContentType;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestSyntaxDetermination {

    public static Stream<Arguments> provideArgs() {
        List<Arguments> x = new ArrayList<>();
        //     marker               url                          contentType     hintLang,     expected
        add(x, "Test-ext-ttl-1",    "http://sparql.org/D.ttl",   "text/turtle",  Lang.TTL,     Lang.TTL);
        add(x, "Test-ext-ttl-2",    "http://sparql.org/D.ttl",   "text/turtle",  Lang.RDFXML,  Lang.TTL);
        add(x, "Test-ext-ttl-3",    "http://sparql.org/D.ttl",   "text/plain",   null,         Lang.TTL);
        add(x, "Test-ext-ttl-4",    "http://sparql.org/D.ttl",   "text/plain",   Lang.RDFXML,  Lang.RDFXML);
        add(x, "Test-ext-ttl-5",    "http://sparql.org/D.ttl",   null,           Lang.TTL,     Lang.TTL);
        add(x, "Test-ext-ttl-6",    "http://sparql.org/D.ttl",   null,           null,         Lang.TTL);

        add(x, "Test-no-ext-1",     "http://sparql.org/D",       "text/turtle",  Lang.TTL,     Lang.TTL);
        add(x, "Test-no-ext-2",     "http://sparql.org/D",       "text/turtle",  Lang.RDFXML,  Lang.TTL);
        add(x, "Test-no-ext-3",     "http://sparql.org/D",       "text/plain",   null,         null);
        add(x, "Test-no-ext-4",     "http://sparql.org/D",       "text/plain",   Lang.RDFXML,  Lang.RDFXML);
        add(x, "Test-no-ext-5",     "http://sparql.org/D",       null,          Lang.NT,      Lang.NT);
        add(x, "Test-no-ext-6",     "http://sparql.org/D",       null,           null,         null);

        add(x, "Test-ext-rdf-1",    "http://sparql.org/D.rdf",  "text/turtle",  Lang.TTL,     Lang.TTL);
        add(x, "Test-ext-rdf-2",    "http://sparql.org/D.rdf",  "text/turtle",  Lang.RDFXML,  Lang.TTL);
        add(x, "Test-ext-rdf-3",    "http://sparql.org/D.rdf",  "text/plain",   null,         Lang.RDFXML);
        add(x, "Test-ext-rdf-4",    "http://sparql.org/D.rdf",  "text/plain",   Lang.RDFXML,  Lang.RDFXML);
        add(x, "Test-ext-rdf-5",    "http://sparql.org/D.rdf",  null,           Lang.TTL,     Lang.TTL);
        add(x, "Test-ext-rdf-6",    "http://sparql.org/D.rdf",  null,           null,         Lang.RDFXML);

        add(x, "Test-unknown-ext-1", "http://sparql.org/D.xyz", "text/turtle",  Lang.TTL,     Lang.TTL);
        add(x, "Test-unknown-ext-2", "http://sparql.org/D.xyz", "text/turtle",  Lang.RDFXML,  Lang.TTL);
        add(x, "Test-unknown-ext-3", "http://sparql.org/D.xyz", "text/plain",   null,         null);
        add(x, "Test-unknown-ext-4", "http://sparql.org/D.xyz", "text/plain",   Lang.RDFXML,  Lang.RDFXML);
        add(x, "Test-unknown-ext-5", "http://sparql.org/D.xyz", null,           Lang.NT,      Lang.NT);
        add(x, "Test-unknown-ext-6", "http://sparql.org/D.xyz", null,           null,         null);

        return x.stream();
    }

    private static void add(List<Arguments> x, String marker, String url, String contentType, Lang hintLang, Lang expected) {
        x.add(Arguments.of(marker, url, contentType, hintLang, expected));
    }

    private final String url;
    private final String contentType;
    private final Lang hintLang;
    private final Lang expected;

    public TestSyntaxDetermination(String marker, String url, String contentType, Lang hintLang, Lang expected) {
        this.url = url;
        this.contentType = contentType;
        this.hintLang = hintLang;
        this.expected = expected;
    }

    @Test public void syntaxDetermination()
    { test(url, contentType, hintLang, expected); }

    static void test(String url, String ct, Lang hint, Lang expected) {
        ContentType x = WebContent.determineCT(ct, hint, url);
        Lang lang = RDFDataMgr.determineLang(url, ct, hint);
        assertEquals(expected, lang);
    }
}

