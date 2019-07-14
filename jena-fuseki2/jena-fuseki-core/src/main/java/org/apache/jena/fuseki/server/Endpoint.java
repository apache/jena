/**
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

/*
 * An {@code Endpoint} is an instance of an {@link Operation} within a {@link DataService} and has counters.
 * An {@code Endpoint} may have a name which is a path component.
 */
public class Endpoint implements Counters {

    /** The endpoint name used for a dataset-level endpoint. */  
    public static final String    DatasetEP = "";  
    
    private final Operation       operation;
    private       ActionProcessor processor = null;
    private final String          endpointName;
    private final AuthPolicy      authPolicy;
    private final Context         context;
    // Endpoint-level counters.
    private final CounterSet      counters = new CounterSet();

    public static EndpointBuilder create() { return EndpointBuilder.create(); }
    
    public Endpoint(Operation operation, String endpointName, AuthPolicy requestAuth) {
        this(operation, endpointName, requestAuth, null, new Context());
    }
    
    public Endpoint(Operation operation, String endpointName, AuthPolicy requestAuth, ActionProcessor processor, Context context) {
        this.operation = Objects.requireNonNull(operation, "operation");
        // Canonicalise to "" for dataset-level operations.
        this.endpointName = endpointName==null? DatasetEP : endpointName;
        this.authPolicy = requestAuth;
        this.context = context;
        this.processor = processor;
        
        // Standard counters - there may be others
        counters.add(CounterName.Requests);
        counters.add(CounterName.RequestsGood);
        counters.add(CounterName.RequestsBad);
        // Default. Better to explicitly set later.
        //processor = OperationRegistry.get().findHandler(operation);
    }

    @Override
    public CounterSet getCounters() {
        return counters;
    }

    public Operation getOperation() {
        return operation;
    }

    public ActionProcessor getProcessor() {
        return processor;
    }

    /** Directly replace the {@link ActionProcessor}.
     * This allows an endpoint to be created, and then latest have the ActionProcessor set,
     * such as applying a default (normal case) or a security version injected.
     */  
    public void setProcessor(ActionProcessor proc) {
        processor = proc;
    }
    
    public Context getContext() {
        return context;
    }
    
    public boolean isUnnamed() {
        return endpointName == null || endpointName.isEmpty();
    }

    public String getName() {
        return isUnnamed() ? DatasetEP : endpointName;
    }

    public AuthPolicy getAuthPolicy() {
        return authPolicy;
    }

    public long getRequests() {
        return counters.value(CounterName.Requests);
    }

    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood);
    }

    public long getRequestsBad() {
        return counters.value(CounterName.RequestsBad);
    }

    @Override
    public String toString() {
        return getName()+"["+operation+"]";
    }
}
