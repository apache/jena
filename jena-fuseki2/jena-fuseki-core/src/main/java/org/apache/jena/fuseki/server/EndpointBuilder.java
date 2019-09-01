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

package org.apache.jena.fuseki.server;

import java.util.Objects;

import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.servlets.ActionProcessor;
import org.apache.jena.sparql.util.Context;

/** {@link Endpoint} builder */
public class EndpointBuilder {

    private Context         context      = null;
    private Operation       operation    = null;
    private String          endpointName = null;
    private AuthPolicy      authPolicy   = null;
    private ActionProcessor processor    = null;

    public static EndpointBuilder create() { return new EndpointBuilder(); }

    private EndpointBuilder() { }

    public EndpointBuilder operation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public EndpointBuilder endpointName(String endpointName) {
        this.endpointName = endpointName;
        return this;
    }

    public EndpointBuilder context(Context context) {
        this.context = context;
        return this;
    }

    public EndpointBuilder authPolicy(AuthPolicy authPolicy) {
        this.authPolicy = authPolicy;
        return this;
    }

    public EndpointBuilder processor(ActionProcessor processor) {
        this.processor = processor;
        return this;
    }

    public Context context() { return context; }

    public Operation operation() { return operation; }

    public String endpointName() { return endpointName; }

    public AuthPolicy authPolicy() { return authPolicy; }

    public ActionProcessor processor() { return processor; }

    public Endpoint build() {
        Objects.requireNonNull(operation, "Operation for Endpoint");
        return new Endpoint(operation, endpointName, authPolicy, processor, context);
    }
}
