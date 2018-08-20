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

package org.apache.jena.fuseki.embedded;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertNull ;
import static org.junit.Assert.assertTrue ;

import java.io.OutputStream ;
import java.util.function.Consumer ;

import org.apache.http.HttpEntity ;
import org.apache.http.entity.ContentProducer ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.server.DataAccessPointRegistry ;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.server.Operation ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.system.Txn ;
import org.apache.jena.update.UpdateExecutionFactory ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;
import org.apache.jena.web.HttpSC ;
import org.junit.Test ;

public class TestEmbeddedFuseki {
    
    private static final String DIR = "testing/FusekiEmbedded/" ;

    // Test - build on default port. 
    @Test public void embedded_01() {
        DatasetGraph dsg = dataset() ;
        int port = 3330 ;   // Default port.
        FusekiServer server = FusekiServer.create().add("/ds", dsg).build() ;
        assertTrue(server.getDataAccessPointRegistry().isRegistered("/ds")) ;
        server.start() ;
        query("http://localhost:"+port+"/ds/query", "SELECT * { ?s ?p ?o}", qExec-> {
            ResultSet rs = qExec.execSelect() ; 
            assertFalse(rs.hasNext()) ;
        }) ;
        server.stop() ;
    }
    
    // Different dataset name.
    @Test public void embedded_02() {
        DatasetGraph dsg = dataset() ;
        int port = 0 ;//FusekiEnv.choosePort() ;
        FusekiServer server = FusekiServer.make(port, "/ds2", dsg) ;
        DataAccessPointRegistry registry = server.getDataAccessPointRegistry() ;
        // But no /ds
        assertEquals(1, registry.size()) ;
        assertTrue(registry.isRegistered("/ds2")) ;
        assertFalse(registry.isRegistered("/ds")) ;
        try {
            server.start() ;
        } finally { server.stop() ; }
    }
    
