/**
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

import java.io.IOException ;

import javax.servlet.* ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.slf4j.Logger ;

/** Look at all requests and see if they match a registered dataset name; 
 * if they do, pass down to the uber servlet, which can dispatch any request
 * for any service. 
 *  
 */
public class SPARQL_UberFilter implements Filter {
    private static Logger log = Fuseki.requestLog ; //LoggerFactory.getLogger(SomeFilter.class) ;
    private static SPARQL_UberServlet überServlet = new SPARQL_UberServlet.AccessByConfig() ;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            // See SPARQL_Servlet.execCommonWorker
            HttpServletRequest req = (HttpServletRequest)request ;
            HttpServletResponse resp = (HttpServletResponse)response ;

            String uri = req.getRequestURI() ;
            String datasetUri = ActionLib.mapRequestToDataset(uri) ;

            if ( datasetUri != null ) {        
                if ( DatasetRegistry.get().isRegistered(datasetUri) ) {
                    // Intercept and redirect
                    log.info("Redirect: "+uri);
                    überServlet.doCommon(req, resp) ;
                    return ;
                }
            }
        } catch (Exception ex) {}
        
        // Not found - continue. 
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

}

