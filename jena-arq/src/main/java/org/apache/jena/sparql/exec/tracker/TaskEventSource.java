package org.apache.jena.sparql.exec.tracker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskEventSource {
    private static final Logger logger = LoggerFactory.getLogger(TaskEventSource.class);

    protected Map<TaskListener<?>, TaskListener<BasicTaskExec>> listenersByType =
            Collections.synchronizedMap(new LinkedHashMap<>())
                // new ConcurrentSkipListMap<>()
                ;

    public <Y extends BasicTaskExec> Runnable addListener(Class<Y> clz, TaskListener<? super Y> listener) {
        TaskListenerTypeAdapter<Y> adapter = new TaskListenerTypeAdapter<>(clz, listener);
        listenersByType.put(listener, adapter);
        return () -> listenersByType.remove(listener);
    }


    public void advertiseStateChange(BasicTaskExec task) {
        for (TaskListener<BasicTaskExec> listener : listenersByType.values()) {
            try {
                listener.onStateChange(task);
            } catch (Throwable t) {
                logger.warn("Failure while notifying listener.", t);
            }
        }
    }

    class TaskListenerTypeAdapter<Y extends BasicTaskExec>
        implements TaskListener<BasicTaskExec>
    {
        protected Class<Y> clz;
        protected TaskListener<? super Y> delegate;

        public TaskListenerTypeAdapter(Class<Y> clz, TaskListener<? super Y> delegate) {
            super();
            this.clz = clz;
            this.delegate = delegate;
        }

        @Override
        public void onStateChange(BasicTaskExec task) {
            if (clz.isInstance(task)) {
                Y obj = clz.cast(task);
                delegate.onStateChange(obj);
            }
        }
    }
}
