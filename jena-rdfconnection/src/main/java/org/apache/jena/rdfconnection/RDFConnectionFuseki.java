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

import java.util.stream.Stream;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/** 
 * Implementation of the {@link RDFConnection} interface for connecting to an Apache Jena Fuseki.
 * <p>
 * This adds the ability to work with blank nodes across the network.
 */
public class RDFConnectionFuseki extends RDFConnectionRemote2 {

    /**
     * Create a connection builder which is initialized for the default Fuseki
     * configuration. The application must call
     * {@link RDFConnectionRemoteBuilder#destination(String)} to set the URL of the remote
     * dataset.
     * @return RDFConnectionRemoteBuilder
     */
    public static RDFConnectionRemoteBuilder create() {
        return setupForFuseki(RDFConnectionRemote2.create());
    }

    /** 
     * Create a connection builder which is initialized from an existing {@code RDFConnectionFuseki}.
     * @param other The RDFConnectionFuseki to clone.
     * @return RDFConnectionRemoteBuilder
     */
    public static RDFConnectionRemoteBuilder create(RDFConnectionFuseki other) {
        return setupCreator(RDFConnectionRemote2.create(other));
    }
    
    /** Fuseki settings */
    private static RDFConnectionRemoteBuilder setupForFuseki(RDFConnectionRemoteBuilder builder) {
        String ctRDFThrift = Lang.RDFTHRIFT.getContentType().getContentType();
        return 
            builder
                .quadsFormat(RDFFormat.RDF_THRIFT)
                .triplesFormat(RDFFormat.RDF_THRIFT)
                .acceptHeaderGraph(ctRDFThrift)
                .acceptHeaderDataset(ctRDFThrift)
                .acceptHeaderSelectQuery(ResultSetLang.SPARQLResultSetThrift.getHeaderString())
                .acceptHeaderAskQuery(ResultSetLang.SPARQLResultSetJSON.getHeaderString())
                .acceptHeaderGraphQuery(ResultSetLang.SPARQLResultSetThrift.getHeaderString())
                // Create object of this class.
                .creator((b)->fusekiMaker(b));
    }
    
    private static RDFConnectionRemoteBuilder setupCreator(RDFConnectionRemoteBuilder builder) {
        return builder.creator((b)->fusekiMaker(b));
    }
    
    static RDFConnectionFuseki fusekiMaker(RDFConnectionRemoteBuilder builder) {
        return new RDFConnectionFuseki(builder);
    }

    protected RDFConnectionFuseki(RDFConnectionRemoteBuilder base) {
        this(base.txnLifecycle, base.httpClient, base.httpContext, 
            base.destination, base.queryURL, base.updateURL, base.gspURL,
            base.outputQuads, base.outputTriples,
            base.acceptDataset, base.acceptGraph,
            base.acceptSelectResult, base.acceptAskResult, base.acceptGraphResult);
    }
    
    protected RDFConnectionFuseki(Transactional txnLifecycle, HttpClient httpClient, HttpContext httpContext, String destination,
                                  String queryURL, String updateURL, String gspURL, RDFFormat outputQuads, RDFFormat outputTriples,
                                  String acceptDataset, String acceptGraph, String acceptSelectResult, String acceptAskResult,
                                  String acceptGraphResult) {
        super(txnLifecycle, httpClient, httpContext, 
              destination, queryURL, updateURL, gspURL,
              outputQuads, outputTriples, 
              acceptDataset, acceptGraph,
              acceptSelectResult, acceptAskResult, acceptGraphResult);
    }
    
    // Fuseki specific operations.
    
    @Override
    public void update(String updateString) {
        checkUpdate();
        if ( true ) {
            // XXX Parse local, use original string.
            UpdateRequest req = UpdateFactory.create(updateString);
        }
        exec(()->HttpOp.execHttpPost(svcUpdate, WebContent.contentTypeSPARQLUpdate, updateString, this.httpClient, this.httpContext));
//        update(UpdateFactory.create(updateString));
    }
    
//    @Override
//    public void querySelect(String query, Consumer<QuerySolution> rowAction) {
//        try ( QueryExecution qExec = query(query) ) {
//            qExec.execSelect().forEachRemaining(rowAction);
//        }
//    }
    
    // Make sure all querygoes through query(String) or query(Query) 
    
    @Override
    public QueryExecution query(String queryString) {
        checkQuery();
        
        Query queryLocal = QueryFactory.create(queryString);
        // XXX Kludge until QueryEngineHTTP.setAccept.
        // XXX Accept header builder.
        String acceptHeader = acceptSelectResult+","+acceptAskResult+";q=0.9,"+acceptGraphResult;
        return exec(()-> {
            QueryExecution qExec = new QueryEngineHTTP(svcQuery, queryString, httpClient, httpContext);
            QueryEngineHTTP qEngine = (QueryEngineHTTP)qExec;
            // XXX qEngine.setAccept(acceptHeader);
            // Only one choice, not "Accept:"
            switch ( queryLocal.getQueryType() ) {
                case Query.QueryTypeSelect:
                    qEngine.setSelectContentType(acceptSelectResult);
                    break;
                case Query.QueryTypeAsk:
                    qEngine.setAskContentType(acceptAskResult);
                    break;
                case Query.QueryTypeDescribe:
                case Query.QueryTypeConstruct:
                    qEngine.setModelContentType(acceptGraphResult);
                    break;
            }
            return qEngine ;
        });
//        // XXX Better!
//        String url = svcQuery+"?query="+queryString;
//        // XXX Better accept.
//        TypedInputStream in =  exec(()->HttpOp.execHttpGet(url, acceptSelectResult, this.httpClient,this.httpContext));
//        QueryExecution qExec = 
//        return qExec;
    }

    /**
     * Return a {@link Model} that is proxy for a remote model in a Fuseki server. This
     * support the model operations of accessing statements and changing the model.
     * <p>
     * This provide low level access to the remote data. The application will be working
     * with and manipulating the remote model directly which may involve a significant
     * overhead for every {@code Model} API operation.
     * <p>
     * <b><em>Warning</em>:</b> This is <b>not</b> performant for bulk changes. 
     * <p>
     * Getting the model, using {@link #fetch()}, which copies the whole model into a local
     * {@code Model} object, maniupulating it and putting it back with {@link #put(Model)}
     * provides another way to work with remote data.
     * 
     * @return Model
     */
    public Model getModelProxy() { return null; }
    public Model getModelProxy(String graphName) { return null; }
    
    public Graph getGraphProxy() { return null; }
    public Graph getGraphProxy(String graphName) { return null; }

    public Dataset getDatasetProxy() { return null; }
    public DatasetGraph getDatasetGraphProxy() { return null; }

    // Or remote RDFStorage?
    public Stream<Triple> findStream(Node s, Node p , Node o) { return null; }
    public Stream<Quad> findStream(Node g, Node s, Node p , Node o) { return null; }

    // Send Patch 
}

