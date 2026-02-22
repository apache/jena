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

package org.apache.jena.sparql.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

public class SparqlAdapterRegistry {
    List<SparqlAdapterProvider> providers = Collections.synchronizedList(new ArrayList<>());

    // Singleton
    private static SparqlAdapterRegistry registry;
    static { init(); }

    static public SparqlAdapterRegistry get()
    {
        return registry;
    }

    public List<SparqlAdapterProvider> getProviders() {
        return providers;
    }

    /** If there is a registry in the context then return it otherwise yield the global instance */
    static public SparqlAdapterRegistry chooseRegistry(Context context)
    {
        SparqlAdapterRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the query engine registry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public SparqlAdapterRegistry get(Context context)
    {
        SparqlAdapterRegistry result = context == null
                ? null
                : context.get(ARQConstants.registrySparqlDispatchers);
        return result;
    }

    static public void set(Context context, ParseCheckUtils registry)
    {
        context.set(ARQConstants.registrySparqlDispatchers, registry);
    }

    public SparqlAdapterRegistry copy() {
        SparqlAdapterRegistry result = new SparqlAdapterRegistry();
        result.providers.addAll(providers);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static SparqlAdapterRegistry copyFrom(Context context) {
        SparqlAdapterRegistry tmp = get(context);
        SparqlAdapterRegistry result = tmp != null
                ? tmp.copy()
                : new SparqlAdapterRegistry();
        return result;
    }

    public SparqlAdapterRegistry() { }

    private static void init()
    {
        registry = new SparqlAdapterRegistry();

        registry.add(new SparqlAdapterProviderMain());
    }

    // ----- Query -----

    /** Add a link provider to the default registry */
    public static void addProvider(SparqlAdapterProvider f) { get().add(f); }

    /** Add a query dispatcher */
    public void add(SparqlAdapterProvider f)
    {
        // Add to low end so that newer factories are tried first
        providers.add(0, f);
    }

    /** Remove a query dispatcher */
    public static void removeProvider(SparqlAdapterProvider f)  { get().remove(f); }

    /** Remove a query dispatcher */
    public void remove(SparqlAdapterProvider f)  { providers.remove(f); }

    /** Check whether a query dispatcher is already registered in the default registry */
    public static boolean containsFactory(SparqlAdapterProvider f) { return get().contains(f); }

    /** Check whether a query dispatcher is already registered */
    public boolean contains(SparqlAdapterProvider f) { return providers.contains(f); }

    // FIXME Do we need a separate context (originating from builder config) to choose the adapter registry?
    //       Probably yes!
    public static SparqlAdapter adapt(DatasetGraph dsg) {
        /** If the dataset is null then use Jena's ARQ query engine. */
        if (dsg == null) {
            return new SparqlAdapterDefault(dsg);
        }

        /** FIXME Unwrap Graph view over a DatasetGraph? */
//        if (dsg instanceof DatasetGraphOne dsg1) {
//        	dsg1.getBacking()
//        }


        Context cxt = dsg.getContext();
        SparqlAdapterRegistry registry = chooseRegistry(cxt);

        SparqlAdapter result = null;
        for (SparqlAdapterProvider provider : registry.providers) {
            result = provider.adapt(dsg);
            if (result != null) {
                break;
            }
        }
        Objects.requireNonNull(result, "No provider found for " + dsg.getClass());
        return result;
    }
}
