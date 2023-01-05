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

package org.apache.jena.fuseki.main.examples;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.JettySecurityLib;
import org.apache.jena.fuseki.servlets.FusekiFilter;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Example of intercepting Fuseki dispatch.
 */
public class ExFusekiMain_1_Servlet_AddFilter
{
    public static void main(String ... a) {
        try {
            FusekiLogging.setLogging();

            UserStore userStore = JettySecurityLib.makeUserStore("u", "p");
            SecurityHandler sh = JettySecurityLib.makeSecurityHandler("TripleStore",  userStore, AuthScheme.BASIC);

            FusekiServer server = FusekiServer.create()
                .add("/ds", DatasetGraphFactory.createTxnMem())
                //.verbose(true)
                .serverAuthPolicy(Auth.ANY_USER)
                .securityHandler(sh)
                .build();
            addExtraFilter(server);
            server.start();

            // And use it.
            String URL = "http://localhost:3330/ds";
            try ( RDFConnection conn = RDFConnection.connectPW(URL, "u", "p") ) {
                boolean b = conn.queryAsk("ASK{}");
                System.out.println("ASK="+b);
            }
            //server.join();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally { System.exit(0); }
    }

    // Find the FusekiFilter and replace it with an indirection filter.
    private static void addExtraFilter(FusekiServer server) {
        Handler handler = server.getJettyServer().getHandler();
        ServletContextHandler sch =  (ServletContextHandler)handler;
        ServletHandler servletHander = sch.getServletHandler() ;

        FilterHolder[] fHolders = servletHander.getFilters();
        for ( int i = 0 ; i <fHolders.length ; i++ ) {
            FilterHolder fh = fHolders [i];
            if ( fh.getClassName().equals(FusekiFilter.class.getName()) ) {
                FilterHolder fh2 = replacement(fh);
                // ** Replacement.
                fHolders [i] = fh2;
            }
        }
    }

    /** Build a replacement Filter */
    private static FilterHolder replacement(FilterHolder fh) {
        final Filter fx = (fh.getFilter()==null) ? new FusekiFilter() : fh.getFilter();
        Filter filter2 =  new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                // Example: print the Principal
                Principal user = ((HttpServletRequest)request).getUserPrincipal();
                System.out.println("Request User: "+user);
                fx.doFilter(request, response, chain);
            }
            @Override public void init(FilterConfig filterConfig) throws ServletException { fx.init(filterConfig); }
            @Override public void destroy() { fx.destroy(); }
        } ;
        FilterHolder fh2 = new FilterHolder(filter2);
        fh2.setClassName(filter2.getClass().getName());
        fh2.setName(fh.getName());
        // Not needed if before server.start();
        //fh2.start();
        //fh2.initialize();
        return fh2;
    }


}
