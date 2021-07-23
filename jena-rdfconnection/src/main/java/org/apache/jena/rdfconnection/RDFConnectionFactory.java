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

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.util.Objects;

import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdflink.RDFLinkBuilder;
import org.apache.jena.sys.JenaSystem;

public class RDFConnectionFactory {
    static { JenaSystem.init(); }

    /** Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     *
     *  This call assumes the names of services as:
     *  <ul>
     *  <li>SPARQL Query endpoint : "sparql"
     *  <li>SPARQL Update endpoint : "update"
     *  <li>SPARQL Graph Store Protocol : "data"
     *  </ul>
     *  These are the default names in <a href="http://jena.apache.org/documentation/fuseki2">Fuseki</a>
     *  Other names can be specified using {@link #connect(String, String, String, String)}
     *
     * @param destination
     * @return RDFConnection
     * @see #connect(String, String, String, String)
     */
    public static RDFConnection connect(String destination) {
        return RDFConnectionRemote.service(destination).build();
    }

    /** Create a connection specifying the URLs of the service.
     *
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnection
     */
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
     * @param datasetURL
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFConnection
     */
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
     */
    public static RDFConnection connectPW(String URL, String user, String password) {
        Objects.requireNonNull(URL);
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);

        // Authenticator to hold user and password.
        Authenticator authenticator = LibSec.authenticator(user, password);
        HttpClient client = HttpEnv.httpClientBuilder()
                .authenticator(authenticator)
                .build();
        return RDFConnectionRemote.newBuilder()
            .destination(URL)
            .httpClient(client)
            .build();
    }

    /**
     * Connect to a local (same JVM) dataset.
     * The default isolation is {@code NONE}.
     * See {@link #connect(Dataset, Isolation)} to select an isolation mode.
     *
     * @param dataset
     * @return RDFConnection
     * @see RDFConnectionLocal
     */
    public static RDFConnection connect(Dataset dataset) {
        return adapt(RDFLinkBuilder.newBuilder().dataset(dataset.asDatasetGraph()).build());
    }

    /**
     * Connect to a local (same JVM) dataset.
     * <p>
     * Multiple levels of {@link Isolation} are provided, The default {@code COPY} level makes a local
     * {@link RDFConnection} behave like a remote conenction.
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
     */
    public static RDFConnection connect(Dataset dataset, Isolation isolation) {
        return adapt(RDFLinkBuilder.newBuilder().dataset(dataset.asDatasetGraph()).isolation(isolation).build());
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
     */
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
     */
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
