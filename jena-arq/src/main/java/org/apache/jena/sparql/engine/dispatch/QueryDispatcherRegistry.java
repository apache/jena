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

package org.apache.jena.sparql.engine.dispatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.ChainingQueryDispatcherMain;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.util.Context;

/**
 * Registry of {@link ChainingQueryDispatcher} instances.
 * Allows for plugging into the {@link QueryExecBuilder} creation process
 * based on dataset and context.
 *
 * @see ChainingQueryDispatcher
 * @since 6.1.0
 */
public class QueryDispatcherRegistry {
    List<ChainingQueryDispatcher> dispatchers = Collections.synchronizedList(new ArrayList<>());

    // Singleton
    private static QueryDispatcherRegistry registry;
    static { init(); }

    static public QueryDispatcherRegistry get() {
        return registry;
    }

    /** If there is a QueryDispatcherRegistry in the context then return it otherwise yield the global instance */
    static public QueryDispatcherRegistry chooseRegistry(Context context) {
        QueryDispatcherRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the QueryDispatcherRegistry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public QueryDispatcherRegistry get(Context context) {
        QueryDispatcherRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryQueryDispachers);
        return result;
    }

    static public void set(Context context, QueryDispatcherRegistry registry) {
        context.set(ARQConstants.registryQueryDispachers, registry);
    }

    public QueryDispatcherRegistry copy() {
        QueryDispatcherRegistry result = new QueryDispatcherRegistry();
        result.dispatchers.addAll(dispatchers);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static QueryDispatcherRegistry copyFrom(Context context) {
        QueryDispatcherRegistry tmp = get(context);
        QueryDispatcherRegistry result = tmp != null
                ? tmp.copy()
                : new QueryDispatcherRegistry();
        return result;
    }

    public QueryDispatcherRegistry() { }

    private static void init() {
        registry = new QueryDispatcherRegistry();

        registry.add(ChainingQueryDispatcherMain.get());
    }

    // ----- Query -----

    /** Add a ChainingQueryDispatcher to the default registry. */
    public static void addDispatcher(ChainingQueryDispatcher f) { get().add(f); }

    /** Add a ChainingQueryDispatcher. */
    public void add(ChainingQueryDispatcher f) {
        // Add to low end so that newer factories are tried first
        dispatchers.add(0, f);
    }

    /** Remove a ChainingQueryDispatcher from the default registry. */
    public static void removeDispatcher(ChainingQueryDispatcher f)  { get().remove(f); }

    /** Remove a ChainingQueryDispatcher. */
    public void remove(ChainingQueryDispatcher f)  { dispatchers.remove(f); }

    /** Allow <b>careful</b> manipulation of the dispatchers list */
    public List<ChainingQueryDispatcher> dispatchers() { return dispatchers; }

    /** Check whether a ChainingQueryDispatcher is registered in the default registry. */
    public static boolean containsDispatcher(ChainingQueryDispatcher f) { return get().contains(f); }

    /** Check whether a ChainingQueryDispatcher is already registered. */
    public boolean contains(ChainingQueryDispatcher f) { return dispatchers.contains(f); }

    public static QueryExec create(Query query, DatasetGraph dsg, Context context) {
        QueryDispatcher dispatcher = new QueryDispatcherOverRegistry(registry);
        QueryExec qe = dispatcher.create(query, dsg, context);
        return qe;
    }
}
