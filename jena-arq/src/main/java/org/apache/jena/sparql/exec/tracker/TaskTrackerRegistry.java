package org.apache.jena.sparql.exec.tracker;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME Rename to event broker? Both listener and source.
public class TaskTrackerRegistry
    extends TaskEventSource
    implements TaskListener<BasicTaskExec>
{
    private Map<TaskListener<?>, Runnable> registrations = new ConcurrentHashMap<>();

    public void connect(TaskTrackerRegistry upstream) {
        Runnable runnable = upstream.addListener(BasicTaskExec.class, this);
        if (runnable != null) {
            registrations.put(upstream, () -> {
                runnable.run();
                registrations.remove(upstream);
            });
        }
    }

    public void disconnect() {
        registrations.values().forEach(Runnable::run);
    }


    public static QueryExec track(QueryExec queryExec) {
        Context cxt = queryExec.getContext();
        return track(cxt, queryExec);
    }

    /**
     * If there is a taskTracker in the context then return a {@link QueryExecTask}.
     * Otherwise return the provided query exec.
     */
    public static QueryExec track(Context cxt, QueryExec queryExec) {
        TaskTrackerRegistry registry = get(cxt);
        QueryExec result = (registry == null)
            ? queryExec
            : track(queryExec, registry);
        return result;
    }

    public static QueryExecTask track(QueryExec queryExec, TaskListener<? super QueryExecTask> listener) {
        Objects.requireNonNull(queryExec);
        Objects.requireNonNull(listener);
        return new QueryExecTask(queryExec, listener);
    }

    public static UpdateExec track(UpdateExec updateExec) {
        Context cxt = updateExec.getContext();
        return track(cxt, updateExec);
    }

    /**
     * If there is a taskTracker in the context then return a {@link QueryExecTask}.
     * Otherwise return the provided query exec.
     */
    public static UpdateExec track(Context cxt, UpdateExec updateExec) {
        TaskTrackerRegistry registry = get(cxt);
        return track(registry, updateExec);
    }

    public static UpdateExec track(TaskTrackerRegistry tracker, UpdateExec updateExec) {
        UpdateExec result = (tracker == null)
            ? updateExec
            : track(updateExec, tracker);
        return result;
    }

    public static UpdateExecTask track(UpdateExec updateExec, TaskListener<? super UpdateExecTask> listener) {
        Objects.requireNonNull(updateExec);
        Objects.requireNonNull(listener);
        return new UpdateExecTask(updateExec, listener);
    }

    private static final Logger logger = LoggerFactory.getLogger(TaskTrackerRegistry.class);

    public static final Symbol symTaskTrackerRegistry = SystemARQ.allocSymbol("taskTrackerRegistry");

    public static TaskTrackerRegistry get(DatasetGraph dsg) {
        return dsg == null ? null : get(dsg.getContext());
    }

    public static TaskTrackerRegistry get(Context context) {
        return context == null ? null : context.get(symTaskTrackerRegistry);
    }

    public static TaskTrackerRegistry getOrSet(Context context) {
        TaskTrackerRegistry result = context == null ? null : context.computeIfAbsent(symTaskTrackerRegistry, sym -> new TaskTrackerRegistry());
        return result;
    }

    public static TaskTrackerRegistry require(Context context) {
        TaskTrackerRegistry result = get(context);
        if (result == null) {
            throw new RuntimeException("No exec listener in context.");
        }
        return result;
    }

    @Override
    public void onStateChange(BasicTaskExec task) {
        advertiseStateChange(task);
    }
}

    // --- ARQ Integration ---

    // Is there need for a global instance? Perhaps not.

    // Singleton
//    private static TaskTrackerRegistry registry;
//
//    static { init(); }
//
//    private static void init()
//    {
//        registry = new TaskTrackerRegistry();
//    }
//
//    static public TaskTrackerRegistry get()
//    {
//        return registry;
//    }

    /** If there is a registry in the context then return it otherwise yield the global instance */
