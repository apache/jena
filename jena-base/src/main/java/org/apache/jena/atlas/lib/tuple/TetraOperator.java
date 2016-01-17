package org.apache.jena.atlas.lib.tuple;

import org.apache.jena.atlas.function.TetraFunction;

/**
 * A specialization of {@link TetraFunction} in which all arguments are of the same type.
 *
 * @param <X> the type of all arguments
 * @param <Z> the type of the result of the operation
 */
@FunctionalInterface
public interface TetraOperator<X, Z> extends TetraFunction<X, X, X, X, Z> {}