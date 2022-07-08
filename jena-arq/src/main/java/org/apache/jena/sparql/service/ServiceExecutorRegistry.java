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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulk;
import org.apache.jena.sparql.service.bulk.ServiceExecutorBulkOverRegistry;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sparql.service.single.ChainingServiceExecutorWrapper;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.apache.jena.sparql.service.single.ServiceExecutorHttp;
import org.apache.jena.sparql.util.Context;

/**
 * Registry for service executors that can be extended with custom ones.
 * Bulk and single (=non-bulk) executors are maintained in two separate lists.
 *
 * Default execution will always start with the bulk list first.
 * Once that list is exhausted by means of all bulk executors having delegated the request,
 * then the non-bulk ones will be considered.
 * There is no need to explicitly register a bulk-to-non-bulk bridge.
 */
public class ServiceExecutorRegistry
{
    // A list of bulk service executors which are tried in the given order
    List<ChainingServiceExecutorBulk> bulkChain = new ArrayList<>();

    // A list of single (non-bulk) service executors which are tried in the given order
    // This list is only considered after after the bulk registry
    List<ChainingServiceExecutor> singleChain = new ArrayList<>();

    public static ServiceExecutorRegistry standardRegistry()
    {
        ServiceExecutorRegistry reg = get(ARQ.getContext()) ;
        return reg ;
    }

    /** A "call with SPARQL query" service executor. */
    public static ServiceExecutor httpService = new ServiceExecutorHttp();

    /** Blindly adds the default executor(s); concretely adds the http executor */
    public static void initWithDefaults(ServiceExecutorRegistry registry) {
        registry.add(httpService);
    }

    public static void init() {
        // Initialize if there is no registry already set
        ServiceExecutorRegistry reg = new ServiceExecutorRegistry();
        initWithDefaults(reg);
        set(ARQ.getContext(), reg) ;
    }

    /**
     * Return the global instance from the ARQ context; create that instance if needed.
     * Never returns null.
     */
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

    /** Return the registry from the given context if present; otherwise return the global one */
    public static ServiceExecutorRegistry chooseRegistry(Context context) {
        ServiceExecutorRegistry result = ServiceExecutorRegistry.get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Return the registry from the given context only; null if there is none */
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

    /**
     * Copies the origin registry into a new one, or makes a fresh instance if the specified registry is {@code null}.
     * @param from {@link ServiceExecutorRegistry} or {@code null}
     * @return {@link ServiceExecutorRegistry} a new instance
     */
    public static ServiceExecutorRegistry createFrom(ServiceExecutorRegistry from) {
        ServiceExecutorRegistry res = new ServiceExecutorRegistry();
        if (from != null) {
            res.bulkChain.addAll(from.bulkChain);
            res.singleChain.addAll(from.singleChain);
        }
        return res;
    }

    public ServiceExecutorRegistry()
    {}

    /*
     * Non-bulk API
     */

    /** Prepend the given service executor as a link to the per-binding chain */
    public ServiceExecutorRegistry addSingleLink(ChainingServiceExecutor f) {
        Objects.requireNonNull(f) ;
        singleChain.add(0, f) ;
        return this;
    }

    /** Remove the given service executor from the per-binding chain */
    public ServiceExecutorRegistry removeSingleLink(ChainingServiceExecutor f) {
        singleChain.remove(f) ;
        return this;
    }

    /** Wraps the given service executor as a chaining one and prepends it
     *  to the non-bulk chain via {@link #addSingleLink(ChainingServiceExecutor)} */
    public ServiceExecutorRegistry add(ServiceExecutor f) {
        Objects.requireNonNull(f) ;
        return addSingleLink(new ChainingServiceExecutorWrapper(f));
    }

    /** Remove a given service executor - internally attempts to unwrap every chaining service executor */
    public ServiceExecutorRegistry remove(ServiceExecutor f) {
        Iterator<ChainingServiceExecutor> it = singleChain.iterator();
        while (it.hasNext()) {
            ChainingServiceExecutor cse = it.next();
            if (cse instanceof ChainingServiceExecutorWrapper) {
                ChainingServiceExecutorWrapper wrapper = (ChainingServiceExecutorWrapper)cse;
                ServiceExecutor delegate = wrapper.getDelegate();
                if (Objects.equals(delegate, f)) {
                    it.remove();
                }
            }
        }
        return this;
    }

    /** Retrieve the actual list of per-binding executors; allows for re-ordering */
    public List<ChainingServiceExecutor> getSingleChain() {
        return singleChain;
    }

    /*
     * Bulk API
     */

    /** Add a chaining bulk executor as a link to the executor chain */
    public ServiceExecutorRegistry addBulkLink(ChainingServiceExecutorBulk f) {
        Objects.requireNonNull(f) ;
        bulkChain.add(0, f) ;
        return this;
    }

    /** Remove the given service executor */
    public ServiceExecutorRegistry removeBulkLink(ChainingServiceExecutorBulk f) {
        bulkChain.remove(f) ;
        return this;
    }

    /** Retrieve the actual list of bulk executors; allows for re-ordering */
    public List<ChainingServiceExecutorBulk> getBulkChain() {
        return bulkChain;
    }

    /*
     * Utility
     */

    /** Create an independent copy of the registry */
    public ServiceExecutorRegistry copy() {
        ServiceExecutorRegistry result = new ServiceExecutorRegistry();
        result.getSingleChain().addAll(getSingleChain());
        result.getBulkChain().addAll(getBulkChain());
        return result;
    }

    /** Return a copy of the registry in the context (if present) or a fresh instance */
    public ServiceExecutorRegistry copyFrom(Context cxt) {
        ServiceExecutorRegistry tmp = ServiceExecutorRegistry.get(cxt);
        ServiceExecutorRegistry result = tmp == null ? new ServiceExecutorRegistry() : tmp.copy();
        return result;
    }

    /*
     * Execution
     */

    /** Execute an OpService w.r.t. the execCxt's service executor registry */
    public static QueryIterator exec(QueryIterator input, OpService opService, ExecutionContext execCxt) {
        Context cxt = execCxt.getContext();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.chooseRegistry(cxt);
        ServiceExecutorBulk serviceExecutor = new ServiceExecutorBulkOverRegistry(registry);
        QueryIterator qIter = serviceExecutor.createExecution(opService, input, execCxt);
        return qIter;
    }
}
