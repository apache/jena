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

package org.apache.jena.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.iterator.QueryIterService;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.service.ServiceExecutorFactory;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;
import org.junit.Test;

public class TestCustomServiceExecutor {

    static Table table = SSE.parseTable("(table (row (?s 1) (?p 2) (?o 3) ) )");

    /** A custom service factory that yields the above table for any request
     *  to urn:customService */
    static ServiceExecutorFactory factory = (op, opOriginal, binding, execCxt) ->
        op.getService().getURI().equals("urn:customService")
            ? ()->table.iterator(execCxt)
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

    /** Test setting the registry on a local context */
    @Test
    public void testLocalServiceExecutorRegistry() {
        assertResult("urn:customService",
                     qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry));
    }

    @Test
    public void testCustomExecution() {
        ResultSetRewindable qresults = runTestQuery("urn:customService",
                                                    qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry),
                                                    false
                                                    );
        Binding row = qresults.nextBinding();
        assertEquals(3, row.size());
        assertTrue(row.contains(Var.alloc("s")));
        assertTrue(row.contains(Var.alloc("p")));
        assertTrue(row.contains(Var.alloc("o")));
    }

    /** Check: Use of a service IRI which has no custom processor nor HTTP endpoint. */
    @Test(expected = QueryException.class)
    public void testIllegalServiceIri1() {
        assertResult("urn:illegalServiceIri",
                     qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry),
                     false);
    }

    /**
     * Check: Use of a service IRI, with SILENT, which has no custom processor nor HTTP endpoint.
     */
    @Test
    public void testIllegalServiceIri2() {
        Class<?> logClass = QueryIterService.class;
        String logLevel = LogCtl.getLevel(logClass);
        try {
            LogCtl.setLevel(logClass, "ERROR");
            // Run with SILENT
            runTestQuery("urn:illegalServiceIri",
                         qe -> ServiceExecutorRegistry.set(qe.getContext(), customRegistry),
                         true);
        } finally {
            LogCtl.setLevel(logClass, logLevel);
        }
    }

    // Check to rule out interference with conventional access to remote endpoints.
    // Uncommenting @Test will print out data from the remote endpoint
    //@Test
    public void testAgainstDBpedia() {
        ResultSetRewindable qresults = runTestQuery("http://dbpedia.org/sparql",
                                                    qe -> {},
                                                    false);
        ResultSetFormatter.out(qresults);
    }

    // Run and return a safe result set or throw an exception.
    private static ResultSetRewindable runTestQuery(String serviceIri, Consumer<QueryExecution> setup, boolean withSilent) {
        String strSilent = withSilent ? " SILENT" : "";
        String queryStr = "SELECT * { SERVICE"+strSilent+" <" + serviceIri + "> { { SELECT * { ?s ?p ?o } LIMIT 10 } } }";

        Model model = ModelFactory.createDefaultModel();
        try (QueryExecution qe = QueryExecutionFactory.create(queryStr, model)) {
            setup.accept(qe);
            return qe.execSelect().rewindable();
        }
    }

    public static void assertResult(String serviceIri, Consumer<QueryExecution> qePostProcessor) {
        assertResult(serviceIri, qePostProcessor, false);
    }

    public static void assertResult(String serviceIri, Consumer<QueryExecution> qePostProcessor, boolean withSilent) {
        ResultSetRewindable actual = runTestQuery(serviceIri, qePostProcessor, withSilent);
        boolean isEqual = ResultSetCompare.equalsExact(actual, ResultSet.adapt(table.toRowSet()));
        if (!isEqual) {
            actual.reset();
            ResultSetMgr.write(System.err, actual, ResultSetLang.RS_Text);
        }
        Assert.assertTrue(isEqual);
    }
}
