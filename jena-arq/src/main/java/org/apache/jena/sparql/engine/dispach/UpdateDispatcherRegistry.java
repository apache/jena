package org.apache.jena.sparql.engine.dispach;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.ChainingUpdateDispatcherMain;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.tracker.ChainingUpdateDispatcherExecTracker;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class UpdateDispatcherRegistry
{
    List<ChainingUpdateDispatcher> factories = new ArrayList<>() ;

    // Singleton
    private static UpdateDispatcherRegistry registry ;
    static { init() ; }

    public List<ChainingUpdateDispatcher> getFactories() {
        return factories;
    }

    static public UpdateDispatcherRegistry get()
    {
        return registry;
    }

    private UpdateDispatcherRegistry() { }

    private static void init()
    {
        registry = new UpdateDispatcherRegistry() ;
        registry.add(new ChainingUpdateDispatcherMain()) ;
        registry.add(new ChainingUpdateDispatcherExecTracker()) ;
    }

    /** If there is a registry in the context then return it otherwise yield the global instance */
    static public UpdateDispatcherRegistry chooseRegistry(Context context)
    {
        UpdateDispatcherRegistry result = get(context);
        if (result == null) {
            result = get();
        }
        return result;
    }

    /** Get the update dispatch registry from the context or null if there is none.
     *  Returns null if the context is null. */
    static public UpdateDispatcherRegistry get(Context context)
    {
        UpdateDispatcherRegistry result = context == null
                ? null
                : context.get(ARQConstants.registryUpdateDispatchers);
        return result;
    }

    static public void set(Context context, UpdateDispatcherRegistry registry)
    {
        context.set(ARQConstants.registryUpdateDispatchers, registry);
    }

    /** Add a UpdateDispatchFactory to the default registry */
    public static void addFactory(ChainingUpdateDispatcher f) { get().add(f) ; }

    /** Add a UpdateDispatchFactory */
    public void add(ChainingUpdateDispatcher f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f) ;
    }

    /** Remove a UpdateDispatchFactory */
    public static void removeFactory(ChainingUpdateDispatcher f)  { get().remove(f) ; }

    /** Remove a UpdateDispatchFactory */
    public void remove(ChainingUpdateDispatcher f)  { factories.remove(f) ; }

    /** Allow <b>careful</b> manipulation of the factories list */
    public List<ChainingUpdateDispatcher> factories() { return factories ; }

    /** Check whether an UpdateDispatchFactory is already registered in the default registry*/
    public static boolean containsFactory(ChainingUpdateDispatcher f) { return get().contains(f) ; }

    /** Check whether an UpdateDispatchFactory is already registered */
    public boolean contains(ChainingUpdateDispatcher f) { return factories.contains(f) ; }

    public static UpdateExec exec(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context) {
        UpdateDispatcherRegistry registry = chooseRegistry(context);
        UpdateDispatcher updateDispatcher = new UpdateDispatcherOverRegistry(registry);
        UpdateExec uExec = updateDispatcher.create(updateRequest, dsg, initialBinding, context);
        return uExec;
    }

    public static UpdateExec exec(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context) {
        UpdateDispatcherRegistry registry = chooseRegistry(context);
        UpdateDispatcher updateDispatcher = new UpdateDispatcherOverRegistry(registry);
        UpdateExec uExec = updateDispatcher.create(updateRequestString, dsg, initialBinding, context);
        return uExec;
    }
}
