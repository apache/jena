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

package org.apache.jena.web ;

import java.io.IOException ;
import java.io.OutputStream ;

import org.apache.http.HttpEntity ;
import org.apache.http.client.methods.HttpHead ;
import org.apache.http.client.methods.HttpUriRequest ;
import org.apache.http.entity.ContentProducer ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.auth.HttpAuthenticator ;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.system.IRILib ;
import org.apache.jena.riot.web.* ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.JenaException ;

/**
 * A dataset graph accessor that talks to stores that implement the SPARQL 1.1
 * Graph Store Protocol
 */
public class DatasetGraphAccessorHTTP implements DatasetGraphAccessor {
    // Test for this class are in Fuseki so they can be run with a server.

    private static final HttpResponseHandler noResponse        = HttpResponseLib.nullResponse ;
    /** Format used to send a graph to the server */
    private static final RDFFormat           defaultSendLang   = RDFFormat.RDFXML_PLAIN ;

    private final String                     remote ;
    private HttpAuthenticator                authenticator ;

    private RDFFormat                        formatPutPost     = defaultSendLang ;

    /**
     * Accept header for fetching graphs - prefer N-triples.
     * @See WebContent.defaultGraphAcceptHeader
     */
    private String                           graphAcceptHeader = WebContent.defaultGraphAcceptHeader ;  

    /** RDF syntax to use when sending graphs with POST and PUT. */
    public RDFFormat getOutboundSyntax()  { return formatPutPost ; }

    /** Set the RDF syntax to use when sending graphs with POST and PUT. Defaults to RDF/XML. */
    public void setOutboundSyntax(RDFFormat format)  { formatPutPost = format ; }

    /** HTTP accept header used to GET a graph. */
    public String getGraphAcceptHeader()  { return graphAcceptHeader ; }

    /** Set the HTTP accept header used to GET a graph. */
    public void setGraphAcceptHeader(String header)  { graphAcceptHeader = header ; }

    /**
     * Create a DatasetUpdater for the remote URL
     * 
     * @param remote
     *            Remote URL
     */
    public DatasetGraphAccessorHTTP(String remote) {
        this.remote = remote ;
    }

    /**
     * Create a DatasetUpdater for the remote URL
     * 
     * @param remote
     *            Remote URL
     * @param authenticator
     *            HTTP Authenticator
     */
    public DatasetGraphAccessorHTTP(String remote, HttpAuthenticator authenticator) {
        this(remote) ;
        this.setAuthenticator(authenticator) ;
    }

    /**
     * Sets authentication credentials for the remote URL
     * 
     * @param username
     *            User name
     * @param password
     *            Password
     */
    public void setAuthentication(String username, char[] password) {
        this.setAuthenticator(new SimpleAuthenticator(username, password)) ;
    }

    /**
     * Sets an authenticator to use for authentication to the remote URL
     * 
     * @param authenticator
     *            Authenticator
     */
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator ;
    }

    @Override
    public Graph httpGet() {
        return doGet(targetDefault()) ;
    }

    @Override
    public Graph httpGet(Node graphName) {
        return doGet(target(graphName)) ;
    }

    protected Graph doGet(String url) {
        HttpCaptureResponse<Graph> graph = HttpResponseLib.graphHandler() ;
        try {
            HttpOp.execHttpGet(url, graphAcceptHeader, graph, this.authenticator) ;
        } catch (HttpException ex) {
            if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 )
                return null ;
            throw ex ;
        }
        return graph.get() ;
    }

    @Override
    public boolean httpHead() {
        return doHead(targetDefault()) ;
    }

    @Override
    public boolean httpHead(Node graphName) {
        return doHead(target(graphName)) ;
    }

    protected boolean doHead(String url) {
        HttpUriRequest httpHead = new HttpHead(url) ;
        try {
            HttpOp.execHttpHead(url, WebContent.defaultGraphAcceptHeader, noResponse, null, null, this.authenticator) ;
            return true ;
        } catch (HttpException ex) {
            if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 )
                return false ;
            throw ex ;
        }
    }

    @Override
    public void httpPut(Graph data) {
        doPut(targetDefault(), data) ;
    }

    @Override
    public void httpPut(Node graphName, Graph data) {
        doPut(target(graphName), data) ;
    }

    protected void doPut(String url, Graph data) {
        HttpEntity entity = graphToHttpEntity(data) ;
        HttpOp.execHttpPut(url, entity, null, null, this.authenticator) ;
    }

    @Override
    public void httpDelete() {
        doDelete(targetDefault()) ;
    }

    @Override
    public void httpDelete(Node graphName) {
        doDelete(target(graphName)) ;
    }

    protected void doDelete(String url) {
        try {
            HttpOp.execHttpDelete(url, noResponse, null, null, this.authenticator) ;
        } catch (HttpException ex) {
            if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 )
                return ;
        }
    }

    @Override
    public void httpPost(Graph data) {
        doPost(targetDefault(), data) ;
    }

    @Override
    public void httpPost(Node graphName, Graph data) {
        doPost(target(graphName), data) ;
    }

    protected void doPost(String url, Graph data) {
        HttpEntity entity = graphToHttpEntity(data) ;
        HttpOp.execHttpPost(url, entity, null, null, this.authenticator) ;
    }

    @Override
    public void httpPatch(Graph data) {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public void httpPatch(Node graphName, Graph data) {
        throw new UnsupportedOperationException() ;
    }

    protected final String targetDefault() {
        return remote + "?" + HttpNames.paramGraphDefault + "=" ;
    }

    protected final String target(Node name) {
        if ( !name.isURI() )
            throw new JenaException("Not a URI: " + name) ;
        String guri = name.getURI() ;
        // Encode
        guri = IRILib.encodeUriComponent(guri) ;
        return remote + "?" + HttpNames.paramGraph + "=" + guri ;
    }

    /** Create an HttpEntity for the graph */  
    protected HttpEntity graphToHttpEntity(final Graph graph) {

        final RDFFormat syntax = getOutboundSyntax() ;
        ContentProducer producer = new ContentProducer() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                RDFDataMgr.write(out, graph, syntax) ;
            }
        } ;

        EntityTemplate entity = new EntityTemplate(producer) ;
        String ct = syntax.getLang().getContentType().getContentType() ;
        entity.setContentType(ct) ;
        return entity ;
    }
}
