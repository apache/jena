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

import static org.apache.jena.rdfconnection.LibRDFConn.adapt;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdflink.RDFLinkDatasetBuilder;
import org.apache.jena.sys.JenaSystem;

/**
 * Factory for RDF connections, both local and remote.
 * <p>
 * Applications should use {@code RDFConnection#connect(Dataset)} or {@code RDFConnection#connect(URL)}
 * for most cases and {@link RDFConnectionRemote#service} for detailed setup of an HTTP connection
 * to remote SPARQL endpoints.
 * </p>
 * <p>
 * For complex remote (HTTP) connections, see
 * {@link RDFConnectionRemote#newBuilder} for detailed control.
 * This class provides only some common cases.
 * </p>
 * @deprecated See individual static methods for replacements.
 */
@Deprecated
public class RDFConnectionFactory {
    static { JenaSystem.init(); }

    /**
     * Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     * This call assumes all SPARQL operations (query, update GSP) are available at the given endpoint.
     * <a href="http://jena.apache.org/documentation/fuseki2">Fuseki</a>
     * supports this arrangement.
     * <p>
     * Use {@link RDFConnectionRemote#service} for to set different names for different operations.
     *
     * @param destination
     * @return RDFConnection
     * @deprecated Use {@link RDFConnection#connect}
     */
    @Deprecated
    public static RDFConnection connect(String destination) {
        return RDFConnectionRemote.service(destination).build();
    }

    /** Create a connection specifying the URLs of the service.
     * <p>
     * A common setup used by Fuseki is:
     *  <ul>
     *  <li>SPARQL Query endpoint : "sparql"
     *  <li>SPARQL Update endpoint : "update"
     *  <li>SPARQL Graph Store Protocol : "data"
     *  </ul>
     *  These are the default names in <a href="http://jena.apache.org/documentation/fuseki2">Fuseki</a>
     *  Other names can be specified using {@link #connect(String, String, String, String)}
     *
     * @deprecated Use {@link RDFConnectionRemote#service} and set the endpoints.
     * <pre>
     * RDFConnectionRemote.newBuilder()
     *       .queryEndpoint(queryServiceEndpoint)
     *       .updateEndpoint(updateServiceEndpoint)
     *       .gspEndpoint(graphStoreProtocolEndpoint)
     *       .build();
     * </pre>
     *
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnection
     */
    @Deprecated
    public static RDFConnection connect(String queryServiceEndpoint,
                                        String updateServiceEndpoint,
                                        String graphStoreProtocolEndpoint) {
        return RDFConnectionRemote.newBuilder()
            .queryEndpoint(queryServiceEndpoint)
            .updateEndpoint(updateServiceEndpoint)
            .gspEndpoint(graphStoreProtocolEndpoint)
            .build();
    }

    /** Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     * Each service is then specified by a URL which is relative to the {@code datasetURL}.
     *
     * @deprecated Use {@link RDFConnectionRemote#service} and set the endpoints.
     *
     * <pre>
     * RDFConnectionRemote.service(datasetURL)
     *        .queryEndpoint(queryServiceEndpoint)
     *        .updateEndpoint(updateServiceEndpoint)
     *        .gspEndpoint(graphStoreProtocolEndpoint)
     *        .build();
     * </pre>
     *
     * @param datasetURL
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnection
     */
    @Deprecated
    public static RDFConnection connect(String datasetURL,
                                        String queryServiceEndpoint,
                                        String updateServiceEndpoint,
                                        String graphStoreProtocolEndpoint) {
        return RDFConnectionRemote.newBuilder()
            .destination(datasetURL)
            .queryEndpoint(queryServiceEndpoint)
            .updateEndpoint(updateServiceEndpoint)
            .gspEndpoint(graphStoreProtocolEndpoint)
            .build();
    }

