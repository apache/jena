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

package org.apache.jena.web;

import java.io.IOException ;
import java.io.OutputStream ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpVersion ;
import org.apache.http.client.methods.HttpHead ;
import org.apache.http.client.methods.HttpUriRequest ;
import org.apache.http.entity.ContentProducer ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.http.params.BasicHttpParams ;
import org.apache.http.params.HttpConnectionParams ;
import org.apache.http.params.HttpParams ;
import org.apache.http.params.HttpProtocolParams ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.IRILib ;
import org.apache.jena.riot.web.* ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.JenaException ;

/**
 * A dataset graph accessor that talks to stores that implement the SPARQL 1.1 Graph Store Protocol
 *
 */
public class DatasetGraphAccessorHTTP implements DatasetGraphAccessor
{
    // Test for this class are in Fuseki so they can be run with a server. 
    
    private final String remote ;
    private static final HttpResponseHandler noResponse = HttpResponseLib.nullResponse ;
    private HttpAuthenticator authenticator;

    /** Format used to send a graph to the server */ 
    private static RDFFormat sendLang = RDFFormat.RDFXML_PLAIN ;

    /** 
     * Create a DatasetUpdater for the remote URL 
     * @param remote Remote URL
     */
    public DatasetGraphAccessorHTTP(String remote)
    {
        this.remote = remote ;
    }
    
    /** 
     * Create a DatasetUpdater for the remote URL 
     * @param remote Remote URL
     * @param authenticator HTTP Authenticator
     */
    public DatasetGraphAccessorHTTP(String remote, HttpAuthenticator authenticator) {
        this(remote);
        this.setAuthenticator(authenticator);
    }
    
    /**
     * Sets authentication credentials for the remote URL
     * @param username User name
     * @param password Password
     */
    public void setAuthentication(String username, char[] password) {
        this.setAuthenticator(new SimpleAuthenticator(username, password));
    }
    
    /**
     * Sets an authenticator to use for authentication to the remote URL
     * @param authenticator Authenticator
     */
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    @Override
    public Graph httpGet()                            { return doGet(targetDefault()) ; }

    @Override
    public Graph httpGet(Node graphName)              { return doGet(target(graphName)) ; }
    
    /** Accept header for fetching graphs - prefer N-triples
     * @See WebContent.defaultGraphAcceptHeader 
     *  
     */
    private static String GetAcceptHeader = "application/n-triples,text/turtle;q=0.9,application/rdf+xml;q=0.8,application/xml;q=0.7" ;
    
    private Graph doGet(String url)
    {
        HttpCaptureResponse<Graph> graph = HttpResponseLib.graphHandler() ;
        try {
            HttpOp.execHttpGet(url, GetAcceptHeader, graph, this.authenticator) ;
        } catch (HttpException ex) {
            if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 )
                return null ;
            throw ex ;
        }
        return graph.get(); 
    }
    
    @Override
    public boolean httpHead()
    {
        return doHead(targetDefault()) ;
    }
    
    @Override
    public boolean httpHead(Node graphName)
    {
        return doHead(target(graphName)) ;
    }

    private boolean doHead(String url)
    {
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
    public void httpPut(Graph data)                   { doPut(targetDefault(), data) ; }

    @Override
    public void httpPut(Node graphName, Graph data)   { doPut(target(graphName), data) ; }

    private void doPut(String url, Graph data)
    {
        HttpEntity entity = graphToHttpEntity(data) ;
        HttpOp.execHttpPut(url, entity, null, null, this.authenticator) ;
    }
    
    @Override
    public void httpDelete()                          { doDelete(targetDefault()) ; }

    @Override
    public void httpDelete(Node graphName)            { doDelete(target(graphName)) ; }

    private void doDelete(String url)
    {
        try {
            HttpOp.execHttpDelete(url, noResponse, null, null, this.authenticator) ;
        } catch (HttpException ex) {
            if ( ex.getResponseCode() == HttpSC.NOT_FOUND_404 )
                return ;
        }
    }
    
    @Override
    public void httpPost(Graph data)                  { doPost(targetDefault(), data) ; }

    @Override
    public void httpPost(Node graphName, Graph data)  { doPost(target(graphName), data) ; }

    private void doPost(String url, Graph data)
    {
        HttpEntity entity = graphToHttpEntity(data) ;
        HttpOp.execHttpPost(url, entity, null, null, this.authenticator) ;
    }

    @Override
    public void httpPatch(Graph data)                 { throw new UnsupportedOperationException() ; }

    @Override
    public void httpPatch(Node graphName, Graph data) { throw new UnsupportedOperationException() ; }

    private String targetDefault()
    {
        return remote+"?"+HttpNames.paramGraphDefault+"=" ;
    }

    private String target(Node name)
    {
        if ( ! name.isURI() )
            throw new JenaException("Not a URI: "+name) ;
        String guri = name.getURI() ;
        // Encode
        guri = IRILib.encodeUriComponent(guri) ;
        return remote+"?"+HttpNames.paramGraph+"="+guri ;
    }
    
    // TODO: Move default parameters into HttpOp and use in ensureClient()

    static private HttpParams httpParams = createHttpParams() ;
    
    static private HttpParams createHttpParams()
    {
        HttpParams httpParams$ = new BasicHttpParams() ;
        // See DefaultHttpClient.createHttpParams
        HttpProtocolParams.setVersion(httpParams$,               HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams$,        WebContent.charsetUTF8);
        HttpProtocolParams.setUseExpectContinue(httpParams$,     true);
        HttpConnectionParams.setTcpNoDelay(httpParams$,          true);
        HttpConnectionParams.setSocketBufferSize(httpParams$,    32*1024);
        HttpProtocolParams.setUserAgent(httpParams$,             Jena.NAME+"/"+Jena.VERSION);
        return httpParams$;
    }
    
    private static HttpEntity graphToHttpEntity(final Graph graph) {
        
        ContentProducer producer = new ContentProducer() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                RDFDataMgr.write(out, graph, sendLang) ;
            }
        } ;
        
        EntityTemplate entity = new EntityTemplate(producer) ;
        String ct = sendLang.getLang().getContentType().getContentType() ;
        entity.setContentType(ct) ;
        return entity ;
    }        
}
