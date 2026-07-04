/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.servlets;

import org.apache.jena.http.HttpMethod;

/** Interface for executing {@link HttpAction}. */
public interface ActionProcessor {
    // c.f HttpServlet.
    /**
     * Execute this request.
     *
     * @param action   HTTP Action
     */
    public default void process(HttpAction action) {
        switch ( action.getRequestMethod() ) {
            case HttpMethod.METHOD_GET ->       execGet(action);
            case HttpMethod.METHOD_QUERY ->     execQuery(action);
            case HttpMethod.METHOD_POST ->      execPost(action);
            case HttpMethod.METHOD_PATCH ->     execPatch(action);
            case HttpMethod.METHOD_PUT ->       execPut(action);
            case HttpMethod.METHOD_DELETE ->    execDelete(action);
            case HttpMethod.METHOD_HEAD ->      execHead(action);
            case HttpMethod.METHOD_OPTIONS ->   execOptions(action);
            case HttpMethod.METHOD_TRACE ->     execTrace(action);
            default -> execUnknown(action.getRequestMethod(), action);
        }
    }

    // Override to support the operation.
    // A common override is "executeLifecycle(action);"
    public default void execHead(HttpAction action)     { execAny(HttpMethod.HEAD,    action); }
    public default void execGet(HttpAction action)      { execAny(HttpMethod.GET,     action); }
    public default void execQuery(HttpAction action)    { execAny(HttpMethod.QUERY,   action); }
    public default void execPost(HttpAction action)     { execAny(HttpMethod.POST,    action); }
    public default void execPatch(HttpAction action)    { execAny(HttpMethod.PATCH,   action); }
    public default void execPut(HttpAction action)      { execAny(HttpMethod.PUT,     action); }
    public default void execDelete(HttpAction action)   { execAny(HttpMethod.DELETE,  action); }
    public default void execOptions(HttpAction action)  { execAny(HttpMethod.OPTIONS, action); }
    public default void execTrace(HttpAction action)    { execAny(HttpMethod.TRACE,   action); }

    // Override this for all HTTP verbs.
    public default void execAny(HttpMethod method, HttpAction action) {
        ServletOps.errorMethodNotAllowed(method.method());
    }

    public default void execUnknown(String unknownMethod, HttpAction action) {
        ServletOps.errorMethodNotAllowed(unknownMethod);
    }
}
