package org.apache.jena.atlas.lib;

import java.util.function.Function;
import java.util.stream.Collector;

public interface IdentityFinishCollector<T, A> extends Collector<T, A, A> {

    @Override
    default Function<A, A> finisher() {
        return Function.identity();
    }
}
