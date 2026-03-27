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

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.ChainingUpdateDispatcherMain;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

/**
 * Registry of {@link ChainingUpdateDispatcher} instances.
 * Allows for plugging into the {@link UpdateExec} creation process
 * based on dataset and context.
 *
 * @see ChainingUpdateDispatcher
 * @since 6.1.0
 */
public class UpdateDispatcherRegistry {
    List<ChainingUpdateDispatcher> dispatchers = Collections.synchronizedList(new ArrayList<>());

    // Singleton
    private static UpdateDispatcherRegistry registry;
    static { init(); }

    static public UpdateDispatcherRegistry get() {
        return registry;
    }

    /** If there is a UpdateDispatcherRegistry in the context then return it otherwise yield the global instance */
    static public UpdateDispatcherRegistry chooseRegistry(Context context) {
        UpdateDispatcherRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the UpdateDispatcherRegistry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public UpdateDispatcherRegistry get(Context context) {
        UpdateDispatcherRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryUpdateDispatchers);
        return result;
    }

    static public void set(Context context, UpdateDispatcherRegistry registry) {
        context.set(ARQConstants.registryUpdateDispatchers, registry);
    }

    public UpdateDispatcherRegistry copy() {
        UpdateDispatcherRegistry result = new UpdateDispatcherRegistry();
        result.dispatchers.addAll(dispatchers);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static UpdateDispatcherRegistry copyFrom(Context context) {
        UpdateDispatcherRegistry tmp = get(context);
        UpdateDispatcherRegistry result = tmp != null
                ? tmp.copy()
                : new UpdateDispatcherRegistry();
        return result;
    }

    public UpdateDispatcherRegistry() { }

    private static void init() {
        registry = new UpdateDispatcherRegistry();
        registry.add(ChainingUpdateDispatcherMain.get());
    }

    /** Add an ChainingUpdateDispatcher to the default registry. */
    public static void addDispatcher(ChainingUpdateDispatcher f) { get().add(f); }

    /** Add an ChainingUpdateDispatcher. */
    public void add(ChainingUpdateDispatcher f) {
        // Add to low end so that newer factories are tried first
        dispatchers.add(0, f);
    }

    /** Remove an ChainingUpdateDispatcher from the default registry. */
    public static void removeDispatcher(ChainingUpdateDispatcher f)  { get().remove(f); }

    /** Remove an ChainingUpdateDispatcher. */
    public void remove(ChainingUpdateDispatcher f)  { dispatchers.remove(f); }

    /** Allow <b>careful</b> manipulation of the factories list */
    public List<ChainingUpdateDispatcher> dispatchers() { return dispatchers; }

    /** Check whether an ChainingUpdateDispatcher is already registered in the default registry */
    public static boolean containsDispatcher(ChainingUpdateDispatcher f) { return get().contains(f); }

    /** Check whether an ChainingUpdateDispatcher is already registered. */
    public boolean contains(ChainingUpdateDispatcher f) { return dispatchers.contains(f); }

    public static UpdateExec create(UpdateRequest updateRequest, DatasetGraph dsg, Context context) {
        UpdateDispatcher Dispatcher = new UpdateDispatcherOverRegistry(registry);
        UpdateExec ue = Dispatcher.create(updateRequest, dsg, context);
        return ue;
    }
}
