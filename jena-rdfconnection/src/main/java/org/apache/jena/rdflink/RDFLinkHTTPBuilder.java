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

import static java.util.Objects.requireNonNull;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.http.HttpEnv;
import org.apache.jena.rdflink.RDFLinkHTTPBuilder;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;

/** Builder class for {@link RDFLinkHTTP} */
public class RDFLinkHTTPBuilder {
    /*package*/ static String SameAsDestination  = "";

    protected Transactional txnLifecycle  = TransactionalLock.createMRPlusSW();
    protected HttpClient    httpClient    = null;
    protected String        destination   = null;

    protected String        sQuery        = SameAsDestination;
    protected String        sUpdate       = SameAsDestination;
    protected String        sGSP          = SameAsDestination;

    protected String        queryURL      = null;
    protected String        updateURL     = null;
    protected String        gspURL        = null;

    // On-the-wire settings.
    protected RDFFormat     outputQuads        = HttpEnv.defaultQuadsFormat;
    protected RDFFormat     outputTriples      = HttpEnv.defaultTriplesFormat;

    protected String        acceptGraph        = WebContent.defaultGraphAcceptHeader;
    protected String        acceptDataset      = WebContent.defaultDatasetAcceptHeader;

    protected String        acceptSelectResult = WebContent.defaultSparqlResultsHeader;
    protected String        acceptAskResult    = WebContent.defaultSparqlAskHeader;
    // All-purpose head that works for any query type (but is quite long!)
    protected String        acceptSparqlResults = WebContent.defaultSparqlResultsHeader;
    // Whether to parse SPARQL Queries and Updates for checking purposes.
    protected boolean       parseCheckQueries   = true;
    protected boolean       parseCheckUpdates   = true;

    protected RDFLinkHTTPBuilder() {
        // Default settings are the member declarations.
    }

    protected RDFLinkHTTPBuilder(RDFLinkHTTP base) {
        Objects.requireNonNull(base);
        txnLifecycle = base.txnLifecycle;
        if ( txnLifecycle == null )
            txnLifecycle = TransactionalLock.createMRPlusSW();
        httpClient          = base.httpClient;
        destination         = base.destination;
        sQuery              = base.svcQuery;
        sUpdate             = base.svcUpdate;
        sGSP                = base.svcGraphStore;
        outputQuads         = base.outputQuads;
        outputTriples       = base.outputTriples;

        acceptGraph         = base.acceptGraph;
        acceptDataset       = base.acceptDataset;

        acceptSelectResult  = base.acceptSelectResult;
        acceptAskResult     = base.acceptAskResult;
        parseCheckQueries   = base.parseCheckQueries;
        parseCheckUpdates   = base.parseCheckUpdates;
    }

    /** URL of the remote SPARQL endpoint.
     * For Fuseki, this is the URL of the dataset  e.g. http:/localhost:3030/dataset
     */
    public RDFLinkHTTPBuilder destination(String destination) {
        Objects.requireNonNull(destination);
        this.destination = destination;
        return this;
    }

