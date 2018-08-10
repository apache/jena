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

package org.apache.jena.fuseki;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Convert ;

public class FusekiLib {
    // ==> ActionLib
    
    /** Get the content type of an action or return the default.
     * @param  action
     * @return ContentType
     */
    public static ContentType getContentType(HttpAction action) {
        return getContentType(action.request) ;
    }
    
    /** Get the content type of an action or return the default.
     * @param  request
     * @return ContentType
     */
    public static ContentType getContentType(HttpServletRequest request) {
        String contentTypeHeader = request.getContentType() ;
        if ( contentTypeHeader == null ) 
            return null ;
        return ContentType.create(contentTypeHeader) ;
    }
    
    /** Get the incoming {@link Lang} based on Content-Type of an action.
     * @param  action
     * @param  dft Default if no "Content-Type:" found. 
     * @return ContentType
     */
    public static Lang getLangFromAction(HttpAction action, Lang dft) {
        String contentTypeHeader = action.request.getContentType() ;
        if ( contentTypeHeader == null )
            return dft ;
        return RDFLanguages.contentTypeToLang(contentTypeHeader) ;
    }

    static String fmtRequest(HttpServletRequest request) {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(request.getMethod()) ;
        sbuff.append(" ") ;
        sbuff.append(Convert.decWWWForm(request.getRequestURL())) ;

        String qs = request.getQueryString() ;
        if ( qs != null ) {
            String tmp = request.getQueryString() ;
            tmp = Convert.decWWWForm(tmp) ;
            tmp = tmp.replace('\n', ' ') ;
            tmp = tmp.replace('\r', ' ') ;
            sbuff.append("?").append(tmp) ;
        }
        return sbuff.toString() ;
    }

    /** Parse the query string - do not process the body even for a form */
    public static Multimap<String, String> parseQueryString(HttpServletRequest req) {
        Multimap<String, String> map = ArrayListMultimap.create() ;

        // Don't use ServletRequest.getParameter or getParamterNames
        // as that reads form data. This code parses just the query string.
        if ( req.getQueryString() != null ) {
            String[] params = req.getQueryString().split("&") ;
            for (int i = 0; i < params.length; i++) {
                String p = params[i] ;
                String[] x = p.split("=", 2) ;
                String name = null ;
                String value = null ;

                if ( x.length == 0 ) { // No "="
                    name = p ;
                    value = "" ;
                } else if ( x.length == 1 ) { // param=
                    name = x[0] ;
                    value = "" ;
                } else { // param=value
                    name = x[0] ;
                    value = x[1] ;
                }
                map.put(name, value) ;
            }
        }
        return map ;
    }
    
    public static String safeParameter(HttpServletRequest request, String pName) {
        String value = request.getParameter(pName) ;
        if ( value == null )
            return null ;
        value = value.replace("\r", "") ;
        value = value.replace("\n", "") ;
        return value ;
    }

    // Do the addition directly on the dataset
    public static void addDataInto(Graph data, DatasetGraph dsg, Node graphName) {
        // Prefixes?
        if ( graphName == null )
            graphName = Quad.defaultGraphNodeGenerated ;

        Iterator<Triple> iter = data.find(Node.ANY, Node.ANY, Node.ANY) ;
        for (; iter.hasNext();) {
            Triple t = iter.next() ;
            dsg.add(graphName, t.getSubject(), t.getPredicate(), t.getObject()) ;
        }

        PrefixMapping pmapSrc = data.getPrefixMapping() ;
        PrefixMapping pmapDest = dsg.getDefaultGraph().getPrefixMapping() ;
        pmapDest.setNsPrefixes(pmapSrc) ;
    }
    
    public static void addDataInto(DatasetGraph src, DatasetGraph dest) {
        Iterator<Quad> iter = src.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY) ;
        for (; iter.hasNext();) {
            Quad q = iter.next() ;
            dest.add(q) ;
        }

        PrefixMapping pmapSrc = src.getDefaultGraph().getPrefixMapping() ;
        PrefixMapping pmapDest = dest.getDefaultGraph().getPrefixMapping() ;
        pmapDest.withDefaultMappings(pmapSrc) ;
    }

    /** Choose an unused port for a server to listen on */
    public static int choosePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException ex) {
            throw new FusekiException("Failed to find a port");
        }
    }

    /**
     * Test whether a URL identifies a Fuseki server. This operation can not guarantee to
     * detect a Fuseki server - for example, it may be behind a reverse proxy that masks
     * the signature.
     */
    public static boolean isFuseki(String datasetURL) {
        HttpOptions request = new HttpOptions(datasetURL);
        HttpClient httpClient = HttpOp.getDefaultHttpClient();
        if ( httpClient == null ) 
            httpClient = HttpClients.createSystem();
        return isFuseki(request, httpClient, null);
    }

    /**
     * Test whether a {@link RDFConnectionRemote} connects to a Fuseki server. This
     * operation can not guaranttee to detech a Fuseki server - for example, it may be
     * behind a reverse proxy that masks the signature.
     */
    public static boolean isFuseki(RDFConnectionRemote connection) {
        HttpOptions request = new HttpOptions(connection.getDestination());
        HttpClient httpClient = connection.getHttpClient();
        if ( httpClient == null ) 
            httpClient = HttpClients.createSystem();
        HttpContext httpContext = connection.getHttpContext();
        return isFuseki(request, httpClient, httpContext);
    }

    private static boolean isFuseki(HttpOptions request, HttpClient httpClient, HttpContext httpContext) {
        try {
            HttpResponse response = httpClient.execute(request);
            // Fuseki does not send "Server" in release mode.
            // (best practice).
            // All we can do is try for the "Fuseki-Request-ID" 
            String reqId = safeGetHeader(response, "Fuseki-Request-ID");
            if ( reqId != null )
                return true;

            // If returning "Server"
            String serverIdent = safeGetHeader(response, "Server");
            if ( serverIdent != null ) {
                Log.debug(ARQ.getHttpRequestLogger(), "Server: "+serverIdent);
                boolean isFuseki = serverIdent.startsWith("Apache Jena Fuseki");
                if ( !isFuseki )
                    isFuseki = serverIdent.toLowerCase().contains("fuseki");
                return isFuseki;
            }
            return false; 
        } catch (IOException ex) {
            throw new HttpException("Failed to check for a Fuseki server", ex);
        }
    }
    
    private static String safeGetHeader(HttpResponse response, String header) {
        Header h = response.getFirstHeader(header);
        if ( h == null )
            return null;
        return h.getValue();
    }
}
