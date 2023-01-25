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

import static org.apache.jena.riot.web.HttpNames.*;

/** Interface for executing {@link HttpAction}. */
public interface ActionProcessor {
    // c.f HttpServlet.
    /**
     * Execute this request.
     *
     * @param action   HTTP Action
     */
    public default void process(HttpAction action) {
        switch ( action.getMethod() ) {
            case METHOD_GET:        execGet(action);      break;
            case METHOD_POST:       execPost(action);     break;
            case METHOD_PATCH:      execPatch(action);    break;
            case METHOD_PUT:        execPut(action);      break;
            case METHOD_DELETE:     execDelete(action);   break;
            case METHOD_HEAD:       execHead(action);     break;
            case METHOD_OPTIONS:    execOptions(action);  break;
            case METHOD_TRACE:      execTrace(action);    break;
            // Unknown.
            default:                execAny(action.getMethod(), action); break;
        }
    }

    // Override to support the operation.
    // A common override is "executeLifecycle(action);"
    public default void execHead(HttpAction action)     { execAny(METHOD_HEAD,    action); }
    public default void execGet(HttpAction action)      { execAny(METHOD_GET,     action); }
    public default void execPost(HttpAction action)     { execAny(METHOD_POST,    action); }
    public default void execPatch(HttpAction action)    { execAny(METHOD_PATCH,   action); }
    public default void execPut(HttpAction action)      { execAny(METHOD_PUT,     action); }
    public default void execDelete(HttpAction action)   { execAny(METHOD_DELETE,  action); }
    public default void execOptions(HttpAction action)  { execAny(METHOD_OPTIONS, action); }
    public default void execTrace(HttpAction action)    { execAny(METHOD_TRACE,   action); }

    // Override this for all HTTP verbs.
    public default void execAny(String methodName, HttpAction action) {
        ServletOps.errorMethodNotAllowed(methodName);
    }
}
