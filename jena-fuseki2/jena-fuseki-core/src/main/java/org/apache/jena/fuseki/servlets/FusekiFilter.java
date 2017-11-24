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
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataAccessPointRegistry ;
import org.slf4j.Logger ;

/** Look at all requests and see if they match a registered dataset name; 
 * if they do, pass down to the uber servlet, which can dispatch any request
 * for any service. 
 */
public class FusekiFilter implements Filter {
    private static Logger log = Fuseki.serverLog ;
    private static ServiceRouter routerServlet = new ServiceRouter.AccessByConfig() ;
    
    @Override
    public void init(FilterConfig filterConfig) {
//        log.info("Filter: ["+Utils.className(this)+"] ServletContextName = "+filterConfig.getServletContext().getServletContextName()) ;
//        log.info("Filter: ["+Utils.className(this)+"] ContextPath        = "+filterConfig.getServletContext().getContextPath()) ;
    }

    private static final boolean LogFilter = false ;     // Development debugging (can be excessive!)
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest)request ;
            HttpServletResponse resp = (HttpServletResponse)response ;

            // Handle context path
            String uri = ActionLib.actionURI(req) ;
            String datasetUri = ActionLib.mapActionRequestToDataset(uri) ;
            DataAccessPointRegistry registry = DataAccessPointRegistry.get(request.getServletContext()) ;
            
            // is it a long running operation?
            // (this could be a separate filter)
            
            if ( LogFilter ) {
                log.info("Filter: Request URI = "+req.getRequestURI()) ;
                log.info("Filter: Action URI  = "+uri) ;
                log.info("Filter: Dataset URI = "+datasetUri) ;
            }
            
            if ( datasetUri != null ) {        
                if ( registry.isRegistered(datasetUri) ) {
                    if ( LogFilter )
                        log.info("Filter: dispatch") ;
                    routerServlet.doCommon(req, resp) ;
                    return ;
                }

                // Not found. Last possibility is a GSP direct name.
                // This is a registry scan so if not supported, we can skip the scan and
                // not rely on the überServlet. 
                if ( Fuseki.GSP_DIRECT_NAMING ) {
                    // Not a dataset name ; may be a direct GSP direct name that does not look like a service name.
                    for ( String dsKey : registry.keys() ) {
                        DataAccessPoint dap = registry.get(dsKey) ;
                        String dsName = dap.getName() ;
                        if ( datasetUri.startsWith(dsName) ) {
                            if ( LogFilter )
                                log.info("Filter: dispatch (GSP direct name)") ;
                            routerServlet.doCommon(req, resp) ;
                            return ;
                        }
                    }
                } 
            }
        } catch (Exception ex) {}
        
        if ( LogFilter )
            log.info("Filter: pass to chain") ;
        // Not found - continue. 
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

}

