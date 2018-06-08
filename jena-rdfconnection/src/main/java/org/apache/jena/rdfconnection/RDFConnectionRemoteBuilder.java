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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Function;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/** Builder class for {@link RDFConnectionRemote} */
public class RDFConnectionRemoteBuilder {
    /*package*/ static String SameAsDestination  = "";

    protected Transactional txnLifecycle  = TransactionalLock.createMRPlusSW();
    protected HttpClient    httpClient    = null;
    protected HttpContext   httpContext   = null;
    protected String        destination   = null;
    
    protected String        sQuery        = SameAsDestination;
    protected String        sUpdate       = SameAsDestination;
    protected String        sGSP          = SameAsDestination;

    protected String        queryURL      = null;
    protected String        updateURL     = null;
    protected String        gspURL        = null;

    // On-the-wire settings.
    protected RDFFormat     outputQuads        = RDFFormat.NQUADS;
    protected RDFFormat     outputTriples      = RDFFormat.NTRIPLES;
    
    protected String        acceptGraph        = WebContent.defaultGraphAcceptHeader;
    protected String        acceptDataset      = WebContent.defaultDatasetAcceptHeader;
    
    protected String        acceptSelectResult = QueryEngineHTTP.defaultSelectHeader();
    protected String        acceptAskResult    = QueryEngineHTTP.defaultAskHeader();
    // All-purpose head that works for any query type (but is quite long!)
    protected String        acceptSparqlResults = acceptSelectResult+","+acceptGraph;
    // Whether to parse SPARQL Queries and Updates for checkign purposes.
    protected boolean       parseCheckQueries   = true;
    protected boolean       parseCheckUpdates   = true;

    RDFConnectionRemoteBuilder() { 
        // Default settings are the meber declarations.
    }
    
