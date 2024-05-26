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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.junit.Assert;
import org.junit.Test;

public class TestRDFConnectionHTTP {

    @Test
    public void test_select_01() {
        doAssert(WebContent.contentTypeXML, "SELECT * { ?s a ?o } LIMIT 10");
    }

    @Test
    public void test_ask_01() {
        doAssert(WebContent.contentTypeJSON, "ASK { ?s a ?o }");
    }

    @Test
    public void test_constructTriples_01() {
        doAssert(WebContent.contentTypeTurtle, "CONSTRUCT WHERE { ?s a ?o } LIMIT 10");
    }

    @Test
    public void test_constructQuads_01() {
        doAssert(WebContent.contentTypeTriG, "CONSTRUCT WHERE { GRAPH ?g{ ?s a ?o } } LIMIT 10");
    }

    @Test
    public void test_describe_01() {
        doAssert(WebContent.contentTypeNTriples, "DESCRIBE <urn:x>");
    }

    private static RDFConnectionRemoteBuilder configureHeader(Query query, String header, RDFConnectionRemoteBuilder builder) {
        return switch (query.queryType()) {
        case SELECT -> builder.acceptHeaderSelectQuery(header);
        case CONSTRUCT -> !query.isConstructQuad()
                            ? builder.acceptHeaderGraph(header)
                            : builder.acceptHeaderDataset(header);
        case DESCRIBE -> builder.acceptHeaderGraph(header);
        case ASK -> builder.acceptHeaderAskQuery(header);
        default -> throw new UnsupportedOperationException("Unhandled query type for query: " + query);
        };
    }

    /** Test whether the constructed QueryExec instance has the expected HTTP header set */
    private static void doAssert(String expectedHeader, String queryString) {
        Query query = QueryFactory.create(queryString);

        try (RDFConnection conn = configureHeader(query, expectedHeader, RDFConnectionRemote.newBuilder())
                .queryEndpoint("https://www.example.org/sparql") // Should never be resolved
                .build()) {
            try (QueryExecution qe = conn.query(queryString)) {
                QueryExecHTTP qeh = (QueryExecHTTP)QueryExec.adapt(qe);
                Assert.assertEquals(expectedHeader, qeh.getAppProvidedAcceptHeader());
            }

            try (QueryExecution qe = conn.newQuery().query(queryString).build()) {
                QueryExecHTTP qeh = (QueryExecHTTP)QueryExec.adapt(qe);
                Assert.assertEquals(expectedHeader, qeh.getAppProvidedAcceptHeader());
            }

            try (QueryExecution qe = conn.query(query)) {
                QueryExecHTTP qeh = (QueryExecHTTP)QueryExec.adapt(qe);
                Assert.assertEquals(expectedHeader, qeh.getAppProvidedAcceptHeader());
            }

            try (QueryExecution qe = conn.newQuery().query(query).build()) {
                QueryExecHTTP qeh = (QueryExecHTTP)QueryExec.adapt(qe);
                Assert.assertEquals(expectedHeader, qeh.getAppProvidedAcceptHeader());
            }
        }
    }
}
