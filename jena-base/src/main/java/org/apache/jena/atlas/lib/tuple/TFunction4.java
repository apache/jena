package org.apache.jena.atlas.lib.tuple;

/**
 * A four-argument function in which all arguments are of the same type.
 *
 * @param <X> the type of all arguments
 * @param <Z> the type of the result of the operation
 */
@FunctionalInterface
public interface TFunction4<X, Z> {
    
    Z apply(X x1, X x2, X x3, X x4);
    
}