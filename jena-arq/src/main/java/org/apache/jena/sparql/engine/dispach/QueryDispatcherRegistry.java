package org.apache.jena.sparql.engine.dispach;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.ChainingQueryDispatcherMain;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.tracker.ChainingQueryDispatcherExecTracker;
import org.apache.jena.sparql.util.Context;

public class QueryDispatcherRegistry
{
    List<ChainingQueryDispatcher> factories = new ArrayList<>();

    // Singleton
    private static QueryDispatcherRegistry registry;
    static { init(); }

    static public QueryDispatcherRegistry get()
    {
        return registry;
    }

    public List<ChainingQueryDispatcher> getFactories() {
        return factories;
    }

    /** If there is a registry in the context then return it otherwise yield the global instance */
    static public QueryDispatcherRegistry chooseRegistry(Context context)
    {
        QueryDispatcherRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the query engine registry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public QueryDispatcherRegistry get(Context context)
    {
        QueryDispatcherRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryQueryDispatchers);
        return result;
    }

    static public void set(Context context, QueryDispatcherRegistry registry)
    {
        context.set(ARQConstants.registryQueryDispatchers, registry);
    }

    public QueryDispatcherRegistry copy() {
        QueryDispatcherRegistry result = new QueryDispatcherRegistry();
        result.factories.addAll(factories);
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

    private static void init()
    {
        registry = new QueryDispatcherRegistry();

        registry.add(new ChainingQueryDispatcherMain());
        registry.add(new ChainingQueryDispatcherExecTracker()) ;
        // registry.add(new QueryDispatchFactoryWrapper());
    }

    /** Add a QueryDispatchFactory to the default registry */
    public static void addFactory(ChainingQueryDispatcher f) { get().add(f); }

    /** Add a QueryDispatchFactory */
    public void add(ChainingQueryDispatcher f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f);
    }

    /** Remove a QueryDispatchFactory */
    public static void removeFactory(ChainingQueryDispatcher f)  { get().remove(f); }

    /** Remove a QueryDispatchFactory */
    public void remove(ChainingQueryDispatcher f)  { factories.remove(f); }

    /** Allow <b>careful</b> manipulation of the factories list */
    public List<ChainingQueryDispatcher> factories() { return factories; }

    /** Check whether a query engine factory is already registered in the default registry*/
    public static boolean containsFactory(ChainingQueryDispatcher f) { return get().contains(f); }

    /** Check whether a query engine factory is already registered */
    public boolean contains(ChainingQueryDispatcher f) { return factories.contains(f); }

    public static QueryExec exec(Query query, DatasetGraph dsg, Binding initialBinding, Context context) {
        QueryDispatcherRegistry registry = chooseRegistry(context);
        QueryDispatcher queryDispatcher = new QueryDispatcherOverRegistry(registry);
        QueryExec qExec = queryDispatcher.create(query, dsg, initialBinding, context);
        return qExec;
    }

    public static QueryExec exec(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context) {
        QueryDispatcherRegistry registry = chooseRegistry(context);
        QueryDispatcher queryDispatcher = new QueryDispatcherOverRegistry(registry);
        QueryExec qExec = queryDispatcher.create(queryString, syntax, dsg, initialBinding, context);
        return qExec;
    }
}