    // Different dataset name.
    @Test public void embedded_03() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;
        FusekiServer server = FusekiServer.create()
            .port(port)
            .add("/ds1", dsg) 
            .build() ;
        server.start() ;
        try {
            // Add while live.
            Txn.executeWrite(dsg,  ()->{
                Quad q = SSE.parseQuad("(_ :s :p _:b)") ;
                dsg.add(q); 
            }) ;
            query("http://localhost:"+port+"/ds1/query", "SELECT * { ?s ?p ?o}", qExec->{
                ResultSet rs = qExec.execSelect() ; 
                int x = ResultSetFormatter.consume(rs) ;
                assertEquals(1, x) ;
            }) ;
        } finally { server.stop() ; }
    }
    
    
    @Test public void embedded_04() {
        DatasetGraph dsg = dataset() ;
        Txn.executeWrite(dsg,  ()->{
            Quad q = SSE.parseQuad("(_ :s :p _:b)") ;
            dsg.add(q); 
        }) ;

        // A service with just being able to do quads operations
        // That is, GET, POST, PUT on  "/data" in N-quads and TriG. 
        DataService dataService = new DataService(dsg) ;
        dataService.addEndpoint(Operation.Quads_RW, "");
        dataService.addEndpoint(Operation.Query, "");
        dataService.addEndpoint(Operation.Update, "");
        int port = FusekiLib.choosePort() ;
        
        FusekiServer server = FusekiServer.create()
            .port(port)
            .add("/data", dataService)
            .build() ;
        server.start() ;
        try {
            // Put data in.
            String data = "(graph (:s :p 1) (:s :p 2) (:s :p 3))" ;
            Graph g = SSE.parseGraph(data) ;
            HttpEntity e = graphToHttpEntity(g) ;
            HttpOp.execHttpPut("http://localhost:"+port+"/data", e) ;
    
            // Get data out.
            try ( TypedInputStream in = HttpOp.execHttpGet("http://localhost:"+port+"/data") ) { 
                Graph g2 = GraphFactory.createDefaultGraph() ;
                RDFDataMgr.read(g2, in, RDFLanguages.contentTypeToLang(in.getContentType())) ;
                assertTrue(g.isIsomorphicWith(g2)) ;
            }
            // Query.
            query("http://localhost:"+port+"/data", "SELECT * { ?s ?p ?o}", qExec->{
                ResultSet rs = qExec.execSelect() ; 
                int x = ResultSetFormatter.consume(rs) ;
                assertEquals(3, x) ;
            }) ;
            // Update
            UpdateRequest req = UpdateFactory.create("CLEAR DEFAULT") ;
            UpdateExecutionFactory.createRemote(req, "http://localhost:"+port+"/data").execute(); 
            // Query again.
            query("http://localhost:"+port+"/data", "SELECT * { ?s ?p ?o}", qExec-> {
                ResultSet rs = qExec.execSelect() ; 
                int x = ResultSetFormatter.consume(rs) ;
                assertEquals(0, x) ;
            }) ;
        } finally { server.stop() ; }
    }
    
    @Test public void embedded_05() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;
        FusekiServer server = FusekiServer.create()
            .port(port)
            .add("/ds0", dsg) 
            .build() ;
        server.start() ;
        try {
            // No stats
            String x = HttpOp.execHttpGetString("http://localhost:"+port+"/$/stats") ;
            assertNull(x) ;  
        } finally { server.stop() ; }
    }
    
    @Test public void embedded_06() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;
        FusekiServer server = FusekiServer.create()
            .port(port)
            .add("/ds0", dsg)
            .enableStats(true)
            .build() ;
        server.start() ;
        // No stats
        String x = HttpOp.execHttpGetString("http://localhost:"+port+"/$/stats") ;
        assertNotNull(x) ;
        server.stop() ;
    }

    // Context path.
    @Test public void embedded_07() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;
        
        FusekiServer server = FusekiServer.create()
            .port(port)
            .contextPath("/ABC")
            .add("/ds", dsg) 
            .build() ;
        server.start() ;
        try {
            String x1 = HttpOp.execHttpGetString("http://localhost:"+port+"/ds") ;
            assertNull(x1) ;
            String x2 = HttpOp.execHttpGetString("http://localhost:"+port+"/ABC/ds") ;
            assertNotNull(x2) ;
        } finally { server.stop() ; }
    }
    
    @Test public void embedded_08() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;

        FusekiServer server = FusekiServer.create()
            .port(port)
            .parseConfigFile(DIR+"config.ttl") 
            .build() ;
        server.start() ;
        try {
            query("http://localhost:"+port+"/FuTest", "SELECT * {}", x->{}) ;
        } finally { server.stop() ; } 
    }
    
    @Test public void embedded_09() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;

        FusekiServer server = FusekiServer.create()
            .port(port)
            .contextPath("/ABC")
            .parseConfigFile(DIR+"config.ttl") 
            .build() ;
        server.start() ;
        try {
            try {
                query("http://localhost:"+port+"/FuTest", "ASK{}", x->{}) ;
            } catch (HttpException ex) {
                assertEquals(HttpSC.METHOD_NOT_ALLOWED_405, ex.getResponseCode()) ;
            }

            query("http://localhost:"+port+"/ABC/FuTest","ASK{}",x->{}) ;
        } finally { server.stop() ; } 
    }

    @Test public void embedded_20() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;

        DataService dSrv = new DataService(dsg) ;
        dSrv.addEndpoint(Operation.Query, "q") ;
        dSrv.addEndpoint(Operation.GSP_R, "gsp") ;
        FusekiServer server = FusekiServer.create()
            .add("/dsrv1", dSrv)
            .port(port)
            .build() ;
        server.start() ;
        try {
            query("http://localhost:"+port+"/dsrv1/q","ASK{}",x->{}) ;
            String x1 = HttpOp.execHttpGetString("http://localhost:"+port+"/dsrv1/gsp") ;
            assertNotNull(x1) ;
        } finally { server.stop() ; } 
    }
    
    @Test public void embedded_21() {
        DatasetGraph dsg = dataset() ;
        int port = FusekiLib.choosePort() ;

        DataService dSrv = new DataService(dsg) ;
        dSrv.addEndpoint(Operation.Query, "q") ;
        dSrv.addEndpoint(Operation.GSP_R, "gsp") ;
        FusekiServer server = FusekiServer.create()
            .add("/dsrv1", dSrv)
            .staticFileBase(DIR)
            .port(port)
            .build() ;
        server.start() ;
        
        try {
            query("http://localhost:"+port+"/dsrv1/q","ASK{}",x->{}) ;
            String x1 = HttpOp.execHttpGetString("http://localhost:"+port+"/dsrv1/gsp") ;
            assertNotNull(x1) ;
            // Static
            String x2 = HttpOp.execHttpGetString("http://localhost:"+port+"/test.txt");
            assertNotNull(x2) ;
        } finally { server.stop() ; } 
    }

    
    /** Create an HttpEntity for the graph */  
    protected static HttpEntity graphToHttpEntity(final Graph graph) {
        final RDFFormat syntax = RDFFormat.TURTLE_BLOCKS ;
        ContentProducer producer = new ContentProducer() {
            @Override
            public void writeTo(OutputStream out) {
                RDFDataMgr.write(out, graph, syntax) ;
            }
        } ;
        EntityTemplate entity = new EntityTemplate(producer) ;
        ContentType ct = syntax.getLang().getContentType() ;
        entity.setContentType(ct.getContentType()) ;
        return entity ;
    }

    /*package*/ static DatasetGraph dataset() {
        return DatasetGraphFactory.createTxnMem() ;
    }

    /*package*/ static void query(String URL, String query, Consumer<QueryExecution> body) {
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(URL, query) ) {
            body.accept(qExec);
        }
    }
}
