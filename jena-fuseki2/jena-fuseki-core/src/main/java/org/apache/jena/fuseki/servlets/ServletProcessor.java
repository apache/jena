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

import static org.apache.jena.fuseki.servlets.ActionExecLib.allocHttpAction;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.system.ActionCategory;
import org.slf4j.Logger;

/**
 * Servlet with inheritance for {@link ActionProcessor}.
 * Execution of the servlet is handled by {@link ActionExecLib#execAction}
 * with Fuseki server logging and error handling.
 *
 * @see ServletAction
 */
public abstract class ServletProcessor extends HttpServlet implements ActionProcessor {
    private final Logger LOG;
    private final ActionCategory category;

    /**
     * Constructor for an external {@link ActionProcessor}.
     */
    protected ServletProcessor(Logger log, ActionCategory category) {
        this.LOG = log;
        this.category = category;
    }

    /**
     * Direct all servlet calls to the {@code HttpAction} framework.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        HttpAction action = allocHttpActionServlet(request, response);
        ActionExecLib.execAction(action, this);
    }

    private HttpAction allocHttpActionServlet(HttpServletRequest request, HttpServletResponse response) {
        HttpAction action = allocHttpAction(null, LOG, category, request, response);
        action.setEndpoint(null);
        return action;
    }
}
