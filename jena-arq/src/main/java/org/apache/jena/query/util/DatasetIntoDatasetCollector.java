package org.apache.jena.query.util;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static org.apache.jena.system.Txn.executeRead;
import static org.apache.jena.system.Txn.executeWrite;

import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.query.Dataset;

public class DatasetIntoDatasetCollector implements DatasetCollector<Dataset> {

    @Override
    public BiConsumer<Dataset, Dataset> accumulator() {
        return (d1, d2) -> {
            d1.getDefaultModel().add(d2.getDefaultModel());
            d2.listNames().forEachRemaining(name -> d1.getNamedModel(name).add(d2.getNamedModel(name)));
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
    }

    public static class ConcurrentStatementIntoModelCollector extends DatasetIntoDatasetCollector {

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> executeRead(d2, () -> executeWrite(d1, () -> super.accumulator().accept(d1, d2)));
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(UNORDERED, IDENTITY_FINISH, CONCURRENT);
        }
    }
}