    /** Make a remote RDFConnection to the URL, with user and password for the client access using basic auth.
     *  Use with care &ndash; basic auth over plain HTTP reveals the password on the network.
     * @param URL
     * @param user
     * @param password
     * @return RDFConnection
     *
     * @deprecated Use {@link RDFConnection#connectPW}.
     */
    @Deprecated
    public static RDFConnection connectPW(String URL, String user, String password) {
        return RDFConnection.connectPW(URL, user, password);
    }

    /**
     * Connect to a local (same JVM) dataset.
     * The default isolation is {@code NONE}.
     * See {@link #connect(Dataset, Isolation)} to select an isolation mode.
     *
     * @deprecated Use {@link RDFConnection#connect(Dataset)}.
     *
     * @param dataset
     * @return RDFConnection
     * @see RDFConnectionLocal
     */
    @Deprecated
    public static RDFConnection connect(Dataset dataset) {
        return RDFConnection.connect(dataset);
    }

    /**
     * Connect to a local (same JVM) dataset.
     * <p>
     * Multiple levels of {@link Isolation} are provided, The default {@code COPY} level makes a local
     * {@link RDFConnection} behave like a remote connection.
     * See <a href="https://jena.apache.org/documentation/rdfconnection/">the documentation for more details.</a>
     * <ul>
     * <li>{@code COPY} &ndash; {@code Model}s and {@code Dataset}s are copied.
     *     This is most like a remote connection.
     * <li>{@code READONLY} &ndash; Read-only wrappers are added but changes to
     *     the underlying model or dataset will be seen.
     * <li>{@code NONE} (default) &ndash; Changes to the returned {@code Model}s or {@code Dataset}s act on the original object.
     * </ul>
     *
     * @param dataset
     * @param isolation
     * @return RDFConnection
     *
     * @deprecated Use {@link RDFConnection#connect(Dataset, Isolation)}.
     */
    @Deprecated
    public static RDFConnection connect(Dataset dataset, Isolation isolation) {
        return adapt(RDFLinkDatasetBuilder.newBuilder().dataset(dataset.asDatasetGraph()).isolation(isolation).build());
    }

    /** Create a connection to a remote Fuseki server by URL.
     * This is the URL for the dataset.
     * <p>
     * A {@link RDFConnectionFuseki} is an {@link RDFConnection} that:
     * <ul>
     * <li>provides round-trip of blank nodes between this application and the server
     * <li>uses the more efficient <a href="http://jena.apache.org/documentation/io/rdf-binary.html">RDF Thrift binary</a> format.
     * </ul>
     *
     *  This factory call assumes the names of services as:
     *  <ul>
     *  <li>SPARQL Query endpoint : "sparql"
     *  <li>SPARQL Update endpoint : "update"
     *  <li>SPARQL Graph Store Protocol : "data"
     *  </ul>
     *  These are the default names in <a href="http://jena.apache.org/documentation/fuseki2">Fuseki</a>
     *  Other names can be specified using {@link #connectFuseki(String, String, String, String)}.
     *
     * @param destination
     * @return RDFConnectionFuseki
     *
     * @deprecated Use {@link RDFConnectionFuseki#service}.
     */
    @Deprecated
    public static RDFConnectionFuseki connectFuseki(String destination) {
        return (RDFConnectionFuseki)RDFConnectionFuseki.create().destination(destination).build();
    }

    /** Create a connection to a remote Fuseki server by URL.
     * This is the URL for the dataset.
     *
     * Each service is then specified by a URL which is relative to the {@code datasetURL}.
     *
     * @param datasetURL
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnectionFuseki
     *
     * @deprecated Use {@link RDFConnectionFuseki#service}.
     */
    @Deprecated
    public static RDFConnectionFuseki connectFuseki(String datasetURL,
                                                    String queryServiceEndpoint,
                                                    String updateServiceEndpoint,
                                                    String graphStoreProtocolEndpoint) {
        return (RDFConnectionFuseki)RDFConnectionFuseki.create()
                .destination(datasetURL)
                .queryEndpoint(queryServiceEndpoint)
                .updateEndpoint(updateServiceEndpoint)
                .gspEndpoint(graphStoreProtocolEndpoint)
                .build();
        }
}
