/**
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

package org.apache.jena.sparql.exec.http;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.http.sys.HttpRequestModifier;
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.query.*;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.single.ServiceExecutorHttp;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Service implementation code -- Service.exec */
public class TestService {

    private static String SERVICE;
    private static EnvTest env;
    // Local dataset for execution of SERVICE. Can be used to carry a context.
    private static final DatasetGraph localDataset() {return DatasetGraphZero.create(); }

    /*package*/ static ElementService makeElt(EnvTest env) {
        Node serviceNode = NodeFactory.createURI(env.datasetURL());
        ElementGroup elt = new ElementGroup();
        Element elt1 = new ElementTriplesBlock(bgp);
        elt.addElement(elt1);
        ElementService eltService = new ElementService(SERVICE, elt);
        return eltService;
    }

    /*package*/ static OpService makeOp(EnvTest env) {
        Node serviceNode = NodeFactory.createURI(env.datasetURL());
        return makeOp(env, serviceNode);
    }

    /*package*/ static OpService makeOp(EnvTest env, Node serviceNode) {
        ElementGroup elt = new ElementGroup();
        Element elt1 = new ElementTriplesBlock(bgp);
        elt.addElement(elt1);
        Op subOp = Algebra.compile(elt1);

        //ElementService eltService = new ElementService(SERVICE, elt);
        OpService opService = new OpService(serviceNode, subOp, false);
        return opService;
    }

    /*package*/ static OpService makeOpElt(EnvTest env) {
        Node serviceNode = NodeFactory.createURI(env.datasetURL());

        ElementGroup elt = new ElementGroup();
        Element elt1 = new ElementTriplesBlock(bgp);
        elt.addElement(elt1);
        Op subOp = Algebra.compile(elt1);

        ElementService eltService = new ElementService(SERVICE, elt);
        OpService opService = new OpService(serviceNode, subOp, eltService, false);
        return opService;
    }

    // Remember the initial settings.
    static String logLevelQueryIterService = LogCtl.getLevel(ServiceExecutorRegistry.class);
    static String logLevelFuseki = LogCtl.getLevel(Fuseki.class);

    @BeforeClass public static void beforeClass() {
        //FusekiLogging.setLogging();
        env = EnvTest.create("/ds");
        SERVICE = env.datasetURL();
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    private static Element subElt = null;
    private static BasicPattern bgp = SSE.parseBGP("(bgp (?s ?p ?o))");

    // Forms of the OpService elements.

    @Test public void service_exec_1() {
        OpService op = makeOp(env);
        QueryIterator qIter = Service.exec(op, new Context());
        assertNotNull(qIter);
    }

    @Test public void service_exec_2() {
        OpService op = makeOpElt(env);
        QueryIterator qIter = Service.exec(op, new Context());
        assertNotNull(qIter);
    }

    @Test public void service_exec_3() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        OpService op = makeOpElt(env);
        QueryIterator qIter = Service.exec(op, new Context());
        assertNotNull(qIter);
        assertTrue(qIter.hasNext());
        qIter.next();
        assertFalse(qIter.hasNext());
    }

