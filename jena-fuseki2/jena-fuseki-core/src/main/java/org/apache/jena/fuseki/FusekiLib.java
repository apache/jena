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
import org.apache.jena.fuseki.server.SystemState ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Convert ;
import org.apache.jena.vocabulary.RDFS ;

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
    
    /** Get the incoming Lang based on Content-Type of an action.
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

    // ---- Helper code
    public static ResultSet query(String string, Model m) {
        return query(string, m, null, null) ;
    }

    public static ResultSet query(String string, Dataset ds) {
        return query(string, ds, null, null) ;
    }

    public static ResultSet query(String string, Model m, String varName, RDFNode value) {
        Query query = QueryFactory.create(SystemState.PREFIXES + string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null )
            initValues = querySolution(varName, value) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, m, initValues) ) {
            return ResultSetFactory.copyResults(qExec.execSelect()) ;
        }
    }

    public static ResultSet query(String string, Dataset ds, String varName, RDFNode value) {
        Query query = QueryFactory.create(SystemState.PREFIXES + string) ;
        QuerySolutionMap initValues = null ;
        if ( varName != null )
            initValues = querySolution(varName, value) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, ds, initValues) ) {
            return ResultSetFactory.copyResults(qExec.execSelect()) ;
        }
    }

    private static QuerySolutionMap querySolution(String varName, RDFNode value) {
        QuerySolutionMap qsm = new QuerySolutionMap() ;
        querySolution(qsm, varName, value) ;
        return qsm ;
    }

    public static QuerySolutionMap querySolution(QuerySolutionMap qsm, String varName, RDFNode value) {
        qsm.add(varName, value) ;
        return qsm ;
    }
    
    public static RDFNode getOne(Resource svc, String property) {
        ResultSet rs = FusekiLib.query("SELECT * { ?svc " + property + " ?x}", svc.getModel(), "svc", svc) ;
        if ( !rs.hasNext() )
            throw new FusekiConfigException("No property '" + property + "' for service " + FusekiLib.nodeLabel(svc)) ;
        RDFNode x = rs.next().get("x") ;
        if ( rs.hasNext() )
            throw new FusekiConfigException("Multiple properties '" + property + "' for service " + FusekiLib.nodeLabel(svc)) ;
        return x ;
    }
    
    // Node presentation
    public static String nodeLabel(RDFNode n) {
        if ( n == null )
            return "<null>" ;
        if ( n instanceof Resource )
            return strForResource((Resource)n) ;
    
        Literal lit = (Literal)n ;
        return lit.getLexicalForm() ;
    }

    public static String strForResource(Resource r) {
        return strForResource(r, r.getModel()) ;
    }

    public static String strForResource(Resource r, PrefixMapping pm) {
        if ( r == null )
            return "NULL " ;
        if ( r.hasProperty(RDFS.label) ) {
            RDFNode n = r.getProperty(RDFS.label).getObject() ;
            if ( n instanceof Literal )
                return ((Literal)n).getString() ;
        }
    
        if ( r.isAnon() )
            return "<<blank node>>" ;
    
        if ( pm == null )
            pm = r.getModel() ;
    
        return strForURI(r.getURI(), pm) ;
    }

    public static String strForURI(String uri, PrefixMapping pm) {
        if ( pm != null ) {
            String x = pm.shortForm(uri) ;
    
            if ( !x.equals(uri) )
                return x ;
        }
        return "<" + uri + ">" ;
    }

    /** Choose an unused port for a server to listen on */
    public static int choosePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException ex) {
            throw new FusekiException("Failed to find a port");
        }
    }

    /** Test whether a URL identifies a Fuseki server */  
    public static boolean isFuseki(String datasetURL) {
		System.err.println("isFuseki: "+datasetURL);
        HttpOptions request = new HttpOptions(datasetURL);
        HttpClient httpClient = HttpOp.getDefaultHttpClient();
        if ( httpClient == null ) 
            httpClient = HttpClients.createSystem();
        return isFuseki(request, httpClient, null);
    }

    /** Test whether a {@link RDFConnectionRemote} connects to a Fuseki server */  
    public static boolean isFuseki(RDFConnectionRemote connection) {
        HttpOptions request = new HttpOptions(connection.getDestination());
        HttpClient httpClient = connection.getHttpClient();
        if ( httpClient == null ) 
            httpClient = HttpClients.createSystem();
        HttpContext httpContext = connection.getHttpContext();
        return isFuseki(request, httpClient, httpContext);
    }

    private static boolean isFuseki(HttpOptions request, HttpClient httpClient, HttpContext httpContext) {
		System.err.println("isFuseki(worker)");
		if ( httpClient == null ) {
			System.err.println("httpClient is null");
		}
        try {
            HttpResponse response = httpClient.execute(request);
            // Fuseki-Request-ID:
            //String reqId = response.getFirstHeader("Fuseki-Request-ID").getValue();
            // Server:
				System.err.println("isFuseki(worker)-1");
            String serverIdent = response.getFirstHeader("Server").getValue();
				System.err.println("isFuseki(worker)-2");
            Log.debug(ARQ.getHttpRequestLogger(), "Server: "+serverIdent);
				System.err.println("isFuseki(worker)-3");
            boolean isFuseki = serverIdent.startsWith("Apache Jena Fuseki");
				System.err.println("isFuseki(worker)-4");
            if ( !isFuseki )
                isFuseki = serverIdent.toLowerCase().contains("fuseki");
			System.err.println("isFuseki(worker)-return");
            return isFuseki; // Maybe
        } catch (IOException ex) {
            throw new HttpException("Failed to check for a Fuseki server", ex);
        }
    }
}
