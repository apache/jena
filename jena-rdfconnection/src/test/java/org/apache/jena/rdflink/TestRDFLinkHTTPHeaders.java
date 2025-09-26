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

package org.apache.jena.rdflink;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.exec.http.QueryExecHTTPBuilder;
import org.junit.jupiter.api.Test;

public class TestRDFLinkHTTPHeaders {

    /**
     * Test whether connection headers are properly passed on to the query execution.
     * No query is actually executed.
     */
    @Test void test_headers_parsed() {
        try (RDFLink link = RDFLinkHTTP.service("urn:dummy-service")
            .acceptHeaderSelectQuery("s")
            .acceptHeaderAskQuery("a")
            .acceptHeaderGraph("g")
            .acceptHeaderDataset("d")
            .build()) {

            try (QueryExecHTTP qe = ((QueryExecHTTPBuilder)link.newQuery())
                    .query("SELECT * { ?s ?p ?o }")
                    .acceptHeaderSelectQuery("x").build()) {
                assertEquals("x", qe.getAcceptHeaderSelect());

                assertEquals("a", qe.getAcceptHeaderAsk());
                assertEquals("g", qe.getAcceptHeaderDescribe());
                assertEquals("g", qe.getAcceptHeaderConstructGraph());
                assertEquals("d", qe.getAcceptHeaderConstructDataset());
            }

            try (QueryExecHTTP qe = ((QueryExecHTTPBuilder)link.newQuery())
                    .query("ASK { ?s ?p ?o }")
                    .acceptHeaderAskQuery("x").build()) {
                assertEquals("x", qe.getAcceptHeaderAsk());

                assertEquals("s", qe.getAcceptHeaderSelect());
                assertEquals("g", qe.getAcceptHeaderDescribe());
                assertEquals("g", qe.getAcceptHeaderConstructGraph());
                assertEquals("d", qe.getAcceptHeaderConstructDataset());
            }

            try (QueryExecHTTP qe = ((QueryExecHTTPBuilder)link.newQuery())
                    .query("DESCRIBE <urn:x>")
                    .acceptHeaderGraph("x").build()) {
                assertEquals("x", qe.getAcceptHeaderDescribe());
                assertEquals("x", qe.getAcceptHeaderConstructGraph());

                assertEquals("s", qe.getAcceptHeaderSelect());
                assertEquals("a", qe.getAcceptHeaderAsk());
                assertEquals("d", qe.getAcceptHeaderConstructDataset());
            }

            try (QueryExecHTTP qe = ((QueryExecHTTPBuilder)link.newQuery())
                    .query("CONSTRUCT WHERE { ?s ?p ?o }")
                    .acceptHeaderGraph("x").build()) {
                assertEquals("x", qe.getAcceptHeaderConstructGraph());
                assertEquals("x", qe.getAcceptHeaderDescribe());

                assertEquals("s", qe.getAcceptHeaderSelect());
                assertEquals("a", qe.getAcceptHeaderAsk());
                assertEquals("d", qe.getAcceptHeaderConstructDataset());
            }

            try (QueryExecHTTP qe = ((QueryExecHTTPBuilder)link.newQuery())
                    .query("CONSTRUCT WHERE { GRAPH ?g { ?s ?p ?o } }")
                    .acceptHeaderDataset("x").build()) {
                assertEquals("x", qe.getAcceptHeaderConstructDataset());

                assertEquals("s", qe.getAcceptHeaderSelect());
                assertEquals("a", qe.getAcceptHeaderAsk());
                assertEquals("g", qe.getAcceptHeaderDescribe());
                assertEquals("g", qe.getAcceptHeaderConstructGraph());
            }
        }
    }
}
