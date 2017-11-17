package org.apache.jena.atlas.lib;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;

public interface IdentityFinishCollector<T, A> extends Collector<T, A, A> {

    static Set<Characteristics> CHARACTERISTICS = ImmutableSet.of(IDENTITY_FINISH);

    @Override
    default Function<A, A> finisher() {
        return Function.identity();
    }

    @Override
    default Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }

    public interface UnorderedIdentityFinishCollector<T, A> extends IdentityFinishCollector<T, A> {

        static Set<Characteristics> CHARACTERISTICS = ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
    }

    public interface ConcurrentUnorderedIdentityFinishCollector<T, A> extends UnorderedIdentityFinishCollector<T, A> {

        static Set<Characteristics> CHARACTERISTICS = ImmutableSet.of(UNORDERED, IDENTITY_FINISH);

    }
}