    RDFConnectionRemoteBuilder(RDFConnectionRemote base) {
        Objects.requireNonNull(base);
        txnLifecycle = base.txnLifecycle;
        if ( txnLifecycle == null )
            txnLifecycle = TransactionalLock.createMRPlusSW();
        httpClient          = base.httpClient;
        httpContext         = base.httpContext;
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
    public RDFConnectionRemoteBuilder destination(String destination) {
        Objects.requireNonNull(destination);
        this.destination = destination;
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
    public RDFConnectionRemoteBuilder updateEndpoint(String sUpdate) {
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
    public RDFConnectionRemoteBuilder gspEndpoint(String sGSP) {
        this.sGSP = sGSP;
        return this;
    }
    
    /** Set the transaction lifecycle. */
    /*Future possibility*/
    private RDFConnectionRemoteBuilder txnLifecycle(Transactional txnLifecycle) {
        this.txnLifecycle = txnLifecycle;
        return this;
    
    }

    /** Set the {@link HttpClient} fir the connection to tbe built */
    public RDFConnectionRemoteBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /** Set the {@link HttpContext} for the connection to tbe built */
    public RDFConnectionRemoteBuilder httpContext(HttpContext httpContext) {
        this.httpContext = httpContext;
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset. 
     * This must be a quads format.
     */
    public RDFConnectionRemoteBuilder quadsFormat(RDFFormat fmtQuads) {
        if ( ! RDFLanguages.isQuads(fmtQuads.getLang()) )
            throw new RiotException("Not a language for RDF Datasets: "+fmtQuads);
        this.outputQuads = fmtQuads;
        return this;
    }

    /** Set the output format for sending RDF Datasets to the remote server.
     * This is used for HTTP PUT and POST to a dataset. 
     * This must be a quads format.
     */
    public RDFConnectionRemoteBuilder quadsFormat(Lang langQuads) {
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
    public RDFConnectionRemoteBuilder quadsFormat(String langQuads) {
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
    public RDFConnectionRemoteBuilder triplesFormat(RDFFormat fmtTriples) {
        if ( ! RDFLanguages.isTriples(fmtTriples.getLang()) )
            throw new RiotException("Not a language for RDF Graphs: "+fmtTriples);
        this.outputTriples = fmtTriples;
        return this;
    }
    
    /** Set the output format for sending RDF graphs to the remote server. 
     * This is used for the SPARQ Graph Store Protocol.
     */
    public RDFConnectionRemoteBuilder triplesFormat(Lang langTriples) {
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
    public RDFConnectionRemoteBuilder triplesFormat(String langTriples) {
        Objects.requireNonNull(langTriples);
        Lang lang = RDFLanguages.nameToLang(langTriples);
        if ( lang == null )
            throw new RiotException("Language name not recognized: "+langTriples);
        quadsFormat(lang);
        return this;
    }
    
    /** Set the HTTP {@code Accept:} header used to fetch RDF graph using the SPARQL Graph Store Protocol. */ 
    public RDFConnectionRemoteBuilder acceptHeaderGraph(String acceptGraph) {
        this.acceptGraph = acceptGraph;
        return this;
    }
    
    /** Set the HTTP {@code Accept:} header used to fetch RDF datasets using HTTP GET operations. */ 
    public RDFConnectionRemoteBuilder acceptHeaderDataset(String acceptDataset) {
        this.acceptDataset = acceptDataset;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a SPARQL Protocol SELECT query. */ 
    public RDFConnectionRemoteBuilder acceptHeaderSelectQuery(String acceptSelectHeader) {
        this.acceptSelectResult = acceptSelectHeader;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a SPARQL Protocol ASK query. */ 
    public RDFConnectionRemoteBuilder acceptHeaderAskQuery(String acceptAskHeader) {
        this.acceptAskResult = acceptAskHeader;
        return this;
    }

    /** Set the HTTP {@code Accept:} header used to when making a 
     * SPARQL Protocol query if no query type specific setting available.
     */ 
    public RDFConnectionRemoteBuilder acceptHeaderQuery(String acceptHeader) {
        this.acceptSparqlResults = acceptHeader;
        return this;
    }
    
    /**
     * Set the flag for whether to check SPARQL queries and SPARQL updates provided as a string.   
     */
    public RDFConnectionRemoteBuilder parseCheckSPARQL(boolean parseCheck) {
        this.parseCheckQueries = parseCheck;
        this.parseCheckUpdates = parseCheck;
        return this;
    }
    
    private Function<RDFConnectionRemoteBuilder, RDFConnection> creator = null;
    /** Provide an alternative function to make the {@link RDFConnection} object.
     * <p>
     * Specialized use: This method allows for custom {@code RDFConnection}s.
     */
    public RDFConnectionRemoteBuilder creator(Function<RDFConnectionRemoteBuilder, RDFConnection> function) {
        this.creator = function;
        return this;
    }

    /** Build an {RDFConnection}. */ 
    public RDFConnection build() {
        requireNonNull(txnLifecycle);
        requireNonNull(destination);
        
        Function<RDFConnectionRemoteBuilder, RDFConnection> maker = creator ;
        
        if ( maker == null )
            maker = (b)->b.buildConnection();
        
        // Sort out service URLs.
        // Delay until here. The builder may be setting destination and service endpoint
        // names. We can't calculate the full URL until build() is called.
        
        queryURL = LibRDFConn.formServiceURL(destination, sQuery);
        updateURL = LibRDFConn.formServiceURL(destination, sUpdate);
        gspURL = LibRDFConn.formServiceURL(destination, sGSP);
        
        return maker.apply(this);
    }
    
    protected RDFConnectionRemote buildConnection() {
        return new RDFConnectionRemote(txnLifecycle, httpClient, httpContext, 
                                        destination, queryURL, updateURL, gspURL,
                                        outputQuads, outputTriples,
                                        acceptDataset, acceptGraph,
                                        acceptSparqlResults, acceptSelectResult, acceptAskResult,
                                        parseCheckQueries, parseCheckUpdates);
    }
}