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

package org.apache.jena.sparql.exec;

import static org.apache.jena.sparql.exec.QueryExecBuilderRegistry.getClassLabel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of {@link UpdateExecBuilderFactory} instances.
 * Allows for plugging into the {@link UpdateExecBuilder} creation process
 * based on dataset and context.
 *
 * @see UpdateExecBuilderFactory
 * @since 6.1.0
 */
public class UpdateExecBuilderRegistry {
    private static final Logger logger = LoggerFactory.getLogger(UpdateExecBuilderRegistry.class);

    List<UpdateExecBuilderFactory> factories = Collections.synchronizedList(new ArrayList<>());

    // Singleton
    private static UpdateExecBuilderRegistry registry;
    static { init(); }

    static public UpdateExecBuilderRegistry get() {
        return registry;
    }

    /** If there is a UpdateExecBuilderRegistry in the context then return it otherwise yield the global instance */
    static public UpdateExecBuilderRegistry chooseRegistry(Context context) {
        UpdateExecBuilderRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the UpdateExecBuilderRegistry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public UpdateExecBuilderRegistry get(Context context) {
        UpdateExecBuilderRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryUpdateExecBuilders);
        return result;
    }

    static public void set(Context context, UpdateExecBuilderRegistry registry) {
        context.set(ARQConstants.registryUpdateExecBuilders, registry);
    }

    public UpdateExecBuilderRegistry copy() {
        UpdateExecBuilderRegistry result = new UpdateExecBuilderRegistry();
        result.factories.addAll(factories);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static UpdateExecBuilderRegistry copyFrom(Context context) {
        UpdateExecBuilderRegistry tmp = get(context);
        UpdateExecBuilderRegistry result = tmp != null
                ? tmp.copy()
                : new UpdateExecBuilderRegistry();
        return result;
    }

    public UpdateExecBuilderRegistry() { }

    private static void init() {
        registry = new UpdateExecBuilderRegistry();
        registry.add(UpdateExecBuilderFactoryMain.get());
    }

    /** Add an UpdateExecBuilderFactory to the default registry. */
    public static void addFactory(UpdateExecBuilderFactory f) { get().add(f); }

    /** Add an UpdateExecBuilderFactory. */
    public void add(UpdateExecBuilderFactory f) {
        // Add to low end so that newer factories are tried first
        factories.add(0, f);
    }

    /** Remove an UpdateExecBuilderFactory from the default registry. */
    public static void removeFactory(UpdateExecBuilderFactory f)  { get().remove(f); }

    /** Remove an UpdateExecBuilderFactory. */
    public void remove(UpdateExecBuilderFactory f)  { factories.remove(f); }

    /** Allow <b>careful</b> manipulation of the factories list */
    public List<UpdateExecBuilderFactory> factories() { return factories; }

    /** Check whether an UpdateExecBuilderFactory is already registered in the default registry */
    public static boolean containsFactory(UpdateExecBuilderFactory f) { return get().contains(f); }

    /** Check whether an UpdateExecBuilderFactory is already registered. */
    public boolean contains(UpdateExecBuilderFactory f) { return factories.contains(f); }

    /**
     * Locate a suitable factory from the default registry.
     *
     * Registry lookup will use the provided context if non−null.
     * Otherwise, the dataset's context is used instead.
     *
     * @return A UpdateExecBuilderFactory or null if none accept the request
     */
    public static UpdateExecBuilderFactory findFactory(DatasetGraph dataset, Context context) {
        Context cxt = (context != null) ? context : Context.fromDataset(dataset);
        UpdateExecBuilderRegistry registry = chooseRegistry(cxt);
        UpdateExecBuilderFactory factory = registry.find(dataset, context);
        return factory;
    }

    /**
     * Locate a suitable factory for this dataset and context.
     *
     * @return A UpdateExecBuilderFactory or null if none accept the request
     */
    public UpdateExecBuilderFactory find(DatasetGraph dsg, Context cxt) {
        UpdateExecBuilderFactory result = null;
        int i = 0;
        int n = factories.size();
        for (UpdateExecBuilderFactory factory : factories) {
            ++i;
            if (factory.accept(dsg, cxt)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{}/{}: {} accepted {}",
                        i, n, getClassLabel(factory.getClass()),
                        getClassLabel(dsg.getClass()) + "@" + ObjectUtils.identityHashCodeHex(dsg));
                }
                result = factory;
                break;
            }
        }
        return result;
    }

    public static UpdateExecBuilder newUpdateExecBuilder(DatasetGraph dsg) {
        return newUpdateExecBuilder(dsg, null);
    }

    public static UpdateExecBuilder newUpdateExecBuilder(DatasetGraph dsg, Context context) {
        UpdateExecBuilderFactory factory = findFactory(dsg, context);
        if (factory == null) {
            throw new NoSuchElementException("No UpdateExecBuilderFactory accepted dataset " + ObjectUtils.identityToString(dsg));
        }

        UpdateExecBuilder builder = factory.create(dsg, context);
        if (builder == null) {
            throw new ARQInternalErrorException("UpdateExecBuilderFactory returned null: " + ObjectUtils.identityToString(factory));
        }
        return builder;
    }
}
