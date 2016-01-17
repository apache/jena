package org.apache.jena.atlas.lib.tuple;

import org.apache.jena.atlas.function.TetraConsumer;

/**
 * A specialization of {@link TetraConsumer} in which all arguments are of the same type.
 *
 * @param <X> the type of all arguments
 */
@FunctionalInterface
public interface Consumer4<X> extends TetraConsumer<X, X, X, X> {}