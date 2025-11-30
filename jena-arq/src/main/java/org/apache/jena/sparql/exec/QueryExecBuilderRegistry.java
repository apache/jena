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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of {@link QueryExecBuilderFactory} instances.
 * Allows for plugging into the {@link QueryExecBuilder} creation process
 * based on dataset and context.
 *
 * @see QueryExecBuilderFactory
 * @since 6.1.0
 */
public class QueryExecBuilderRegistry {
    private static final Logger logger = LoggerFactory.getLogger(QueryExecBuilderRegistry.class);

    List<QueryExecBuilderFactory> factories = Collections.synchronizedList(new ArrayList<>());

    // Singleton
    private static QueryExecBuilderRegistry registry;
    static { init(); }

    static public QueryExecBuilderRegistry get() {
        return registry;
    }

    /** If there is a QueryExecBuilderRegistry in the context then return it otherwise yield the global instance */
    static public QueryExecBuilderRegistry chooseRegistry(Context context) {
        QueryExecBuilderRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the QueryExecBuilderRegistry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public QueryExecBuilderRegistry get(Context context) {
        QueryExecBuilderRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryQueryExecBuilders);
        return result;
    }

    static public void set(Context context, QueryExecBuilderRegistry registry) {
        context.set(ARQConstants.registryQueryExecBuilders, registry);
    }

    public QueryExecBuilderRegistry copy() {
        QueryExecBuilderRegistry result = new QueryExecBuilderRegistry();
        result.factories.addAll(factories);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static QueryExecBuilderRegistry copyFrom(Context context) {
        QueryExecBuilderRegistry tmp = get(context);
        QueryExecBuilderRegistry result = tmp != null
                ? tmp.copy()
                : new QueryExecBuilderRegistry();
        return result;
    }

    public QueryExecBuilderRegistry() { }

    private static void init() {
        registry = new QueryExecBuilderRegistry();

        registry.add(QueryExecBuilderFactoryMain.get());
    }

    // ----- Query -----

    /** Add a QueryExecBuilderFactory to the default registry. */
    public static void addFactory(QueryExecBuilderFactory f) { get().add(f); }

    /** Add a QueryExecBuilderFactory. */
    public void add(QueryExecBuilderFactory f) {
        // Add to low end so that newer factories are tried first
        factories.add(0, f);
    }

    /** Remove a QueryExecBuilderFactory from the default registry. */
    public static void removeFactory(QueryExecBuilderFactory f)  { get().remove(f); }

    /** Remove a QueryExecBuilderFactory. */
    public void remove(QueryExecBuilderFactory f)  { factories.remove(f); }

    /** Allow <b>careful</b> manipulation of the factories list */
    public List<QueryExecBuilderFactory> factories() { return factories; }

    /** Check whether a QueryExecBuilderFactory is registered in the default registry. */
    public static boolean containsFactory(QueryExecBuilderFactory f) { return get().contains(f); }

    /** Check whether a QueryExecBuilderFactory is already registered. */
    public boolean contains(QueryExecBuilderFactory f) { return factories.contains(f); }

    /**
     * Locate a suitable factory from the default registry.
     *
     * @return A QueryExecBuilderFactory or null if none accept the request
     */
    public static QueryExecBuilderFactory findFactory(DatasetGraph dataset, Context context) {
        Context cxt = (context != null) ? context : Context.fromDataset(dataset);
        QueryExecBuilderRegistry registry = chooseRegistry(cxt);
        QueryExecBuilderFactory factory = registry.find(dataset, context);
        return factory;
    }

    /**
     * Locate a suitable factory for this dataset and context.
     *
     * Registry lookup will use the provided context if non−null.
     * Otherwise, the dataset's context is used instead.
     *
     * @return A QueryExecBuilderFactory or null if none accept the request
     */
    public QueryExecBuilderFactory find(DatasetGraph dsg, Context cxt) {
        QueryExecBuilderFactory result = null;
        int i = 0;
        int n = factories.size();
        for (QueryExecBuilderFactory factory : factories) {
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

    public static QueryExecBuilder newQueryExecBuilder(DatasetGraph dsg) {
        return newQueryExecBuilder(dsg, null);
    }

    public static QueryExecBuilder newQueryExecBuilder(DatasetGraph dsg, Context context) {
        QueryExecBuilderFactory factory = findFactory(dsg, context);
        if (factory == null) {
            throw new NoSuchElementException("No QueryExecBuilderFactory accepted dataset " + ObjectUtils.identityToString(dsg));
        }

        QueryExecBuilder builder = factory.create(dsg, context);
        if (builder == null) {
            throw new ARQInternalErrorException("QueryExecBuilderFactory returned null: " + ObjectUtils.identityToString(factory));
        }
        return builder;
    }

    /**
     * Utility to derive a short class label for logging.
     * Returns the simple name if available.
     * Anonymous inner classes (whose simple name is an empty string)
     * become "ParentClass.X" with X some integer.
     */
    static String getClassLabel(Class<?> cls) {
        String name = cls.getSimpleName();
        if (name == null || name.isBlank()) {
            // Returns inner classes as Parent.Child
            name = ClassUtils.getShortClassName(cls);
        }
        return name;
    }
}
