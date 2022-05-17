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

import java.util.Objects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.system.ActionCategory;
import org.slf4j.Logger;

/**
 * Adapter of ActionProcessor to a plain servlet.
 *
 * @see ServletProcessor
 */
public class ServletAction extends HttpServlet {

    private final Logger log;
    private final ActionProcessor actionProcessor;

    /**
     * Constructor for an external {@link ActionProcessor}.
     */
    public ServletAction(ActionProcessor actionProcessor, Logger log) {
        this.actionProcessor = Objects.requireNonNull(actionProcessor, "actionProcessor is null");;
        this.log = log;
    }

    /**
     * Direct all servlet calls to the {@code HttpAction} framework.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        HttpAction action = allocHttpActionServlet(request, response);
        ActionExecLib.execAction(action, actionProcessor);
    }

    private HttpAction allocHttpActionServlet(HttpServletRequest request, HttpServletResponse response) {
        return allocHttpAction(null, log, ActionCategory.ACTION, request, response);
    }
}
