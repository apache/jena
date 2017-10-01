package org.apache.jena.util;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public class StatementIntoModelCollector implements ModelCollector<Statement> {

    @Override
    public BiConsumer<Model, Statement> accumulator() {
        return Model::add;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
    }

    public static class ConcurrentStatementIntoModelCollector extends StatementIntoModelCollector {

        @Override
        public BiConsumer<Model, Statement> accumulator() {
            return (m, s) -> m.executeInTxn(() -> m.add(s));
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(UNORDERED, IDENTITY_FINISH, CONCURRENT);
        }
    }
}
