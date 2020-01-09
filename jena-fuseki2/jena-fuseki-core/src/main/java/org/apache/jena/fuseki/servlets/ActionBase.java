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

import org.apache.jena.fuseki.server.Dispatcher;

/**
 * Base of all implementations of service {@link HttpAction}. This class provides the
 * two steps execution of "validate" and "perform". 
 * 
 * Subclasses choose which HTTP
 * methods they handle by implementing "execGet(HttpAction)" etc. These often call
 * {@link #executeLifecycle} for their normal {@code HttpAction} lifecycle, for example,
 * when GET and POST do the same steps so common "validate" and "execute".
 * 
 * See {@link ActionExecLib#execAction} for the common ActionProcessor execution with logging and error handling.
 * This is used by {@link Dispatcher#dispatchAction(HttpAction)}.
 * 
 * See {@link ActionService} which overrides {@link #executeLifecycle} to add statistics counters.
 * 
 * Some operations like OPTIONS will implement differently.    
 * 
 * <pre>
 * public void execGet(HttpAction action) { super.executeLifecycle(action); }
 * </pre>  
 * 
 */
public abstract class ActionBase implements ActionProcessor, ActionLifecycle {
    protected ActionBase() { }

    /**
     * Subclasses must override {@code execGet}, {@code execPost} etc to say which
     * methods they support.
     * Typically, the implementation is a call to {@code executeLifecycle(action)}.
     */
    @Override
    public void process(HttpAction action) {
        // Enforce splitting to be "exec" for each HTTP method.
        ActionProcessor.super.process(action);
    }

    /**
     * Simple execution lifecycle for a SPARQL Request.
     * No statistics.
     *
     * @param action
     */
    protected void executeLifecycle(HttpAction action) {
        validate(action);
        execute(action);
    }
}
