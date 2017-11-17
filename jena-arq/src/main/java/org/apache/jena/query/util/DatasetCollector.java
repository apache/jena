package org.apache.jena.query.util;

import static org.apache.jena.atlas.iterator.Iter.filter;
import static org.apache.jena.system.Txn.executeRead;
import static org.apache.jena.system.Txn.executeWrite;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector.UnorderedIdentityFinishCollector;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;

public abstract class DatasetCollector implements UnorderedIdentityFinishCollector<Dataset, Dataset> {

    @Override
    public Supplier<Dataset> supplier() {
        return DatasetFactory::createGeneral;
    }

    public ConcurrentDatasetCollector concurrent() {
        return new ConcurrentDatasetCollector(this);
    }

    /**
     * Use only with {@link Dataset}s that support transactions.
     */
    public static class ConcurrentDatasetCollector extends DatasetCollector
            implements ConcurrentUnorderedIdentityFinishCollector<Dataset, Dataset> {

        private final DatasetCollector collector;

        public ConcurrentDatasetCollector(DatasetCollector col) {
            this.collector = col;
        }

        @Override
        public BinaryOperator<Dataset> combiner() {
            return collector.combiner();
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> executeRead(d2, () -> executeWrite(d1, () -> collector.accumulator().accept(d1, d2)));
        }
    }

    public static class UnionDatasetCollector extends DatasetCollector {

        @Override
        public BinaryOperator<Dataset> combiner() {
            return DatasetLib::union;
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> {
                d1.getDefaultModel().add(d2.getDefaultModel());
                d2.listNames().forEachRemaining(
                        name -> d1.replaceNamedModel(name, d1.getNamedModel(name).union(d2.getNamedModel(name))));
            };
        }
    }

    public static class IntersectionDatasetCollector extends DatasetCollector {

        @Override
        public BinaryOperator<Dataset> combiner() {
            return DatasetLib::intersection;
        }

        @Override
        public BiConsumer<Dataset, Dataset> accumulator() {
            return (d1, d2) -> {
                d1.setDefaultModel(d1.getDefaultModel().intersection(d2.getDefaultModel()));
                filter(d2.listNames(), d1::containsNamedModel).forEachRemaining(name -> {
                    Model intersection = d1.getNamedModel(name).intersection(d2.getNamedModel(name));
                    d1.replaceNamedModel(name, intersection);
                });
            };
        }
    }
}
