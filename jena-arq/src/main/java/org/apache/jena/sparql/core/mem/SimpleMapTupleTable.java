package org.apache.jena.sparql.core.mem;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;

public abstract class SimpleMapTupleTable<Tail, TupleType, ConsumerType>
        extends OrderedTupleTable<TupleType, ConsumerType> implements TupleTable<TupleType> {

    protected final Map<Node, Tail> table = new HashMap<>();

    public SimpleMapTupleTable(final TupleMap order) {
        super(order);
    }

    @Override
    public void clear() {
        table.clear();
    }

    @Override
    public void begin(final ReadWrite readWrite) {
        // NOOP
    }

    @Override
    public void commit() {
        // NOOP
    }

    @Override
    public void abort() {
        // NOOP
    }

    @Override
    public void end() {
        // NOOP
    }
}
