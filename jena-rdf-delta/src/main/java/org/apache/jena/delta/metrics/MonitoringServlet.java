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

package org.apache.jena.delta.metrics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that serves the monitoring dashboard web UI.
 */
public class MonitoringServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringServlet.class);
    
    private static final String DASHBOARD_PATH = "/webapp/monitoring/index.html";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Get the dashboard HTML from the classpath
        try (InputStream in = getClass().getResourceAsStream(DASHBOARD_PATH)) {
            if (in == null) {
                LOG.warn("Dashboard HTML not found at {}", DASHBOARD_PATH);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Dashboard HTML not found");
                return;
            }
            
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            
            try (OutputStream out = resp.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}