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

package org.apache.jena.sparql.engine.dispatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.ChainingQueryDispatcherMain;
import org.apache.jena.sparql.exec.ChainingUpdateDispatcherMain;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.update.UpdateRequest;

/**
 * The SparqlDispatcherRegistry provides a plugin system for
 * how to execute SPARQL statements against DatasetGraphs.
 */
public class SparqlDispatcherRegistry
{
    List<ChainingQueryDispatcher> queryDispatchers = Collections.synchronizedList(new ArrayList<>());
    List<ChainingUpdateDispatcher> updateDispatchers = Collections.synchronizedList(new ArrayList<>());

    // Singleton
    private static SparqlDispatcherRegistry registry;
    static { init(); }

    static public SparqlDispatcherRegistry get()
    {
        return registry;
    }

    public List<ChainingQueryDispatcher> getQueryDispatchers() {
        return queryDispatchers;
    }

    public List<ChainingUpdateDispatcher> getUpdateDispatchers() {
        return updateDispatchers;
    }

    /** If there is a registry in the context then return it otherwise yield the global instance */
    static public SparqlDispatcherRegistry chooseRegistry(Context context)
    {
        SparqlDispatcherRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the query engine registry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public SparqlDispatcherRegistry get(Context context)
    {
        SparqlDispatcherRegistry result = context == null
                ? null
                : context.get(ARQConstants.registrySparqlDispatchers);
        return result;
    }

    static public void set(Context context, SparqlDispatcherRegistry registry)
    {
        context.set(ARQConstants.registrySparqlDispatchers, registry);
    }

    public SparqlDispatcherRegistry copy() {
        SparqlDispatcherRegistry result = new SparqlDispatcherRegistry();
        result.queryDispatchers.addAll(queryDispatchers);
        result.updateDispatchers.addAll(updateDispatchers);
        return result;
    }

    /** Create a copy of the registry from the context or return a new instance */
    public static SparqlDispatcherRegistry copyFrom(Context context) {
        SparqlDispatcherRegistry tmp = get(context);
        SparqlDispatcherRegistry result = tmp != null
                ? tmp.copy()
                : new SparqlDispatcherRegistry();

        return result;
    }

    public SparqlDispatcherRegistry() { }

    private static void init()
    {
        registry = new SparqlDispatcherRegistry();

        registry.add(new ChainingQueryDispatcherMain());
        registry.add(new ChainingUpdateDispatcherMain());
    }

    // ----- Query -----

    /** Add a query dispatcher to the default registry */
    public static void addDispatcher(ChainingQueryDispatcher f) { get().add(f); }

    /** Add a query dispatcher */
    public void add(ChainingQueryDispatcher f)
    {
        // Add to low end so that newer factories are tried first
        queryDispatchers.add(0, f);
    }

    /** Remove a query dispatcher */
    public static void removeDispatcher(ChainingQueryDispatcher f)  { get().remove(f); }

    /** Remove a query dispatcher */
    public void remove(ChainingQueryDispatcher f)  { queryDispatchers.remove(f); }

    /** Check whether a query dispatcher is already registered in the default registry */
    public static boolean containsFactory(ChainingQueryDispatcher f) { return get().contains(f); }

    /** Check whether a query dispatcher is already registered */
    public boolean contains(ChainingQueryDispatcher f) { return queryDispatchers.contains(f); }

    public static QueryExec exec(Query query, DatasetGraph dsg, Binding initialBinding, Context context) {
        SparqlDispatcherRegistry registry = chooseRegistry(context);
        QueryDispatcher queryDispatcher = new QueryDispatcherOverRegistry(registry);
        QueryExec qExec = queryDispatcher.create(query, dsg, initialBinding, context);
        return qExec;
    }

    public static QueryExec exec(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context) {
        SparqlDispatcherRegistry registry = chooseRegistry(context);
        QueryDispatcher queryDispatcher = new QueryDispatcherOverRegistry(registry);
        QueryExec qExec = queryDispatcher.create(queryString, syntax, dsg, initialBinding, context);
        return qExec;
    }

    // ----- Update -----

    /** Add an update dispatcher to the default registry */
    public static void addDispatcher(ChainingUpdateDispatcher f) { get().add(f); }

    /** Add an update dispatcher */
    public void add(ChainingUpdateDispatcher f)
    {
        // Add to low end so that newer factories are tried first
        updateDispatchers.add(0, f);
    }

    /** Remove an update dispatcher */
    public static void removeDispatcher(ChainingUpdateDispatcher f)  { get().remove(f); }

    /** Remove an update dispatcher */
    public void remove(ChainingUpdateDispatcher f)  { updateDispatchers.remove(f); }

    /** Check whether an update dispatcher is already registered in the default registry */
    public static boolean containsDispatcher(ChainingUpdateDispatcher f) { return get().contains(f); }

    /** Check whether an update dispatcher is already registered */
    public boolean contains(ChainingUpdateDispatcher f) { return updateDispatchers.contains(f); }

    public static UpdateExec exec(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context) {
        SparqlDispatcherRegistry registry = chooseRegistry(context);
        UpdateDispatcher updateDispatcher = new UpdateDispatcherOverRegistry(registry);
        UpdateExec uExec = updateDispatcher.create(updateRequest, dsg, initialBinding, context);
        return uExec;
    }

    public static UpdateExec exec(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context) {
        SparqlDispatcherRegistry registry = chooseRegistry(context);
        UpdateDispatcher updateDispatcher = new UpdateDispatcherOverRegistry(registry);
        UpdateExec uExec = updateDispatcher.create(updateRequestString, dsg, initialBinding, context);
        return uExec;
    }

    // ----- Parse Check -----

    public static void setParseCheck(Context cxt, Boolean value) {
        cxt.set(ARQConstants.parseCheck, value);
    }

    public static Boolean getParseCheck(DatasetGraph dsg) {
        return dsg == null ? null : getParseCheck(dsg.getContext());
    }

    public static Boolean getParseCheck(Context cxt) {
        Boolean result = cxt == null ? null : cxt.get(ARQConstants.parseCheck);
        return result;
    }

    public static Boolean getParseCheck(ContextAccumulator cxtAcc) {
        Boolean result = cxtAcc == null ? null : cxtAcc.get(ARQConstants.parseCheck);
        return result;
    }

    public static boolean effectiveParseCheck(Boolean parseCheck, Context cxt) {
        boolean result = parseCheck != null
            ? parseCheck.booleanValue()
            : !Boolean.FALSE.equals(getParseCheck(cxt));
        return result;
    }

    public static boolean effectiveParseCheck(Boolean parseCheck, ContextAccumulator cxtAcc) {
        boolean result = parseCheck != null
            ? parseCheck.booleanValue()
            : !Boolean.FALSE.equals(getParseCheck(cxtAcc));
        return result;
    }
}
