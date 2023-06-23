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

import jakarta.servlet.http.HttpServletRequest;

import org.apache.jena.riot.web.HttpNames;

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
        return action.getRequestQueryString() == null;
    }

    private static void validateQuads(HttpAction action) { }

    /**
     * Test a SPARQL GSP request; if there are any protocol errors, respond
     * by throwing {@link ActionErrorException} with a specific error messages to the problem
     * identified.
     */
    private static void validateGSP(HttpAction action) {    
        HttpServletRequest request = action.getRequest();
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
}
