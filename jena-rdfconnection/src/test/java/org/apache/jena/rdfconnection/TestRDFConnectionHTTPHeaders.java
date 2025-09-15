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

package org.apache.jena.rdfconnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.junit.jupiter.api.Test;

public class TestRDFConnectionHTTPHeaders {

    /**
     * Test whether connection headers are properly passed on to the query execution.
     * No query is actually executed.
     */
    @Test void test_headers_non_parsed() {
        try (RDFConnection conn = RDFConnectionRemote.service("urn:dummy-service")
            .parseCheckSPARQL(false)
            .acceptHeaderSelectQuery("s")
            .acceptHeaderAskQuery("a")
            .acceptHeaderGraph("g")
            .acceptHeaderDataset("d")
            .build()) {

            try (QueryExecution qExec = conn.query("dummy query string")) {
                QueryExecHTTP qe = (QueryExecHTTP)QueryExec.adapt(qExec);
                assertEquals("s", qe.getAcceptHeaderSelect());
                assertEquals("a", qe.getAcceptHeaderAsk());
                assertEquals("g", qe.getAcceptHeaderDescribe());
                assertEquals("g", qe.getAcceptHeaderConstructGraph());
                assertEquals("d", qe.getAcceptHeaderConstructDataset());
            }
        }
    }

    @Test void test_headers_non_parsed_fallback() {
        try (RDFConnection conn = RDFConnectionRemote.service("urn:dummy-service")
            .parseCheckSPARQL(false)
            .acceptHeaderQuery("f") // fallback
            .acceptHeaderSelectQuery(null)
            .acceptHeaderAskQuery(null)
            .acceptHeaderGraph(null)
            .acceptHeaderDataset(null)
            .build()) {

            try (QueryExecution qExec = conn.query("dummy query string")) {
                QueryExecHTTP qe = (QueryExecHTTP)QueryExec.adapt(qExec);
                assertEquals("f", qe.getAcceptHeaderSelect());
                assertEquals("f", qe.getAcceptHeaderAsk());
                assertEquals("f", qe.getAcceptHeaderDescribe());
                assertEquals("f", qe.getAcceptHeaderConstructGraph());
                assertEquals("f", qe.getAcceptHeaderConstructDataset());
            }
        }
    }

    @Test void test_headers_parsed_fallback() {
        try (RDFConnection conn = RDFConnectionRemote.service("urn:dummy-service")
            .acceptHeaderQuery("f") // fallback
            .acceptHeaderSelectQuery(null)
            .acceptHeaderAskQuery(null)
            .acceptHeaderGraph(null)
            .acceptHeaderDataset(null)
            .build()) {

            try (QueryExecution qExec = conn.newQuery()
                    .query("SELECT * { ?s ?p ?o }")
                    .build()) {
                QueryExecHTTP qe = (QueryExecHTTP)QueryExec.adapt(qExec);
                assertEquals("f", qe.getAcceptHeaderSelect());

                assertEquals("f", qe.getAcceptHeaderAsk());
                assertEquals("f", qe.getAcceptHeaderDescribe());
                assertEquals("f", qe.getAcceptHeaderConstructGraph());
                assertEquals("f", qe.getAcceptHeaderConstructDataset());
            }
        }
    }

    /**
     * Setting an override-header on the builder should set all
     * headers on the QueryExecHTTP instance to that override.
     */
    @Test
    public void test_override_header_01() {
        try (QueryExecutionHTTP qExec = QueryExecutionHTTPBuilder.create()
                .endpoint("urn:dummy-service")
                .queryString("SELECT * { ?s ?p ?o }")
                .acceptHeader("o") // Override header
                .build()) {
            QueryExecHTTP qe = (QueryExecHTTP)QueryExec.adapt(qExec);
            assertEquals("o", qe.getAcceptHeaderSelect());
            assertEquals("o", qe.getAcceptHeaderAsk());
            assertEquals("o", qe.getAcceptHeaderDescribe());
            assertEquals("o", qe.getAcceptHeaderConstructGraph());
            assertEquals("o", qe.getAcceptHeaderConstructDataset());
        }
    }

    @Test
    public void test_override_header_02() {
        try (QueryExecutionHTTP qExec = QueryExecutionHTTPBuilder.create()
                .endpoint("urn:dummy-service")
                .queryString("SELECT * { ?s ?p ?o }")
                .acceptHeader("o") // Override header first
                .acceptHeaderGraph("g")
                .build()) {
            QueryExecHTTP qe = (QueryExecHTTP)QueryExec.adapt(qExec);
            assertEquals("o", qe.getAcceptHeaderSelect());
            assertEquals("o", qe.getAcceptHeaderAsk());
            assertEquals("g", qe.getAcceptHeaderDescribe());
            assertEquals("g", qe.getAcceptHeaderConstructGraph());
            assertEquals("o", qe.getAcceptHeaderConstructDataset());
        }
    }

    @Test
    public void test_override_header_03() {
        try (QueryExecutionHTTP qExec = QueryExecutionHTTPBuilder.create()
                .endpoint("urn:dummy-service")
                .queryString("SELECT * { ?s ?p ?o }")
                .acceptHeaderGraph("g")
                .acceptHeader("o") // Override header last - overrides prior settings.
                .build()) {
            QueryExecHTTP qe = (QueryExecHTTP)QueryExec.adapt(qExec);
            assertEquals("o", qe.getAcceptHeaderSelect());
            assertEquals("o", qe.getAcceptHeaderAsk());
            assertEquals("o", qe.getAcceptHeaderDescribe());
            assertEquals("o", qe.getAcceptHeaderConstructGraph());
            assertEquals("o", qe.getAcceptHeaderConstructDataset());
        }
    }
}
