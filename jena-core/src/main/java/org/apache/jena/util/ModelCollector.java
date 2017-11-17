package org.apache.jena.util;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector.UnorderedIdentityFinishCollector;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public abstract class ModelCollector implements UnorderedIdentityFinishCollector<Model, Model> {

    @Override
    public Supplier<Model> supplier() {
        return ModelFactory::createDefaultModel;
    }
    
    public ConcurrentModelCollector concurrent() {
        return new ConcurrentModelCollector(this);
    }

    public static class ConcurrentModelCollector extends ModelCollector
            implements ConcurrentUnorderedIdentityFinishCollector<Model, Model> {

        private final ModelCollector collector;

        public ConcurrentModelCollector(ModelCollector col) {
            this.collector = col;
        }

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return (m1, m2) -> m2.executeInTxn(() -> m1.executeInTxn(() -> collector.accumulator().accept(m1, m2)));
        }

        @Override
        public BinaryOperator<Model> combiner() {
            return collector.combiner();
        }
    }

    public static class UnionModelCollector extends ModelCollector {

        @Override
        public BinaryOperator<Model> combiner() {
            return ModelFactory::createUnion;
        }

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return Model::add;
        }
    }

    public static class IntersectionModelCollector extends ModelCollector {

        @Override
        public BinaryOperator<Model> combiner() {
            return Model::intersection;
        }

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return (m1, m2) -> m1.remove(m1.difference(m2));
        }
    }
}
