package org.apache.jena.sparql.core.mem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.tuple.Consumer4;
import org.apache.jena.atlas.lib.tuple.TetraOperator;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public class SimpleMapQuadTable extends SimpleMapTupleTable<Map<Node, Map<Node, Set<Node>>>, Quad, Consumer4<Node>>
        implements QuadTable {

    public SimpleMapQuadTable(final TupleMap order) {
        super(order);
    }
    

    @Override
    public void add(final Quad q) {
        map(add()).accept(q);
    }

    @Override
    public void delete(final Quad q) {
        map(delete()).accept(q);
    }

    @Override
    protected Consumer4<Node> add() {
        return (first, second, third, fourth) -> {
            table.getOrDefault(first, new HashMap<>()).getOrDefault(second, new HashMap<>())
                    .getOrDefault(third, new HashSet<>()).add(fourth);
        };
    }

    @Override
    protected Consumer4<Node> delete() {
        return (first, second, third, fourth) -> {
            if (table.containsKey(first)) {
                final Map<Node, Map<Node, Set<Node>>> threetuples = table.get(first);
                if (threetuples.containsKey(second)) {
                    final Map<Node, Set<Node>> twotuples = threetuples.get(second);
                    if (twotuples.containsKey(third)) {
                        final Set<Node> fourths = twotuples.get(fourth);
                        if (fourths.remove(fourth) && fourths.isEmpty()) {
                            twotuples.remove(third);
                            if (twotuples.isEmpty()) {
                                threetuples.remove(second);
                                if (threetuples.isEmpty()) table.remove(first);
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return map(find).apply(g, s, p, o);
    }

    private TetraOperator<Node, Stream<Quad>> find = (first, second, third, fourth) -> {
        if (first != null && first.isConcrete()) {
            // concrete value for first slot
            final Map<Node, Map<Node, Set<Node>>> threetuples = table.get(first);
            if (second != null && second.isConcrete()) {
                // concrete value for second slot
                final Map<Node, Set<Node>> twotuples = threetuples.get(second);
                if (third != null && third.isConcrete()) {
                    // concrete value for third slot
                    if (fourth != null && fourth.isConcrete())
                        // concrete value for fourth slot
                        return Stream.of(unmap(first, second, third, fourth));
                    // wildcard for fourth slot
                    return twotuples.get(third).stream().map(slot4 -> unmap(first, second, third, slot4));
                }
                // wildcard for third slot
                return threetuples.get(second).entrySet().stream()
                        .flatMap(e -> e.getValue().stream().map(slot4 -> unmap(first, second, e.getKey(), slot4)));
            }
            // wildcard for second slot
            return threetuples.entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()
                    .flatMap(e1 -> e1.getValue().stream().map(slot4 -> unmap(first, e.getKey(), e1.getKey(), slot4))));
        }
        // wildcard for first slot
        return table.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                        .flatMap(e1 -> e1.getValue().entrySet().stream().flatMap(e2 -> e2.getValue().stream()
                                .map(slot4 -> unmap(e.getKey(), e1.getKey(), e2.getKey(), slot4)))));
    };
}