    @Test public void service_query_QueryExecution() {
        // Via QueryExecution
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        Dataset dataset = DatasetFactory.create();
        Query query = QueryFactory.create("SELECT * { SERVICE <"+SERVICE+"> { ?s ?p ?o }} ");
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ) {
            ResultSet rs = qExec.execSelect();
            int x = ResultSetFormatter.consume(rs);
            assertEquals(1, x);
        }
    }

    @Test public void service_query_RDFLink() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        String queryString = "SELECT * { SERVICE <"+SERVICE+"> { ?s ?p ?o }} ";

        // Via RDFLink(local) and QueryExec
        // Connect to local, unused, permanently empty dataset
        try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
            link.queryRowSet(queryString, rs->{
                long x = Iter.count(rs);
                assertEquals(1, x);
            });
        }
    }

    @Test public void service_query_silent_no_service() {
        logOnlyErrors(ServiceExecutorHttp.class, ()->{
            DatasetGraph dsg = env.dsg();
            String queryString = "SELECT * { SERVICE SILENT <"+SERVICE+"JUNK> { VALUES ?X { 1 2 } }} ";
            try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
                try ( QueryExec qExec = link.query(queryString) ) {
                    RowSet rs = qExec.select();
                    assertTrue(rs.hasNext());
                    Binding binding = rs.next();
                    assertFalse(rs.hasNext());
                    assertTrue(binding.isEmpty());
                }
            }
        });
    }

    @Test public void service_query_silent_nosite() {
        logOnlyErrors(ServiceExecutorHttp.class, ()->{
            DatasetGraph dsg = env.dsg();
            String queryString = "SELECT * { SERVICE SILENT <http://nosuchsite/> { VALUES ?X { 1 2 } }} ";
            try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
                try ( QueryExec qExec = link.query(queryString) ) {
                    RowSet rs = qExec.select();
                    assertTrue(rs.hasNext());
                    Binding binding = rs.next();
                    assertFalse(rs.hasNext());
                    assertTrue(binding.isEmpty());
                }
            }
        });
    }

    @Test public void service_query_extra_params() {
        String queryString = "ASK { SERVICE <"+SERVICE+"?format=json> { BIND(now() AS ?now) } }";
        try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
            boolean b = link.queryAsk(queryString);
            assertTrue(b);
        }
    }

    private static void logOnlyErrors(Class<?> logClass, Runnable action) {
        String original = LogCtl.getLevel(logClass);
        try {
            LogCtl.setLevel(logClass, "ERROR");
            action.run();
        } finally {
            LogCtl.setLevel(logClass, original);
        }
    }

    // Uses a HttpRequestModifier to check the changes.
    @Test public void service_query_extra_params_oldstyle_by_context_1() {

        Map<String, Map<String, List<String>>> testServiceParams = new HashMap<>();
        Map<String, List<String>> settings =  new HashMap<>();
        settings.put("apikey", List.of("BristolCalling"));
        testServiceParams.put(SERVICE, settings);

        DatasetGraph clientDGS = localDataset();
        clientDGS.getContext().set(ARQ.serviceParams, testServiceParams);

        AtomicBoolean seen = new AtomicBoolean(false);
        HttpRequestModifier inspector = (params, header) -> {
            seen.set(params.containsParam("apikey"));
        };

        logOnlyErrors(Fuseki.class, ()->{
            runWithModifier(SERVICE, inspector, ()->{
                String queryString = "ASK { SERVICE <"+SERVICE+"> { BIND(now() AS ?now) } }";
                try ( QueryExec qExec = QueryExec.dataset(clientDGS).query(queryString).build() ) {
                    boolean b = qExec.ask();
                    assertTrue(b);
                }
            });
        });
        assertTrue(seen.get());
    }

    // Uses a HttpRequestModifier to check the changes.
    @Test public void service_query_extra_params_oldstyle_by_context_2() {

        Map<String, Map<String, List<String>>> testServiceParams = new HashMap<>();
        Map<String, List<String>> settings =  new HashMap<>();
        settings.put("apikey", List.of("BristolCallingToTheFarawayTowns"));
        testServiceParams.put(SERVICE, settings);

        DatasetGraph clientDGS = localDataset();
        clientDGS.getContext().set(ARQ.serviceParams, testServiceParams);

        AtomicBoolean seen = new AtomicBoolean(false);
        HttpRequestModifier inspector = (params, header) -> {
            seen.set(params.containsParam("apikey"));
        };

        logOnlyErrors(Fuseki.class, ()->{
            runWithModifier(SERVICE, inspector, ()->{
                String queryString = "ASK { SERVICE <"+SERVICE+"> { BIND(now() AS ?now) } }";
                try ( QueryExec qExec = QueryExec.dataset(clientDGS).query(queryString).build() ) {
                    boolean b = qExec.ask();
                    assertTrue(b);
                }
            });
        });
        assertTrue(seen.get());
    }


    // Same except set the QExec context.

    @Test (expected=QueryExecException.class)
    public void service_query_disabled_local_dataset() {
        String queryString = "ASK { SERVICE <"+SERVICE+"?format=json> { BIND(now() AS ?now) } }";
        DatasetGraph localdsg = localDataset();
        localdsg.getContext().set(Service.httpServiceAllowed, false);
        try ( RDFLink link = RDFLinkFactory.connect(localdsg) ) {
            boolean b = link.queryAsk(queryString);
        }
    }

    @Test (expected=QueryExecException.class)
    public void service_query_disabled_global() {
        String queryString = "ASK { SERVICE <"+SERVICE+"?format=json> { BIND(now() AS ?now) } }";
        try {
            ARQ.getContext().set(Service.httpServiceAllowed, false);
            try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
                boolean b = link.queryAsk(queryString);
            }
        } finally {
            ARQ.getContext().unset(Service.httpServiceAllowed);
        }
    }

    @Test (expected=QueryExecException.class)
    public void service_query_disabled_queryexec() {
        String queryString = "ASK { SERVICE <"+SERVICE+"?format=json> { BIND(now() AS ?now) } }";
        Context context = Context.create().set(Service.httpServiceAllowed, false);
        try ( QueryExec qExec = QueryExec.dataset(localDataset()).query(queryString).context(context).build() ) {
            qExec.ask();
        }
    }

    @Test public void service_query_modified_cxt() {
        DatasetGraph dsg = env.dsg();
        String queryString = "SELECT * { SERVICE <"+SERVICE+"> { BIND (123 AS ?X) } }";

        // RequestModifer that sets a flag to show it has been run.
        AtomicInteger COUNTER = new AtomicInteger(0);
        HttpRequestModifier testModifier = (Params params, Map<String, String> httpHeaders) -> {
            COUNTER.incrementAndGet();
        };
        DatasetGraph localdsg = localDataset();

        localdsg.getContext().put(ARQ.httpRequestModifer, testModifier);

        try ( RDFLink link = RDFLinkFactory.connect(localdsg) ) {
            try ( QueryExec qExec = link.query(queryString) ) {
                RowSet rs = qExec.select();
                long x = Iter.count(rs);
                assertEquals(1, x);
            }
        }
        assertEquals("Modifier did not run", 1, COUNTER.get());
    }

    @Test public void service_query_modified_registry() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        String queryString = "SELECT * { SERVICE <"+SERVICE+"> { ?s ?p ?o }} ";

        RDFLink link = RDFLinkFactory.connect(localDataset());

        // RequestModifer that sets a flag to show it has been run.
        AtomicInteger COUNTER = new AtomicInteger(0);
        HttpRequestModifier testModifier = (Params params, Map<String, String> httpHeaders) -> {
            COUNTER.incrementAndGet();
        };

        runWithModifier(SERVICE, testModifier, ()->{
            // Via RDFLink(local) and QueryExec
            // Connect to local, unused, permanently empty dataset
            try ( QueryExec qExec = QueryExec.dataset(localDataset()).query(queryString).build() ) {
                RowSet rs = qExec.select();
                long x = Iter.count(rs);
                assertEquals(1, x);
            }
        });
        assertEquals("Modifier did not run", 1, COUNTER.get());
    }


    @Test(expected=QueryExceptionHTTP.class)
    public void service_query_bad_no_service() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        // Not a service of the dataset.
        String queryString = "SELECT * { SERVICE <"+SERVICE+"/JUNK> { ?s ?p ?o }} ";

        // Via RDFLink(local) and QueryExec
        // Connect to local, unused, permanently empty dataset
        try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
            try ( QueryExec qExec = link.query(queryString) ) {
                RowSet rs = qExec.select();
                // Should go on execution.
                rs.hasNext();
                fail("Should not get here");
            }
        }
    }

    @Test(expected=QueryExceptionHTTP.class)
    public void service_query_bad_no_dataset() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        // Not a dataset of the server
        String queryString = "SELECT * { SERVICE <"+env.serverPath("JUNK")+"> { ?s ?p ?o }} ";

        // Via RDFLink(local) and QueryExec
        // Connect to local, unused, permanently empty dataset
        try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
            try ( QueryExec qExec = link.query(queryString) ) {
                // Where it should go wrong.
                RowSet rs = qExec.select();
                // Should go on execution.
                rs.hasNext();
                fail("Should not get here");
            }
        }
    }

    @Test(expected=QueryExceptionHTTP.class)
    public void service_query_bad_3() {
        DatasetGraph dsg = env.dsg();
        dsg.executeWrite(()->dsg.add(SSE.parseQuad("(_ :s :p :o)")));

        // Not a dataset of the server
        String queryString = "SELECT * { SERVICE <http://nosuchsite/> { ?s ?p ?o }} ";

        // Via RDFLink(local) and QueryExec
        // Connect to local, unused, permanently empty dataset
        try ( RDFLink link = RDFLinkFactory.connect(localDataset()) ) {
            try ( QueryExec qExec = link.query(queryString) ) {
                // Where it should go wrong.
                RowSet rs = qExec.select();
                // Should go on execution.
                rs.hasNext();
                fail("Should not get here");
            }
        }
    }

    // JENA-2207
    // The inner query involves a rename of variables ?p ?o. This should be undone by Service.exec.
    @Test public void service_query_nested_select_1() {
        String innerQuery = "SELECT ?s { ?s ?p ?o }";
        String queryString = "ASK { SERVICE <"+SERVICE+ "> { "+innerQuery+" } }";
        QueryExec.dataset(localDataset()).query(queryString).ask();
    }

    // JENA-2280 : No scope renaming. Tests the setup for following tests.
    @Test public void service_scope_service_0() {
        String queryString = StrUtils.strjoinNL
                ("SELECT (?value as ?temp) {"
                ,"  SELECT ?value {"
                ,"    SERVICE <"+SERVICE+ "> { VALUES ?value { 'test' }  }"
                ,"  }"
                ,"}");
        RowSet rs = QueryExec.dataset(localDataset()).query(queryString).select().materialize();
        Binding row = rs.next();
        assertTrue(row.contains("temp"));
    }

    // JENA-2280
    // ?value is scoped as ?/value and this needs dealing with in SERVCE results.
    @Test public void service_scope_service_2() {

        String queryString = StrUtils.strjoinNL
                ("SELECT ?temp {"
                ,"  SELECT (?value as ?temp) {"
                ,"    SERVICE <"+SERVICE+ "> { VALUES ?value { 'test' }  }"
                ,"  }"
                ,"}");
        RowSet rs = QueryExec.dataset(localDataset()).query(queryString).select().materialize();
        Binding row = rs.next();
        assertTrue(row.contains("temp"));
    }

    // JENA-2280
    @Test public void service_scope_service_3() {
        String queryString = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  SELECT (?value as ?temp) {"
                ,"    SERVICE <"+SERVICE+ "> { VALUES ?value { 'test' }  }"
                ,"  }"
                ,"}");
        RowSet rs = QueryExec.dataset(localDataset()).query(queryString).select().materialize();
        Binding row = rs.next();
        assertTrue(row.contains("temp"));
    }

    private static void runWithModifier(String key, HttpRequestModifier modifier, Runnable action) {
        RegistryRequestModifier.get().add(SERVICE, modifier);
        try {
            action.run();
        } finally {
            RegistryRequestModifier.get().remove(SERVICE);
        }
    }
}
