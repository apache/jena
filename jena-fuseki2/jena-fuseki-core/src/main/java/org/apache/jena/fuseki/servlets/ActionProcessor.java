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
        switch (action.getMethod() ) {
            case METHOD_GET:        execGet(action);      break;
            case METHOD_POST:       execPost(action);     break;
            case METHOD_PATCH:      execPatch(action);    break;
            case METHOD_PUT:        execPut(action);      break;
            case METHOD_DELETE:     execDelete(action);   break;
            case METHOD_HEAD:       execHead(action);     break;
            case METHOD_OPTIONS:    execOptions(action);  break;
            case METHOD_TRACE:      execTrace(action);    break;
        }
    }

    public default void execHead(HttpAction action)     { ServletOps.errorMethodNotAllowed(METHOD_HEAD); }
    public default void execGet(HttpAction action)      { ServletOps.errorMethodNotAllowed(METHOD_GET); }
    public default void execPost(HttpAction action)     { ServletOps.errorMethodNotAllowed(METHOD_POST); }
    public default void execPatch(HttpAction action)    { ServletOps.errorMethodNotAllowed(METHOD_PATCH); }
    public default void execPut(HttpAction action)      { ServletOps.errorMethodNotAllowed(METHOD_PUT); }
    public default void execDelete(HttpAction action)   { ServletOps.errorMethodNotAllowed(METHOD_DELETE); }
    public default void execOptions(HttpAction action)  { ServletOps.errorMethodNotAllowed(METHOD_OPTIONS); }
    public default void execTrace(HttpAction action)    { ServletOps.errorMethodNotAllowed(METHOD_TRACE); }
}
