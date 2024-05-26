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

package org.apache.jena.sparql.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.util.Context;


public class QueryEngineRegistry
{
    List<QueryEngineFactory> factories = new ArrayList<>();

    // Singleton
    private static QueryEngineRegistry registry;
    static { init(); }

    static public QueryEngineRegistry get()
    {
        return registry;
    }

    /** If there is a registry in the context then return it otherwise yield the global instance */
    static public QueryEngineRegistry chooseRegistry(Context context)
    {
        QueryEngineRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the query engine registry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public QueryEngineRegistry get(Context context)
    {
        QueryEngineRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryQueryEngines);
        return result;
    }

    static public void set(Context context, QueryEngineRegistry registry)
    {
        context.set(ARQConstants.registryQueryEngines, registry);
    }

    public QueryEngineRegistry copy() {
        QueryEngineRegistry result = new QueryEngineRegistry();
        result.factories.addAll(factories);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static QueryEngineRegistry copyFrom(Context context) {
        QueryEngineRegistry tmp = get(context);
        QueryEngineRegistry result = tmp != null
                ? tmp.copy()
                : new QueryEngineRegistry();

        return result;
    }

    public QueryEngineRegistry() { }

    private static void init()
    {
        registry = new QueryEngineRegistry();
        registry.add(QueryEngineMain.getFactory());
        registry.add(QueryEngineFactoryWrapper.get());
    }

    /** Locate a suitable factory for this query and dataset from the default registry
     *
     * @param query   Query
     * @param dataset Dataset
     * @return A QueryExecutionFactory or null if none accept the request
     */

    public static QueryEngineFactory findFactory(Query query, DatasetGraph dataset, Context context)
    { return chooseRegistry(context).find(query, dataset, context); }

    /** Locate a suitable factory for this algebra expression
     *  and dataset from the default registry
     *
     * @param op   Algebra expression
     * @param dataset DatasetGraph
     * @param context
     * @return A QueryExecutionFactory or null if none accept the request
     */

    public static QueryEngineFactory findFactory(Op op, DatasetGraph dataset, Context context)
    { return chooseRegistry(context).find(op, dataset, context); }

    /** Locate a suitable factory for this query and dataset
     *
     * @param query   Query
     * @param dataset Dataset
     * @return A QueryExecutionFactory or null if none accept the request
     */

    public QueryEngineFactory find(Query query, DatasetGraph dataset)
    { return find(query, dataset, null); }

    /** Locate a suitable factory for this query and dataset
     *
     * @param query   Query
     * @param dataset Dataset
     * @return A QueryExecutionFactory or null if none accept the request
     */

    public QueryEngineFactory find(Query query, DatasetGraph dataset, Context context) {
        for ( QueryEngineFactory f : factories ) {
            if ( f.accept(query, dataset, context) ) {
                return f;
            }
        }
        return null;
    }

    /** Locate a suitable factory for this algebra expression and dataset
     *
     * @param op   Algebra expression
     * @param dataset DatasetGraph
     * @param context
     *
     * @return A QueryExecutionFactory or null if none accept the request
     */

    public QueryEngineFactory find(Op op, DatasetGraph dataset, Context context) {
        for ( QueryEngineFactory f : factories ) {
            if ( f.accept(op, dataset, context) ) {
                return f;
            }
        }
        return null;
    }

    /** Add a QueryExecutionFactory to the default registry */
    public static void addFactory(QueryEngineFactory f) { get().add(f); }

    /** Add a QueryExecutionFactory */
    public void add(QueryEngineFactory f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f);
    }

    /** Remove a QueryExecutionFactory */
    public static void removeFactory(QueryEngineFactory f)  { get().remove(f); }

    /** Remove a QueryExecutionFactory */
    public void remove(QueryEngineFactory f)  { factories.remove(f); }

    /** Allow <b>careful</b> manipulation of the factories list */
    public List<QueryEngineFactory> factories() { return factories; }

    /** Check whether a query engine factory is already registered in the default registry*/
    public static boolean containsFactory(QueryEngineFactory f) { return get().contains(f); }

    /** Check whether a query engine factory is already registered */
    public boolean contains(QueryEngineFactory f) { return factories.contains(f); }
}
