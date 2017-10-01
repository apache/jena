package org.apache.jena.query.util;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static org.apache.jena.system.Txn.executeWrite;

import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

public class ModelIntoDatasetCollector implements DatasetCollector<Model> {

    private String graphName;

    public ModelIntoDatasetCollector(String graphName) {
        this.graphName = graphName;
    }

    /**
     * Collects models into the default graph.
     */
    public ModelIntoDatasetCollector() {
        this(Quad.defaultGraphIRI.getURI());
    }

    @Override
    public BiConsumer<Dataset, Model> accumulator() {
        return (d, m) -> d.getNamedModel(graphName).add(m);
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
    }

    public static class ConcurrentStatementIntoModelCollector extends ModelIntoDatasetCollector {

        @Override
        public BiConsumer<Dataset, Model> accumulator() {
            return (d, m) -> m.executeInTxn(() -> executeWrite(d, () -> super.accumulator().accept(d, m)));
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(UNORDERED, IDENTITY_FINISH, CONCURRENT);
        }
    }
}
