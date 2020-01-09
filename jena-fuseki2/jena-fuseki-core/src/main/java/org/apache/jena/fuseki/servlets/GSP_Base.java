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

package org.apache.jena.fuseki.servlets;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;

public abstract class GSP_Base extends ActionREST {

    protected GSP_Base() {}
    
    @Override
    public void validate(HttpAction action) {
        if ( isQuads(action) )
            validateQuads(action);
        else
            validateGSP(action);
    }
    
    protected static boolean isQuads(HttpAction action) {
        return action.request.getQueryString() == null;
    }

    private static void validateQuads(HttpAction action) { }

    /**
     * Test a SPARQL GSP request; if there are any protocol errors, respond
     * by throwing {@link ActionErrorException} with a specific error messages to the problem
     * identified.
     */
    private static void validateGSP(HttpAction action) {    
        HttpServletRequest request = action.request;
        if ( request.getQueryString() == null )
            ServletOps.errorBadRequest("No query string. ?default or ?graph required.");

        String g = request.getParameter(HttpNames.paramGraph);
        String d = request.getParameter(HttpNames.paramGraphDefault);

        if ( g != null && d != null )
            ServletOps.errorBadRequest("Both ?default and ?graph in the query string of the request");

        if ( g == null && d == null )
            ServletOps.errorBadRequest("Neither ?default nor ?graph in the query string of the request");

        int x1 = SPARQLProtocol.countParamOccurences(request, HttpNames.paramGraph);
        int x2 = SPARQLProtocol.countParamOccurences(request, HttpNames.paramGraphDefault);

        if ( x1 > 1 )
            ServletOps.errorBadRequest("Multiple ?default in the query string of the request");
        if ( x2 > 1 )
            ServletOps.errorBadRequest("Multiple ?graph in the query string of the request");

        Enumeration<String> en = request.getParameterNames();
        for (; en.hasMoreElements(); ) {
            String h = en.nextElement();
            if ( !HttpNames.paramGraph.equals(h) && !HttpNames.paramGraphDefault.equals(h) )
                ServletOps.errorBadRequest("Unknown parameter '" + h + "'");
            // one of ?default and &graph
            if ( request.getParameterValues(h).length != 1 )
                ServletOps.errorBadRequest("Multiple parameters '" + h + "'");
        }
    }
    
    protected final static GSPTarget determineTarget(DatasetGraph dsg, HttpAction action) {
        // Inside a transaction.
        if ( dsg == null )
            ServletOps.errorOccurred("Internal error : No action graph (not in a transaction?)");
//        if ( ! dsg.isInTransaction() )
//            ServletOps.errorOccurred("Internal error : No transaction");

        boolean dftGraph = GSPLib.getOneOnly(action.request, HttpNames.paramGraphDefault) != null;
        String uri = GSPLib.getOneOnly(action.request, HttpNames.paramGraph);

        if ( !dftGraph && uri == null ) {
            // No params - direct naming.
            if ( !Fuseki.GSP_DIRECT_NAMING )
                ServletOps.errorBadRequest("Neither default graph nor named graph specified");

            // Direct naming.
            String directName = action.request.getRequestURL().toString();
            if ( action.request.getRequestURI().equals(action.getDatasetName()) )
                // No name (should have been a quads operations).
                ServletOps.errorBadRequest("Neither default graph nor named graph specified and no direct name");
            Node gn = NodeFactory.createURI(directName);
            return namedTarget(dsg, directName);
        }

        if ( dftGraph )
            return GSPTarget.createDefault(dsg);

        // Named graph
        if ( uri.equals(HttpNames.valueDefault) )
            // But "named" default
            return GSPTarget.createDefault(dsg);

        // Strictly, a bit naughty on the URI resolution. But more sensible.
        // Base is dataset.

        String base = action.request.getRequestURL().toString(); // wholeRequestURL(request);
        // Make sure it ends in "/", ie. dataset as container.
        if ( action.request.getQueryString() != null && !base.endsWith("/") )
            base = base + "/";
        String absUri = null;
        try {
            absUri = IRIResolver.resolveString(uri, base);
        } catch (RiotException ex) {
            // Bad IRI
            ServletOps.errorBadRequest("Bad IRI: " + ex.getMessage());
        }
        return namedTarget(dsg, absUri);
    }

    private static GSPTarget namedTarget(DatasetGraph dsg, String graphName) {
        Node gn = NodeFactory.createURI(graphName);
        return GSPTarget.createNamed(dsg, graphName, gn);
    }
}