//    static public TaskTrackerRegistry chooseRegistry(Context context)
//    {
//        TaskTrackerRegistry result = get(context);
//        if (result == null) {
//            result = get();
//        }
//        return result;
//    }

//    public Closeable addQueryExecListener(TaskListener<?> listener) {
//        return addQueryExecListener(QueryExec.class, new QueryExecListenerUnsafeAdapter(listener));
//    }


//
//    class QueryExecListenerUnsafeAdapter
//        implements QueryExecListener<QueryExec>
//    {
//        @SuppressWarnings("rawtypes")
//        protected QueryExecListener delegate;
//
//        public QueryExecListenerUnsafeAdapter(QueryExecListener<?> delegate) {
//            super();
//            this.delegate = delegate;
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public void onQueryExecStart(QueryExec queryExec) {
//            delegate.onQueryExecStart(queryExec);
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public void onQueryExecFinish(QueryExec queryExec) {
//            delegate.onQueryExecFinish(queryExec);
//        }
//    }
//
//
//
//    protected Map<UpdateExecListener<?>, UpdateExecListener<UpdateExec>> updateExecListenersByType = new ConcurrentSkipListMap<>();
//
//    public <Y extends UpdateExec> Closeable addListener(Class<Y> clz, UpdateExecListener<? super Y> listener) {
//        UpdateExecListenerTypeAdapter<Y> adapter = new UpdateExecListenerTypeAdapter<>(clz, listener);
//        updateExecListenersByType.put(listener, adapter);
//        return () -> updateExecListenersByType.remove(listener);
//    }
//
//    @Override
//    public void onUpdateExecStart(UpdateExec queryExec) {
//        for (UpdateExecListener<UpdateExec> listener : updateExecListenersByType.values()) {
//            try {
//                listener.onUpdateExecStart(queryExec);
//            } catch (Throwable t) {
//                System.out.println("Failure while notifying start listener.");
//            }
//        }
//    }
//
//    @Override
//    public void onUpdateExecFinish(UpdateExec queryExec) {
//        for (UpdateExecListener<UpdateExec> listener : updateExecListenersByType.values()) {
//            try {
//                listener.onUpdateExecFinish(queryExec);
//            } catch (Throwable t) {
//                System.out.println("Failure while notifying finish listener.");
//            }
//        }
//    }
//
//    class UpdateExecListenerTypeAdapter<Y extends UpdateExec>
//        implements UpdateExecListener<UpdateExec>
//    {
//        protected Class<Y> clz;
//        protected UpdateExecListener<? super Y> delegate;
//
//        public UpdateExecListenerTypeAdapter(Class<Y> clz, UpdateExecListener<? super Y> delegate) {
//            super();
//            this.clz = clz;
//            this.delegate = delegate;
//        }
//
//        @Override
//        public void onUpdateExecStart(UpdateExec queryExec) {
//            if (clz.isInstance(queryExec)) {
//                Y obj = clz.cast(queryExec);
//                delegate.onUpdateExecStart(obj);
//            }
//        }
//
//        @Override
//        public void onUpdateExecFinish(UpdateExec queryExec) {
//            if (clz.isInstance(queryExec)) {
//                Y obj = clz.cast(queryExec);
//                delegate.onUpdateExecFinish(obj);
//            }
//        }
//    }

//
//class IdWrap<T> {
//    protected T ref;
//
//    public static <T> IdWrap<T> of(T ref) {
//      return new IdWrap<>(ref);
//    }
//
//    protected IdWrap(T ref) {
//      super();
//      this.ref = ref;
//    }
//
//    public T get() {
//      return ref;
//    }
//
//    @Override
//    public int hashCode() {
//      return System.identityHashCode(ref);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//      return obj instanceof IdWrap<?> wrapper && wrapper.ref == ref;
//    }
//
//    @Override
//    public String toString() {
//      return Objects.toString(ref);
//    }
//}
