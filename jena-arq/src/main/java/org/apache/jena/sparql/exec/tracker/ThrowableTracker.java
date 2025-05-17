package org.apache.jena.sparql.exec.tracker;

import java.util.Iterator;

public interface ThrowableTracker {
    void report(Throwable throwable);
    Iterator<Throwable> getThrowables();

    default Throwable getFirstThrowable() {
        Iterator<Throwable> it = getThrowables();
        Throwable result = it.hasNext() ? it.next() : null;
        return result;
    }
}
