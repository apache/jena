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

package org.apache.jena.sparql.service;

import java.util.function.Consumer;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;
import org.junit.Test;

public class TestCustomServiceExecutor {

    static Table table = SSE.parseTable("(table (row (?s 1) (?p 2) (?o 3) ) )");

    /** A custom service factory that yields the above table for any request
     *  to urn:customService */
    static ServiceExecutorFactory factory = (op, binding, execCxt) ->
        op.getService().getURI().equals("urn:customService")
            ? () -> table.iterator(execCxt)
            : null;

    static ServiceExecutorRegistry customRegistry = new ServiceExecutorRegistry().add(factory);

    @Test
    public void testGlobalServiceExecutorRegistry() {
        ServiceExecutorRegistry.get().add(factory);

        try {
            assertResult("urn:customService", qe -> {});
        } finally {
            // Better eventually remove the global registration
            ServiceExecutorRegistry.get().remove(factory);
        }
    }

    /** Test setting the registory on a local context*/
    @Test
    public void testLocalServiceExecutorRegistry() {
        assertResult("urn:customService",
                qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry));
    }

    /** Sanity check: Use of an illegal service iri */
    @Test(expected = QueryExceptionHTTP.class)
    public void testIllegalServiceIri() {
        assertResult("urn:illegalServiceIri",
                qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry));
    }

    // Sanity check to rule out interference where access to remote endpoints
    // Uncommenting @Test is expected to print out data from the remote endpoint
    // @Test
    public void testAgainstDBpedia() {
        assertResult("http://dbpedia.org/sparql",
                qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry));
    }


    public static void assertResult(String serviceIri, Consumer<QueryExecution> qePostProcessor) {

        String queryStr = "SELECT * { SERVICE <" + serviceIri + "> { { SELECT * { ?s ?p ?o } LIMIT 10 } } }";

        Model model = ModelFactory.createDefaultModel();
        try (QueryExecution qe = QueryExecutionFactory.create(queryStr, model)) {
            qePostProcessor.accept(qe);
            ResultSetRewindable actual = qe.execSelect().rewindable();

            boolean isEqual = ResultSetCompare.equalsExact(actual, table.toResultSet());

            if (!isEqual) {
                actual.reset();
                ResultSetMgr.write(System.err, actual, ResultSetLang.RS_Text);
            }

            Assert.assertTrue(isEqual);
        }
    }
}
