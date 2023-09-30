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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import jakarta.servlet.DispatcherType;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.FilterMapping;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.session.DefaultSessionIdManager;
import org.eclipse.jetty.session.SessionHandler;
import org.eclipse.jetty.session.SessionIdManager;

public class ExFusekiMain_2_AddShiro {
    // From Barry Nouwt : https://lists.apache.org/thread.html/r1e3fa952ff9f4a9108e16f07f1edf78c67e08c9b081497c627e3b833%40%3Cusers.jena.apache.org%3E
    // https://lists.apache.org/thread/q37s6kb3vy0ff6qbbrqy44qvbx8lojkq
    public static void addShiroFilter(FusekiServer fusekiServer) {
        Server jettyServer = fusekiServer.getJettyServer();

        ServletContextHandler servletContextHandler = (ServletContextHandler) jettyServer.getHandler();
        ServletHandler servletHandler = servletContextHandler.getServletHandler();

        // for shiro
        EnvironmentLoaderListener ell = new EnvironmentLoaderListener();
        servletContextHandler.addEventListener(ell);

        // Copies
        List<FilterMapping> mappings = new ArrayList<>(Arrays.asList(servletHandler.getFilterMappings()));
        List<FilterHolder> holders = new ArrayList<>(Arrays.asList(servletHandler.getFilters()));

        {
            //add Shiro Filter and mapping
            FilterHolder holder1 = new FilterHolder();
            holder1.setFilter(new ShiroFilter());
            FilterMapping mapping1 = new FilterMapping();
            mapping1.setFilterName(holder1.getName());
            mapping1.setPathSpec("/*");
            mapping1.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
            mappings.add(0, mapping1);
            holders.add(0, holder1);
        }

        FilterMapping[] mappings3 = new FilterMapping[mappings.size()];
        mappings3 = mappings.toArray(mappings3);
        FilterHolder[] holders3 = new FilterHolder[holders.size()];
        holders3 = holders.toArray(holders3);
        servletHandler.setFilters(holders3);
        servletHandler.setFilterMappings(mappings3);

        // Specify the Session ID Manager
        SessionIdManager idManager = new DefaultSessionIdManager(jettyServer);
        jettyServer.addBean(idManager, true);

        // Specify the session handler
        SessionHandler sessionsHandler = new SessionHandler();
        sessionsHandler.setUsingCookies(false);
        servletHandler.setHandler(sessionsHandler);
    }
}

