package org.apache.jena.util;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.rdf.model.Model;

public class ModelIntoModelCollector implements ModelCollector<Model> {

    @Override
    public BiConsumer<Model, Model> accumulator() {
        return Model::add;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
    }

    public static class ConcurrentModelIntoModelCollector extends ModelIntoModelCollector {

        @Override
        public BiConsumer<Model, Model> accumulator() {
            return (m1, m2) -> m1.executeInTxn(() -> m2.executeInTxn(() -> super.accumulator().accept(m1, m2)));
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(UNORDERED, IDENTITY_FINISH, CONCURRENT);
        }
    }
}
