package org.apache.jena.sparql.exec.tracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

// FIXME Rename to event broker, hub, endpoint? Both listener and source.
public class TaskTrackerRegistry
    extends TaskEventSource
    implements TaskListener<BasicTaskExec>
{
    private Map<TaskListener<?>, Runnable> registrations = new ConcurrentHashMap<>();

    public void connect(TaskTrackerRegistry upstream) {
        Runnable runnable = upstream.addListener(BasicTaskExec.class, this);
        registrations.put(upstream, () -> {
            runnable.run();
            registrations.remove(upstream);
        });
    }

    @Override
    public void onStateChange(BasicTaskExec task) {
        advertiseStateChange(task);
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
            : QueryExecTask.create(queryExec, registry);
        return result;
    }

//    public static QueryExecTask track(QueryExec queryExec, TaskListener<? super QueryExecTask> listener) {
//        Objects.requireNonNull(queryExec);
//        Objects.requireNonNull(listener);
//        return QueryExecTask.create(queryExec, listener);
//    }

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
            : UpdateExecTask.create(updateExec, tracker);
        return result;
    }

    // ----- ARQ Integration -----

    // private static final Logger logger = LoggerFactory.getLogger(TaskTrackerRegistry.class);

    public static final Symbol symTaskTrackerRegistry = SystemARQ.allocSymbol("taskTrackerRegistry");

    public static TaskTrackerRegistry get(DatasetGraph dsg) {
        return dsg == null ? null : get(dsg.getContext());
    }

    public static TaskTrackerRegistry get(Context context) {
        return context == null ? null : context.get(symTaskTrackerRegistry);
    }

    public static void remove(Context context) {
        if (context != null) {
            context.remove(symTaskTrackerRegistry);
        }
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
}
