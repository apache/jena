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

package org.apache.jena.sparql.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.engine.http.Service;
import org.apache.jena.sparql.util.Context;

public class ServiceExecutorRegistry
{
    // A list of custom service executors which are tried in the given order
    List<ServiceExecutorFactory> registry = new ArrayList<>();

    public static ServiceExecutorRegistry standardRegistry()
    {
        ServiceExecutorRegistry reg = get(ARQ.getContext()) ;
        return reg ;
    }

    /** A "call with SPARQL query" service execution factory. */
    public static ServiceExecutorFactory httpService = (op, opx, binding, execCxt) -> ()->Service.exec(op, execCxt.getContext());

    public static void init() {
        // Initialize if there is no registry already set
        ServiceExecutorRegistry reg = new ServiceExecutorRegistry() ;
        reg.add(httpService);
        set(ARQ.getContext(), reg) ;
    }

    public static ServiceExecutorRegistry get()
    {
        // Initialize if there is no registry already set
        ServiceExecutorRegistry reg = get(ARQ.getContext()) ;
        if ( reg == null )
        {
            init() ;
            reg = get(ARQ.getContext()) ;
        }

        return reg ;
    }

    public static ServiceExecutorRegistry get(Context context)
    {
        if ( context == null )
            return null ;
        return (ServiceExecutorRegistry)context.get(ARQConstants.registryServiceExecutors) ;
    }

    public static void set(Context context, ServiceExecutorRegistry reg)
    {
        context.set(ARQConstants.registryServiceExecutors, reg) ;
    }

    public ServiceExecutorRegistry()
    {}


    /** Insert a service executor factory. Must not be null. */
    public ServiceExecutorRegistry add(ServiceExecutorFactory f) {
        Objects.requireNonNull(f) ;
        registry.add(0, f) ;
        return this;
    }

    /** Remove the given service executor factory. */
    public ServiceExecutorRegistry remove(ServiceExecutorFactory f) {
        registry.remove(f) ;
        return this;
    }

    /** Retrieve the actual list of factories; allows for re-ordering */
    public List<ServiceExecutorFactory> getFactories() {
        return registry;
    }

}