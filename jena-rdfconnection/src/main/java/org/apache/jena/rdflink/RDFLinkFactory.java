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

package org.apache.jena.rdflink;

import org.apache.jena.rdfconnection.Isolation;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sys.JenaSystem;

public class RDFLinkFactory {
    static { JenaSystem.init(); }

    /**
     * Create a connection to a remote location by URL. This is the URL for the
     * dataset. This call assumes the SPARQL Query endpoint, SPARQL Update endpoint
     * and SPARQL Graph Store Protocol endpoinst are the same URL.
     * Thisis suported by <a href="http://jena.apache.org/documentation/fuseki2">Apache Jena Fuseki</a>.
     * Other names can be specified using {@link #connect(String, String, String, String)}
     * or {@link RDFLinkHTTPBuilder}.
     *
     * @param destination
     * @return RDFLink
     * @see #connect(String, String, String, String)
     */
    public static RDFLink connect(String destination) {
        return RDFLinkHTTP.service(destination).build();
    }

    /** Create a connection specifying the URLs of the service.
     *
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFLink
     */
    public static RDFLink connect(String queryServiceEndpoint,
                                  String updateServiceEndpoint,
                                  String graphStoreProtocolEndpoint) {
        return RDFLinkHTTP.newBuilder()
            .queryEndpoint(queryServiceEndpoint)
            .updateEndpoint(updateServiceEndpoint)
            .gspEndpoint(graphStoreProtocolEndpoint)
            .build();
    }

    /** Create a builder for a connection to a remote location by URL.
     * This is the URL for the dataset.
     *
     * @param destination
     * @return RDFLink
     * @see RDFLinkHTTPBuilder
     */
    public static RDFLinkHTTPBuilder newBuilder(String destination) {
        return RDFLinkHTTP.newBuilder().destination(destination);
    }

    /** Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     * Each service is then specified by a URL which is relative to the {@code datasetURL}.
     *
     * @param datasetURL
     * @param queryServiceEndpoint
     * @param updateServiceEndpoint
     * @param graphStoreProtocolEndpoint
     * @return RDFLink
     */
    public static RDFLink connect(String datasetURL,
                                  String queryServiceEndpoint,
                                  String updateServiceEndpoint,
                                  String graphStoreProtocolEndpoint) {
        return RDFLinkHTTP.newBuilder()
            .destination(datasetURL)
            .queryEndpoint(queryServiceEndpoint)
            .updateEndpoint(updateServiceEndpoint)
            .gspEndpoint(graphStoreProtocolEndpoint)
            .build();
    }

    /**
     * Connect to a local (same JVM) dataset.
     * The default isolation is {@code NONE}.
     * See {@link #connect(DatasetGraph, Isolation)} to select an isolation mode.
     *
     * @param dataset
     * @return RDFLink
     * @see RDFLinkDataset
     */
    public static RDFLink connect(DatasetGraph dataset) {
        return RDFLinkDatasetBuilder.connect(dataset);
    }

    /**
     * Connect to a local (same JVM) dataset.
     * <p>
     * Multiple levels of {@link Isolation} are provided, The default {@code COPY} level makes a local
     * {@link RDFLink} behave like a remote conenction.
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
     * @return RDFLink
     */
    public static RDFLink connect(DatasetGraph dataset, Isolation isolation) {
        return RDFLinkDatasetBuilder.connect(dataset, isolation);
    }

    /** Create a connection to a remote Fuseki server by URL.
     * This is the URL for the dataset.
     * <p>
     * A {@link RDFLinkFuseki} is an {@link RDFLink} that:
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
     * @return RDFLinkFuseki
     */
    public static RDFLinkFuseki connectFuseki(String destination) {
        return (RDFLinkFuseki)RDFLinkFuseki.newBuilder().destination(destination).build();
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
     * @return RDFLinkFuseki
     */
    public static RDFLinkFuseki connectFuseki(String datasetURL,
                                              String queryServiceEndpoint,
                                              String updateServiceEndpoint,
                                              String graphStoreProtocolEndpoint) {
        return (RDFLinkFuseki)RDFLinkFuseki.newBuilder()
                .destination(datasetURL)
                .queryEndpoint(queryServiceEndpoint)
                .updateEndpoint(updateServiceEndpoint)
                .gspEndpoint(graphStoreProtocolEndpoint)
                .build();
        }
}
