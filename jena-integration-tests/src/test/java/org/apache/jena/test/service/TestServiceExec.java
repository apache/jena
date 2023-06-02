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

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.http.CtlService;
import org.apache.jena.sparql.service.single.ServiceExecutorHttp;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestServiceExec {
    // ---- Enable service
    @BeforeClass public static void enableAllowServiceExecution() { CtlService.enableAllowServiceExecution(); }
    @AfterClass public static void resetAllowServiceExecution() { CtlService.resetAllowServiceExecution(); }
    public static Context minimalContext() { return CtlService.minimalContext(); }
    // ----

    private static FusekiServer server;
    public static String testDB;
    private static DatasetGraph emptyLocal = DatasetGraphFactory.create();

    @BeforeClass
    public static void beforeClass() {
        DatasetGraph dsg = SSE.parseDatasetGraph("(dataset (graph (:s :p 1) (:s :p 2) (:s :p 3) ) )");
        server = FusekiServer.create().add("/ds", dsg).port(0).build();
        server.start();
        testDB = "http://localhost:"+server.getPort()+"/ds";
    }

    public static void afterClass() {
        try {
            server.stop();
        } catch (Throwable th) {}
    }

    @Test public void service_exec_1() {
        Query query = QueryFactory.create("SELECT * { SERVICE <"+testDB+"> { ?s ?p ?o} }");
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, emptyLocal) ) {
            ResultSet rs = qExec.execSelect();
            long x = Iter.count(rs);
            assertEquals(3, x);
        }
    }

    @Test(expected=QueryExceptionHTTP.class)
    public void service_exec_2() {
        Query query = QueryFactory.create("SELECT * { SERVICE <"+testDB+"/junk> { ?s ?p ?o} }");
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, emptyLocal) ) {
            ResultSet rs = qExec.execSelect();
            long x = Iter.count(rs);
            assertEquals(3, x);
        }
    }

    @Test
    public void service_exec_3() {
        Class<?> logClass = ServiceExecutorHttp.class;
        String logLevel = LogCtl.getLevel(logClass);
        try {
            LogCtl.setLevel(logClass, "ERROR");
            Query query = QueryFactory.create("SELECT * { SERVICE SILENT <"+testDB+"/junk> { ?s ?p ?o} }");
            try ( QueryExecution qExec = QueryExecutionFactory.create(query, emptyLocal) ) {
                ResultSet rs = qExec.execSelect();
                long x = Iter.count(rs);
                assertEquals(1, x);
            }
        } finally {
            LogCtl.setLevel(logClass, logLevel);
        }
    }
}
