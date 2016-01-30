package org.apache.jena.atlas.lib.tuple;

/**
 * A four-argument consumer in which all arguments are of the same type. Unlike most other functional interfaces,
 * {@code TConsumer4} is expected to operate via side-effects.
 *
 * @param <X> the type of all arguments
 */
@FunctionalInterface
public interface TConsumer4<X> {

    void accept(X x1, X x2, X x3, X x4);

}