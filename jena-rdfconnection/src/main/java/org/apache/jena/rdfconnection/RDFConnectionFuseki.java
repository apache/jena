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

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.core.Transactional;

/**
 * Implementation of the {@link RDFConnection} interface for connecting to an Apache Jena Fuseki.
 * <p>
 * This adds the ability to work with blank nodes across the network.
 */
public class RDFConnectionFuseki extends RDFConnectionRemote {

    /**
     * Create a connection builder which is initialized for the default Fuseki
     * configuration. The application must call
     * {@link RDFConnectionRemoteBuilder#destination(String)} to set the URL of the remote
     * dataset.
     * @return RDFConnectionRemoteBuilder
     */
    public static RDFConnectionRemoteBuilder create() {
        return setupForFuseki(RDFConnectionRemote.newBuilder());
    }

    /**
     * Create a connection builder which is initialized from an existing {@code RDFConnectionFuseki}.
     * @param other The RDFConnectionFuseki to clone.
     * @return RDFConnectionRemoteBuilder
     */
    public static RDFConnectionRemoteBuilder create(RDFConnectionFuseki other) {
        return setupCreator(RDFConnectionRemote.newBuilder(other));
    }

    /** Fuseki settings */
    private static RDFConnectionRemoteBuilder setupForFuseki(RDFConnectionRemoteBuilder builder) {
        String ctRDFThrift = Lang.RDFTHRIFT.getContentType().getContentTypeStr();
        String acceptHeaderSPARQL = String.join(","
                            , ResultSetLang.RS_Thrift.getHeaderString()
                            , ResultSetLang.RS_JSON.getHeaderString()+";q=0.9"
                            , Lang.RDFTHRIFT.getHeaderString());
        return
            builder
                .quadsFormat(RDFFormat.RDF_THRIFT)
                .triplesFormat(RDFFormat.RDF_THRIFT)
                .acceptHeaderGraph(ctRDFThrift)
                .acceptHeaderDataset(ctRDFThrift)
                .acceptHeaderSelectQuery(ResultSetLang.RS_Thrift.getHeaderString())
                .acceptHeaderAskQuery(ResultSetLang.RS_JSON.getHeaderString())
                .acceptHeaderQuery(acceptHeaderSPARQL)
                .parseCheckSPARQL(false)
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
            base.acceptSparqlResults, base.acceptSelectResult, base.acceptAskResult,
            base.parseCheckQueries, base.parseCheckUpdates);
    }

    protected RDFConnectionFuseki(Transactional txnLifecycle, HttpClient httpClient, HttpContext httpContext, String destination,
                                  String queryURL, String updateURL, String gspURL, RDFFormat outputQuads, RDFFormat outputTriples,
                                  String acceptDataset, String acceptGraph,
                                  String acceptSparqlResults, String acceptSelectResult, String acceptAskResult,
                                  boolean parseCheckQueries, boolean parseCheckUpdates) {
        super(txnLifecycle, httpClient, httpContext,
              destination, queryURL, updateURL, gspURL,
              outputQuads, outputTriples,
              acceptDataset, acceptGraph,
              acceptSparqlResults, acceptSelectResult, acceptAskResult, parseCheckQueries, parseCheckUpdates);
    }

    // Fuseki specific operations.

//    /**
//     * Return a {@link Model} that is proxy for a remote model in a Fuseki server. This
//     * support the model operations of accessing statements and changing the model.
//     * <p>
//     * This provide low level access to the remote data. The application will be working
//     * with and manipulating the remote model directly which may involve a significant
//     * overhead for every {@code Model} API operation.
//     * <p>
//     * <b><em>Warning</em>:</b> This is <b>not</b> performant for bulk changes.
//     * <p>
//     * Getting the model, using {@link #fetch()}, which copies the whole model into a local
//     * {@code Model} object, maniupulating it and putting it back with {@link #put(Model)}
//     * provides another way to work with remote data.
//     *
//     * @return Model
//     */
//    public Model getModelProxy() { return null; }
//    public Model getModelProxy(String graphName) { return null; }
//
//    public Graph getGraphProxy() { return null; }
//    public Graph getGraphProxy(String graphName) { return null; }
//
//    public Dataset getDatasetProxy() { return null; }
//    public DatasetGraph getDatasetGraphProxy() { return null; }
//
//    // Or remote RDFStorage?
//    public Stream<Triple> findStream(Node s, Node p , Node o) { return null; }
//    public Stream<Quad> findStream(Node g, Node s, Node p , Node o) { return null; }

    // Send Patch
}

