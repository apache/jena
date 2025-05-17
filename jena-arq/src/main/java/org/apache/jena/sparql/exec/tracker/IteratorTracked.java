package org.apache.jena.sparql.exec.tracker;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.jena.atlas.iterator.IteratorWrapper;

/**
 * Iterator wrapper that forwards an encountered exception
 * to a configured destination.
 */
public class IteratorTracked<T>
    extends IteratorWrapper<T>
{
    protected ThrowableTracker tracker;

    public IteratorTracked(Iterator<T> iterator, ThrowableTracker tracker) {
        super(iterator);
        this.tracker = Objects.requireNonNull(tracker);
    }

    @Override
    public boolean hasNext() {
        return trackBoolean(tracker, get()::hasNext);
    }

    @Override
    public T next() {
        return track(tracker, get()::next);
    }

    public static boolean trackBoolean(ThrowableTracker tracker, BooleanSupplier action) {
        try {
            boolean result = action.getAsBoolean();
            return result;
        } catch (Throwable t) {
            tracker.report(t);
            t.addSuppressed(new RuntimeException("Error during hasNext."));
            throw t;
        }
    }

    public static <T> T track(ThrowableTracker tracker, Supplier<T> action) {
        try {
            T result = action.get();
            return result;
        } catch (Throwable t) {
            tracker.report(t);
            t.addSuppressed(new RuntimeException("Error during hasNext."));
            throw t;
        }
    }
}
