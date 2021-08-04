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

import java.net.http.HttpClient;

import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.rdflink.RDFLinkHTTPBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

/** Builder class for {@link RDFConnectionRemote} */

/**
 * @see RDFLinkHTTPBuilder
 */
public class RDFConnectionRemoteBuilder {
    /*package*/ static String SameAsDestination  = "";

    protected final RDFLinkHTTPBuilder builder;

    protected RDFConnectionRemoteBuilder() {
        this(RDFLinkHTTP.newBuilder());
    }

    protected RDFConnectionRemoteBuilder(RDFLinkHTTPBuilder builder) {
        this.builder = builder;
    }

    /** URL of the remote SPARQL endpoint.
     * For Fuseki, this is the URL of the dataset  e.g. http://localhost:3030/dataset
     */
    public RDFConnectionRemoteBuilder destination(String destination) {
        builder.destination(destination);
        return this;
    }

    /** Name of the SPARQL query service.
     * <p>
     * This can be a short name, relative to the destination URL,
     * or a full URL (with "http:" or "https:")
     * <p>
     * Use {@code ""} for "same as destination".
     * <br/>
     * Use null for "none".
     */
    public RDFConnectionRemoteBuilder queryEndpoint(String sQuery) {
        builder.queryEndpoint(sQuery);
        return this;
    }

    /** Name of the SPARQL update service.
     * <p>
     * This can be a short name, relative to the destination URL,
     * or a full URL (with "http:" or "https:")
     * <p>
     * Use {@code ""} for "same as destination".
     * <br/>
     * Use null for "none".
     */
    public RDFConnectionRemoteBuilder updateEndpoint(String sUpdate) {
        builder.updateEndpoint(sUpdate);
        return this;
    }

    /** Name of the SPARQL GraphStore Protocol endpoint.
     * <p>
     * This can be a short name, relative to the destination URL,
     * or a full URL (with "http:" or "https:")
     * <p>
     * Use {@code ""} for "same as destination".
     * <br/>
     * Use null for "none".
     */
    public RDFConnectionRemoteBuilder gspEndpoint(String sGSP) {
		builder.gspEndpoint(sGSP);
        return this;
    }

//    /** Set the transaction lifecycle. */
//    /*Future possibility*/
//    private RDFConnectionRemoteBuilder txnLifecycle(Transactional txnLifecycle) {
//        builder.txnLifecycle(txnLifecycle);
//        return this;
//    }

    /** Set the {@link HttpClient} for the connection to be built */
    public RDFConnectionRemoteBuilder httpClient(HttpClient httpClient) {
		builder.httpClient(httpClient);
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset.
     * This must be a quads format.
     */
    public RDFConnectionRemoteBuilder quadsFormat(RDFFormat fmtQuads) {
		builder.quadsFormat(fmtQuads);
        return this;
    }

    /**
     * Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset.
     * This must be a quads format.
     */
    public RDFConnectionRemoteBuilder quadsFormat(Lang langQuads) {
		builder.quadsFormat(langQuads);
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset.
     * This must be a quads format.
     */
    public RDFConnectionRemoteBuilder quadsFormat(String langQuads) {
		builder.quadsFormat(langQuads);
        return this;
    }

    /** Set the output format for sending RDF graphs to the remote server.
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFConnectionRemoteBuilder triplesFormat(RDFFormat fmtTriples) {
		builder.triplesFormat(fmtTriples);
        return this;
    }

    /** Set the output format for sending RDF graphs to the remote server.
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFConnectionRemoteBuilder triplesFormat(Lang langTriples) {
		builder.triplesFormat(langTriples);
        return this;
    }

    /** Set the output format for sending RDF graphs to the remote server.
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFConnectionRemoteBuilder triplesFormat(String langTriples) {
		builder.triplesFormat(langTriples);
        Lang lang = RDFLanguages.nameToLang(langTriples);
        if ( lang == null )
            throw new RiotException("Language name not recognized: "+langTriples);
        quadsFormat(lang);
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to fetch RDF graph using the SPARQL Graph Store Protocol. */
    public RDFConnectionRemoteBuilder acceptHeaderGraph(String acceptGraph) {
		builder.acceptHeaderGraph(acceptGraph);
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to fetch RDF datasets using HTTP GET operations. */
    public RDFConnectionRemoteBuilder acceptHeaderDataset(String acceptDataset) {
		builder.acceptHeaderDataset(acceptDataset);
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a SPARQL Protocol SELECT query. */
    public RDFConnectionRemoteBuilder acceptHeaderSelectQuery(String acceptSelectHeader) {
		builder.acceptHeaderSelectQuery(acceptSelectHeader);
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a SPARQL Protocol ASK query. */
    public RDFConnectionRemoteBuilder acceptHeaderAskQuery(String acceptAskHeader) {
		builder.acceptHeaderAskQuery(acceptAskHeader);
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a
     * SPARQL Protocol query if no query type specific setting available.
     */
    public RDFConnectionRemoteBuilder acceptHeaderQuery(String acceptHeader) {
		builder.acceptHeaderQuery(acceptHeader);
        return this;
    }

    /**
     * Set the flag for whether to check SPARQL queries and SPARQL updates provided as a string.
     */
    public RDFConnectionRemoteBuilder parseCheckSPARQL(boolean parseCheck) {
		builder.parseCheckSPARQL(parseCheck);
        return this;
    }

    /**
     * Build an {@link RDFConnection}.
     *
     * @implNote This operation is fixed as:
     *
     *     <pre>
     *     public final RDFConnection build() {
     *         RDFLink rdfLink = buildLink();
     *         return adapt(rdfLink);
     *     }
     *     </pre>
     *
     *     Subclasses of {@link RDFConnectionRemote} may build and override {@code buildLink}
     *     and/or {@code adaptLink}
     */
    public final RDFConnection build() {
        RDFLink rdfLink = buildLink();
        return adaptLink(rdfLink);
    }

    protected RDFLink buildLink() {
        return  builder.build();
    }

    protected RDFConnection adaptLink(RDFLink rdfLink) {
        return RDFConnectionAdapter.adapt(rdfLink);
    }
}
