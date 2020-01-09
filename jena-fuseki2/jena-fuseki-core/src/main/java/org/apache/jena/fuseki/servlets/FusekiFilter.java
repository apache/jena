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

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.Dispatcher;
import org.slf4j.Logger;

/** Look at all requests and see if they match a registered dataset name;
 * if they do, pass down to the uber servlet, which can dispatch any request
 * for any service.
 */
public class FusekiFilter implements Filter {
    private static Logger log = Fuseki.serverLog;

    @Override
    public void init(FilterConfig filterConfig) {
//        log.info("Filter: ["+Utils.className(this)+"] ServletContextName = "+filterConfig.getServletContext().getServletContextName());
//        log.info("Filter: ["+Utils.className(this)+"] ContextPath        = "+filterConfig.getServletContext().getContextPath());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse)response;

            boolean handled = Dispatcher.dispatch(req, resp);
            if ( handled )
                return;
        } catch (Throwable ex) {
            log.info("Filter: unexpected exception: "+ex.getMessage(),ex);
        }

        // Not found - continue.
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

}

