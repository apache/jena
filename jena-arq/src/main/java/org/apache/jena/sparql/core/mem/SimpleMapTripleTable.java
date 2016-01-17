package org.apache.jena.sparql.core.mem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.tuple.TriConsumer.Consumer3;
import org.apache.jena.atlas.lib.tuple.TriFunction.TriOperator;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class SimpleMapTripleTable extends SimpleMapTupleTable<Map<Node, Set<Node>>, Triple, Consumer3<Node>>
        implements TripleTable {

    public SimpleMapTripleTable(final TupleMap order) {
        super(order);
    }
    

    @Override
    public void add(final Triple t) {
        map(add()).accept(t);
    }

    @Override
    public void delete(final Triple t) {
        map(delete()).accept(t);
    }

    protected Consumer3<Node> add() {
        return (first, second, third) -> table.getOrDefault(first, new HashMap<>())
                .getOrDefault(second, new HashSet<>()).add(third);
    }

    protected Consumer3<Node> delete() {
        return (first, second, third) -> {
            if (table.containsKey(first)) {
                final Map<Node, Set<Node>> twotuples = table.get(first);
                if (twotuples.containsKey(second)) {
                    final Set<Node> thirds = twotuples.get(second);
                    if (thirds.remove(third) && thirds.isEmpty()) {
                        twotuples.remove(second);
                        if (twotuples.isEmpty()) table.remove(first);
                    }
                }
            }
        };
    }

    @Override
    public Stream<Triple> find(final Node s, final Node p, final Node o) {
        return map(find).apply(s, p, o);
    }

    private TriOperator<Node, Stream<Triple>> find = (first, second, third) -> {
        if (first != null && first.isConcrete()) {
            // concrete value for first slot
            final Map<Node, Set<Node>> twotuples = table.get(first);
            if (second != null && second.isConcrete()) {
                // concrete value for second slot
                if (third != null && third.isConcrete())
                    // concrete value for third slot
                    return Stream.of(unmap(first, second, third));
                // wildcard for third slot
                return twotuples.get(second).stream().map(slot3 -> unmap(first, second, slot3));
            }
            // wildcard for second slot
            return twotuples.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(slot3 -> unmap(first, e.getKey(), slot3)));
        }
        // wildcard for first slot
        return table.entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()
                .flatMap(e1 -> e1.getValue().stream().map(slot3 -> unmap(e.getKey(), e1.getKey(), slot3))));
    };
}
