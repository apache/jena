package org.apache.jena.sparql.exec.tracker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ThrowableTrackerFirst
    implements ThrowableTracker
{
    protected Throwable throwable = null;

    @Override
    public void report(Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
        // Ignore any throwables after the first
    }

    @Override
    public Iterator<Throwable> getThrowables() {
        return throwable == null ? Collections.emptyIterator() : List.of(throwable).iterator();
    }
}
