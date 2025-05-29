package org.apache.jena.sparql.exec.tracker;

import java.util.List;
import java.util.Objects;

import org.apache.jena.riot.rowset.RowSetWrapper;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

/** RowSetWrapper that tracks any encountered exceptions in the provided tracker. */
public class RowSetTracked
    extends RowSetWrapper
{
    protected ThrowableTracker tracker;

    public RowSetTracked(RowSet other, ThrowableTracker tracker) {
        super(other);
        this.tracker = Objects.requireNonNull(tracker);
    }

    public ThrowableTracker getTracker() {
        return tracker;
    }

    @Override
    public boolean hasNext() {
        return IteratorTracked.trackBoolean(tracker, get()::hasNext);
    }

    @Override
    public Binding next() {
        return IteratorTracked.track(tracker, get()::next);
    }

    @Override
    public List<Var> getResultVars() {
        return IteratorTracked.track(tracker, get()::getResultVars);
    }
}