    public RDFLinkHTTPBuilder queryOnly() {
        sUpdate = null;
        sGSP = null;
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
    public RDFLinkHTTPBuilder queryEndpoint(String sQuery) {
        this.sQuery = sQuery;
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
    public RDFLinkHTTPBuilder updateEndpoint(String sUpdate) {
        this.sUpdate = sUpdate;
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
    public RDFLinkHTTPBuilder gspEndpoint(String sGSP) {
        this.sGSP = sGSP;
        return this;
    }

    /** Set the transaction lifecycle. */
    /*Future possibility*/
    private RDFLinkHTTPBuilder txnLifecycle(Transactional txnLifecycle) {
        this.txnLifecycle = txnLifecycle;
        return this;

    }

    /** Set the {@link HttpClient} fir the connection to tbe built */
    public RDFLinkHTTPBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset.
     * This must be a quads format.
     */
    public RDFLinkHTTPBuilder quadsFormat(RDFFormat fmtQuads) {
        if ( ! RDFLanguages.isQuads(fmtQuads.getLang()) )
            throw new RiotException("Not a language for RDF Datasets: "+fmtQuads);
        this.outputQuads = fmtQuads;
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset.
     * This must be a quads format.
     */
    public RDFLinkHTTPBuilder quadsFormat(Lang langQuads) {
        Objects.requireNonNull(langQuads);
        if ( ! RDFLanguages.isQuads(langQuads) )
            throw new RiotException("Not a language for RDF Datasets: "+langQuads);
        RDFFormat fmt = RDFWriterRegistry.defaultSerialization(langQuads);
        if ( fmt == null )
            throw new RiotException("Language name not recognized: "+langQuads);
        quadsFormat(fmt);
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset.
     * This must be a quads format.
     */
    public RDFLinkHTTPBuilder quadsFormat(String langQuads) {
        Objects.requireNonNull(langQuads);
        Lang lang = RDFLanguages.nameToLang(langQuads);
        if ( lang == null )
            throw new RiotException("Language name not recognized: "+langQuads);
        quadsFormat(lang);
        return this;
    }

    /** Set the output format for sending RDF graphs to the remote server.
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFLinkHTTPBuilder triplesFormat(RDFFormat fmtTriples) {
        if ( ! RDFLanguages.isTriples(fmtTriples.getLang()) )
            throw new RiotException("Not a language for RDF Graphs: "+fmtTriples);
        this.outputTriples = fmtTriples;
        return this;
    }

    /** Set the output format for sending RDF graphs to the remote server.
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFLinkHTTPBuilder triplesFormat(Lang langTriples) {
        Objects.requireNonNull(langTriples);
        if ( ! RDFLanguages.isTriples(langTriples) )
            throw new RiotException("Not a language for RDF triples: "+langTriples);
        RDFFormat fmt = RDFWriterRegistry.defaultSerialization(langTriples);
        if ( fmt == null )
            throw new RiotException("Language name not recognized: "+langTriples);
        triplesFormat(fmt);
        return this;
    }

    /** Set the output format for sending RDF graphs to the remote server.
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFLinkHTTPBuilder triplesFormat(String langTriples) {
        Objects.requireNonNull(langTriples);
        Lang lang = RDFLanguages.nameToLang(langTriples);
        if ( lang == null )
            throw new RiotException("Language name not recognized: "+langTriples);
        quadsFormat(lang);
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to fetch RDF graph using the SPARQL Graph Store Protocol. */
    public RDFLinkHTTPBuilder acceptHeaderGraph(String acceptGraph) {
        this.acceptGraph = acceptGraph;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to fetch RDF datasets using HTTP GET operations. */
    public RDFLinkHTTPBuilder acceptHeaderDataset(String acceptDataset) {
        this.acceptDataset = acceptDataset;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a SPARQL Protocol SELECT query. */
    public RDFLinkHTTPBuilder acceptHeaderSelectQuery(String acceptSelectHeader) {
        this.acceptSelectResult = acceptSelectHeader;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a SPARQL Protocol ASK query. */
    public RDFLinkHTTPBuilder acceptHeaderAskQuery(String acceptAskHeader) {
        this.acceptAskResult = acceptAskHeader;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a
     * SPARQL Protocol query if no query type specific setting available.
     */
    public RDFLinkHTTPBuilder acceptHeaderQuery(String acceptHeader) {
        this.acceptSparqlResults = acceptHeader;
        return this;
    }

    /**
     * Set the flag for whether to check SPARQL queries and SPARQL updates provided as a string.
     */
    public RDFLinkHTTPBuilder parseCheckSPARQL(boolean parseCheck) {
        this.parseCheckQueries = parseCheck;
        this.parseCheckUpdates = parseCheck;
        return this;
    }

    private Function<RDFLinkHTTPBuilder, RDFLink> creator = null;
    /** Provide an alternative function to make the {@link RDFLink} object.
     * <p>
     * Specialized use: This method allows for custom {@code RDFLink}s.
     */
    public RDFLinkHTTPBuilder creator(Function<RDFLinkHTTPBuilder, RDFLink> function) {
        this.creator = function;
        return this;
    }

    /** Build an {RDFLink}. */
    public RDFLink build() {
        requireNonNull(txnLifecycle);

        Function<RDFLinkHTTPBuilder, RDFLink> maker = creator ;

        if ( maker == null )
            maker = (b)->b.buildConnection();

        // Sort out service URLs.
        // Delay until here. The builder may be setting destination and service endpoint
        // names. We can't calculate the full URL until build() is called.

        queryURL = LibRDFLink.formServiceURL(destination, sQuery);
        updateURL = LibRDFLink.formServiceURL(destination, sUpdate);
        gspURL = LibRDFLink.formServiceURL(destination, sGSP);

        return maker.apply(this);
    }

    protected RDFLinkHTTP buildConnection() {
        return new RDFLinkHTTP(txnLifecycle, httpClient,
                                 destination, queryURL, updateURL, gspURL,
                                 outputQuads, outputTriples,
                                 acceptDataset, acceptGraph,
                                 acceptSparqlResults, acceptSelectResult, acceptAskResult,
                                 parseCheckQueries, parseCheckUpdates);
    }
}
